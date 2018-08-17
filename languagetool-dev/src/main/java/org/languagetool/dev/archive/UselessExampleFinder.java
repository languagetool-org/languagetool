/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.archive;

import org.apache.commons.io.IOUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRule;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Finds and removes "useless" examples sentences. "Useless" are sentences
 * of type="correct" that are already covered by an incorrect counterpart
 * with a correction that, when applied, leads to the correct sentence.
 * NOTE: this is an ugly hack, it might miss sentences or even remove ones
 * that should not be removed.
 */
final class UselessExampleFinder {

  private int uselessExampleCount;
  private int removedLinesCount;

  private void run(Language lang) throws IOException {
    File basePath = new File("/lt/git/languagetool/languagetool-language-modules");
    if (!basePath.exists()) {
      throw new RuntimeException("basePath does not exist: " + basePath);
    }
    String langCode = lang.getShortCode();
    File xml = new File(basePath, "/" + langCode + "/src/main/resources/org/languagetool/rules/" + langCode + "/grammar.xml");
    List<String> xmlLines = IOUtils.readLines(new FileReader(xml));
    JLanguageTool tool = new JLanguageTool(lang);
    for (Rule rule : tool.getAllActiveRules()) {
      if (!(rule instanceof PatternRule)) {
        continue;
      }
      List<CorrectExample> correctExamples = rule.getCorrectExamples();
      List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
      for (IncorrectExample incorrectExample : incorrectExamples) {
        checkCorrections(rule, correctExamples, incorrectExample, xmlLines);
      }
    }
    System.err.println("Useless examples: " + uselessExampleCount);
    System.err.println("Removed lines: " + removedLinesCount);
    for (String xmlLine : xmlLines) {
      System.out.println(xmlLine);
    }
  }

  private void checkCorrections(Rule rule, List<CorrectExample> correctExamplesObjs, IncorrectExample incorrectExample, List<String> xmlLines) {
    List<String> correctExamples = correctExamplesObjs.stream().map(k -> k.getExample()).collect(Collectors.toList());
    List<String> corrections = incorrectExample.getCorrections();
    for (String correction : corrections) {
      String fixedSentence = incorrectExample.getExample().replaceAll("<marker>.*?</marker>", "<marker>" + correction.replace("$", "\\$") + "</marker>");
      String fixedSentenceNoMarker = incorrectExample.getExample().replaceAll("<marker>.*?</marker>", correction.replace("$", "\\$"));
      if (correctExamples.contains(fixedSentence)) {
        System.err.println("Useless: " + fixedSentence + " in " + rule.getId());
        removeLinesFromXml(rule, fixedSentence, xmlLines);
        uselessExampleCount++;
      }
      if (correctExamples.contains(fixedSentenceNoMarker)) {
        System.err.println("Useless: " + fixedSentenceNoMarker + " in " + rule.getId());
        removeLinesFromXml(rule, fixedSentenceNoMarker, xmlLines);
        uselessExampleCount++;
      }
    }
  }

  // Note: this is a bad hack, we just iterate through the file's lines
  private void removeLinesFromXml(Rule rule, String sentenceToRemove, List<String> xmlLines) {
    List<Integer> linesToRemove = new ArrayList<>();
    String currentRuleId = null;
    Pattern pattern = Pattern.compile(".*id=[\"'](.*?)[\"'].*");
    String expectedSubId = ((AbstractPatternRule) rule).getSubId();
    int lineCount = 0;
    int subRuleCount = 0;
    int removedCount = 0;
    boolean inRuleGroup = false;
    for (String xmlLine : xmlLines) {
      if (xmlLine.contains("<rulegroup")) {
        subRuleCount = 0;
        inRuleGroup = true;
      } else if (xmlLine.contains("</rulegroup>")) {
        subRuleCount = 0;
        inRuleGroup = false;
      } else if ((xmlLine.contains("<rule ")||xmlLine.contains("<rule>")) && inRuleGroup) {
        subRuleCount++;
      }
      Matcher m = pattern.matcher(xmlLine);
      if (m.matches()) {
        currentRuleId = m.group(1);
      }
      if (!xmlLine.contains("correction=") && xmlLine.contains(sentenceToRemove + "</example>")) {
        if (currentRuleId != null && !currentRuleId.equals(rule.getId())) {
          lineCount++;
          continue;
        }
        if (!inRuleGroup) {
          subRuleCount = 1;
        }
        if (!expectedSubId.equals("0") && !expectedSubId.equals(String.valueOf(subRuleCount))) {
          lineCount++;
          continue;
        }
        linesToRemove.add(lineCount);
        break;
      }
      lineCount++;
    }
    Collections.reverse(linesToRemove); // start from end, as we need to remove items
    for (Integer s : linesToRemove) {
      xmlLines.remove(s.intValue());
      removedLinesCount++;
      removedCount++;
    }
    if (removedCount == 0) {
      System.err.println("No line removed: " + rule + "[" + expectedSubId + "]");
    }
  }

  public static void main(String[] args) throws IOException {
    UselessExampleFinder prg = new UselessExampleFinder();
    prg.run(Languages.getLanguageForShortCode("de"));
  }

}
