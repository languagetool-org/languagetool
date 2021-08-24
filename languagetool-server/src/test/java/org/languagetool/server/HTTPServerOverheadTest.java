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
package org.languagetool.server;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Runs checks for all languages with the empty string, to 
 * measure overhead. Requires a HTTP service running on default port.
 */
public class HTTPServerOverheadTest {
  
  @Test
  @Ignore("for interactive use only")
  public void test() throws IOException {
    HashMap<Language, Float> results = new LinkedHashMap<>();
    List<Language> languages = Languages.get();
    //List<Language> languages = Collections.singletonList(Languages.getLanguageForShortCode("ca"));
    warmup(languages);
    runLoop(results, languages);
    System.out.println("=== Results of checking empty string, i.e. overhead per request: ===");
    for (Map.Entry<Language, Float> entry : results.entrySet()) {
      System.out.printf(Locale.ENGLISH, "%.2fms %s\n", entry.getValue(), entry.getKey());
    }
  }

  private void warmup(List<Language> languages) throws IOException {
    for (Language lang : languages) {
      System.out.println(lang + " (warm up)...");
      for (int i = 0; i < 10; i++) {
        checkTextOnServer(lang, "");
      }
    }
  }

  private void runLoop(HashMap<Language, Float> results, List<Language> languages) throws IOException {
    for (Language lang : languages) {
      System.out.println(lang + "...");
      int runs = 30;
      long times = 0;
      for (int i = 0; i < runs; i++) {
        long startTime = System.currentTimeMillis();
        checkTextOnServer(lang, "");
        times += System.currentTimeMillis() - startTime;
      }
      float averageTime = (float)times / (float)runs;
      results.put(lang, averageTime);
    }
  }

  private String checkTextOnServer(Language lang, String text) throws IOException {
    String postData = "language=" + lang.getShortCodeWithCountryAndVariant() + "&text=" + URLEncoder.encode(text, "UTF-8");
    URL url = new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + "/v2/check");
    return HTTPTestTools.checkAtUrlByPost(url, postData);
  }

}
