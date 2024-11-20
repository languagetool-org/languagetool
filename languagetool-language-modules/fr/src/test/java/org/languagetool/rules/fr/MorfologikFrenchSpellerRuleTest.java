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

import static org.junit.Assert.assertEquals;

public class MorfologikFrenchSpellerRuleTest {
  private static final JLanguageTool lt = new JLanguageTool(new French());
  private final MorfologikFrenchSpellerRule rule;

  public MorfologikFrenchSpellerRuleTest() throws IOException {
    rule = getRule();
  }

  private static MorfologikFrenchSpellerRule getRule() throws IOException {
    return new MorfologikFrenchSpellerRule(TestTools.getMessages("fr"), new French(), null,
      Collections.emptyList());
  }

  private List<String> getTopSuggestions(RuleMatch match, int maxSuggestions) {
    return match.getSuggestedReplacements().subList(0, Math.min(maxSuggestions, match.getSuggestedReplacements().size()));
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertEquals(expectedMatches, matches.length);
  }

  private void assertNoMatches(String input) throws IOException {
    assertMatches(input, 0);
  }

  private void assertIterateOverSuggestions(List<String> returnedSuggestions, String[] expectedSuggestions) {
    for (String expectedSuggestion : expectedSuggestions) {
      assert returnedSuggestions.contains(expectedSuggestion) : "Expected suggestions to contain '" + expectedSuggestion + "' but got " + returnedSuggestions;
    }
  }

  private void assertSuggestionsContain(String input, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    List<String> suggestions = getTopSuggestions(matches[0], 5);
    assertIterateOverSuggestions(suggestions, expectedSuggestions);
  }

  private void assertSingleMatchWithSuggestions(String input, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertEquals(1, matches.length);
    List<String> suggestions = getTopSuggestions(matches[0], 5);
    assertIterateOverSuggestions(suggestions, expectedSuggestions);
  }

  public void assertSingleMatchZeroSuggestions(String input) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertEquals(1, matches.length);
    List<String> suggestions = matches[0].getSuggestedReplacements();
    assertEquals(0, suggestions.size());
  }

  private void assertExactSuggestion(String input, String... expected) throws IOException {
    RuleMatch[]  matches = rule.match(lt.getAnalyzedSentence(input));
    int i = 0;
    List<String> suggestions = getTopSuggestions(matches[0], 5);
    for (String s : expected) {
      assertEquals(s, suggestions.get(i));
      i++;
    }
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    assertSuggestionsContain("darriver", "d'arriver", "arriver");
    assertSuggestionsContain("decodés", "décodés", "décodes", "de codés");
  }

  @Test
  public void testApostrophes() throws Exception {
    assertNoMatches("d'1");
    assertNoMatches("l'email");
    assertNoMatches("Aujourd'hui et jusqu'à demain.");
    assertNoMatches("Aujourd’hui et jusqu’à demain.");
    assertNoMatches("L'Allemagne et l'Italie.");
    assertMatches("L’allemagne et l’italie.", 2);
    assertNoMatches("de Harvard ou d'Harvard");
  }

  @Test
  public void testHyphenated() throws Exception {
    assertNoMatches("L'Haÿ-les-Roses");
    assertMatches("L'Haÿ les Roses", 1);
    assertNoMatches("Il arrive après-demain.");
  }

  @Test
  public void testEmptyString() throws Exception {
    assertNoMatches("");
  }

  @Test
  public void testUnusualPunctuaion() throws Exception {
    assertNoMatches("À propos de cette chose… ");
  }

  @Test
  public void testSanity() throws Exception {
    assertNoMatches("Un test simple.");
    assertNoMatches("Le cœur, la sœur.");
    assertNoMatches("Ç'avait");
    assertNoMatches("LanguageTool");
    assertMatches("Un test simpple.", 1);
  }

  @Test
  public void testCorrectWords() throws Exception {
    assertNoMatches("Écoute-moi.");
    assertNoMatches("35%");
    assertNoMatches("20$");
    assertNoMatches("4x4");
    assertNoMatches("300 000 yen");
    assertNoMatches("20°C");
    assertNoMatches("même s'il coûte 10.000 yens");
    assertNoMatches("J'ai 38,9 de fièvre.");
    assertNoMatches("Thunderbird 2.0.0.14");
    assertNoMatches("Va-t’en !");
    assertNoMatches("-Je ne suis pas venu par manque de temps.");
    assertNoMatches("12hr-14hr");
    assertNoMatches("Dominique Strauss-Kahn");
    assertNoMatches("L'ONU");
    assertNoMatches("d'1");
    assertNoMatches("L'email");
    assertNoMatches("Et d'Harvard");
    assertNoMatches("déconfinement");
    assertNoMatches("Déconfinement");
    assertNoMatches("Le Déconfinement");
    assertNoMatches("Cesse de t'autoflageller.");
    assertNoMatches("L'iPhone");
    assertNoMatches("Une #sprache @mentioned mywebsite.org ereredd.7z, domaine .com, NH₄OH");
  }

  @Test
  public void testMultiwords() throws Exception {
    assertNoMatches("vox populi");
    assertNoMatches("statu quo");
    assertNoMatches("Bugs Bunny");
  }

  @Test
  public void testMixedCase() throws Exception {
    assertNoMatches("pH");
    assertSingleMatchWithSuggestions("Mcdonald", "McDonald", "Macdonald");
    assertNoMatches("McDonald's");
    assertNoMatches("McDonald’s");
    assertNoMatches("McDonald");
    assertMatches("thisisanerror", 1);
    assertMatches("thisIsAnError", 1);
    assertMatches("Thisisanerror", 1);
    assertMatches("ThisIsAnError", 1);
    assertExactSuggestion("Wordpress", "WordPress");
    assertExactSuggestion("wordpress", "WordPress");
    assertExactSuggestion("Playstation", "PlayStation");
  }

  @Test
  public void testIncorrectWords() throws Exception {
    // might be too strict, but work
    assertExactSuggestion("Décu", "Déçu");  // see #912
    assertExactSuggestion("etant", "étant");  // see #1633
    assertExactSuggestion("Cliqez", "Cliquez");
    assertExactSuggestion("cliqez", "cliquez");
    assertExactSuggestion("problemes", "problèmes");
    assertExactSuggestion("la sante", "santé"); // see #2900
    // had to make these laxer as the order changed
    assertSuggestionsContain("damazon", "d'Amazon", "Amazon", "d'Amazone", "Damazan");
    assertSuggestionsContain("coulurs", "couleurs");
    assertSingleMatchWithSuggestions("Den", "De");
    assertSuggestionsContain("offe", "effet", "offre", "coffre", "bouffe");
    assertSuggestionsContain("camara", "caméra", "camard");
    assertSuggestionsContain("boton", "bâton", "béton", "Boston", "coton", "bouton");  // "bouton" would be better?
    assertSuggestionsContain("La journé", "journée"); // see #2900. Better: journée
    // strict
    assertExactSuggestion("deja", "déjà");
  }

  @Test
  public void testWordSplitting() throws Exception {
    assertSingleMatchWithSuggestions("BretagneItinéraire", "Bretagne Itinéraire");
    assertSingleMatchWithSuggestions("BruxellesCapitale", "Bruxelles Capitale");
    assertExactSuggestion("Parcontre", "Par contre");  // see #1797
    assertExactSuggestion("parcontre", "par contre");  // see #1797
    assertExactSuggestion("Situé àseulement 9 km", "seulement", "à seulement");
  }

  @Test
  public void testVerbsWithPronouns() throws Exception {
    assertSingleMatchWithSuggestions("ecoute-moi", "Écoute", "Écouté", "Coûte");
    assertSingleMatchWithSuggestions("ecrit-il", "Écrit", "Décrit");
    assertSingleMatchWithSuggestions("Etais-tu", "Étais", "Étés"); //TODO: suggest only verbs
    assertSingleMatchWithSuggestions("etais-tu", "Étais", "Étés"); //TODO: suggest only verbs
    assertSingleMatchWithSuggestions("etiez-vous", "Étiez");
    assertSingleMatchWithSuggestions("étaistu", "étais-tu");
    assertSingleMatchWithSuggestions("etaistu", "étais-tu");
    assertSingleMatchWithSuggestions("voulezvous", "voulez-vous");
    assertSingleMatchWithSuggestions("ecoutemoi", "écoute-moi");
    assertSingleMatchWithSuggestions("mappelle", "m'appelle", "mappe-le");
    assertSingleMatchWithSuggestions("mapelle", "ma pelle", "m'appelle");
    assertSingleMatchWithSuggestions("allonsy", "allons-y");
    assertSingleMatchWithSuggestions("buvezen", "buvez-en");
    assertSingleMatchWithSuggestions("avaisje", "avais-je");
    assertSingleMatchWithSuggestions("depeche-toi", "Dépêche", "Dépêché");
    assertSingleMatchWithSuggestions("depechetoi", "dépêche-toi");
    assertSingleMatchWithSuggestions("sattendre", "s'attendre", "attendre");
    assertSingleMatchWithSuggestions("preferes-tu", "Préférés", "Préfères"); //TODO
    assertSingleMatchWithSuggestions("àllonsy", "allons-y");
  }

  @Test
  public void testWordEdgeElision() throws Exception {
    assertSingleMatchWithSuggestions("Lhomme", "L'homme");
    assertSingleMatchWithSuggestions("dhommes", "d'hommes");
    assertSingleMatchWithSuggestions("ladolescence", "l'adolescence", "adolescence");
    assertSingleMatchWithSuggestions("qu’il sagissait", "il s'agissait"); // see #3068 TODO: change order
    assertSingleMatchWithSuggestions("dIsraël", "d'Israël");
    assertSingleMatchWithSuggestions("dOrient", "d'Orient");
  }

  @Test
  public void testWordEdgeElisionWithTypos() throws Exception {
    assertSingleMatchWithSuggestions("dOrien", "dorien", "d'Orient");
  }

  @Test
  public void testWordBoundaryIssues() throws Exception {
    assertSingleMatchWithSuggestions("bonne sante", "bonne santé", "bonnes ante"); // see #3068
  }

  @Test
  public void testScreaming() throws Exception {
    assertSingleMatchZeroSuggestions("AAAAAAAAAAAH");
  }

  @Test
  public void testTokenisation() throws Exception {
    // digits and letters must be split
    assertSingleMatchWithSuggestions("123heures", "123 heures");
    // emoji and letters are tokenised separately, so this is not a spelling mistake
    assertNoMatches("⏰heures");
    // typo in "heuras", so we get "heures" as a suggestion, but the emoji is not touched
    assertSingleMatchWithSuggestions("⏰heuras", "heures");
    // "©" is a word character, so this is a single token, which triggers the speller
    assertSingleMatchWithSuggestions("©heures", "© heures");
    // these symbols are *not* word characters, so we get two tokens, and "heures" is fine
    assertNoMatches("►heures");
    assertNoMatches("◦heures");
  }

  @Test
  public void testNoPrefixSplit() throws Exception {
    assertSingleMatchZeroSuggestions("macrodiscipline");
  }

  @Test
  public void testDigits() throws Exception {
    assertSingleMatchWithSuggestions("windows1", "Windows");
    assertSingleMatchWithSuggestions("windows95", "Windows 95");
    assertSingleMatchWithSuggestions("à1930", "à 1930");
  }

  @Test
  public void testToImprove() throws Exception {
    assertSuggestionsContain("language", "l'engage", "l'aiguage", "l'engagé", "langage", "langages");
    assertSuggestionsContain("saperçoit", "sa perçoit", "s'aperçoit");
    assertSuggestionsContain("saperçu", "sa perçu", "aperçu");
  }

  @Test
  public void testMultitokens() throws IOException {
    assertNoMatches("MERCEDES-BENZ");
    assertNoMatches("Walt Disney Animation Studios");
    assertNoMatches("MÉTÉO-FRANCE");
    assertNoMatches("CLERMONT-FERRAND");
  }
}
