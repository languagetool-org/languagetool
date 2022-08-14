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
package org.languagetool.dev;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.Tagger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Print words unknown to the spell checker, sorted by number of occurrences.
 */
public class UnknownWordFinder {

  private final Map<String,Integer> unknownWords = new HashMap<>();
  private final Set<String> unknownSpelling = new HashSet<>();
  private final Set<String> unknownTag = new HashSet<>();

  private void run(File dir, JLanguageTool lt) throws IOException {
    SpellingCheckRule spellerRule = getSpellingCheckRule(lt);
    Tagger tagger = lt.getLanguage().getTagger();
    List<Path> files = Files.walk(dir.toPath()).filter(Files::isRegularFile).collect(Collectors.toList());
    for (Path file : files) {
      handle(file, lt, spellerRule, tagger);
    }
    printResult(unknownWords);
  }

  @NotNull
  private SpellingCheckRule getSpellingCheckRule(JLanguageTool lt) {
    SpellingCheckRule spellerRule = null;
    for (Rule rule : lt.getAllActiveRules()) {
      if (rule.isDictionaryBasedSpellingRule()) {
        if (spellerRule != null) {
          throw new RuntimeException("Found more than one spell rule: " + rule + ", " + spellerRule);
        }
        spellerRule = (SpellingCheckRule) rule;
      }
    }
    if (spellerRule == null) {
      throw new RuntimeException("No speller rule found for " + lt.getLanguage());
    }
    return spellerRule;
  }

  private void handle(Path f, JLanguageTool lt, SpellingCheckRule rule, Tagger tagger) throws IOException {
    String text = null;
    if (f.toString().toLowerCase().endsWith(".txt")) {
      List<String> lines = Files.readAllLines(f);
      text = String.join(" ", lines);
    } else if (f.toString().toLowerCase().endsWith(".rtf")) {
      text = getTextFromRtf(f);
    } else {
      System.out.println("Ignoring " + f + ": unknown suffix");
    }
    if (text != null) {
      System.out.println("Working on " + f);
      List<AnalyzedSentence> analyzedSentences = lt.analyzeText(text);
      for (AnalyzedSentence analyzedSentence : analyzedSentences) {
        AnalyzedTokenReadings[] tokens = analyzedSentence.getTokensWithoutWhitespace();
        for (AnalyzedTokenReadings token : tokens) {
          String t = token.getToken();
          boolean misspelled = !t.matches("[\\d%$]+") && rule.isMisspelled(t);
          if (misspelled) {
            unknownSpelling.add(t);
          }
          List<AnalyzedTokenReadings> tags = tagger.tag(Collections.singletonList(t));
          boolean noTag = tags.size() == 1 && !tags.get(0).isTagged() && !t.matches("[\\d%$]+");
          if (noTag) {
            unknownTag.add(t);
          }
          if (misspelled || noTag) {
            if (unknownWords.containsKey(t)) {
              unknownWords.put(t, unknownWords.get(t) + 1);
            } else {
              unknownWords.put(t, 1);
            }
          }
        }
      }
    }
  }

  private String getTextFromRtf(Path f) throws IOException {
    JEditorPane p = new JEditorPane();
    p.setContentType("text/rtf");
    EditorKit rtfKit = p.getEditorKitForContentType("text/rtf");
    try {
      rtfKit.read(new FileReader(f.toFile()), p.getDocument(), 0);
      Writer writer = new StringWriter();
      EditorKit txtKit = p.getEditorKitForContentType("text/plain");
      txtKit.write(writer, p.getDocument(), 0, p.getDocument().getLength());
      return  writer.toString();
    } catch (BadLocationException e) {
      System.err.println("Problem running on " + f + ": " + e.getMessage());
      return null;
    }
  }

  private void printResult(Map<String, Integer> unknownWords) {
    List<CountedWord> countedWords = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : unknownWords.entrySet()) {
      countedWords.add(new CountedWord(entry.getKey(), entry.getValue()));
    }
    Collections.sort(countedWords);
    System.out.println("== RESULT ==");
    System.out.println("count\tterm\tunknownSpelling\tunknownTag");
    for (CountedWord countedWord : countedWords) {
      String t = countedWord.word;
      System.out.println(countedWord.count + "\t" + t + "\t" + unknownSpelling.contains(t) + "\t" + unknownTag.contains(t));
    }
  }

  static class CountedWord implements Comparable<CountedWord> {
    int count;
    String word;
    CountedWord(String key, Integer value) {
      word = key;
      count = value;
    }
    @Override
    public int compareTo(@NotNull CountedWord countedWord) {
      return Integer.compare(countedWord.count, count);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + UnknownWordFinder.class.getSimpleName() +  " <langCode> <dir>");
      System.exit(1);
    }
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode(args[0]));
    new UnknownWordFinder().run(new File(args[1]), lt);
  }
}
