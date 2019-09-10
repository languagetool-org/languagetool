/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class FrenchCompoundAwareHunspellRuleTest {
  
  @Test
  public void testSpellcheck() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("fr"));
    TestTools.disableAllRulesExcept(lt, "FR_SPELLING_RULE");
    assertSuggestion(lt, "Parcontre", "Par contre");  // see #1797
    assertSuggestion(lt, "parcontre", "par contre");  // see #1797
    assertSuggestion(lt, "Ca", "Ça");  // see #912
    assertSuggestion(lt, "Décu", "Déçu");  // see #912
    assertSuggestion(lt, "etant", "étant");  // see #1633
    assertSuggestion(lt, "Cliqez", "Cliquez");
    assertSuggestion(lt, "cliqez", "cliquez");
    assertSuggestion(lt, "offe", "effet", "offre");  // "offre" would be better as first suggestion? 
    assertSuggestion(lt, "problemes", "problèmes"); 
    assertSuggestion(lt, "coulurs", "couleurs"); 
    assertSuggestion(lt, "boton", "bot on", "bâton", "béton");  // "bouton" would be better? 
  }

  private void assertSuggestion(JLanguageTool lt, String input, String... expected) throws IOException {
    List<RuleMatch> matches = lt.check(input);
    assertThat(matches.size(), is(1));
    int i = 0;
    for (String s : expected) {
      assertThat("Got " + matches.get(0).getSuggestedReplacements(), matches.get(0).getSuggestedReplacements().get(i++), is(s));
    }
  }

}