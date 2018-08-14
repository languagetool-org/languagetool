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
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.LanguageIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluate the quality of our language detection.
 * @since 2.9
 */
class LanguageDetectionEval {

  private static final int MIN_CHARACTERS = 10;

  private final LanguageIdentifier languageIdentifier;

  private int totalInputs = 0;
  private int totalFailures = 0;
  private int misclassifications = 0;
  private int numClassifications = 0;

  private LanguageDetectionEval() {
    languageIdentifier = new LanguageIdentifier();
//    languageIdentifier.enableFasttext(new File("/path/to/fasttext/binary"), new File("/path/to/fasttext/model"));
  }

  private float evaluate(Language language) throws IOException {
//    String evalTextFile = "/org/languagetool/dev/eval/lang/" + language.getShortCode() + "_long.txt";
    String evalTextFile = "/org/languagetool/dev/eval/lang/" + language.getShortCode() + ".txt";
    InputStream stream = LanguageDetectionEval.class.getResourceAsStream(evalTextFile);
    System.out.println("=== " + language + " ===");
    if (stream == null) {
      throw new RuntimeException("No eval data found for " + language);
    } else {
      int minChars = 0;
      int failures = 0;
      float errors = 0;
      List<String> list = getLines(stream);
      for (String line : list) {
        try {
//          int minChar = getShortestCorrectDetection(line, language);
//          minChars += minChar;
          errors += getNumberOfWrongDetections(line, language, MIN_CHARACTERS);
        } catch (DetectionException e) {
          //System.out.println("FAIL: " + e.getMessage());
          failures++;
        }
      }
//      float avgMinChars = (float) minChars / list.size();
      float avgErrors = errors / list.size();
//      System.out.println("Average minimum size still correctly detected: " + avgMinChars);
//      System.out.println("Detection failures: " + failures + " of " + list.size());
      totalFailures += failures;
//      return avgMinChars;
      return avgErrors;
    }
  }

  private int getShortestCorrectDetection(String line, Language expectedLanguage) {
    totalInputs++;
    int textLength = 1;
    for (int i = line.length(); i > 0; i--) {
      String text = line.substring(0, i);
      Language detectedLangObj = languageIdentifier.detectLanguage(text);
      numClassifications++;
      String detectedLang = null;
      if (detectedLangObj != null) {
        detectedLang = detectedLangObj.getShortCode();
      }
      if (detectedLang == null && i == line.length()) {
        throw new DetectionException("Detection failed for '" + line + "', detected <null>");
      } else if (detectedLang == null) {
        textLength = i + 1;
        //System.out.println("minLen: " + textLength);
        //System.out.println("TEXT     : " + line);
        //System.out.println("TOO SHORT : " + text + " => " + detectedLang + " (" + textLength + ")");
      } else if (!expectedLanguage.getShortCode().equals(detectedLang)){
        misclassifications++;
        System.out.printf("WRONG: Expected %s, but got %s -> %s%n", expectedLanguage.getShortCode(), detectedLang, text);
      } else {
        //System.out.println("STILL OKAY: " + text + " => " + detectedLang);
      }
    }
    return textLength;
  }

  private float getNumberOfWrongDetections(String line, Language expectedLanguage, int threshold) {
    int errors = 0;
    int checks = 0;
    for (int i = threshold; i < line.length(); i++) {
      String text = line.substring(0, i);
      Language detectedLangObj = languageIdentifier.detectLanguage(text);
      checks++;
      totalInputs++;
      String detectedLang = null;
      if (detectedLangObj != null) {
        detectedLang = detectedLangObj.getShortCode();
      }
      if (detectedLang == null || !expectedLanguage.getShortCode().equals(detectedLang)) {
        //System.out.printf("detected %s, expected %s: %s%n", detectedLang, expectedLanguage.getShortCode(), text);
        errors++;
      }
    }
    return (float) errors / checks;
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
    LanguageDetectionEval eval = new LanguageDetectionEval();
    long startTime = System.currentTimeMillis();
    float minCharsTotal = 0;
    float errorsTotal = 0.0f;
    int languageCount = 0;
    for (Language language : Languages.get()) {
      //if (!(language.getShortCode().equals("de") || language.getShortCode().equals("en"))) {
      //  continue;
      //}
      if (language.isVariant()) {
        continue;
      }
      //minCharsTotal += eval.evaluate(language);
      float errors = eval.evaluate(language);
      System.out.printf("Average Errors: %.2f%%%n", errors * 100f);
      errorsTotal += errors;
      languageCount++;
    }
    long endTime = System.currentTimeMillis();
    System.out.println();
    long totalTime = endTime - startTime;
    float timePerInput = (float)totalTime / eval.totalInputs;
    System.out.printf("Time: " + totalTime + "ms = %.2fms per input\n", timePerInput);
    //System.out.println("Total detection failures: " + eval.totalFailures + "/" + eval.totalInputs);
    //float avgMinChars = (float) minCharsTotal / languageCount;
    float avgErrors =  errorsTotal / languageCount;
    //System.out.printf("Avg. minimum chars: %.5f\n", avgMinChars);
    System.out.printf("Total avg. errors: %.2f%%\n", avgErrors * 100f);
    //System.out.printf("Misclassifications: %d / %d (%.5f) \n", eval.misclassifications, eval.numClassifications, (float) eval.misclassifications / eval.numClassifications);
  }

  class DetectionException extends RuntimeException {
    DetectionException(String s) {
      super(s);
    }
  }
}
