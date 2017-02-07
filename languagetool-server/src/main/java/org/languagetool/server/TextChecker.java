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
import org.jetbrains.annotations.NotNull;
import org.languagetool.*;
import org.languagetool.gui.Configuration;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.*;

import static org.languagetool.server.ServerTools.print;

/**
 * @since 3.4
 */
abstract class TextChecker {

  protected abstract void setHeaders(HttpExchange httpExchange);
  protected abstract String getResponse(String text, Language lang, Language motherTongue, List<RuleMatch> matches);
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
  
  private final boolean internalServer;
  private final LanguageIdentifier identifier;
  private final ExecutorService executorService;
  private final ResultCache cache;

  TextChecker(HTTPServerConfig config, boolean internalServer) {
    this.config = config;
    this.internalServer = internalServer;
    this.identifier = new LanguageIdentifier();
    this.executorService = Executors.newCachedThreadPool();
    this.cache = config.getCacheSize() > 0 ? new ResultCache(config.getCacheSize()) : null;
  }

  void shutdownNow() {
    executorService.shutdownNow();
  }
  
  void checkText(String text, HttpExchange httpExchange, Map<String, String> parameters) throws Exception {
    checkParams(parameters);
    long timeStart = System.currentTimeMillis();
    if (text.length() > config.maxTextLength) {
      throw new TextTooLongException("Your text exceeds this server's limit of " + config.maxTextLength +
              " characters (it's " + text.length() + " characters). Please submit a shorter text.");
    }
    //print("Check start: " + text.length() + " chars, " + langParam);
    boolean autoDetectLanguage = getLanguageAutoDetect(parameters);
    List<String> preferredVariants = getPreferredVariants(parameters);
    Language lang = getLanguage(text, parameters, preferredVariants);
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
    QueryParams params = new QueryParams(enabledRules, disabledRules, enabledCategories, disabledCategories, useEnabledOnly, useQuerySettings);

    Future<List<RuleMatch>> future = executorService.submit(new Callable<List<RuleMatch>>() {
      @Override
      public List<RuleMatch> call() throws Exception {
        // use to fake OOM in thread for testing:
        /*if (Math.random() < 0.1) {
          throw new OutOfMemoryError();
        }*/
        return getRuleMatches(text, parameters, lang, motherTongue, params);
      }
    });
    List<RuleMatch> matches;
    if (config.maxCheckTimeMillis < 0) {
      matches = future.get();
    } else {
      try {
        matches = future.get(config.maxCheckTimeMillis, TimeUnit.MILLISECONDS);
      } catch (ExecutionException e) {
        if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
          throw (OutOfMemoryError)e.getCause();
        } else {
          throw e;
        }
      } catch (TimeoutException e) {
        boolean cancelled = future.cancel(true);
        throw new RuntimeException("Text checking took longer than allowed maximum of " + config.maxCheckTimeMillis +
                " milliseconds (cancelled: " + cancelled +
                ", language: " + lang.getShortCodeWithCountryAndVariant() +
                ", " + text.length() + " characters of text)", e);
      }
    }

    setHeaders(httpExchange);
    String xmlResponse = getResponse(text, lang, motherTongue, matches);
    String messageSent = "sent";
    String languageMessage = lang.getShortCodeWithCountryAndVariant();
    String referrer = httpExchange.getRequestHeaders().getFirst("Referer");
    try {
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, xmlResponse.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(xmlResponse.getBytes(ENCODING));
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
    String clazz = this.getClass().getSimpleName();
    print("Check done: " + text.length() + " chars, " + languageMessage + ", " + referrer + ", "
            + matches.size() + " matches, "
            + (System.currentTimeMillis() - timeStart) + "ms, class: " + clazz + ", agent:" + agent
            + ", " + messageSent);
  }

  protected void checkParams(Map<String, String> parameters) {
    if (parameters.get("text") == null) {
      throw new IllegalArgumentException("Missing 'text' parameter");
    }
  }

  private List<RuleMatch> getRuleMatches(String text, Map<String, String> parameters, Language lang,
                                         Language motherTongue, QueryParams params) throws Exception {
    if (cache != null && cache.requestCount() % CACHE_STATS_PRINT == 0) {
      String hitPercentage = String.format(Locale.ENGLISH, "%.2f", cache.hitRate() * 100.0f);
      print("Cache stats: " + hitPercentage + "% hit rate");
    }
    String sourceText = parameters.get("srctext");
    if (sourceText == null) {
      JLanguageTool lt = getLanguageToolInstance(lang, motherTongue, params);
      return lt.check(text);
    } else {
      if (parameters.get("motherTongue") == null) {
        throw new IllegalArgumentException("Missing 'motherTongue' parameter for bilingual checks");
      }
      print("Checking bilingual text, with source length " + sourceText.length() +
              " and target length " + text.length() + " (characters), source language " +
              motherTongue + " and target language " + lang.getShortCodeWithCountryAndVariant());
      JLanguageTool sourceLt = getLanguageToolInstance(motherTongue, null, params);
      JLanguageTool targetLt = getLanguageToolInstance(lang, null, params);
      List<BitextRule> bRules = Tools.selectBitextRules(Tools.getBitextRules(motherTongue, lang),
              params.disabledRules, params.enabledRules, params.useEnabledOnly);
      return Tools.checkBitext(sourceText, text, sourceLt, targetLt, bRules);
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
    if (config.getLanguageModelDir() != null) {
      lt.activateLanguageModelRules(config.getLanguageModelDir());
    }
    if (params.useQuerySettings) {
      Tools.selectRules(lt, new HashSet<>(params.disabledCategories), new HashSet<>(params.enabledCategories),
              new HashSet<>(params.disabledRules), new HashSet<>(params.enabledRules), params.useEnabledOnly);
    } else {
      if (config.getRulesConfigFile() != null) {
        configureFromRulesFile(lt, lang);
      } else {
        configureFromGUI(lt, lang);
      }
    }
    return lt;
  }

  private void configureFromRulesFile(JLanguageTool langTool, Language lang) throws IOException {
    print("Using options configured in " + config.getRulesConfigFile());
    // If we are explicitly configuring from rules, ignore the useGUIConfig flag
    if (config.getRulesConfigFile() != null) {
      org.languagetool.gui.Tools.configureFromRules(langTool, new Configuration(config.getRulesConfigFile().getParentFile(),
              config.getRulesConfigFile().getName(), lang));
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

    QueryParams(List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings) {
      this.enabledRules = enabledRules;
      this.disabledRules = disabledRules;
      this.enabledCategories = enabledCategories;
      this.disabledCategories = disabledCategories;
      this.useEnabledOnly = useEnabledOnly;
      this.useQuerySettings = useQuerySettings;
    }
  }

}
