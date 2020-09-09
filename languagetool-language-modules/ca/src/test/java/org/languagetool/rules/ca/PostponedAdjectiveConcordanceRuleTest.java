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
public class PostponedAdjectiveConcordanceRuleTest {

  private PostponedAdjectiveConcordanceRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws IOException {
    rule = new PostponedAdjectiveConcordanceRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    
    //de l'altra més neguitós
    //per primera vegada documentat
    //en alguns casos documentat
    //en tot cas poc honesta
    //amb la mirada de cadascun dels homes clavada en la de l'adversari
    //de fer una torre o fortificació bo i al·legant que això
    // a confondre en un mateix amor amics i enemics
    // es van posar en camí proveïts de presents
    /* d'una banda tossut i, de l'altra, del tot inepte
     * principis mascle i femella de la foscor//els elements reproductors mascle
     * i femella// les formigues mascle i femella
     */
    /*
     * multiwords: en aparença, en essència,per essència, amb excés,en repòs,
     * amb rapidesa, en algun grau, per molt de temps altres vegades estacionat,
     * en molts casos subordinada?, era al principi instintiva, de moment
     * imperfectament conegudes de llarg menys perfectes, és de totes passades
     * exactament intermèdia, és, en conjunt, gairebé intermèdia en cert grau
     * paral·lela en algun grau en grau lleuger menys distintes han estat de fet
     * exterminades
     */
    // (en especial si hi ha un adverbi entremig: en algun grau més distintes
    //assertCorrect("Es van somriure l'una a l'altra encara dretes, suades i panteixants,");
    //assertCorrect("una combinació de dos o més metalls obtinguda generalment");
    

    // errors:
    assertIncorrect("França mateix ho necessita.");
    assertIncorrect("recull de llegendes i cançons populars en part inventats per ell");
    assertIncorrect("amb dos conjunts territorial diferents entre si");
    assertIncorrect("per mitjà de gàmetes haploides obtingudes per meiosi");
    assertIncorrect("és tan ple d'urgències, tan ple de desitjós materials");
    assertIncorrect("Tesis doctoral");
    assertIncorrect("vaig posar mans a l'obra: a dins de casa mateix vaig cavar un sot per enterrar");
    assertIncorrect("amb alguns motllurats de guixeria retallat");
    assertIncorrect("amb alguns motllurats de guixeria retallades");
    assertIncorrect("Aquella va ser la seva peça mestre.");
    assertIncorrect("La petició de tramitar el cas per lesions dolosa.");
    // policia i justícia són més usualment femenins, encara que poden ser masculins
    assertIncorrect("Especialment en matèria de policia i justícia autonòmics");
    assertIncorrect("amb rigor i honor barrejades.");
    assertIncorrect("hi ha hagut una certa recuperació (3,2%), efecte en part de la descongestió madrilenya cap a les províncies limítrofs de Toledo i Guadalajara.");
    assertIncorrect("Son molt boniques");
    //assertIncorrect("La casa destrossat"); ambigu
    assertIncorrect("pantalons curt o llargs");
    assertIncorrect("sota les grans persianes de color verd recalcada");
    assertIncorrect("sota les grans persianes de color verd recalcat");
    assertIncorrect("sota les grans persianes de color verd recalcats");
    assertIncorrect("Són unes corbes de llum complexos.");
    assertIncorrect("fets moltes vegades inexplicable.");
    assertIncorrect("eren uns fets cada volta més inexplicable");
    assertIncorrect("Unes explotacions ramaderes porcina.");
    // assertIncorrect("amb un rendiment del 5,62%, més alta que el 5,44%");
    // assertIncorrect("un a baix i un altre a dalt identificada amb el símbol");
    // assertIncorrect("un a baix i un altre a dalt identificades amb el símbol");
    // assertIncorrect("En efecte, hi ha consideracions, llavors força comuns");
    assertIncorrect("En efecte, hi ha consideracions llavors força comuns");
    // assertIncorrect("En efecte, hi ha consideracions racistes, llavors força comuns");
    assertIncorrect("amb una alineació impròpiament habituals");
    assertIncorrect("amb una alineació poc habituals");
    assertIncorrect("amb una alineació molt poc habituals");
    // assertIncorrect("Era un home força misteriosa"); -> permet
    // "en pocs anys força hegemònica"
    assertIncorrect("Era un home força misteriosos");
    assertIncorrect("El rei ha trobat l'excusa perfecte.");
    assertIncorrect("El rei ha trobat l'excusa i l'explicació adequats.");
    assertIncorrect("El rei ha trobat l'excusa i l'explicació adequat.");
    assertIncorrect("Les perspectives de futur immediata.");
    assertIncorrect("Les perspectives de futur immediats.");
    assertIncorrect("la llengua i la cultura catalans.");
    assertIncorrect("En una molècula de glucosa i una de fructosa units.");
    assertIncorrect("Un punt de densitat i gravetat infinits.");
    assertIncorrect("Índex de desenvolupament humà i qualitat de vida elevades.");
    // Should be Incorrect, but it is impossible to detect
    // assertIncorrect("Índex de desenvolupament humà i qualitat de vida elevat");
    assertIncorrect("La massa, el radi i la lluminositat llistat per ell.");
    assertIncorrect("La massa, el radi i la lluminositat llistades per ell.");
    
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

}
