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
package org.languagetool.dev.eval;

import com.google.common.io.CharStreams;
import org.languagetool.DetectedLanguage;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.LanguageIdentifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Evaluate the quality of our language detection - looking for shortest strings still being detected correctly.
 * @since 4.4
 */
class LanguageDetectionMinLengthEval {

  private final LanguageIdentifier languageIdentifier;

  private int totalInputs = 0;
  private int totalFailures = 0;

  private LanguageDetectionMinLengthEval() {
    languageIdentifier = new LanguageIdentifier();
    //languageIdentifier = new CLD2Identifier();
    //languageIdentifier.enableFasttext(new File("/path/to/fasttext/binary"), new File("/path/to/fasttext/model"));
    // Daniel's paths:
    //languageIdentifier.enableFasttext(new File("/prg/fastText-0.1.0/fasttext"), new File("/prg/fastText-0.1.0/data/lid.176.bin"));
  }

  private float evaluate(Language language) throws IOException {
    String evalTextFile = "/org/languagetool/dev/eval/lang/" + language.getShortCode() + ".txt";
    InputStream stream = LanguageDetectionMinLengthEval.class.getResourceAsStream(evalTextFile);
    System.out.println("=== " + language + " ===");
    if (stream == null) {
      throw new RuntimeException("No eval data found for " + language);
    } else {
      int minCharsMax = Integer.MIN_VALUE;
      String maxText = null;
      int minChars = 0;
      int failures = 0;
      List<String> list = getLines(stream);
      for (String line : list) {
        try {
          int minChar = getShortestCorrectDetection(line, language);
          minChars += minChar;
          if (minChar > minCharsMax) {
            minCharsMax = minChar;
            maxText = line.substring(0, Math.min(line.length(), minChar));
          }
        } catch (DetectionException e) {
          //System.out.println("FAIL: " + e.getMessage());
          failures++;
        }
      }
      float avgMinChars = (float) minChars / list.size();
      System.out.printf(Locale.ENGLISH, "Average minimum size still correctly detected: %.2f, max: %d ('%s')\n", avgMinChars, minCharsMax, maxText);
      if (failures > 0) {
        System.out.println("Detection failures: " + failures + " of " + list.size());
      }
      totalFailures += failures;
      return avgMinChars;
    }
  }

  private int getShortestCorrectDetection(String line, Language expectedLanguage) {
    totalInputs++;
    int textLength = 1;
    for (int i = line.length(); i > 0; i--) {
      String text = line.substring(0, i);
      DetectedLanguage detectedLangObj = languageIdentifier.detectLanguage(text, Collections.emptyList(), Collections.emptyList());
      String detectedLang = null;
      if (detectedLangObj != null) {
        detectedLang = detectedLangObj.getDetectedLanguage().getShortCode();
      }
      if (detectedLang == null && i == line.length()) {
        throw new DetectionException("Detection failed for '" + line + "', detected <null>");
      } else if (detectedLang == null) {
        if (textLength == 1) {
          textLength = i + 1;
        }
        //System.out.println("minLen: " + textLength);
        //System.out.println("TEXT     : " + line);
        //System.out.println("TOO SHORT : " + text + " => " + detectedLang + " (" + textLength + ")");
      } else if (!expectedLanguage.getShortCode().equals(detectedLang)){
        //System.out.printf(Locale.ENGLISH, "WRONG: Expected %s, but got %s -> %s (%.2f)%n", expectedLanguage.getShortCode(), detectedLang, text, detectedLangObj.getDetectionConfidence());
        if (textLength == 1) {
          textLength = i + 1;
        }
      } else {
        //System.out.println("STILL OKAY: " + text + " => " + detectedLang);
      }
    }
    return textLength;
  }

  private List<String> getLines(InputStream stream) throws IOException {
    List<String> lines = CharStreams.readLines(new InputStreamReader(stream));
    List<String> result = new ArrayList<>();
    for (String line : lines) {
      if (!line.startsWith("#")) {
        result.add(line);
      }
    }
    return result;
  }

  public static void main(String[] args) throws IOException {
    LanguageDetectionMinLengthEval eval = new LanguageDetectionMinLengthEval();
    long startTime = System.currentTimeMillis();
    float minCharsTotal = 0;
    int languageCount = 0;
    for (Language language : Languages.get()) {
      //if ((language.getShortCode().equals("ja"))) { continue; }
      if (language.isVariant()) {
        continue;
      }
      minCharsTotal += eval.evaluate(language);
      languageCount++;
    }
    long endTime = System.currentTimeMillis();
    System.out.println();
    long totalTime = endTime - startTime;
    float timePerInput = (float)totalTime / eval.totalInputs;
    System.out.printf(Locale.ENGLISH, "Time: " + totalTime + "ms = %.2fms per input\n", timePerInput);
    System.out.println("Total detection failures: " + eval.totalFailures + "/" + eval.totalInputs);
    float avgMinChars = minCharsTotal / languageCount;
    System.out.printf(Locale.ENGLISH, "Avg. minimum chars: %.3f\n", avgMinChars);
  }

  class DetectionException extends RuntimeException {
    DetectionException(String s) {
      super(s);
    }
  }
}
