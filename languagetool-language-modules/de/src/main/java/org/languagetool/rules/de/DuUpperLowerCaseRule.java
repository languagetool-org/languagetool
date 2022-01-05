/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.*;

/**
 * Coherent use of Du/du, Dich/dich etc. Assume that the first use has 'correct'
 * capitalization and suggest the same capitalization for subsequent uses. 
 * 
 * @author Daniel Naber
 * @since 4.1
 */
public class DuUpperLowerCaseRule extends TextLevelRule {

  private static final Set<String> lowerWords = new HashSet<>(
          Arrays.asList("du", "dir", "dich", "dein", "deine", "deines", "deins", "deiner", "deinen", "deinem",
                        "euch", "euer", "eure", "euere", "euren", "eueren", "euern", "eurer", "euerer",
                        "eurem", "euerem", "eures", "eueres")
  );

  public DuUpperLowerCaseRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    addExamplePair(Example.wrong("Wie geht es Dir? Bist <marker>du</marker> wieder gesund?"),
                   Example.fixed("Wie geht es Dir? Bist <marker>Du</marker> wieder gesund?"));
    setUrl(Tools.getUrl("https://languagetool.org/insights/de/beitrag/duzen-grossgeschrieben/"));
  }

  @Override
  public String getId() {
    return "DE_DU_UPPER_LOWER";
  }

  @Override
  public String getDescription() {
    return "Einheitliche Verwendung von Du/du, Dir/dir etc.";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    String firstUse = null;
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 0; i < tokens.length; i++) {
        if (i > 0 && (tokens[i-1].isSentenceStart() || StringUtils.equalsAny(tokens[i-1].getToken(), "\"","„", ":", "»", "“"))) {
          continue;
        }
        AnalyzedTokenReadings token = tokens[i];
        String word = token.getToken();
        String lcWord = word.toLowerCase();
        if (lowerWords.contains(lcWord)) {
          if (firstUse == null) {
            firstUse = word;
          } else {
            boolean firstUseIsUpper = StringTools.startsWithUppercase(firstUse);
            String msg = null;
            String replacement = null;
            if (firstUseIsUpper && !StringTools.startsWithUppercase(word)) {
              replacement =  StringTools.uppercaseFirstChar(word);
              msg = "Vorher wurde bereits '" + firstUse + "' großgeschrieben. " +
                      "Aus Gründen der Einheitlichkeit '" + replacement + "' hier auch großschreiben?";
            } else if (!firstUseIsUpper && StringTools.startsWithUppercase(word) && !StringUtils.isAllUpperCase(word)) {
              replacement = StringTools.lowercaseFirstChar(word);
              msg = "Vorher wurde bereits '" + firstUse + "' kleingeschrieben. " +
                      "Aus Gründen der Einheitlichkeit '" + replacement + "' hier auch kleinschreiben?";
            }
            if (msg != null) {
              RuleMatch ruleMatch = new RuleMatch(this, sentence, pos + token.getStartPos(), pos + token.getEndPos(), msg);
              ruleMatch.setSuggestedReplacement(replacement);
              ruleMatches.add(ruleMatch);
            }
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return -1;
  }

}
