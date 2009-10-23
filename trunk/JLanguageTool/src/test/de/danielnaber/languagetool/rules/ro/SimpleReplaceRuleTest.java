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

	public void testRule() throws IOException {
		SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getMessages("ro"));

		RuleMatch[] matches;
		JLanguageTool langTool = new JLanguageTool(Language.ROMANIAN);

		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Paisprezece case."));
		assertEquals(0, matches.length);

		// incorrect sentences:
		// patrusprezece=paisprezece
		// inside sentence
		matches = rule.match(langTool.getAnalyzedSentence("Satul are patrusprezece case."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals("paisprezece", matches[0].getSuggestedReplacements().get(0));
		// at the beginning of a sentence
		// TODO:  AbstractSimpleReplaceRule is CASE-SENSITIVE!
		// fix AbstractSimpleReplaceRule or modify this test case
		// The word "patrusprezece" exists in the ro/replace.txt file, as shown in the above test.
		// When used capitalized, as "Patrusprezece", it is no longer recognized by the rule!
		matches = rule.match(langTool.getAnalyzedSentence("Patrusprezece case."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals("Paisprezece", matches[0].getSuggestedReplacements().get(0));
		//șasesprezece=șaisprezece
		matches = rule.match(langTool.getAnalyzedSentence("El are șasesprezece ani."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals("șaisprezece", matches[0].getSuggestedReplacements().get(0));

	}
}
