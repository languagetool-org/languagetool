/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà (http://www.languagetool.org)
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

package org.languagetool.rules.fr;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MorfologikFrenchSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikFrenchSpellerRule rule = new MorfologikFrenchSpellerRule(TestTools.getMessages("fr"), new French(), null,
        Collections.emptyList());

    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(new French());

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Écoute-moi.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("35%")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("20$")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("300 000 yen")).length);   
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("20°C")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("même s'il coûte 10.000 yens")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("J'ai 38,9 de fièvre.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Thunderbird 2.0.0.14")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Va-t’en !")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("-Je ne suis pas venu par manque de temps.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("12hr-14hr")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Dominique Strauss-Kahn")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'ONU")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("d'1")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'email")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Et d'Harvard")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("déconfinement")).length);  // from spelling.txt
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Déconfinement")).length); 
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Le Déconfinement")).length); // Should be only lower-case??
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Cesse de t'autoflageller.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'iPhone")).length);
        
    // Test for Multiwords.
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("vox populi")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("statu quo.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Bugs Bunny")).length);

    // tests for mixed case words
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("pH")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald's")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald’s")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("thisisanerror")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("thisIsAnError")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Thisisanerror")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("ThisIsAnError")).length);

    // incorrect words:
    matches = rule.match(langTool.getAnalyzedSentence("ecoute-moi"));
    assertEquals(1, matches.length);
    assertEquals("écoute", matches[0].getSuggestedReplacements().get(0));
    assertEquals("écouté", matches[0].getSuggestedReplacements().get(1));
    assertEquals("écoutè", matches[0].getSuggestedReplacements().get(2));
    assertEquals("coûte", matches[0].getSuggestedReplacements().get(3));

    matches = rule.match(langTool.getAnalyzedSentence("ecrit-il"));
    assertEquals(1, matches.length);
    assertEquals("écrit", matches[0].getSuggestedReplacements().get(0));
    assertEquals("décrit", matches[0].getSuggestedReplacements().get(1));

    matches = rule.match(langTool.getAnalyzedSentence("Mcdonald"));
    assertEquals(1, matches.length);
    assertEquals("McDonald", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Macdonald", matches[0].getSuggestedReplacements().get(1));

    matches = rule.match(langTool.getAnalyzedSentence("Lhomme"));
    assertEquals(1, matches.length);
    assertEquals("L'homme", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("dhommes"));
    assertEquals(1, matches.length);
    assertEquals("d'hommes", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("language"));
    assertEquals(1, matches.length);
    assertEquals("l'engagé", matches[0].getSuggestedReplacements().get(0));
    assertEquals("langage", matches[0].getSuggestedReplacements().get(1));
    
    assertSuggestion(rule, langTool, "qu’il sagissait", "ils agissait", "il s'agissait"); // see #3068 TODO: change order
    assertSuggestion(rule, langTool, "bonne sante", "bonnes ante", "bonne santé"); // see #3068 TODO: change order
    //assertSuggestion(rule, langTool, "et ca", "CA", "cA"); // see #2900
    assertSuggestion(rule, langTool, "La journé", "journée"); // see #2900. Better: journée
    assertSuggestion(rule, langTool, "la sante", "santé"); // see #2900
    assertSuggestion(rule, langTool, "Parcontre", "Par contre");  // see #1797
    assertSuggestion(rule, langTool, "parcontre", "par contre");  // see #1797
    //assertSuggestion(rule, langTool, "Ca", "Ça");  // see #912
    //assertSuggestion(rule, langTool, "aus", "aux"); // TODO: to be improved with frequency information
    assertSuggestion(rule, langTool, "Décu", "Déçu");  // see #912
    assertSuggestion(rule, langTool, "etant", "étant");  // see #1633
    assertSuggestion(rule, langTool, "Cliqez", "Cliquez");
    assertSuggestion(rule, langTool, "cliqez", "cliquez");
    assertSuggestion(rule, langTool, "offe", "effet", "offre", "coffre");  // "offre" would be better as first suggestion? 
    assertSuggestion(rule, langTool, "problemes", "problèmes"); 
    assertSuggestion(rule, langTool, "coulurs", "couleurs"); 
    assertSuggestion(rule, langTool, "boton", "bâton", "béton", "Boston", "coton", "bouton");  // "bouton" would be better? 
    //assertSuggestion(rule, langTool, "skype", "Skype");
    assertSuggestion(rule, langTool, "Wordpress", "WordPress");
    assertSuggestion(rule, langTool, "wordpress", "WordPress");
    assertSuggestion(rule, langTool, "Etais-tu", "Étés", "Étais"); //TODO: suggest only verbs
    assertSuggestion(rule, langTool, "etais-tu", "étés", "étais"); //TODO: suggest only verbs
    assertSuggestion(rule, langTool, "Playstation", "PlayStation"); 

    // don't split prefixes 
    matches = rule.match(langTool.getAnalyzedSentence("macrodiscipline"));
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getSuggestedReplacements().size());

  }
  
  private void assertSuggestion(MorfologikFrenchSpellerRule rule, JLanguageTool langTool, String input, String... expected) throws IOException {
    RuleMatch[]  matches = rule.match(langTool.getAnalyzedSentence(input));
    int i = 0;
    for (String s : expected) {
      assertEquals(s, matches[0].getSuggestedReplacements().get(i));
      i++;
    }
    
  }
}
