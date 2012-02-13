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
import java.util.List;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

/**
 * @author Daniel Naber
 */
public class UppercaseSentenceStartRuleTest extends TestCase {

  public void testRule() throws IOException {
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    List<RuleMatch> matches;
    
    matches = langTool.check("Dies ist ein Satz. Und hier kommt noch einer");
    assertEquals(0, matches.size());
    matches = langTool.check("Dies ist ein Satz. Ätsch, noch einer mit Umlaut.");
    assertEquals(0, matches.size());
    matches = langTool.check("Dieser Satz ist bspw. okay so.");
    assertEquals(0, matches.size());
    matches = langTool.check("Dieser Satz ist z.B. okay so.");
    assertEquals(0, matches.size());
    matches = langTool.check("Dies ist ein Satz. \"Aber der hier auch!\".");
    assertEquals(0, matches.size());
    matches = langTool.check("\"Dies ist ein Satz!\"");
    assertEquals(0, matches.size());
    matches = langTool.check("'Dies ist ein Satz!'");
    assertEquals(0, matches.size());
    
    matches = langTool.check("Sehr geehrte Frau Merkel,\nwie wir Ihnen schon früher mitgeteilt haben...");
    assertEquals(0, matches.size());

    matches = langTool.check("Dies ist ein Satz. und hier kommt noch einer");
    assertEquals(1, matches.size());
    matches = langTool.check("Dies ist ein Satz. ätsch, noch einer mit Umlaut.");
    assertEquals(1, matches.size());
    matches = langTool.check("Dies ist ein Satz. \"aber der hier auch!\"");
    assertEquals(1, matches.size());
    matches = langTool.check("\"dies ist ein Satz!\"");
    assertEquals(1, matches.size());
    matches = langTool.check("'dies ist ein Satz!'");
    assertEquals(1, matches.size());

    langTool = new JLanguageTool(Language.ENGLISH);
    matches = langTool.check("In Nov. next year.");
    assertEquals(0, matches.size());
  }

  public void testDutchSpecialCases() throws IOException {
    JLanguageTool langTool = new JLanguageTool(Language.DUTCH);
    List<RuleMatch> matches;
    
    matches = langTool.check("A sentence.");
    assertEquals(0, matches.size());
    matches = langTool.check("'s Morgens...");
    assertEquals(0, matches.size());

    matches = langTool.check("a sentence.");
    assertEquals(1, matches.size());
    matches = langTool.check("'s morgens...");
    assertEquals(1, matches.size());
    matches = langTool.check("s sentence.");
    assertEquals(1, matches.size());
  }
  
  public void testPolishSpecialCases() throws IOException {
    JLanguageTool langTool = new JLanguageTool(Language.POLISH);
    List<RuleMatch> matches;
    
    matches = langTool.check("Zdanie.");
    assertEquals(0, matches.size());
    matches = langTool.check("To jest lista punktowana:\n\npunkt pierwszy,\n\npunkt drugi,\n\npunkt trzeci.");
    assertEquals(0, matches.size());
  }

  public void testUkrainian() throws IOException {
    final UppercaseSentenceStartRule rule = new UppercaseSentenceStartRule(TestTools.getEnglishMessages(), Language.UKRAINIAN);

    final JLanguageTool langTool = new JLanguageTool(Language.UKRAINIAN);

    // correct sentences:
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Автор написав це речення з великої літери."));
    assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("автор написав це речення з маленької літери."));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals("Автор", matches[0].getSuggestedReplacements().get(0));
  }

}
