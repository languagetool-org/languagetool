/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CompoundCoherencyRuleTest {

  private final CompoundCoherencyRule rule = new CompoundCoherencyRule(TestTools.getEnglishMessages());
  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));

  @Test
  public void testRule() throws IOException {
    assertOkay("Ein Jugendfoto.", "Und ein Jugendfoto.");
    assertOkay("Ein Jugendfoto.", "Der Rahmen eines Jugendfotos.");
    assertOkay("Der Rahmen eines Jugendfotos.", "Ein Jugendfoto.");
    assertOkay("Der Zahn-Ärzte-Verband.", "Der Zahn-Ärzte-Verband.");
    assertOkay("Der Zahn-Ärzte-Verband.", "Des Zahn-Ärzte-Verbands.");
    assertOkay("Der Zahn-Ärzte-Verband.", "Des Zahn-Ärzte-Verbandes.");
    assertOkay("Es gibt E-Mail.", "Und es gibt E-Mails.");
    assertOkay("Es gibt E-Mails.", "Und es gibt E-Mail.");
    assertOkay("Ein Jugend-Foto.", "Der Rahmen eines Jugend-Fotos.");
    
    assertError("Ein Jugendfoto.", "Und ein Jugend-Foto.", 23, 34, "Jugendfoto");
    assertError("Ein Jugend-Foto.", "Und ein Jugendfoto.", 24, 34, "Jugend-Foto");
    
    assertError("Viele Zahn-Ärzte.", "Oder Zahnärzte.", 22, 31, null);
    assertError("Viele Zahn-Ärzte.", "Oder Zahnärzte.", 22, 31, null);
    assertError("Viele Zahn-Ärzte.", "Oder Zahnärzten.", 22, 32, null);
    
    assertError("Der Zahn-Ärzte-Verband.", "Der Zahn-Ärzteverband.", 27, 44, "Zahn-Ärzte-Verband");
    assertError("Der Zahn-Ärzte-Verband.", "Der Zahnärzte-Verband.", 27, 44, "Zahn-Ärzte-Verband");
    assertError("Der Zahn-Ärzte-Verband.", "Der Zahnärzteverband.", 27, 43, "Zahn-Ärzte-Verband");
    assertError("Der Zahn-Ärzteverband.", "Der Zahn-Ärzte-Verband.", 26, 44, "Zahn-Ärzteverband");
    assertError("Der Zahnärzte-Verband.", "Der Zahn-Ärzte-Verband.", 26, 44, "Zahnärzte-Verband");
    assertError("Der Zahnärzteverband.", "Der Zahn-Ärzte-Verband.", 25, 43, "Zahnärzteverband");

    assertError("Der Zahn-Ärzte-Verband.", "Des Zahn-Ärzteverbandes.", 27, 46, null);
    assertError("Der Zahn-Ärzte-Verband.", "Des Zahnärzte-Verbandes.", 27, 46, null);
    assertError("Der Zahn-Ärzte-Verband.", "Des Zahnärzteverbandes.", 27, 45, null);
  }

  @Test
  @Ignore("for debugging")
  public void testRuleInteractive() throws IOException {
    RuleMatch[] matches = getMatches("Der Zahn-Ärzte-Verband.", "Der Zahn-Ärzteverband.");
    assertThat("Got " + Arrays.toString(matches), matches.length, is(1));
  } 

  private void assertOkay(String s1, String s2) throws IOException {
    RuleMatch[] matches = getMatches(s1, s2);
    assertThat("Got " + Arrays.toString(matches), matches.length, is(0));
  }

  private void assertError(String s1, String s2, int fromPos, int toPos, String suggestion) throws IOException {
    RuleMatch[] matches = getMatches(s1, s2);
    assertThat("Got " + Arrays.toString(matches), matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(fromPos));
    assertThat(matches[0].getToPos(), is(toPos));
    if (suggestion == null) {
      assertThat("Did not expect suggestion, but got: " + matches[0].getSuggestedReplacements(),
                 matches[0].getSuggestedReplacements().size(), is(0));
    } else {
      assertThat("Expected suggestion: " + suggestion + ", got: " + matches[0].getSuggestedReplacements(),
                 matches[0].getSuggestedReplacements(), is(Arrays.asList(suggestion)));
    }
  }

  private RuleMatch[] getMatches(String s1, String s2) throws IOException {
    AnalyzedSentence sent1 = lt.getAnalyzedSentence(s1);
    AnalyzedSentence sent2 = lt.getAnalyzedSentence(s2);
    return rule.match(Arrays.asList(sent1, sent2));
  }

}