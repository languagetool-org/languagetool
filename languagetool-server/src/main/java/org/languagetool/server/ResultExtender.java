/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Experimental;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;

import javax.xml.stream.XMLStreamException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Extend results by adding rules matches from a different API server.
 * @since 4.0
 */
@Experimental
class ResultExtender {

  private final URL url;
  private final int connectTimeoutMillis;
  private final ObjectMapper mapper = new ObjectMapper();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  ResultExtender(String url, int connectTimeoutMillis) {
    this.url = Tools.getUrl(url);
    if (connectTimeoutMillis <= 0) {
      throw new IllegalArgumentException("connectTimeoutMillis must be > 0: " + connectTimeoutMillis);
    }
    this.connectTimeoutMillis = connectTimeoutMillis;
  }

  /**
   * Filter {@code extensionMatches} so that only those matches are left that don't cover or touch one of the {@code matches}. 
   */
  @NotNull
  List<RuleMatch> getFilteredExtensionMatches(List<RuleMatch> matches, List<RemoteRuleMatch> extensionMatches) {
    RuleMatch hiddenRuleMatch = new RuleMatch(new HiddenRule(), new AnalyzedSentence(new AnalyzedTokenReadings[]{}), 0, 1, "(hidden message)");
    List<RuleMatch> filteredExtMatches = new ArrayList<>();
    for (RemoteRuleMatch extensionMatch : extensionMatches) {
      if (!extensionMatch.isTouchedByOneOf(matches)) {
        filteredExtMatches.add(hiddenRuleMatch);
      }
    }
    return filteredExtMatches;
  }

  @NotNull
  Future<List<RemoteRuleMatch>> getExtensionMatches(String plainText, Language lang) throws IOException, XMLStreamException {
    return executor.submit(() -> {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        HttpURLConnection.setFollowRedirects(false);
        huc.setConnectTimeout(connectTimeoutMillis);
        huc.setRequestMethod("POST");
        huc.setDoOutput(true);
        huc.connect();
        try (DataOutputStream wr = new DataOutputStream(huc.getOutputStream())) {
          String urlParameters = "language=" + lang.getShortCodeWithCountryAndVariant() + "&text=" + plainText;
          byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
          wr.write(postData);
        }
        InputStream input = huc.getInputStream();
        List<RemoteRuleMatch> remoteRuleMatches = parseJson(input);
        huc.disconnect();
        return remoteRuleMatches;
      });
  }

  @NotNull
  private List<RemoteRuleMatch> parseJson(InputStream inputStream) throws XMLStreamException, IOException {
    Map map = mapper.readValue(inputStream, Map.class);
    List matches = (ArrayList) map.get("matches");
    List<RemoteRuleMatch> result = new ArrayList<>();
    for (Object match : matches) {
      RemoteRuleMatch remoteMatch = getMatch((Map<String, Object>)match);
      result.add(remoteMatch);
    }
    return result;
  }

  @NotNull
  private RemoteRuleMatch getMatch(Map<String, Object> match) {
    Map<String, Object> rule = (Map<String, Object>) match.get("rule");
    int offset = (int) getRequired(match, "offset");
    int errorLength = (int) getRequired(match, "length");

    Map<String, Object> context = (Map<String, Object>) match.get("context");
    int contextOffset = (int) getRequired(context, "offset");
    RemoteRuleMatch remoteMatch = new RemoteRuleMatch(getRequiredString(rule, "id"), getRequiredString(match, "message"),
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
  
  class HiddenRule extends Rule {
    @Override
    public String getId() {
      return "HIDDEN_RULE";
    }
    @Override
    public String getDescription() {
      return "(description hidden)";
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new RuntimeException("not implemented");
    }
  }
}
