package de.danielnaber.languagetool.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.bitext.BitextRule;
import de.danielnaber.languagetool.tools.StringTools;
import de.danielnaber.languagetool.tools.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

class LanguageToolHttpHandler implements HttpHandler {

  private static final String CONTENT_TYPE_VALUE = "text/xml; charset=UTF-8";
  private static final int CONTEXT_SIZE = 40; // characters
  
  private final Set<String> allowedIps;  
  private final boolean verbose;

  LanguageToolHttpHandler(boolean verbose, Set<String> allowedIps) {
    this.verbose = verbose;
    this.allowedIps = allowedIps;
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    final URI requestedUri = httpExchange.getRequestURI();
    final Map<String, String> parameters = getRequestQuery(httpExchange, requestedUri);
    final long timeStart = System.currentTimeMillis();
    String text = null;
    try {
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
      final InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
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
    final String response = getSupportedLanguagesAsXML();
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
    httpExchange.getResponseBody().write(response.getBytes());
    httpExchange.close();
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

    // TODO: how to take options from the client?
    // TODO: customize LT here after reading client options

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
          motherTongue + "and target language " + langParam);
      final JLanguageTool sourceLt = getLanguageToolInstance(motherTongue, null);
      final JLanguageTool targetLt = getLanguageToolInstance(lang, null);
      final List<BitextRule> bRules = Tools.getBitextRules(motherTongue, lang);
      matches = Tools.checkBitext(sourceText, text, sourceLt, targetLt, bRules);
    }
    httpExchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE_VALUE);
    final String response = StringTools.ruleMatchesToXML(matches, text,
            CONTEXT_SIZE, StringTools.XmlPrintMode.NORMAL_XML);
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
    httpExchange.getResponseBody().write(response.getBytes());
    httpExchange.close();
  }

  private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
    final Map<String, String> parameters = new HashMap<String, String>();
    if (query != null) {
      final String[] pairs = query.split("[&]");
      for (String pair : pairs) {
        final String param = pair.substring(0, pair.indexOf("="));
        String key = null;
        String value;
        if (param != null) {
          key = URLDecoder.decode(param, System.getProperty("file.encoding"));
        }
        if (pair.substring(pair.indexOf("=") + 1) == null || pair.substring(pair.indexOf("=") + 1).equals("")) {
          value = "";
        } else {
          value = URLDecoder.decode(pair.substring(pair.indexOf("=") + 1), "UTF-8");
        }
        value = value.replaceAll("\\+", " ");
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
    return newLanguageTool;
  }

  /**
   * Construct an xml string containing all supported languages. <br/>The xml format is:<br/>
   * &lt;languages&gt;<br/>
   *    &nbsp;&nbsp;&lt;language name="Catalan" abbr="ca" /&gt;<br/> 
   *    &nbsp;&nbsp;&lt;language name="Dutch" abbr="nl" /&gt;<br/>
   *    &nbsp;&nbsp;...<br/>
   *  &lt;languages&gt;<br/>
   *  The languages are alphabetically sorted.  
   * @return an xml string containing all supported languages.
   */
  public static String getSupportedLanguagesAsXML() {
    final List<Language> languages = Arrays.asList(Language.REAL_LANGUAGES);
    Collections.sort(languages, new Comparator<Language>() {
      @Override
      public int compare(Language o1, Language o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    final StringBuilder xmlBuffer = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n<languages>\n");
    for (Language lang : languages) {
      xmlBuffer.append(String.format("\t<language name=\"%s\" abbr=\"%s\" /> \n", lang.getName(), lang.getShortName()));
    }
    xmlBuffer.append("</languages>\n");
    return xmlBuffer.toString();
  }
}
