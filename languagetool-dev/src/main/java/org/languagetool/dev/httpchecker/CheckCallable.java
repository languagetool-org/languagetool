/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.httpchecker;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.RuleMatchesAsJsonSerializer;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

import static java.lang.Thread.currentThread;

class CheckCallable implements Callable<File> {

  static final String FAIL_MESSAGE = "API request failed in a way so that re-try makes no sense: ";

  // This many sentences are aggregated into one request. Do NOT just increase, as the chance
  // of results getting mixed up increases then (the batchSize determines the filename, which is then used
  // as a title in MatchKey):
  private final static int maxTries = 10;  // maximum tries for HTTP problems
  private final static int retrySleepMillis = 1000;

  private final int count;
  private final String baseUrl;
  private final String token;
  private final List<String> texts;
  private final String langCode;
  @Nullable
  private final String user;
  @Nullable
  private final String password;

  CheckCallable(int count, String baseUrl, String token, List<String> texts, String langCode, @Nullable String user, @Nullable String password) {
    this.count = count;
    this.baseUrl = Objects.requireNonNull(baseUrl);
    this.token = token;
    this.texts = Objects.requireNonNull(texts);
    this.langCode = Objects.requireNonNull(langCode);
    this.user = user;
    this.password = password;
  }

  @Override
  public File call() throws Exception {
    String threadName = currentThread().getName();
    ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    // use a filename with a very low chance of being used by someone else (unless this very code runs in parallel):
    String baseUrlCode = String.valueOf(baseUrl.hashCode()).substring(0, 5);
    String filename = HttpApiSentenceChecker.class.getSimpleName() + "-result-" + langCode + "-" + baseUrlCode + "-" + count + ".json";
    File outFile = new File(System.getProperty("java.io.tmpdir"), filename);
    int totalLen = texts.stream().mapToInt(String::length).sum();
    printOut(threadName + " - Going to post " + texts.size() + " texts with a total length of " + totalLen + " chars");
    try (FileWriter fw = new FileWriter(outFile)) {
      int i = 0;
      for (String text : texts) {
        URL url = Tools.getUrl(baseUrl + "/v2/check");
        String postData = "language=" + langCode +
            "&text=" + URLEncoder.encode(text, "UTF-8") +
            "&level=picky" +
            "&enableTempOffRules=true";
        postData += token != null ? "&token=" + URLEncoder.encode(token, "UTF-8"): "";
        String tokenInfo = token != null ? " with token" : " without token";
        float progress = (float)i++ / texts.size() * 100.0f;
        printOut(String.format(Locale.ENGLISH, threadName + " - Posting text with " + text.length() +
          " chars to " + url +  tokenInfo + ", %.1f%%", progress));
        for (int retry = 1; true; retry++) {
          String pseudoFileName = HttpApiSentenceChecker.class.getSimpleName() + "-result-" + text.hashCode();
          try {
            CheckResult result = checkByPost(url, postData);
            //printOut(threadName + " - answered by " + result.backendServer);
            JsonNode jsonNode = mapper.readTree(result.json);
            //if (result.json.contains("SET_RULE_ID_HERE")) {
            //  System.out.println("-----------------------------");
            //  System.out.println(text);
            //}
            ((ObjectNode)jsonNode).put("title", pseudoFileName);  // needed for MatchKey to be specific enough
            fw.write(jsonNode + "\n");
            break;
          } catch (ApiErrorException e) {
            // Convert the error to a fake rule match so it will appear as part of the diff, instead
            // of ending up in some log file:
            printErr(threadName + " - POST to " + url + " failed with " + e.getClass().getName() + ": " + e.getMessage() +
              ", try " + retry + ", max tries " + maxTries + ", no retries useful for this type of error, storing error as pseudo match");
            writeFakeError(mapper, fw, text, pseudoFileName, e, 0);
            break;
          } catch (Exception e) {
            if (retry >= maxTries) {
              printErr(threadName + " - POST to " + url + " failed with " + e.getClass().getName() + ": " + e.getMessage() +
                ", try " + retry + ", max tries " + maxTries + ", no retries left, writing fake error");
              writeFakeError(mapper, fw, text, pseudoFileName, new ApiErrorException(e.getClass().getName() + ": " + e.getMessage()), retry);
              break;
            } else {
              long sleepMillis = retrySleepMillis * retry;
              printErr(threadName + " - POST to " + url + " failed with " + e.getClass().getName() + ": " + e.getMessage() +
                ", try " + retry + ", max tries " + maxTries + ", sleeping " + sleepMillis + "ms before retry");
              Thread.sleep(sleepMillis);
              //e.printStackTrace();
            }
          }
        }
      }
    }
    printOut(threadName + " - Done.");
    printOut(threadName + " - Output written to " + outFile.getName());
    return outFile;
  }

  private void printOut(String s) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    System.out.println(sdf.format(new Date()) + " " + s);
  }

  private void printErr(String s) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    System.err.println(sdf.format(new Date()) + " " + s);
  }

  private synchronized void writeFakeError(ObjectMapper mapper, FileWriter fw, String textToCheck, String pseudoFileName, ApiErrorException e, int retries) throws IOException {
    Language lang = Languages.getLanguageForShortCode(langCode);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
    RuleMatch ruleMatch = new RuleMatch(new FakeRule(), lt.getAnalyzedSentence(textToCheck), 0, 1, FAIL_MESSAGE + e.getMessage() + " (retries: " + retries + ")");
    DetectedLanguage detectedLang = new DetectedLanguage(lang, lang);
    String json = new RuleMatchesAsJsonSerializer().ruleMatchesToJson(Collections.singletonList(ruleMatch), textToCheck, 100, detectedLang);
    JsonNode jsonNode = mapper.readTree(json);
    ((ObjectNode)jsonNode).put("title", pseudoFileName);  // needed for MatchKey to be specific enough
    fw.write(jsonNode + "\n");
  }

  private CheckResult checkByPost(URL url, String postData) throws IOException, ApiErrorException {
    String keepAlive = System.getProperty("http.keepAlive");
    try {
      System.setProperty("http.keepAlive", "false");  // without this, there's an overhead of about 1 second - not sure why
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(20*1000);
      conn.setReadTimeout(60*1000);
      conn.setDoOutput(true);
      if (user != null && password != null) {
        String authString = user + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(authString.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encoded);
      }
      try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
        writer.write(postData);
        writer.flush();
        HttpURLConnection httpConn = (HttpURLConnection)conn;
        InputStream inputStream;
        if (httpConn.getResponseCode() >= 400) {
          inputStream = httpConn.getErrorStream();
          String error = StringTools.streamToString(inputStream, "UTF-8");
          if (httpConn.getResponseCode() == 400) {
            // errors where repeating the request probably won't help
            throw new ApiErrorException(error);
          } else {
            String backendServer = httpConn.getHeaderField("x-backend-server");
            throw new IOException("Failed posting to " + url + ", server " + backendServer + " responded with code " + httpConn.getResponseCode() + " and error: " + error + " for postData: " + postData);
          }
        } else {
          inputStream = httpConn.getInputStream();
          String json = StringTools.streamToString(inputStream, "UTF-8");
          String backendServer = conn.getHeaderField("x-backend-server");
          return new CheckResult(json, backendServer);
        }
      }
    } finally {
      if (keepAlive != null) {
        System.setProperty("http.keepAlive", keepAlive);
      }
    }
  }

  static class FakeRule extends Rule {
    @Override
    public String getId() {
      return "FAKE_RULE";
    }
    @Override
    public String getDescription() {
      return "Pseudo rule to contain API error";
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) {
      return new RuleMatch[0];
    }
  }

  static class ApiErrorException extends Exception {
    ApiErrorException(String message) {
      super(message);
    }
  }

  static class CheckResult {
    String json;
    String backendServer;
    private CheckResult(String json, String backendServer) {
      this.json = json;
      this.backendServer = backendServer;
    }
  }
}
