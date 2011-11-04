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

package de.danielnaber.languagetool.rules.ca;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * 
 * Simple tests for rules/ca/SimpleReplaceRule class
 * 
 * @author Ionuț Păduraru
 */
public class CastellanismesReplaceRuleTest extends TestCase {

	private CastellanismesReplaceRule rule;
	private JLanguageTool langTool;

	protected void setUp() throws Exception {
		super.setUp();
		rule = new CastellanismesReplaceRule(TestTools.getMessages("ca"));
		langTool = new JLanguageTool(Language.CATALAN);
	}

	public void testRule() throws IOException {

		// correct sentences:
		assertEquals(0, rule.match(langTool.getAnalyzedSentence("Tot està bé.")).length);

		// incorrect sentences:

		// at the beginning of a sentence (Romanian replace rule is case-sensitive)
		checkSimpleReplaceRule("Después de la mort de Lenin.", "Després");
		// inside sentence
		checkSimpleReplaceRule("Un any después.", "després");
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
