/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AdaptSuggestionFilterTest {

  private final AdaptSuggestionFilter filter = new AdaptSuggestionFilter();
  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));

  @Ignore("for development")
  @Test
  public void testAcceptRuleMatchDevTest() throws IOException {
    runAcceptRuleMatch("Hier steht unsere Roadmap.", "Roadmap", "Plan", "[unseren Plan, unser Plan]");
  }
  
  @Test
  public void testAcceptRuleMatchWithDet() throws IOException {
    // MAS (Der Plan):
    runAcceptRuleMatch("Die Roadmap ist gut.", "Roadmap",       "Plan", "[Den Plan, Der Plan]");
    // MAS (der Plan):
    runAcceptRuleMatch("Hier steht die Roadmap.", "Roadmap",    "Plan", "[den Plan, der Plan]");
    runAcceptRuleMatch("Hier steht eine Roadmap.", "Roadmap",   "Plan", "[einen Plan, ein Plan]");
    runAcceptRuleMatch("Hier steht meine Roadmap.", "Roadmap",  "Plan", "[meinen Plan, mein Plan]");
    runAcceptRuleMatch("Hier steht deine Roadmap.", "Roadmap",  "Plan", "[deinen Plan, dein Plan]");
    runAcceptRuleMatch("Hier steht seine Roadmap.", "Roadmap",  "Plan", "[seinen Plan, sein Plan]");
    runAcceptRuleMatch("Hier steht ihre Roadmap.", "Roadmap",   "Plan", "[ihren Plan, ihr Plan]");
    runAcceptRuleMatch("Hier steht unsere Roadmap.", "Roadmap", "Plan", "[unseren Plan, unser Plan]");
    runAcceptRuleMatch("Hier steht eure Roadmap.", "Roadmap",   "Plan", "[euren Plan, euer Plan]");
    // FEM (die Idee):
    runAcceptRuleMatch("Hier steht die Roadmap.", "Roadmap",    "Idee", "[die Idee]");
    runAcceptRuleMatch("Hier steht eine Roadmap.", "Roadmap",   "Idee", "[eine Idee]");
    runAcceptRuleMatch("Hier steht meine Roadmap.", "Roadmap",  "Idee", "[meine Idee]");
    runAcceptRuleMatch("Hier steht deine Roadmap.", "Roadmap",  "Idee", "[deine Idee]");
    runAcceptRuleMatch("Hier steht seine Roadmap.", "Roadmap",  "Idee", "[seine Idee]");
    runAcceptRuleMatch("Hier steht ihre Roadmap.", "Roadmap",   "Idee", "[ihre Idee]");
    runAcceptRuleMatch("Hier steht unsere Roadmap.", "Roadmap", "Idee", "[unsere Idee]");
    runAcceptRuleMatch("Hier steht eure Roadmap.", "Roadmap",   "Idee", "[eure Idee]");
    // NEU (das Verfahren):
    runAcceptRuleMatch("Hier steht die Roadmap.", "Roadmap",    "Verfahren", "[die Verfahren, das Verfahren]");
    runAcceptRuleMatch("Hier steht eine Roadmap.", "Roadmap",   "Verfahren", "[ein Verfahren]");
    runAcceptRuleMatch("Hier steht meine Roadmap.", "Roadmap",  "Verfahren", "[meine Verfahren, mein Verfahren]");
    runAcceptRuleMatch("Hier steht deine Roadmap.", "Roadmap",  "Verfahren", "[deine Verfahren, dein Verfahren]");
    runAcceptRuleMatch("Hier steht seine Roadmap.", "Roadmap",  "Verfahren", "[seine Verfahren, sein Verfahren]");
    runAcceptRuleMatch("Hier steht ihre Roadmap.", "Roadmap",   "Verfahren", "[ihre Verfahren, ihr Verfahren]");
    runAcceptRuleMatch("Hier steht unsere Roadmap.", "Roadmap", "Verfahren", "[unsere Verfahren, unser Verfahren]");
    runAcceptRuleMatch("Hier steht eure Roadmap.", "Roadmap",   "Verfahren", "[eure Verfahren, euer Verfahren]");
  }

  @Ignore("WIP")
  @Test
  public void testAcceptRuleMatchWithDetAdj() throws IOException {
    runAcceptRuleMatch("Hier steht die neue Roadmap.", "Roadmap",    "Plan", "[den neuen Plan, der neue Plan]");
    runAcceptRuleMatch("Hier steht eine neue Roadmap.", "Roadmap",   "Plan", "[einen neuen Plan, ein neuer Plan]");
    runAcceptRuleMatch("Hier steht meine neue Roadmap.", "Roadmap",  "Plan", "[mein neuer Plan]");
    runAcceptRuleMatch("Hier steht deine neue Roadmap.", "Roadmap",  "Plan", "[dein neuer Plan]");
    runAcceptRuleMatch("Hier steht seine neue Roadmap.", "Roadmap",  "Plan", "[sein neuer Plan]");
    runAcceptRuleMatch("Hier steht ihre neue Roadmap.", "Roadmap",   "Plan", "[ihr neuer Plan]");
    runAcceptRuleMatch("Hier steht unsere neue Roadmap.", "Roadmap", "Plan", "[unser neuer Plan]");
    runAcceptRuleMatch("Hier steht eure neue Roadmap.", "Roadmap",   "Plan", "[euer neuer Plan]");
  }
  
  private void runAcceptRuleMatch(String sentenceStr, String word, String origReplacement, String newReplacements) throws IOException {
    AnalyzedSentence sentence = lt.getAnalyzedSentence(sentenceStr);
    int fromPos = sentenceStr.indexOf(word);
    int toPos = fromPos + word.length();
    RuleMatch match = new RuleMatch(new FakeRule(), sentence, fromPos, toPos, "fake message");
    match.setSuggestedReplacement(origReplacement);
    int tokenPos = -1;
    int i = 0;
    for (AnalyzedTokenReadings tokens : sentence.getTokensWithoutWhitespace()) {
      if (tokens.getToken().equals(word)) {
        tokenPos = i;
        break;
      }
      i++;
    }
    if (i == -1) {
      throw new RuntimeException("Word '" + word + "' not found in sentence: '" + sentenceStr + "'");
    }
    Map<String,String> map = new HashMap<>();
    map.put("sub", "\\1");
    RuleMatch newMatch = filter.acceptRuleMatch(match, map, i, Arrays.copyOfRange(sentence.getTokensWithoutWhitespace(), tokenPos, tokenPos+1));
    assertNotNull(newMatch);
    assertThat(newMatch.getSuggestedReplacements().toString(), is(newReplacements));
  }
  
  @Test
  public void testAdaptedDet() {
    assertDet(new AnalyzedToken("die", "ART:DEF:NOM:SIN:FEM", "der"), "Mann", "[der]");
    assertDet(new AnalyzedToken("der", "ART:DEF:NOM:SIN:MAS", "der"), "Frau", "[die]");
    assertDet(new AnalyzedToken("der", "ART:DEF:NOM:SIN:NEU", "der"), "Kind", "[das]");
    
    assertDet(new AnalyzedToken("eine", "ART:IND:NOM:SIN:FEM", "ein"), "Plan", "[ein]");     // eine Roadmap -> ein Plan
    assertDet(new AnalyzedToken("eine", "ART:IND:AKK:SIN:FEM", "ein"), "Plan", "[einen]");   // ich habe eine Roadmap -> ich habe einen Plan
    assertDet(new AnalyzedToken("einer", "ART:IND:GEN:SIN:FEM", "ein"), "Plan", "[eines]");  // die Ausführung einer Roadmap -> die Ausführung eines Plans
    assertDet(new AnalyzedToken("einer", "ART:IND:DAT:SIN:FEM", "ein"), "Plan", "[einem]");  // einer Roadmap -> einem Plan
  }

  @Ignore("WIP")
  @Test
  public void testdAdaptedDetAdj() {
    assertDetAdj(new AnalyzedToken("eine", "ART:IND:NOM:SIN:FEM", "ein"),
                 new AnalyzedToken("neue", "ADJ:NOM:SIN:FEM:GRU:IND", "neu"), "Plan", "[ein neuer]");     // eine neue Roadmap -> ein neuer Plan
  }

  private void assertDet(AnalyzedToken detToken, String replWord, String expectedDet) {
    List<String> adaptedDet = filter.getAdaptedDet(new AnalyzedTokenReadings(detToken, 0), replWord);
    assertThat(adaptedDet.toString(), is(expectedDet));
  }
  
  private void assertDetAdj(AnalyzedToken detToken, AnalyzedToken adjToken, String replWord, String expectedDet) {
    List<String> adaptedDet = filter.getAdaptedDetAdj(new AnalyzedTokenReadings(detToken, 0),
      new AnalyzedTokenReadings(adjToken, 0), replWord);
    assertThat(adaptedDet.toString(), is(expectedDet));
  }
  
}
