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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.*;
import org.languagetool.gui.Configuration;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.languagetool.server.ServerTools.print;

/**
 * @since 3.4
 */
abstract class TextChecker {

  protected abstract void setHeaders(HttpExchange httpExchange);
  protected abstract String getResponse(String text, Language lang, Language motherTongue, List<RuleMatch> matches,
                                        List<RuleMatch> hiddenMatches, String incompleteResultReason);
  @NotNull
  protected abstract List<String> getPreferredVariants(Map<String, String> parameters);
  protected abstract Language getLanguage(String text, Map<String, String> parameters, List<String> preferredVariants);
  protected abstract boolean getLanguageAutoDetect(Map<String, String> parameters);
  @NotNull
  protected abstract List<String> getEnabledRuleIds(Map<String, String> parameters);
  @NotNull
  protected abstract List<String> getDisabledRuleIds(Map<String, String> parameters);
    
  protected static final int CONTEXT_SIZE = 40; // characters

  protected final HTTPServerConfig config;

  private static final String ENCODING = "UTF-8";
  private static final int CACHE_STATS_PRINT = 500; // print cache stats every n cache requests 
  
  private final Map<String,Integer> languageCheckCounts = new HashMap<>(); 
  private final boolean internalServer;
  private Queue<Runnable> workQueue;
  private RequestCounter reqCounter;
  private final LanguageIdentifier identifier;
  private final ExecutorService executorService;
  private final ResultCache cache;

  TextChecker(HTTPServerConfig config, boolean internalServer, Queue<Runnable> workQueue, RequestCounter reqCounter) {
    this.config = config;
    this.internalServer = internalServer;
    this.workQueue = workQueue;
    this.reqCounter = reqCounter;
    this.identifier = new LanguageIdentifier();
    this.executorService = Executors.newCachedThreadPool();
    this.cache = config.getCacheSize() > 0 ? new ResultCache(config.getCacheSize()) : null;
  }

  void shutdownNow() {
    executorService.shutdownNow();
  }
  
  void checkText(AnnotatedText aText, HttpExchange httpExchange, Map<String, String> parameters, ErrorRequestLimiter errorRequestLimiter, String remoteAddress) throws Exception {
    checkParams(parameters);
    long timeStart = System.currentTimeMillis();
    UserLimits limits = getUserLimits(parameters);
    if (aText.getPlainText().length() > limits.getMaxTextLength()) {
      throw new TextTooLongException("Your text exceeds the limit of " + limits.getMaxTextLength() +
              " characters (it's " + aText.getPlainText().length() + " characters). Please submit a shorter text.");
    }
    //print("Check start: " + text.length() + " chars, " + langParam);
    boolean autoDetectLanguage = getLanguageAutoDetect(parameters);
    List<String> preferredVariants = getPreferredVariants(parameters);
    Language lang = getLanguage(aText.getPlainText(), parameters, preferredVariants);
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
    List<String> enabledRules = getEnabledRuleIds(parameters);

    List<String> disabledRules = getDisabledRuleIds(parameters);
    List<CategoryId> enabledCategories = getCategoryIds("enabledCategories", parameters);
    List<CategoryId> disabledCategories = getCategoryIds("disabledCategories", parameters);

    if ((disabledRules.size() > 0 || disabledCategories.size() > 0) && useEnabledOnly) {
      throw new IllegalArgumentException("You cannot specify disabled rules or categories using enabledOnly=true");
    }
    if (enabledRules.size() == 0 && enabledCategories.size() == 0 && useEnabledOnly) {
      throw new IllegalArgumentException("You must specify enabled rules or categories when using enabledOnly=true");
    }

    boolean useQuerySettings = enabledRules.size() > 0 || disabledRules.size() > 0 ||
            enabledCategories.size() > 0 || disabledCategories.size() > 0;
    boolean allowIncompleteResults = "true".equals(parameters.get("allowIncompleteResults"));
    QueryParams params = new QueryParams(enabledRules, disabledRules, enabledCategories, disabledCategories, useEnabledOnly, useQuerySettings, allowIncompleteResults);

    List<RuleMatch> ruleMatchesSoFar = Collections.synchronizedList(new ArrayList<>());
    
    Future<List<RemoteRuleMatch>> hiddenMatchesFuture = null;
    ResultExtender resultExtender = null;
    if (config.getHiddenMatchesServer() != null && config.getHiddenMatchesLanguages().contains(lang)) {
      resultExtender = new ResultExtender(config.getHiddenMatchesServer(), config.getHiddenMatchesServerTimeout());
      hiddenMatchesFuture = resultExtender.getExtensionMatches(aText.getPlainText(), lang);
    }

    Future<List<RuleMatch>> future = executorService.submit(new Callable<List<RuleMatch>>() {
      @Override
      public List<RuleMatch> call() throws Exception {
        // use to fake OOM in thread for testing:
        /*if (Math.random() < 0.1) {
          throw new OutOfMemoryError();
        }*/
        return getRuleMatches(aText, lang, motherTongue, params, f -> ruleMatchesSoFar.add(f));
      }
    });
    String incompleteResultReason = null;
    List<RuleMatch> matches;
    if (limits.getMaxCheckTimeMillis() < 0) {
      matches = future.get();
    } else {
      try {
        matches = future.get(limits.getMaxCheckTimeMillis(), TimeUnit.MILLISECONDS);
      } catch (ExecutionException e) {
        if (params.allowIncompleteResults && ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
          print(e.getMessage() + " - returning " + ruleMatchesSoFar.size() + " matches found so far");
          matches = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
          incompleteResultReason = "Results are incomplete: " + ExceptionUtils.getRootCause(e).getMessage();
        } else if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
          throw (OutOfMemoryError)e.getCause();
        } else {
          throw e;
        }
      } catch (TimeoutException e) {
        boolean cancelled = future.cancel(true);
        Path loadFile = Paths.get("/proc/loadavg");  // works in Linux only(?)
        String loadInfo = loadFile.toFile().exists() ? Files.readAllLines(loadFile).toString() : "(unknown)";
        if (errorRequestLimiter != null) {
          errorRequestLimiter.logAccess(remoteAddress);
        }
        String message = "Text checking took longer than allowed maximum of " + limits.getMaxCheckTimeMillis() +
                         " milliseconds (cancelled: " + cancelled +
                         ", language: " + lang.getShortCodeWithCountryAndVariant() + ", #" + count +
                         ", " + aText.getPlainText().length() + " characters of text" +
                         ", h: " + reqCounter.getHandleCount() + ", r: " + reqCounter.getRequestCount() + ", system load: " + loadInfo + ")";
        if (params.allowIncompleteResults) {
          print(message + " - returning " + ruleMatchesSoFar.size() + " matches found so far");
          matches = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
          incompleteResultReason = "Results are incomplete: text checking took longer than allowed maximum of " + 
                  String.format(Locale.ENGLISH, "%.2f", limits.getMaxCheckTimeMillis()/1000.0) + " seconds";
        } else {
          throw new RuntimeException(message, e);
        }
      }
    }

    setHeaders(httpExchange);
    List<RuleMatch> hiddenMatches = new ArrayList<>();
    if (resultExtender != null) {
      try {
        List<RemoteRuleMatch> tmpHiddenMatches = hiddenMatchesFuture.get(config.getHiddenMatchesServerTimeout(), TimeUnit.MILLISECONDS);
        hiddenMatches = resultExtender.getFilteredExtensionMatches(matches, tmpHiddenMatches);
      } catch (TimeoutException e) {
        print("Warn: Failed to query hidden matches server at " + config.getHiddenMatchesServer() +
              " due to timeout (" + config.getHiddenMatchesServerTimeout() + "ms): " + e.getMessage());
      } catch (Exception e) {
        print("Warn: Failed to query hidden matches server at " + config.getHiddenMatchesServer() + ": " + e.getMessage());
      }
    }
    String response = getResponse(aText.getPlainText(), lang, motherTongue, matches, hiddenMatches, incompleteResultReason);
    String messageSent = "sent";
    String languageMessage = lang.getShortCodeWithCountryAndVariant();
    String referrer = httpExchange.getRequestHeaders().getFirst("Referer");
    try {
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(response.getBytes(ENCODING));
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
    String agent = parameters.get("useragent") != null ? parameters.get("useragent") : "-";
    languageCheckCounts.put(lang.getShortCodeWithCountryAndVariant(), count);
    print("Check done: " + aText.getPlainText().length() + " chars, " + languageMessage + ", #" + count + ", " + referrer + ", "
            + matches.size() + " matches, "
            + (System.currentTimeMillis() - timeStart) + "ms, agent:" + agent
            + ", " + messageSent + ", q:" + (workQueue != null ? workQueue.size() : "?")
            + ", h:" + reqCounter.getHandleCount() + ", distinctH:" + reqCounter.getDistinctIps()
            + ", r:" + reqCounter.getRequestCount());
  }

  private UserLimits getUserLimits(Map<String, String> params) {
    String token = params.get("token");
    if (token != null) {
      return UserLimits.getLimitsFromToken(config, token);
    } else if (params.get("username") != null && params.get("password") != null) {
      return UserLimits.getLimitsFromUserAccount(config, params.get("username"), params.get("password"));
    } else {
      return UserLimits.getDefaultLimits(config);
    }
  }

  protected void checkParams(Map<String, String> parameters) {
    if (parameters.get("text") == null && parameters.get("data") == null) {
      throw new IllegalArgumentException("Missing 'text' or 'data' parameter");
    }
  }

  private List<RuleMatch> getRuleMatches(AnnotatedText aText, Language lang,
                                         Language motherTongue, QueryParams params, RuleMatchListener listener) throws Exception {
    if (cache != null && cache.requestCount() > 0 && cache.requestCount() % CACHE_STATS_PRINT == 0) {
      String hitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.hitRate() * 100.0f);
      print("Cache stats: " + hitPercentage + "% hit rate");
    }
    JLanguageTool lt = getLanguageToolInstance(lang, motherTongue, params);
    return lt.check(aText, listener);
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

  Language detectLanguageOfString(String text, String fallbackLanguage, List<String> preferredVariants) {
    Language lang = identifier.detectLanguage(text);
    if (lang == null) {
      lang = Languages.getLanguageForShortCode(fallbackLanguage != null ? fallbackLanguage : "en");
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
    return lang;
  }

  /**
   * Create a JLanguageTool instance for a specific language, mother tongue, and rule configuration.
   *
   * @param lang the language to be used
   * @param motherTongue the user's mother tongue or {@code null}
   */
  private JLanguageTool getLanguageToolInstance(Language lang, Language motherTongue, QueryParams params) throws Exception {
    JLanguageTool lt = new JLanguageTool(lang, motherTongue, cache);
    lt.setMaxErrorsPerWordRate(config.getMaxErrorsPerWordRate());
    if (config.getLanguageModelDir() != null) {
      lt.activateLanguageModelRules(config.getLanguageModelDir());
    }
    if (config.getWord2VecModelDir () != null) {
      lt.activateWord2VecModelRules(config.getWord2VecModelDir());
    }
    if (config.getRulesConfigFile() != null) {
      configureFromRulesFile(lt, lang);
    } else {
      configureFromGUI(lt, lang);
    }
    if (params.useQuerySettings) {
      Tools.selectRules(lt, new HashSet<>(params.disabledCategories), new HashSet<>(params.enabledCategories),
              new HashSet<>(params.disabledRules), new HashSet<>(params.enabledRules), params.useEnabledOnly);
    }
    return lt;
  }

  private void configureFromRulesFile(JLanguageTool langTool, Language lang) throws IOException {
    print("Using options configured in " + config.getRulesConfigFile());
    // If we are explicitly configuring from rules, ignore the useGUIConfig flag
    if (config.getRulesConfigFile() != null) {
      org.languagetool.gui.Tools.configureFromRules(langTool, new Configuration(config.getRulesConfigFile()
          .getCanonicalFile().getParentFile(), config.getRulesConfigFile().getName(), lang));
    } else {
      throw new RuntimeException("config.getRulesConfigFile() is null");
    }
  }

  private void configureFromGUI(JLanguageTool langTool, Language lang) throws IOException {
    Configuration config = new Configuration(lang);
    if (internalServer && config.getUseGUIConfig()) {
      print("Using options configured in the GUI");
      org.languagetool.gui.Tools.configureFromRules(langTool, config);
    }
  }

  private static class QueryParams {
    final List<String> enabledRules;
    final List<String> disabledRules;
    final List<CategoryId> enabledCategories;
    final List<CategoryId> disabledCategories;
    final boolean useEnabledOnly;
    final boolean useQuerySettings;
    final boolean allowIncompleteResults;

    QueryParams(List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults) {
      this.enabledRules = enabledRules;
      this.disabledRules = disabledRules;
      this.enabledCategories = enabledCategories;
      this.disabledCategories = disabledCategories;
      this.useEnabledOnly = useEnabledOnly;
      this.useQuerySettings = useQuerySettings;
      this.allowIncompleteResults = allowIncompleteResults;
    }
  }

}
