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

package de.danielnaber.languagetool.rules.uk;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;


public class SimpleReplaceRuleTest extends TestCase {

	public void testRule() throws IOException {
		SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getEnglishMessages());

		RuleMatch[] matches;
		JLanguageTool langTool = new JLanguageTool(Language.UKRAINIAN);
		
		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Ці рядки повинні збігатися."));
		assertEquals(0, matches.length);

		// incorrect sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Ці рядки повинні співпадати."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals("збігатися", matches[0].getSuggestedReplacements().get(0));
	}
}
