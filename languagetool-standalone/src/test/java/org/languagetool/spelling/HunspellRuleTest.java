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
package org.languagetool.spelling;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class HunspellRuleTest {

  @Test
  public void testRuleWithGermanAndAltLang() throws Exception {
    List<Language> altLangs = Arrays.asList(Languages.getLanguageForShortCode("en-US"));
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"), altLangs, null, null, null, null);
    List<RuleMatch> matches = lt.check("Der ROI ist schoon.");
    assertThat(matches.size(), is(1));
    assertTrue(matches.get(0).getMessage().contains("Tippfehler"));
  }
  
}
