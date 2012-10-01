/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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
package org.languagetool.rules.ca;

import java.io.IOException;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

/**
 * @author Jaume Ortolà
 */
public class ReflexiveVerbsRuleTest extends TestCase {

	private ReflexiveVerbsRule rule;
	private JLanguageTool langTool;

	@Override
	public void setUp() throws IOException {
		rule = new ReflexiveVerbsRule(null);
		langTool = new JLanguageTool(Language.CATALAN);
	}

	public void testRule() throws IOException { 

		// correct sentences:
		assertCorrect("comencen queixant-se");
		assertCorrect("comenceu a queixar-vos");
		assertCorrect("em puc queixar");
		assertCorrect("en teniu prou amb queixar-vos");
		assertCorrect("ens en podem queixar");
		assertCorrect("es queixa");
		assertCorrect("es va queixant");
		//assertCorrect("es va desfent");
		assertCorrect("es va queixar");
		assertCorrect("has d'emportar-t'hi");
		assertCorrect("has de poder-te queixar");
		assertCorrect("t'has de poder queixar");
		assertCorrect("havent-se queixat");
		assertCorrect("haver-se queixat");
		assertCorrect("no es va poder emportar");
		assertCorrect("no has de poder-te queixar");
		assertCorrect("no has de queixar-te");
		assertCorrect("no podeu deixar de queixar-vos");
		assertCorrect("no t'has de queixar");
		assertCorrect("no us podeu deixar de queixar");
		assertCorrect("pareu de queixar-vos");
		assertCorrect("podent abstenir-se");
		assertCorrect("poder-se queixar");
		assertCorrect("podeu queixar-vos");
		assertCorrect("queixa't");
		assertCorrect("queixant-vos");
		assertCorrect("queixar-se");
		assertCorrect("queixeu-vos");
		assertCorrect("s'ha queixat");
		assertCorrect("se li ha queixat");
		assertCorrect("se li queixa");
		assertCorrect("se li va queixar");
		assertCorrect("va decidir suïcidar-se");
		assertCorrect("va queixant-se");
		assertCorrect("va queixar-se");
		assertCorrect("va queixar-se-li");
		assertCorrect("Se'n pujà al cel");
		assertCorrect("Se li'n va anar la mà");
		assertCorrect("El nen pot callar");
		

		// errors:
		assertIncorrect("L'home es marxà de seguida");
		assertIncorrect("El nen es cau");
		assertIncorrect("El nen se li cau");
		assertIncorrect("El nen s'ha de caure");
		assertIncorrect("El nen pot caure's");
		assertIncorrect("Calleu-vos");
		assertIncorrect("Es pujà al cel");
		assertIncorrect("Va baixar-se del cotxe en marxa.");
		assertIncorrect("Se li va anar la mà");
		assertIncorrect("comencen queixant");
		assertIncorrect("comenceu a queixar-nos");
		assertIncorrect("et puc queixar");
		assertIncorrect("en teniu prou amb queixar");
		assertIncorrect("en podem queixar");
		assertIncorrect("et queixa");
		assertIncorrect("em va queixant");
		assertIncorrect("li va queixar");
		assertIncorrect("hem d'emportar-t'hi");
		assertIncorrect("heu de poder-te queixar");
		assertIncorrect("m'has de poder queixar");
		assertIncorrect("havent queixat");
		assertIncorrect("haver queixat");
		assertIncorrect("no es vam poder emportar");
		assertIncorrect("no has de poder-vos queixar");
		assertIncorrect("no has de queixar-ne");
		assertIncorrect("no podeu deixar de queixar-ne");
		assertIncorrect("no li has de queixar");
		assertIncorrect("no em podeu deixar de queixar");
		assertIncorrect("pareu de queixar-se'n");
		assertIncorrect("podent abstenir");
		assertIncorrect("poder queixar");
		assertIncorrect("podeu queixar");
		assertIncorrect("queixa'n");
		assertIncorrect("queixant");
		assertIncorrect("queixar");
		assertIncorrect("queixeu-se'n");
		assertIncorrect("de n'ha queixat");
		assertIncorrect("me li ha queixat");
		assertIncorrect("te li queixa");
		assertIncorrect("us li va queixar");
		assertIncorrect("va decidir suïcidar-me");
		assertIncorrect("va queixant");
		assertIncorrect("va queixar");
		assertIncorrect("va queixar-li");

	}

	private void assertCorrect(String sentence) throws IOException {
		final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
		assertEquals(0, matches.length);
	}

	private void assertIncorrect(String sentence) throws IOException {
		final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
		assertEquals(1, matches.length);
	}

}
