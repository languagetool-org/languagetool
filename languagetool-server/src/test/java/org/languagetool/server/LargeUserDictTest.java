/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

/**
 * Test whether performance gets worse with many words in personal dictionary.
 */
public class LargeUserDictTest {

  private static final String USERNAME1 = "naber" + "@" + "danielnaber.de";
  private static final String API_KEY1 = "foo";
  private static final String USERNAME2 = "expired" + "@" + "danielnaber.de";
  private static final String API_KEY2 = "foo2";
  private static final int WARMUP_RUNS = 10;
  private static final int RUNS = 100;

  @Test
  @Ignore("requires real database and modifies it")
  public void testHTTPServer() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort());
    config.setDatabaseDriver("org.mariadb.jdbc.Driver");
    config.setDatabaseUrl("jdbc:mysql://localhost:3306/languagetoolpremium");
    config.setDatabaseUsername("root");
    config.setDatabasePassword("root");
    //config.setCacheSize(100);
    DatabaseAccess.init(config);
    HTTPServer server = new HTTPServer(config);
    try {
      server.run();
      Language lang = Languages.getLanguageForShortCode("fr");
      //Language lang = Languages.getLanguageForShortCode("de-DE");
      //Language lang = Languages.getLanguageForShortCode("en-US");
      warmup(lang);
      runPerformanceTest(lang, "This is test no {}.");
      addWords(1000, USERNAME1, API_KEY1);
      runPerformanceTest(lang, "This is test no {}.");
      addWords(1000, USERNAME2, API_KEY2);
      runPerformanceTest(lang, "This is test no {}.");
    } finally {
      server.stop();
    }
  }

  private void warmup(Language lang) throws Exception {
    runPerformanceTest(lang, "This is a test", WARMUP_RUNS);
  }
  
  private void runPerformanceTest(Language lang, String input) throws Exception {
    long time1 = System.currentTimeMillis();
    runPerformanceTest(lang, input, RUNS);
    long time2 = System.currentTimeMillis();
    long total = time2 - time1;
    System.out.println(RUNS + " runs in " + total + "ms, average = " + ((float)total/RUNS) + "ms");
  }

  private void runPerformanceTest(Language lang, String input, int runs) throws Exception {
    for (int i = 0; i < runs; i++) {
      check(lang, input, USERNAME1, API_KEY1);
    }
  }

  private void check(Language lang, String text, String username, String apiKey) throws IOException {
    String urlOptions = "?language=" + lang.getShortCodeWithCountryAndVariant();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8");
    urlOptions += "&username=" + URLEncoder.encode(username, "UTF-8");
    urlOptions += "&apikey=" + URLEncoder.encode(apiKey, "UTF-8");
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/check" + urlOptions);
    HTTPTools.checkAtUrl(url);
  }

  private void addWords(int numberOfWords, String username, String apiKey) throws IOException {
    System.out.println("Adding " + numberOfWords + " words...");
    for (int i = 0; i < numberOfWords; i++) {
      addWord("word" + i, username, apiKey);
    }
  }
  
  private void addWord(String word, String username, String apiKey) throws IOException {
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/words/add");
    HTTPTools.checkAtUrlByPost(url, "word=" + word + "&username=" + username + "&apikey=" + apiKey);
  }  
  
}
