/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.el;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Greek;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * NumeralStressRule TestCase.
 * 
 * @author Panagiotis Minos
 * @since 3.3
 */
public class NumeralStressRuleTest {

  private NumeralStressRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws IOException {
    rule = new NumeralStressRule(TestTools.getMessages("el"));
    lt = new JLanguageTool(new Greek());
  }

  @Test
  public void testRule() throws IOException {

    assertCorrect("1ος");
    assertCorrect("2η");
    assertCorrect("3ο");
    assertCorrect("20ός");
    assertCorrect("30ή");
    assertCorrect("40ό");
    assertCorrect("1000ών");
    assertCorrect("1010ες");

    assertIncorrect("4ός", "4ος");
    assertIncorrect("5ή", "5η");
    assertIncorrect("6ό", "6ο");
    assertIncorrect("100ος","100ός");
    assertIncorrect("200η","200ή");
    assertIncorrect("300ο","300ό");
    assertIncorrect("2000ων", "2000ών");
    assertIncorrect("2010ές", "2010ες");
    assertIncorrect("2020α", "2020ά");
    
  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence, String correction) throws IOException {
    final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals(correction, matches[0].getSuggestedReplacements().get(0));
  }

}
