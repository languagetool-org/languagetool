/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.German;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MorfologikGermanyGermanSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    final MorfologikGermanyGermanSpellerRule rule =
          new MorfologikGermanyGermanSpellerRule(TestTools.getMessages("English"), new German());
    final JLanguageTool langTool = new JLanguageTool(new German());

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier stimmt jedes Wort!")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Hir nicht so ganz.")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Überall äußerst böse Umlaute!")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Üperall äußerst böse Umlaute!")).length);
  }
  
}
