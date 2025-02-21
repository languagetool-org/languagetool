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
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class BorderNumberRuleTest {
  private JLanguageTool lt;

  @Before
  public void setUp() {
    lt = new JLanguageTool(TestTools.getDemoLanguage());

    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }

    BorderNumberRule rule = new BorderNumberRule(TestTools.getEnglishMessages(), TestTools.getDemoLanguage());

    lt.addRule(rule);
  }

  @Test
  public void testRule_happyPath_noMatchFound() throws IOException {
    List<RuleMatch> matches;

    matches = lt.check("1000   Das ist eine Randnummer.");

    assertEquals(0, matches.size());
  }

  @Test
  public void testRule_noBorderNumber_MatchFound() throws IOException {
    List<RuleMatch> matches;

    matches = lt.check("Das ist das 1000 Mal, dass ich sage es ist keine Randnummer.");

    assertEquals(1, matches.size());
  }

  @Test
  public void testRule_allowedUppercase_forNouns() throws IOException {
    List<RuleMatch> matches;

    matches = lt.check("Das ist das 1000 Teller, dass ich sage es ist keine Randnummer.");

    assertEquals(1, matches.size());
  }
}
