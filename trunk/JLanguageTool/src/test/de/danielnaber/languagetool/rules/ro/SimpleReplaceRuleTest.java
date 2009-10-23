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

package de.danielnaber.languagetool.rules.ro;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * 
 * Simple tests for rules/ro/SimpleReplaceRule class
 * 
 * @author Ionuț Păduraru
 */
public class SimpleReplaceRuleTest extends TestCase {

	private SimpleReplaceRule rule;
	private JLanguageTool langTool;

	protected void setUp() throws Exception {
		super.setUp();
		rule = new SimpleReplaceRule(TestTools.getMessages("ro"));
		langTool = new JLanguageTool(Language.ROMANIAN);
	}

	public void testRule() throws IOException {

		// correct sentences:
		assertEquals(0, rule.match(langTool.getAnalyzedSentence("Paisprezece case.")).length);

		// incorrect sentences:

		// at the beginning of a sentence (Romanian replace rule is case-sensitive)
		checkSimpleReplaceRule("Patrusprezece case.", "Paisprezece");
		// inside sentence
		checkSimpleReplaceRule("Satul are patrusprezece case.", "paisprezece");
		checkSimpleReplaceRule("Satul are (patrusprezece) case.", "paisprezece");
		checkSimpleReplaceRule("Satul are «patrusprezece» case.", "paisprezece");
		
		checkSimpleReplaceRule("El are șasesprezece ani.", "șaisprezece");
		checkSimpleReplaceRule("El a luptat pentru întâiele cărți.", "întâile");
		checkSimpleReplaceRule("El are cinsprezece cărți.", "cincisprezece");
		checkSimpleReplaceRule("El a fost patruzecioptist.", "pașoptist");
		checkSimpleReplaceRule("M-am adresat întâiei venite.", "întâii");
		checkSimpleReplaceRule("M-am adresat întâielor venite.", "întâilor");
		checkSimpleReplaceRule("A ajuns al douăzecelea.", "douăzecilea");
		checkSimpleReplaceRule("A ajuns al zecilea.", "zecelea");
	}

	/**
	 * Check if a specific replace rule applies.
	 * 
	 * @param sentence
	 *            the sentence containing the incorrect/misspeled word.
	 * @param word
	 *            the word that is correct (the suggested replacement).
	 * @throws IOException
	 */
	private void checkSimpleReplaceRule(String sentence, String word)
			throws IOException {
		RuleMatch[] matches;
		matches = rule.match(langTool.getAnalyzedSentence(sentence));
		assertEquals("Invalid matches.length while checking sentence: "
				+ sentence, 1, matches.length);
		assertEquals("Invalid replacement count wile checking sentence: "
				+ sentence, 1, matches[0].getSuggestedReplacements().size());
		assertEquals("Invalid suggested replacement while checking sentence: "
				+ sentence, word, matches[0].getSuggestedReplacements().get(0));
	}
}
