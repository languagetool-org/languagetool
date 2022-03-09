/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Stefan Viol
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
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.AbstractNumberInWordFilter;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class GermanNumberInWordFilterTest {

  @Test
  public void testFilter() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));
    runFilter("Schöne Grüß0e aus Potsdam.", "Grüß0e", "Grüße", 2, 0, 5, lt);
    runFilter("Schöne Grü0e aus Potsdam.", "Grü0e", "Grüße", 2, 0, 5, lt);
    runFilter("Der Aut0r schreibt ein Buch.", "Aut0r", "Autor", 2, 0, 5, lt);
  }

  private void runFilter(String input, String arg, String newRepl, int patternTokenPos, int fromPos, int toPos, JLanguageTool lt) throws IOException {
    AbstractNumberInWordFilter filter = new GermanNumberInWordFilter();
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    RuleMatch match = new RuleMatch(new FakeRule(), sentence, fromPos, toPos, "fake msg");
    HashMap<String, String> args = new HashMap<>();
    args.put("word", arg);
    RuleMatch matchTmp = filter.acceptRuleMatch(match, args, patternTokenPos, sentence.getTokensWithoutWhitespace());
    assertNotNull(matchTmp);
    assertTrue(matchTmp.getSuggestedReplacements().contains(newRepl));
  }
}
