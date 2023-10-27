/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.server;

import com.sun.net.httpserver.HttpExchange;
import io.opentelemetry.api.common.Attributes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.RowBounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.spelling.morfologik.suggestions_ordering.SuggestionsOrdererConfig;
import org.languagetool.tools.TelemetryProvider;
import org.languagetool.tools.LtThreadPoolFactory;
import org.languagetool.tools.Tools;
import org.slf4j.MDC;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.languagetool.server.ServerTools.getHttpReferrer;
import static org.languagetool.server.ServerTools.getHttpUserAgent;

/**
 * @since 3.4
 */
@Slf4j
abstract class TextChecker {

  private static final int PINGS_CLEAN_MILLIS = 60 * 1000;  // internal pings database will be cleaned this often
  private static final int PINGS_MAX_SIZE = 5000;
  private static final String SPAN_NAME_PREFIX = "/v2/check-";

  protected abstract void setHeaders(HttpExchange httpExchange);
  protected abstract String getResponse(AnnotatedText text, Language language, DetectedLanguage lang, Language motherTongue, List<CheckResults> matches,
                                        List<RuleMatch> hiddenMatches, String incompleteResultReason, int compactMode, boolean showPremiumHint, JLanguageTool.Mode mode);
  @NotNull
  protected abstract List<String> getPreferredVariants(Map<String, String> parameters);
  protected abstract DetectedLanguage getLanguage(String text, Map<String, String> parameters, List<String> preferredVariants,
                                                  List<String> additionalDetectLangs, List<String> preferredLangs, boolean testMode);
  protected abstract boolean getLanguageAutoDetect(Map<String, String> parameters);
  @NotNull
  protected abstract List<String> getEnabledRuleIds(Map<String, String> parameters);
  @NotNull
  protected abstract List<String> getDisabledRuleIds(Map<String, String> parameters);
    
  protected static final int CONTEXT_SIZE = 40; // characters
  protected static final int NUM_PIPELINES_PER_SETTING = 3; // for prewarming

  protected final HTTPServerConfig config;

  private static final String ENCODING = "UTF-8";
  private static final int CACHE_STATS_PRINT = 500; // print cache stats every n cache requests
  
  private final Map<String,Integer> languageCheckCounts = new HashMap<>();
  private final Queue<Runnable> workQueue;
  private final RequestCounter reqCounter;
  private final LanguageIdentifier languageIdentifier;
  private final ExecutorService executorService;
  private final ResultCache cache;
  private final DatabaseLogger databaseLogger;
  private final Long logServerId;
  private final Random random = new Random();
  private final Set<DatabasePingLogEntry> pings = new HashSet<>();

  private long pingsCleanDateMillis = System.currentTimeMillis();
  PipelinePool pipelinePool; // mocked in test -> package-private / not final

  TextChecker(HTTPServerConfig config, boolean internalServer, Queue<Runnable> workQueue, RequestCounter reqCounter) {
    this.config = config;
    this.workQueue = workQueue;
    this.reqCounter = reqCounter;
    if (config.isLocalApiMode()) {
      this.languageIdentifier = LanguageIdentifierService.INSTANCE.getSimpleLanguageIdentifier(config.preferredLanguages);
    } else {
      this.languageIdentifier = LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(
              0,
              config.getNgramLangIdentData(),
              config.getFasttextBinary(),
              config.getFasttextModel());
    }
    this.executorService = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      LtThreadPoolFactory.TEXT_CHECKER_POOL,
      config.getMaxTextCheckerThreads(), config.getMaxTextCheckerThreads(),
      config.getTextCheckerQueueSize(),
      60L, false, (thread, throwable) -> {
        log.error("Thread: " + thread.getName() + " failed with: " + throwable.getMessage());
      },
      false);

    // set up other pools used by text checker and remote rule
    //Need to use own thread pool, otherwise the text-checker thread-pool will be full very soon
    int remoteRuleCount = 0;
    if (config.getRemoteRulesConfigFile() != null) {
      try (FileInputStream fis = new FileInputStream(config.getRemoteRulesConfigFile())) {
        remoteRuleCount = RemoteRuleConfig.parse(fis).size();
      } catch (IOException e) {
        log.error("Couldn't read RemoteRule configuration", e);
      }
    }
    if (remoteRuleCount > 0) {
      LtThreadPoolFactory.createFixedThreadPoolExecutor(
        LtThreadPoolFactory.REMOTE_RULE_EXECUTING_POOL,
        config.getMaxCheckThreads(),
        config.getMaxCheckThreads() * remoteRuleCount * LtThreadPoolFactory.REMOTE_RULE_POOL_SIZE_FACTOR,
        -1,
        5L, true, (thread, throwable) -> {
          log.error("Thread: " + thread.getName() + " failed with: " + throwable.getMessage());
        },
        true
      );
    }

    this.cache = config.getCacheSize() > 0 ? new ResultCache(
      config.getCacheSize(), config.getCacheTTLSeconds(), TimeUnit.SECONDS) : null;
    this.databaseLogger = DatabaseLogger.getInstance();
    if (databaseLogger.isLogging()) {
      this.logServerId = DatabaseAccess.getInstance().getOrCreateServerId();
    } else {
      this.logServerId = null;
    }

        if (cache != null && !config.isLocalApiMode()) {
      ServerMetricsCollector.getInstance().monitorCache("languagetool_matches_cache", cache.getMatchesCache());
      ServerMetricsCollector.getInstance().monitorCache("languagetool_remote_matches_cache", cache.getRemoteMatchesCache());
      ServerMetricsCollector.getInstance().monitorCache("languagetool_sentences_cache", cache.getSentenceCache());
      ServerMetricsCollector.getInstance().monitorCache("languagetool_remote_matches_cache", cache.getRemoteMatchesCache());
    }

    pipelinePool = new PipelinePool(config, cache, internalServer);
    if (config.isPipelinePrewarmingEnabled()) {
      log.info("Prewarming pipelines...");
      prewarmPipelinePool();
      log.info("Prewarming finished.");
    }
    if (config.getAbTest() != null) {
      UserConfig.enableABTests();
      log.info("A/B-Test enabled: " + config.getAbTest());
      if (config.getAbTest().equals("SuggestionsOrderer")) {
        SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(true);
      }
    }
  }

  protected static Language parseLanguage(String code) throws BadRequestException {
    try {
      return Languages.getLanguageForShortCode(code);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  private void prewarmPipelinePool() {
    // setting + number of pipelines
    // typical addon settings at the moment (2018-11-05)
    Map<PipelineSettings, Integer> prewarmSettings = new HashMap<>();
    List<Language> prewarmLanguages = new ArrayList<>();
    if (config.preferredLanguages.isEmpty()) {
      prewarmLanguages.addAll(Stream.of(
                      "de-DE", "en-US", "en-GB", "pt-BR", "ru-RU", "es", "it", "fr", "pl-PL", "uk-UA")
              .map(Languages::getLanguageForShortCode)
              .collect(Collectors.toList()));
    } else {
      config.preferredLanguages.forEach(s -> {
        prewarmLanguages.add(Languages.getLanguageForShortCode(s));
      });
    }

    List<String> addonDisabledRules = Collections.singletonList("WHITESPACE_RULE");
    List<JLanguageTool.Mode> addonModes = Arrays.asList(JLanguageTool.Mode.TEXTLEVEL_ONLY, JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY);
    UserConfig user = new UserConfig();
    for (Language language : prewarmLanguages) {
      // add-on uses picky mode since 2021-01-20
      for (JLanguageTool.Mode mode : addonModes) {
        QueryParams params = new QueryParams(Collections.emptyList(), Collections.emptyList(), addonDisabledRules,
          Collections.emptyList(), Collections.emptyList(), false, true,
          true, true, Premium.isPremiumVersion(), false, mode, JLanguageTool.Level.PICKY, null);
        PipelineSettings settings = new PipelineSettings(language, null, params, config.globalConfig, user);
        prewarmSettings.put(settings, NUM_PIPELINES_PER_SETTING);

        PipelineSettings settingsMotherTongueEqual = new PipelineSettings(language, language, params, config.globalConfig, user);
        PipelineSettings settingsMotherTongueEnglish = new PipelineSettings(language,
          Languages.getLanguageForName("English"), params, config.globalConfig, user);
        prewarmSettings.put(settingsMotherTongueEqual, NUM_PIPELINES_PER_SETTING);
        prewarmSettings.put(settingsMotherTongueEnglish, NUM_PIPELINES_PER_SETTING);
      }
    }
    try {
      for (Map.Entry<PipelineSettings, Integer> prewarmSetting : prewarmSettings.entrySet()) {
          int numPipelines = prewarmSetting.getValue();
          PipelineSettings setting = prewarmSetting.getKey();

          // request n pipelines first, return all afterwards -> creates multiple for same setting
          List<Pipeline> pipelines = new ArrayList<>();
          for (int i = 0; i < numPipelines; i++) {
            Pipeline p = pipelinePool.getPipeline(setting);
            p.check("LanguageTool");
            pipelines.add(p);
          }
          for (Pipeline p : pipelines) {
            pipelinePool.returnPipeline(setting, p);
          }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error while prewarming pipelines", e);
    }
  }

  void shutdownNow() {
    executorService.shutdownNow();
    RemoteRule.shutdown();
  }

  void checkText(AnnotatedText aText, HttpExchange httpExchange, Map<String, String> params, ErrorRequestLimiter errorRequestLimiter,
                 String remoteAddress) throws Exception {
    checkParams(params);
    long timeStart = System.currentTimeMillis();
    UserLimits limits = ServerTools.getUserLimits(params, config);

    String requestId = httpExchange.getRequestHeaders().getFirst("X-Request-ID");

    // logging information
    String agent = params.get("useragent") != null ? params.get("useragent") : "-";
    Long agentId = null, userId = null;
    if (databaseLogger.isLogging()) {
      DatabaseAccess db = DatabaseAccess.getInstance();
      agentId = db.getOrCreateClientId(params.get("useragent"));
      userId = limits.getPremiumUid();
    }
    String referrer = httpExchange.getRequestHeaders().getFirst("Referer");
    String userAgent = httpExchange.getRequestHeaders().getFirst("User-Agent");

    if (!config.isAnonymousAccessAllowed() && limits.getPremiumUid() == null) {
      throw new AuthException("Anonymous access is prohibited on this server, please provide authentication.");
    }

    int length = aText.getPlainText().length();
    if ("true".equals(params.get("languageChanged"))) {
      log.info("languageChanged to " + params.get("language") + " for text with length " + aText.getPlainText().trim().length());
    }
    if (length > limits.getMaxTextLength()) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.MAX_TEXT_SIZE);
      throw new TextTooLongException("Your text exceeds the limit of " + limits.getMaxTextLength() +
              " characters (it's " + length + " characters). Please submit a shorter text.");
    }
    // static because we can't rely on errorRequestLimiter, null when timeoutRequestLimit option not set
    if (!config.isLocalApiMode()) {
      try {
        RequestLimiter.checkUserLimit(referrer, userAgent, limits);
      } catch (TooManyRequestsException e) {
        String response = "Error: Access denied: " + e.getMessage();
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, response.getBytes(ENCODING).length);
        httpExchange.getResponseBody().write(response.getBytes(ENCODING));
        String message = "Blocked request from uid:" + userId + " because user limit is reached: ";
        message += "limit = " + limits.getRequestsPerDay() + ", mode = " + limits.getLimitEnforcementMode() + ". ";
        message += "Access from " + remoteAddress + ", ";
        message += "HTTP user agent: " + userAgent + ", ";
        message += "User agent param: " + params.get("useragent") + ", ";
        message += "Referrer: " + referrer + ", ";
        message += "language: " + params.get("language") + ", ";
        message += "h: " + reqCounter.getHandleCount() + ", ";
        message += "r: " + reqCounter.getRequestCount();
        if (params.get("username") != null) {
          message += ", user: " + params.get("username");
        }
        if (params.get("apiKey") != null) {
          message += ", apiKey: " + params.get("apiKey");
        }
        String text = params.get("text");
        if (text != null) {
          message += ", text length: " + text.length();
        }
        log.warn(message);
        return;
      }
    }
    List<String> dictGroups = null;
    String dictName = "default";
    if (params.containsKey("dicts")) {
      dictGroups = Arrays.asList(params.get("dicts").split(","));
      dictGroups.sort(Comparator.naturalOrder());
      dictName = "groups_" + String.join(",", dictGroups);
    }
    final List<String> finalDictGroups = dictGroups;
    List<String> dictWords = limits.getPremiumUid() != null ?
      TelemetryProvider.INSTANCE.createSpan(SPAN_NAME_PREFIX +"GetUserDictWords", Attributes.empty(), () -> getUserDictWords(limits, finalDictGroups)) : Collections.emptyList();

    boolean filterDictionaryMatches = "true".equals(params.getOrDefault("filterDictionaryMatches", "true"));

    Long textSessionId = null;
    try {
      if (params.containsKey("textSessionId")) {
        String textSessionIdStr = params.get("textSessionId");
        if (textSessionIdStr.startsWith("user:")) {
          int sepPos = textSessionIdStr.indexOf(':');
          String sessionId = textSessionIdStr.substring(sepPos + 1);
          textSessionId = Long.valueOf(sessionId);
        } else if (textSessionIdStr.contains(":")) { // transitioning to new format used in chrome addon
          // format: "{random number in 0..99999}:{unix time}"
          long random, timestamp;
          int sepPos = textSessionIdStr.indexOf(':');
          random = Long.parseLong(textSessionIdStr.substring(0, sepPos));
          timestamp = Long.parseLong(textSessionIdStr.substring(sepPos + 1));
          // use random number to choose a slice in possible range of values
          // then choose position in slice by timestamp
          long maxRandom = 100000;
          long randomSegmentSize = (Long.MAX_VALUE - maxRandom) / maxRandom;
          long segmentOffset = random * randomSegmentSize;
          if (timestamp > randomSegmentSize) {
            log.warn(String.format("Could not transform textSessionId '%s'", textSessionIdStr));
          }
          textSessionId = segmentOffset + timestamp;
        } else {
          textSessionId = Long.valueOf(textSessionIdStr);
        }
      }
    } catch (NumberFormatException ex) {
      log.info("Could not parse textSessionId '" + params.get("textSessionId") + "' as long: " + ex.getMessage() +
        ", user agent: " + params.get("useragent") + ", version: " + params.get("v") +
        ", HTTP user agent: " + getHttpUserAgent(httpExchange) + ", referrer: " + getHttpReferrer(httpExchange));
    }

    List<String> abTest = null;
    if (agent != null && config.getAbTestClients() != null && config.getAbTestClients().matcher(agent).matches()) {
      //TODO: it is not possible to have individual AbTestClients per AbTest
      boolean testRolledOut;
      // partial rollout; deterministic if textSessionId given to make testing easier
      if (textSessionId != null) {
        testRolledOut = textSessionId % 100 < config.getAbTestRollout();
      } else {
        testRolledOut = random.nextInt(100) < config.getAbTestRollout();
      }
      if (testRolledOut) {
        abTest = Collections.unmodifiableList(config.getAbTest());
      }
    }
    String paramActivatedAbTest = params.get("abtest");
    if (paramActivatedAbTest != null) {
      String[] abParams = paramActivatedAbTest.trim().split(",");
      List<String> tmpAb = new ArrayList<>();
      for (String abParam : abParams) {
        if (config.getAbTest().contains(abParam)) {
          tmpAb.add(abParam.trim());
        }
      }
      if (!tmpAb.isEmpty()) {
        abTest = Collections.unmodifiableList(tmpAb);
      }
    }

    boolean enableHiddenRules = "true".equals(params.get("enableHiddenRules"));
    if (limits.hasPremium()) {
      enableHiddenRules = false;
    }

    boolean autoDetectLanguage = getLanguageAutoDetect(params);
    List<String> preferredVariants = getPreferredVariants(params);
    if (params.get("noopLanguages") != null && !autoDetectLanguage) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new BadRequestException("You can specify 'noopLanguages' only when also using 'language=auto'");
    }
    List<String> noopLangs = params.get("noopLanguages") != null ?
            Arrays.asList(params.get("noopLanguages").split(",")) : Collections.emptyList();
    List<String> preferredLangs = params.get("preferredLanguages") != null ?
            Arrays.asList(params.get("preferredLanguages").split(",")) : Collections.emptyList();
    DetectedLanguage detLang = TelemetryProvider.INSTANCE.createSpan(SPAN_NAME_PREFIX + "DetetectLanguage", Attributes.empty(), () -> getLanguage(aText.getPlainText(), params, preferredVariants, noopLangs, preferredLangs,
      params.getOrDefault("ld", "control").equalsIgnoreCase("test")));
    Language lang = detLang.getGivenLanguage();

    List<Rule> userRules = TelemetryProvider.INSTANCE.createSpan(SPAN_NAME_PREFIX + "GetUserRules", Attributes.empty(), () -> getUserRules(limits, lang, finalDictGroups));
    boolean isMultiLangEnabled = false;
    //only enable this feature with parameter
    if (params.get("enableMultiLanguageChecks") != null && params.get("enableMultiLanguageChecks").equals("true")) {
      isMultiLangEnabled = true;
    }
    
    UserConfig userConfig =
      new UserConfig(dictWords, userRules,
                     getRuleValues(params), config.getMaxSpellingSuggestions(),
                     limits.getPremiumUid(), dictName, limits.getDictCacheSize(),
                     null, filterDictionaryMatches, abTest, textSessionId,
                     !limits.hasPremium() && enableHiddenRules, preferredLangs);

    //print("Check start: " + text.length() + " chars, " + langParam);

    // == temporary counting code ======================================
    /*
    if (httpExchange.getRequestHeaders() != null && httpExchange.getRequestHeaders().get("Accept-Language") != null) {
      List<String> langs = httpExchange.getRequestHeaders().get("Accept-Language");
      if (langs.size() > 0) {
        String[] split = langs.get(0).split(",");
        if (split.length > 0 && detLang.getDetectedLanguage() != null && detLang.getDetectedLanguage().getShortCode().equals("en")) {
          int theCount1 = StringUtils.countMatches(aText.toString(), " the ");
          int theCount2 = StringUtils.countMatches(aText.toString(), "The ");
          String browserLang = split[0];
          System.out.println("STAT\t" + detLang.getDetectedLanguage().getShortCode() + "\t" + detLang.getDetectionConfidence() + "\t" + aText.toString().length() + "\t" + browserLang + "\t" + theCount1 + "\t" + theCount2);
        }
      }
    }
    */
    // ========================================

    Integer count = languageCheckCounts.get(lang.getShortCodeWithCountryAndVariant());
    if (count == null) {
      count = 1;
    } else {
      count++;
    }
    //print("Starting check: " + aText.getPlainText().length() + " chars, #" + count);
    String motherTongueParam = params.get("motherTongue");
    Language motherTongue = motherTongueParam != null ? parseLanguage(motherTongueParam) : null;
    boolean useEnabledOnly = "yes".equals(params.get("enabledOnly")) || "true".equals(params.get("enabledOnly"));
    List<Language> altLanguages = new ArrayList<>();
    if (params.get("altLanguages") != null) {
      String[] altLangParams = params.get("altLanguages").split(",\\s*");
      for (String langCode : altLangParams) {
        Language altLang = parseLanguage(langCode);
        altLanguages.add(altLang);
        if (altLang.hasVariant() && !altLang.isVariant()) {
          ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
          throw new BadRequestException("You specified altLanguage '" + langCode + "', but for this language you need to specify a variant, e.g. 'en-GB' instead of just 'en'");
        }
      }
    }
    List<String> enabledRules = getEnabledRuleIds(params);

    List<String> disabledRules = getDisabledRuleIds(params);
    List<CategoryId> enabledCategories = getCategoryIds("enabledCategories", params);
    List<CategoryId> disabledCategories = getCategoryIds("disabledCategories", params);

    if ((disabledRules.size() > 0 || disabledCategories.size() > 0) && useEnabledOnly) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new BadRequestException("You cannot specify disabled rules or categories using enabledOnly=true");
    }
    if (enabledRules.isEmpty() && enabledCategories.isEmpty() && useEnabledOnly) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new BadRequestException("You must specify enabled rules or categories when using enabledOnly=true");
    }

    boolean enableTempOffRules = "true".equals(params.get("enableTempOffRules"));
    boolean useQuerySettings = enabledRules.size() > 0 || disabledRules.size() > 0 ||
            enabledCategories.size() > 0 || disabledCategories.size() > 0 || enableTempOffRules;
    boolean allowIncompleteResults = "true".equals(params.get("allowIncompleteResults"));
    JLanguageTool.Mode mode = ServerTools.getMode(params);
    JLanguageTool.Level level = ServerTools.getLevel(params);
    String[] toneTagNames = params.get("toneTags") != null ? params.get("toneTags").split(",") : null;
    Set<ToneTag> toneTags = new HashSet<>(ToneTag.values().length);
    if (toneTagNames != null) {
      if (toneTagNames.length == 1 && toneTagNames[0].isEmpty()) { //&toneTags=
        toneTags.add(ToneTag.ALL_WITHOUT_GOAL_SPECIFIC);
      } else {
        for (String toneTagName : toneTagNames) {
          if (toneTagNames.length > 1 && (toneTagName.equals("NO_TONE_RULE") || toneTagName.equals("ALL_TONE_RULES"))) { //&toneTags=ALL_TONE_RULES or //&toneTags=NO_TONE_RULE
            log.warn("NO_TONE_RULE and ALL_TONE_RULES will be ignored if more than one toneTag is in params.");
            continue;
          }
          try {
            toneTags.add(ToneTag.valueOf(toneTagName));
          } catch (IllegalArgumentException ex) {
            //just ignore unsupported toneTags
            log.warn("Unsupported toneTag found in params: {}", toneTagName);
          }
        }
      }
    } else {
      toneTags.add(ToneTag.ALL_WITHOUT_GOAL_SPECIFIC); //No toneTags param in request
    }
    String callback = params.get("callback");
    // allowed to log input on errors?
    boolean inputLogging = !params.getOrDefault("inputLogging", "").equals("no");
    QueryParams qParams = new QueryParams(altLanguages, enabledRules, disabledRules,
      enabledCategories, disabledCategories, useEnabledOnly,
      useQuerySettings, allowIncompleteResults, enableHiddenRules, limits.getPremiumUid() != null && limits.hasPremium(), enableTempOffRules, mode, level, toneTags, callback, inputLogging);

    int textSize = length;
    List<CheckResults> ruleMatchesSoFar = Collections.synchronizedList(new ArrayList<>());
    Future<List<CheckResults>> future;
    try {
      future = executorService.submit(() -> {
        try (MDC.MDCCloseable c = MDC.putCloseable("rID", LanguageToolHttpHandler.getRequestId(httpExchange))) {
          log.debug("Starting text check on {} chars; params: {}", length, qParams);
          long time = System.currentTimeMillis();
          List<CheckResults> results = getRuleMatches(aText, lang, motherTongue, params, qParams, userConfig, f -> ruleMatchesSoFar.add(new CheckResults(Collections.singletonList(f), Collections.emptyList())));
          log.debug("Finished text check in {}ms. Starting suggestion generation.", System.currentTimeMillis() - time);
          time = System.currentTimeMillis();
          // generate suggestions, otherwise this is not part of the timeout logic and not properly measured in the metrics
          results.stream().flatMap(r -> r.getRuleMatches().stream()).forEach(RuleMatch::computeLazySuggestedReplacements);
          log.debug("Finished suggestion generation in {}ms, returning results.", System.currentTimeMillis() - time);
          return results;
        }
      });
    } catch (RejectedExecutionException e) {
      throw new UnavailableException("Server overloaded, please try again later", e);
    }
    String incompleteResultReason = null;
    List<CheckResults> res;
    Attributes textCheckingAttributes = Attributes.builder()
            .put("text.language", lang.getShortCode())
            .put("text.size", textSize)
            .put("userRules.size", userRules.size())
            .put("dictionary.size", dictWords.size())
            .build();
    Integer finalCount = count;
    Map.Entry<List<CheckResults>, String> resAndReason = TelemetryProvider.INSTANCE.createSpan(SPAN_NAME_PREFIX + "GetRuleMatches", textCheckingAttributes, (span) -> {
        List<CheckResults> localRes;
        String localReason = null;
        try {
          if (limits.getMaxCheckTimeMillis() < 0) {
            localRes = future.get();
          } else {
            localRes = future.get(limits.getMaxCheckTimeMillis(), TimeUnit.MILLISECONDS);
          }
        } catch (ExecutionException e) {
          future.cancel(true);
          if (ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
            ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.TOO_MANY_ERRORS);
          }
          if (qParams.allowIncompleteResults && ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
            log.warn(e.getMessage() + " - returning " + ruleMatchesSoFar.size() + " matches found so far. " +
              "Detected language: " + detLang + ", " + ServerTools.getLoggingInfo(remoteAddress, null, -1, httpExchange,
              params, System.currentTimeMillis() - timeStart, reqCounter));
            localRes = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
            localReason = "Results are incomplete: " + ExceptionUtils.getRootCause(e).getMessage();
          } else if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
            throw (OutOfMemoryError) e.getCause();
          } else {
            throw new RuntimeException(ServerTools.cleanUserTextFromMessage(e.getMessage(), params) + ", detected: " + detLang, e);
          }
        } catch (TimeoutException e) {
          boolean cancelled = future.cancel(true);
          Path loadFile = Paths.get("/proc/loadavg");  // works in Linux only(?)
          String loadInfo = loadFile.toFile().exists() ? Files.readAllLines(loadFile).toString() : "(unknown)";
          if (errorRequestLimiter != null) {
            errorRequestLimiter.logAccess(remoteAddress, httpExchange.getRequestHeaders(), params);
          }
          String message = "Text checking took longer than allowed maximum of " + limits.getMaxCheckTimeMillis() +
            " milliseconds (cancelled: " + cancelled +
            ", lang: " + lang.getShortCodeWithCountryAndVariant() +
            ", detected: " + detLang +
            ", #" + finalCount +
            ", " + length + " characters of text" +
            ", mode: " + mode.toString().toLowerCase() +
            ", h: " + reqCounter.getHandleCount() +
            ", r: " + reqCounter.getRequestCount() +
            ", requestId: " + requestId +
            ", system load: " + loadInfo + ")";
          if (qParams.allowIncompleteResults) {
            log.info(message + " - returning " + ruleMatchesSoFar.size() + " matches found so far");
            localRes = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
            localReason = "Results are incomplete: text checking took longer than allowed maximum of " +
              String.format(Locale.ENGLISH, "%.2f", limits.getMaxCheckTimeMillis() / 1000.0) + " seconds";
            span.setAttribute("incompleteResults", true);
          } else {
            ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.MAX_CHECK_TIME);
            throw new RuntimeException(message, e);
          }
        }
        return new AbstractMap.SimpleEntry(localRes, localReason);
      });
    res = resAndReason.getKey();
    incompleteResultReason = resAndReason.getValue();

    // no lazy computation at later points (outside of timeout enforcement)
    // e.g. ruleMatchesSoFar can have matches without computeLazySuggestedReplacements called yet
    res.forEach(checkResults -> checkResults.getRuleMatches().forEach(RuleMatch::discardLazySuggestedReplacements));

    setHeaders(httpExchange);

    List<RuleMatch> hiddenMatches = new ArrayList<>();
    boolean temporaryPremiumDisabledRuleMatch = false;
    Set<String> temporaryPremiumDisabledRuleMatchedIds = new HashSet<>();
    // filter computed premium matches, convert to hidden matches - no separate hidden matches server needed
    if (!qParams.premium && qParams.enableHiddenRules) {
      List<RuleMatch> allMatches = new ArrayList<>(); // for filtering out overlapping matches, collect across CheckResults
      List<RuleMatch> premiumMatches = new ArrayList<>();
      for (CheckResults result : res) {
        List<RuleMatch> filteredMatches = new ArrayList<>();
        for (RuleMatch match : result.getRuleMatches()) {
          if (Premium.get().isPremiumRule(match.getRule()) && !Premium.isTempNotPremium(match.getRule())) {
            premiumMatches.add(match);
          } else if (userConfig.getAbTest() != null && userConfig.getAbTest().equals("ALLOW_PREMIUM_IN_BASIC") && Premium.get().isPremiumRule(match.getRule()) && Premium.isTempNotPremium(match.getRule())) {
            System.out.println("Rule: " + match.getRule().getId() + " is premium but temporary available in basic");
            filteredMatches.add(match);
            allMatches.add(match);
            temporaryPremiumDisabledRuleMatch = true;
            temporaryPremiumDisabledRuleMatchedIds.add(match.getRule().getId());
          } else {
            // filter out premium matches
            filteredMatches.add(match);
            // keep track for filtering out overlapping matches
            allMatches.add(match);
          }
          // need to replace list, can't iterate and remove since some rules may return unmodifiable lists
          result.setRuleMatches(filteredMatches);
        }
      }
      hiddenMatches.addAll(ResultExtender.getAsHiddenMatches(allMatches, premiumMatches));
    }

    //### Start multiLangPart
    //TODO: implement recheck of ignoreRanges
    if (isMultiLangEnabled) {
      log.debug("Not implemented yet");
//      long startTimeRecheck = System.currentTimeMillis();
//      Map<String, List<Range>> rangesOrderedByLanguage = new HashMap<>();
//      res.forEach(checkResults -> {
//        checkResults.getIgnoredRanges().forEach(range -> {
//          List<Range> sentenceRanges = rangesOrderedByLanguage.getOrDefault(range.getLang(), new ArrayList<>());
//          sentenceRanges.add(range);
//          rangesOrderedByLanguage.put(range.getLang(), sentenceRanges);
//        });
//      });
//      rangesOrderedByLanguage.forEach((shortLangCode, ranges) -> {
//        Language rangeLanguage = Languages.getLanguageForShortCode(shortLangCode);
//        StringBuilder languageTextBuilder = new StringBuilder();
//        ranges.forEach(range -> {
//          String text = range.getAnalyzedSentence().getText().trim() + " ";
//          languageTextBuilder.append(text);
//        });
//        AnnotatedText finalTextToCheckAgain = new AnnotatedTextBuilder().addText(languageTextBuilder.toString().trim()).build();
//      });
//      long endTimeRecheck = System.currentTimeMillis();
//      log.trace("Time needed for recheck other languages: {}", (endTimeRecheck - startTimeRecheck) / 1000f);
    }
    //### End multiLangPart

    int compactMode = Integer.parseInt(params.getOrDefault("c", "0"));
    String response = getResponse(aText, lang, detLang, motherTongue, res, hiddenMatches, incompleteResultReason, compactMode,
      limits.getPremiumUid() == null, qParams.mode);
    if (qParams.callback != null) {
      // JSONP - still needed today for the special case of hosting your own on-premise LT without SSL
      // and using it from a local MS Word (not Online Word) - issue #89 in the add-in repo:
      response = qParams.callback + "(" + response + ");";
    }
    String messageSent = "sent";
    String languageMessage = lang.getShortCodeWithCountryAndVariant();
    try {
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(response.getBytes(ENCODING));
      ServerMetricsCollector.getInstance().logResponse(HttpURLConnection.HTTP_OK);
    } catch (IOException exception) {
      // the client is disconnected
      messageSent = "notSent: " + exception.getMessage();
    }
    if (motherTongue != null) {
      languageMessage += " (mother tongue: " + motherTongue.getShortCodeWithCountryAndVariant() + ")";
    }
    if (autoDetectLanguage) {
      languageMessage += "[auto]";
    }
    languageCheckCounts.put(lang.getShortCodeWithCountryAndVariant(), count);
    int computationTime = (int) (System.currentTimeMillis() - timeStart);
    Premium premium = Premium.get();

    List<String> premiumMatchRuleIds = res.stream().
            flatMap(r -> r.getRuleMatches().stream()).
            filter(k -> premium.isPremiumRule(k.getRule())).
            map(k -> k.getRule().getId()).
            collect(Collectors.toList());

    Map<String, Integer> ruleMatchCount = getRuleMatchCount(res);
    int matchCount = ruleMatchCount.size();

    String version = params.get("v") != null ? ", version: " + params.get("v") : "";
    String skipLimits = limits.getSkipLimits() ? ", skipLimits" : "";
    log.info("Check done: " + length + " chars, " + languageMessage +
            ", requestId: " + requestId + ", #" + count + ", " + referrer + ", "
            + premiumMatchRuleIds.size() + "/"
            + matchCount + " matches, "
            + computationTime + "ms, agent:" + agent + version
            + ", " + messageSent + ", q:" + (workQueue != null ? workQueue.size() : "?")
            + ", h:" + reqCounter.getHandleCount() + ", dH:" + reqCounter.getDistinctIps()
            + ", r:" + reqCounter.getRequestCount()
            + ", m:" + ServerTools.getModeForLog(mode) + skipLimits
            + ", premium: " + (limits.getPremiumUid() != null && limits.hasPremium())
            //+ ", temporaryPremiumDisabledRuleMatches: " + temporaryPremiumDisabledRuleMatch //TODO activate if used
            //+ ", temporaryPremiumDisabledRuleMatchedIds: " + temporaryPremiumDisabledRuleMatchedIds //TODO activate if used
            + (limits.getPremiumUid() != null ? ", uid:" + limits.getPremiumUid() : ""));
    if (limits.getPremiumUid() != null && limits.getPremiumUid() == 1456) { // Fernando Moon, fernando.moon@eggbun-edu.com - allows logging text in exchange for free API access (see email 2018-05-31):
      log.info("Eggbun input: " + aText.getPlainText().replace("\n", "\\n").replace("\r", "\\r"));
    }
    if (premiumMatchRuleIds.size() > 0) {
      for (String premiumMatchRuleId : premiumMatchRuleIds) {
        log.info("premium:" + lang.getShortCodeWithCountryAndVariant() + ":" + premiumMatchRuleId);
      }
    }

    if (!Premium.isPremiumStatusCheck(aText)) { // exclude status checks from add-on from metrics
      ServerMetricsCollector.getInstance().logCheck(
        lang, computationTime, textSize, matchCount, mode);

      if (!config.isSkipLoggingChecks()) {
        // NOTE: Java/DB (not sure) can't keep up with logging the volume of new entries we've reached,
        // so we limit it to enterprise customers where we actually pay attention to the request limits
        if (limits.getRequestsPerDay() != null) {
          DatabaseCheckLogEntry logEntry = new DatabaseCheckLogEntry(userId, agentId, logServerId, textSize, matchCount,
            lang, detLang.getDetectedLanguage(), computationTime, textSessionId, mode.toString());
          databaseLogger.log(logEntry);
        }
      }

      if (databaseLogger.isLogging()) {
        if (System.currentTimeMillis() - pingsCleanDateMillis > PINGS_CLEAN_MILLIS && pings.size() < PINGS_MAX_SIZE) {
          log.info("Cleaning pings DB (" + pings.size() + " items)");
          pings.clear();
          pingsCleanDateMillis = System.currentTimeMillis();
        }
        if (agentId != null && userId != null) {
          DatabasePingLogEntry ping = new DatabasePingLogEntry(agentId, userId);
          if (!pings.contains(ping)) {
            databaseLogger.log(ping);
            if (pings.size() >= PINGS_MAX_SIZE) {
              // prevent pings taking up unlimited amounts of memory
              log.warn("Pings DB has reached max size: " + pings.size());
            } else {
              pings.add(ping);
            }
          }
        }
      }

    }

  }

  public boolean checkerQueueAlmostFull() {
    if (this.executorService instanceof ThreadPoolExecutor) {
      ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) this.executorService;
      int maxQueueSize = config.getTextCheckerQueueSize();
      int queuesize = threadPoolExecutor.getQueue().size();
      if (queuesize > maxQueueSize/2) { //should not happen in normal cases (workQueue.size() == config.getTextCheckerQueueSize())
        log.warn("TextChecker queue is almost full requests in queue: {} active request: {}", queuesize, threadPoolExecutor.getActiveCount());
        return true;
      }
    }
    return false;
  }

  @NotNull
  private Map<String, Integer> getRuleMatchCount(List<CheckResults> res) {
    Map<String, Integer> ruleMatchCount = new HashMap<>();
    for (CheckResults r : res) {
      for (RuleMatch ruleMatch : r.getRuleMatches()) {
        String ruleId = ruleMatch.getRule().getId();
        ruleMatchCount.put(ruleId, ruleMatchCount.getOrDefault(ruleId, 0) + 1);
      }
    }
    return ruleMatchCount;
  }

  private Map<String, Integer> getRuleValues(Map<String, String> parameters) {
    Map<String, Integer> ruleValues = new HashMap<>();
    String parameterString = parameters.get("ruleValues");
    if (parameterString == null) {
      return ruleValues;
    }
    String[] pairs = parameterString.split(",");
    for (String pair : pairs) {
      String[] ruleAndValue  = pair.split(":");
      ruleValues.put(ruleAndValue[0], Integer.parseInt(ruleAndValue[1]));
    }
    return ruleValues;
  }

  private List<String> getUserDictWords(UserLimits limits, List<String> groups) {
    DatabaseAccess db = DatabaseAccess.getInstance();
    return db.getWords(limits, groups, RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
  }

  private List<Rule> getUserRules(UserLimits limits, Language lang, List<String> groups) {
    if (limits.getPremiumUid() != null && DatabaseAccess.isReady()) {
      DatabaseAccess db = DatabaseAccess.getInstance();
      return db.getRules(limits, lang, groups);
    } else {
      return Collections.emptyList();
    }
  }

  protected void checkParams(Map<String, String> parameters) {
    if (parameters.get("text") == null && parameters.get("data") == null) {
      throw new BadRequestException("Missing 'text' or 'data' parameter");
    }
  }

  private List<CheckResults> getRuleMatches(AnnotatedText aText, Language lang,
                                         Language motherTongue, Map<String, String> parameters,
                                         QueryParams params, UserConfig userConfig,
                                         /*DetectedLanguage detLang,
                                         List<String> preferredLangs, List<String> preferredVariants,*/
                                         RuleMatchListener listener) throws Exception {
    if (cache != null && cache.requestCount() > 0 && cache.requestCount() % CACHE_STATS_PRINT == 0) {
      String sentenceHitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.getSentenceCache().stats().hitRate() * 100.0f);
      String matchesHitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.getMatchesCache().stats().hitRate() * 100.0f);
      String remoteHitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.getRemoteMatchesCache().stats().hitRate() * 100.0f);
      log.info("Cache stats: " + sentenceHitPercentage + "% / " + matchesHitPercentage + "% / " + remoteHitPercentage + "% hit rate");
    }

    if (parameters.get("sourceText") != null) {
      if (parameters.get("sourceLanguage") == null) {
        throw new BadRequestException("'sourceLanguage' parameter missing - must be set when 'sourceText' is set");
      }
      Language sourceLanguage = parseLanguage(parameters.get("sourceLanguage"));
      JLanguageTool sourceLt = new JLanguageTool(sourceLanguage);
      JLanguageTool targetLt = new JLanguageTool(lang);
      if (userConfig.filterDictionaryMatches()) {
        targetLt.addMatchFilter(new DictionaryMatchFilter(userConfig));
      }
      List<BitextRule> bitextRules = Tools.getBitextRules(sourceLanguage, lang);
      return Collections.singletonList(
              new CheckResults(Tools.checkBitext(parameters.get("sourceText"), aText.getPlainText(), sourceLt, targetLt, bitextRules), Collections.emptyList())
      );
    } else {
      List<CheckResults> res = new ArrayList<>();
      res.addAll(getPipelineResults(aText, lang, motherTongue, params, userConfig, listener));
//      NOTE: Not needed anymore. The "multilingual" parameter is not used.
//      if (preferredLangs.size() < 2 || parameters.get("multilingual") == null || parameters.get("multilingual").equals("false")) {
//        res.addAll(getPipelineResults(aText, lang, motherTongue, params, userConfig, listener));
//      } 
//      else {
//        // support for multilingual texts:
//        try {
//          Language mainLang = getLanguageVariantForCode(detLang.getDetectedLanguage().getShortCode(), preferredVariants);
//          List<Language> secondLangs = new ArrayList<>();
//          for (String preferredLangCode : preferredLangs) {
//            if (!preferredLangCode.equals(mainLang.getShortCode())) {
//              secondLangs.add(getLanguageVariantForCode(preferredLangCode, preferredVariants));
//              break;
//            }
//          }
//          LanguageAnnotator annotator = new LanguageAnnotator();
//          List<FragmentWithLanguage> fragments = annotator.detectLanguages(aText.getPlainText(), mainLang, secondLangs);
//          List<Language> langs = new ArrayList<>();
//          langs.add(mainLang);
//          langs.addAll(secondLangs);
//          Map<Language, AnnotatedTextBuilder> lang2builder = getBuilderMap(fragments, new HashSet<>(langs));
//          for (Map.Entry<Language, AnnotatedTextBuilder> entry : lang2builder.entrySet()) {
//            res.addAll(getPipelineResults(entry.getValue().build(), entry.getKey(), motherTongue, params, userConfig, listener));
//          }
//        } catch (Exception e) {
//          log.error("Problem with multilingual mode (preferredLangs=" + preferredLangs+ ", preferredVariants=" + preferredVariants + "), " +
//            "falling back to single language.", e);
//          res.addAll(getPipelineResults(aText, lang, motherTongue, params, userConfig, listener));
//        }
//      }
      return res;
    }
  }

  private Language getLanguageVariantForCode(String langCode, List<String> preferredVariants) {
    for (String preferredVariant : preferredVariants) {
      if (preferredVariant.startsWith(langCode + "-")) {
        return parseLanguage(preferredVariant);
      }
    }
    return parseLanguage(langCode);
  }

  private List<CheckResults> getPipelineResults(AnnotatedText aText, Language lang, Language motherTongue, QueryParams params, UserConfig userConfig, RuleMatchListener listener) throws Exception {
    PipelineSettings settings = null;
    Pipeline lt = null;
    List<CheckResults> res = new ArrayList<>();
    try {
      settings = new PipelineSettings(lang, motherTongue, params, config.globalConfig, userConfig);
      lt = pipelinePool.getPipeline(settings);
      Long textSessionId = userConfig.getTextSessionId();
      if (params.regressionTestMode) {
        textSessionId = -2L; // magic value for remote rule roll-out - includes all results, even from disabled models
      }
      res.add(lt.check2(aText, true, JLanguageTool.ParagraphHandling.NORMAL, listener,
        params.mode, params.level, params.toneTags, textSessionId));
    } finally {
      if (lt != null) {
        pipelinePool.returnPipeline(settings, lt);
      }
    }
    return res;
  }

  @NotNull
  private Map<Language, AnnotatedTextBuilder> getBuilderMap(List<FragmentWithLanguage> fragments, Set<Language> maybeUsedLangs) {
    Map<Language, AnnotatedTextBuilder> lang2builder = new HashMap<>();
    for (Language usedLang : maybeUsedLangs) {
      if (!lang2builder.containsKey(usedLang)) {
        lang2builder.put(usedLang, new AnnotatedTextBuilder());
      }
      AnnotatedTextBuilder atb = lang2builder.get(usedLang);
      for (FragmentWithLanguage fragment : fragments) {
        if (usedLang.getShortCodeWithCountryAndVariant().equals(fragment.getLangCode())) {
          atb.addText(fragment.getFragment());
        } else {
          atb.addMarkup(fragment.getFragment());  // markup = ignore this text
        }
      }
    }
    return lang2builder;
  }

  @NotNull
  private List<CategoryId> getCategoryIds(String paramName, Map<String, String> parameters) {
    List<String> stringIds = getCommaSeparatedStrings(paramName, parameters);
    List<CategoryId> ids = new ArrayList<>();
    for (String stringId : stringIds) {
      ids.add(new CategoryId(stringId));
    }
    return ids;
  }

  @NotNull
  protected List<String> getCommaSeparatedStrings(String paramName, Map<String, String> parameters) {
    String disabledParam = parameters.get(paramName);
    List<String> result = new ArrayList<>();
    if (disabledParam != null) {
      result.addAll(Arrays.asList(disabledParam.split(",")));
    }
    return result;
  }
  
  DetectedLanguage detectLanguageOfString(String text, String fallbackLanguage, List<String> preferredVariants,
                                          List<String> noopLangs, List<String> preferredLangs) {
    return this.detectLanguageOfString(text, fallbackLanguage, preferredVariants, noopLangs, preferredLangs, false);
  }

  DetectedLanguage detectLanguageOfString(String text, String fallbackLanguage, List<String> preferredVariants,
                                          List<String> noopLangs, List<String> preferredLangs, boolean forcePreferredLanguages) {
    Language lang;
    String cleanText = languageIdentifier.cleanAndShortenText(text);
    DetectedLanguage detected = languageIdentifier.detectLanguage(cleanText, noopLangs, preferredLangs, forcePreferredLanguages);
    if (detected == null) {
      lang = parseLanguage(fallbackLanguage != null ? fallbackLanguage : "en");
    } else {
      lang = detected.getDetectedLanguage();
    }
    //String mode;
    //long t1 = System.nanoTime();
    //long t2 = System.nanoTime();
    //float runTime = (t2-t1)/1000.0f/1000.0f;
    //System.out.printf(Locale.ENGLISH, "detected " + detected + " using " + mode + " in %.2fms for %d chars\n", runTime, text.length());
    
    if (preferredVariants.size() > 0) {
      for (String preferredVariant : preferredVariants) {
        if (!preferredVariant.contains("-")) {
          throw new BadRequestException("Invalid format for 'preferredVariants', expected a dash as in 'en-GB': '" + preferredVariant + "'");
        }
        String preferredVariantLang = preferredVariant.split("-")[0];
        if (preferredVariantLang.equals(lang.getShortCode())) {
          lang = parseLanguage(preferredVariant);
          if (lang == null) {
            throw new BadRequestException("Invalid 'preferredVariants', no such language/variant found: '" + preferredVariant + "'");
          }
        }
      }
    } else {
      if (lang.getDefaultLanguageVariant() != null) {
        lang = lang.getDefaultLanguageVariant();
      }
    }
    return new DetectedLanguage(null, lang, detected != null ? detected.getDetectionConfidence() : 0f,
      detected != null ? detected.getDetectionSource() : null);
  }

  static class QueryParams {
    final List<Language> altLanguages;
    final List<String> enabledRules;
    final List<String> disabledRules;
    final List<CategoryId> enabledCategories;
    final List<CategoryId> disabledCategories;
    final boolean useEnabledOnly;
    final boolean useQuerySettings;
    final boolean allowIncompleteResults;
    final boolean enableHiddenRules;
    final boolean premium;
    final boolean enableTempOffRules;
    final JLanguageTool.Mode mode;
    final JLanguageTool.Level level;
    final Set<ToneTag> toneTags;
    final String callback;
    /** allowed to log input with stack traces to reproduce errors? */
    final boolean inputLogging;

    final boolean regressionTestMode; // no fallbacks for remote rules, retries, enable all rules

    QueryParams() {
      this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
           false, false, false, false, false, false, JLanguageTool.Mode.ALL, JLanguageTool.Level.DEFAULT, null);
    }

    QueryParams(List<Language> altLanguages, List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults, boolean enableHiddenRules, boolean premium, boolean enableTempOffRules, JLanguageTool.Mode mode, JLanguageTool.Level level, @Nullable String callback) {
      this(altLanguages, enabledRules, disabledRules, enabledCategories, disabledCategories, useEnabledOnly, useQuerySettings, allowIncompleteResults, enableHiddenRules, premium, enableTempOffRules, mode, level, callback, true);
    }

    QueryParams(List<Language> altLanguages, List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults, boolean enableHiddenRules, boolean premium, boolean enableTempOffRules, JLanguageTool.Mode mode, JLanguageTool.Level level, @Nullable String callback, boolean inputLogging) {
      this(altLanguages, enabledRules, disabledRules, enabledCategories,disabledCategories,useEnabledOnly, useQuerySettings, allowIncompleteResults, enableHiddenRules, premium, enableTempOffRules, mode, level, null, callback, inputLogging);
    }

    QueryParams(List<Language> altLanguages, List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults, boolean enableHiddenRules, boolean premium, boolean enableTempOffRules, JLanguageTool.Mode mode, JLanguageTool.Level level, Set<ToneTag> toneTags, @Nullable String callback, boolean inputLogging) {
      this.altLanguages = Objects.requireNonNull(altLanguages);
      this.enabledRules = enabledRules;
      this.disabledRules = disabledRules;
      this.enabledCategories = enabledCategories;
      this.disabledCategories = disabledCategories;
      this.useEnabledOnly = useEnabledOnly;
      this.useQuerySettings = useQuerySettings;
      this.allowIncompleteResults = allowIncompleteResults;
      this.enableHiddenRules = enableHiddenRules;
      this.premium = premium;
      this.enableTempOffRules = enableTempOffRules;
      this.regressionTestMode = enableTempOffRules;
      this.mode = Objects.requireNonNull(mode);
      this.level = Objects.requireNonNull(level);
      this.toneTags = toneTags;
      if (callback != null && !callback.matches("[a-zA-Z]+")) {
        throw new BadRequestException("'callback' value must match [a-zA-Z]+: '" + callback + "'");
      }
      this.callback = callback;
      this.inputLogging = inputLogging;
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder()
        .append(altLanguages)
        .append(enabledRules)
        .append(disabledRules)
        .append(enabledCategories)
        .append(disabledCategories)
        .append(useEnabledOnly)
        .append(useQuerySettings)
        .append(allowIncompleteResults)
        .append(enableHiddenRules)
        .append(premium)
        .append(enableTempOffRules)
        .append(regressionTestMode)
        .append(mode)
        .append(level)
        .append(callback)
        .append(inputLogging)
        .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      QueryParams other = (QueryParams) obj;
      return new EqualsBuilder()
        .append(altLanguages, other.altLanguages)
        .append(enabledRules, other.enabledRules)
        .append(disabledRules, other.disabledRules)
        .append(enabledCategories, other.enabledCategories)
        .append(disabledCategories, other.disabledCategories)
        .append(useEnabledOnly, other.useEnabledOnly)
        .append(useQuerySettings, other.useQuerySettings)
        .append(allowIncompleteResults, other.allowIncompleteResults)
        .append(enableHiddenRules, other.enableHiddenRules)
        .append(premium, other.premium)
        .append(enableTempOffRules, other.enableTempOffRules)
        .append(regressionTestMode, other.regressionTestMode)
        .append(mode, other.mode)
        .append(level, other.level)
        .append(callback, other.callback)
        .append(inputLogging, other.inputLogging)
        .isEquals();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
        .append("altLanguages", altLanguages)
        .append("enabledRules", enabledRules)
        .append("disabledRules", disabledRules)
        .append("enabledCategories", enabledCategories)
        .append("disabledCategories", disabledCategories)
        .append("useEnabledOnly", useEnabledOnly)
        .append("useQuerySettings", useQuerySettings)
        .append("allowIncompleteResults", allowIncompleteResults)
        .append("enableHiddenRules", enableHiddenRules)
        .append("premium", premium)
        .append("enableTempOffRules", enableTempOffRules)
        .append("regressionTestMode", regressionTestMode)
        .append("mode", mode)
        .append("level", level)
        .append("callback", callback)
        .append("inputLogging", inputLogging)
        .build();
    }
  }

}
