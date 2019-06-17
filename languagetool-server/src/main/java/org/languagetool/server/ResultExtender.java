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
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;

import javax.net.ssl.SSLHandshakeException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.languagetool.server.ServerTools.print;

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
    List<RuleMatch> filteredExtMatches = new ArrayList<>();
    for (RemoteRuleMatch extensionMatch : extensionMatches) {
      if (!extensionMatch.isTouchedByOneOf(matches)) {
        AnalyzedSentence sentence = new AnalyzedSentence(new AnalyzedTokenReadings[]{});
        HiddenRule hiddenRule = new HiddenRule(extensionMatch.getLocQualityIssueType().orElse(null), extensionMatch.estimatedContextForSureMatch());
        RuleMatch hiddenRuleMatch = new RuleMatch(hiddenRule, sentence, extensionMatch.getErrorOffset(),
                extensionMatch.getErrorOffset()+extensionMatch.getErrorLength(), "(hidden message)");
        filteredExtMatches.add(hiddenRuleMatch);
      }
    }
    return filteredExtMatches;
  }

  @NotNull
  Future<List<RemoteRuleMatch>> getExtensionMatchesFuture(String plainText, Map<String, String> params) {
    return executor.submit(() -> getExtensionMatches(plainText, params));
  }  
  
  @NotNull
  List<RemoteRuleMatch> getExtensionMatches(String plainText, Map<String, String> params) throws IOException {
    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
    HttpURLConnection.setFollowRedirects(false);
    huc.setConnectTimeout(connectTimeoutMillis);
    huc.setReadTimeout(connectTimeoutMillis*2);
    huc.setRequestMethod("POST");
    huc.setDoOutput(true);
    try {
      huc.connect();
      try (DataOutputStream wr = new DataOutputStream(huc.getOutputStream())) {
        String urlParameters = "";
        List<String> ignoredParameters = Arrays.asList("enableHiddenRules", "username", "password", "token", "apiKey");
        for (Map.Entry<String, String> entry : params.entrySet()) {
          // We could set 'language' to the language already detected, so the queried server
          // wouldn't need to guess the language again. But then we'd run into cases where
          // we get an error because e.g. 'noopLanguages' can only be used with 'language=auto'
          if (!ignoredParameters.contains(entry.getKey())) {
            urlParameters += "&" + encode(entry.getKey()) + "=" + encode(entry.getValue());
          }
        }
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        wr.write(postData);
      }
      InputStream input = huc.getInputStream();
      return parseJson(input);
    } catch (SSLHandshakeException | SocketTimeoutException e) {
      // "hard" errors that will probably not resolve themselves easily:
      throw e;
    } catch (Exception e) {
      // These are issue that can be request-specific, like wrong parameters. We don't throw an
      // exception, as the calling code would otherwise assume this is a persistent error:
      print("Warn: Failed to query hidden matches server at " + url + ": " + e.getClass() + ": " + e.getMessage());
      return Collections.emptyList();
    } finally {
      huc.disconnect();
    }
  }

  private String encode(String plainText) throws UnsupportedEncodingException {
    return URLEncoder.encode(plainText, StandardCharsets.UTF_8.name());
  }

  @NotNull
  private List<RemoteRuleMatch> parseJson(InputStream inputStream) throws IOException {
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
    int contextForSureMatch = match.get("contextForSureMatch") != null ? (int) match.get("contextForSureMatch") : 0;
    RemoteRuleMatch remoteMatch = new RemoteRuleMatch(getRequiredString(rule, "id"), getRequiredString(match, "message"),
            getRequiredString(context, "text"), contextOffset, offset, errorLength, contextForSureMatch);
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
    final ITSIssueType itsType;
    final int estimatedContextForSureMatch;
    HiddenRule(String type, int estimatedContextForSureMatch) {
      itsType = type != null ? ITSIssueType.getIssueType(type) : ITSIssueType.Uncategorized;
      this.estimatedContextForSureMatch = estimatedContextForSureMatch;
    }
    @Override
    public String getId() {
      return "HIDDEN_RULE";
    }
    @Override
    public ITSIssueType getLocQualityIssueType() {
      return itsType;
    }
    @Override
    public String getDescription() {
      return "(description hidden)";
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) {
      throw new RuntimeException("not implemented");
    }
    @Override
    public int estimateContextForSureMatch() {
      return estimatedContextForSureMatch;
    }
  }
}
