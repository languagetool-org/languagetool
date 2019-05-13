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
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.spelling.morfologik.suggestions_ordering.SuggestionsOrdererConfig;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.languagetool.server.ServerTools.print;

/**
 * @since 3.4
 */
abstract class TextChecker {

  protected abstract void setHeaders(HttpExchange httpExchange);
  protected abstract String getResponse(AnnotatedText text, DetectedLanguage lang, Language motherTongue, List<RuleMatch> matches,
                                        List<RuleMatch> hiddenMatches, String incompleteResultReason);
  @NotNull
  protected abstract List<String> getPreferredVariants(Map<String, String> parameters);
  protected abstract DetectedLanguage getLanguage(String text, Map<String, String> parameters, List<String> preferredVariants,
                                                  List<String> additionalDetectLangs, List<String> preferredLangs);
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
  private Queue<Runnable> workQueue;
  private RequestCounter reqCounter;
  // keep track of timeouts of the hidden matches server, check health periodically;
  // -1 => healthy, else => check timed out at given date, check back if time difference > config.getHiddenMatchesFailTimeout()
  private long lastHiddenMatchesServerTimeout;
  private final LanguageIdentifier identifier;
  private final ExecutorService executorService;
  private final ResultCache cache;
  private final DatabaseLogger logger;
  private final Long logServerId;
  PipelinePool pipelinePool; // mocked in test -> package-private / not final

  TextChecker(HTTPServerConfig config, boolean internalServer, Queue<Runnable> workQueue, RequestCounter reqCounter) {
    this.config = config;
    this.workQueue = workQueue;
    this.reqCounter = reqCounter;
    this.identifier = new LanguageIdentifier();
    this.identifier.enableFasttext(config.getFasttextBinary(), config.getFasttextModel());
    this.executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("lt-textchecker-thread-%d").build());
    this.cache = config.getCacheSize() > 0 ? new ResultCache(config.getCacheSize()) : null;
    this.logger = DatabaseLogger.getInstance();
    if (logger.isLogging()) {
      this.logServerId = DatabaseAccess.getInstance().getOrCreateServerId();
    } else {
      this.logServerId = null;
    }

    ServerMetricsCollector.getInstance().logHiddenServerConfiguration(config.getHiddenMatchesServer() != null);

    if (cache != null) {
      ServerMetricsCollector.getInstance().monitorCache("languagetool_matches_cache", cache.getMatchesCache());
      ServerMetricsCollector.getInstance().monitorCache("languagetool_sentences_cache", cache.getSentenceCache());
    }

    pipelinePool = new PipelinePool(config, cache, internalServer);
    if (config.isPipelinePrewarmingEnabled()) {
      ServerTools.print("Prewarming pipelines...");
      prewarmPipelinePool();
      ServerTools.print("Prewarming finished.");
    }
    if (config.getAbTest() != null) {
      ServerTools.print("A/B-Test enabled: " + config.getAbTest());
      if (config.getAbTest().equals("SuggestionsOrderer")) {
        SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(true);
      }
    }
    // enable logging after warmup to avoid false alarms
    if (config.getSlowRuleLoggingThreshold() >= 0) {
      //RuleLoggerManager.getInstance().addLogger(new SlowRuleLogger(this.logServerId, config.getSlowRuleLoggingThreshold()));
      RuleLoggerManager.getInstance().addLogger(new SlowRuleLogger(System.out, config.getSlowRuleLoggingThreshold()));
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
      for (JLanguageTool.Mode mode : addonModes) {
        QueryParams params = new QueryParams(Collections.emptyList(), Collections.emptyList(), addonDisabledRules,
          Collections.emptyList(), Collections.emptyList(), false, true,
          false, false, mode, null);
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
  }
  
  void checkText(AnnotatedText aText, HttpExchange httpExchange, Map<String, String> parameters, ErrorRequestLimiter errorRequestLimiter,
                 String remoteAddress) throws Exception {
    checkParams(parameters);
    long timeStart = System.currentTimeMillis();
    UserLimits limits = ServerTools.getUserLimits(parameters, config);

    // logging information
    String agent = parameters.get("useragent") != null ? parameters.get("useragent") : "-";
    Long agentId = null, userId = null;
    if (logger.isLogging()) {
      DatabaseAccess db = DatabaseAccess.getInstance();
      agentId = db.getOrCreateClientId(parameters.get("useragent"));
      userId = limits.getPremiumUid();
    }
    String referrer = httpExchange.getRequestHeaders().getFirst("Referer");
    String userAgent = httpExchange.getRequestHeaders().getFirst("User-Agent");

    if (aText.getPlainText().length() > limits.getMaxTextLength()) {
      String msg = "limit: " + limits.getMaxTextLength() + ", size: " + aText.getPlainText().length();
      logger.log(new DatabaseAccessLimitLogEntry("MaxCharacterSizeExceeded", logServerId, agentId, userId, msg, referrer, userAgent));
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.MAX_TEXT_SIZE);
      throw new TextTooLongException("Your text exceeds the limit of " + limits.getMaxTextLength() +
              " characters (it's " + aText.getPlainText().length() + " characters). Please submit a shorter text.");
    }
    UserConfig userConfig = new UserConfig(
            limits.getPremiumUid() != null ? getUserDictWords(limits.getPremiumUid()) : Collections.emptyList(),
            new HashMap<>(), config.getMaxSpellingSuggestions());

    // NOTE: at the moment, feedback for A/B-Tests is only delivered from this client, so only run tests there
    if (agent != null && agent.equals("ltorg")) {
      userConfig.setAbTest(config.getAbTest());
    }

    //print("Check start: " + text.length() + " chars, " + langParam);
    boolean autoDetectLanguage = getLanguageAutoDetect(parameters);
    List<String> preferredVariants = getPreferredVariants(parameters);
    if (parameters.get("noopLanguages") != null && !autoDetectLanguage) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new IllegalArgumentException("You can specify 'noopLanguages' only when also using 'language=auto'");
    }
    List<String> noopLangs = parameters.get("noopLanguages") != null ?
            Arrays.asList(parameters.get("noopLanguages").split(",")) : Collections.emptyList();
    List<String> preferredLangs = parameters.get("preferredLanguages") != null ?
            Arrays.asList(parameters.get("preferredLanguages").split(",")) : Collections.emptyList();
    DetectedLanguage detLang = getLanguage(aText.getPlainText(), parameters, preferredVariants, noopLangs, preferredLangs);
    Language lang = detLang.getGivenLanguage();
    Integer count = languageCheckCounts.get(lang.getShortCodeWithCountryAndVariant());
    if (count == null) {
      count = 1;
    } else {
      count++;
    }
    //print("Starting check: " + aText.getPlainText().length() + " chars, #" + count);
    String motherTongueParam = parameters.get("motherTongue");
    Language motherTongue = motherTongueParam != null ? Languages.getLanguageForShortCode(motherTongueParam) : null;
    boolean useEnabledOnly = "yes".equals(parameters.get("enabledOnly")) || "true".equals(parameters.get("enabledOnly"));
    List<Language> altLanguages = new ArrayList<>();
    if (parameters.get("altLanguages") != null) {
      String[] altLangParams = parameters.get("altLanguages").split(",\\s*");
      for (String langCode : altLangParams) {
        Language altLang = Languages.getLanguageForShortCode(langCode);
        altLanguages.add(altLang);
        if (altLang.hasVariant() && !altLang.isVariant()) {
          ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
          throw new IllegalArgumentException("You specified altLanguage '" + langCode + "', but for this language you need to specify a variant, e.g. 'en-GB' instead of just 'en'");
        }
      }
    }
    List<String> enabledRules = getEnabledRuleIds(parameters);

    List<String> disabledRules = getDisabledRuleIds(parameters);
    List<CategoryId> enabledCategories = getCategoryIds("enabledCategories", parameters);
    List<CategoryId> disabledCategories = getCategoryIds("disabledCategories", parameters);

    if ((disabledRules.size() > 0 || disabledCategories.size() > 0) && useEnabledOnly) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new IllegalArgumentException("You cannot specify disabled rules or categories using enabledOnly=true");
    }
    if (enabledRules.isEmpty() && enabledCategories.isEmpty() && useEnabledOnly) {
      ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.INVALID_REQUEST);
      throw new IllegalArgumentException("You must specify enabled rules or categories when using enabledOnly=true");
    }

    boolean useQuerySettings = enabledRules.size() > 0 || disabledRules.size() > 0 ||
            enabledCategories.size() > 0 || disabledCategories.size() > 0;
    boolean allowIncompleteResults = "true".equals(parameters.get("allowIncompleteResults"));
    boolean enableHiddenRules = "true".equals(parameters.get("enableHiddenRules"));
    JLanguageTool.Mode mode = ServerTools.getMode(parameters);
    String callback = parameters.get("callback");
    QueryParams params = new QueryParams(altLanguages, enabledRules, disabledRules, enabledCategories, disabledCategories,
            useEnabledOnly, useQuerySettings, allowIncompleteResults, enableHiddenRules, mode, callback);

    Long textSessionId = null;
    try {
      if (parameters.containsKey("textSessionId")) {
        String textSessionIdStr = parameters.get("textSessionId");
        if (textSessionIdStr.contains(":")) { // transitioning to new format used in chrome addon
          // format: "{random number in 0..99999}:{unix time}"
          long random, timestamp;
          int sepPos = textSessionIdStr.indexOf(':');
          random = Long.valueOf(textSessionIdStr.substring(0, sepPos));
          timestamp = Long.valueOf(textSessionIdStr.substring(sepPos + 1));
          // use random number to choose a slice in possible range of values
          // then choose position in slice by timestamp
          long maxRandom = 100000;
          long randomSegmentSize = (Long.MAX_VALUE - maxRandom) / maxRandom;
          long segmentOffset = random * randomSegmentSize;
          if (timestamp > randomSegmentSize) {
            print(String.format("Could not transform textSessionId '%s'", textSessionIdStr));
          }
          textSessionId = segmentOffset + timestamp;
        } else {
          textSessionId = Long.valueOf(textSessionIdStr);
        }

        userConfig.setTextSessionId(textSessionId);
      }
    } catch (NumberFormatException ex) {
      print("Could not parse textSessionId '" + parameters.get("textSessionId") + "' as long: " + ex.getMessage());
    }
    int textSize = aText.getPlainText().length();

    List<RuleMatch> ruleMatchesSoFar = Collections.synchronizedList(new ArrayList<>());
    
    Future<List<RuleMatch>> future = executorService.submit(new Callable<List<RuleMatch>>() {
      @Override
      public List<RuleMatch> call() throws Exception {
        // use to fake OOM in thread for testing:
        /*if (Math.random() < 0.1) {
          throw new OutOfMemoryError();
        }*/
        return getRuleMatches(aText, lang, motherTongue, parameters, params, userConfig, f -> ruleMatchesSoFar.add(f));
      }
    });
    String incompleteResultReason = null;
    List<RuleMatch> matches;
    try {
      if (limits.getMaxCheckTimeMillis() < 0) {
        matches = future.get();
      } else {
        matches = future.get(limits.getMaxCheckTimeMillis(), TimeUnit.MILLISECONDS);
      }
    } catch (ExecutionException e) {
      future.cancel(true);
      if (ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
        ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.TOO_MANY_ERRORS);
        logger.log(new DatabaseCheckErrorLogEntry("ErrorRateTooHigh", logServerId, agentId, userId, lang, detLang.getDetectedLanguage(), textSize, "matches: " + ruleMatchesSoFar.size()));
      }
      if (params.allowIncompleteResults && ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
        print(e.getMessage() + " - returning " + ruleMatchesSoFar.size() + " matches found so far. Detected language: " + detLang);
        matches = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
        incompleteResultReason = "Results are incomplete: " + ExceptionUtils.getRootCause(e).getMessage();
      } else if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
        throw (OutOfMemoryError)e.getCause();
      } else {
        throw new RuntimeException(e.getMessage() + ", detected: " + detLang, e);
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
                       ", h: " + reqCounter.getHandleCount() + ", r: " + reqCounter.getRequestCount() + ", system load: " + loadInfo + ")";
      if (params.allowIncompleteResults) {
        print(message + " - returning " + ruleMatchesSoFar.size() + " matches found so far");
        matches = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
        incompleteResultReason = "Results are incomplete: text checking took longer than allowed maximum of " + 
                String.format(Locale.ENGLISH, "%.2f", limits.getMaxCheckTimeMillis()/1000.0) + " seconds";
      } else {
        ServerMetricsCollector.getInstance().logRequestError(ServerMetricsCollector.RequestErrorType.MAX_CHECK_TIME);
        logger.log(new DatabaseCheckErrorLogEntry("MaxCheckTimeExceeded",
          logServerId, agentId, limits.getPremiumUid(), lang, detLang.getDetectedLanguage(), textSize, "load: "+ loadInfo));
        throw new RuntimeException(message, e);
      }
    }

    setHeaders(httpExchange);

    List<RuleMatch> hiddenMatches = new ArrayList<>();
    if (config.getHiddenMatchesServer() != null && params.enableHiddenRules &&
      config.getHiddenMatchesLanguages().contains(lang)) {
      if(config.getHiddenMatchesServerFailTimeout() > 0 && lastHiddenMatchesServerTimeout != -1 &&
        System.currentTimeMillis() - lastHiddenMatchesServerTimeout < config.getHiddenMatchesServerFailTimeout()) {
        ServerMetricsCollector.getInstance().logHiddenServerStatus(false);
        print("Warn: Skipped querying hidden matches server at " +
          config.getHiddenMatchesServer() + " because of recent error/timeout (timeout=" + config.getHiddenMatchesServerFailTimeout() + "ms).");
      } else {
        ResultExtender resultExtender = new ResultExtender(config.getHiddenMatchesServer(), config.getHiddenMatchesServerTimeout());
        try {
          long start = System.currentTimeMillis();
          List<RemoteRuleMatch> extensionMatches = resultExtender.getExtensionMatches(aText.getPlainText(), parameters);
          hiddenMatches = resultExtender.getFilteredExtensionMatches(matches, extensionMatches);
          long end = System.currentTimeMillis();
          print("Hidden matches: " + extensionMatches.size() + " -> " + hiddenMatches.size() + " in " + (end - start) + "ms for " + lang.getShortCodeWithCountryAndVariant());
          ServerMetricsCollector.getInstance().logHiddenServerStatus(true);
          lastHiddenMatchesServerTimeout = -1;
        } catch (Exception e) {
          ServerMetricsCollector.getInstance().logHiddenServerStatus(false);
          print("Warn: Failed to query hidden matches server at " + config.getHiddenMatchesServer() + ": " + e.getClass() + ": " + e.getMessage());
          lastHiddenMatchesServerTimeout = System.currentTimeMillis();
        }
      }
    }
    String response = getResponse(aText, detLang, motherTongue, matches, hiddenMatches, incompleteResultReason);
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
    print("Check done: " + aText.getPlainText().length() + " chars, " + languageMessage + ", #" + count + ", " + referrer + ", "
            + matches.size() + " matches, "
            + computationTime + "ms, agent:" + agent + version
            + ", " + messageSent + ", q:" + (workQueue != null ? workQueue.size() : "?")
            + ", h:" + reqCounter.getHandleCount() + ", dH:" + reqCounter.getDistinctIps()
            + ", m:" + mode.toString().toLowerCase());

    int matchCount = matches.size();
    Map<String, Integer> ruleMatchCount = new HashMap<>();
    for (RuleMatch match : matches) {
      String ruleId = match.getRule().getId();
      ruleMatchCount.put(ruleId, ruleMatchCount.getOrDefault(ruleId, 0) + 1);
    }

    ServerMetricsCollector.getInstance().logCheck(
      lang, computationTime, textSize, matchCount, mode, agent, ruleMatchCount);

    if (!config.isSkipLoggingChecks()) {
      DatabaseCheckLogEntry logEntry = new DatabaseCheckLogEntry(userId, agentId, logServerId, textSize, matchCount,
        lang, detLang.getDetectedLanguage(), computationTime, textSessionId, mode.toString());
      logEntry.setRuleMatches(new DatabaseRuleMatchLogEntry(
        config.isSkipLoggingRuleMatches() ? Collections.emptyMap() : ruleMatchCount));
      logger.log(logEntry);
    }
  }

  private List<String> getUserDictWords(Long userId) {
    DatabaseAccess db = DatabaseAccess.getInstance();
    return db.getUserDictWords(userId);
  }

  protected void checkParams(Map<String, String> parameters) {
    if (parameters.get("text") == null && parameters.get("data") == null) {
      throw new IllegalArgumentException("Missing 'text' or 'data' parameter");
    }
  }

  private List<RuleMatch> getRuleMatches(AnnotatedText aText, Language lang,
                                         Language motherTongue, Map<String, String> parameters, 
                                         QueryParams params, UserConfig userConfig, RuleMatchListener listener) throws Exception {
    if (cache != null && cache.requestCount() > 0 && cache.requestCount() % CACHE_STATS_PRINT == 0) {
      double hitRate = cache.hitRate();
      String hitPercentage = String.format(Locale.ENGLISH, "%.2f", hitRate * 100.0f);
      print("Cache stats: " + hitPercentage + "% hit rate");
      //print("Matches    : " + cache.getMatchesCache().stats().hitRate() + " hit rate");
      //print("Sentences  : " + cache.getSentenceCache().stats().hitRate() + " hit rate");
      //print("Size       : " + cache.getMatchesCache().size() + " (matches cache), " + cache.getSentenceCache().size() + " (sentence cache)");
      //logger.log(new DatabaseCacheStatsLogEntry(logServerId, (float) hitRate));
    }
    PipelinePool.PipelineSettings settings = null;

    if (parameters.get("sourceText") != null) {
      if (parameters.get("sourceLanguage") == null) {
        throw new IllegalArgumentException("'sourceLanguage' parameter missing - must be set when 'sourceText' is set");
      }
      Language sourceLanguage = Languages.getLanguageForShortCode(parameters.get("sourceLanguage"));
      JLanguageTool sourceLt = new JLanguageTool(sourceLanguage);
      JLanguageTool targetLt = new JLanguageTool(lang);
      List<BitextRule> bitextRules = Tools.getBitextRules(sourceLanguage, lang);
      return Tools.checkBitext(parameters.get("sourceText"), aText.getPlainText(), sourceLt, targetLt, bitextRules);
    } else {
      Pipeline lt = null;
      try {
        settings = new PipelinePool.PipelineSettings(lang, motherTongue, params, config.globalConfig, userConfig);
        lt = pipelinePool.getPipeline(settings);
        return lt.check(aText, true, JLanguageTool.ParagraphHandling.NORMAL, listener, params.mode);
      } finally {
        if (lt != null) {
          pipelinePool.returnPipeline(settings, lt);
        }
      }
    }
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
    DetectedLanguage detected = identifier.detectLanguage(text, noopLangs, preferredLangs);
    Language lang;
    if (detected == null) {
      lang = Languages.getLanguageForShortCode(fallbackLanguage != null ? fallbackLanguage : "en");
    } else {
      lang = detected.getDetectedLanguage();
    }
    if (preferredVariants.size() > 0) {
      for (String preferredVariant : preferredVariants) {
        if (!preferredVariant.contains("-")) {
          throw new IllegalArgumentException("Invalid format for 'preferredVariants', expected a dash as in 'en-GB': '" + preferredVariant + "'");
        }
        String preferredVariantLang = preferredVariant.split("-")[0];
        if (preferredVariantLang.equals(lang.getShortCode())) {
          lang = Languages.getLanguageForShortCode(preferredVariant);
          if (lang == null) {
            throw new IllegalArgumentException("Invalid 'preferredVariants', no such language/variant found: '" + preferredVariant + "'");
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
    final JLanguageTool.Mode mode;
    final String callback;

    QueryParams(List<Language> altLanguages, List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults, boolean enableHiddenRules, JLanguageTool.Mode mode, @Nullable String callback) {
      this.altLanguages = Objects.requireNonNull(altLanguages);
      this.enabledRules = enabledRules;
      this.disabledRules = disabledRules;
      this.enabledCategories = enabledCategories;
      this.disabledCategories = disabledCategories;
      this.useEnabledOnly = useEnabledOnly;
      this.useQuerySettings = useQuerySettings;
      this.allowIncompleteResults = allowIncompleteResults;
      this.enableHiddenRules = enableHiddenRules;
      this.mode = Objects.requireNonNull(mode);
      if (callback != null && !callback.matches("[a-zA-Z]+")) {
        throw new IllegalArgumentException("'callback' value must match [a-zA-Z]+: '" + callback + "'");
      }
      this.callback = callback;
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
        .append(mode)
        .append(callback)
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
        .append(mode, other.mode)
        .append(callback, other.callback)
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
        .append("mode", mode)
        .append("callback", mode)
        .build();
    }
  }

}
