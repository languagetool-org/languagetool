/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.language.identifier.detector.FastTextDetector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.languagetool.Languages.getLanguageForShortCode;

/**
 * Evaluate the FastText confidence.
 */
class FastTextConfidenceEval {

  private static final boolean CORRECT_DETECTION_CONFIDENCE = true;
  private static final int MIN_INPUT_LEN = 5;
  private static final int MAX_INPUT_LEN = 50;
  private static final File MODEL_PATH = new File("/prg/fastText-0.1.0/data/lid.176.bin");
  private static final File BINARY_PATH = new File("/prg/fastText-0.1.0/fasttext");

  private final FastTextDetector ft;

  private FastTextConfidenceEval() throws IOException {
    ft = new FastTextDetector(MODEL_PATH, BINARY_PATH);
  }

  private void evaluate(Language language) throws IOException {
    String evalTextFile = "/org/languagetool/dev/eval/lang/" + language.getShortCode() + ".txt";
    InputStream stream = FastTextConfidenceEval.class.getResourceAsStream(evalTextFile);
    System.out.println("\n=== " + language + " ===");
    if (stream == null) {
      throw new RuntimeException("No eval data found for " + language);
    }
    List<String> list = getLines(stream);
    Map<Integer, Double> textLengthToConfidence = new HashMap<>();
    Map<Integer, Integer> textLengthToConfidenceCount = new HashMap<>();
    double failConfidenceSum = 0;  // confidence just when the input gets short enough so detection fails
    int failConfidenceCount = 0;  // confidence just when the input gets short enough so detection fails
    for (String line : list) {
      if (line.trim().isEmpty()) {
        System.err.println("Skipping empty input for " + language.getShortCode());
        continue;
      }
      if (line.trim().length() < MIN_INPUT_LEN) {
        System.err.println("Skipping short input for " + language.getShortCode() + ": " + line);
        continue;
      }
      boolean prevDetectionCorrect = false;
      String prevText = null;
      for (int i = Math.min(line.length(), MAX_INPUT_LEN); i > MIN_INPUT_LEN; i--) {
        String text = line.substring(0, i);
        Map<String, Double> map = ft.runFasttext(text, Collections.emptyList());
        double max = 0;
        String bestLang = null;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
          if (entry.getValue() > max) {
            max = entry.getValue();
            bestLang = entry.getKey();
          }
        }
        if (bestLang != null) {
          if (bestLang.equals(language.getShortCode())) {
            if (CORRECT_DETECTION_CONFIDENCE) {
              updateMaps(textLengthToConfidence, textLengthToConfidenceCount, text, max);
            }
            prevDetectionCorrect = true;
          } else {
            if (!CORRECT_DETECTION_CONFIDENCE) {
              updateMaps(textLengthToConfidence, textLengthToConfidenceCount, text, max);
            }
            if (prevDetectionCorrect && prevText != null) {
              //System.out.println("Detection of " + language + " not correct anymore at confidence " + max + " at " + text.length() + " chars");
              //System.out.println("OK  : " + prevText);
              //System.out.println("N.OK: " + text);
              failConfidenceSum += max;
              failConfidenceCount++;
            }
            prevDetectionCorrect = false;
          }
        }
        prevText = text;
      }
    }
    for (int i = MIN_INPUT_LEN + 1; i <= MAX_INPUT_LEN; i++) {
      if (i < 11 || i % 10 == 0) {
        if (textLengthToConfidence.get(i) == null || textLengthToConfidenceCount.get(i) == null) {
          System.out.printf("%d\t-\n", i);
        } else {
          System.out.printf(Locale.ENGLISH, "%d\t%.2f\t%d\n", i,
                  textLengthToConfidence.get(i)/textLengthToConfidenceCount.get(i), textLengthToConfidenceCount.get(i));
        }
      }
    }
    System.out.printf(Locale.ENGLISH, "Avg. confidence when just failing detection: %.2f\n", failConfidenceSum/failConfidenceCount);
  }

  private void updateMaps(Map<Integer, Double> textLengthToConfidence, Map<Integer, Integer> textLengthToConfidenceCount, String text, double max) {
    textLengthToConfidence.compute(text.length(), (k, v) -> v == null ? max : v + max);
    textLengthToConfidenceCount.compute(text.length(), (k, v) -> v == null ? 1 : v + 1);
  }

  private List<String> getLines(InputStream stream) throws IOException {
    List<String> lines = CharStreams.readLines(new InputStreamReader(stream));
    return lines.stream().filter(k -> !k.startsWith("#")).collect(Collectors.toList());
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Input length: " + MIN_INPUT_LEN + " to " + MAX_INPUT_LEN + " characters");
    System.out.println("Output format: charCount avgConfidence sampleSize");
    FastTextConfidenceEval eval = new FastTextConfidenceEval();
    //List<Language> languages = new ArrayList<>(Languages.get());
    List<Language> languages = Arrays.asList(
            getLanguageForShortCode("en"),
            getLanguageForShortCode("de"),
            getLanguageForShortCode("fr"),
            getLanguageForShortCode("es"),
            getLanguageForShortCode("nl")
    );
    for (Language language : languages) {
      //if (!(language.getShortCode().equals("de"))) { continue; }
      if (language.isVariant()) {
        continue;
      }
      eval.evaluate(language);
    }
  }

}
