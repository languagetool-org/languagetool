/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.language.Spanish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QuestionMarkRuleTest {

  private final QuestionMarkRule rule = new QuestionMarkRule(JLanguageTool.getMessageBundle());
  private final JLanguageTool lt = new JLanguageTool(Spanish.getInstance());
  
  @Before
  public void setup() {
    for (Rule r : lt.getAllActiveRules()) {
      if (!r.getId().equals(rule.getId())) {
        lt.disableRule(r.getId());
      }
    }
  }

  @Test
  public void test() throws IOException {

    RuleMatch[] matches = check("Hola, ¿cómo estás?");
    assertThat(matches.length, is(0));

    RuleMatch[] matches2 = check("Hola, cómo estás?");
    assertThat(matches2.length, is(1));
    assertThat(matches2[0].getSuggestedReplacements().toString(), is("[¿cómo]")); 

    RuleMatch[] matches3 = check("¿Que pasa?");
    assertThat(matches3.length, is(0));

    RuleMatch[] matches4 = check("Que pasa?");
    assertThat(matches4.length, is(1));
    assertThat(matches4[0].getSuggestedReplacements().toString(), is("[¿Que]"));

    RuleMatch[] matches5 = check("Que pasa?\n");
    assertThat(matches5.length, is(1));
    assertThat(matches5[0].getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches6 = lt.check("Que pasa?");
    assertThat(matches6.size(), is(1));
    assertThat(matches6.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches7 = lt.check("Que pasa?\n");
    assertThat(matches7.size(), is(1));
    assertThat(matches7.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches8 = lt.check("Que pasa?\n\n");
    assertThat(matches8.size(), is(1));
    assertThat(matches8.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches9 = lt.check("¿Que pasa?\n\n");
    assertThat(matches9.size(), is(0));

    List<RuleMatch> matches10 = lt.check("¡¿Nunca tienes clases o qué?!");
    assertThat(matches10.size(), is(0));

    List<RuleMatch> matches11 = lt.check("¡Usted tiene un gusto caro! -exclamó la dependienta- ¿Está seguro?");
    assertThat(matches11.size(), is(0));

    List<RuleMatch> matches12 = lt.check("¿Quién sabe hablar francés mejor: Tom o Mary?");
    assertThat(matches12.size(), is(0));

    List<RuleMatch> matches13 = lt.check("Esto es una prueba. Que pasa?\n\n");
    assertThat(matches13.size(), is(1));
    assertThat(matches13.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));
    assertThat(matches13.get(0).getFromPos(), is(20));

    RuleMatch[] matches14 = check("Hola, de qué me hablas?");
    assertThat(matches14.length, is(1));
    assertThat(matches14[0].getSuggestedReplacements().toString(), is("[¿de]"));
    
    RuleMatch[] matches15 = check("Después de todo lo que pasó, qué quieres que te diga?");
    assertThat(matches15.length, is(1));
    assertThat(matches15[0].getSuggestedReplacements().toString(), is("[¿qué]"));
    
    RuleMatch[] matches16 = check("Pero cómo quieres que te lo diga?");
    assertThat(matches16[0].getSuggestedReplacements().toString(), is("[¿Pero]"));
    
    RuleMatch[] matches17 = check("Pero, cómo quieres que te lo diga?");
    assertThat(matches17[0].getSuggestedReplacements().toString(), is("[¿cómo]"));
    
    RuleMatch[] matches18 = check("Puedes imaginarte por qué no vino con nosotros?");
    assertThat(matches18[0].getSuggestedReplacements().toString(), is("[¿Puedes]"));
     
    RuleMatch[] matches19 = check("Hola, Marco: Puedes darme tu dirección de correo?");
    assertThat(matches19[0].getSuggestedReplacements().toString(), is("[¿Puedes]"));

    RuleMatch[] matches33 =check("Están aquí: https://gtlb.com/smll-cued-datasets/hu[…]nitized/-/tree/main/beling?ref_type=heads");
    assertThat(matches33.length, is(0));

    // Exclamation marks:
    List<RuleMatch> matches20 = lt.check("Qué irritante!");
    assertThat(matches20.size(), is(1));
    assertThat(matches20.get(0).getSuggestedReplacements().toString(), is("[¡Qué]"));

    List<RuleMatch> matches21 = lt.check("¡Qué irritante!");
    assertThat(matches21.size(), is(0));
    
    List<RuleMatch> matches22 = lt.check("—Hola!");
    assertThat(matches22.size(), is(1));
    assertThat(matches22.get(0).getSuggestedReplacements().toString(), is("[¡Hola]"));

    List<RuleMatch> matches23 = lt.check("Muchas gracias! ✌\uFE0F");
    assertThat(matches23.size(), is(1));
    assertThat(matches23.get(0).getSuggestedReplacements().toString(), is("[¡Muchas]"));

    List<RuleMatch> matches24 = lt.check("Muchas gracias! ✌\uFE0F✌\uFE0F");
    assertThat(matches24.size(), is(1));
    assertThat(matches24.get(0).getSuggestedReplacements().toString(), is("[¡Muchas]"));

    List<RuleMatch> matches25 = lt.check("Muchas gracias! ✌\uFE0F ✌\uFE0F");
    assertThat(matches25.size(), is(1));
    assertThat(matches25.get(0).getSuggestedReplacements().toString(), is("[¡Muchas]"));

    List<RuleMatch> matches26 = lt.check("Muchas gracias!, le dijo entonces.");
    assertThat(matches26.size(), is(1));
    assertThat(matches26.get(0).getSuggestedReplacements().toString(), is("[¡Muchas]"));

    List<RuleMatch> matches27 = lt.check("\"Muchas gracias!\".");
    assertThat(matches27.size(), is(1));
    assertThat(matches27.get(0).getSuggestedReplacements().toString(), is("[¡Muchas]"));

    List<RuleMatch> matches28 = lt.check("Seguro que estás más habituado a algunas de estas formas verbales, pero cuál es la correcta?");
    assertThat(matches28.size(), is(1));
    assertThat(matches28.get(0).getSuggestedReplacements().toString(), is("[¿pero]"));

    List<RuleMatch> matches29 = lt.check("Tengo razón, o no?");
    assertThat(matches29.size(), is(1));
    assertThat(matches29.get(0).getSuggestedReplacements().toString(), is("[¿o]"));

    List<RuleMatch> matches30 = lt.check("Tengo razón, no?");
    assertThat(matches30.size(), is(1));
    assertThat(matches30.get(0).getSuggestedReplacements().toString(), is("[¿no]"));

    List<RuleMatch> matches31 = lt.check("Tengo razón, eh?");
    assertThat(matches31.size(), is(1));
    assertThat(matches31.get(0).getSuggestedReplacements().toString(), is("[¿eh]"));

    List<RuleMatch> matches32 = lt.check("qué me recomendarías???….");
    assertThat(matches32.size(), is(1));
    assertThat(matches32.get(0).getSuggestedReplacements().toString(), is("[¿qué]"));
  }

  private RuleMatch[] check(String s) throws IOException {
    return rule.match(lt.analyzeText(s));
  }
}
