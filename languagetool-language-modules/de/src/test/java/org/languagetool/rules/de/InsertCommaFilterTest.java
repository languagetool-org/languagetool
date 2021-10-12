/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class InsertCommaFilterTest {

  @Test
  public void testFilter() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));
    runFilter("Das Auto, das am Straßenrand steht parkt im Halteverbot.", 7,
      7, 8, 29, 40, "steht parkt", "[steht, parkt]", lt);
    runFilter("Hoffe bei euch ist alles gut.", 1,
      1, 4, 0, 7, "Hoffe bei euch ist", "[Hoffe, bei euch ist]", lt);
  }

  private void runFilter(String input, int patternTokenPos, int atrFromPos, int atrToPos, int fromPos, int toPos, String origRepl, String newRepl, JLanguageTool lt) throws IOException {
    HashMap<String,String> args = new HashMap<>();
    InsertCommaFilter filter = new InsertCommaFilter();
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    RuleMatch match = new RuleMatch(new FakeRule(), sentence, fromPos, toPos, "fake msg");
    match.setSuggestedReplacement(origRepl);
    AnalyzedTokenReadings[] atr = new AnalyzedTokenReadings[]{
      sentence.getTokensWithoutWhitespace()[atrFromPos],
      sentence.getTokensWithoutWhitespace()[atrToPos]
    };
    RuleMatch matchTmp = filter.acceptRuleMatch(match, args, patternTokenPos, atr);
    assertThat(matchTmp.getSuggestedReplacements().toString(), is(newRepl));
  }

}
