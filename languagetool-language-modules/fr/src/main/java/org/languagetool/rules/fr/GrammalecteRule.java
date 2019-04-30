/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Experimental;
import org.languagetool.GlobalConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Queries a local Grammalecte server.
 * @since 4.6
 */
@Experimental
public class GrammalecteRule extends Rule {

  private final static int TIMEOUT_MILLIS = 500;
  private final ObjectMapper mapper = new ObjectMapper();
  private final GlobalConfig globalConfig;

  // https://github.com/languagetooler-gmbh/languagetool-premium/issues/197:
  private final Set<String> ignoreRules = new HashSet<>(Arrays.asList(
    "tab_fin_ligne",
    "apostrophe_typographique",
    "typo_guillemets_typographiques_doubles_ouvrants",
    "typo_guillemets_typographiques_doubles_fermants"
  ));

  public GrammalecteRule(ResourceBundle messages, GlobalConfig globalConfig) {
    super(messages);
    //addExamplePair(Example.wrong(""),
    //               Example.fixed(""));
    this.globalConfig = globalConfig;
  }

  @Override
  public String getId() {
    return "FR_GRAMMALECTE";
  }

  @Override
  public String getDescription() {
    return "Returns matches of a local Grammalecte server";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    URL serverUrl = new URL(globalConfig.getGrammalecteServer());
    HttpURLConnection huc = (HttpURLConnection) serverUrl.openConnection();
    HttpURLConnection.setFollowRedirects(false);
    huc.setConnectTimeout(TIMEOUT_MILLIS);
    huc.setReadTimeout(TIMEOUT_MILLIS*2);
    huc.setRequestMethod("POST");
    huc.setDoOutput(true);
    try {
      huc.connect();
      try (DataOutputStream wr = new DataOutputStream(huc.getOutputStream())) {
        String urlParameters = "text=" + encode(sentence.getText());
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        wr.write(postData);
      }
      InputStream input = huc.getInputStream();
      List<RuleMatch> ruleMatches = parseJson(input);
      return toRuleMatchArray(ruleMatches);
    } catch (SSLHandshakeException | SocketTimeoutException e) {
      // "hard" errors that will probably not resolve themselves easily:
      throw e;
    } catch (Exception e) {
      // These are issue that can be request-specific, like wrong parameters. We don't throw an
      // exception, as the calling code would otherwise assume this is a persistent error:
      System.err.println("Warn: Failed to query Grammalecte server at " + serverUrl + ": " + e.getClass() + ": " + e.getMessage());
      e.printStackTrace();
    } finally {
      huc.disconnect();
    }
    return new RuleMatch[0];
  }

  @NotNull
  private List<RuleMatch> parseJson(InputStream inputStream) throws IOException {
    Map map = mapper.readValue(inputStream, Map.class);
    List matches = (ArrayList) map.get("data");
    List<RuleMatch> result = new ArrayList<>();
    for (Object match : matches) {
      List<RuleMatch> remoteMatches = getMatches((Map<String, Object>)match);
      result.addAll(remoteMatches);
    }
    return result;
  }

  protected String encode(String plainText) throws UnsupportedEncodingException {
    return URLEncoder.encode(plainText, StandardCharsets.UTF_8.name());
  }

  @NotNull
  private List<RuleMatch> getMatches(Map<String, Object> match) {
    List<RuleMatch> remoteMatches = new ArrayList<>();
    ArrayList matches = (ArrayList) match.get("lGrammarErrors");
    for (Object o : matches) {
      Map pairs = (Map) o;
      int offset = (int) pairs.get("nStart");
      int endOffset = (int)pairs.get("nEnd");
      String id = (String)pairs.get("sRuleId");
      if (ignoreRules.contains(id)) {
        continue;
      }
      String message = pairs.get("sMessage") + " [Grammalecte]";
      GrammalecteInternalRule rule = new GrammalecteInternalRule(id, message);
      RuleMatch extMatch = new RuleMatch(rule, null, offset, endOffset, message);
      List<String> suggestions = (List<String>) pairs.get("aSuggestions");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZ");
      //System.out.println(sdf.format(new Date()) + " Grammalecte: " + pairs.get("sRuleId") + "; " + pairs.get("sMessage") + " => " + suggestions);
      extMatch.setSuggestedReplacements(suggestions);
      remoteMatches.add(extMatch);
    }
    return remoteMatches;
  }

  class GrammalecteInternalRule extends Rule {
    private String id;
    private String desc;

    GrammalecteInternalRule(String id, String desc) {
      this.id = id;
      this.desc = desc;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getDescription() {
      return desc;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) {
      throw new RuntimeException("Not implemented");
    }
  }
  
}
