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
package org.languagetool.rules.patterns;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.FakeLanguage;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;
import static org.languagetool.rules.patterns.RuleSet.textLemmaHinted;

public class RuleSetTest {
  private static final AnalyzedSentence sampleSentence = new AnalyzedSentence(new AnalyzedTokenReadings[]{new AnalyzedTokenReadings(
    new AnalyzedToken("token", "pos", "lemma")
  )});

  @Test
  public void textHintsAreHonored() {
    PatternRule suitable = ruleOf(csToken("token"));
    assertRulesForSentence(RuleSet.textHinted(Collections.singletonList(suitable)), suitable);

    suitable = ruleOf(token("Token"));
    assertRulesForSentence(RuleSet.textHinted(Collections.singletonList(suitable)), suitable);

    suitable = ruleOf(tokenRegex("token|another"));
    assertRulesForSentence(RuleSet.textHinted(Collections.singletonList(suitable)), suitable);

    assertRulesForSentence(RuleSet.textHinted(Collections.singletonList(ruleOf(csToken("unsuitable")))));
  }

  @Test
  public void lemmaHintsAreHonored() {
    PatternRule suitable = ruleOf(new PatternTokenBuilder().token("lemma").matchInflectedForms().build());
    assertRulesForSentence(textLemmaHinted(Collections.singletonList(suitable)), suitable);

    suitable = ruleOf(new PatternTokenBuilder().token("Lemma").matchInflectedForms().build());
    assertRulesForSentence(textLemmaHinted(Collections.singletonList(suitable)), suitable);

    suitable = ruleOf(new PatternTokenBuilder().csTokenRegex("lemm[ab]").matchInflectedForms().build());
    assertRulesForSentence(textLemmaHinted(Collections.singletonList(suitable)), suitable);

    PatternToken unsuitable = new PatternTokenBuilder().csToken("unsuitable").matchInflectedForms().build();
    assertRulesForSentence(textLemmaHinted(Collections.singletonList(ruleOf(unsuitable))));

    PatternRule unrelated = ruleOf(pos("somePos"));
    assertRulesForSentence(textLemmaHinted(Arrays.asList(ruleOf(unsuitable), unrelated)), unrelated);
  }

  private static void assertRulesForSentence(RuleSet ruleSet, PatternRule... expected) {
    assertEquals(Arrays.asList(expected), ruleSet.rulesForSentence(sampleSentence));
  }

  private static PatternRule ruleOf(PatternToken token) {
    return new PatternRule("", new FakeLanguage(), Collections.singletonList(token), "", "", "");
  }
}
