/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */
package org.languagetool.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Ignore;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Ignore("for interactive use; requires local Tatoeba data")
public class HTTPServerLanguageIdentifierTest extends HTTPServerMultiLangLoadTest {

  private static final int TOTAL_REPEATS = 150;
  private final ObjectMapper mapper = new ObjectMapper();

  private final AtomicInteger numDetectionFailures = new AtomicInteger();

  @SuppressWarnings("unchecked")
  private String getDetectedLanguageCodeFromJSON(String json) throws IOException {
    Map map = mapper.readValue(json, Map.class);
    Map languageObj = (Map<String, String>) map.get("language");
    Map<String, String> detectedLanguageObj = (Map<String, String>) languageObj.get("detectedLanguage");
    return detectedLanguageObj.get("code");
  }
@Override
  protected int getRepeatCount() {
    return  TOTAL_REPEATS;
  }

  @After
  public void tearDown() throws Exception {
    System.out.printf("%d / %d detection failures%n", numDetectionFailures.get(), getRepeatCount() * getThreadCount());
  }

  @Override
  void runTestsV2() throws IOException {
    Language language = getRandomLanguage();
    String text = langCodeToText.get(language);
    int toPos = random.nextInt(MAX_TEXT_LENGTH-MIN_TEXT_LENGTH) + MIN_TEXT_LENGTH;
    String textSubstring = text.substring(0, Math.min(toPos, text.length()));
    long sleepTime = random.nextInt(MAX_SLEEP_MILLIS);
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    counter.incrementAndGet();

    String response = checkByPOST("auto", textSubstring);
    String detectedLang = getDetectedLanguageCodeFromJSON(response);
    String detectedLangShort = Languages.getLanguageForShortCode(detectedLang).getShortCode();
    boolean correctDetection = detectedLangShort.equals(language.getShortCode());
    if (!correctDetection) {
      System.out.printf("Expected %s / Detected %s -> %s%n", language.getShortCode(), detectedLangShort, textSubstring.replaceAll("\n", ""));
      synchronized (this) {
        numDetectionFailures.incrementAndGet();
      }
    }
  }
}
