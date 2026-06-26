/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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

package org.languagetool.rules.ca;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class SimpleReplaceAnglicismTest {

  private SimpleReplaceAnglicism rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    lt = new JLanguageTool(Catalan.getInstance());
    rule = new SimpleReplaceAnglicism(TestTools.getMessages("ca"));
  }

  @Test
  public void testRule() throws IOException {

    // frases correctes:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Això és un zombi molt perillós.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("El pòdcast d'avui ha estat molt interessant.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Hem rebut molts tiquets de suport.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("La bretxa digital és un problema greu.")).length);

    // frases incorrectes — adaptacions gràfiques:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("El zombie era molt perillós."));
    assertEquals(1, matches.length);
    assertEquals("[El zombi]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Els zombies eren molt perillosos."));
    assertEquals(1, matches.length);
    assertEquals("[Els zombis]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("He sentit el podcast."));
    assertEquals(1, matches.length);
    assertEquals("[el pòdcast]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("He sentit els podcasts."));
    assertEquals(1, matches.length);
    assertEquals("[els pòdcasts]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Tenim un ticket de suport obert."));
    assertEquals(1, matches.length);
    assertEquals("[un tiquet]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Tenim dos tickets de suport oberts."));
    assertEquals(1, matches.length);
    assertEquals("[dos tiquets]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("El troll va inundar el fòrum de missatges."));
    assertEquals(1, matches.length);
    assertEquals("[El trol]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("El footing és un esport popular."));
    assertEquals(1, matches.length);
    assertEquals("[El fúting]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Hi ha un canvi en l'snorkeling gratuït."));
    assertEquals(1, matches.length);
    assertEquals("[l'esnòrquel gratuït, la immersió lleugera gratuïta]", matches[0].getSuggestedReplacements().toString());

    // frases incorrectes — anglicismes innecessaris:
    matches = rule.match(lt.getAnalyzedSentence("El spam és un problema al correu electrònic."));
    assertEquals(1, matches.length);
    assertEquals("[El correu brossa, El contingut brossa]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Hi ha un gap important entre els salaris."));
    assertEquals(1, matches.length);
    assertEquals("[una bretxa important]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("El cringe que em va fer aquell moment."));
    assertEquals(1, matches.length);
    assertEquals("[L'angúnia, La vergonya, La incomoditat]", matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Ens van programar uns briefings."));
    assertEquals(1, matches.length);
    assertEquals("[unes sessions informatives, unes reunions informatives, uns reports, uns brífings]", matches[0].getSuggestedReplacements().toString());
    assertEquals(18, matches[0].getFromPos());
    assertEquals(31, matches[0].getToPos());

    // frases incorrectes — sintagmes multiparaula (el filtre de gènere/nombre no s'aplica):
    matches = rule.match(lt.getAnalyzedSentence("El vol era low cost."));
    assertEquals(1, matches.length);
    assertEquals("[baix cost, de baix cost, barat, barats]", matches[0].getSuggestedReplacements().toString());
    assertEquals(11, matches[0].getFromPos());
    assertEquals(19, matches[0].getToPos());

    matches = rule.match(lt.getAnalyzedSentence("Farem el seminari online."));
    assertEquals(1, matches.length);
    assertEquals("[en línia, digital, electrònic, connectat, per internet, en remot, en internet]",
      matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Farem el seminari on line."));
    assertEquals(1, matches.length);
    assertEquals(18, matches[0].getFromPos());
    assertEquals(25, matches[0].getToPos());
    assertEquals("[en línia, digital, electrònic, connectat, per internet, en remot, en internet]",
      matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Farem el seminari on-line."));
    assertEquals(1, matches.length);
    assertEquals(18, matches[0].getFromPos());
    assertEquals(25, matches[0].getToPos());
    assertEquals("[en línia, digital, electrònic, connectat, per internet, en remot, en internet]",
      matches[0].getSuggestedReplacements().toString());

    matches = rule.match(lt.getAnalyzedSentence("Necessitem el know-how necessari per fer-ho."));
    assertEquals(1, matches.length);
    assertEquals(14, matches[0].getFromPos());
    assertEquals(22, matches[0].getToPos());
    assertEquals("[saber fer]", matches[0].getSuggestedReplacements().toString());

    // majúscula a l'inici de frase:
    matches = rule.match(lt.getAnalyzedSentence("Zombie és el nom de la pel·lícula."));
    assertEquals(1, matches.length);
    assertEquals("[Zombi]", matches[0].getSuggestedReplacements().toString());

    // diverses coincidències en una mateixa frase:
    matches = rule.match(lt.getAnalyzedSentence("El spam i el troll fan malbé les discussions en línia."));
    assertEquals(2, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("La primera masterclass serà la de literatura barroca."));
    assertEquals(1, matches.length);
    assertEquals("[La primera classe magistral, Les primeres classes magistrals]", matches[0].getSuggestedReplacements().toString());

  }

}
