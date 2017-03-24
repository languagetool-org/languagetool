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
import java.util.Arrays;
import java.util.List;

/**
 * Evaluate the quality of our language detection.
 *
 * @since 2.9
 */
class LanguageDetectionEval {

  private final LanguageIdentifier languageIdentifier;

  private int totalInputs = 0;
  private int totalFailures = 0;

  LanguageDetectionEval() {
    languageIdentifier = new LanguageIdentifier();
  }

  private void evaluate(Language language) throws IOException {
    if (language.isVariant()) {
      return;
    }
    String evalTextFile = "/org/languagetool/dev/eval/lang/" + language.getShortCode() + ".txt";
    InputStream stream = LanguageDetectionEval.class.getResourceAsStream(evalTextFile);
    System.out.println("=== " + language + " ===");
    if (stream == null) {
      throw new RuntimeException("No eval data found for " + language);
    } else {
      int minChars = 0;
      int failures = 0;
      List<String> list = getLines(stream);
      for (String line : list) {
        try {
          int minChar = getShortestCorrectDetection(line, language);
          minChars += minChar;
        } catch (DetectionException e) {
          //System.out.println("FAIL: " + e.getMessage());
          failures++;
        }
      }
      int avgMinChars = minChars / list.size();
      System.out.println("Average minimum size still correctly detected: " + avgMinChars);
      System.out.println("Detection failures: " + failures + " of " + list.size());
      totalFailures += failures;
    }
  }

  private int getShortestCorrectDetection(String line, Language expectedLanguage) {
    totalInputs++;
    String[] tokens = line.split("\\s+");
    for (int i = tokens.length; i > 0; i--) {
      String text = String.join(" ", Arrays.asList(tokens).subList(0, i));
      Language detectedLangObj = languageIdentifier.detectLanguage(text);
      String detectedLang = null;
      if (detectedLangObj != null) {
        detectedLang = detectedLangObj.getShortCode();
      }
      if (detectedLang == null && i == tokens.length) {
        throw new DetectionException("Detection failed for '" + line + "', detected <null>");
      } else if (detectedLang != null && !expectedLanguage.getShortCode().equals(detectedLang)) {
        if (i == tokens.length) {
          throw new DetectionException("Detection failed for '" + line + "', detected " + detectedLang);
        } else {
          int textLength = getTextLength(tokens, i + 1);
          //System.out.println("TEXT     : " + line);
          //System.out.println("TOO SHORT: " + text + " => " + detectedLang + " (" + textLength + ")");
          return textLength;
        }
      }
    }
    return tokens[0].length();
  }

  private int getTextLength(String[] tokens, int tokenPos) {
    int i = 0;
    int charCount = 0;
    for (String token : tokens) {
      if (i++ > tokenPos) {
        return charCount;
      }
      charCount += token.length();
    }
    return charCount;
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
    for (Language language : Languages.get()) {
      eval.evaluate(language);
    }
    long endTime = System.currentTimeMillis();
    System.out.println();
    System.out.println("Time: " + (endTime - startTime) + "ms");
    System.out.println("Total detection failures: " + eval.totalFailures + "/" + eval.totalInputs);
  }

  class DetectionException extends RuntimeException {
    DetectionException(String s) {
      super(s);
    }
  }
}
