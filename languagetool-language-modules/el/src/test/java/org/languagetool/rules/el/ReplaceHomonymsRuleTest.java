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
 * ReplaceHomonymsRule TestCase.
 * 
 * @author Nikos-Antonopoulos
 * 
 */
public class ReplaceHomonymsRuleTest {
	private ReplaceHomonymsRule rule;
	private JLanguageTool langTool;

	@Before
	public void setUp() throws IOException {
		rule = new ReplaceHomonymsRule(TestTools.getMessages("el"), new Greek());
		langTool = new JLanguageTool(new Greek());
	}

	// correct sentences
	@Test
	public void testRule() throws IOException {
		assertEquals(0, rule.match(langTool.getAnalyzedSentence("Στην Ελλάδα επικρατεί εύκρατο κλίμα.")).length);
		assertEquals(0, rule.match(langTool.getAnalyzedSentence("Καλή τύχη σου εύχομαι.")).length);
	}

	// test for a wrong usage of a word inside a sentence
	@Test
	public void testRuleInsideOfSentence() throws IOException {
		RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Του ευχήθηκα καλή τείχη για το διαγώνισμα."));
		assertEquals(1, matches.length);
		assertEquals("καλή τύχη", matches[0].getSuggestedReplacements().get(0));
	}

	// test for a wrong usage of a word in the beggining of a sentence.
	@Test
	public void testRuleBegginingOfSentence() throws IOException {
		RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(
				"Τεχνητό κόμμα είναι μια ακραία μορφή αναισθησίας."));
		assertEquals(1, matches.length);
		assertEquals("Τεχνητό κώμα", matches[0].getSuggestedReplacements().get(0));
	}
	
	// test for a wrong usage of a word in the beggining of a sentence while capitalizing letter if needed. 
	@Test
	public void testRuleWithCapitalization() throws IOException {
		RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(
				"γάλος πρόεδρος."));
		assertEquals(1, matches.length);
		assertEquals("Γάλλος πρόεδρος", matches[0].getSuggestedReplacements().get(0));
	}

}
