/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.language.German;
import org.languagetool.tools.StringTools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

/**
 * Test HTTP server access from multiple threads with multiple languages.
 * Unlike HTTPServerMultiLangLoadTest, this always sends the same text 
 * but actually checks results (compares multi-thread results to non-multi-thread).
 */
@Ignore("for interactive use; requires local Tatoeba data")
public class HTTPServerMultiLangLoadTest2 extends HTTPServerMultiLangLoadTest {

  private static final String DATA_PATH = "/media/Data/tatoeba/";
  private static final int MIN_TEXT_LENGTH = 500;
  private static final int MAX_TEXT_LENGTH = 1_000;
  private static final int MAX_SLEEP_MILLIS = 10;

  private final Map<Language, String> textToResult = new HashMap<>();

  @Test
  @Override
  public void testHTTPServer() throws Exception {
    File dir = new File(DATA_PATH);
    List<Language> languages = new ArrayList<>();
    //languages.add(new German());
    languages.addAll(Languages.get());
    for (Language language : languages) {
      File file = new File(dir, "tatoeba-" + language.getShortCode() + ".txt");
      if (!file.exists()) {
        System.err.println("No data found for " + language + ", language will not be tested");
      } else {
        String content = StringTools.readerToString(new FileReader(file));
        int fromPos = random.nextInt(content.length());
        int toPos = fromPos + random.nextInt(MAX_TEXT_LENGTH) + MIN_TEXT_LENGTH;
        String textSubstring = content.substring(fromPos, Math.min(toPos, content.length()));
        langCodeToText.put(language, textSubstring);
        String response = checkByPOST(language, textSubstring);
        textToResult.put(language, response);
        System.err.println("Using " + content.length() + " bytes of data for " + language);
      }
    }
    if (langCodeToText.isEmpty()) {
      throw new RuntimeException("No input data found in " + dir);
    }
    System.out.println("Testing " + langCodeToText.keySet().size() + " languages and variants");
    //super.testHTTPServer();  // start server in this JVM
    super.doTest();  // assume server has been started manually in its own JVM
  }

  @Override
  void runTestsV2() throws IOException, SAXException, ParserConfigurationException {
    Language language = getRandomLanguage();
    String text = langCodeToText.get(language);
    long sleepTime = random.nextInt(MAX_SLEEP_MILLIS);
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    long startTime = System.currentTimeMillis();
    counter.incrementAndGet();
    String realResult = checkByPOST(language, text);
    String expectedResult = textToResult.get(language);
    if (!realResult.equals(expectedResult)) {
      fail("Real result != expected result for " + language + ", input: " + text + "\n" +
           "Real result: " + realResult + "\n" +
           "Exp. result: " + expectedResult
      );
    }
    System.out.println(counter.get() + ". Sleep: " + sleepTime + "ms, Lang: " + language.getShortCodeWithCountryAndVariant()
            + ", Length: " + text.length() + ", Time: " + (System.currentTimeMillis()-startTime) + "ms");
  }
  
}
