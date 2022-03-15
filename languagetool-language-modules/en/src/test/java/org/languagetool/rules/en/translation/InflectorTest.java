/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en.translation;

import org.junit.jupiter.api.Test;

import org.hamcrest.MatcherAssert;

import static org.hamcrest.CoreMatchers.is;

public class InflectorTest {

  @Test
  public void inflect() {
    Inflector inflector = new Inflector();
    MatcherAssert.assertThat(inflector.inflect("pump", "SUB:AKK:PLU:FEM").toString(), is("[pumps]"));  // "Pumpen"
    MatcherAssert.assertThat(inflector.inflect("child", "SUB:NOM:PLU:NEU").toString(), is("[children]"));  // "Kinder"
    MatcherAssert.assertThat(inflector.inflect("walk", "VER:3:SIN:PRÄ:NON").toString(), is("[walks]"));  // "er geht"
    MatcherAssert.assertThat(inflector.inflect("walk", "VER:3:SIN:PRT:NON").toString(), is("[walked]")); // "ging" (-> gehen)
    MatcherAssert.assertThat(inflector.inflect("walk", "PA1:PRD:GRU:VER").toString(), is("[walking]"));  // "gehend"
    MatcherAssert.assertThat(inflector.inflect("walk", "PA2:PRD:GRU:VER").toString(), is("[walked]"));   // "gegangen"
    // forms that are not inflected in English do not need to be supported here, e.g. "geh" (imperativ) is simply "go"

    MatcherAssert.assertThat(inflector.inflect("large", "ADJ:PRD:KOM").toString(), is("[larger]"));   // "größer"
    MatcherAssert.assertThat(inflector.inflect("large", "ADJ:AKK:SIN:FEM:SUP:DEF").toString(), is("[largest]"));   // "größte"

    MatcherAssert.assertThat(inflector.inflect("walk", "VER:1:SIN:PRÄ:NON").toString(), is("[walk]"));  // "ich gehe"
    MatcherAssert.assertThat(inflector.inflect("walk", "VER:2:SIN:PRÄ:NON").toString(), is("[walk]"));  // "du gehst"
    MatcherAssert.assertThat(inflector.inflect("walk", "VER:1:PLU:PRÄ:NON").toString(), is("[walk]"));  // "wir gehen"
    MatcherAssert.assertThat(inflector.inflect("walk", "VER:2:PLU:PRÄ:NON").toString(), is("[walk]"));  // "ihr geht"
    MatcherAssert.assertThat(inflector.inflect("walk", "VER:3:PLU:PRÄ:NON").toString(), is("[walk]"));  // "sie gehen"

    MatcherAssert.assertThat(inflector.inflect("walk", null).toString(), is("[walk]"));
    MatcherAssert.assertThat(inflector.inflect("walk", "").toString(), is("[walk]"));
    MatcherAssert.assertThat(inflector.inflect("walk", "FAKE-TAG").toString(), is("[walk]"));
  }

  @Test
  public void inflectMultiWord() {
    Inflector inflector = new Inflector();
    MatcherAssert.assertThat(inflector.inflect("tire pump", "SUB:AKK:PLU:FEM").toString(), is("[tire pumps]"));  // "Pumpen"
    MatcherAssert.assertThat(inflector.inflect("fake tire pump", "SUB:AKK:PLU:FEM").toString(), is("[fake tire pumps]"));
  }
}
