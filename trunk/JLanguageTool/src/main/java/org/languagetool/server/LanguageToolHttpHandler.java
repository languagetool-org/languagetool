package org.languagetool.server;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.gui.Configuration;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class LanguageToolHttpHandler implements HttpHandler {

  private static final String CONTENT_TYPE_VALUE = "text/xml; charset=UTF-8";
  private static final String ENCODING = "utf-8";
  private static final int CONTEXT_SIZE = 40; // characters

  private final Set<String> allowedIps;  
  private final boolean verbose;
  private final boolean internalServer;
  
  private Configuration config;
  
  private boolean useQuerySettings;
  
  private String[] enabledRules = {};
  private String[] disabledRules = {};
 
  LanguageToolHttpHandler(boolean verbose, Set<String> allowedIps,
		  boolean internal) throws IOException {
    this.verbose = verbose;
    this.allowedIps = allowedIps;
    this.internalServer = internal;
    config = new Configuration(null);
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    final long timeStart = System.currentTimeMillis();
    String text = null;
    try {
      final URI requestedUri = httpExchange.getRequestURI();
      final Map<String, String> parameters = getRequestQuery(httpExchange, requestedUri);
      final String remoteAddress = httpExchange.getRemoteAddress().getAddress().getHostAddress();
      if (allowedIps.contains(remoteAddress)) {
        if (requestedUri.getRawPath().endsWith("/Languages")) {
          // request type: list known languages
          printListOfLanguages(httpExchange);
        } else {
          // request type: text checking
          text = parameters.get("text");
          if (text == null) {
            throw new IllegalArgumentException("Missing 'text' parameter");
          }
          checkText(text, httpExchange, parameters);
        }
      } else {
        final String errorMessage = "Error: Access from " + StringTools.escapeXML(remoteAddress) + " denied";
        sendError(httpExchange, HttpURLConnection.HTTP_FORBIDDEN, errorMessage);
        throw new RuntimeException(errorMessage);
      }
    } catch (Exception e) {
      if (verbose) {
        print("Exception was caused by this text: " + text);
      }
      e.printStackTrace();
      final String response = "Error: " + StringTools.escapeXML(Tools.getFullStackTrace(e));
      sendError(httpExchange, HttpURLConnection.HTTP_INTERNAL_ERROR, response);
    } finally {
      httpExchange.close();
    }
    print("Check done in " + (System.currentTimeMillis() - timeStart) + "ms");
  }

  private void sendError(HttpExchange httpExchange, int returnCode, String response) throws IOException {
    httpExchange.sendResponseHeaders(returnCode, response.getBytes().length);
    httpExchange.getResponseBody().write(response.getBytes());
  }

  private Map<String, String> getRequestQuery(HttpExchange httpExchange, URI requestedUri) throws IOException {
    Map<String, String> parameters;
    if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) { // POST
      final InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), ENCODING);
      try {
        final BufferedReader br = new BufferedReader(isr);
        try {
          final String query = br.readLine();
          parameters = parseQuery(query);
        } finally {
          br.close();
        }
      } finally {
        isr.close();
      }
    } else {   // GET
      final String query = requestedUri.getRawQuery();
      parameters = parseQuery(query);
    }
    return parameters;
  }

  private void printListOfLanguages(HttpExchange httpExchange) throws IOException {
    httpExchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE_VALUE);
    //httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    final String response = getSupportedLanguagesAsXML();
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
  }

  private void checkText(String text, HttpExchange httpExchange, Map<String, String> parameters) throws Exception {
    final String langParam = parameters.get("language");
    if (langParam == null) {
      throw new IllegalArgumentException("Missing 'language' parameter");
    }
    final Language lang = Language.getLanguageForShortName(langParam);
    if (lang == null) {
      throw new IllegalArgumentException("Unknown language '" + langParam + "'");
    }
    final String motherTongueParam = parameters.get("motherTongue");
    Language motherTongue = null;
    if (null != motherTongueParam) {
      motherTongue = Language.getLanguageForShortName(motherTongueParam);
    }

    final String enabledParam = parameters.get("enabled");
    enabledRules = new String[0];
    if (null != enabledParam) {
    	enabledRules = enabledParam.split(",");
    }
    
    final String disabledParam = parameters.get("disabled");
    disabledRules = new String[0];
    if (null != disabledParam) {
    	disabledRules = disabledParam.split(",");
    }
    
    useQuerySettings = enabledRules.length > 0 || disabledRules.length > 0; 
    
    final List<RuleMatch> matches;
    final String sourceText = parameters.get("srctext");
    if (sourceText == null) {
      final JLanguageTool lt = getLanguageToolInstance(lang, motherTongue);
      print("Checking " + text.length() + " characters of text, language " + langParam);
      matches = lt.check(text);
    } else {
      if (motherTongueParam == null) {
        throw new IllegalArgumentException("Missing 'motherTongue' for bilingual checks");
      }
      print("Checking bilingual text, with source length " + sourceText.length() +
          " and target length " + text.length() + " (characters), source language " +
          motherTongue + " and target language " + langParam);
      final JLanguageTool sourceLt = getLanguageToolInstance(motherTongue, null);
      final JLanguageTool targetLt = getLanguageToolInstance(lang, null);
      final List<BitextRule> bRules = Tools.getBitextRules(motherTongue, lang);
      matches = Tools.checkBitext(sourceText, text, sourceLt, targetLt, bRules);
    }
    httpExchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE_VALUE);
    //httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    final String response = StringTools.ruleMatchesToXML(matches, text,
            CONTEXT_SIZE, StringTools.XmlPrintMode.NORMAL_XML);
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
  }

  private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
    final Map<String, String> parameters = new HashMap<String, String>();
    if (query != null) {
      final String[] pairs = query.split("[&]");
      final Map<String, String> parameterMap = getParameterMap(pairs);
      parameters.putAll(parameterMap);
    }
    return parameters;
  }

  private Map<String, String> getParameterMap(String[] pairs) throws UnsupportedEncodingException {
    final Map<String, String> parameters = new HashMap<String, String>();
    for (String pair : pairs) {
      final int delimPos = pair.indexOf("=");
      if (delimPos != -1) {
        final String param = pair.substring(0, delimPos);
        final String key = URLDecoder.decode(param, ENCODING);
        final String value = URLDecoder.decode(pair.substring(delimPos + 1), ENCODING);
        parameters.put(key, value);
      }
    }
    return parameters;
  }

  private void print(String s) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat();
    final String now = dateFormat.format(new Date());
    System.out.println(now + " " + s);
  }

  /**
   * Find or create a JLanguageTool instance for a specific language and mother tongue.
   * The instance will be reused. If any customization is required (like disabled rules), 
   * it will be done after acquiring this instance.
   * 
   * @param lang the language to be used.
   * @param motherTongue the user's mother tongue or <code>null</code>
   * @return a JLanguageTool instance for a specific language and mother tongue.
   * @throws Exception when JLanguageTool creation failed
   */
  private JLanguageTool getLanguageToolInstance(Language lang, Language motherTongue) throws Exception {
    print("Creating JLanguageTool instance for language " + lang + ((null != motherTongue) ? (" and mother tongue " + motherTongue) : ""));
    final JLanguageTool newLanguageTool = new JLanguageTool(lang, motherTongue);
    newLanguageTool.activateDefaultPatternRules();
    newLanguageTool.activateDefaultFalseFriendRules();
    config = new Configuration(lang);
    if (!useQuerySettings && internalServer && config.getUseGUIConfig()) { // use the GUI config values
    	configureGUI(newLanguageTool);
    }
    
    if (useQuerySettings) {
    	Tools.selectRules(newLanguageTool, disabledRules, enabledRules);
    }
    
    return newLanguageTool;
  }

  private void configureGUI(JLanguageTool langTool) {
    print("Using options configured in the GUI");
    //TODO: add a parameter to config to set language
    final Set<String> disabledRules = config.getDisabledRuleIds();
    if (disabledRules != null) {
      for (final String ruleId : disabledRules) {
        langTool.disableRule(ruleId);
      }
    }
    final Set<String> disabledCategories = config.
            getDisabledCategoryNames();
    if (disabledCategories != null) {
      for (final String categoryName : disabledCategories) {
        langTool.disableCategory(categoryName);
      }
    }
    final Set<String> enabledRules = config.getEnabledRuleIds();
    if (enabledRules != null) {
      for (String ruleName : enabledRules) {
        langTool.enableDefaultOffRule(ruleName);
        langTool.enableRule(ruleName);
      }
    }
  }
 

  /**
   * Construct an XML string containing all supported languages. <br/>The XML format looks like this:<br/><br/>
   * &lt;languages&gt;<br/>
   *    &nbsp;&nbsp;&lt;language name="Catalan" abbr="ca" abbrWithVariant="ca-ES"/&gt;<br/>
   *    &nbsp;&nbsp;&lt;language name="German" abbr="de" abbrWithVariant="de"/&gt;<br/>
   *    &nbsp;&nbsp;&lt;language name="German (Germany)" abbr="de" abbrWithVariant="de-DE"/&gt;<br/>
   *  &lt;languages&gt;<br/><br/>
   *  The languages are sorted alphabetically by their name.
   * @return an XML document listing all supported languages
   */
  public static String getSupportedLanguagesAsXML() {
    final Language[] languageCopy = Language.REAL_LANGUAGES.clone();
    final List<Language> languages = Arrays.asList(languageCopy);
    Collections.sort(languages, new Comparator<Language>() {
      @Override
      public int compare(Language o1, Language o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    final StringBuilder xmlBuffer = new StringBuilder("<?xml version='1.0' encoding='" + ENCODING + "'?>\n<languages>\n");
    for (Language lang : languages) {
      xmlBuffer.append(String.format("\t<language name=\"%s\" abbr=\"%s\" abbrWithVariant=\"%s\"/> \n", lang.getName(),
              lang.getShortName(), lang.getShortNameWithVariant()));
    }
    xmlBuffer.append("</languages>\n");
    return xmlBuffer.toString();
  }
}
