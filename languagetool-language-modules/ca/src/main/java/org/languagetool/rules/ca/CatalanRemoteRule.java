/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedSentence;
import org.languagetool.UserConfig;
import org.languagetool.rules.Categories;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.tools.DiffsAsMatches;
import org.languagetool.tools.PseudoMatch;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatalanRemoteRule extends TextLevelRule {

  private static String[] serverUrls;
  private UserConfig userConfig;
  private static int TIMEOUT_MS = 2000;
  boolean ab_test = false; //AB test disabled, opened 100%
  private static int MAX_SENTENCES_FIRST_SERVER = 4;

  public CatalanRemoteRule(ResourceBundle messages, UserConfig userConfig) {
    super.setCategory(Categories.PUNCTUATION.getCategory(messages));
    this.userConfig = userConfig;
    String serverUrlStr = System.getenv("CA_REMOTE_RULE_SERVER");
    if (serverUrlStr != null && !serverUrlStr.isEmpty()) {
      serverUrls = serverUrlStr.split(",");
    } else {
      this.setDefaultOff();
    }
    String timeoutStr = System.getenv("CA_REMOTE_RULE_SERVER_TIMEOUT");
    if (timeoutStr != null && !timeoutStr.isEmpty()) {
      TIMEOUT_MS = Integer.parseInt(timeoutStr);
    }
    String maxSentencesFirstServerStr = System.getenv("MAX_SENTENCES_FIRST_SERVER");
    if (maxSentencesFirstServerStr != null && !maxSentencesFirstServerStr.isEmpty()) {
      MAX_SENTENCES_FIRST_SERVER = Integer.parseInt(maxSentencesFirstServerStr);
    }
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    return doRule(sentences);
  }

  private List<String> getSentencesInPlainText(List<AnalyzedSentence> sentences) {
    List<String> sentencesPlainText = new ArrayList<>();
    for (AnalyzedSentence sentence : sentences) {
      sentencesPlainText.add(sentence.getText());
    }
    return sentencesPlainText;
  }

  private static final Pattern TRIM_PATTERN = Pattern.compile("^[\\s\\u00A0]+|[\\s\\u00A0]+$");

  private String trimAllSpaces (String s) {
    Matcher matcher = TRIM_PATTERN.matcher(s);
    return matcher.replaceAll("");
  }
  private RuleMatch[] doRule(List<AnalyzedSentence> sentences) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    List<String> sentencesPlainText = getSentencesInPlainText(sentences);
    List<String> correctedSentences = sendPostRequest(sentencesPlainText);
    if (correctedSentences == null || correctedSentences.isEmpty()) {
      return toRuleMatchArray(ruleMatches);
    }
    DiffsAsMatches diffsAsMatches = new DiffsAsMatches();
    for (int i=0; i<sentencesPlainText.size();i++) {
      List<PseudoMatch> pseudoMatches = diffsAsMatches.getPseudoMatches(sentencesPlainText.get(i), correctedSentences.get(i));
      for (PseudoMatch pseudoMatch : pseudoMatches) {
        String suggestion = pseudoMatch.getReplacements().get(0);
        // ignora espais al final o al principi
        String underlined = sentencesPlainText.get(i).substring(pseudoMatch.getFromPos(), pseudoMatch.getToPos());
        if ((pseudoMatch.getToPos() == sentencesPlainText.get(i).length() || pseudoMatch.getFromPos()==0) && trimAllSpaces(underlined).isEmpty()) {
          continue;
        }
        // ignora canvis en espais
        if (trimAllSpaces(suggestion).equals(trimAllSpaces(underlined))) {
          continue;
        }
        if (pseudoMatch.getToPos() <= pseudoMatch.getFromPos()) {
          //No hauria de passar
          throw new IllegalArgumentException("fromPos (" + pseudoMatch.getFromPos() + ") must be less than toPos ("
            + pseudoMatch.getToPos()  + "). Sentence: "+sentencesPlainText.get(i));
        }
        String message = "Canvi recomanat pel model d'aprenentatge automàtic.";
        if (suggestion.equals(underlined+",")) {
          message = "Sembla que hi falta una coma.";
        } else {
          continue;
        }

        RuleMatch match = new RuleMatch(this, sentences.get(i), pos + pseudoMatch.getFromPos(),
          pos + pseudoMatch.getToPos(), message);
        match.setSuggestedReplacements(pseudoMatch.getReplacements());
        ruleMatches.add(match);
      }
      pos += sentences.get(i).getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }


  @Override
  public int minToCheckParagraph() {
    return 0;
  }

  @Override
  public String getId() {
    return "CA_REMOTE_RULE";
  }

  @Override
  public String getDescription() {
    return "Recomanació del model d'aprenentatge automàtic.";
  }

  public void setABTest(boolean _ab_test) {
    ab_test = _ab_test;
  }

  private static List<String> sendPostRequest(List<String> sentences) {
    try {
      // URL del endpoint
      String serverUrl = serverUrls[0];
      if (serverUrls.length == 2) {
        // short texts go to the first server (CPU)
        // long texts go to the second server (GPU)
        if (sentences.size() > MAX_SENTENCES_FIRST_SERVER) {
          serverUrl = serverUrls[1];
        }
      }
      URL url = new URL(serverUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json; utf-8");
      conn.setRequestProperty("Accept", "application/json");
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setDoOutput(true);
      conn.setConnectTimeout(TIMEOUT_MS);
      conn.setReadTimeout(TIMEOUT_MS);
      //conn.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));

      // Crear el JSON a partir de la llista de textos
      JSONObject jsonInput = new JSONObject();
      jsonInput.put("sentences", sentences); // Afegir la llista com a JSONArray
      String jsonInputString = jsonInput.toString();

      // Enviar la petició
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      // Obtenir la resposta
      int code = conn.getResponseCode();
      if (code == HttpURLConnection.HTTP_OK) { // Resposta exitosa (200)
        // Llegir el cos de la resposta
        InputStream inputStream = conn.getInputStream();
        String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // Analitzar el JSON de la resposta
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray outputSentences = jsonResponse.getJSONArray("output_sentences");

        // Convertir el JSONArray a una llista de String
        List<String> result = new ArrayList<>();
        for (int i = 0; i < outputSentences.length(); i++) {
          result.add(outputSentences.getString(i));
        }

        conn.disconnect();
        return result; // Retornar la llista de frases processades
      } else {
        System.err.println("Error in the resquest. Code: " + code);
        conn.disconnect();
        return List.of(); // Retornar una llista buida en cas d'error
      }
    } catch (Exception e) {
      e.printStackTrace();
      return List.of(); // Retornar una llista buida en cas d'excepció
    }
  }

}
