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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

	/**
	 * Make sure that the suggested word is not the same as the wrong word
	 */
	public void testInvalidSuggestion()  {
		List<String> invalidSuggestions = new ArrayList<String>();
		List<Map<String,String>> wrongWords = rule.getWrongWords();
		for (Map<String, String> ruleEntry : wrongWords) {
			for (Map.Entry<String,String> fromWord : ruleEntry.entrySet()) {
				String toWord = fromWord.getValue();
				if (toWord == null || fromWord.equals(toWord)) {
					invalidSuggestions.add(toWord);
				}
			}
		}
		if (!invalidSuggestions.isEmpty()) {
			fail("Invalid suggestions found for: " + Arrays.toString(invalidSuggestions.toArray(new String[]{})));
		}
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
		checkSimpleReplaceRule("A primit jumate de litru de lapte.", "jumătate");

		// multiple words / compounds
			// space-delimited
		checkSimpleReplaceRule("aqua forte", "acvaforte");
		checkSimpleReplaceRule("aqua forte.", "acvaforte");
		checkSimpleReplaceRule("A folosit «aqua forte».", "acvaforte");
		checkSimpleReplaceRule("Aqua forte.", "Acvaforte");
		checkSimpleReplaceRule("este aqua forte", "acvaforte");
		checkSimpleReplaceRule("este aqua forte.", "acvaforte");
		checkSimpleReplaceRule("este Aqua Forte.", "Acvaforte");
		checkSimpleReplaceRule("este AquA Forte.", "Acvaforte");
		checkSimpleReplaceRule("A primit jumate de litru de lapte și este aqua forte.", "jumătate", "acvaforte");
		checkSimpleReplaceRule("du-te vino", "du-te-vino");
			// dash-delimited
		checkSimpleReplaceRule("cou-boi", "cowboy");
		checkSimpleReplaceRule("cow-boy", "cowboy");
		checkSimpleReplaceRule("cau-boi", "cowboy");
		checkSimpleReplaceRule("Cau-boi", "Cowboy");
		checkSimpleReplaceRule("cowboy"); // correct, no replacement
		checkSimpleReplaceRule("Iată un cau-boi", "cowboy");
		checkSimpleReplaceRule("Iată un cau-boi.", "cowboy");
		checkSimpleReplaceRule("Iată un (cau-boi).", "cowboy");
		checkSimpleReplaceRule("văcar=cau-boi", "cowboy");
		
		
		// multiple suggestions
		checkSimpleReplaceRule("A fost adăogită o altă regulă.", "adăugită/adăugată");
		checkSimpleReplaceRule("A venit adinioarea.", "adineaori/adineauri");
		
		// words with multiple wrong forms
		checkSimpleReplaceRule("A pus axterix.", "asterisc");
		checkSimpleReplaceRule("A pus axterics.", "asterisc");
		checkSimpleReplaceRule("A pus asterics.", "asterisc");
	}

	/**
	 * Check if a specific replace rule applies.
	 * 
	 * @param sentence
	 *            the sentence containing the incorrect/misspeled word.
	 * @param words
	 *            the words that are correct (the suggested replacement). Use "/" to separate multiple forms.
	 * @throws IOException
	 */
	private void checkSimpleReplaceRule(String sentence, String... words)
			throws IOException {
		RuleMatch[] matches;
		matches = rule.match(langTool.getAnalyzedSentence(sentence));
		assertEquals("Invalid matches.length while checking sentence: "
				+ sentence, words.length, matches.length);
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			String[] replacements = word.split("\\/"); 
			assertEquals("Invalid replacement count wile checking sentence: "
					+ sentence, replacements.length, matches[i].getSuggestedReplacements().size());
			for (int j = 0; j < replacements.length; j++) {
				assertEquals("Invalid suggested replacement while checking sentence: "
					+ sentence, replacements[j], matches[i].getSuggestedReplacements().get(j));
			}
		}
	}
}
