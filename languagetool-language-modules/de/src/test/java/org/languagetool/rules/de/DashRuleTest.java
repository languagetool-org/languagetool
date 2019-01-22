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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;

public class DashRuleTest {

  private final DashRule rule = new DashRule(TestTools.getMessages("de"));

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());

    // correct sentences:
    assertGood("Die große Diäten-Erhöhung kam dann doch.", lt);
    assertGood("Die große Diätenerhöhung kam dann doch.", lt);
    assertGood("Die große Diäten-Erhöhungs-Manie kam dann doch.", lt);
    assertGood("Die große Diäten- und Gehaltserhöhung kam dann doch.", lt);
    assertGood("Die große Diäten- sowie Gehaltserhöhung kam dann doch.", lt);
    assertGood("Die große Diäten- oder Gehaltserhöhung kam dann doch.", lt);
    assertGood("Erst so - Karl-Heinz dann blah.", lt);
    assertGood("Erst so -- Karl-Heinz aber...", lt);
    assertGood("Nord- und Südkorea", lt);
    assertGood("NORD- UND SÜDKOREA", lt);
    assertGood("NORD- BZW. SÜDKOREA", lt);
    
    // incorrect sentences:
    assertBad("Die große Diäten- Erhöhung kam dann doch.", lt);
    assertBad("Die große Diäten-  Erhöhung kam dann doch.", lt);
    assertBad("Die große Diäten-Erhöhungs- Manie kam dann doch.", lt);
    assertBad("Die große Diäten- Erhöhungs-Manie kam dann doch.", lt);
    assertBad("MAZEDONIEN- SKOPJE Str.", lt);
  }

  private void assertGood(String text, JLanguageTool lt) throws IOException {
    assertEquals(0, rule.match(lt.getAnalyzedSentence(text)).length);
  }

  private void assertBad(String text, JLanguageTool lt) throws IOException {
    assertEquals(1, rule.match(lt.getAnalyzedSentence(text)).length);
  }

}
