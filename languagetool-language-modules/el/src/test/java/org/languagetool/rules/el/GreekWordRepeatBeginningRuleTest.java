/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.el;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


public class GreekWordRepeatBeginningRuleTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("el"));
    
    // ================== correct sentences ===================
    // two successive sentences that start with the same non-adverb word.
    assertEquals(0, lt.check("Εγώ παίζω ποδόσφαιρο. Εγώ παίζω μπάσκετ").size());
    // three successive sentences that start with the same exception word ("Το").
    assertEquals(0, lt.check("Το αυτοκίνητο είναι καινούργιο. Το ποδήλατο είναι παλιό. Το καράβι είναι καινούργιο.").size());
    
    // =================== errors =============================
    // two successive sentences that start with one of the saved adverbs ("Επίσης").
    List<RuleMatch> matches2 = lt.check("Επίσης, μιλάω Ελληνικά. Επίσης, μιλάω Αγγλικά.");
    assertEquals(1, matches2.size());
    // check suggestions (because the adverbs are contained in a Set it is safer to check if the correct suggestions
    // are contained in the real suggestions)
    assertTrue(matches2.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Επιπλέον")));
    assertTrue(matches2.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Ακόμη")));
    assertTrue(matches2.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Επιπρόσθετα")));
    assertTrue(matches2.get(0).getSuggestedReplacements().stream().anyMatch(sugg ->  sugg.equals("Συμπληρωματικά")));
    // three successive sentences that start with the same non-exception word (no suggestions to check).
    List<RuleMatch> matches1 = lt.check("Εγώ παίζω μπάσκετ. Εγώ παίζω ποδόσφαιρο. Εγώ παίζω βόλεϊ.");
    assertEquals(1, matches1.size());
  }

}