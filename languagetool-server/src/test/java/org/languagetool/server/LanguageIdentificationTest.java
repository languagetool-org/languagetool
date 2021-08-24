/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Simple interactive test to see how much of a string can be removed before language
 * identification starts to fail. Requires server to be manually started locally.
 */
public class LanguageIdentificationTest {

  @Test
  @Ignore("for interactive use, to find cases where e.g. ngram-based language detection makes a difference")
  public void testEverShorterText() throws IOException {
    String expected = "de-DE";
    List<String> lines = Files.readAllLines(Paths.get("/home/dnaber/lt/lang-detect-test.txt"));
    ObjectMapper mapper = new ObjectMapper();
    System.out.println("Loaded " + lines.size() + " lines");
    int lineCount = 0;
    for (String line : lines) {
      //System.out.println(line);
      lineCount++;
      for (int i = line.length(); i > 0; i--) {
        String shortened = line.substring(0, i);
        String json = check(shortened);
        Map map = mapper.readValue(json, Map.class);
        Map languageObj = (Map<String, String>) map.get("language");
        Map<String, String> detectedLanguageObj = (Map<String, String>) languageObj.get("detectedLanguage");
        if (!detectedLanguageObj.get("code").equals(expected)) {
          System.out.println(lineCount + ". " + detectedLanguageObj.get("code") + " " + shortened + " @" + shortened.length());
          break;
        }
      }
    }
  }

  private String check(String text) throws IOException {
    String urlOptions = "/v2/check?language=auto";
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8");
    URL url = new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + urlOptions);
    return HTTPTestTools.checkAtUrl(url);
  }

}
