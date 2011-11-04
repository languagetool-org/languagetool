/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules.sk;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author Zdenko Podobný
 */
public class SlovakVesRuleTest extends TestCase {

  public void testRule() throws IOException {
    final SlovakVesRule rule = new SlovakVesRule(null);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.SLOVAK);

    //correct
    matches = rule.match(langTool.getAnalyzedSentence("Navštívil Záhorskú Ves."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Aj v Karlovej Vesi sme doma."));
    assertEquals(0, matches.length);
    //incorrect
    matches = rule.match(langTool.getAnalyzedSentence("Spišská nová Ves je ešte ďaleko."));
    //TODO: this doesn't work:
    //assertEquals(1, matches.length);
  }

}
