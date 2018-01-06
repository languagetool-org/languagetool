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
package org.languagetool.rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Experimental;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries an OpenNMT server started like this:
 * {@code th tools/rest_translation_server.lua -replace_unk -model ...}
 */
@Experimental
public class OpenNMTRule extends Rule {

  private static final String defaultServerUrl = "http://127.0.0.1:7784/translator/translate";
  
  private final String serverUrl;
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * @param serverUrl URL of the OpenNMT server, like {@code http://127.0.0.1:7784/translator/translate}
   */
  public OpenNMTRule(String serverUrl) {
    this.serverUrl = serverUrl;
    setDefaultOff();
  }

  /**
   * Expects an OpenNMT server running at http://127.0.0.1:7784/translator/translate
   */
  public OpenNMTRule() {
    this(defaultServerUrl);
  }

  @Override
  public String getId() {
    return "OPENNMT_RULE";
  }

  @Override
  public String getDescription() {
    return "Get corrections from an OpenNMT server (beta)";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    // TODO: send all sentences at once for less overhead?
    String json = createJson(sentence);
    URL url = new URL(serverUrl);
    HttpURLConnection conn = postToServer(json, url);
    int responseCode = conn.getResponseCode();
    if (responseCode == 200) {
      InputStream inputStr = conn.getInputStream();
      JsonNode response = mapper.readTree(inputStr);
      String translation = response.get(0).get(0).get("tgt").textValue();
      if (translation.contains("<unk>")) {
        throw new RuntimeException("'<unk>' found in translation - please start the OpenNMT server with the -replace_unk option");
      }
      // TODO: whitespace is introduced and needs to be properly removed - we should use the 'src'
      // key for comparison, but we'll still need to clean up when using the suggestion...
      translation = detokenize(translation);
      String cleanTranslation = translation.replaceAll(" ([.,;:])", "$1");
      String sentenceText = sentence.getText();
      if (!cleanTranslation.trim().equals(sentenceText.trim())) {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        int from = getLeftWordBoundary(sentenceText, getFirstDiffPosition(sentenceText, cleanTranslation));
        int to = getRightWordBoundary(sentenceText, getLastDiffPosition(sentenceText, cleanTranslation));
        int replacementTo = getRightWordBoundary(cleanTranslation, getLastDiffPosition(cleanTranslation, sentenceText));
        String message = "OpenNMT suggests that this might(!) be better phrased differently, please check.";
        RuleMatch ruleMatch = new RuleMatch(this, sentence, from, to, message);
        ruleMatch.setSuggestedReplacement(cleanTranslation.substring(from, replacementTo)); 
        ruleMatches.add(ruleMatch);
        return toRuleMatchArray(ruleMatches);
      }
      return new RuleMatch[0];
    } else {
      InputStream inputStr = conn.getErrorStream();
      String error = CharStreams.toString(new InputStreamReader(inputStr, Charsets.UTF_8));
      throw new RuntimeException("Got error " + responseCode + " from " + url + ": " + error);
    }
  }

  private String detokenize(String translation) {
    return translation
                    .replaceAll(" 's", "'s")
                    .replaceAll(" ' s", "'s")
                    .replaceAll(" ' t", "'t")
                    .replace(" .", ".")
                    .replace(" ,", ",");
  }

  int getFirstDiffPosition(String text1, String text2) {
    for (int i = 0; i < text1.length(); i++) {
      if (i < text2.length()) {
        if (text1.charAt(i) != text2.charAt(i)) {
          return i;
        }
      } else {
        return i;
      }
    }
    if (text1.length() != text2.length()) {
      return text1.length();
    }
    return -1;
  }

  int getLastDiffPosition(String text1, String text2) {
    StringBuilder reverse1 = new StringBuilder(text1.trim()).reverse();
    StringBuilder reverse2 = new StringBuilder(text2.trim()).reverse();
    int revDiffPos = getFirstDiffPosition(reverse1.toString(), reverse2.toString());
    if (revDiffPos != -1) {
      return text1.length() - revDiffPos;
    } else {
      return -1;
    }
  }

  int getLeftWordBoundary(String text, int pos) {
    while (pos >= 1) {
      if (Character.isAlphabetic(text.charAt(pos - 1))) {
        pos--;
      } else {
        break;
      }
    }
    return pos;
  }
  
  int getRightWordBoundary(String text, int pos) {
    while (pos >= 0 && pos < text.length()) {
      if (Character.isAlphabetic(text.charAt(pos))) {
        pos++;
      } else {
        break;
      }
    }
    return pos;
  }
  
  private String createJson(AnalyzedSentence sentence) throws JsonProcessingException {
    ArrayNode list = mapper.createArrayNode();
    ObjectNode node = list.addObject();
    node.put("src", sentence.getText());
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
  }

  @NotNull
  private HttpURLConnection postToServer(String json, URL url) throws IOException {
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("POST");
    conn.setUseCaches(false);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);
    try {
      try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
        //System.out.println("POSTING: " + json);
        dos.write(json.getBytes("utf-8"));
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not connect OpenNMT server at " + url);
    }
    return conn;
  }

}
