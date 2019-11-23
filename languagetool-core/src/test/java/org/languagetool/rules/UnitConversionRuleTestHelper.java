/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.Arrays;

public class UnitConversionRuleTestHelper {
  private boolean verbose = false;

  public UnitConversionRuleTestHelper() {
  }

  public UnitConversionRuleTestHelper(boolean verbose) {
    this.verbose = verbose;
  }

  public void assertMatches(String input, int expectedMatches, String converted, AbstractUnitConversionRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    if (verbose) {
      System.out.println("----------------------------------------");
      System.out.println(input);
      for (RuleMatch match : matches) {
        System.out.println(match);
        System.out.println(match.getSuggestedReplacements());
      }
    }
    Assert.assertThat("Got matches: " + Arrays.toString(matches), matches.length, CoreMatchers.is(expectedMatches));
    if (expectedMatches > 0 && converted != null) {
      RuleMatch match = matches[0];
      boolean suggestionCorrect = false;
      String suggestion = null;
      for (String s : match.getSuggestedReplacements()) {
        if (s.contains(converted)) {
          suggestionCorrect = true;
          suggestion = s;
          break;
        }
      }
      Assert.assertTrue("Suggestion is correct: " + suggestion + " / expected: " + converted, suggestionCorrect);
    }
  }
}
