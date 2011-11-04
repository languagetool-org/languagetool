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
package de.danielnaber.languagetool.rules.de;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * @author Daniel Naber
 */
public class WiederVsWiderRuleTest extends TestCase {

  public void testRule() throws IOException {
    WiederVsWiderRule rule = new WiederVsWiderRule(null);
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das spiegelt wider, wie es wieder läuft.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das spiegelt die Situation gut wider.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das spiegelt die Situation.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Immer wieder spiegelt das die Situation.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Immer wieder spiegelt das die Situation wider.")).length);
    // known to match although sentence is okay:
    //assertEquals(0, rule.match(langTool.getAnalyzedText("Das spiegelt wieder wider, wie es läuft.")).length);
    // errors:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das spiegelt wieder, wie es wieder läuft.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das spiegelt die Situation gut wieder.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Immer wieder spiegelt das die Situation wieder.")).length);
  }
    
}
