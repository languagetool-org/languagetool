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

package org.languagetool.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;


/*
 * Synthesize suggestions using the lemma from one token (lemma_from) 
 * and the POS tag from another one (postag_from).
 * 
 * The lemma_select and postag_select attributes are required 
 * to choose one among several possible readings.
 */
public abstract class AbstractAdvancedSynthesizerFilter extends RuleFilter {
  
  abstract protected Synthesizer getSynthesizer();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    String postagSelect = getRequired("postag_select", arguments);
    String lemmaSelect = getRequired("lemma_select", arguments);

    String postagFromStr = getOptional("postag_from", arguments);
    int postagFrom;
    String lemmaFromStr = getOptional("lemma_from", arguments);
    int lemmaFrom;
    if (postagFromStr == null || lemmaFromStr == null) {
      return match;
    }

    postagFrom = Integer.parseInt(postagFromStr);
    if (postagFrom < 1 || postagFrom > patternTokens.length) {
      throw new IllegalArgumentException("AdvancedSynthesizerFilter: Index out of bounds in "
          + match.getRule().getFullId() + ", value: " + postagFromStr);
    }
    lemmaFrom = Integer.parseInt(lemmaFromStr);
    if (lemmaFrom < 1 || lemmaFrom > patternTokens.length) {
      throw new IllegalArgumentException("AdvancedSynthesizerFilter: Index out of bounds in "
          + match.getRule().getFullId() + ", value: " + lemmaFromStr);
    }

    String desiredLemma = getAnalyzedToken(patternTokens[lemmaFrom - 1], lemmaSelect).getLemma();
    String desiredPostag = getAnalyzedToken(patternTokens[postagFrom - 1], postagSelect).getPOSTag();
    AnalyzedToken token = new AnalyzedToken("", desiredPostag, desiredLemma);
    String[] replacements = getSynthesizer().synthesize(token, desiredPostag);

    if (replacements.length > 0) {
      RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
          match.getMessage(), match.getShortMessage());
      newMatch.setType(match.getType());
      List<String> replacementsList = new ArrayList<String>(); 
      replacementsList.addAll(match.getSuggestedReplacements());
      replacementsList.addAll(Arrays.asList(replacements));
      newMatch.setSuggestedReplacements(replacementsList);
      return newMatch;
    }
    return match;
  }

  private AnalyzedToken getAnalyzedToken(AnalyzedTokenReadings aToken, String regexp) {
    Pattern pattern = Pattern.compile(regexp);
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return analyzedToken;
      }
    }
    return null;
  }

}
