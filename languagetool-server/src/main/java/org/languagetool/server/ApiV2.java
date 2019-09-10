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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.*;

import static org.languagetool.server.LanguageToolHttpHandler.API_DOC_URL;

/**
 * Handle requests to {@code /v2/} of the HTTP API. 
 * @since 3.4
 */
class ApiV2 {

  private static final String JSON_CONTENT_TYPE = "application/json";
  private static final String ENCODING = "UTF-8";

  private final TextChecker textChecker;
  private final String allowOriginUrl;
  private final JsonFactory factory = new JsonFactory();

  ApiV2(TextChecker textChecker, String allowOriginUrl) {
    this.textChecker = textChecker;
    this.allowOriginUrl = allowOriginUrl;
  }

  void handleRequest(String path, HttpExchange httpExchange, Map<String, String> parameters, ErrorRequestLimiter errorRequestLimiter, String remoteAddress, HTTPServerConfig config) throws Exception {
    if (path.equals("languages")) {
      handleLanguagesRequest(httpExchange);
    } else if (path.equals("check")) {
      handleCheckRequest(httpExchange, parameters, errorRequestLimiter, remoteAddress);
    } else if (path.equals("words")) {
      handleWordsRequest(httpExchange, parameters, config);
    } else if (path.equals("words/add")) {
      handleWordAddRequest(httpExchange, parameters, config);
    } else if (path.equals("words/delete")) {
      handleWordDeleteRequest(httpExchange, parameters, config);
    } else if (path.equals("rule/examples")) {
      // private (i.e. undocumented) API for our own use only
      handleRuleExamplesRequest(httpExchange, parameters, config);
    } else if (path.equals("log")) {
      // private (i.e. undocumented) API for our own use only
      handleLogRequest(httpExchange, parameters);
    } else {
      throw new PathNotFoundException("Unsupported action: '" + path + "'. Please see " + API_DOC_URL);
    }
  }

  private void handleLanguagesRequest(HttpExchange httpExchange) throws IOException {
    String response = getLanguages();
    ServerTools.setCommonHeaders(httpExchange, JSON_CONTENT_TYPE, allowOriginUrl);
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
    ServerMetricsCollector.getInstance().logResponse(HttpURLConnection.HTTP_OK);
  }

  private void handleCheckRequest(HttpExchange httpExchange, Map<String, String> parameters, ErrorRequestLimiter errorRequestLimiter, String remoteAddress) throws Exception {
    AnnotatedText aText;
    int paramCount = (parameters.containsKey("text") ? 1 : 0) + (parameters.containsKey("data") ? 1 : 0);
    if (paramCount > 1) {
      throw new IllegalArgumentException("Set only 'text' or 'data' parameters, not both");
    }
    if (parameters.containsKey("text")) {
      aText = new AnnotatedTextBuilder().addText(parameters.get("text")).build();
    } else if (parameters.containsKey("data")) {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode data = mapper.readTree(parameters.get("data"));
      if (data.get("text") != null && data.get("annotation") != null) {
        throw new IllegalArgumentException("'data' key in JSON requires either 'text' or 'annotation' key, not both");
      } else if (data.get("text") != null) {
        aText = getAnnotatedTextFromString(data, data.get("text").asText());
      } else if (data.get("annotation") != null) {
        aText = getAnnotatedTextFromJson(data);
      } else {
        throw new IllegalArgumentException("'data' key in JSON requires 'text' or 'annotation' key");
      }
    } else {
      throw new IllegalArgumentException("Missing 'text' or 'data' parameter");
    }
    textChecker.checkText(aText, httpExchange, parameters, errorRequestLimiter, remoteAddress);
  }

  private void handleWordsRequest(HttpExchange httpExchange, Map<String, String> params, HTTPServerConfig config) throws Exception {
    ensureGetMethod(httpExchange, "/words");
    UserLimits limits = getUserLimits(params, config);
    DatabaseAccess db = DatabaseAccess.getInstance();
    int offset = params.get("offset") != null ? Integer.parseInt(params.get("offset")) : 0;
    int limit = params.get("limit") != null ? Integer.parseInt(params.get("limit")) : 10;
    List<UserDictEntry> words = db.getWords(limits.getPremiumUid(), offset, limit);
    writeListResponse("words", words, httpExchange);
  }
  
  private void handleWordAddRequest(HttpExchange httpExchange, Map<String, String> parameters, HTTPServerConfig config) throws Exception {
    ensurePostMethod(httpExchange, "/words/add");
    UserLimits limits = getUserLimits(parameters, config);
    DatabaseAccess db = DatabaseAccess.getInstance();
    boolean added = db.addWord(parameters.get("word"), limits.getPremiumUid());
    writeResponse("added", added, httpExchange);
  }

  private void handleWordDeleteRequest(HttpExchange httpExchange, Map<String, String> parameters, HTTPServerConfig config) throws Exception {
    ensurePostMethod(httpExchange, "/words/delete");
    UserLimits limits = getUserLimits(parameters, config);
    DatabaseAccess db = DatabaseAccess.getInstance();
    boolean deleted = db.deleteWord(parameters.get("word"), limits.getPremiumUid());
    writeResponse("deleted", deleted, httpExchange);
  }

  private void handleRuleExamplesRequest(HttpExchange httpExchange, Map<String, String> params, HTTPServerConfig config) throws Exception {
    ensureGetMethod(httpExchange, "/rule/examples");
    if (params.get("lang") == null) {
      throw new IllegalArgumentException("'lang' parameter missing");
    }
    if (params.get("ruleId") == null) {
      throw new IllegalArgumentException("'ruleId' parameter missing");
    }
    Language lang = Languages.getLanguageForShortCode(params.get("lang"));
    JLanguageTool lt = new JLanguageTool(lang);
    if (textChecker.config.languageModelDir != null) {
      lt.activateLanguageModelRules(textChecker.config.languageModelDir);
    }
    List<Rule> rules = lt.getAllRules();
    List<Rule> foundRules = new ArrayList<>();
    for (Rule rule : rules) {
      if (rule.getId().equals(params.get("ruleId"))) {
        foundRules.add(rule);
      }
    }
    if (foundRules.isEmpty()) {
      throw new PathNotFoundException("Rule '" + params.get("ruleId") + "' not found for language " + lang +
              " (LanguageTool version/date: " + JLanguageTool.VERSION + "/" + JLanguageTool.BUILD_DATE + ", total rules of language: " + rules.size() + ")");
    }
    StringWriter sw = new StringWriter();
    try (JsonGenerator g = factory.createGenerator(sw)) {
      g.writeStartObject();
      g.writeArrayFieldStart("results");
      g.writeStartObject();
      g.writeStringField("warning", "*** This is not a public API - it may change anytime ***");
      g.writeEndObject();
      for (Rule foundRule : foundRules) {
        for (CorrectExample example : foundRule.getCorrectExamples()) {
          g.writeStartObject();
          g.writeStringField("status", "correct");
          g.writeStringField("sentence", example.getExample());
          g.writeEndObject();
        }
        for (IncorrectExample example : foundRule.getIncorrectExamples()) {
          g.writeStartObject();
          g.writeStringField("status", "incorrect");
          g.writeStringField("sentence", example.getExample());
          g.writeArrayFieldStart("corrections");
          for (String s : example.getCorrections()) {
            g.writeString(s);
          }
          g.writeEndArray();
          g.writeEndObject();
        }
      }
      g.writeEndArray();
      g.writeEndObject();
    }
    sendJson(httpExchange, sw);
  }

  private void ensureGetMethod(HttpExchange httpExchange, String url) {
    if (!httpExchange.getRequestMethod().equalsIgnoreCase("get")) {
      throw new IllegalArgumentException(url + " needs to be called with GET");
    }
  }
  
  private void ensurePostMethod(HttpExchange httpExchange, String url) {
    if (!httpExchange.getRequestMethod().equalsIgnoreCase("post")) {
      throw new IllegalArgumentException(url + " needs to be called with POST");
    }
  }

  @NotNull
  private UserLimits getUserLimits(Map<String, String> parameters, HTTPServerConfig config) {
    UserLimits limits = ServerTools.getUserLimits(parameters, config);
    if (limits.getPremiumUid() == null) {
      throw new IllegalStateException("This end point needs a user id");
    }
    return limits;
  }

  private void writeResponse(String fieldName, boolean added, HttpExchange httpExchange) throws IOException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator g = factory.createGenerator(sw)) {
      g.writeStartObject();
      g.writeBooleanField(fieldName, added);
      g.writeEndObject();
    }
    sendJson(httpExchange, sw);
  }
  
  private void writeListResponse(String fieldName, List<UserDictEntry> words, HttpExchange httpExchange) throws IOException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator g = factory.createGenerator(sw)) {
      g.writeStartObject();
      g.writeArrayFieldStart(fieldName);
      for (UserDictEntry word : words) {
        g.writeString(word.getWord());
      }
      g.writeEndArray();
      g.writeEndObject();
    }
    sendJson(httpExchange, sw);
  }

  private void sendJson(HttpExchange httpExchange, StringWriter sw) throws IOException {
    String response = sw.toString();
    ServerTools.setCommonHeaders(httpExchange, JSON_CONTENT_TYPE, allowOriginUrl);
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
    ServerMetricsCollector.getInstance().logResponse(HttpURLConnection.HTTP_OK);
  }

  private void handleLogRequest(HttpExchange httpExchange, Map<String, String> parameters) throws IOException {
    // used so the client (especially the browser add-ons) can report internal issues:
    String message = parameters.get("message");
    if (message != null && message.length() > 250) {
      message = message.substring(0, 250) + "...";
    }
    ServerTools.print("Log message from client: " + message + " - User-Agent: " + httpExchange.getRequestHeaders().getFirst("User-Agent"));
    String response = "OK";
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
    ServerMetricsCollector.getInstance().logResponse(HttpURLConnection.HTTP_OK);
  }

  private AnnotatedText getAnnotatedTextFromString(JsonNode data, String text) {
    AnnotatedTextBuilder textBuilder = new AnnotatedTextBuilder().addText(text);
    if (data.has("metaData")) {
      JsonNode metaData = data.get("metaData");
      Iterator<String> it = metaData.fieldNames();
      while (it.hasNext()) {
        String key = it.next();
        String val = metaData.get(key).asText();
        try {
          AnnotatedText.MetaDataKey metaDataKey = AnnotatedText.MetaDataKey.valueOf(key);
          textBuilder.addGlobalMetaData(metaDataKey, val);
        } catch (IllegalArgumentException e) {
          textBuilder.addGlobalMetaData(key, val);
        }
      }
    }
    return textBuilder.build();
  }

  private AnnotatedText getAnnotatedTextFromJson(JsonNode data) {
    AnnotatedTextBuilder atb = new AnnotatedTextBuilder();
    // Expected format:
    // annotation: [
    //   {text: 'text'},
    //   {markup: '<b>'}
    //   {text: 'more text'},
    //   {markup: '</b>'}
    // ]
    //
    for (JsonNode node : data.get("annotation")) {
      if (node.get("text") != null && node.get("markup") != null) {
        throw new IllegalArgumentException("Only either 'text' or 'markup' are supported in an object in 'annotation' list, not both: " + node);
      } else if (node.get("text") != null && node.get("interpretAs") != null) {
        throw new IllegalArgumentException("'text' cannot be used with 'interpretAs' (only 'markup' can): " + node);
      } else if (node.get("text") != null) {
        atb.addText(node.get("text").asText());
      } else if (node.get("markup") != null) {
        if (node.get("interpretAs") != null) {
          atb.addMarkup(node.get("markup").asText(), node.get("interpretAs").asText());
        } else {
          atb.addMarkup(node.get("markup").asText());
        }
      } else {
        throw new IllegalArgumentException("Only 'text' and 'markup' are supported in 'annotation' list: " + node);
      }
    }
    return atb.build();
  }

  String getLanguages() throws IOException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator g = factory.createGenerator(sw)) {
      g.writeStartArray();
      List<Language> languages = new ArrayList<>(Languages.get());
      languages.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
      for (Language lang : languages) {
        g.writeStartObject();
        g.writeStringField("name", lang.getName());
        g.writeStringField("code", lang.getShortCode());
        g.writeStringField("longCode", lang.getShortCodeWithCountryAndVariant());
        g.writeEndObject();
      }
      g.writeEndArray();
    }
    return sw.toString();
  }

}
