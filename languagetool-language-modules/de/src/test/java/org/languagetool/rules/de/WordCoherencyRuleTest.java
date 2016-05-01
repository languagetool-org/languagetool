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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.German;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WordCoherencyRuleTest {

  @Test
  public void testRule() throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    JLanguageTool langTool = new JLanguageTool(new German());
    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist aufwendig, aber nicht zu aufwendig.")).length);
    // as WordCoherencyRule keeps its state to check more than one sentence 
    // we need to create a new object each time:
    rule.reset();
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist aufwändig, aber nicht zu aufwändig.")).length);
    // errors:
    assertError("Das ist aufwendig, aber nicht zu aufwändig.", langTool);
    assertError("Das ist aufwendiger, aber nicht zu aufwändig.", langTool);
    assertError("Das ist aufwändig, aber nicht zu aufwendig.", langTool);
    assertError("Das ist aufwändiger, aber nicht zu aufwendig.", langTool);
    assertError("Delfin und Delphin", langTool);
    assertError("Delfins und Delphine", langTool);
    assertError("essentiell und essenziell", langTool);
    assertError("essentieller und essenzielles", langTool);
    assertError("Differential und Differenzial", langTool);
    assertError("Differentials und Differenzials", langTool);
    assertError("Facette und Fassette", langTool);
    assertError("Facetten und Fassetten", langTool);
    assertError("Joghurt und Jogurt", langTool);
    assertError("Joghurts und Jogurt", langTool);
    assertError("Joghurt und Jogurts", langTool);
    assertError("Joghurts und Jogurts", langTool);
    assertError("Ketchup und Ketschup", langTool);
    assertError("Ketchups und Ketschups", langTool);
    assertError("Kommuniqué und Kommunikee", langTool);
    assertError("Kommuniqués und Kommunikees", langTool);
    assertError("Necessaire und Nessessär", langTool);
    assertError("Necessaires und Nessessärs", langTool);
    assertError("Orthographie und Orthografie", langTool);
    assertError("Orthographien und Orthografien", langTool);
    assertError("Potential und Potenzial", langTool);
    assertError("Potentials und Potenziale", langTool);
    assertError("Portemonnaie und Portmonee", langTool);
    assertError("Portemonnaies und Portmonees", langTool);
    assertError("potentiell und potenziell", langTool);
    assertError("potentielles und potenzieller", langTool);
    assertError("Schenke und Schänke", langTool);
    // see TODO comment in WordCoherencyRule:
    //assertError("Schenken und Schänken", langTool);
    assertError("substantiell und substanziell", langTool);
    assertError("substantieller und substanzielles", langTool);
    assertError("Thunfisch und Tunfisch", langTool);
    assertError("Thunfische und Tunfische", langTool);
    assertError("Xylophon und Xylofon", langTool);
    assertError("Xylophone und Xylofone", langTool);
    assertError("selbständig und selbstständig", langTool);
    assertError("selbständiges und selbstständiger", langTool);
    assertError("Bahnhofsplatz und Bahnhofplatz", langTool);
    // TODO: known to fail because jWordSplitters list is not complete:
    //assertError("Testketchup und Testketschup", langTool);
  }

  @Test
  public void testCallIndependence() throws IOException {
    JLanguageTool langTool = new JLanguageTool(new German());
    assertGood("Das ist aufwendig.", langTool);
    assertGood("Aber nicht zu aufwändig.", langTool);  // this won't be noticed, the calls are independent of each other
  }

  private void assertError(String s, JLanguageTool langTool) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(s)).length);
  }

  private void assertGood(String s, JLanguageTool langTool) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(s)).length);
  }

  @Test
  public void testRuleCompleteTexts() throws IOException {
    JLanguageTool lt = new JLanguageTool(new German());

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
