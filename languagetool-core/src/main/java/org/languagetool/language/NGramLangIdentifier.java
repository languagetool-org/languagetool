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
import java.io.File;
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

  public NGramLangIdentifier(File sourceFolder, int maxLength, boolean knSmoothing, boolean scaling) throws IOException {
    this.maxLength = maxLength;
    this.knp = knSmoothing;
    this.scaling = scaling;
    String vocabPath = Paths.get(sourceFolder.getAbsolutePath(), "vocab.txt").toString();
    String isoPath = Paths.get(sourceFolder.getAbsolutePath(), "iso_codes.tsv").toString();
    File ugPath = new File(sourceFolder.getAbsolutePath(), "/ug/");
    File sumsPathPre = new File(sourceFolder.getAbsolutePath(), "/sums/pre/");
    File sumsPathPost = new File(sourceFolder.getAbsolutePath(), "/sums/post/");
    String scalesPath = Paths.get(sourceFolder.getAbsolutePath(), "scales.txt").toString();

    //Load language codes - Line format = {Language Name}\t{2-code or "NULL"}\t{3-code}
    codes = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(isoPath))) {
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
    try (BufferedReader br = new BufferedReader(new FileReader(vocabPath))) {
      String line;
      int i = 0;
      while ((line = br.readLine()) != null) {
        vocab.put(line.split("\t")[0].trim(), i);
        i++;
      }
    }

    //Load transition matrices - Line format = {i} {j} {val}
    bigramCounts = new ArrayList<>();
    for (String path : expectedFiles(sourceFolder)) {
      bigramCounts.add(loadDict(path));
    }

    unigramCounts = new ArrayList<>();
    for (String path : expectedFiles(ugPath)) {
      unigramCounts.add(loadDict(path));
    }

    //Load sums - Line format = {i} {val}
    bigramSumsPre = new ArrayList<>();
    for (String path : expectedFiles(sumsPathPre)) {
      bigramSumsPre.add(loadDict(path));
    }

    bigramSumsPost = new ArrayList<>();
    for (String path : expectedFiles(sumsPathPost)) {
      bigramSumsPost.add(loadDict(path));
    }

    if (scaling) {
      //Load scales - Line format = {val} {val} ... {val}
      scales = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(scalesPath))) {
        String line;
        while ((line = br.readLine()) != null) {
          String[] parts = line.trim().split(" ");
          scales.add(Arrays.stream(parts).map(Double::parseDouble).collect(Collectors.toList()));
        }
      }
    } else {
      scales = null;
    }
  }

  public Map<String, Double> detectLanguages(String text, List<String> additionalLanguageCodes) {
    List<Integer> enc = encode(text);
    List<Double> vals = new ArrayList<>();
    List<int[]> keys = keys(enc);

    for (int i = 0; i < codes.size(); i++) {
      double val = 0;
      for (int[] key: keys) {
        double prob;
        if (knp) {
          prob = knp(key[0], key[1], i);
        } else {
          int ugCnt = unigramCounts.get(i).getOrDefault("0_" + key[0], 0);
          if (ugCnt == 0) {
            prob = EPSILON;
          } else {
            prob = (double) (bigramCounts.get(i).getOrDefault(key[0] + "_" + key[1], 1)) / ugCnt;
          }
        }
        val += log(prob);
      }
      vals.add(exp(val));
    }

    if (scaling) {
      List<Double> l1normed = vals;
      vals = new ArrayList<>();
      for (int i = 0; i < l1normed.size(); i++) {
        double val = 0;
        for (double d : scales.get(i)) {
          val += d * l1normed.get(i);
        }
        vals.add(val);
      }
    }

    vals = normalize(vals);

    Map<String, Double> result = new HashMap<>();
    for (int i = 0; i < codes.size(); i++) {
      String langCode = codes.get(i)[1].equals("NULL") ? codes.get(i)[2] : codes.get(i)[1]; //2-character code if possible
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

  private List<String> expectedFiles(File folderPath) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < codes.size(); i++) {
      String name = String.format("%02d.txt", i);
      String fp = Paths.get(folderPath.getAbsolutePath(), name).toString();
      result.add(fp);
    }
    return result;
  }

  private List<Integer> encode(String text) {
    List<Integer> result = new ArrayList<>();
    result.add(1); //Start of sentence token
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength);
    }
    text = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase().replaceAll("\\s+", "▁");
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

  private Double knp(int a, int b, int tmI) {
    Map<String, Integer> tm = bigramCounts.get(tmI);
    Map<String, Integer> tmU = unigramCounts.get(tmI);
    Map<String, Integer> tmS = bigramSumsPre.get(tmI);
    Map<String, Integer> tmSd = bigramSumsPost.get(tmI);

    int xaCnt = tmS.getOrDefault("" + b, 0);
    int axCnt = tmSd.getOrDefault("" + a, 0);
    int bigramsTotal = tm.size();
    int unigramCnt = tmU.getOrDefault("0_" + a, 1);
    int bigramCnt = tm.getOrDefault(a + "_" + b, 1);

    double d = 0.75;
    double bigramProbNormalized = Double.max(bigramCnt - d, 0) / unigramCnt;
    double pCont = (double) xaCnt / bigramsTotal;
    double lamb = (d * axCnt) / unigramCnt;
    return bigramProbNormalized + (lamb * pCont) + EPSILON;
  }

  private List<Double> normalize(List<Double> vals) {
    double tot = vals.stream().mapToDouble(f -> f).sum();
    return vals.stream().map(n -> n/tot).collect(Collectors.toList());
  }
}
