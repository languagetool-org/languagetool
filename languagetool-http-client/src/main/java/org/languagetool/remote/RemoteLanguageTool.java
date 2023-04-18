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
package org.languagetool.remote;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Check a text using a <a href="https://dev.languagetool.org/http-server">remote LanguageTool server</a> via HTTP or HTTPS.
 * Our public HTTPS API and its restrictions are documented
 * <a href="https://dev.languagetool.org/public-http-api">here</a>.
 *
 * @since 3.4
 */
@SuppressWarnings("unchecked")
public class RemoteLanguageTool {

  private static final String V2_CHECK = "/v2/check";
  private static final String V2_MAXTEXTLENGTH = "/v2/maxtextlength";
  private static final String V2_CONFIGINFO = "/v2/configinfo";

  private final ObjectMapper mapper = new ObjectMapper();
  private final URL serverBaseUrl;

  /**
   * @param serverBaseUrl for example {@code https://languagetool.org/api} (not ending in slash)
   */
  public RemoteLanguageTool(URL serverBaseUrl) {
    if (serverBaseUrl.toString().endsWith("/")) {
      throw new IllegalArgumentException("Server base URL must not end with '/': " + serverBaseUrl);
    }
    this.serverBaseUrl = Objects.requireNonNull(serverBaseUrl);
  }

  /**
   * @param text     the text to be checked
   * @param langCode the language code like {@code en} or {@code en-US} - <strong>note that for some languages (like English) you
   *                 need to specify the country code (like {@code US} or {@code GB}) to get spell checking</strong>
   */
  public RemoteResult check(String text, String langCode) {
    return check(getUrlParams(text, new CheckConfigurationBuilder(langCode).build(), null));
  }
  
  public RemoteResult check(String text, String langCode, Map<String, String> customParams) {
    return check(getUrlParams(text, new CheckConfigurationBuilder(langCode).build(), customParams));
  }

  /**
   * @param text the text to be checked
   */
  public RemoteResult check(String text, CheckConfiguration config) {
    return check(getUrlParams(text, config, null));
  }
  
  public RemoteResult check(String text, CheckConfiguration config, Map<String, String> customParams) {
    return check(getUrlParams(text, config, customParams));
  }

  private String getUrlParams(String text, CheckConfiguration config, Map<String, String> customParams) {
    StringBuilder params = new StringBuilder();
    append(params, "text", text);
    if (config.getMotherTongueLangCode() != null) {
      append(params, "motherTongue", config.getMotherTongueLangCode());
    }
    if (config.guessLanguage()) {
      append(params, "language", "auto");
    } else {
      append(params, "language", config.getLangCode().orElse("auto"));
    }
    if (config.getEnabledRuleIds().size() > 0) {
      append(params, "enabledRules", String.join(",", config.getEnabledRuleIds()));
    }
    if (config.enabledOnly()) {
      append(params, "enabledOnly", "yes");
    }
    if (config.getDisabledRuleIds().size() > 0) {
      append(params, "disabledRules", String.join(",", config.getDisabledRuleIds()));
    }
    if (config.getMode() != null) {
      append(params, "mode", config.getMode());
    }
    if (config.getLevel() != null) {
      append(params, "level", config.getLevel());
    }
    if (config.getRuleValues().size() > 0) {
      append(params, "ruleValues", String.join(",", config.getRuleValues()));
    }
    if (config.getTextSessionID() != null) {
      append(params, "textSessionId", config.getTextSessionID());
    }
    if (config.getUsername() != null) {
      append(params, "username", config.getUsername());
    }
    if (config.getAPIKey() != null) {
      append(params, "apiKey", config.getAPIKey());
    }
    if (customParams != null) {
      customParams.forEach((key, value) -> {
        append(params, key, value);
      });
    }
    append(params, "useragent", "java-http-client");
    return params.toString();
  }

  private void append(StringBuilder params, String paramName, String paramValue) {
    if (params.length() > 0) {
      params.append('&');
    }
    params.append(paramName).append('=').append(encode(paramValue));
  }

  private String encode(String text) {
    try {
      return URLEncoder.encode(text, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private RemoteResult check(String urlParameters) {
    byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
    URL checkUrl;
    try {
      checkUrl = new URL(serverBaseUrl + V2_CHECK);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    HttpURLConnection conn = getConnection(postData, checkUrl);
    try {
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (InputStream inputStream = conn.getInputStream()) {
          return parseJson(inputStream);
        }
      } else {
        try (InputStream inputStream = conn.getErrorStream()) {
          String error = readStream(inputStream, "utf-8");
          throw new RuntimeException("Got error: " + error + " - HTTP response code " + conn.getResponseCode());
        }
      }
    } catch (ConnectException e) {
      throw new RuntimeException("Could not connect to server at " + serverBaseUrl, e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      conn.disconnect();
    }
  }

  public RemoteConfigurationInfo getConfigurationInfo(String urlParameters) {
    if (!urlParameters.startsWith("language=")) {
      throw new IllegalArgumentException("'language' parameter missing");
    }
    byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
    URL checkUrl;
    try {
      checkUrl = new URL(serverBaseUrl + V2_CONFIGINFO);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    HttpURLConnection conn = getConnection(postData, checkUrl);
    try {
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (InputStream inputStream = conn.getInputStream()) {
          RemoteConfigurationInfo configInfo = new RemoteConfigurationInfo(mapper, inputStream);
          return configInfo;
        }
      } else {
        try (InputStream inputStream = conn.getErrorStream()) {
          String error = readStream(inputStream, "utf-8");
          throw new RuntimeException("Got error: " + error + " - HTTP response code " + conn.getResponseCode());
        }
      }
    } catch (ConnectException e) {
      throw new RuntimeException("Could not connect to server at " + serverBaseUrl, e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      conn.disconnect();
    }
  }

  public int getMaxTextLength() {
    byte[] postData = {0};
    URL checkUrl;
    try {
      checkUrl = new URL(serverBaseUrl + V2_MAXTEXTLENGTH);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    HttpURLConnection conn = getConnection(postData, checkUrl);
    try {
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (InputStream inputStream = conn.getInputStream()) {
          StringBuilder sb = new StringBuilder();
          try (InputStreamReader isr = new InputStreamReader(inputStream, "utf-8");
               BufferedReader br = new BufferedReader(isr)) {
            String line = br.readLine();
            return Integer.parseInt(line);
          }
        }
      } else {
        try (InputStream inputStream = conn.getErrorStream()) {
          String error = readStream(inputStream, "utf-8");
          throw new RuntimeException("Got error: " + error + " - HTTP response code " + conn.getResponseCode());
        }
      }
    } catch (ConnectException e) {
      throw new RuntimeException("Could not connect to server at " + serverBaseUrl, e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      conn.disconnect();
    }
  }

  HttpURLConnection getConnection(byte[] postData, URL url) {
    try {
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("charset", "utf-8");
      conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
      try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
        wr.write(postData);
      }
      return conn;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String readStream(InputStream stream, String encoding) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (InputStreamReader isr = new InputStreamReader(stream, encoding);
         BufferedReader br = new BufferedReader(isr)) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString();
  }

  private RemoteResult parseJson(InputStream inputStream) throws XMLStreamException, IOException {
    Map map = mapper.readValue(inputStream, Map.class);
    Map<String, String> languageObj = (Map<String, String>) map.get("language");
    String language = languageObj.get("name");
    String languageCode = languageObj.get("code");
    Map<String, String> detectedLanguageObj = (Map<String, String>) ((Map) languageObj).get("detectedLanguage");
    String languageDetectedCode = null, languageDetectedName = null;
    if (detectedLanguageObj != null) {
      languageDetectedCode = detectedLanguageObj.get("code");
      languageDetectedName = detectedLanguageObj.get("name");
    }
    Map<String, String> software = (Map<String, String>) map.get("software");
    RemoteServer remoteServer = new RemoteServer(software.get("name"), software.get("version"), software.get("buildDate"));
    List matches = (ArrayList) map.get("matches");
    List<RemoteRuleMatch> result = new ArrayList<>();
    for (Object match : matches) {
      RemoteRuleMatch remoteMatch = getMatch((Map<String, Object>) match);
      result.add(remoteMatch);
    }
    List ignoreRanges = (ArrayList) map.get("ignoreRanges");
    List<RemoteIgnoreRange> remoteIgnoreRanges = new ArrayList<>();
    if (ignoreRanges != null) {
      for (Object range : ignoreRanges) {
        RemoteIgnoreRange remoteIgnoreRange = getIgnoreRange((Map<String, Object>) range);
        remoteIgnoreRanges.add(remoteIgnoreRange);
      }
    }
    return new RemoteResult(language, languageCode, languageDetectedCode, languageDetectedName, result, remoteIgnoreRanges, remoteServer);
  }

  private RemoteIgnoreRange getIgnoreRange(Map<String, Object> range) {
    int from = (int) range.get("from");
    int to = (int) range.get("to");
    String langCode = (String) ((Map<String, Object>) range.get("language")).get("code");
    return new RemoteIgnoreRange(from, to, langCode);
  }

  private RemoteRuleMatch getMatch(Map<String, Object> match) {
    Map<String, Object> rule = (Map<String, Object>) match.get("rule");
    int offset = (int) getRequired(match, "offset");
    int errorLength = (int) getRequired(match, "length");

    Map<String, Object> context = (Map<String, Object>) match.get("context");
    int contextOffset = (int) getRequired(context, "offset");
    RemoteRuleMatch remoteMatch = new RemoteRuleMatch(getRequiredString(rule, "id"), getRequiredString(rule, "description"), getRequiredString(match, "message"),
            getRequiredString(context, "text"), contextOffset, offset, errorLength);
    remoteMatch.setShortMsg(getOrNull(match, "shortMessage"));
    remoteMatch.setRuleSubId(getOrNull(rule, "subId"));
    remoteMatch.setLocQualityIssueType(getOrNull(rule, "issueType"));
    List<String> urls = getValueList(rule, "urls");
    if (urls.size() > 0) {
      remoteMatch.setUrl(urls.get(0));
    }
    Map<String, Object> category = (Map<String, Object>) rule.get("category");
    remoteMatch.setCategory(getOrNull(category, "name"));
    remoteMatch.setCategoryId(getOrNull(category, "id"));

    remoteMatch.setReplacements(getValueList(match, "replacements"));
    return remoteMatch;
  }

  private Object getRequired(Map<String, Object> elem, String propertyName) {
    Object val = elem.get(propertyName);
    if (val != null) {
      return val;
    }
    throw new RuntimeException("JSON item " + elem + " doesn't contain required property '" + propertyName + "'");
  }

  private String getRequiredString(Map<String, Object> elem, String propertyName) {
    return (String) getRequired(elem, propertyName);
  }

  private String getOrNull(Map<String, Object> elem, String propertyName) {
    Object val = elem.get(propertyName);
    if (val != null) {
      return (String) val;
    }
    return null;
  }

  private List<String> getValueList(Map<String, Object> match, String propertyName) {
    List<Object> matches = (List<Object>) match.get(propertyName);
    List<String> l = new ArrayList<>();
    if (matches != null) {
      for (Object o : matches) {
        Map<String, Object> item = (Map<String, Object>) o;
        l.add((String) item.get("value"));
      }
    }
    return l;
  }

}
