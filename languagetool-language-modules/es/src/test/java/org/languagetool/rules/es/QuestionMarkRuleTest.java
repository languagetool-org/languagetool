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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Spanish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class QuestionMarkRuleTest {

  private final QuestionMarkRule rule = new QuestionMarkRule(JLanguageTool.getMessageBundle());
  private final JLanguageTool lt = new JLanguageTool(new Spanish());
  
  @BeforeEach
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
    MatcherAssert.assertThat(matches.length, is(0));

    RuleMatch[] matches2 = check("Hola, cómo estás?");
    MatcherAssert.assertThat(matches2.length, is(1));
    MatcherAssert.assertThat(matches2[0].getSuggestedReplacements().toString(), is("[¿cómo]")); 

    RuleMatch[] matches3 = check("¿Que pasa?");
    MatcherAssert.assertThat(matches3.length, is(0));

    RuleMatch[] matches4 = check("Que pasa?");
    MatcherAssert.assertThat(matches4.length, is(1));
    MatcherAssert.assertThat(matches4[0].getSuggestedReplacements().toString(), is("[¿Que]"));

    RuleMatch[] matches5 = check("Que pasa?\n");
    MatcherAssert.assertThat(matches5.length, is(1));
    MatcherAssert.assertThat(matches5[0].getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches6 = lt.check("Que pasa?");
    MatcherAssert.assertThat(matches6.size(), is(1));
    MatcherAssert.assertThat(matches6.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches7 = lt.check("Que pasa?\n");
    MatcherAssert.assertThat(matches7.size(), is(1));
    MatcherAssert.assertThat(matches7.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches8 = lt.check("Que pasa?\n\n");
    MatcherAssert.assertThat(matches8.size(), is(1));
    MatcherAssert.assertThat(matches8.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));

    List<RuleMatch> matches9 = lt.check("¿Que pasa?\n\n");
    MatcherAssert.assertThat(matches9.size(), is(0));

    List<RuleMatch> matches10 = lt.check("¡¿Nunca tienes clases o qué?!");
    MatcherAssert.assertThat(matches10.size(), is(0));

    List<RuleMatch> matches11 = lt.check("¡Usted tiene un gusto caro! -exclamó la dependienta- ¿Está seguro?");
    MatcherAssert.assertThat(matches11.size(), is(0));

    List<RuleMatch> matches12 = lt.check("¿Quién sabe hablar francés mejor: Tom o Mary?");
    MatcherAssert.assertThat(matches12.size(), is(0));

    List<RuleMatch> matches13 = lt.check("Esto es una prueba. Que pasa?\n\n");
    MatcherAssert.assertThat(matches13.size(), is(1));
    MatcherAssert.assertThat(matches13.get(0).getSuggestedReplacements().toString(), is("[¿Que]"));
    MatcherAssert.assertThat(matches13.get(0).getFromPos(), is(20));

    RuleMatch[] matches14 = check("Hola, de qué me hablas?");
    MatcherAssert.assertThat(matches14.length, is(1));
    MatcherAssert.assertThat(matches14[0].getSuggestedReplacements().toString(), is("[¿de]"));
    
    RuleMatch[] matches15 = check("Después de todo lo que pasó, qué quieres que te diga?");
    MatcherAssert.assertThat(matches15.length, is(1));
    MatcherAssert.assertThat(matches15[0].getSuggestedReplacements().toString(), is("[¿qué]"));
    
    RuleMatch[] matches16 = check("Pero cómo quieres que te lo diga?");
    MatcherAssert.assertThat(matches16[0].getSuggestedReplacements().toString(), is("[¿Pero]"));
    
    RuleMatch[] matches17 = check("Pero, cómo quieres que te lo diga?");
    MatcherAssert.assertThat(matches17[0].getSuggestedReplacements().toString(), is("[¿cómo]"));
    
    RuleMatch[] matches18 = check("Puedes imaginarte por qué no vino con nosotros?");
    MatcherAssert.assertThat(matches18[0].getSuggestedReplacements().toString(), is("[¿Puedes]"));
     

    // Exclamation marks:
    List<RuleMatch> matches20 = lt.check("Qué irritante!");
    MatcherAssert.assertThat(matches20.size(), is(1));
    MatcherAssert.assertThat(matches20.get(0).getSuggestedReplacements().toString(), is("[¡Qué]"));

    List<RuleMatch> matches21 = lt.check("¡Qué irritante!");
    MatcherAssert.assertThat(matches21.size(), is(0));
  }

  private RuleMatch[] check(String s) throws IOException {
    return rule.match(lt.analyzeText(s));
  }
}
