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
package org.languagetool.rules.uk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testUkrainian() throws IOException {
    Ukrainian ukrainian = new Ukrainian();
    UkrainianUppercaseSentenceStartRule rule = new UkrainianUppercaseSentenceStartRule(TestTools.getEnglishMessages(), ukrainian,
        Example.wrong("<marker>речення</marker> має починатися з великої."),
        Example.fixed("<marker>Речення</marker> має починатися з великої"));

    JLanguageTool lt = new JLanguageTool(ukrainian);

    assertEquals(0, rule.match(lt.analyzeText("Автор написав це речення з великої літери.")).length);

    assertEquals(0, rule.match(lt.analyzeText("Він приїхав в с. Вижівка.")).length);

    assertEquals(0, rule.match(lt.analyzeText("         http://narodna.pravda.com.ua")).length);

    RuleMatch[] matches = rule.match(lt.analyzeText("автор написав це речення з маленької літери."));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals("Автор", matches[0].getSuggestedReplacements().get(0));

    assertEquals(new ArrayList<RuleMatch>(), lt.check("Цей список з декількох рядків:\n\nрядок 1,\n\nрядок 2,\n\nрядок 3."));
    assertEquals(0, lt.check("Цей список з декількох рядків:\n\nрядок 1;\n\nрядок 2;\n\nрядок 3.").size());
    assertEquals(0, lt.check("Цей список з декількох рядків:\n\n 1) рядок 1;\n\n2) рядок 2;\n\n3)рядок 3.").size());
    
    
    assertEquals(new ArrayList<RuleMatch>(), lt.check("а) водопі́й"));
  }

}
