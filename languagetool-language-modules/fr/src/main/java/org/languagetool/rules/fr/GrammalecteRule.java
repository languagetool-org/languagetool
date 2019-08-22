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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Queries a local Grammalecte server.
 * @since 4.6
 */
@Experimental
public class GrammalecteRule extends Rule {

  private static Logger logger = LoggerFactory.getLogger(GrammalecteRule.class);
  private static final int TIMEOUT_MILLIS = 500;
  private static final long DOWN_INTERVAL_MILLISECONDS = 5000;

  private static long lastRequestError = 0;

  private final ObjectMapper mapper = new ObjectMapper();
  private final GlobalConfig globalConfig;

  // https://github.com/languagetooler-gmbh/languagetool-premium/issues/197:
  private final Set<String> ignoreRules = new HashSet<>(Arrays.asList(
    "tab_fin_ligne",
    "apostrophe_typographique",
    "typo_guillemets_typographiques_doubles_ouvrants",
    "nbsp_avant_double_ponctuation",
    "typo_guillemets_typographiques_doubles_fermants",
    // for discussion, see https://github.com/languagetooler-gmbh/languagetool-premium/issues/229:
    "nbsp_avant_deux_points",  // Useful only if we decide to have the rest of the non-breakable space rules.
    "nbsp_ajout_avant_double_ponctuation",  // Useful only if we decide to have the rest of the non-breakable space rules.
    "apostrophe_typographique_après_t",  // Not useful. While being the technically correct character, it does not matter much.
    "unit_nbsp_avant_unités1",
    "typo_tiret_début_ligne",  // Arguably the same as 50671 and 17342 ; the french special character for lists is a 'tiret cadratin' ; so it should be that instead of a dash. Having it count as a mistake is giving access to the otherwise unaccessible special character. However, lists are a common occurrence, and the special character does not make a real difference. Not really useful but debatable
    "typo_guillemets_typographiques_simples_fermants",
    "typo_apostrophe_incorrecte",
    "unit_nbsp_avant_unités3",
    "nbsp_après_double_ponctuation",
    "typo_guillemets_typographiques_simples_ouvrants",
    "num_grand_nombre_avec_espaces",
    "num_grand_nombre_soudé",
    "typo_parenthèse_ouvrante_collée",  // we already have UNPAIRED_BRACKETS
    "nbsp_après_chevrons_ouvrants",
    "nbsp_avant_chevrons_fermants",
    "nbsp_avant_chevrons_fermants1",
    "nbsp_avant_chevrons_fermants2",
    "typo_points_suspension1",
    "typo_points_suspension2",
    "typo_points_suspension3",
    "tab_début_ligne"
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
    // very basic health check -> mark server as down after an error for given interval
    if (System.currentTimeMillis() - lastRequestError < DOWN_INTERVAL_MILLISECONDS) {
      logger.warn("Warn: Temporarily disabled Grammalecte server because of recent error.");
      return new RuleMatch[0];
    }

    URL serverUrl = new URL(globalConfig.getGrammalecteServer());
    HttpURLConnection huc = (HttpURLConnection) serverUrl.openConnection();
    HttpURLConnection.setFollowRedirects(false);
    huc.setConnectTimeout(TIMEOUT_MILLIS);
    huc.setReadTimeout(TIMEOUT_MILLIS*2);
    if (globalConfig.getGrammalecteUser() != null && globalConfig.getGrammalectePassword() != null) {
      String authString = globalConfig.getGrammalecteUser() + ":" + globalConfig.getGrammalectePassword();
      String encoded = Base64.getEncoder().encodeToString(authString.getBytes());
      huc.setRequestProperty("Authorization", "Basic " + encoded);
    }
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
      lastRequestError = System.currentTimeMillis();
      // still fail silently, better to return partial results than an error
      //throw e;
      logger.warn("Warn: Failed to query Grammalecte server at " + serverUrl + ": " + e.getClass() + ": " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      lastRequestError = System.currentTimeMillis();
      // These are issue that can be request-specific, like wrong parameters. We don't throw an
      // exception, as the calling code would otherwise assume this is a persistent error:
      logger.warn("Warn: Failed to query Grammalecte server at " + serverUrl + ": " + e.getClass() + ": " + e.getMessage());
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
      String message = pairs.get("sMessage").toString();
      GrammalecteInternalRule rule = new GrammalecteInternalRule("grammalecte_" + id, message);
      RuleMatch extMatch = new RuleMatch(rule, null, offset, endOffset, message);
      List<String> suggestions = (List<String>) pairs.get("aSuggestions");
      //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZ");
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
