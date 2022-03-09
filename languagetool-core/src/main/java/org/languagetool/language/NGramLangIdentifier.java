/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 LanguageTooler GmbH
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
package org.languagetool.language;

import org.languagetool.noop.NoopLanguage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static java.lang.StrictMath.*;
import static org.languagetool.language.LanguageIdentifier.canLanguageBeDetected;

public class NGramLangIdentifier {

  private final static double EPSILON = 1e-4;

  private final Map<String, Integer> vocab;
  private final List<String[]> codes; // Elem format = {Name, 2-code (or "NULL"), 3-code}

  private final List<Map<String, Double>> knpBigramProbs;
  private final int thresholdsStart;
  private final List<double[]> thresholds;

  private final int maxLength;
  private final ZipFile zipFile;

  public NGramLangIdentifier(File sourceModelZip, int maxLength) throws IOException {
    this.maxLength = maxLength;
    this.zipFile = new ZipFile(sourceModelZip);

    //Load language codes - Line format = {Language Name}\t{2-code or "NULL"}\t{3-code}
    codes = new ArrayList<>();
    try (BufferedReader br = getReader("iso_codes.tsv")) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split("\t");
        if (values[3].equals("1")) {
          codes.add(values);
        }
      }
    }

    //Load vocab - Line format = {token}
    vocab = new HashMap<>();
    try (BufferedReader br = getReader("vocab.txt")) {
      String line;
      int i = 0;
      while ((line = br.readLine()) != null) {
        vocab.put(line.split("\t")[0].trim(), i);
        i++;
      }
    }

    //Load thresholds
    thresholds = new ArrayList<>();
    try (BufferedReader br = getReader("thresholds.txt")) {
      String line;
      thresholdsStart = Integer.parseInt(br.readLine());
      while ((line = br.readLine()) != null) {
        double[] vals = Arrays.stream(line.split(" ")).mapToDouble(Double::parseDouble).toArray();
        thresholds.add(vals);
      }
      //assert (thresholds.size() == maxLength - thresholdsStart) : "Thresholds file is incomplete: " + thresholds.size() + " != " + maxLength + "-" + thresholdsStart;
    }

    //Load transition matrices - Line format = {i} {j} {val}
    knpBigramProbs = expectedFiles().stream().map(this::readLines).parallel().map(NGramLangIdentifier::loadDict).collect(Collectors.toList());
  }

  public Map<String, Double> detectLanguages(String text, List<String> additionalLanguageCodes) {
    List<Integer> enc = encode(text);
    List<Double> finalProbs = new ArrayList<>();
    List<int[]> keys = keys(enc);

    for (int i = 0; i < codes.size(); i++) {
      double val = 0;
      for (int[] key: keys) {
        double prob = knpBigramProbs.get(i).getOrDefault(key[0] + "_" + key[1], EPSILON);
        val += log(prob);
      }
      finalProbs.add(val);
    }

    Map<String, Double> result = new HashMap<>();

    if (text.length() >= this.thresholdsStart) {
      int argMax = 0;
      for (int i = 1; i < finalProbs.size(); i++) {
        if (finalProbs.get(i) > finalProbs.get(argMax)) {
          argMax = i;
        }
      }
      int thresholdIndex = min(text.length(), maxLength) - this.thresholdsStart;
      if (finalProbs.get(argMax) < thresholds.get(thresholdIndex)[argMax]) {
        result.put(NoopLanguage.SHORT_CODE, 100.0);
        return result;
      }
    }

    finalProbs = finalProbs.stream().map(StrictMath::exp).collect(Collectors.toList());
    finalProbs = normalize(finalProbs);
    for (int i = 0; i < codes.size(); i++) {
      String langCode = codes.get(i)[1].equals("NULL") ? codes.get(i)[2] : codes.get(i)[1]; //2-character code if possible
      if (canLanguageBeDetected(langCode, additionalLanguageCodes)) {
        result.put(langCode, finalProbs.get(i));
      }
    }

    return result;
  }

  private BufferedReader getReader(String fileName) throws IOException {
    InputStream is = this.zipFile.getInputStream(this.zipFile.getEntry(fileName));
    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
    return new BufferedReader(isr);
  }

  private List<String> readLines(String path) {
    ArrayList<String> result = new ArrayList<>();
    try (BufferedReader br = getReader(path)) {
      String line;
      while ((line = br.readLine()) != null) {
        result.add(line);
      }
    } catch(java.io.IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private static Map<String, Double> loadDict(List<String> lines)  {
    Map<String, Double> tm = new HashMap<>();
    for (String line : lines) {
      String[] parts = line.trim().split(" ");
      String key = String.join("_", Arrays.copyOfRange(parts, 0, parts.length-1));
      tm.put(key, Double.parseDouble(parts[parts.length-1]));
    }
    return tm;
  }

  private List<String> expectedFiles() {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < codes.size(); i++) {
      String name = String.format("%02d.txt", i);
      result.add(name);
    }
    return result;
  }

  private List<Integer> encode(String text) {
    List<Integer> result = new ArrayList<>();
    result.add(1); //Start of sentence token
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength);
    }
    text = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase();
    text = text.replaceAll("\\d+", "<NUM>");
    text = text.replaceAll("[\\uac00-\\ud7a3]", "<KO>"); // Korean
    text = text.replaceAll("[\\u3040-\\u30ff]", "<JA>"); // Japanese
    text = text.replaceAll("[\\u4e00-\\u9FFF]", "<ZH>"); // Chinese
    text = text.replaceAll("[\\u1780-\\u17FF]", "<KM>"); // Khmer
    text = text.replaceAll("[\\u1700-\\u171F]", "<TL>"); // Tagalog
    text = text.replaceAll("[\\u0530-\\u058F]", "<HY>"); // Armenian
    text = text.replaceAll("[\\u0370-\\u03FF]", "<EL>"); // Greek
    text = text.replaceAll("[\\u0B80-\\u0BFF]", "<TA>"); // Tamil
    text = text.replaceAll("\\s+", "▁");
    if (text.length() == 0) {
      return result;
    }
    text = "▁" + text;

    int cur = 0;
    while (cur < text.length()) {
      int tok = 0;
      int ci = 1;
      for (int i = cur + 1; i <= text.length(); i++) {
        int maybeTok = vocab.getOrDefault(text.substring(cur, i), -1);
        if (maybeTok > -1) {
          tok = maybeTok;
          ci = i - cur;
        }
      }
      cur += ci;
      result.add(tok);
    }
    return result;
  }

  private List<int[]> keys(List<Integer> enc) {
    //For now just bigrams
    List<int[]> result = new ArrayList<>();
    for (int i = 1; i < enc.size(); i++) {
      result.add(new int[]{enc.get(i-1), enc.get(i)});
    }
    return result;
  }

  private List<Double> normalize(List<Double> vals) {
    double tot = vals.stream().mapToDouble(f -> f).sum();
    return vals.stream().map(n -> n/tot).collect(Collectors.toList());
  }
}
