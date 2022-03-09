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

package org.languagetool.rules.es;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SpanishWordRepeatRuleTest {

  /*
   * Test method for
   * 'org.languagetool.rules.es.SpanishWordRepeatRule.match(AnalyzedSentence)'
   */
  @Test
  public void testRule() throws IOException {
    final SpanishWordRepeatRule rule = new SpanishWordRepeatRule(TestTools.getMessages("ca"), new Spanish());
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Spanish());
    // correct
    matches = rule.match(lt.getAnalyzedSentence("Bienvenido/a a LanguageTool."));
    assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("HUCHA-GANGA.ES es la web de referencia."));
    assertEquals(0, matches.length);
  }

}
