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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.languagetool.tools.StringTools.trimLeadingAndTrailingSpaces;

public class CatalanRemoteRewriteHelper {

  private static final String SERVER_URL = System.getenv("CA_REMOTE_REWRITE_SERVER");
  private static final String API_KEY = System.getenv("CA_REMOTE_REWRITE_API_KEY");
  private static final String MODEL_ID = System.getenv("CA_REMOTE_REWRITE_MODEL_ID");
  private static int TIMEOUT_MS;
  static {
    try {
      String val = System.getenv("CA_REMOTE_REWRITE_TIME_OUT");
      TIMEOUT_MS = (val != null) ? Integer.parseInt(val) : 5000;
    } catch (NumberFormatException e) {
      TIMEOUT_MS = 5000;
    }
  }
  private static final Logger logger = LoggerFactory.getLogger(CatalanRemoteRewriteHelper.class);

  static boolean isRemoteServiceAvailable() {
    return (SERVER_URL != null && API_KEY !=null && MODEL_ID != null);
  }

  static String sendPostRequest(String sentence, String ruleid) {
    String trimSentence = trimLeadingAndTrailingSpaces(sentence);
    if (sentence == null || trimSentence.isEmpty()) {
      return "";
    }
    Map<String, String> responsesMap = cachedResponses.get(ruleid);
    if (responsesMap != null) {
      String correctedSentence = responsesMap.getOrDefault(trimSentence, "");
      if (!correctedSentence.isEmpty()) {
        return correctedSentence;
      }
    }
    if (!isRemoteServiceAvailable()) {
      return "";
    }
    HttpURLConnection conn = null;
    System.out.println("Requesting server " + SERVER_URL + " for rule " + ruleid);
    logger.info("Requesting server " + SERVER_URL + " for rule " + ruleid);
    try {
      JSONObject payload = new JSONObject();
      payload.put("model", MODEL_ID);
      JSONArray messages = new JSONArray();
      //messages.put(new JSONObject().put("role", "system").put("content", PROMPTS.get(ruleid)));
      //messages.put(new JSONObject().put("role", "user").put("content", sentence));
      messages.put(new JSONObject().put("role", "user").put("content",  PROMPTS.get(ruleid) + "\n\n" + sentence));
      payload.put("messages", messages);
      byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
      URL url = new URL(SERVER_URL);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setUseCaches(false); // IMPORTANT: En POST sempre a false
      conn.setInstanceFollowRedirects(true);
      conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setRequestProperty("Accept", "application/json");
      //conn.setRequestProperty("HTTP-Referer", "");
      //conn.setRequestProperty("X-Title", "");
      conn.setConnectTimeout(TIMEOUT_MS);
      conn.setReadTimeout(TIMEOUT_MS);
      conn.setChunkedStreamingMode(0);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(input);
        os.flush();
      } catch (Exception e) {
        System.err.println("Error opening the output stream: " + e.getMessage());
        throw e;
      }
      int code = conn.getResponseCode();
      InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
      if (is == null) {
        return "";
      }
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
          response.append(line);
        }
        if (code == HttpURLConnection.HTTP_OK) {
          JSONObject jsonResponse = new JSONObject(response.toString());
          return jsonResponse.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim();
        } else {
          System.err.println("API error (" + code + "): " + response + "// PAYLOAD: " + payload.toString());
          logger.error("API error (" + code + "): " + response);
          return "";
        }
      }
    } catch (Exception e) {
      System.err.println("Exception in sendPostRequest: " + e.getMessage());
      e.printStackTrace();
      return "";
    } finally {
      if (conn != null) conn.disconnect();
    }
  }

  static final Map<String, String> PROMPTS = Map.of(
    "GERUNDI_POSTERIORITAT", "Reescriu les frases sense el gerundi de posterioritat. Respon directament " +
      "amb la frase, sense comentaris ni puntuació extra.",
    "CA_SPLIT_LONG_SENTENCE", "Aquesta frase és massa llarga. Divideix-la fent els mínims canvis possibles. " +
      "Respon només amb la frase dividida.",
        "EN_NO_INFINITIU_CAUSAL", "Reescriu la frase canviant la construcció 'al no + infinitiu' o " +
      "'en no + infinitiu' per 'com que...' o 'perquè...', fent els mínims canvis possibles. Respon amb la frase reescrita " +
      "sense comentaris extra.",
  "CA_REMOTE_ESCOLTAR_SENTIR", "Reescriu aquesta frase canviant el verb 'escoltar' pel verb 'sentir', si està mal usat, és a dir, si no vol dir 'parar atenció, atendre o obeir'." +
      "No canviïs el verb si 'escoltar' vol dir 'parar atenció, atendre o obeir', i per tant està ben usat (p. ex., El vaig escoltar atentament)." +
      "Respon directament amb la frase reescrita, sense comentaris ni puntuació extra.");

  //For testing
  static final Map<String, Map<String, String>> cachedResponses = Map.of("GERUNDI_POSTERIORITAT", Map.ofEntries(
    Map.entry("El lladre va atracar dues joieries, escapant-se més tard amb un cotxe robat.",
      ">El lladre va atracar dues joieries i després va escapar-se amb un cotxe robat."),
    Map.entry("El lladre va atracar dues joieries, fugint després amb un cotxe robat.",
      ". El lladre va atracar dues joieries i després va fugir amb un cotxe robat."),
    Map.entry("El lladre va atracar dues joieries, essent assassinat pocs dies després.",
      "El lladre va atracar dues joieries i va ser assassinat pocs dies després."),
    Map.entry("El lladre va atracar dues joieries, fugint al cap de poc amb un cotxe robat.",
      "El lladre va atracar dues joieries i al cap de poc va fugir amb un cotxe robat."),
    Map.entry("El lladre va atracar dues joieries, escapant-se al cap d'una estona amb un cotxe robat.",
      "El lladre va atracar dues joieries i després es va escapar amb un cotxe robat."),
    Map.entry("Es van presentar els estatuts, aprovant-se l'endemà mateix.",
      "Es van presentar els estatuts i es van aprovar l'endemà mateix."),
    Map.entry("Es van presentar els estatuts, sent aprovats l'endemà mateix.",
      "Es van presentar els estatuts i van ser aprovats l'endemà mateix."),
    Map.entry("Es van presentar els estatuts, sent aprovats al cap de poc.",
      "Es van presentar els estatuts i al cap de poc van ser aprovats."),
    Map.entry("Es van presentar diverses esmenes, sent aprovades per una àmplia majoria.",
      "Es van presentar diverses esmenes, i van ser aprovades per una àmplia majoria."),
    Map.entry("Es presentaren diverses esmenes, sent aprovades per una àmplia majoria.",
      "Es presentaren diverses esmenes, i foren aprovades per una àmplia majoria."),
    Map.entry("Hi hagué un accident greu a l'autopista entre un camió i un turisme, morint al cap de poc els passatgers del turisme.",
      "Hi hagué un accident greu a l'autopista entre un camió i un turisme, i els passatgers del turisme van morir al cap de poc."),
    Map.entry("Hi hagué un accident greu a l'autopista entre un camió i un turisme, morint els passatgers del turisme al cap de poc.",
      "Hi hagué un accident greu a l'autopista entre un camió i un turisme i els passatgers del turisme van morir al cap de poc."),
    Map.entry("Va arribar tard a l'examen, perdent així després tota oportunitat d'aprovar l'assignatura.",
        "Va arribar tard a l'examen i, per això, va perdre tota oportunitat d'aprovar l'assignatura.")

  ),
    "CA_SPLIT_LONG_SENTENCE", Map.ofEntries(
      Map.entry("En una tarda grisa que avançava sense pressa sobre els carrers estrets de la ciutat, mentre els comerços abaixaven persianes i el soroll del trànsit es diluïa en un murmuri constant, un home caminava pensant en decisions ajornades, en paraules no dites i en projectes que havia volgut compondre amb rigor, però que el cansament havia anat desfigurant i, així i tot, convençut que encara disposava de prou lucidesa per a ordenar les idees, assumir els errors, fer servir l'experiència acumulada com a criteri i continuar avançant amb una determinació menys impulsiva però més sòlida.",
        "En una tarda grisa que avançava sense pressa sobre els carrers estrets de la ciutat, mentre els comerços abaixaven persianes i el soroll del trànsit es diluïa en un murmuri constant, un home caminava pensant en decisions ajornades, en paraules no dites i en projectes que havia volgut compondre amb rigor, però que el cansament havia anat desfigurant. I, així i tot, convençut que encara disposava de prou lucidesa per a ordenar les idees, assumir els errors, fer servir l'experiència acumulada com a criteri i continuar avançant amb una determinació menys impulsiva però més sòlida.")
  ),
    "CA_REMOTE_ESCOLTAR_SENTIR", Map.ofEntries(
      Map.entry("Vaig escoltar que deien coses inversemblants.","Vaig sentir que deien coses inversemblants."),
      Map.entry("Vaig escoltar atentament les seves explicacions.","Vaig escoltar atentament les seves explicacions.")
      ),
      "EN_NO_INFINITIU_CAUSAL", Map.ofEntries(
        Map.entry("En no tenir efectes pràctics, vam decidir deixar-ho córrer.", "Com que no tenia efectes pràctics, vam decidir deixar-ho córrer."),
        Map.entry("Al no tenir efectes pràctics, se suspèn la sessió.", "Com que no té efectes pràctics, se suspèn la sessió.")
    )

  );
}
