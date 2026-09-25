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

package org.languagetool.rules.ca;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CatalanPhraseRepeatRuleTest {


  final JLanguageTool lt = new JLanguageTool(Catalan.getInstance());
  final CatalanPhraseRepeatRule rule = new CatalanPhraseRepeatRule(TestTools.getMessages("ca"), Catalan.getInstance());

  /*
   * Test method for 'org.languagetool.rules.ca.CatalanPhraseRepeatRuleTest.match(AnalyzedSentence)'
   */
  @Test
  public void testRule() throws IOException {

    /*
    aquesta és la meitat de la de la població en general
     */

    //correct
    assertCorrect("sobretot, que dubta, que dubta constantment");
    assertCorrect("no deixava de tenir febre i més febre i més febre");
    assertCorrect("una vegada i una altra i una altra i una altra. ");
    assertCorrect("trobava aquest títol o aquest o aquest altre,");
    assertCorrect(" Que sí, que sí, que ho heu llegit bé.");
    assertCorrect("—Molt bé, molt bé, tampoc cal que et posis així ");
    assertCorrect("però la mort –la Mort–,");
    assertCorrect("Que…, que…, ja hi ha les entrades?");
    assertCorrect("Que et penses que parlant descobrirem res de res de per què som aquí?");
    assertCorrect("Què es podia esperar d'un… d'un…?");
    assertCorrect("Imperfet de subjuntiu [é] [é] [í]");
    assertCorrect("Soc molt vella molt vella, però que encara hi soc.");

    assertCorrect("No volia res més que mirar i mirar i mirar.");
    assertCorrect("A més a més, ho va fer a poc a poc.");
    assertCorrect("A diferència dels dels ocells.");
    assertCorrect("Puja pas a pas a les ruïnes");
    assertCorrect("No cal posar 'a' a tot arreu.");
    assertCorrect("No volia res més que mirar i mirar i mirar.");
    assertCorrect("A més a més, ho va fer a poc a poc.");
    assertCorrect("A diferència dels dels ocells.");
    assertCorrect("Puja pas a pas a les ruïnes");
    assertCorrect("estava malalt de bo de bo");
    assertCorrect("Ve ací de tant en tant en lloc d'anar allà");
    assertCorrect("Sigui qui sigui qui vingui.");
    assertCorrect("D'hereu a hereu a través de generacions.");
    assertCorrect("De dos en dos en l'arbre.");
    assertCorrect("No n'hi havia ni massa ni massa pocs");
    assertCorrect("Podia tractar qualsevol persona de tu a tu a qualsevol nivell.");
    assertCorrect("Eren xifres molt -molt- modestes.");
    assertCorrect("Revisa milions de milions de dades en pocs segons.");
    assertCorrect(" © © © © ©");
    assertCorrect(" © © © © © © © ©");
    assertCorrect("p=(16+16+1+1)");
    assertCorrect("p=(16 + + 16 + +)");
    assertCorrect("16 15 15 16 15 15");
    assertCorrect("16 15 16 15 16 15");
    assertCorrect("~~~~");
    assertCorrect("~~~~~~~~");
    assertCorrect("l'honestedat d’alguns ˗alguns˗ humans.");
    assertCorrect("Mader | Andorra | Andorra La Vella ");
    assertCorrect("==== Arc de Zou ====");
    assertCorrect("============");
    assertCorrect("=r=r[s[i]]||{}");
    assertCorrect("Serveis que ofereix el CRAI > Préstec > Préstec, reserves");
    assertCorrect("total de folis: 166 (III + III + I+ 156 + III) ff.");
    assertCorrect("📚📚📚📚📚📚📚📚📚📚");

    //incorrect
    assertIncorrect("Benvinguts a casa a casa meva.", "a casa");
    assertIncorrect("Benvinguts a la a la casa.", "a la");
    assertIncorrect("Benvinguts a casa meva a casa meva.", "a casa meva");
    assertIncorrect("Ho poso com a com a exemple", "com a");
    assertIncorrect("És l'americà l'americà.", "l'americà");
    assertIncorrect("És d'aconseguir-los d'aconseguir-los així", "d'aconseguir-los");
    assertIncorrect("La casa és la casa és verda", "La casa és");

  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence, String suggestion) throws IOException {
    final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertNotEquals(0, matches.length);
    assertEquals(suggestion, matches[0].getSuggestedReplacements().get(0));
  }

}
