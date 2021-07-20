/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

public class GreekRedundancyRuleTest {
	private GreekRedundancyRule rule;
	private JLanguageTool langTool;

	@Before
	public void setUp() throws IOException {
		rule = new GreekRedundancyRule(TestTools.getMessages("el"), new Greek());
		langTool = new JLanguageTool(new Greek());
	}

	// correct sentences
	@Test
	public void testRule() throws IOException {
		assertEquals(0, rule.match(langTool.getAnalyzedSentence("Τώρα μπαίνω στο σπίτι.")).length);
		assertEquals(0, rule.match(langTool.getAnalyzedSentence("Απόψε θα βγω.")).length);
	}

	// test for redundancy within the sentence
	@Test
	public void testRuleWithinSentence() throws IOException {
		RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Τώρα μπαίνω μέσα στο σπίτι."));
		assertEquals(1, matches.length);
		assertEquals("μπαίνω", matches[0].getSuggestedReplacements().get(0));
	}

	// test for redundancy in the beggining of a sentence.
	@Test
	public void testRuleBegginingOfSentence() throws IOException {
		RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(
				"Απόψε το βράδυ θα βγω."));
		assertEquals(1, matches.length);
		assertEquals("Απόψε", matches[0].getSuggestedReplacements().get(0));
	}
	
	// test for redundancy with multiple suggestions
	@Test
	public void testRuleMultipleSuggestions() throws IOException {
		RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(
				"Το μαγαζί ήταν ωραίο, αλλά όμως δεν πέρασα καλά."));
		assertEquals(1, matches.length);
		assertEquals("αλλά,όμως", matches[0].getSuggestedReplacements().get(0));

	}

}