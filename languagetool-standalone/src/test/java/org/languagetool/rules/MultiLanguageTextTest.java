/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.IOException;


import static org.junit.Assert.assertEquals;

public class MultiLanguageTextTest {

  private static final German GERMAN_DE = (German) Languages.getLanguageForShortCode("de-DE");

  @Test
  public void testEnglishInGermanDetected() throws IOException {
    HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);

    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("He is a very cool guy from Poland."));
    RuleMatch lastMatch1 = matches1[matches1.length - 1];
    assertEquals(lastMatch1.getErrorLimitLang(), "en");

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("How are you?"));
    RuleMatch lastMatch2 = matches2[matches2.length - 1];
    assertEquals(lastMatch2.getErrorLimitLang(), "en");

    RuleMatch[] matches3 = rule.match(lt.getAnalyzedSentence("CONFIDENTIALITY NOTICE:"));
    RuleMatch lastMatch3 = matches3[matches3.length - 1];
    assertEquals(lastMatch3.getErrorLimitLang(), "en");
  }
}
