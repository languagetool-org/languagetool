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
package org.languagetool.dev.simulation;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sends requests to a local LT server, simulating a real user of the browser add-on or languagetool.org.
 * TODO: use several threads
 */
class TypingSimulator {

  private static final String apiUrl = "http://localhost:8081/v2/check";
  private static final boolean dryMode = false;  // don't actually send requests in dry mode
  //private static final String apiUrl = "https://api.languagetool.org/v2/check";
  private static final int warmUpChecks = 20;        // checks at start-up not to be considered for calculation of average values
  private static final float copyPasteProb = 0.05f;  // per document
  private static final float backSpaceProb = 0.05f;  // per character
  private static final float typoProb = 0.03f;       // per character
  private static final int minWaitMillis = 0;        // more real: 10
  private static final int avgWaitMillis = 0;        // more real: 100
  private static final int checkAtMostEveryMillis = 10;  // more real: 1500

  static class Stats {
    private static final int stepSize = 50;
    private long totalTime = 0;
    private long totalChecks = 0;
    private long totalChecksSkipped = 0;
    private final Map<Integer,Integer> sizeToChecks = new TreeMap<>();
    private int sizeToChecksLarger = 0;
    Stats() {
      for (int i = 0; i <= 500; i+= stepSize) {
        sizeToChecks.put(i, 0);
      }
    }
    void trackRequestBySize(int size) {
      if (size >= 550) {
        sizeToChecksLarger++;
      } else {
        int key = size - size % stepSize;
        sizeToChecks.put(key, sizeToChecks.get(key)+1);
      }
    }
    void printRequestSizeSummary() {
      System.out.println("Summary of request sizes sent ('0' means 0 to 49 chars):");
      for (int i = 0; i <= 500; i+= stepSize) {
        Integer checks = sizeToChecks.get(i);
        float percent = (float)checks / (float)totalChecks * 100.0f;
        System.out.printf(Locale.ENGLISH, "%s  -> %d (%.0f%%)\n", StringUtils.leftPad(String.valueOf(i), 3), checks, percent);
      }
      float percent = (float)sizeToChecksLarger / (float)totalChecks * 100.0f;
      System.out.printf(Locale.ENGLISH, "%s  -> %d (%.0f%%)\n", StringUtils.leftPad("550+", 3), sizeToChecksLarger, percent);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + TypingSimulator.class.getSimpleName() + " <input> <docsPerRun>");
      System.exit(1);
    }
    if (avgWaitMillis < 2 && avgWaitMillis != 0) {
      throw new RuntimeException("Set avgWaitMillis to > 1 or to 0");
    }
    DocProvider docProvider = new DocProvider(Files.readAllLines(Paths.get(args[0])));
    //DocProvider docProvider = new DocProvider(Arrays.asList("Das hier ist ein Test"/*, "Hier kommt das zweite Dokument."*/));
    int docsPerRun = Integer.parseInt(args[1]);
    if (docsPerRun < 1000) {
      System.out.println("*** WARNING: use >= 1000 for docsPerRun or results will not be realistic (watch the size distribution printed at the end)");
    }
    long startTime = System.currentTimeMillis();
    new TypingSimulator().run(docProvider, docsPerRun);
    long endTime = System.currentTimeMillis();
    System.out.println("Total time for " + TypingSimulator.class.getSimpleName() + ": " + (endTime-startTime) + "ms");
  }

  private void run(DocProvider docs, int docsPerRun) {
    System.out.println("Using API at " + apiUrl);
    List<Long> totalTimes = new ArrayList<>();
    List<Float> avgTimes = new ArrayList<>();
    int maxRuns = 3;  // keep at 3, the chart library needs 3 values for the error bars
    Random rnd = new Random(123);  // not inside loop, so every loop gets its own random data (so we don't just measure cache)
    System.out.printf("Using %d docs per run\n", docsPerRun);
    for (int i = 0; i < maxRuns; i++) {
      System.out.println("=== Run " + (i+1) + " of " + maxRuns + " =====================");
      Stats stats = new Stats();
      for (int j = 0; j < docsPerRun; j++) {
        System.out.printf("Run %d, doc %d/%d...\n", i+1, j, docsPerRun);
        runOnDoc(docs.getDoc(), rnd, stats);
      }
      totalTimes.add(stats.totalTime);
      float avg = (float) stats.totalTime / (float) stats.totalChecks;
      avgTimes.add(avg);
      stats.printRequestSizeSummary();
      docs.reset();  // reset so that each run gets different docs, but of same length (to keep results comparable)
    }
    totalTimes.sort(Long::compareTo);
    avgTimes.sort(Float::compareTo);
    System.out.println("Total times: " + totalTimes + " ms");
    System.out.printf(Locale.ENGLISH, "Avg. times per doc: %s ms\n", avgTimes);
    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    String totalTimesStr = totalTimes.stream().map(k -> k.toString()).collect(Collectors.joining(";"));
    String avgTimesStr = avgTimes.stream().map(k -> k.toString()).collect(Collectors.joining(";"));
    System.out.printf(Locale.ENGLISH, "CSV: %s,%s,%s\n", date, totalTimesStr, avgTimesStr);  // so results can easily be grepped into a CSV
  }

  private void runOnDoc(String doc, Random rnd, Stats stats) {
    if (rnd.nextFloat() < copyPasteProb) {
      check(doc, stats, true);
    } else {
      long lastCheck = 0;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < doc.length(); i++) {
        if (rnd.nextFloat() < typoProb) {
          if (rnd.nextBoolean()) {
            sb.append("x");  // simulate randomly inserted char
          } else if (sb.length() > 0) {
            sb.replace(sb.length()-1, sb.length(), "");  // simulate random char left out
          }
        }
        if (rnd.nextFloat() < backSpaceProb && i > 2) {
          sb.replace(sb.length()-1, sb.length(), "");
          check(sb.toString(), stats, false);
          i -= 2;
        } else {
          char c = doc.charAt(i);
          sb.append(c);
        }
        long millisSinceLastCheck = System.currentTimeMillis() - lastCheck;
        if (millisSinceLastCheck > checkAtMostEveryMillis || i == doc.length()-1) {
          check(sb.toString(), stats, false);
          lastCheck = System.currentTimeMillis();
        }
        sleep(rnd);
      }
    }
  }

  private void sleep(Random rnd) {
    if (avgWaitMillis == 0) {
      return;
    }
    if (!dryMode) {
      try {
        int waitMillis;
        do {
          double val = rnd.nextGaussian() * avgWaitMillis + avgWaitMillis;
          waitMillis = (int) Math.round(val);
        } while (waitMillis <= 0);
        //System.out.println("waiting " + waitMillis);
        Thread.sleep(minWaitMillis + waitMillis);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void check(String doc, Stats stats, boolean checkCompleteText) {
    try {
      if (checkCompleteText) {
        checkByPOST(doc, "allButTextLevelOnly", stats);
      } else {
        String[] paras = doc.split("\n\n");
        String lastPara = paras[paras.length-1];
        //if (!lastPara.equals(doc)) {
        //  System.out.println("doc/last: " + doc.length() + " / " + lastPara.length() + ": " + lastPara);
        //}
        checkByPOST(lastPara, "allButTextLevelOnly", stats);
      }
      checkByPOST(doc, "textLevelOnly", stats);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void checkByPOST(String text, String mode, Stats stats) throws IOException {
    long runTimeStart = System.currentTimeMillis();
    String postData =
            "&mode=" + mode +
            "&text=" + URLEncoder.encode(text, "UTF-8") +
            "&textSessionId=10914:1608926270970" +
            "&enableHiddenRules=true" +
            "&motherTongue=de" +
            "&language=auto" +
            "&noopLanguages=de,en" +
            "&preferredLanguages=de,en" +
            "&preferredVariants=en-US,de-DE,pt-BR,ca-ES" +
            "&disabledRules=WHITESPACE_RULE" +
            "&useragent=performance-test";
    URL url = new URL(apiUrl +
            "?instanceId=10914%3A1608926270970" +
            "&c=1" +
            "&v=0.0.0");
    //System.out.println("Sending to " + apiUrl + ", " + mode + ": " + text);
    try {
      Map<String, String> map = new HashMap<>();
      if (!dryMode) {
        checkAtUrlByPost(url, postData, map);
      }
      long runTime = System.currentTimeMillis() - runTimeStart;
      System.out.printf("%sms %s chars %s: %s %s\n", String.format("%1$5d", runTime), String.format("%1$5d", text.length()),
              String.format("%1$10s", mode.replaceAll("[Tt]extLevelOnly", "TLO")), StringUtils.abbreviate(text.replace("\n", "\\n"), 100),
              dryMode ? "[dryMode]": "");
      //System.out.println("Checking " + text.length() + " chars took " + runTime + "ms");
      if (stats.totalChecksSkipped < warmUpChecks) {
        if (!dryMode) {
          System.out.println("Warm-up, ignoring result...");
        }
        stats.totalChecksSkipped++;
      } else {
        stats.totalChecks++;
        stats.totalTime += runTime;
        stats.trackRequestBySize(text.length());
      }
    } catch (IOException e) {
      System.err.println("Got error from " + url + " (" + text.length() + " chars): "
              + e.getMessage() + ", text was (" + text.length() +  " chars): '" + StringUtils.abbreviate(text, 100) + "'");
      e.printStackTrace();
    }
  }

  @SuppressWarnings("UnusedReturnValue")
  private String checkAtUrlByPost(URL url, String postData, Map<String, String> properties) throws IOException {
    String keepAlive = System.getProperty("http.keepAlive");
    try {
      System.setProperty("http.keepAlive", "false");  // without this, there's an overhead of about 1 second - not sure why
      URLConnection connection = url.openConnection();
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
      connection.setDoOutput(true);
      try (Writer writer = new OutputStreamWriter(connection.getOutputStream(), UTF_8)) {
        writer.write(postData);
        writer.flush();
        return StringTools.streamToString(connection.getInputStream(), "UTF-8");
      }
    } finally {
      if (keepAlive != null) {
        System.setProperty("http.keepAlive", keepAlive);
      }
    }
  }

}
