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
    
    assertCorrect("los niños solo aman lo desconocido");
    assertCorrect("y al final supero en altura a su padre");
    assertCorrect("¿De qué país vienes?");
    assertCorrect("¡Pero si tú solo vienes aquí durante el verano!");
    assertCorrect("¿Cómo vienes a la escuela?");
    assertCorrect("a cuyos integrantes no catalogo de santos precisamente");
    assertCorrect("esta propuesta de directiva prorroga por un año");
    assertCorrect("El informe d'Ancona formula gran número de recomendaciones");
    assertCorrect("la enmienda n.º 4 duplica gran parte");
    assertCorrect("Ya no decimos post-Lomé");
    assertCorrect("que nadie interprete mal los votos");
    assertCorrect("Cuando anuncio una votación especifico de cuál se trata");
    assertCorrect("qué clase de política practica la Unión Europea");
    assertCorrect("El proyecto de informe critica el hecho");
    assertCorrect("Ella practica política de consumidores.");
    assertCorrect("y participe activa y directamente");
    assertCorrect("que nadie participe de ellos");
    assertCorrect("la UE que opera aislada");
    assertCorrect("que los diputados borren del mapa");
    assertCorrect("Por tanto solicito a la comisión.");
    assertCorrect("por parte de quien critica dicha responsabilidad");
    assertCorrect("el terreno que domino mejor");
    assertCorrect("las naciones, participe derecho en este proceso");
    assertCorrect("la protección de los consumidores limite la libre prestación");
    assertCorrect("el artículo 30 de la propuesta estipula que los Estados miembros");
    assertCorrect("el proyecto de informe anima a la Comisión");
    assertCorrect("Y por supuesto que no libero de responsabilidad");
    assertCorrect("Critico del mismo modo las restricciones");
    assertCorrect("y por tanto termino aquí");
    assertCorrect("Los talibán violan los derechos humanos.");
    assertCorrect("la transición de 1999 indica también que Macao");
    assertCorrect("¿Por qué decimos esto de una manera tan clara");
    assertCorrect("El grupo PPE-DE solicita que se retire");
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
    assertIncorrect("debe valer la celebre máxima de Voltaire");
    assertIncorrect("el magnifico trabajo realizado");
    assertIncorrect("de entrada el medico diagnosticó");
    assertIncorrect("El publico deberá tener");
    //assertIncorrect("Fue participe de la operación");
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
    assertIncorrect("Hace falta una nueva formula que la sustituya.");
    
    // this sentences shouldn't be shown in rules EL_TILDE, PREPOSICION_VERBO   
    assertIncorrect("El termino.");
    assertIncorrect("El ejercito.");
    assertIncorrect("En el dialogo");
    assertIncorrect("El arbitro");
    assertIncorrect("El petroleo");
    assertIncorrect("El limite");
    
    assertIncorrect("en mi ultima intervención");
    //assertIncorrect("hubiera hecho más explicito lo que acaba de decir");
    assertIncorrect("salía de esas fabricas a un precio mucho más bajo");
    assertIncorrect("y de forma simultanea");
    assertIncorrect("pretende organizar un modulo sobre la capacitación");
    assertIncorrect("A su precio integro.");
    assertIncorrect("las disposiciones explicitas de la directiva");
    assertIncorrect("Una nueva formula que fue descubierta");
    assertIncorrect("las placas de matricula que han sido robadas");
    assertIncorrect("el critico más conocido de las industrias");
    assertIncorrect("y de entrada el medico diagnosticó que");
    assertIncorrect(" y adoptar el termino «compartir la responsabilidad»");
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
    RuleMatch[] matches;
    final JLanguageTool langTool = new JLanguageTool(new Spanish());

    matches = rule.match(langTool
        .getAnalyzedSentence("de cascaras vacías."));
    assertEquals(3, matches[0].getFromPos());
    assertEquals(11, matches[0].getToPos());
    assertEquals("cáscaras", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool
        .getAnalyzedSentence("El termino."));
    assertEquals(0, matches[0].getFromPos());
    assertEquals(10, matches[0].getToPos());
    assertEquals("El término", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool
        .getAnalyzedSentence("El frio."));
    assertEquals("El frío", matches[0].getSuggestedReplacements().get(0));
  }

}
