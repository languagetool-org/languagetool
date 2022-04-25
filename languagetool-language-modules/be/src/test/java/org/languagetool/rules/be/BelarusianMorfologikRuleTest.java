/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Belarusian;
import org.languagetool.rules.RuleMatch;

public class BelarusianMorfologikRuleTest {
  
  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikBelarusianSpellerRule rule = new MorfologikBelarusianSpellerRule(TestTools.getMessages("be"), new Belarusian(), null, Collections.emptyList());
  JLanguageTool lt = new JLanguageTool(new Belarusian());
  // accent
  RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(" сумна знакаміты вялікімі стратамі"));
  assertEquals(0, matches.length);

  matches = rule.match(lt.getAnalyzedSentence("знакамты"));
  assertEquals(1, matches.length);
  assertEquals("[знакаміты, знакам ты]", matches[0].getSuggestedReplacements().toString());
  }
}

