
/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

public class SpanishRepeatedWordsRuleTest {

  private TextLevelRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new SpanishRepeatedWordsRule(TestTools.getMessages("es"));
    lt = new JLanguageTool(Languages.getLanguageForShortCode("es"));
  }

  @Test
  public void testRule() throws IOException {

    assertCorrectText("Emplearon la fuerza. Pero los empleados se resistieron.");
    assertCorrectText("Antes dije esto. Antes de venir.");
    assertCorrectText("Propuse aquello. Pero la propuesta no fue acceptada.");
    assertCorrectText("Creó cosas interesantes. Pero creo que no eran útiles.");
    assertCorrectText(
        "Fue excelente. El arquitecto de la catedral parece ser que fue el maestro Enrique, seguramente natural de Francia, que ya había trabajado anteriormente en la catedral de Burgos. Es evidente que conocía la forma arquitectónica gótica de la isla de Francia. Falleció en el año 1277 y fue sustituido por el español Juan Pérez. En el año 1289 fallecía también el obispo Martín Fernández, cuando la cabecera del templo ya estaba abierta al culto. La estructura fundamental de la catedral se finaliza pronto, en el año 1302, abriendo el obispo Gonzalo Osorio la totalidad de la iglesia a los fieles, aunque en el siglo xiv aún se terminarían el claustro y la torre norte; la torre sur no se finalizó hasta la segunda mitad del siglo xv. Esta prontitud en el acabamiento de las obras le da una gran unidad de estilo arquitectónico. La catedral de León se inspira en la planta de la catedral de Reims (aunque esta es de menor superficie), que bien pudo conocer el maestro Enrique. Al igual que la mayoría de catedrales francesas, la de León está construida con un módulo geométrico basado en el triángulo (ad triangulum), cuyos miembros se relacionan con la raíz cuadrada de 3, al que responden la totalidad de sus partes y del todo. Este aspecto, como la planta, los alzados, y los repertorios decorativos y simbólicos convierten esta catedral en un auténtico edificio transpirenaico, alejado de la corriente hispánica, que le ha merecido los calificativos de «la más francesa de las catedrales españolas» o el de Pulchra Leonina. Fue excelente.");

    assertCorrectText("Yo propuse aquello. Pero también propuse esto otro.");
    assertCorrectText("Yo propuse aquello. Pero propuse también esto otro.");
    
    RuleMatch[] matches = getRuleMatches(
        "Yo propuse aquello. Pero la sugerencia propuesta por el presidente no fue acceptada.");
    assertEquals(1, matches.length);

    matches = getRuleMatches("Propuse aquello. Pero la sugerencia propuesta por el presidente no fue acceptada.");
    assertEquals(1, matches.length);

    matches = getRuleMatches("Fue excelente. Fue un resultado excelente.");
    assertEquals(1, matches.length);
    assertEquals("magnífico", matches[0].getSuggestedReplacements().get(0));
    assertEquals("fantástico", matches[0].getSuggestedReplacements().get(1));
    assertEquals("maravilloso", matches[0].getSuggestedReplacements().get(2));

    matches = getRuleMatches("Esto propuse. ¿Propones tu algo diferente?");
    assertEquals(1, matches.length);
    assertEquals("Sugieres", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Recomiendas", matches[0].getSuggestedReplacements().get(1));
    
    matches = getRuleMatches("Inicia el debate. Inicia la conversación.");
    assertEquals(1, matches.length);
    assertEquals("Comienza", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Empieza", matches[0].getSuggestedReplacements().get(1));
    assertEquals("Pone en marcha", matches[0].getSuggestedReplacements().get(2));

  }

  private RuleMatch[] getRuleMatches(String sentences) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(sentences).build();
    return rule.match(lt.analyzeText(sentences), aText);
  }

  private void assertCorrectText(String sentences) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(sentences).build();
    RuleMatch[] matches = rule.match(lt.analyzeText(sentences), aText);
    assertEquals(0, matches.length);
  }

}

