/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sr.jekavian;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.JekavianSerbian;
import org.languagetool.rules.Rule;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

public class MorfologikJekavianSpellerRuleTest {

  private Rule rule;
  private JLanguageTool languageTool;

  @Before
  public void setUp() throws Exception {
    rule = new MorfologikJekavianSpellerRule(TestTools.getMessages("sr"), new JekavianSerbian(), null, Collections.emptyList());
    languageTool = new JLanguageTool(new JekavianSerbian());
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    // correct sentences
    assertEquals(0, rule.match(languageTool.getAnalyzedSentence("Тамо је лијеп цвијет.")).length);
    assertEquals(0, rule.match(languageTool.getAnalyzedSentence("Дјечак и дјевојчица играју се заједно.")).length);
    // Punctuation
    assertEquals(0, rule.match(languageTool.getAnalyzedSentence(",")).length);
    // Roman numerals
    assertEquals(0, rule.match(languageTool.getAnalyzedSentence("III")).length);
  }
  
  @Test
  public void testSpellingCheck() throws IOException {
    assertEquals(1, rule.match(languageTool.getAnalyzedSentence("Misspelled.")).length);
  }
}