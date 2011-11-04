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

package de.danielnaber.languagetool.rules.pl;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

public class PolishWordRepeatRuleTest extends TestCase {

	/*
	 * Test method for 'de.danielnaber.languagetool.rules.pl.PolishWordRepeatRule.match(AnalyzedSentence)'
	 */
	public void testRule() throws IOException {
	    final PolishWordRepeatRule rule = new PolishWordRepeatRule(null);
	    RuleMatch[] matches;
	    JLanguageTool langTool = new JLanguageTool(Language.POLISH);
	    //correct
	    matches = rule.match(langTool.getAnalyzedSentence("To jest zdanie próbne."));
	    assertEquals(0, matches.length);
	    //repeated prepositions, don't count'em
	    matches = rule.match(langTool.getAnalyzedSentence("Na dyskotece tańczył jeszcze, choć był na bani."));
	    assertEquals(0, matches.length);
	    //incorrect
	    matches = rule.match(langTool.getAnalyzedSentence("Był on bowiem pięknym strzelcem bowiem."));
	    assertEquals(1, matches.length);
	    matches = rule.match(langTool.getAnalyzedSentence("Mówiła długo, żeby tylko mówić długo."));
	    assertEquals(2, matches.length);
	}

}
