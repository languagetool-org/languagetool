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
package org.languagetool.rules.de;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    TestTools.disableAllRulesExcept(lt, "UPPERCASE_SENTENCE_START");
    
    assertEquals(2, lt.check("etwas beginnen. und der auch nicht").size());
    
    assertEquals(0, lt.check("schön").size());  // not a real sentence
    assertEquals(0, lt.check("Satz").size());
    assertEquals(0, lt.check("Dies ist ein Satz. Und hier kommt noch einer").size());
    assertEquals(0, lt.check("Dies ist ein Satz. Ätsch, noch einer mit Umlaut.").size());
    assertEquals(0, lt.check("Dieser Satz ist bspw. okay so.").size());
    assertEquals(0, lt.check("Dieser Satz ist z.B. okay so.").size());
    assertEquals(0, lt.check("Dies ist ein Satz. \"Aber der hier auch!\".").size());
    assertEquals(0, lt.check("\"Dies ist ein Satz!\"").size());
    assertEquals(0, lt.check("'Dies ist ein Satz!'").size());
    
    assertEquals(0, lt.check("Sehr geehrte Frau Merkel,\nwie wir Ihnen schon früher mitgeteilt haben...").size());
    //assertEquals(0, lt.check("Dies ist ein Satz. aber das hier noch nicht").size());

    assertEquals(1, lt.check("schön!").size());
    assertEquals(1, lt.check("Dies ist ein Satz. ätsch, noch einer mit Umlaut.").size());
    assertEquals(1, lt.check("Dies ist ein Satz. \"aber der hier auch!\"").size());
    assertEquals(1, lt.check("Dies ist ein Satz. „aber der hier auch!“").size());
    assertEquals(1, lt.check("\"dies ist ein Satz!\"").size());
    assertEquals(1, lt.check("'dies ist ein Satz!'").size());

    // Test soft hyphen removal / position fixing:
    List<RuleMatch> matches0 = lt.check("Ein Test. was?");  // no soft hyphen yet
    assertEquals(1, matches0.size());
    assertEquals(10, matches0.get(0).getFromPos());
    assertEquals(13, matches0.get(0).getToPos());

    List<RuleMatch> matches1 = lt.check("Ein \u00ADTest. was?");
    assertEquals(1, matches1.size());
    assertEquals(11, matches1.get(0).getFromPos());
    assertEquals(14, matches1.get(0).getToPos());

    List<RuleMatch> matches2 = lt.check("Ein \u00ADTe\u00ADst. was?");
    assertEquals(1, matches2.size());
    assertEquals(12, matches2.get(0).getFromPos());
    assertEquals(15, matches2.get(0).getToPos());

    List<RuleMatch> matches3 = lt.check("Ein \u00ADTe\u00ADst. Te\u00ADst. was?");
    assertEquals(1, matches3.size());
    assertEquals(19, matches3.get(0).getFromPos());
    assertEquals(22, matches3.get(0).getToPos());
  }

}
