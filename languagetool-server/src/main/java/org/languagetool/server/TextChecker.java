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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.DictionaryMatchFilter;
import org.languagetool.rules.RemoteRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.spelling.morfologik.suggestions_ordering.SuggestionsOrdererConfig;
import org.languagetool.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since 3.4
 */
abstract class TextChecker {

  private static final int PINGS_CLEAN_MILLIS = 60 * 1000;  // internal pings database will be cleaned this often
  private static final int PINGS_MAX_SIZE = 5000;
  private static final int NGRAM_THRESHOLD = 50;

  protected abstract void setHeaders(HttpExchange httpExchange);
  protected abstract String getResponse(AnnotatedText text, Language language, DetectedLanguage lang, Language motherTongue, List<CheckResults> matches,
                                        List<RuleMatch> hiddenMatches, String incompleteResultReason, int compactMode, boolean showPremiumHint);
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
  private static final Logger logger = LoggerFactory.getLogger(TextChecker.class);

  private static final String ENCODING = "UTF-8";
  private static final int CACHE_STATS_PRINT = 500; // print cache stats every n cache requests
  
  private final Map<String,Integer> languageCheckCounts = new HashMap<>();
  private final Queue<Runnable> workQueue;
  private final RequestCounter reqCounter;

  // keep track of timeouts of the hidden matches server, check health periodically;
  // -1 => healthy, else => check timed out at given date, check back if time difference > config.getHiddenMatchesFailTimeout()
  private long lastHiddenMatchesServerTimeout;
  // counter; mark as down if this reaches hiddenMatchesServerFall
  private long hiddenMatchesServerFailures = 0;
  private final LanguageIdentifier fastTextIdentifier;
  private final ExecutorService executorService;
  private final ResultCache cache;
  private final DatabaseLogger databaseLogger;
  private final Long logServerId;
  private final Random random = new Random();
  private final Set<DatabasePingLogEntry> pings = new HashSet<>();
  private long pingsCleanDateMillis = System.currentTimeMillis();
  private LanguageIdentifier ngramIdentifier = null;
  PipelinePool pipelinePool; // mocked in test -> package-private / not final

  TextChecker(HTTPServerConfig config, boolean internalServer, Queue<Runnable> workQueue, RequestCounter reqCounter) {
    this.config = config;
    this.workQueue = workQueue;
    this.reqCounter = reqCounter;
    this.fastTextIdentifier = new LanguageIdentifier();
    this.fastTextIdentifier.enableFasttext(config.getFasttextBinary(), config.getFasttextModel());
    if (config.getNgramLangIdentData() != null) {
      this.ngramIdentifier = new LanguageIdentifier();
      this.ngramIdentifier.enableNgrams(config.getNgramLangIdentData());
    }
    this.executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("lt-textchecker-thread-%d").build());
    this.cache = config.getCacheSize() > 0 ? new ResultCache(
      config.getCacheSize(), config.getCacheTTLSeconds(), TimeUnit.SECONDS) : null;
    this.databaseLogger = DatabaseLogger.getInstance();
    if (databaseLogger.isLogging()) {
      this.logServerId = DatabaseAccess.getInstance().getOrCreateServerId();
    } else {
      this.logServerId = null;
    }

    ServerMetricsCollector.getInstance().logHiddenServerConfiguration(config.getHiddenMatchesServer() != null);

    if (cache != null) {
      ServerMetricsCollector.getInstance().monitorCache("languagetool_matches_cache", cache.getMatchesCache());
      ServerMetricsCollector.getInstance().monitorCache("languagetool_remote_matches_cache", cache.getRemoteMatchesCache());
      ServerMetricsCollector.getInstance().monitorCache("languagetool_sentences_cache", cache.getSentenceCache());
      ServerMetricsCollector.getInstance().monitorCache("languagetool_remote_matches_cache", cache.getRemoteMatchesCache());
    }

    pipelinePool = new PipelinePool(config, cache, internalServer);
    if (config.isPipelinePrewarmingEnabled()) {
      logger.info("Prewarming pipelines...");
      prewarmPipelinePool();
      logger.info("Prewarming finished.");
    }
    if (config.getAbTest() != null) {
      UserConfig.enableABTests();
      logger.info("A/B-Test enabled: " + config.getAbTest());
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
    Map<PipelinePool.PipelineSettings, Integer> prewarmSettings = new HashMap<>();
    List<Language> prewarmLanguages = Stream.of(
      "de-DE", "en-US", "en-GB", "pt-BR", "ru-RU", "es", "it", "fr", "pl-PL", "uk-UA")
      .map(Languages::getLanguageForShortCode)
      .collect(Collectors.toList());
    List<String> addonDisabledRules = Collections.singletonList("WHITESPACE_RULE");
    List<JLanguageTool.Mode> addonModes = Arrays.asList(JLanguageTool.Mode.TEXTLEVEL_ONLY, JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY);
    UserConfig user = new UserConfig();
    for (Language language : prewarmLanguages) {
      // add-on uses picky mode since 2021-01-20
      for (JLanguageTool.Mode mode : addonModes) {
        QueryParams params = new QueryParams(Collections.emptyList(), Collections.emptyList(), addonDisabledRules,
          Collections.emptyList(), Collections.emptyList(), false, true,
          true, true, false, mode, JLanguageTool.Level.PICKY, null);
        PipelinePool.PipelineSettings settings = new PipelinePool.PipelineSettings(language, null, params, config.globalConfig, user);
        prewarmSettings.put(settings, NUM_PIPELINES_PER_SETTING);

        PipelinePool.PipelineSettings settingsMotherTongueEqual = new PipelinePool.PipelineSettings(language, language, params, config.globalConfig, user);
        PipelinePool.PipelineSettings settingsMotherTongueEnglish = new PipelinePool.PipelineSettings(language,
          Languages.getLanguageForName("English"), params, config.globalConfig, user);
        prewarmSettings.put(settingsMotherTongueEqual, NUM_PIPELINES_PER_SETTING);
        prewarmSettings.put(settingsMotherTongueEnglish, NUM_PIPELINES_PER_SETTING);
      }
    }
    try {
      for (Map.Entry<PipelinePool.PipelineSettings, Integer> prewarmSetting : prewarmSettings.entrySet()) {
          int numPipelines = prewarmSetting.getValue();
          PipelinePool.PipelineSettings setting = prewarmSetting.getKey();

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

  void checkText(AnnotatedText aText, HttpExchange httpExchange, Map<String, String> parameters, ErrorRequestLimiter errorRequestLimiter,
                 String remoteAddress) throws Exception {
    checkParams(parameters);
    long timeStart = System.currentTimeMillis();
    UserLimits limits = ServerTools.getUserLimits(parameters, config);

    String requestId = httpExchange.getRequestHeaders().getFirst("X-Request-ID");

    // logging information
    String agent = parameters.get("useragent") != null ? parameters.get("useragent") : "-";
    Long agentId = null, userId = null;
    if (databaseLogger.isLogging()) {
      DatabaseAccess db = DatabaseAccess.getInstance();
      agentId = db.getOrCreateClientId(parameters.get("useragent"));
      userId = limits.getPremiumUid();
    }
    String referrer = httpExchange.getRequestHeaders().getFirst("Referer");
    String userAgent = httpExchange.getRequestHeaders().getFirst("User-Agent");

    if (aText.getPlainText().length() > limits.getMaxTextLength()) {
      String msg = "limit: " + limits.getMaxTextLength() + ", size: " + aText.getPlainText().length();
      databaseLogger.log(new DatabaseAccessLimitLogEntry("MaxCharacterSizeExceeded", logServerId, agentId, userId, msg, referrer, userAgent));
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.MAX_TEXT_SIZE);
      throw new TextTooLongException("Your text exceeds the limit of " + limits.getMaxTextLength() +
              " characters (it's " + aText.getPlainText().length() + " characters). Please submit a shorter text.");
    }

    boolean filterDictionaryMatches = "true".equals(parameters.get("filterDictionaryMatches"));

    Long textSessionId = null;
    try {
      if (parameters.containsKey("textSessionId")) {
        String textSessionIdStr = parameters.get("textSessionId");
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
            logger.warn(String.format("Could not transform textSessionId '%s'", textSessionIdStr));
          }
          textSessionId = segmentOffset + timestamp;
        } else {
          textSessionId = Long.valueOf(textSessionIdStr);
        }
      }
    } catch (NumberFormatException ex) {
      logger.warn("Could not parse textSessionId '" + parameters.get("textSessionId") + "' as long: " + ex.getMessage());
    }

    String abTest = null;
    if (agent != null && config.getAbTestClients() != null && config.getAbTestClients().matcher(agent).matches()) {
      boolean testRolledOut;
      // partial rollout; deterministic if textSessionId given to make testing easier
      if (textSessionId != null) {
        testRolledOut = textSessionId % 100 < config.getAbTestRollout();
      } else {
        testRolledOut = random.nextInt(100) < config.getAbTestRollout();
      }
      if (testRolledOut) {
        abTest = config.getAbTest();
      }
    }

    UserConfig userConfig = new UserConfig(
            limits.getPremiumUid() != null ? getUserDictWords(limits.getPremiumUid()) : Collections.emptyList(),
            getRuleValues(parameters), config.getMaxSpellingSuggestions(), null, null, filterDictionaryMatches,
      abTest, textSessionId);

    //print("Check start: " + text.length() + " chars, " + langParam);
    boolean autoDetectLanguage = getLanguageAutoDetect(parameters);
    List<String> preferredVariants = getPreferredVariants(parameters);
    if (parameters.get("noopLanguages") != null && !autoDetectLanguage) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new BadRequestException("You can specify 'noopLanguages' only when also using 'language=auto'");
    }
    List<String> noopLangs = parameters.get("noopLanguages") != null ?
            Arrays.asList(parameters.get("noopLanguages").split(",")) : Collections.emptyList();
    List<String> preferredLangs = parameters.get("preferredLanguages") != null ?
            Arrays.asList(parameters.get("preferredLanguages").split(",")) : Collections.emptyList();
    DetectedLanguage detLang = getLanguage(aText.getPlainText(), parameters, preferredVariants, noopLangs, preferredLangs,
      parameters.getOrDefault("ld", "control").equalsIgnoreCase("test"));
    Language lang = detLang.getGivenLanguage();

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
    String motherTongueParam = parameters.get("motherTongue");
    Language motherTongue = motherTongueParam != null ? parseLanguage(motherTongueParam) : null;
    boolean useEnabledOnly = "yes".equals(parameters.get("enabledOnly")) || "true".equals(parameters.get("enabledOnly"));
    List<Language> altLanguages = new ArrayList<>();
    if (parameters.get("altLanguages") != null) {
      String[] altLangParams = parameters.get("altLanguages").split(",\\s*");
      for (String langCode : altLangParams) {
        Language altLang = parseLanguage(langCode);
        altLanguages.add(altLang);
        if (altLang.hasVariant() && !altLang.isVariant()) {
          ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
          throw new BadRequestException("You specified altLanguage '" + langCode + "', but for this language you need to specify a variant, e.g. 'en-GB' instead of just 'en'");
        }
      }
    }
    List<String> enabledRules = getEnabledRuleIds(parameters);

    List<String> disabledRules = getDisabledRuleIds(parameters);
    List<CategoryId> enabledCategories = getCategoryIds("enabledCategories", parameters);
    List<CategoryId> disabledCategories = getCategoryIds("disabledCategories", parameters);

    if ((disabledRules.size() > 0 || disabledCategories.size() > 0) && useEnabledOnly) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new BadRequestException("You cannot specify disabled rules or categories using enabledOnly=true");
    }
    if (enabledRules.isEmpty() && enabledCategories.isEmpty() && useEnabledOnly) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new BadRequestException("You must specify enabled rules or categories when using enabledOnly=true");
    }

    boolean enableTempOffRules = "true".equals(parameters.get("enableTempOffRules"));
    boolean useQuerySettings = enabledRules.size() > 0 || disabledRules.size() > 0 ||
            enabledCategories.size() > 0 || disabledCategories.size() > 0 || enableTempOffRules;
    boolean allowIncompleteResults = "true".equals(parameters.get("allowIncompleteResults"));
    boolean enableHiddenRules = "true".equals(parameters.get("enableHiddenRules"));
    JLanguageTool.Mode mode = ServerTools.getMode(parameters);
    JLanguageTool.Level level = ServerTools.getLevel(parameters);
    String callback = parameters.get("callback");
    // allowed to log input on errors?
    boolean inputLogging = !parameters.getOrDefault("inputLogging", "").equals("no");
    QueryParams params = new QueryParams(altLanguages, enabledRules, disabledRules,
      enabledCategories, disabledCategories, useEnabledOnly,
      useQuerySettings, allowIncompleteResults, enableHiddenRules, enableTempOffRules, mode, level, callback, inputLogging);

    int textSize = aText.getPlainText().length();

    List<CheckResults> ruleMatchesSoFar = Collections.synchronizedList(new ArrayList<>());

    Future<List<CheckResults>> future = executorService.submit(new Callable<List<CheckResults>>() {
      @Override
      public List<CheckResults> call() throws Exception {
        // use to fake OOM in thread for testing:
        /*if (Math.random() < 0.1) {
          throw new OutOfMemoryError();
        }*/
        List<CheckResults> results = getRuleMatches(aText, lang, motherTongue, parameters, params, userConfig, detLang, preferredLangs,
                preferredVariants, f -> ruleMatchesSoFar.add(new CheckResults(Collections.singletonList(f), Collections.emptyList())));
        // generate suggestions, otherwise this is not part of the timeout logic and not properly measured in the metrics
        results.stream().flatMap(r -> r.getRuleMatches().stream()).forEach(RuleMatch::computeLazySuggestedReplacements);
        return results;
      }
    });
    String incompleteResultReason = null;
    List<CheckResults> res;
    try {
      if (limits.getMaxCheckTimeMillis() < 0) {
        res = future.get();
      } else {
        res = future.get(limits.getMaxCheckTimeMillis(), TimeUnit.MILLISECONDS);
      }
    } catch (ExecutionException e) {
      future.cancel(true);
      if (ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
        ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.TOO_MANY_ERRORS);
        databaseLogger.log(new DatabaseCheckErrorLogEntry("ErrorRateTooHigh", logServerId, agentId, userId, lang, detLang.getDetectedLanguage(), textSize, "matches: " + ruleMatchesSoFar.size()));
      }
      if (params.allowIncompleteResults && ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
        logger.warn(e.getMessage() + " - returning " + ruleMatchesSoFar.size() + " matches found so far. " +
          "Detected language: " + detLang + ", " + ServerTools.getLoggingInfo(remoteAddress, null, -1, httpExchange,
          parameters, System.currentTimeMillis()-timeStart, reqCounter));
        res = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
        incompleteResultReason = "Results are incomplete: " + ExceptionUtils.getRootCause(e).getMessage();
      } else if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
        throw (OutOfMemoryError)e.getCause();
      } else {
        throw new RuntimeException(ServerTools.cleanUserTextFromMessage(e.getMessage(), parameters) + ", detected: " + detLang, e);
      }
    } catch (TimeoutException e) {
      boolean cancelled = future.cancel(true);
      Path loadFile = Paths.get("/proc/loadavg");  // works in Linux only(?)
      String loadInfo = loadFile.toFile().exists() ? Files.readAllLines(loadFile).toString() : "(unknown)";
      if (errorRequestLimiter != null) {
        errorRequestLimiter.logAccess(remoteAddress, httpExchange.getRequestHeaders(), parameters);
      }
      String message = "Text checking took longer than allowed maximum of " + limits.getMaxCheckTimeMillis() +
                       " milliseconds (cancelled: " + cancelled +
                       ", lang: " + lang.getShortCodeWithCountryAndVariant() +
                       ", detected: " + detLang +
                       ", #" + count +
                       ", " + aText.getPlainText().length() + " characters of text" +
                       ", mode: " + mode.toString().toLowerCase() +
                       ", h: " + reqCounter.getHandleCount() +
                       ", r: " + reqCounter.getRequestCount() +
                       ", requestId: " + requestId +
                       ", system load: " + loadInfo + ")";
      if (params.allowIncompleteResults) {
        logger.info(message + " - returning " + ruleMatchesSoFar.size() + " matches found so far");
        res = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
        incompleteResultReason = "Results are incomplete: text checking took longer than allowed maximum of " +
                String.format(Locale.ENGLISH, "%.2f", limits.getMaxCheckTimeMillis()/1000.0) + " seconds";
      } else {
        ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.MAX_CHECK_TIME);
        databaseLogger.log(new DatabaseCheckErrorLogEntry("MaxCheckTimeExceeded",
          logServerId, agentId, limits.getPremiumUid(), lang, detLang.getDetectedLanguage(), textSize, "load: "+ loadInfo));
        throw new RuntimeException(message, e);
      }
    }

    setHeaders(httpExchange);

    List<RuleMatch> hiddenMatches = new ArrayList<>();
    if (config.getHiddenMatchesServer() != null && params.enableHiddenRules &&
      config.getHiddenMatchesLanguages().contains(lang)) {
      if (config.getHiddenMatchesServerFailTimeout() > 0 && lastHiddenMatchesServerTimeout != -1 &&
        System.currentTimeMillis() - lastHiddenMatchesServerTimeout < config.getHiddenMatchesServerFailTimeout()) {
        ServerMetricsCollector.getInstance().logHiddenServerStatus(false);
        ServerMetricsCollector.getInstance().logHiddenServerRequest(false, lang, 0);
        logger.warn("Warn: Skipped querying hidden matches server at " +
          config.getHiddenMatchesServer() + " because of recent error/timeout (timeout=" + config.getHiddenMatchesServerFailTimeout() + "ms).");
      } else {
        ResultExtender resultExtender = new ResultExtender(config.getHiddenMatchesServer(), config.getHiddenMatchesServerTimeout());
        try {
          long start = System.currentTimeMillis();
          List<RemoteRuleMatch> extensionMatches = resultExtender.getExtensionMatches(aText.getPlainText(), parameters);
          List<RuleMatch> matches = new ArrayList<>();
          for (CheckResults result : res) {
            matches.addAll(result.getRuleMatches());
          }
          hiddenMatches = resultExtender.getFilteredExtensionMatches(matches, extensionMatches);
          long end = System.currentTimeMillis();
          logger.info("Hidden matches: " + extensionMatches.size() + " -> " + hiddenMatches.size() + " in " + (end - start) + "ms for " + lang.getShortCodeWithCountryAndVariant());
          ServerMetricsCollector.getInstance().logHiddenServerStatus(true);
          lastHiddenMatchesServerTimeout = -1;
          hiddenMatchesServerFailures = 0;
          ServerMetricsCollector.getInstance().logHiddenServerRequest(true, lang, hiddenMatches.size());
        } catch (Exception e) {
          ServerMetricsCollector.getInstance().logHiddenServerRequest(false, lang, 0);
          hiddenMatchesServerFailures++;
          if (hiddenMatchesServerFailures >= config.getHiddenMatchesServerFall()) {
            ServerMetricsCollector.getInstance().logHiddenServerStatus(false);
            logger.warn("Failed to query hidden matches server at " + config.getHiddenMatchesServer() + ": " + e.getClass() + ": " + e.getMessage() + ", input was " + aText.getPlainText().length() + " characters - marked as down now");
            lastHiddenMatchesServerTimeout = System.currentTimeMillis();
          } else {
            logger.warn("Failed to query hidden matches server at " + config.getHiddenMatchesServer() + ": " + e.getClass() + ": " + e.getMessage() + ", input was " + aText.getPlainText().length() + " characters - " + (config.getHiddenMatchesServerFall() - hiddenMatchesServerFailures) + " errors until marked as down");
          }
        }
      }
    }
    int compactMode = Integer.parseInt(parameters.getOrDefault("c", "0"));
    String response = getResponse(aText, lang, detLang, motherTongue, res, hiddenMatches, incompleteResultReason, compactMode, limits.getPremiumUid() == null);
    if (params.callback != null) {
      // JSONP - still needed today for the special case of hosting your own on-premise LT without SSL
      // and using it from a local MS Word (not Online Word) - issue #89 in the add-in repo:
      response = params.callback + "(" + response + ");";
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
    String version = parameters.get("v") != null ? ", v:" + parameters.get("v") : "";
    String skipLimits = limits.getSkipLimits() ? ", skipLimits" : "";
    
    int matchCount = 0;
    Map<String, Integer> ruleMatchCount = new HashMap<>();
    for (CheckResults r : res) {
      for (RuleMatch ruleMatch : r.getRuleMatches()) {
        matchCount++;
        String ruleId = ruleMatch.getRule().getId();
        ruleMatchCount.put(ruleId, ruleMatchCount.getOrDefault(ruleId, 0) + 1);
      }
    }
    logger.info("Check done: " + aText.getPlainText().length() + " chars, " + languageMessage +
        ", requestId: " + requestId + ", #" + count + ", " + referrer + ", "
        + matchCount + " matches, "
        + computationTime + "ms, agent:" + agent + version
        + ", " + messageSent + ", q:" + (workQueue != null ? workQueue.size() : "?")
        + ", h:" + reqCounter.getHandleCount() + ", dH:" + reqCounter.getDistinctIps()
        + ", m:" + ServerTools.getModeForLog(mode) + skipLimits);
    ServerMetricsCollector.getInstance().logCheck(
      lang, computationTime, textSize, matchCount, mode);
    
    if (!config.isSkipLoggingChecks()) {
      DatabaseCheckLogEntry logEntry = new DatabaseCheckLogEntry(userId, agentId, logServerId, textSize, matchCount,
        lang, detLang.getDetectedLanguage(), computationTime, textSessionId, mode.toString());
      logEntry.setRuleMatches(new DatabaseRuleMatchLogEntry(
        config.isSkipLoggingRuleMatches() ? Collections.emptyMap() : ruleMatchCount));
      databaseLogger.log(logEntry);
    }

    if (databaseLogger.isLogging()) {
      if (System.currentTimeMillis() - pingsCleanDateMillis > PINGS_CLEAN_MILLIS && pings.size() < PINGS_MAX_SIZE) {
        logger.info("Cleaning pings DB (" + pings.size() + " items)");
        pings.clear();
        pingsCleanDateMillis = System.currentTimeMillis();
      }
      if (agentId != null && userId != null) {
        DatabasePingLogEntry ping = new DatabasePingLogEntry(agentId, userId);
        if (!pings.contains(ping)) {
          databaseLogger.log(ping);
          if (pings.size() >= PINGS_MAX_SIZE) {
            // prevent pings taking up unlimited amounts of memory
            logger.warn("Pings DB has reached max size: " + pings.size());
          } else {
            pings.add(ping);
          }
        }
      }
    }

  }
  
  private Map<String, Integer> getRuleValues(Map<String, String> parameters) {
    Map<String, Integer> ruleValues = new HashMap<>();
    String parameterString = parameters.get("ruleValues");
    if (parameterString == null) {
      return ruleValues;
    }
    String[] pairs = parameterString.split("[,]");
    for (String pair : pairs) {
      String[] ruleAndValue  = pair.split("[:]");
      ruleValues.put(ruleAndValue[0], Integer.parseInt(ruleAndValue[1]));
    }
    return ruleValues;
  }

  private List<String> getUserDictWords(Long userId) {
    DatabaseAccess db = DatabaseAccess.getInstance();
    return db.getUserDictWords(userId);
  }

  protected void checkParams(Map<String, String> parameters) {
    if (parameters.get("text") == null && parameters.get("data") == null) {
      throw new BadRequestException("Missing 'text' or 'data' parameter");
    }
  }

  private List<CheckResults> getRuleMatches(AnnotatedText aText, Language lang,
                                         Language motherTongue, Map<String, String> parameters,
                                         QueryParams params, UserConfig userConfig,
                                         DetectedLanguage detLang,
                                         List<String> preferredLangs, List<String> preferredVariants,
                                         RuleMatchListener listener) throws Exception {
    if (cache != null && cache.requestCount() > 0 && cache.requestCount() % CACHE_STATS_PRINT == 0) {
      String sentenceHitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.getSentenceCache().stats().hitRate() * 100.0f);
      String matchesHitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.getMatchesCache().stats().hitRate() * 100.0f);
      String remoteHitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.getRemoteMatchesCache().stats().hitRate() * 100.0f);
      logger.info("Cache stats: " + sentenceHitPercentage + "% / " + matchesHitPercentage + "% / " + remoteHitPercentage + "% hit rate");
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

      if (preferredLangs.size() < 2 || parameters.get("multilingual") == null || parameters.get("multilingual").equals("false")) {
        res.addAll(getPipelineResults(aText, lang, motherTongue, params, userConfig, listener));
      } else {
        // support for multilingual texts:
        try {
          Language mainLang = getLanguageVariantForCode(detLang.getDetectedLanguage().getShortCode(), preferredVariants);
          List<Language> secondLangs = new ArrayList<>();
          for (String preferredLangCode : preferredLangs) {
            if (!preferredLangCode.equals(mainLang.getShortCode())) {
              secondLangs.add(getLanguageVariantForCode(preferredLangCode, preferredVariants));
              break;
            }
          }
          LanguageAnnotator annotator = new LanguageAnnotator();
          List<FragmentWithLanguage> fragments = annotator.detectLanguages(aText.getPlainText(), mainLang, secondLangs);
          List<Language> langs = new ArrayList<>();
          langs.add(mainLang);
          langs.addAll(secondLangs);
          Map<Language, AnnotatedTextBuilder> lang2builder = getBuilderMap(fragments, new HashSet<>(langs));
          for (Map.Entry<Language, AnnotatedTextBuilder> entry : lang2builder.entrySet()) {
            res.addAll(getPipelineResults(entry.getValue().build(), entry.getKey(), motherTongue, params, userConfig, listener));
          }
        } catch (Exception e) {
          logger.error("Problem with multilingual mode (preferredLangs=" + preferredLangs+ ", preferredVariants=" + preferredVariants + "), " +
            "falling back to single language.", e);
          res.addAll(getPipelineResults(aText, lang, motherTongue, params, userConfig, listener));
        }
      }
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
    PipelinePool.PipelineSettings settings = null;
    Pipeline lt = null;
    List<CheckResults> res = new ArrayList<>();
    try {
      settings = new PipelinePool.PipelineSettings(lang, motherTongue, params, config.globalConfig, userConfig);
      lt = pipelinePool.getPipeline(settings);
      Long textSessionId = userConfig.getTextSessionId();
      if (params.regressionTestMode) {
        textSessionId = -2L; // magic value for remote rule roll-out - includes all results, even from disabled models
      }
      res.add(lt.check2(aText, true, JLanguageTool.ParagraphHandling.NORMAL, listener,
        params.mode, params.level, executorService, textSessionId));
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
                                          List<String> noopLangs, List<String> preferredLangs, boolean testMode) {
    DetectedLanguage detected;
    //String mode;
    //long t1 = System.nanoTime();
    String cleanText = ngramIdentifier != null ? ngramIdentifier.cleanAndShortenText(text) : fastTextIdentifier.cleanAndShortenText(text);
    if (ngramIdentifier != null && cleanText.length() < NGRAM_THRESHOLD) {
      detected = ngramIdentifier.detectLanguage(cleanText, noopLangs, preferredLangs);
      //mode = "ngram";
    } else {
      detected = fastTextIdentifier.detectLanguage(cleanText, noopLangs, preferredLangs);
      //mode = fastTextIdentifier.isFastTextEnabled() ? "fasttext" : "built-in";
    }
    //long t2 = System.nanoTime();
    //float runTime = (t2-t1)/1000.0f/1000.0f;
    //System.out.printf(Locale.ENGLISH, "detected " + detected + " using " + mode + " in %.2fms for %d chars\n", runTime, text.length());
    Language lang;
    if (detected == null) {
      lang = parseLanguage(fallbackLanguage != null ? fallbackLanguage : "en");
    } else {
      lang = detected.getDetectedLanguage();
    }
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
    return new DetectedLanguage(null, lang, detected != null ? detected.getDetectionConfidence() : 0f);
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
    final boolean enableTempOffRules;
    final JLanguageTool.Mode mode;
    final JLanguageTool.Level level;
    final String callback;
    /** allowed to log input with stack traces to reproduce errors? */
    final boolean inputLogging;

    final boolean regressionTestMode; // no fallbacks for remote rules, retries, enable all rules

    QueryParams(List<Language> altLanguages, List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults, boolean enableHiddenRules, boolean enableTempOffRules, JLanguageTool.Mode mode, JLanguageTool.Level level, @Nullable String callback) {
      this(altLanguages, enabledRules, disabledRules, enabledCategories, disabledCategories, useEnabledOnly, useQuerySettings, allowIncompleteResults, enableHiddenRules, enableTempOffRules, mode, level, callback, true);
    }

    QueryParams(List<Language> altLanguages, List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults, boolean enableHiddenRules, boolean enableTempOffRules, JLanguageTool.Mode mode, JLanguageTool.Level level, @Nullable String callback, boolean inputLogging) {
      this.altLanguages = Objects.requireNonNull(altLanguages);
      this.enabledRules = enabledRules;
      this.disabledRules = disabledRules;
      this.enabledCategories = enabledCategories;
      this.disabledCategories = disabledCategories;
      this.useEnabledOnly = useEnabledOnly;
      this.useQuerySettings = useQuerySettings;
      this.allowIncompleteResults = allowIncompleteResults;
      this.enableHiddenRules = enableHiddenRules;
      this.enableTempOffRules = enableTempOffRules;
      this.regressionTestMode = enableTempOffRules;
      this.mode = Objects.requireNonNull(mode);
      this.level = Objects.requireNonNull(level);
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
