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
    List<RuleMatch> matches1 = lt.check("Ca");
    assertThat(matches1.size(), is(1));
    assertThat(matches1.get(0).getSuggestedReplacements().get(0), is("Ça"));   // see #912
    List<RuleMatch> matches2 = lt.check("Décu");
    assertThat(matches2.size(), is(1));
    assertThat(matches2.get(0).getSuggestedReplacements().get(0), is("Déçu"));   // see #912
  }

}