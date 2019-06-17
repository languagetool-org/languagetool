/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

public class WordCoherencyRuleTest {

  private final JLanguageTool lt = new JLanguageTool(new GermanyGerman());

  @Before
  public void before() throws IOException {
    TestTools.disableAllRulesExcept(lt, "DE_WORD_COHERENCY");
  }
  
  @Test
  public void testRule() throws IOException {
    // correct sentences:
    assertGood("Das ist aufwendig, aber nicht zu aufwendig.");
    assertGood("Das ist aufwendig. Aber nicht zu aufwendig.");
    assertGood("Das ist aufwändig, aber nicht zu aufwändig.");
    assertGood("Das ist aufwändig. Aber nicht zu aufwändig.");
    // errors:
    assertError("Das ist aufwendig, aber nicht zu aufwändig.");
    assertError("Das ist aufwendig. Aber nicht zu aufwändig.");
    assertError("Das ist aufwendiger, aber nicht zu aufwändig.");
    assertError("Das ist aufwendiger. Aber nicht zu aufwändig.");
    assertError("Das ist aufwändig, aber nicht zu aufwendig.");
    assertError("Das ist aufwändig. Aber nicht zu aufwendig.");
    assertError("Das ist aufwändiger, aber nicht zu aufwendig.");
    assertError("Das ist aufwändiger. Aber nicht zu aufwendig.");
    assertError("Delfin und Delphin");
    assertError("Delfins und Delphine");
    assertError("essentiell und essenziell");
    assertError("essentieller und essenzielles");
    assertError("Differential und Differenzial");
    assertError("Differentials und Differenzials");
    assertError("Facette und Fassette");
    assertError("Facetten und Fassetten");
    assertError("Joghurt und Jogurt");
    assertError("Joghurts und Jogurt");
    assertError("Joghurt und Jogurts");
    assertError("Joghurts und Jogurts");
    assertError("Ketchup und Ketschup");
    assertError("Ketchups und Ketschups");
    assertError("Kommuniqué und Kommunikee");
    assertError("Kommuniqués und Kommunikees");
    assertError("Necessaire und Nessessär");
    assertError("Necessaires und Nessessärs");
    assertError("Orthographie und Orthografie");
    assertError("Orthographien und Orthografien");
    assertError("Potential und Potenzial");
    assertError("Potentials und Potenziale");
    assertError("Portemonnaie und Portmonee");
    assertError("Portemonnaies und Portmonees");
    assertError("potentiell und potenziell");
    assertError("potentielles und potenzieller");
    assertError("Schenke und Schänke");
    // see TODO comment in WordCoherencyRule:
    //assertError("Schenken und Schänken");
    assertError("substantiell und substanziell");
    assertError("substantieller und substanzielles");
    assertError("Thunfisch und Tunfisch");
    assertError("Thunfische und Tunfische");
    assertError("Xylophon und Xylofon");
    assertError("Xylophone und Xylofone");
    assertError("selbständig und selbstständig");
    assertError("selbständiges und selbstständiger");
    assertError("Bahnhofsplatz und Bahnhofplatz");
    // TODO: known to fail because jWordSplitters list is not complete:
    //assertError("Testketchup und Testketschup");
  }

  @Test
  public void testCallIndependence() throws IOException {
    assertGood("Das ist aufwendig.");
    assertGood("Aber nicht zu aufwändig.");  // this won't be noticed, the calls are independent of each other
  }

  @Test
  public void testMatchPosition() throws IOException {
    List<RuleMatch> ruleMatches = lt.check("Das ist aufwendig. Aber nicht zu aufwändig");
    assertThat(ruleMatches.size(), is(1));
    assertThat(ruleMatches.get(0).getFromPos(), is(33));
    assertThat(ruleMatches.get(0).getToPos(), is(42));
  }

  private void assertError(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(1, rule.match(analyzedSentences).length);
  }

  private void assertGood(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(0, rule.match(analyzedSentences).length);
  }

  @Test
  public void testRuleCompleteTexts() throws IOException {
    assertEquals(0, lt.check("Das ist aufwändig. Aber hallo. Es ist wirklich aufwändig.").size());
    assertEquals(1, lt.check("Das ist aufwendig. Aber hallo. Es ist wirklich aufwändig.").size());
    assertEquals(1, lt.check("Das ist aufwändig. Aber hallo. Es ist wirklich aufwendig.").size());
    
    // also find full forms:
    assertEquals(0, lt.check("Das ist aufwendig. Aber hallo. Es ist wirklich aufwendiger als so.").size());
    assertEquals(1, lt.check("Das ist aufwendig. Aber hallo. Es ist wirklich aufwändiger als so.").size());
    
    assertEquals(1, lt.check("Das ist aufwändig. Aber hallo. Es ist wirklich aufwendiger als so.").size());
    assertEquals(1, lt.check("Das ist das aufwändigste. Aber hallo. Es ist wirklich aufwendiger als so.").size());
    assertEquals(1, lt.check("Das ist das aufwändigste. Aber hallo. Es ist wirklich aufwendig.").size());

    // cross-paragraph checks
    assertEquals(1, lt.check("Das ist das aufwändigste.\n\nAber hallo. Es ist wirklich aufwendig.").size());
  }

}
