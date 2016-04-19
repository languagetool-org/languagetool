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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Check a text using a <a href="http://wiki.languagetool.org/http-server">remote LanguageTool server</a> via HTTP or HTTPS.
 * Our public HTTPS API and its restrictions are documented
 * <a href="http://wiki.languagetool.org/public-http-api">in our wiki</a>.
 * @since 3.4
 */
public class RemoteLanguageTool {

  private final URL serverUrl;

  public RemoteLanguageTool(URL serverUrl) {
    this.serverUrl = Objects.requireNonNull(serverUrl);
  }

  /**
   * @param text the text to be checked
   * @param langCode the language code like {@code en} or {@code en-US} - <strong>note that for some languages (like English) you
   *                 need to specify the country code (like {@code US} or {@code GB}) to get spell checking</strong>
   */
  public RemoteResult check(String text, String langCode) {
    return check(getUrlParams(text, new CheckConfigurationBuilder(langCode).build()));
  }

  /**
   * @param text the text to be checked
   */
  public RemoteResult check(String text, CheckConfiguration config) {
    return check(getUrlParams(text, config));
  }

  private String getUrlParams(String text, CheckConfiguration config) {
    StringBuilder params = new StringBuilder();
    append(params, "language", config.getLangCode());
    append(params, "text", text);
    if (config.getMotherTongueLangCode() != null) {
      append(params, "motherTongue", config.getMotherTongueLangCode());
    }
    if (config.guessLanguage()) {
      append(params, "autodetect", "1");
    }
    if (config.getEnabledRuleIds().size() > 0) {
      append(params, "enabled", String.join(",", config.getEnabledRuleIds()));
    }
    if (config.enabledOnly()) {
      append(params, "enabledOnly", "yes");
    }
    if (config.getDisabledRuleIds().size() > 0) {
      append(params, "disabled", String.join(",", config.getDisabledRuleIds()));
    }
    append(params, "useragent", "java-http-client");
    return params.toString();
  }

  private void append(StringBuilder params, String paramName, String paramValue) {
    if (params.length() > 0) {
      params.append("&");
    }
    params.append(paramName).append("=").append(encode(paramValue));
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
    HttpURLConnection conn = getConnection(postData);
    try {
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (InputStream inputStream = conn.getInputStream()) {
          return parseXml(inputStream);
        }
      } else {
        try (InputStream inputStream = conn.getErrorStream()) {
          String error = readStream(inputStream, "utf-8");
          throw new RuntimeException("Got error: " + error + " - HTTP response code " + conn.getResponseCode());
        }
      }
    } catch (ConnectException e) {
      throw new RuntimeException("Could not connect to server at " + serverUrl, e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      conn.disconnect();
    }
  }

  HttpURLConnection getConnection(byte[] postData) {
    try {
      HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
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

  private RemoteResult parseXml(InputStream inputStream) throws XMLStreamException {
    List<RemoteRuleMatch> result = new ArrayList<>();
    String language = null;
    String languageCode = null;
    RemoteServer remoteServer = null;
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLEventReader reader = factory.createXMLEventReader(inputStream);
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
        String elementName = event.asStartElement().getName().getLocalPart();
        switch (elementName) {
          case "matches":
            StartElement matches = event.asStartElement();
            remoteServer = new RemoteServer(get(matches, "software"), get(matches, "version"), get(matches, "buildDate"));
            break;
          case "language":
            StartElement langElem = event.asStartElement();
            language = get(langElem, "name");
            languageCode = get(langElem, "shortname");
            break;
          case "error":
            StartElement error = event.asStartElement();
            result.add(getMatch(error));
            break;
        }
      }
    }
    return new RemoteResult(language, languageCode, result, remoteServer);
  }

  private RemoteRuleMatch getMatch(StartElement error) {
    String ruleId = get(error, "ruleId");
    int offset = Integer.parseInt(get(error, "offset"));
    int errorLength = Integer.parseInt(get(error, "errorlength"));
    int contextOffset = Integer.parseInt(get(error, "contextoffset"));
    RemoteRuleMatch match = new RemoteRuleMatch(ruleId, get(error, "msg"), get(error, "context"), contextOffset, offset, errorLength);
    match.setShortMsg(getOrNull(error, "shortmsg"));
    match.setRuleSubId(getOrNull(error, "subId"));
    match.setShortMsg(getOrNull(error, "shortmsg"));
    match.setUrl(getOrNull(error, "url"));
    match.setCategory(getOrNull(error, "category"));
    match.setCategoryId(getOrNull(error, "categoryid"));
    match.setLocQualityIssueType(getOrNull(error, "locqualityissuetype"));
    String replacements = getOrNull(error, "replacements");
    if (replacements != null) {
      match.setReplacements(Arrays.asList(replacements.split("#")));
    }
    return match;
  }

  private String get(StartElement elem, String attributeName) {
    Attribute val = elem.getAttributeByName(new QName(attributeName));
    if (val != null) {
      return val.getValue();
    }
    throw new RuntimeException("XML element " + elem + " doesn't contain required attribute '" + attributeName  + "'");
  }
  
  private String getOrNull(StartElement elem, String attributeName) {
    Attribute val = elem.getAttributeByName(new QName(attributeName));
    if (val != null) {
      return val.getValue();
    }
    return null;
  }
  
}
