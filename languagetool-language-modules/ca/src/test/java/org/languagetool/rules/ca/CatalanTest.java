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
package org.languagetool.rules.ca;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CatalanTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Introduïu açí el vostre text. o feu servir aquest texts com a a exemple per a alguns errades que LanguageTool hi pot detectat.";
    Catalan lang = new Catalan();
    testDemoText(lang, s,
      Arrays.asList("MORFOLOGIK_RULE_CA_ES", "UPPERCASE_SENTENCE_START", "CONCORDANCES_DET_NOM", "CATALAN_WORD_REPEAT_RULE", "CONCORDANCES_DET_NOM", "VERB_SEGUIT_DINFINITIU")
    );
    runTests(lang, null, "·");
  }
  
  @Test
  public void testRepeatedPatternRules() throws IOException {
    Language lang = new Catalan();
    JLanguageTool lt = new JLanguageTool(lang);
    List<RuleMatch> matches = lt.check("Iniciem les converses. Llavors s'inicià una altra cosa.");
    assertEquals("Matches across rules in a rule group", 1, matches.size());
    assertEquals("Match ID", "REP_INICIAR[1]", matches.get(0).getRule().getFullId());
    
    matches = lt.check("Iniciem les converses. Llavors inicià una altra cosa.");
    assertEquals("Matches across rules in a rule group", 1, matches.size());
    assertEquals("Match ID", "REP_INICIAR[2]", matches.get(0).getRule().getFullId());
    
    matches = lt.check("Aleshores iniciem les converses. Llavors inicià una altra cosa.");
    assertEquals("Matches across rules in a rule group", 1, matches.size());
    assertEquals("Match ID", "REP_INICIAR[2]", matches.get(0).getRule().getFullId());
    
    matches = lt.check("S'inicia el debat. Llavors inicià una altra cosa.");
    assertEquals("Matches across rules in a rule group", 1, matches.size());
    assertEquals("Match ID", "REP_INICIAR[2]", matches.get(0).getRule().getFullId());
    
    matches = lt.check("S'inicia el debat. Llavors s'inicià una altra cosa.");
    assertEquals("Matches across rules in a rule group", 1, matches.size());
    assertEquals("Match ID", "REP_INICIAR[1]", matches.get(0).getRule().getFullId());   
    
    matches = lt.check("Això no obstant, és clar. No obstant això, la cosa és clara.");
    assertEquals("Matches across rules in a rule group", 1, matches.size());
    assertEquals("Match ID", "REP_NO_OBSTANT_AIXO[1]", matches.get(0).getRule().getFullId());
  }
}
