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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    TestTools.disableAllRulesExcept(lt, "UPPERCASE_SENTENCE_START");
    
    Assertions.assertEquals(2, lt.check("etwas beginnen. und der auch nicht").size());
    
    Assertions.assertEquals(0, lt.check("schön").size());  // not a real sentence
    Assertions.assertEquals(0, lt.check("Satz").size());
    Assertions.assertEquals(0, lt.check("Dies ist ein Satz. Und hier kommt noch einer").size());
    Assertions.assertEquals(0, lt.check("Dies ist ein Satz. Ätsch, noch einer mit Umlaut.").size());
    Assertions.assertEquals(0, lt.check("Dieser Satz ist bspw. okay so.").size());
    Assertions.assertEquals(0, lt.check("Dieser Satz ist z.B. okay so.").size());
    Assertions.assertEquals(0, lt.check("Dies ist ein Satz. \"Aber der hier auch!\".").size());
    Assertions.assertEquals(0, lt.check("\"Dies ist ein Satz!\"").size());
    Assertions.assertEquals(0, lt.check("'Dies ist ein Satz!'").size());
    
    Assertions.assertEquals(0, lt.check("Sehr geehrte Frau Merkel,\nwie wir Ihnen schon früher mitgeteilt haben...").size());
    //assertEquals(0, lt.check("Dies ist ein Satz. aber das hier noch nicht").size());

    Assertions.assertEquals(1, lt.check("schön!").size());
    Assertions.assertEquals(1, lt.check("Dies ist ein Satz. ätsch, noch einer mit Umlaut.").size());
    Assertions.assertEquals(1, lt.check("Dies ist ein Satz. \"aber der hier auch!\"").size());
    Assertions.assertEquals(1, lt.check("Dies ist ein Satz. „aber der hier auch!“").size());
    Assertions.assertEquals(1, lt.check("\"dies ist ein Satz!\"").size());
    Assertions.assertEquals(1, lt.check("'dies ist ein Satz!'").size());

    // Test soft hyphen removal / position fixing:
    List<RuleMatch> matches0 = lt.check("Ein Test. was?");  // no soft hyphen yet
    Assertions.assertEquals(1, matches0.size());
    Assertions.assertEquals(10, matches0.get(0).getFromPos());
    Assertions.assertEquals(13, matches0.get(0).getToPos());

    List<RuleMatch> matches1 = lt.check("Ein \u00ADTest. was?");
    Assertions.assertEquals(1, matches1.size());
    Assertions.assertEquals(11, matches1.get(0).getFromPos());
    Assertions.assertEquals(14, matches1.get(0).getToPos());

    List<RuleMatch> matches2 = lt.check("Ein \u00ADTe\u00ADst. was?");
    Assertions.assertEquals(1, matches2.size());
    Assertions.assertEquals(12, matches2.get(0).getFromPos());
    Assertions.assertEquals(15, matches2.get(0).getToPos());

    List<RuleMatch> matches3 = lt.check("Ein \u00ADTe\u00ADst. Te\u00ADst. was?");
    Assertions.assertEquals(1, matches3.size());
    Assertions.assertEquals(19, matches3.get(0).getFromPos());
    Assertions.assertEquals(22, matches3.get(0).getToPos());
  }

}
