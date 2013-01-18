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
package org.languagetool.rules;

import java.io.IOException;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

public class UppercaseSentenceStartRuleTest extends TestCase {

  public void testNonSentences() throws IOException {
    // In OO/LO we get text per paragraph, and list items are a paragraph.
    // Make sure the items that don't look like a sentence generate no error.
    final JLanguageTool lt = new JLanguageTool(Language.ENGLISH);
    
    assertEquals(0, lt.check("a list item").size());
    assertEquals(0, lt.check("a list item,").size());
    assertEquals(0, lt.check("with trailing whitespace, ").size());
    assertEquals(0, lt.check("a list item;").size());
    assertEquals(0, lt.check("A sentence.").size());
    assertEquals(0, lt.check("A sentence!").size());

    assertEquals(1, lt.check("a sentence.").size());
    assertEquals(1, lt.check("a sentence!").size());
  }
  
  public void testRule() throws IOException {
    final JLanguageTool lt = new JLanguageTool(Language.GERMAN);
    
    assertEquals(0, lt.check("Dies ist ein Satz. Und hier kommt noch einer").size());
    assertEquals(0, lt.check("Dies ist ein Satz. Ätsch, noch einer mit Umlaut.").size());
    assertEquals(0, lt.check("Dieser Satz ist bspw. okay so.").size());
    assertEquals(0, lt.check("Dieser Satz ist z.B. okay so.").size());
    assertEquals(0, lt.check("Dies ist ein Satz. \"Aber der hier auch!\".").size());
    assertEquals(0, lt.check("\"Dies ist ein Satz!\"").size());
    assertEquals(0, lt.check("'Dies ist ein Satz!'").size());
    
    assertEquals(0, lt.check("Sehr geehrte Frau Merkel,\nwie wir Ihnen schon früher mitgeteilt haben...").size());
    assertEquals(0, lt.check("Dies ist ein Satz. aber das hier noch nicht").size());
    
    assertEquals(1, lt.check("Dies ist ein Satz. ätsch, noch einer mit Umlaut.").size());
    assertEquals(1, lt.check("Dies ist ein Satz. \"aber der hier auch!\"").size());
    assertEquals(1, lt.check("Dies ist ein Satz. „aber der hier auch!“").size());
    assertEquals(1, lt.check("\"dies ist ein Satz!\"").size());
    assertEquals(1, lt.check("'dies ist ein Satz!'").size());

    final JLanguageTool ltEnglish = new JLanguageTool(Language.ENGLISH);
    assertEquals(0, ltEnglish.check("In Nov. next year.").size());
  }

  public void testDutchSpecialCases() throws IOException {
    final JLanguageTool lt = new JLanguageTool(Language.DUTCH);
    
    assertEquals(1, lt.check("A sentence.").size());
    assertEquals(0, lt.check("'s Morgens...").size());

    assertEquals(2, lt.check("a sentence.").size());
    assertEquals(1, lt.check("'s morgens...").size());
    assertEquals(2, lt.check("s sentence.").size());
  }
  
  public void testPolishSpecialCases() throws IOException {
    final JLanguageTool lt = new JLanguageTool(Language.POLISH);
    
    assertEquals(0, lt.check("Zdanie.").size());
    assertEquals(0, lt.check("To jest lista punktowana:\n\npunkt pierwszy,\n\npunkt drugi,\n\npunkt trzeci.").size());
  }

  public void testUkrainian() throws IOException {
    final UppercaseSentenceStartRule rule = new UppercaseSentenceStartRule(TestTools.getEnglishMessages(), Language.UKRAINIAN);
    final JLanguageTool lt = new JLanguageTool(Language.UKRAINIAN);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Автор написав це речення з великої літери.")).length);

    final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("автор написав це речення з маленької літери."));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals("Автор", matches[0].getSuggestedReplacements().get(0));
    
    assertEquals(0, lt.check("Це список з декількох рядків:\n\nрядок 1,\n\nрядок 2,\n\nрядок 3.").size());
    assertEquals(0, lt.check("Це список з декількох рядків:\n\nрядок 1;\n\nрядок 2;\n\nрядок 3.").size());
    assertEquals(0, lt.check("Це список з декількох рядків:\n\n 1) рядок 1;\n\n2) рядок 2;\n\n3)рядок 3.").size());
  }

}
