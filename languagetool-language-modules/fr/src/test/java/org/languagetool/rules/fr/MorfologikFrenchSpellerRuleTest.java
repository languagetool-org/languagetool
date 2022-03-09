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

import static org.junit.Assert.assertEquals;

public class MorfologikFrenchSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikFrenchSpellerRule rule = new MorfologikFrenchSpellerRule(TestTools.getMessages("fr"), new French(), null,
        Collections.emptyList());

    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new French());

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Écoute-moi.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("35%")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("20$")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("4x4")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("300 000 yen")).length);   
    assertEquals(0, rule.match(lt.getAnalyzedSentence("20°C")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("même s'il coûte 10.000 yens")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("J'ai 38,9 de fièvre.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Thunderbird 2.0.0.14")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Va-t’en !")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("-Je ne suis pas venu par manque de temps.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("12hr-14hr")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Dominique Strauss-Kahn")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("L'ONU")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("d'1")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("L'email")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Et d'Harvard")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("déconfinement")).length);  // from spelling.txt
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Déconfinement")).length); 
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Le Déconfinement")).length); // Should be only lower-case??
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Cesse de t'autoflageller.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("L'iPhone")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Une #sprache @mentioned mywebsite.org ereredd.7z, domaine .com, NH₄OH")).length);
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Un test simple.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Le cœur, la sœur.")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Un test simpple.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ç'avait")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("LanguageTool")).length);
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("L'ONU")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Il arrive après-demain.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("L'Haÿ-les-Roses")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("L'Haÿ les Roses")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Aujourd'hui et jusqu'à demain.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Aujourd’hui et jusqu’à demain.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("L'Allemagne et l'Italie.")).length);
    assertEquals(2, rule.match(lt.getAnalyzedSentence("L’allemagne et l’italie.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("de Harvard ou d'Harvard")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("d'1")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("l'email")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("À propos de cette chose… ")).length);
        
    // Test for Multiwords.
    assertEquals(0, rule.match(lt.getAnalyzedSentence("vox populi")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("statu quo.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Bugs Bunny")).length);

    // tests for mixed case words
    assertEquals(0, rule.match(lt.getAnalyzedSentence("pH")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("McDonald's")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("McDonald’s")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("McDonald")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("thisisanerror")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("thisIsAnError")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Thisisanerror")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("ThisIsAnError")).length);

    // incorrect words:
    matches = rule.match(lt.getAnalyzedSentence("ecoute-moi"));
    assertEquals(1, matches.length);
    assertEquals("écoute", matches[0].getSuggestedReplacements().get(0));
    assertEquals("écouté", matches[0].getSuggestedReplacements().get(1));
    assertEquals("écoutè", matches[0].getSuggestedReplacements().get(2));
    assertEquals("coûte", matches[0].getSuggestedReplacements().get(3));

    matches = rule.match(lt.getAnalyzedSentence("ecrit-il"));
    assertEquals(1, matches.length);
    assertEquals("écrit", matches[0].getSuggestedReplacements().get(0));
    assertEquals("décrit", matches[0].getSuggestedReplacements().get(1));

    matches = rule.match(lt.getAnalyzedSentence("Mcdonald"));
    assertEquals(1, matches.length);
    assertEquals("McDonald", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Macdonald", matches[0].getSuggestedReplacements().get(1));

    matches = rule.match(lt.getAnalyzedSentence("Lhomme"));
    assertEquals(1, matches.length);
    assertEquals("L'homme", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("dhommes"));
    assertEquals(1, matches.length);
    assertEquals("d'hommes", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("ladolescence"));
    // no: "l adolescence" 
    assertEquals(1, matches.length);
    assertEquals(2, matches[0].getSuggestedReplacements().size());
    assertEquals("l'adolescence", matches[0].getSuggestedReplacements().get(0));
    assertEquals("adolescence", matches[0].getSuggestedReplacements().get(1));
        
    assertSuggestion(rule, lt, "qu’il sagissait", "ils agissait", "il s'agissait"); // see #3068 TODO: change order
    assertSuggestion(rule, lt, "bonne sante", "bonnes ante", "bonne santé"); // see #3068 TODO: change order
    //assertSuggestion(rule, lt, "et ca", "CA", "cA"); // see #2900
    assertSuggestion(rule, lt, "La journé", "journée"); // see #2900. Better: journée
    assertSuggestion(rule, lt, "la sante", "santé"); // see #2900
    assertSuggestion(rule, lt, "Parcontre", "Par contre");  // see #1797
    assertSuggestion(rule, lt, "parcontre", "par contre");  // see #1797
    //assertSuggestion(rule, lt, "Ca", "Ça");  // see #912
    //assertSuggestion(rule, lt, "aus", "aux"); // TODO: to be improved with frequency information
    assertSuggestion(rule, lt, "Décu", "Déçu");  // see #912
    assertSuggestion(rule, lt, "etant", "étant");  // see #1633
    assertSuggestion(rule, lt, "Cliqez", "Cliquez");
    assertSuggestion(rule, lt, "cliqez", "cliquez");
    assertSuggestion(rule, lt, "offe", "effet", "offre", "coffre");  // "offre" would be better as first suggestion? 
    assertSuggestion(rule, lt, "problemes", "problèmes"); 
    assertSuggestion(rule, lt, "coulurs", "couleurs"); 
    assertSuggestion(rule, lt, "boton", "bâton", "béton", "Boston", "coton", "bouton");  // "bouton" would be better? 
    //assertSuggestion(rule, lt, "skype", "Skype");
    assertSuggestion(rule, lt, "Wordpress", "WordPress");
    assertSuggestion(rule, lt, "wordpress", "WordPress");
    assertSuggestion(rule, lt, "Etais-tu", "Étais", "Étés"); //TODO: suggest only verbs
    assertSuggestion(rule, lt, "etais-tu", "étais", "étés"); //TODO: suggest only verbs 
    assertSuggestion(rule, lt, "depechetoi", "dépêche-toi", "dépêcherai");
    assertSuggestion(rule, lt, "etiez-vous", "étiez");
    assertSuggestion(rule, lt, "preferes-tu", "préférés", "préfères"); //TODO
    assertSuggestion(rule, lt, "Playstation", "PlayStation"); 
    assertSuggestion(rule, lt, "étaistu", "étais-tu");
    assertSuggestion(rule, lt, "etaistu", "étais-tu", "était");
    assertSuggestion(rule, lt, "voulezvous", "voulez-vous");
    assertSuggestion(rule, lt, "ecoutemoi", "écoute-moi");
    assertSuggestion(rule, lt, "mappelle", "m'appelle", "mappe-le");
    assertSuggestion(rule, lt, "mapelle", "ma pelle", "m'appelle");
    assertSuggestion(rule, lt, "camara", "caméra", "Samara");
    assertSuggestion(rule, lt, "allonsy", "allons-y");
    assertSuggestion(rule, lt, "àllonsy", "allons-y");
    assertSuggestion(rule, lt, "buvezen", "buvez-en");
    assertSuggestion(rule, lt, "avaisje", "avais-je");
    assertSuggestion(rule, lt, "damazon", "d'Amazon", "d'amazone", "d'Amazone");
    assertSuggestion(rule, lt, "deja", "déjà", "d'EA");
    assertSuggestion(rule, lt, "depeche-toi", "dépêche", "dépêché", "dépêchè", "d'empêché", "d'évêché", "repêché");
    assertSuggestion(rule, lt, "sattendre", "s'attendre", "attendre");
    assertSuggestion(rule, lt, "darriver", "d'arriver", "arriver");
    assertSuggestion(rule, lt, "Situé àseulement 9 km", "seulement", "à seulement");
    assertSuggestion(rule, lt, "decodés", "décodés", "décodes", "de codés");
    // to improve
    assertSuggestion(rule, lt, "language", "l'engage", "l'engagé", "l'aiguage", "langage");
    assertSuggestion(rule, lt, "saperçoit", "sa perçoit", "s'aperçoit");
    assertSuggestion(rule, lt, "saperçu", "sa perçu", "aperçu");
    
    // don't split prefixes 
    matches = rule.match(lt.getAnalyzedSentence("macrodiscipline"));
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getSuggestedReplacements().size());
    
    // digits
    matches = rule.match(lt.getAnalyzedSentence("windows1"));
    assertEquals(1, matches.length);
    assertEquals("Windows 1", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("windows10"));
    assertEquals(1, matches.length);
    assertEquals("Windows 10", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("à1930"));
    assertEquals(1, matches.length);
    assertEquals("à 1930", matches[0].getSuggestedReplacements().get(0));
      

  }
  
  private void assertSuggestion(MorfologikFrenchSpellerRule rule, JLanguageTool lt, String input, String... expected) throws IOException {
    RuleMatch[]  matches = rule.match(lt.getAnalyzedSentence(input));
    int i = 0;
    for (String s : expected) {
      assertEquals(s, matches[0].getSuggestedReplacements().get(i));
      i++;
    }
    
  }
}
