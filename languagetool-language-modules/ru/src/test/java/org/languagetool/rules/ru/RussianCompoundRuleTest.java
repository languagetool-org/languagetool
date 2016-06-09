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
package org.languagetool.rules.ru;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Russian;
import org.languagetool.rules.AbstractCompoundRuleTest;

import java.io.IOException;

/**
 * Russian Compound rule test
 * @author Yakov Reztsov 
 * Based on German Compound rule test
 * @author Daniel Naber
 */
public class RussianCompoundRuleTest extends AbstractCompoundRuleTest {

  @Before
  public void setUp() throws Exception {
    langTool = new JLanguageTool(new Russian());
    rule = new RussianCompoundRule(TestTools.getEnglishMessages());
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "Он вышел из-за дома.");
    // Both  suggestion for some words:
    check(0, "естественно-научный");
    // incorrect sentences:
    check(1, "из за", new String[]{"из-за"});
    check(1, "нет нет из за да да");
    //FIXME: suggestions / longest match
    check(1, "Ростов на Дону", new String[]{"Ростов-на-Дону"});
    // no hyphen suggestion for some words:
    check(1, "кругло суточный", new String[]{"круглосуточный"});
    // also accept incorrect upper/lowercase spelling:
    check(1, "Ростов на дону", new String[]{"Ростов-на-дону"});
    // also detect an error if only some of the hyphens are missing:
    check(1, "Ростов-на Дону", new String[]{"Ростов-на-Дону"});
    // first part is a single character:
    check(0, "во-первых");
    check(1, "во первых", new String[]{"во-первых"});
  }
  
}
