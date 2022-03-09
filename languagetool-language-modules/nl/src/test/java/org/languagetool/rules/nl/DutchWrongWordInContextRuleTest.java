/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Markus Brenneis
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
package org.languagetool.rules.nl;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Dutch;

import java.io.IOException;

/**
 * @author Markus Brenneis
 */
public class DutchWrongWordInContextRuleTest {

  @Test
  @Ignore("no tests yet")
  public void testRule() throws IOException {
    DutchWrongWordInContextRule rule = new DutchWrongWordInContextRule(null);
    JLanguageTool lt = new JLanguageTool(new Dutch());

    // Mine/Miene (example, can be removed)
//     assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Explosion der Mine.")).length); // correct sentence
//     assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Mienen sind gestern Abend explodiert.")).length); // wrong sentence
//     assertEquals("Minen", rule.match(langTool.getAnalyzedSentence("Er hat das mit den Mienen weggesprengt."))[0].getSuggestedReplacements().get(0)); // test suggestion
  }
  
}
