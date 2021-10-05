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
 * Evaluate the quality of our language detection. Also see LanguageDetectionMinLengthEval.
 * @since 2.9
 */
class LanguageDetectionEval {

  private static final int MIN_CHARACTERS = 10;

  private final LanguageIdentifier languageIdentifier;

  private int totalInputs = 0;

  private LanguageDetectionEval() {
    languageIdentifier = new LanguageIdentifier();
    //languageIdentifier.enableFasttext(new File("/path/to/fasttext/binary"), new File("/path/to/fasttext/model"));
    // Daniel's paths:
    //languageIdentifier.enableFasttext(new File("/home/languagetool/fasttext/fasttext"), new File("/home/languagetool/fasttext/lid.176.bin"));
    //languageIdentifier.enableNgrams(new File("/home/languagetool/model_ml50_new.zip"));
  }

  private float evaluate(Language language) throws IOException {
//    String evalTextFile = "/org/languagetool/dev/eval/lang/" + language.getShortCode() + "_long.txt";
    String evalTextFile = "/org/languagetool/dev/eval/lang/" + language.getShortCode() + ".txt";
    InputStream stream = LanguageDetectionEval.class.getResourceAsStream(evalTextFile);
    System.out.println("=== " + language + " ===");
    if (stream == null) {
      throw new RuntimeException("No eval data found for " + language);
    } else {
      float errors = 0;
      List<String> list = getLines(stream);
      for (String line : list) {
        try {
          errors += getWrongDetectionRatio(line, language, MIN_CHARACTERS);
        } catch (DetectionException e) {
          //System.out.println("FAIL: " + e.getMessage());
        }
      }
      float avgErrors = errors / list.size();
      return avgErrors;
    }
  }

  private float getWrongDetectionRatio(String line, Language expectedLanguage, int threshold) {
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
    float errorsTotal = 0.0f;
    int languageCount = 0;
    for (Language language : Languages.get()) {
      //if (!(language.getShortCode().equals("de") || language.getShortCode().equals("en"))) { continue; }
      if (language.isVariant()) {
        continue;
      }
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
    float avgErrors =  errorsTotal / languageCount;
    System.out.printf("Total avg. errors: %.2f%%\n", avgErrors * 100f);
  }

  class DetectionException extends RuntimeException {
    DetectionException(String s) {
      super(s);
    }
  }
}
