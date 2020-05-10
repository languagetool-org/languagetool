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
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;

import static java.lang.Thread.currentThread;

class CheckCallable implements Callable<File> {

  // This many sentences are aggregated into one request. Do NOT just increase, as the chance
  // of results getting mixed up increases then (the batchSize determines the filename, which is then used
  // as a title in MatchKey):
  private final static int batchSize = 10;
  private final static int maxTries = 10;  // maximum tries for HTTP problems
  private final static int retrySleepMillis = 1000;
  private static final JsonFactory factory = new JsonFactory();

  private final int count;
  private final String baseUrl;
  private final String token;
  private final File file;
  private final String langCode;

  CheckCallable(int count, String baseUrl, String token, File file, String langCode) {
    this.count = count;
    this.baseUrl = Objects.requireNonNull(baseUrl);
    this.token = token;
    this.file = Objects.requireNonNull(file);
    this.langCode = Objects.requireNonNull(langCode);
  }

  @Override
  public File call() throws Exception {
    List<String> allLines = Files.readAllLines(file.toPath());
    String threadName = currentThread().getName();
    System.out.println(threadName + " - loaded " + allLines.size() + " lines from " + file.getName());
    List<String> tempLines = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    int startLine = 0;
    File outFile = new File(System.getProperty("java.io.tmpdir"), HttpApiSentenceChecker.class.getSimpleName() + "-result-" + count + ".json");
    try (FileWriter fw = new FileWriter(outFile)) {
      for (int i = 0; i < allLines.size(); i++) {
        String line = allLines.get(i);
        tempLines.add(line);
        if (tempLines.size() >= batchSize || i == allLines.size() - 1) {
          String textToCheck = String.join("\n\n", tempLines);
          URL url = Tools.getUrl(baseUrl + "/v2/check");
          //System.out.println("textToCheck: " + textToCheck);
          String postData = "language=" + langCode + "&text=" + URLEncoder.encode(textToCheck, "UTF-8");
          postData += token != null ? "&token=" + URLEncoder.encode(token, "UTF-8"): "";
          String tokenInfo = token != null ? " with token" : " without token";
          float progress = (float)i / allLines.size() * 100.0f;
          System.out.printf(Locale.ENGLISH, threadName + " - Posting " + tempLines.size() + " texts with " + textToCheck.length() +
            " chars to " + url +  tokenInfo + ", %.1f%%\n", progress);
          for (int retry = 1; true; retry++) {
            String pseudoFileName = HttpApiSentenceChecker.class.getSimpleName() + "-result-" + count + "-" + startLine + "-" + i;
            try {
              CheckResult result = checkByPost(url, postData);
              //System.out.println(threadName + " - answered by " + result.backendServer);
              JsonNode jsonNode = mapper.readTree(result.json);
              ((ObjectNode)jsonNode).put("title", pseudoFileName);  // needed for MatchKey to be specific enough
              fw.write(jsonNode + "\n");
              tempLines.clear();
              startLine = i;
              break;
            } catch (ApiErrorException e) {
              // Convert the error to a fake rule match so it will appear as part of the diff, instead
              // of ending up in some log file:
              System.err.println(threadName + " - POST to " + url + " failed: " + e.getMessage() +
                ", try " + retry + ", max tries " + maxTries + ", no retries useful for this type of error, storing error as pseudo match");
              writeFakeError(mapper, fw, textToCheck, pseudoFileName, e);
              tempLines.clear();
              startLine = i;
              break;
            } catch (Exception e) {
              if (retry >= maxTries) {
                System.err.println(threadName + " - POST to " + url + " failed: " + e.getMessage() +
                  ", try " + retry + ", max tries " + maxTries + ", no retries left, throwing exception");
                throw e;
              } else {
                long sleepMillis = retrySleepMillis * retry;
                System.err.println(threadName + " - POST to " + url + " failed: " + e.getMessage() +
                  ", try " + retry + ", max tries " + maxTries + ", sleeping " + sleepMillis + "ms before retry");
                Thread.sleep(sleepMillis);
                //e.printStackTrace();
              }
            }
          }
        }
      }
    }
    System.out.println(threadName + " - Done.");
    return outFile;
  }

  private void writeFakeError(ObjectMapper mapper, FileWriter fw, String textToCheck, String pseudoFileName, ApiErrorException e) throws IOException {
    Language lang = Languages.getLanguageForShortCode(langCode);
    JLanguageTool lt = new JLanguageTool(lang);
    RuleMatch ruleMatch = new RuleMatch(new FakeRule(), lt.getAnalyzedSentence(textToCheck), 0, 1, "API request failed in a way so that re-try makes no sense: " + e.getMessage());
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
      conn.setDoOutput(true);
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
            throw new IOException("Failed posting to " + url + ", server responded with code " + httpConn.getResponseCode() + " and error: " + error);
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
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
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
