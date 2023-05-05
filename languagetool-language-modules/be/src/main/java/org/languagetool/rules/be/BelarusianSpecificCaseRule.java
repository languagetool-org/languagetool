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
package org.languagetool.rules.be;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.languagetool.*;
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

import java.util.*;

/**
 * A rule that matches words which need a specific upper/lowercase spelling.
 * Based on English and Russian SpecificCaseRule
 * @since 5.0
 */
public class BelarusianSpecificCaseRule extends Rule {
  
  private static final Set<String> phrases = new THashSet<>(loadPhrases("/be/specific_case.txt"));
  private static int maxLen;

  private static List<String> loadPhrases(String path) {
    List<String> l = new ArrayList<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      int parts = line.split(" ").length;
      maxLen = Math.max(parts, maxLen);
      l.add(line.trim());
    }
    return l;
  }

  private static final Map<String,String> lcToProperSpelling = new THashMap<>();
  static {
    for (String phrase : phrases) {
      lcToProperSpelling.put(phrase.toLowerCase(), phrase);
    }
  }

  public BelarusianSpecificCaseRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("<marker>Вялікая айчынная Вайна</marker> — гэта спецыфічны тэрмін савецкай гістарыяграфіі."),
                   Example.fixed("<marker>Вялікая Айчынная вайна</marker> — гэта спецыфічны тэрмін савецкай гістарыяграфіі."));
  }

  @Override
  public final String getId() {
    return "BE_SPECIFIC_CASE";
  }

  @Override
  public String getDescription() {
    return "Напісанне спецыяльных найменняў у верхнім або ніжнім рэгістры";
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
        if (properSpelling != null && !StringTools.isAllUppercase(phrase) && !phrase.equals(properSpelling)) {
          if (i > 0 && tokens[i-1].isSentenceStart() && !StringTools.startsWithUppercase(properSpelling)) {
            // avoid suggesting e.g. "vitamin C" at sentence start:
            continue;
          }
          String msg;
          if (allWordsUppercase(properSpelling)) {
            msg = "Спецыяльнае найменне, пішацца вялікімі літарамі.";
          } else {
            msg = "Спецыяльные найменне, выкарыстоўвайце прапанаванае напіспанне вялікіх і малых літар.";
          }
          RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i+j-1].getEndPos(), msg);
          match.setSuggestedReplacement(properSpelling);
          matches.add(match);
        }
      }
    }
    return toRuleMatchArray(matches);
  }

  private boolean allWordsUppercase(String s) {
    return Arrays.stream(s.split(" ")).allMatch(StringTools::startsWithUppercase);
  }

}
