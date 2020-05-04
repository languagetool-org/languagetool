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
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;

import static java.lang.Thread.currentThread;

class CheckCallable implements Callable<File> {

  // This many sentences are aggregated into one request. Do NOT just increase, as the chance
  // of results getting mixed up increases then (the batchSize determines the filename, which is then used
  // as a title in MatchKey):
  private final static int batchSize = 10;
  private final static int maxTries = 10;  // maximum tries for HTTP problems
  private final static int retrySleepMillis = 1000;

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
            try {
              CheckResult result = checkByPost(url, postData);
              //System.out.println(threadName + " - answered by " + result.backendServer);
              JsonNode jsonNode = mapper.readTree(result.json);
              ((ObjectNode)jsonNode).put("title", HttpApiSentenceChecker.class.getSimpleName() + "-result-" + count + "-" + startLine + "-" + i);  // needed for MatchKey to be specific enough
              fw.write(jsonNode.toString() + "\n");
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

  private CheckResult checkByPost(URL url, String postData) throws IOException {
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
          throw new IOException("Failed posting to " + url + ", server responded with code " + httpConn.getResponseCode() + " and error: " + error);
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

  static class CheckResult {
    String json;
    String backendServer;
    private CheckResult(String json, String backendServer) {
      this.json = json;
      this.backendServer = backendServer;
    }
  }
}
