/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

/**
 * Load testing the HTTPS server. Start the server yourself.
 */
public class HTTPSServerTesting {

  private static final String SERVER_URL = "https://localhost:8081";
  //private static final String SERVER_URL = "https://languagetool.org:8081";
  private static final int REPEAT_COUNT = 100;
  private static final int THREAD_COUNT = 3;

  private final ExampleSentenceProvider provider = new ExampleSentenceProvider(1, 500);
  private final Random rnd = new Random(10);
  private int checkCount = 0;

  @Ignore("For interactive testing, thus ignored for unit tests")
  @Test
  public void interactiveHTTPServerTest() throws Exception {
    HTTPTestTools.disableCertChecks();
    long startTime = System.currentTimeMillis();
    try {
      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
      List<Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < THREAD_COUNT; i++) {
        Future<?> future = executorService.submit(new TestRunnable(i));
        futures.add(future);
      }
      for (Future<?> future : futures) {
        future.get();
      }
    } finally {
      long runtime = System.currentTimeMillis() - startTime;
      System.out.println("Running with " + THREAD_COUNT + " threads in " + runtime + "ms for " + checkCount + " checks");
      if (checkCount > 0) {
        long timePerCheck = runtime / checkCount;
        System.out.println(" => on average " + timePerCheck + "ms per check");
      }
    }
  }

  private class TestRunnable implements Runnable {
    private final int threadNumber;

    TestRunnable(int threadNumber) {
      this.threadNumber = threadNumber;
    }

    @Override
    public void run() {
      try {
        for (int i = 0; i < REPEAT_COUNT; i++) {
          runTests(threadNumber);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void runTests(int threadNumber) throws IOException {
    List<Language> languages = Languages.get();
    Language lang = languages.get(rnd.nextInt(languages.size()));
    List<ExampleSentence> sentences = provider.getRandomSentences(lang);
    String text = getSentencesAsText(sentences);
    String data = "language=" + lang.getShortCodeWithCountryAndVariant() + "&text=" + URLEncoder.encode(text, "utf-8");
    String resultXml = checkAtUrl(new URL(SERVER_URL), data, threadNumber);
    for (ExampleSentence sentence : sentences) {
      assertTrue("Expected " + sentence.getRuleId() + " for '" + text + "' (" + sentences.size() + " sentences)", resultXml.contains(sentence.getRuleId()));
    }
  }

  private static String getSentencesAsText(List<ExampleSentence> sentences) {
    StringBuilder sb = new StringBuilder();
    for (ExampleSentence sentence : sentences) {
      String sentenceStr = org.languagetool.rules.ExampleSentence.cleanMarkersInExample(sentence.getSentence());
      String cleanSentenceStr = sentenceStr.replaceAll("[\\n\\t]+", "");
      sb.append(cleanSentenceStr);
      sb.append("\n\n");
    }
    return sb.toString();
  }

  private String checkAtUrl(URL url, String data, int threadNumber) throws IOException {
    long startTime = System.currentTimeMillis();
    String startOfData = data.substring(0, Math.min(30, data.length()));
    synchronized(this) {
      checkCount++;
    }
    String result = HTTPTestTools.checkAtUrlByPost(url, data);
    System.out.println(checkCount + ". [" + threadNumber + "] Got " + url + " with data (" + data.length() + " bytes) " + startOfData
            + "...: " + (System.currentTimeMillis() - startTime) + "ms");
    return result;
  }

}
