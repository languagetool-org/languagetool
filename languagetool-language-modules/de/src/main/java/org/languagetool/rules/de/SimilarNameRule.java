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
package org.languagetool.rules.de;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

/**
 * Search for names similar to ones used before in the same text, as these
 * might be typos. Note: this rule if off by default, as it only works on known
 * names and the internal dictionary doesn't know that many names.
 * 
 * @author Daniel Naber
 * @since 3.0
 */
public class SimilarNameRule extends TextLevelRule {

  private static final int MIN_LENGTH = 4;
  private static final int MAX_DIFF = 1;
  
  public SimilarNameRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    addExamplePair(Example.wrong("Angela Müller ist CEO. <marker>Miller</marker> wurde in Hamburg geboren."),
                   Example.fixed("Angela Müller ist CEO. <marker>Müller</marker> wurde in Hamburg geboren."));
    setDefaultOff();
  }

  @Override
  public String getId() {
    return "DE_SIMILAR_NAMES";
  }

  @Override
  public String getDescription() {
    return "Mögliche Tippfehler in Namen finden";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    Set<String> namesSoFar = new HashSet<>();
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (AnalyzedTokenReadings token : tokens) {
        String word = token.getToken();
        // not tagged = too many correct words are not known so we cannot use that:
        //boolean isName = word.length() > MIN_LENGTH && (token.hasPartialPosTag("EIG:") || !token.isTagged());
        boolean isMaybeName = word.length() >= MIN_LENGTH 
                && ((token.hasPartialPosTag("EIG:") && !token.hasPartialPosTag(":COU")) || token.isPosTagUnknown())
                && !word.equals("Dein") && !word.equals("Deine") && !word.equals("Deinen") && !word.equals("Deiner") && !word.equals("Deines") && !word.equals("Deinem")
                && !word.equals("Ihr") && !word.equals("Ihre") && !word.equals("Ihren") && !word.equals("Ihrer") && !word.equals("Ihres") && !word.equals("Ihrem");
        if (isMaybeName && StringTools.startsWithUppercase(word)) {
          String similarName = similarName(word, namesSoFar);
          if (similarName != null) {
            String msg = "'" + word + "' ähnelt dem vorher benutzten '" + similarName + "', handelt es sich evtl. um einen Tippfehler?";
            RuleMatch ruleMatch = new RuleMatch(this, sentence, pos+token.getStartPos(), pos+token.getEndPos(), msg);
            ruleMatch.setSuggestedReplacement(similarName);
            ruleMatches.add(ruleMatch);
          }
          namesSoFar.add(word);
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Nullable
  private String similarName(String nameHere, Set<String> namesSoFar) {
    for (String name : namesSoFar) {
      if (name.equals(nameHere)) {
        continue;
      }
      int lenDiff = Math.abs(name.length() - nameHere.length());
      boolean nameEndsWithS = name.endsWith("s") && !nameHere.endsWith("s");
      boolean otherNameEndsWithS = !name.endsWith("s") && nameHere.endsWith("s");
      boolean nameEndsWithN = name.endsWith("n") && !nameHere.endsWith("n");   // probably a dative
      boolean otherNameEndsWithN = !name.endsWith("n") && nameHere.endsWith("n");
      if (nameEndsWithS || otherNameEndsWithS || nameEndsWithN || otherNameEndsWithN) {
        // we assume this is a genitive, e.g. "Angela Merkels Ehemann"
        continue;
      }
      if (lenDiff <= MAX_DIFF && StringUtils.getLevenshteinDistance(name, nameHere) <= MAX_DIFF) {
        return name;
      }
    }
    return null;
  }
  
}
