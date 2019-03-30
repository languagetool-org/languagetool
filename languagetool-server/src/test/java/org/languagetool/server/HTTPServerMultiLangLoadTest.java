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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tools.StringTools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test HTTP server access from multiple threads with multiple languages.
 * Sends random texts but doesn't really check the results.
 */
@Ignore("for interactive use; requires local Tatoeba data")
public class HTTPServerMultiLangLoadTest extends HTTPServerLoadTest {

  private static final String DATA_PATH = "/home/fabian/Downloads/tatoeba";
  static final int MIN_TEXT_LENGTH = 10;
  static final int MAX_TEXT_LENGTH = 5_000;
  static final int MAX_SLEEP_MILLIS = 1;

  long totalTimes = 0;
  long totalChars = 0;
  
  final Map<Language, String> langCodeToText = new HashMap<>();
  final Random random = new Random(1234);
  final AtomicInteger counter = new AtomicInteger();


  @Test
  @Override
  public void testHTTPServer() throws Exception {
    File dir = new File(DATA_PATH);
    List<Language> languages = new ArrayList<>();
    languages.addAll(Languages.get());
    for (Language language : languages) {
      File file = new File(dir, "tatoeba-" + language.getShortCode() + ".txt");
      if (!file.exists()) {
        System.err.println("No data found for " + language + ", language will not be tested");
      } else {
        String content = StringTools.readerToString(new FileReader(file));
        langCodeToText.put(language, content);
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
  protected int getThreadCount() {
    return 4;
  }

  @Override
  protected int getRepeatCount() {
    return Integer.MAX_VALUE;
  }

  @Override
  void runTestsV2() throws IOException, SAXException, ParserConfigurationException {
    Language language = getRandomLanguage();
    String text = langCodeToText.get(language);
    int fromPos = random.nextInt(text.length());
    int toPos = fromPos + random.nextInt(MAX_TEXT_LENGTH-MIN_TEXT_LENGTH) + MIN_TEXT_LENGTH;
    String textSubstring = text.substring(fromPos, Math.min(toPos, text.length()));
    long sleepTime = random.nextInt(MAX_SLEEP_MILLIS);
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    long startTime = System.currentTimeMillis();
    counter.incrementAndGet();

    checkByPOST(language, textSubstring);
    long runtime = System.currentTimeMillis() - startTime;
    totalTimes += runtime;
    totalChars += textSubstring.length();
    float millisPer100Chars = (float)runtime / (float)textSubstring.length() * 100.0f;
    float avgMillisPer100Chars = (float)totalTimes / (float)totalChars * 100.0f;
    System.out.println(counter.get() + ". Sleep: " + sleepTime + "ms"
            + ", Lang: " + padLeft(language.getShortCodeWithCountryAndVariant(), 5)
            + ", Length: " + padLeft(textSubstring.length() + "", 5) + ", Time: " + padLeft(runtime + "", 6) + "ms"
            + ", per100Chars: " + padLeft((int)millisPer100Chars + "", 4) + "ms" 
            + ", per100Chars on avg.: " + padLeft((int)avgMillisPer100Chars + "", 3) + "ms");
  }

  static String padLeft(String s, int n) {
    return String.format("%1$" + n + "s", s);
  }

  Language getRandomLanguage() {
    int randomNumber = random.nextInt(langCodeToText.size());
    int i = 0;
    for (Language lang : langCodeToText.keySet()) {
      if (i++ == randomNumber) {
        return lang;
      }
    }
    throw new RuntimeException("Could not find a random language (" + i + ")");
  }

}
