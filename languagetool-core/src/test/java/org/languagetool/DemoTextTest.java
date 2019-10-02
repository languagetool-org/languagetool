/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class DemoTextTest {

  protected void testDemoText(Language lang, String text, List<String> expectedMatchIds) throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    List<RuleMatch> matches = lt.check(text);
    int i = 0;
    List<String> actualRuleIds = new ArrayList<>();
    for (RuleMatch match : matches) {
      actualRuleIds.add(match.getRule().getId());
    }
    if (expectedMatchIds.size() != actualRuleIds.size()) {
      failTest(lang, text, expectedMatchIds, actualRuleIds);
    }
    for (String actualRuleId : actualRuleIds) {
      if (!expectedMatchIds.get(i).equals(actualRuleId)) {
        failTest(lang, text, expectedMatchIds, actualRuleIds);
      }
      i++;
    }
  }

  private void failTest(Language lang, String text, List<String> expectedMatchIds, List<String> actualRuleIds) {
    fail("The website demo text matches for " + lang + " have changed. Demo text:\n" + text +
            "\nExpected rule matches:\n" + expectedMatchIds + "\nActual rule matches:\n" + actualRuleIds);
  }
  
}
