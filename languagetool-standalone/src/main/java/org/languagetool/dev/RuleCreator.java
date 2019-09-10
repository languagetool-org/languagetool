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
package org.languagetool.dev;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.rules.ConfusionPair;
import org.languagetool.rules.ConfusionSetLoader;
import org.languagetool.rules.ConfusionString;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.util.*;

/**
 * Takes the output of {@link HomophoneOccurrenceDumper} and automatically
 * writes the error probabilities of variants (e.g. "0.977 public hair")
 * when {@code XML_MODE = false} or writes XML rules when {@code XML_MODE = true}.
 * @since 2.8
 */
public class RuleCreator {

  private static final boolean XML_MODE = true;
  
  private final Map<String, List<OccurrenceInfo>> occurrenceInfos = new HashMap<>();
  private final Map<String, Long> ngramToOccurrence = new HashMap<>();
  private final WordTokenizer wordTokenizer = new English().getWordTokenizer();
  private final float minErrorProb;

  private int ruleCount = 0;
  private int tokenFilteredRules = 0;
  private int probFilteredRules = 0;

  public RuleCreator(float minErrorProb) {
    this.minErrorProb = minErrorProb;
  }

  private void run(File homophoneOccurrences, String homophonePath) throws IOException {
    ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
    InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(homophonePath);
    Map<String,List<ConfusionPair>> confusionPairsMap = confusionSetLoader.loadConfusionPairs(inputStream);
    initMaps(homophoneOccurrences);
    int groupCount = 0;
    if (XML_MODE) {
      System.out.println("<rules lang='en'>\n");
      System.out.println("<category name='Auto-generated rules'>\n");
    }
    for (Map.Entry<String, List<ConfusionPair>> entry : confusionPairsMap.entrySet()) {
      System.err.println(" === " + entry + " === ");
      if (entry.getValue().size() > 1) {
        System.err.println("WARN: will use only first pair of " + entry.getValue().size() + ": " + entry.getValue().get(0));
      }
      List<OccurrenceInfo> infos = occurrenceInfos.get(entry.getKey());
      if (infos == null) {
        System.err.println("Could not find occurrence infos for '" + entry.getKey() + "', skipping");
        continue;
      }
      Set cleanSet = new HashSet<>(entry.getValue().get(0).getTerms());
      cleanSet.remove(entry.getKey());
      String name = StringUtils.join(cleanSet, "/") + " -> " + entry.getKey();
      if (XML_MODE) {
        System.out.println("<rulegroup id='R" + groupCount + "' name=\"" + StringTools.escapeXML(name) + "\">\n");
      }
      groupCount++;
      for (OccurrenceInfo occurrenceInfo : infos) {
        String[] parts = occurrenceInfo.ngram.split(" ");
        for (ConfusionString variant : entry.getValue().get(0).getTerms()) {
          if (variant.getString().equals(entry.getKey())) {
            continue;
          }
          printRule(occurrenceInfo, parts, variant.getString());
        }
      }
      if (XML_MODE) {
        System.out.println("</rulegroup>\n");
      }
    }
    if (XML_MODE) {
      System.out.println("</category>");
      System.out.println("</rules>");
    }
    System.err.println("Done. Wrote " + ruleCount + " rules.");
    System.err.println("Rules ignored because of different tokenization: " + tokenFilteredRules);
    System.err.println("Rules ignored because of error probability limit (" + minErrorProb + "): " + probFilteredRules);
  }

  private void printRule(OccurrenceInfo occurrenceInfo, String[] parts, String variant) {
    String term = parts[1];
    String termPhrase = parts[0] + " " + parts[1] + " " + parts[2];
    String variantPhrase = parts[0] + " " + variant + " " + parts[2];
    List<String> tokens = wordTokenizer.tokenize(variantPhrase);
    if (tokens.size() != 3+2) {  // 3 tokens, 2 whitespace
      System.err.println("Skipping '" + variantPhrase + "', does not tokenize to 3 tokens: " + tokens);
      tokenFilteredRules++;
      return;
    }
    Long variantOccObj = ngramToOccurrence.get(variantPhrase);
    long variantOcc = variantOccObj != null ? variantOccObj : 0;
    long totalOcc = occurrenceInfo.occurrence + variantOcc;
    float variantProb = (float)variantOcc / totalOcc;
    float variantErrorProb = 1.0f - variantProb;
    if (variantErrorProb < minErrorProb) {
      System.err.println("Skipping '" + variantPhrase + "', error probability too low: " + variantErrorProb + " < " + minErrorProb);
      probFilteredRules++;
      return;
    }
    if (XML_MODE) {
      System.out.printf(Locale.ENGLISH,
                      "  <rule case_sensitive='yes'>\n" +
                      "    <!-- auto-generated, error probability: %.3f, correct phrase occurrences: %d -->\n" +
                      "    <pattern>\n" +
                      "      <token>%s</token>\n" +
                      "      <marker><token>%s</token></marker>\n" +
                      "      <token>%s</token>\n" +
                      "    </pattern>\n" +
                      "    <message>Did you mean <suggestion>%s</suggestion>?</message>\n" +
                      "    <example type='incorrect'>%s</example>\n" +
                      "    <example type='correct'>%s</example>\n" +
                      "  </rule>\n\n", variantErrorProb, occurrenceInfo.occurrence,
                                       StringTools.escapeXML(parts[0]), StringTools.escapeXML(variant), StringTools.escapeXML(parts[2]),
                                       StringTools.escapeXML(term), StringTools.escapeXML(variantPhrase), StringTools.escapeXML(termPhrase));
    } else {
      System.out.printf(Locale.ENGLISH, "%.2f\t%s\t%s\n", variantErrorProb, variantPhrase, term);
    }
    ruleCount++;
  }

  private void initMaps(File homophoneOccurrenceFile) throws FileNotFoundException {
    try (Scanner s = new Scanner(homophoneOccurrenceFile)) {
      while (s.hasNextLine()) {
        String line = s.nextLine();
        String[] parts = line.split("\t");
        if (parts.length != 3) {
          throw new RuntimeException("Unexpected format: '" + line + "'");
        }
        long occurrenceCount = Integer.parseInt(parts[1]);
        OccurrenceInfo occurrenceInfo = new OccurrenceInfo(parts[2], occurrenceCount);
        List<OccurrenceInfo> list;
        if (occurrenceInfos.containsKey(parts[0])) {
          list = occurrenceInfos.get(parts[0]);
        } else {
          list = new ArrayList<>();
        }
        list.add(occurrenceInfo);
        occurrenceInfos.put(parts[0], list);
        ngramToOccurrence.put(parts[2], occurrenceCount);
      }
    }
  }

  static class OccurrenceInfo {
    private final String ngram;
    private final long occurrence;
    OccurrenceInfo(String ngram, long occurrence) {
      this.ngram = ngram;
      this.occurrence = occurrence;
    }
    @Override
    public String toString() {
      return ngram + "/" + occurrence;
    }
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length < 1 || args.length > 2) {
      System.out.println("Usage: " + RuleCreator.class.getSimpleName() + " <homophoneResultFile> [minErrorProbability]");
      System.out.println("    homophoneResultFile   the output of org.languagetool.dev.HomophoneOccurrenceDumper");
      System.out.println("    minErrorProbability   the minimal error probability (0.0-1.0), other rules will be ignored");
      System.exit(1);
    }
    float minErrorProb = args.length >= 2 ? Float.parseFloat(args[1]) : 0.0f;
    RuleCreator creator = new RuleCreator(minErrorProb);
    creator.run(new File(args[0]), "/en/confusion_sets_subset.txt");
  }

}
