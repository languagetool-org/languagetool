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
import java.util.stream.Collectors;

/**
 * Evaluate the quality of our language detection - looking for shortest strings still being detected correctly.
 * @since 4.4
 */
class LanguageDetectionMinLengthEval {

  private final LanguageIdentifier languageIdentifier;

  private static final int MIN_INPUT_LEN = 5;
  private static final int MAX_INPUT_LEN = 30;

  private int totalInputs = 0;
  private int totalFailures = 0;

  private LanguageDetectionMinLengthEval() {
    languageIdentifier = new LanguageIdentifier();
    languageIdentifier.enableNgrams(new File("/home/languagetool/model_ml50_new.zip"));
    //languageIdentifier = new CLD2Identifier();
    //languageIdentifier.enableFasttext(new File("/path/to/fasttext/binary"), new File("/path/to/fasttext/model"));
    // Daniel's paths:
    //languageIdentifier.enableFasttext(new File("/home/languagetool/fasttext/fasttext"), new File("/home/languagetool/fasttext/lid.176.bin"));
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
      int linesConsidered = 0;
      List<String> list = getLines(stream);
      for (String line : list) {
        if (line.trim().isEmpty()) {
          System.err.println("Skipping empty input for " + language.getShortCode());
          continue;
        }
        if (line.trim().length() < MIN_INPUT_LEN) {
          System.err.println("Skipping short input for " + language.getShortCode() + ": " + line);
          continue;
        }
        try {
          int minChar = getShortestCorrectDetection(line, language);
          if (minChar == -1) {
            System.err.println("Skipping line, could not find minimum text length for '" + line + "'");
            continue;
          }
          linesConsidered++;
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
      float avgMinChars = (float) minChars / linesConsidered;
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
    int textLength = -1;
    boolean stillOkay = true;
    for (int i = Math.min(line.length(), MAX_INPUT_LEN); i > MIN_INPUT_LEN; i--) {
      String text = line.substring(0, i);
      if (stillOkay) {
        textLength = text.length();
      }
      DetectedLanguage detectedLangObj = languageIdentifier.detectLanguage(text, Collections.emptyList(), Collections.emptyList());
      //System.out.println("INPUT: " + text + " - " + text.length() + " - " + detectedLangObj);
      String detectedLang = null;
      if (detectedLangObj != null) {
        detectedLang = detectedLangObj.getDetectedLanguage().getShortCode();
      }
      if (detectedLang == null && i == line.length()) {
        throw new DetectionException("Detection failed for '" + line + "', detected <null>");
      } else if (detectedLang == null) {
        //System.out.println("minLen: " + textLength);
        //System.out.println("TEXT     : " + line);
        //System.out.println("TOO SHORT : " + text + " => " + detectedLang + " (" + textLength + ")");
        stillOkay = false;
      } else if (!expectedLanguage.getShortCode().equals(detectedLang)){
        //System.out.printf(Locale.ENGLISH, "WRONG: Expected %s, but got %s -> %s (%.2f)%n", expectedLanguage.getShortCode(), detectedLang, text, detectedLangObj.getDetectionConfidence());
        stillOkay = false;
      } else {
        //System.out.println("STILL OKAY: " + text + " => " + detectedLang);
      }
    }
    //System.out.println("textLen: " + (textLength+1));
    return textLength + 1;
  }

  private List<String> getLines(InputStream stream) throws IOException {
    List<String> lines = CharStreams.readLines(new InputStreamReader(stream));
    return lines.stream().filter(k -> !k.startsWith("#")).collect(Collectors.toList());
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Input length: " + MIN_INPUT_LEN + " to " + MAX_INPUT_LEN + " characters");
    LanguageDetectionMinLengthEval eval = new LanguageDetectionMinLengthEval();
    long startTime = System.currentTimeMillis();
    float minCharsTotal = 0;
    int languageCount = 0;
    List<Language> languages = new ArrayList<>();
    languages.addAll(Languages.get());
    //languages.add(new DynamicMorfologikLanguage("Norwegian (Bokmal)", "nb", new File("/home/dnaber/lt/dynamic-languages/no/nb_NO.dict")));
    //languages.add(new DynamicMorfologikLanguage("Norwegian (Nynorsk)", "nn", new File("/home/dnaber/lt/dynamic-languages/no/nn_NO.dict")));
    for (Language language : languages) {
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

  static class DetectionException extends RuntimeException {
    DetectionException(String s) {
      super(s);
    }
  }
}
