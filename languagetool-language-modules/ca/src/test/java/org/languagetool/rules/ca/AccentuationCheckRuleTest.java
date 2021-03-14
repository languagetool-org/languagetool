/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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
public class AccentuationCheckRuleTest {

  private AccentuationCheckRule rule;
  private JLanguageTool         langTool;

  @Before
  public void setUp() throws IOException {
    rule = new AccentuationCheckRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertCorrect("I la faria desgraciada");
    assertCorrect("jo ja la tenia incorporada abans");
    assertCorrect("tu ja les tenies incorporades abans");
    assertCorrect("Quan anunciïs una votació, especifica de quina es tracta");
    //assertCorrect("Quina classe de política practica la UE.");
    assertCorrect("Tu practiques tècniques de ioga");
    assertCorrect("Ella practica política de fets consumats.");
    assertCorrect("la UE que opera aïllada");
    assertCorrect("Una nova estipula que no es pot fer.");
    assertCorrect("El projecte d'informe anima la Comissió a actuar.");
    assertCorrect("L'article primer estipula que és així.");
    assertCorrect("L'article 1 estipula que és així.");
    assertCorrect("L'informe estipula que ha de ser així.");
    assertCorrect("Si presencies males pràctiques en la botiga.");
    assertCorrect("—I continues mantenint que això va succeir");
    assertCorrect("No hi ha ningú aquí que begui vi?");
    assertCorrect("Va tocar l'ària da capo de les variacions Goldberg.");
    assertCorrect("A ponent continua la serra de Fontpobra");
    assertCorrect("com a base de la categoria faria que els enllaços");
    assertCorrect("De jove faria amistat amb ells");
    assertCorrect("De jove tenia admiració");
    assertCorrect("Per tant, espero ansiós.");
    assertCorrect("M'espero qualsevol cosa.");
    assertCorrect("Carrega de nou l'arxiu.");
    assertCorrect("Espero d'ell moltes coses");
    assertCorrect("cal que abans figuri inscrit en l'Ordre del dia");
    assertCorrect("El lloc era, però, habitat de molt abans,");
    assertCorrect("i del bel canto del rococó.");
    assertCorrect("El lloc fou habitat de forma contínua");
    assertCorrect("que havia estat habitat fins al regnat de Joan II");
    assertCorrect("López-Picó publica el seu llibre de poesies");
    assertCorrect("Vaig perdut, tremolo de por.");
    assertCorrect("l'home marra el camí");
    assertCorrect("veié que darrere venia el deixeble");
    assertCorrect("Però el qui begui de l'aigua");
    assertCorrect("a qualsevol qui en mati un altre");
    assertCorrect("tu que prediques de no robar");
    assertCorrect("els següents territoris externs habitats:");
    assertCorrect("Cap faria una cosa així.");
    assertCorrect("El cos genera suficient pressió interna.");
    assertCorrect("Les seues contràries.");
    assertCorrect("Això és una frase de prova.");
    assertCorrect("Amb renúncies i esforç.");
    assertCorrect("He vingut per a cantar");
    assertCorrect("Són circumstàncies d'un altre caire.");
    assertCorrect("La renúncia del president.");
    assertCorrect("Circumstàncies extraordinàries.");
    assertCorrect("Les circumstàncies que ens envolten.");
    assertCorrect("Ella continua enfadada.");
    assertCorrect("Ell obvia els problemes.");
    assertCorrect("De manera òbvia.");
    assertCorrect("Ell fa tasques específiques.");
    assertCorrect("Un home adúlter.");
    assertCorrect("Jo adulter el resultat.");
    assertCorrect("Va deixar els nens atònits.");
    assertCorrect("La sureda ocupa àmplies extensions en la muntanya.");
    assertCorrect("Féu una magnífica digitació.");
    assertCorrect("La disputa continua oberta.");
    assertCorrect("La llum tarda 22 minuts.");
    assertCorrect("És el tretzè municipi més habitat de la comarca.");
    assertCorrect("Els hàbitats de la comarca.");
    assertCorrect("Joan Pau II beatifica Paula Montal.");
    assertCorrect("La magnífica conservació del palau.");

    // errors:
    //TODO assertIncorrect("una votació especifica de diputats.");
    //TODO assertIncorrect("Opera nova de l'estil.");
    
    assertIncorrect("que les vertebres properes al crani");
    assertIncorrect("Unes circumstancies");
    assertIncorrect("una especifica votació de diputats.");
    assertIncorrect("El millor de la historia.");
    assertIncorrect("El millor d'aquesta historia.");
    assertIncorrect("L'ultima consideració.");
    assertIncorrect("Com s'ha dit les primaries autonòmiques s'han ajornat");
    assertIncorrect("Com sabeu les primaries s'han ajornat");
    assertIncorrect("Les continues al·lusions a la victòria.");
    assertIncorrect("De positiva influencia en ell.");
    assertIncorrect("tren de llarga distancia");
    //assertIncorrect("com la nostra pròpia desgracia");
    //assertIncorrect("la seva influencia");
    assertIncorrect("Cal una nova formula que substitueixi el caduc Estat del benestar.");
    assertIncorrect("Porta-la i nosaltres fem la copia i la compulsem.");
    assertIncorrect("Carrega d'arxius.");
    assertIncorrect("Vaig arribar a fer una radio que no va funcionar mai.");
    assertIncorrect("No em fumaré una faria com feia abans.");
    assertIncorrect("M'he fumat una faria.");
    assertIncorrect("Les seues contraries.");
    assertIncorrect("Amb renuncies i esforç.");
    assertIncorrect("La renuncia del president.");
    assertIncorrect("Són circumstancies d'un altre caire.");
    //assertIncorrect("Circumstancies extraordinàries.");
    assertIncorrect("Les circumstancies que ens envolten.");
    assertIncorrect("De manera obvia.");
    assertIncorrect("Ell fa tasques especifiques.");
    assertIncorrect("Un home adulter.");
    // assertIncorrect("Va deixar els nens atonits."); del v. "atonir" (=esbalair)
    assertIncorrect("La sureda ocupa amplies extensions en la muntanya.");
    assertIncorrect("Féu una magnifica digitació.");
    assertIncorrect("La magnifica conservació del palau.");

    final RuleMatch[] matches = rule
        .match(langTool
            .getAnalyzedSentence("Les circumstancies que ens envolten són unes circumstancies extraordinàries."));
    assertEquals(2, matches.length);
  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool
        .getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool
        .getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
  }

  @Test
  public void testPositions() throws IOException {
    final AccentuationCheckRule rule = new AccentuationCheckRule(TestTools.getEnglishMessages());
    final RuleMatch[] matches;
    final JLanguageTool langTool = new JLanguageTool(new Catalan());

    matches = rule.match(langTool
        .getAnalyzedSentence("El millor de la historia."));
    assertEquals(16, matches[0].getFromPos());
    assertEquals(24, matches[0].getToPos());
  }

}
