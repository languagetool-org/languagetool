/* LanguageTool, a natural language style checker 
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

import java.io.InputStream;
import java.util.*;

/**
 * A rule that matches words which are complex and suggests easier to understand alternatives. 
 * @since 4.8
 */
public class SpecificCaseRule extends Rule {
  
  private final Set<String> phrases;
  private static int maxLen;

  private static List<String> loadPhrases(String path) {
    List<String> l = new ArrayList<>();
    InputStream file = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
    try (Scanner scanner = new Scanner(file, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        int parts = line.split(" ").length;
        maxLen = Math.max(parts, maxLen);
        l.add(line.trim());
      }
    }
    return l;
  }

  private final Map<String,String> lcToProperSpelling = new HashMap<>();

  public SpecificCaseRule(ResourceBundle messages, String phrases) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("I really like <marker>Harry potter</marker>."),
                   Example.fixed("I really like <marker>Harry Potter</marker>."));
    this.phrases = new HashSet<>(loadPhrases(phrases));
    for (String phrase : this.phrases) {
      lcToProperSpelling.put(phrase.toLowerCase(), phrase);
    }
  }

  @Override
  public final String getId() {
    return "SPECIFIC_CASE";
  }

  @Override
  public String getDescription() {
    return "Checks upper/lower case spelling of some proper nouns";
  }

  public String getAllUpperMessage() {
    return "If the term is a proper noun, use initial capitals.";
  }

  public String getMixedCaseMessage() {
    return "If the term is a proper noun, use the suggested capitalization.";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int i = 0; i < tokens.length; i++) {
      List<String> l = new ArrayList<>();
      int j = 0;
      while (l.size() < maxLen && i+j < tokens.length) {
        l.add(tokens[i+j].getToken());
        j++;
        String phrase = String.join(" ", l);
        String lcPhrase = phrase.toLowerCase();
        String properSpelling = lcToProperSpelling.get(lcPhrase);
        if (properSpelling != null && !isAllUppercase(phrase) && !phrase.equals(properSpelling)) {
          if (i > 0 && tokens[i-1].isSentenceStart() && !startsWithUppercase(properSpelling)) {
            // avoid suggesting e.g. "vitamin C" at sentence start:
            continue;
          }
          String msg;
          if (allWordsUppercase(properSpelling)) {
            msg = getAllUpperMessage();
          } else {
            msg = getMixedCaseMessage();
          }
          RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getStartPos() + phrase.length(), msg);
          match.setSuggestedReplacement(properSpelling);
          matches.add(match);
        }
      }
    }
    return toRuleMatchArray(matches);
  }

  private boolean allWordsUppercase(String s) {
    return Arrays.stream(s.split(" ")).allMatch(this::startsWithUppercase);
  }

  private boolean isAllUppercase(String str) {
    return StringTools.isAllUppercase(str);
  }

  private boolean startsWithUppercase(String str) {
    return StringTools.startsWithUppercase(str);
  }
}
