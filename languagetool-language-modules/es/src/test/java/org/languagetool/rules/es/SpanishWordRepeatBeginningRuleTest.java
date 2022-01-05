/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.es;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

public class SpanishWordRepeatBeginningRuleTest {
  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("es"));
    lt.enableRule("SPANISH_WORD_REPEAT_BEGINNING_RULE"); // even if it is temp_off
    lt.disableRule("ES_REPEATEDWORDS");

    // ================== correct sentences ===================
    // two successive sentences that start with the same non-adverb word.
    assertEquals(0, lt.check("Esto está bien. Esto es mejor, también.").size());
    // three successive sentences that start with the same exception word ("the").
    assertEquals(0, lt.check("El coche. El profesor. El tercer elemento.").size());

    // =================== errors =============================
    assertEquals(2, lt.check("Mañana me voy. Mañana vuelvo. Mañana se acabó todo.").size());

    // three successive sentences that start with the same non-exception word
    List<RuleMatch> matches1 = lt.check("Yo creo. Yo he visto esto antes. Yo no lo creo.");
    assertEquals(1, matches1.size());
    // check suggestions
    assertThat(matches1.get(0).getSuggestedReplacements().get(0), is("Además, yo"));
    assertThat(matches1.get(0).getSuggestedReplacements().get(1), is("Igualmente, yo"));
    assertThat(matches1.get(0).getSuggestedReplacements().get(2), is("No solo eso, sino que yo"));
    List<RuleMatch> matches2 = lt.check("También, juego a fútbol. También, juego a baloncesto.");
    assertEquals(1, matches2.size());
    // check suggestions (because the adverbs are contained in a Set it is safer to
    // check if the correct suggestions
    // are contained in the real suggestions)
    assertThat(matches2.get(0).getSuggestedReplacements().toString(),
        is("[Adicionalmente, Asimismo, Además, Igualmente, Así mismo]"));

    List<RuleMatch> matches3 = lt.check("Sin embargo, me gusta. Sin embargo, otros me gustan más.");
    assertEquals(1, matches3.size());
    // TODO add suggestions
    assertThat(matches3.get(0).getSuggestedReplacements().toString(), is("[]"));

    List<RuleMatch> matches4 = lt.check("Pero me gusta. Pero otros me gustan más.");
    assertEquals(1, matches4.size());
    assertThat(matches4.get(0).getSuggestedReplacements().toString(), is("[Aun así, Por otra parte, Sin embargo]"));
  }

}
