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

  private final HashMap<String, Integer> vocab;
  private final List<String[]> codes; // Elem format = {Name, 2-code (or "NULL"), 3-code}

  private final List<HashMap<String, Integer>> bigram_counts;
  private final List<HashMap<String, Integer>> unigram_counts;
  private final List<HashMap<String, Integer>> bigram_sums_pre;
  private final List<HashMap<String, Integer>> bigram_sums_post;

  private final List<List<Double>> scales;

  private final int maxLength;
  private final boolean knp;
  private final boolean scaling;

  public NGramLangIdentifier(){
    this("C:\\Users\\Robert\\Desktop\\RobertAIStuff\\language_identification\\data\\prod2", 30, false, false);
    //this("C:\\Users\\Robert\\Desktop\\RobertAIStuff\\language_identification\\data\\tm_30bpe_d2_ml30_n", 30, false);
  }

  private static HashMap<String, Integer> load_dict(String path) {
    HashMap<String, Integer> tm = new HashMap<>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.trim().split(" ");
        String key = String.join("_", Arrays.copyOfRange(parts, 0, parts.length-1));
        tm.put(key, Integer.parseInt(parts[parts.length-1]));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return tm;
  }

  private List<String> expected_files(String folderPath){
    List<String> result = new ArrayList<>();
    for(int i = 0; i < this.codes.size(); i++){
      String name = String.format("%02d.txt", i);
      String fp = Paths.get(folderPath, name).toString();
      result.add(fp);
    }
    return result;
  }

  public NGramLangIdentifier(String source_folder, int maxLength, boolean kn_smoothing, boolean scaling){
    this.maxLength = maxLength;
    this.knp = kn_smoothing;
    this.scaling = scaling;
    String vocab_path = Paths.get(source_folder, "vocab.txt").toString();
    String iso_path = Paths.get(source_folder, "iso_codes.tsv").toString();
    String ug_path = Paths.get(source_folder, "/ug/").toString();
    String sums_path_pre = Paths.get(source_folder, "/sums/pre/").toString();
    String sums_path_post = Paths.get(source_folder, "/sums/post/").toString();
    String scales_path = Paths.get(source_folder, "scales.txt").toString();

    //Load language codes - Line format = {Language Name}\t{2-code or "NULL"}\t{3-code}
    this.codes = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(iso_path))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split("\t");
        this.codes.add(values);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    //Load vocab - Line format = {token}
    this.vocab = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(vocab_path))) {
      String line;
      int i = 0;
      while ((line = br.readLine()) != null) {
        this.vocab.put(line.trim(), i);
        i++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    //Load transition matrices - Line format = {i} {j} {val}
    this.bigram_counts = new ArrayList<>();
    for (String path : this.expected_files(source_folder)){
      this.bigram_counts.add(load_dict(path));
    }

    this.unigram_counts = new ArrayList<>();
    for (String path : this.expected_files(ug_path)){
      this.unigram_counts.add(load_dict(path));
    }

    //Load sums - Line format = {i} {val}
    this.bigram_sums_pre = new ArrayList<>();
    for (String path : this.expected_files(sums_path_pre)){
      this.bigram_sums_pre.add(load_dict(path));
    }

    this.bigram_sums_post = new ArrayList<>();
    for (String path : this.expected_files(sums_path_post)){
      this.bigram_sums_post.add(load_dict(path));
    }

    if (scaling){
      //Load scales - Line format = {val} {val} ... {val}
      this.scales = new ArrayList<>();
      try {
        BufferedReader br = new BufferedReader(new FileReader(scales_path));
        String line;
        while ((line = br.readLine()) != null) {
          String[] parts = line.trim().split(" ");
          this.scales.add(Arrays.stream(parts).map(Double::parseDouble).collect(Collectors.toList()));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else{
      this.scales = null;
    }


  }

  private List<Integer> encode(String text){
    List<Integer> result = new ArrayList<>();
    result.add(1); //Start of sentence token
    if (text.length() > this.maxLength)
      text = text.substring(0, this.maxLength);
    text = Normalizer.normalize(text, Normalizer.Form.NFKC).replaceAll("^\\s+","").replaceAll("\\s+", "▁").toLowerCase();
    if (text.length() == 0)
      return result;
    text = "▁" + text;

    int cur = 0;
    while (cur < text.length()){
      int tok = 0;
      int ci = 1;
      for (int i = cur + 1; i <= text.length(); i++){
        int maybe_tok = this.vocab.getOrDefault(text.substring(cur, i), -1);
        if (maybe_tok > -1){
          tok = maybe_tok;
          ci = i - cur;
        }
      }
      cur += ci;
      result.add(tok);
    }
    return result;
  }

  private List<int[]> keys(List<Integer> enc){
    //For now just bigrams
    List<int[]> result = new ArrayList<>();
    for (int i = 1; i < enc.size(); i++){
      result.add(new int[]{enc.get(i-1), enc.get(i)});
    }
    return result;
  }

  private Double knp(int a, int b, int tm_i){
    HashMap<String, Integer> tm = this.bigram_counts.get(tm_i);
    HashMap<String, Integer> tm_u = this.unigram_counts.get(tm_i);
    HashMap<String, Integer> tm_s = this.bigram_sums_pre.get(tm_i);
    HashMap<String, Integer> tm_sd = this.bigram_sums_post.get(tm_i);

    int unigram_cnt = tm_u.getOrDefault("0_" + a, 0);

    if (unigram_cnt == 0)
      return EPSILON;

    int xa_cnt = tm_s.getOrDefault("" + b, 0);
    int ax_cnt = tm_sd.getOrDefault("" + a, 0);
    int bigrams_total = tm.size();
    int bigram_cnt = tm.getOrDefault(a + "_" + b, 0);

    double d = 0.75;

    double bigram_prob_normalized = Double.max(bigram_cnt - d, 0)/unigram_cnt;
    double p_cont = (double) xa_cnt / bigrams_total;
    double lamb = (d * ax_cnt) / unigram_cnt;
    return bigram_prob_normalized + (lamb * p_cont);
  }

  private List<Double> normalize(List<Double> vals){
    double tot = vals.stream().mapToDouble(f -> f).sum();
    return vals.stream().map(n -> n/tot).collect(Collectors.toList());
  }

  public Map<String, Double> runFasttext(String text, List<String> additionalLanguageCodes) {
    List<Integer> enc = this.encode(text);
    List<Double> vals = new ArrayList<>();

    for(int i = 0; i < this.codes.size(); i++){
      double val = 0;
      for (int[] key: this.keys(enc)){
        double prob;
        if (this.knp)
          prob = knp(key[0], key[1], i);
        else {
          int ug_cnt = this.unigram_counts.get(i).getOrDefault("0_" + key[0], 0);
          if (ug_cnt == 0)
            prob = EPSILON;
          else{
            prob = (double) (this.bigram_counts.get(i).getOrDefault(key[0] + "_" + key[1], 1)) / ug_cnt;
          }
        }
        val += log(max(prob, EPSILON));
      }
      vals.add(exp(val));
    }

    if(this.scaling){
      List<Double> l1_normed = vals;
      vals = new ArrayList<>();
      for (int i = 0; i < l1_normed.size(); i++){
        double val = 0;
        for (double d : this.scales.get(i)){
          val += d * l1_normed.get(i);
        }
        vals.add(val);
      }
    }

    vals = this.normalize(vals);

    HashMap<String, Double> result = new HashMap<>();
    for(int i = 0; i < this.codes.size(); i++){
      String lang_code = this.codes.get(i)[1].equals("NULL") ? this.codes.get(i)[2] : this.codes.get(i)[1]; //2-character code if possible
      if (canLanguageBeDetected(lang_code, additionalLanguageCodes)) {
        result.put(lang_code, vals.get(i));
      }
    }

    return result;
  }
}
