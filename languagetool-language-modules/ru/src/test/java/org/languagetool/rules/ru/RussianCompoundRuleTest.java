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
    lt = new JLanguageTool(new Russian());
    rule = new RussianCompoundRule(TestTools.getEnglishMessages(), new Russian(), null);
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "Он вышел из-за дома.");
    check(0, "Разработка ПО за идею.");
    // Both  suggestion for some words:
    check(0, "естественно-научный");
    // incorrect sentences:
    check(1, "из за", "из-за");
    check(1, "по за", "по-за");
    check(1, "нет нет из за да да");
    //FIXME: suggestions / longest match
    check(1, "Ростов на Дону", "Ростов-на-Дону");
    check(1, "Ростов на Дону — крупнейший город на юге Российской Федерации, административный центр Южного федерального округа и Ростовской области.");
    // no hyphen suggestion for some words:
    check(1, "кругло суточный", "круглосуточный");
    // also must not accept incorrect upper/lowercase spelling:
    check(0, "Ростов на дону");
    check(0, "Ведь сейчас в лос Анджелесе");
    // also detect an error if only some of the hyphens are missing:
    check(1, "Ростов-на Дону", "Ростов-на-Дону");
    // first part is a single character:
    check(0, "во-первых");
    check(1, "во первых", "во-первых");
    check(1, "Лос Анджелес", "Лос-Анджелес");
    check(1, "Ведь сейчас в Лос Анджелесе");
    check(1, "Ведь сейчас в Лос Анджелесе хорошая погода.");
    check(1, "Во первых, мы были довольно высоко над уровнем моря.");
    check(1, "Мы, во первых, были довольно высоко над уровнем моря.");
    // incorrect sentences:
  }
  
}
