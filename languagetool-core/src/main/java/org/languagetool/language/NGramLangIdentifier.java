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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StrictMath.*;
import static org.languagetool.language.LanguageIdentifier.canLanguageBeDetected;

public class NGramLangIdentifier {

  private final static double EPSILON = 1e-8;

  private final Map<String, Integer> vocab;
  private final List<String[]> codes; // Elem format = {Name, 2-code (or "NULL"), 3-code}

  private final List<Map<String, Integer>> bigramCounts;
  private final List<Map<String, Integer>> unigramCounts;
  private final List<Map<String, Integer>> bigramSumsPre;
  private final List<Map<String, Integer>> bigramSumsPost;

  private final List<List<Double>> scales;

  private final int maxLength;
  private final boolean knp;
  private final boolean scaling;

  public NGramLangIdentifier(String sourceFolder, int maxLength, boolean knSmoothing, boolean scaling) throws IOException {
    this.maxLength = maxLength;
    this.knp = knSmoothing;
    this.scaling = scaling;
    String vocabPath = Paths.get(sourceFolder, "vocab.txt").toString();
    String isoPath = Paths.get(sourceFolder, "iso_codes.tsv").toString();
    String ugPath = Paths.get(sourceFolder, "/ug/").toString();
    String sumsPathPre = Paths.get(sourceFolder, "/sums/pre/").toString();
    String sumsPathPost = Paths.get(sourceFolder, "/sums/post/").toString();
    String scalesPath = Paths.get(sourceFolder, "scales.txt").toString();

    //Load language codes - Line format = {Language Name}\t{2-code or "NULL"}\t{3-code}
    this.codes = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(isoPath))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split("\t");
        this.codes.add(values);
      }
    }

    //Load vocab - Line format = {token}
    this.vocab = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(vocabPath))) {
      String line;
      int i = 0;
      while ((line = br.readLine()) != null) {
        this.vocab.put(line.trim(), i);
        i++;
      }
    }

    //Load transition matrices - Line format = {i} {j} {val}
    this.bigramCounts = new ArrayList<>();
    for (String path : this.expectedFiles(sourceFolder)) {
      this.bigramCounts.add(loadDict(path));
    }

    this.unigramCounts = new ArrayList<>();
    for (String path : this.expectedFiles(ugPath)) {
      this.unigramCounts.add(loadDict(path));
    }

    //Load sums - Line format = {i} {val}
    this.bigramSumsPre = new ArrayList<>();
    for (String path : this.expectedFiles(sumsPathPre)) {
      this.bigramSumsPre.add(loadDict(path));
    }

    this.bigramSumsPost = new ArrayList<>();
    for (String path : this.expectedFiles(sumsPathPost)) {
      this.bigramSumsPost.add(loadDict(path));
    }

    if (scaling) {
      //Load scales - Line format = {val} {val} ... {val}
      this.scales = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(scalesPath))) {
        String line;
        while ((line = br.readLine()) != null) {
          String[] parts = line.trim().split(" ");
          this.scales.add(Arrays.stream(parts).map(Double::parseDouble).collect(Collectors.toList()));
        }
      }
    } else {
      this.scales = null;
    }
  }

  public Map<String, Double> detectLanguages(String text, List<String> additionalLanguageCodes) {
    List<Integer> enc = this.encode(text);
    List<Double> vals = new ArrayList<>();

    for (int i = 0; i < this.codes.size(); i++) {
      double val = 0;
      for (int[] key: this.keys(enc)) {
        double prob;
        if (this.knp) {
          prob = knp(key[0], key[1], i);
        } else {
          int ugCnt = this.unigramCounts.get(i).getOrDefault("0_" + key[0], 0);
          if (ugCnt == 0) {
            prob = EPSILON;
          } else {
            prob = (double) (this.bigramCounts.get(i).getOrDefault(key[0] + "_" + key[1], 1)) / ugCnt;
          }
        }
        val += log(max(prob, EPSILON));
      }
      vals.add(exp(val));
    }

    if (this.scaling) {
      List<Double> l1normed = vals;
      vals = new ArrayList<>();
      for (int i = 0; i < l1normed.size(); i++) {
        double val = 0;
        for (double d : this.scales.get(i)) {
          val += d * l1normed.get(i);
        }
        vals.add(val);
      }
    }

    vals = this.normalize(vals);

    Map<String, Double> result = new HashMap<>();
    for (int i = 0; i < this.codes.size(); i++) {
      String langCode = this.codes.get(i)[1].equals("NULL") ? this.codes.get(i)[2] : this.codes.get(i)[1]; //2-character code if possible
      if (canLanguageBeDetected(langCode, additionalLanguageCodes)) {
        result.put(langCode, vals.get(i));
      }
    }

    //System.out.println("ngram result: " + result);
    return result;
  }

  private static Map<String, Integer> loadDict(String path) throws IOException {
    Map<String, Integer> tm = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(path));
    String line;
    while ((line = br.readLine()) != null) {
      String[] parts = line.trim().split(" ");
      String key = String.join("_", Arrays.copyOfRange(parts, 0, parts.length-1));
      tm.put(key, Integer.parseInt(parts[parts.length-1]));
    }
    return tm;
  }

  private List<String> expectedFiles(String folderPath) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < this.codes.size(); i++) {
      String name = String.format("%02d.txt", i);
      String fp = Paths.get(folderPath, name).toString();
      result.add(fp);
    }
    return result;
  }

  private List<Integer> encode(String text) {
    List<Integer> result = new ArrayList<>();
    result.add(1); //Start of sentence token
    if (text.length() > this.maxLength) {
      text = text.substring(0, this.maxLength);
    }
    text = Normalizer.normalize(text, Normalizer.Form.NFKC).replaceAll("^\\s+","").replaceAll("\\s+", "▁").toLowerCase();
    if (text.length() == 0) {
      return result;
    }
    text = "▁" + text;

    int cur = 0;
    while (cur < text.length()) {
      int tok = 0;
      int ci = 1;
      for (int i = cur + 1; i <= text.length(); i++) {
        int maybeTok = this.vocab.getOrDefault(text.substring(cur, i), -1);
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

  private Double knp(int a, int b, int tmI) {
    Map<String, Integer> tm = this.bigramCounts.get(tmI);
    Map<String, Integer> tmU = this.unigramCounts.get(tmI);
    Map<String, Integer> tmS = this.bigramSumsPre.get(tmI);
    Map<String, Integer> tmSd = this.bigramSumsPost.get(tmI);

    int unigramCnt = tmU.getOrDefault("0_" + a, 0);

    if (unigramCnt == 0) {
      return EPSILON;
    }

    int xaCnt = tmS.getOrDefault("" + b, 0);
    int axCnt = tmSd.getOrDefault("" + a, 0);
    int bigramsTotal = tm.size();
    int bigramCnt = tm.getOrDefault(a + "_" + b, 0);

    double d = 0.75;
    double bigramProbNormalized = Double.max(bigramCnt - d, 0) / unigramCnt;
    double pCont = (double) xaCnt / bigramsTotal;
    double lamb = (d * axCnt) / unigramCnt;
    return bigramProbNormalized + (lamb * pCont);
  }

  private List<Double> normalize(List<Double> vals) {
    double tot = vals.stream().mapToDouble(f -> f).sum();
    return vals.stream().map(n -> n/tot).collect(Collectors.toList());
  }
}
