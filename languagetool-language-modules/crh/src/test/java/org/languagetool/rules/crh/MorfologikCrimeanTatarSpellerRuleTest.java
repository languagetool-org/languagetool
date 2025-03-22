/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski
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
package org.languagetool.rules.crh;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.CrimeanTatar;

public class MorfologikCrimeanTatarSpellerRuleTest {
  private JLanguageTool langTool;
  private MorfologikCrimeanTatarSpellerRule rule;
  
  @Before
  public void init() throws IOException {
    rule = new MorfologikCrimeanTatarSpellerRule (TestTools.getMessages("crh"), new CrimeanTatar(), null, Collections.emptyList());
    langTool = new JLanguageTool(new CrimeanTatar());
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    assertEquals(Arrays.asList(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("abadlarnı amutlarıñ!"))));

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("aaabadlarnı")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("abadanlaşırlar")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("meraba")).length);
    
//    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Бафусамы")).length);

//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Бафффусамы")).length);
  }

}
