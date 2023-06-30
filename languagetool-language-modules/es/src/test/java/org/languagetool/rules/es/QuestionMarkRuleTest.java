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
import static org.junit.Assert.*;

public class QuestionMarkRuleTest {

  private final QuestionMarkRule rule = new QuestionMarkRule(JLanguageTool.getMessageBundle());
  private final JLanguageTool lt = new JLanguageTool(new Spanish());
  
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

    // Exclamation marks:
    List<RuleMatch> matches20 = lt.check("Qué irritante!");
    assertThat(matches20.size(), is(1));
    assertThat(matches20.get(0).getSuggestedReplacements().toString(), is("[¡Qué]"));

    List<RuleMatch> matches21 = lt.check("¡Qué irritante!");
    assertThat(matches21.size(), is(0));
    
    List<RuleMatch> matches22 = lt.check("—Hola!");
    assertThat(matches22.size(), is(1));
    assertThat(matches22.get(0).getSuggestedReplacements().toString(), is("[¡Hola]"));
  }

  private RuleMatch[] check(String s) throws IOException {
    return rule.match(lt.analyzeText(s));
  }
}
