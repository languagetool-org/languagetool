/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.synthesis.ca.CatalanSynthesizer;

/**
 * A rule that suggest spelling out some numbers 
 * 
 * @author Jaume Ortolà
 */

public class CatalanNumberSpellRule extends Rule {

  private CatalanSynthesizer synth;

  public CatalanNumberSpellRule(final ResourceBundle messages, Language language) throws IOException {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Style);
    synth = (CatalanSynthesizer) language.getSynthesizer();
  }

  @Override
  public final String getId() {
    return "CA_NUMBER_SPELL";
  }

  @Override
  public String getDescription() {
    return "Suggereix escriure alguns nombres amb lletres";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length - 1; i++) {
      String[] lemmas = {"dia","setmana","mes", "any", "lustre", "dècada", "mil·lenni"};
      if (tokens[i].getToken().matches("\\d+")
          && tokens[i + 1].hasAnyLemma(lemmas)) {
        String strToSpell = tokens[i].getToken();
        if (tokens[i + 1].hasPartialPosTag("F")) {
          strToSpell = "feminine " + strToSpell;
        }
        String spelledNumber = synth.getSpelledNumber(strToSpell);
        if (!spelledNumber.isEmpty() && spelledNumber.replaceAll("-i-", " ").replaceAll("-", " ").split(" ").length < 4) {
          RuleMatch rm = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(),
              "És preferible escriure aquest nombre amb lletres.", "Preferible amb lletres");
          rm.addSuggestedReplacement(spelledNumber);
          ruleMatches.add(rm);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }
}
