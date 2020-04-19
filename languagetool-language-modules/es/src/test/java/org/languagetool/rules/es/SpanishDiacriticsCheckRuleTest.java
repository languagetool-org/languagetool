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
package org.languagetool.rules.es;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class SpanishDiacriticsCheckRuleTest {

  private SpanishDiacriticsCheckRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws IOException {
    rule = new SpanishDiacriticsCheckRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Spanish());
  }

  @Test
  public void testRule() throws IOException {

    
    
    // correct sentences:
    assertCorrect("y termino señor Presidente");
    assertCorrect("Cuando participe de la manera que sea");
    assertCorrect("el Parlamento solicita a la Comisión");
    assertCorrect("Juan Pablo II beatifica Paula Montal.");
    assertCorrect("La magnífica conservación del palacio.");
    assertCorrect("Ella maquina alguna idea.");
    assertCorrect("según estipula el apartado 9");
    assertCorrect("pero yo debo velar por que se desarrolle");
    assertCorrect("Pero solicito gustoso");
    assertCorrect("Por este motivo solicito desde ahora");
    assertCorrect("Pero solicito gustoso");
    assertCorrect("que la gente joven participe de nuevo en la rehabilitación");
    assertCorrect("Cuando todos decimos que estamos contra la xenofobia");
    assertCorrect("Todos decimos, sí, necesitamos nuevas fuentes");
    assertCorrect("por lo tanto solicito de la Comisión");
    assertCorrect("unidad especial como solicita dicha enmienda");
    assertCorrect("lo que no critico pues esta es su tarea");
    assertCorrect("de que dentro de seis meses decimos y hacemos");
    assertCorrect("En los Países Bajos decimos algo así como");
    assertCorrect("Todos juntos decimos sí a la política.");
    assertCorrect("y con esto termino señor Presidente");
    assertCorrect("pero al mismo tiempo indico que no puede ser");
    assertCorrect("a que Libia participe de hecho y derecho");
    assertCorrect("espero que Suecia participe pronto en la cooperación");
    assertCorrect("más joven y dinámica participe de un modo muy significativo");
    
    

    // incorrect sentences:
    /*assertIncorrect("de entrada el medico diagnosticó");
    assertIncorrect("El publico deberá tener");
    assertIncorrect("Fue participe de la operación");
    assertIncorrect("la formula de inspiración americana");
    assertIncorrect("La maquina del tiempo.");
    assertIncorrect("Una maquina del tiempo.");
    assertIncorrect("El arbitro se equivocó pitando el penalti.");
    assertIncorrect("La ultima consideración.");
    assertIncorrect("Fue un filosofo romántico.");
    assertIncorrect("Hace tareas especificas.");
    assertIncorrect("Un hombre adultero.");
    assertIncorrect("Hizo una magnifica interpretación.");
    assertIncorrect("La magnifica conservación del palacio.");
    assertIncorrect("Hace falta una nueva formula que la sustituya.");*/
    
    //TODO assertIncorrect("El termino."); por el ejercito; en el dialogo
    
    assertIncorrect("Formación del vehiculo en");
    assertIncorrect("Formación de vehiculo en");
    assertIncorrect("del diagnostico o grado");
    assertIncorrect("haz clic en el numero");
    assertIncorrect("le espera en linea");
    assertIncorrect("el 0456 entre lineas fijas");
    assertIncorrect("Por ultimo, con que");
    assertIncorrect("Agencia sin linea");
    assertIncorrect("con numero de");
    assertIncorrect("en el genero urbano");
    assertIncorrect("Entra en la pagina oficial");
    assertIncorrect("No. de paginas 1");

    /*final RuleMatch[] matches = rule
        .match(langTool
            .getAnalyzedSentence("Las cascaras que nos rodean son cascaras vacías."));
    assertEquals(2, matches.length);*/
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
    final SpanishDiacriticsCheckRule rule = new SpanishDiacriticsCheckRule(TestTools.getEnglishMessages());
    final RuleMatch[] matches;
    final JLanguageTool langTool = new JLanguageTool(new Spanish());

    matches = rule.match(langTool
        .getAnalyzedSentence("de cascaras vacías."));
    assertEquals(3, matches[0].getFromPos());
    assertEquals(11, matches[0].getToPos());
  }

}
