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
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.gui.Configuration;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.tools.RuleMatchAsXmlSerializer;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.*;

import static org.languagetool.server.ServerTools.*;

/**
 * @since 3.4
 */
class TextChecker {

  private static final String ENCODING = "UTF-8";
  private static final String XML_CONTENT_TYPE = "text/xml; charset=UTF-8";
  private static final int CONTEXT_SIZE = 40; // characters
  
  private final HTTPServerConfig config;
  private final boolean internalServer;
  private final LanguageIdentifier identifier;
  private final ExecutorService executorService;

  TextChecker(HTTPServerConfig config, boolean internalServer) {
    this.config = config;
    this.internalServer = internalServer;
    this.identifier = new LanguageIdentifier();
    this.executorService = Executors.newCachedThreadPool();
  }

  void shutdownNow() {
    executorService.shutdownNow();
  }
  
  void checkText(String text, HttpExchange httpExchange, Map<String, String> parameters, int handleCount) throws Exception {
    long timeStart = System.currentTimeMillis();
    if (text.length() > config.maxTextLength) {
      throw new TextTooLongException("Your text exceeds this server's limit of " + config.maxTextLength +
              " characters (it's " + text.length() + " characters). Please submit a shorter text.");
    }
    //print("Check start: " + text.length() + " chars, " + langParam);
    boolean autoDetectLanguage = getLanguageAutoDetect(parameters);
    List<String> preferredVariants;
    if (parameters.get("preferredvariants") != null) {
      preferredVariants = Arrays.asList(parameters.get("preferredvariants").split(",\\s*"));
      if (!autoDetectLanguage) {
        throw new IllegalArgumentException("You specified 'preferredvariants' but you didn't specify 'autodetect=yes'");
      }
    } else {
      preferredVariants = Collections.emptyList();
    }
    Language lang = getLanguage(text, parameters.get("language"), autoDetectLanguage, preferredVariants);
    String motherTongueParam = parameters.get("motherTongue");
    Language motherTongue = motherTongueParam != null ? Languages.getLanguageForShortName(motherTongueParam) : null;
    boolean useEnabledOnly = "yes".equals(parameters.get("enabledOnly"));
    String enabledParam = parameters.get("enabled");
    List<String> enabledRules = new ArrayList<>();
    if (enabledParam != null) {
      enabledRules.addAll(Arrays.asList(enabledParam.split(",")));
    }

    List<String> disabledRules = getCommaSeparatedStrings("disabled", parameters);
    List<CategoryId> enabledCategories = getCategoryIds("enabledCategories", parameters);
    List<CategoryId> disabledCategories = getCategoryIds("disabledCategories", parameters);

    if ((disabledRules.size() > 0 || disabledCategories.size() > 0) && useEnabledOnly) {
      throw new IllegalArgumentException("You cannot specify disabled rules or categories using enabledOnly=yes");
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
                " milliseconds (cancelled: " + cancelled + ", handleCount: " + handleCount +
                ", language: " + lang.getShortNameWithCountryAndVariant() +
                ", " + text.length() + " characters of text)", e);
      }
    }

    setCommonHeaders(httpExchange, XML_CONTENT_TYPE, config.allowOriginUrl);
    String xmlResponse = getXmlResponse(text, lang, motherTongue, matches);
    String messageSent = "sent";
    String languageMessage = lang.getShortNameWithCountryAndVariant();
    String referrer = httpExchange.getRequestHeaders().getFirst("Referer");
    try {
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, xmlResponse.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(xmlResponse.getBytes(ENCODING));
      if (motherTongue != null) {
        languageMessage += " (mother tongue: " + motherTongue.getShortNameWithCountryAndVariant() + ")";
      }
      if (autoDetectLanguage) {
        languageMessage += "[auto]";
      }
    } catch (IOException exception) {
      // the client is disconnected
      messageSent = "notSent: " + exception.getMessage();
    }
    String agent = parameters.get("useragent") != null ? parameters.get("useragent") : "-";
    print("Check done: " + text.length() + " chars, " + languageMessage + ", " + referrer + ", "
            + "handlers:" + handleCount + ", " + matches.size() + " matches, "
            + (System.currentTimeMillis() - timeStart) + "ms, agent:" + agent
            + ", " + messageSent);
  }

  private boolean getLanguageAutoDetect(Map<String, String> parameters) {
    if (config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline) {
      return "true".equals(parameters.get("guess"));
    } else {
      boolean autoDetect = "1".equals(parameters.get("autodetect")) || "yes".equals(parameters.get("autodetect"));
      if (parameters.get("language") == null && !autoDetect) {
        throw new IllegalArgumentException("Missing 'language' parameter. Specify language or use 'autodetect=yes' for auto-detecting the language of the input text.");
      }
      return autoDetect;
    }
  }

  private Language getLanguage(String text, String langParam, boolean autoDetect, List<String> preferredVariants) {
    Language lang;
    if (autoDetect) {
      lang = detectLanguageOfString(text, langParam, preferredVariants);
    } else {
      if (config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline) {
        lang = config.getAfterTheDeadlineLanguage();
      } else {
        lang = Languages.getLanguageForShortName(langParam);
      }
    }
    return lang;
  }

  private List<RuleMatch> getRuleMatches(String text, Map<String, String> parameters, Language lang,
                                         Language motherTongue, QueryParams params) throws Exception {
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
              motherTongue + " and target language " + lang.getShortNameWithCountryAndVariant());
      JLanguageTool sourceLt = getLanguageToolInstance(motherTongue, null, params);
      JLanguageTool targetLt = getLanguageToolInstance(lang, null, params);
      List<BitextRule> bRules = Tools.selectBitextRules(Tools.getBitextRules(motherTongue, lang),
              params.disabledRules, params.enabledRules, params.useEnabledOnly);
      return Tools.checkBitext(sourceText, text, sourceLt, targetLt, bRules);
    }
  }

  private String getXmlResponse(String text, Language lang, Language motherTongue, List<RuleMatch> matches) {
    if (config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline) {
      AtDXmlSerializer serializer = new AtDXmlSerializer();
      return serializer.ruleMatchesToXml(matches, text);
    } else {
      RuleMatchAsXmlSerializer serializer = new RuleMatchAsXmlSerializer();
      return serializer.ruleMatchesToXml(matches, text, CONTEXT_SIZE, lang, motherTongue);
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
  private List<String> getCommaSeparatedStrings(String paramName, Map<String, String> parameters) {
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
      lang = Languages.getLanguageForShortName(fallbackLanguage != null ? fallbackLanguage : "en");
    }
    if (preferredVariants.size() > 0) {
      for (String preferredVariant : preferredVariants) {
        if (!preferredVariant.contains("-")) {
          throw new IllegalArgumentException("Invalid format for 'preferredvariant', expected a dash as in 'en-GB': '" + preferredVariant + "'");
        }
        String preferredVariantLang = preferredVariant.split("-")[0];
        if (preferredVariantLang.equals(lang.getShortName())) {
          lang = Languages.getLanguageForShortName(preferredVariant);
          if (lang == null) {
            throw new IllegalArgumentException("Invalid 'prefereredvariant', no such language/variant found: '" + preferredVariant + "'");
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
    JLanguageTool lt = new JLanguageTool(lang, motherTongue);
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
