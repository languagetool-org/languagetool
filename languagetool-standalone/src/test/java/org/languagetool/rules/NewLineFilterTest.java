/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2025 Stefan Viol (https://stevio.de)
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

package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewLineFilterTest {

  @Test
  public void testFilter() throws IOException {
    var lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));
    var filter = new NewLineMatchFilter();

    var text1 = "I’m+the+Heeadline\n\n\n\n\u2063\n\n\n\nI’m+some+plain+teext.";
    test(text1, lt, filter, 8, 21, "Headline\n\n\n\n\u2063\n\n\n\n" , 8, 17, true,"Headline", true);
    test(text1, lt, filter, 8, 21, "Heeadline\n\n\n\n\u2063\n\n\n\n" , 8, 17, false,null, true);
  }

  public void test(String text,
                   JLanguageTool lt,
                   NewLineMatchFilter filter,
                   int fromPosBeforeFilter,
                   int toPosBeforeFilter,
                   String suggestionBeforeFilter,
                   int fromPosAfterFilter,
                   int toPosAfterFilter,
                   boolean expectedMatchesAfterFilter,
                   String suggestionAfterFilter,
                   boolean printDebug) throws IOException{
    var sentence = lt.analyzeText(text).get(0);
    var ruleMatch = new RuleMatch(new FakeRule(), sentence, fromPosBeforeFilter, toPosBeforeFilter, "match1");
    ruleMatch.setSuggestedReplacement(suggestionBeforeFilter);

    var filteredRuleMatch = filter.filter(Collections.singletonList(ruleMatch), new AnnotatedTextBuilder().addText(text).build());
    if (expectedMatchesAfterFilter) {

      var matchToCheck = filteredRuleMatch.get(0);
      var matchedTextAfterFilter = text.substring(matchToCheck.getFromPos(), matchToCheck.getToPos());

      if (printDebug) {
        System.out.println("This is the matched text after filter:\n\"" + matchedTextAfterFilter + "\"");
        System.out.println("Suggestion  replacements after filter:\n\"" + matchToCheck.getSuggestedReplacements().get(0) + "\"");
      }

      assertEquals(1, filteredRuleMatch.size());
      assertEquals(fromPosAfterFilter, matchToCheck.getFromPos());
      assertEquals(toPosAfterFilter, matchToCheck.getToPos());
      assertTrue(matchToCheck.getSuggestedReplacements().contains(suggestionAfterFilter));
    } else {
      assertTrue(filteredRuleMatch.isEmpty());
    }
  }
}
