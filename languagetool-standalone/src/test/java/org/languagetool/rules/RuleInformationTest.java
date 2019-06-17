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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.Languages;

import static org.junit.Assert.*;

public class RuleInformationTest {

  @Test
  public void testRuleInformation() {
    assertFalse(RuleInformation.ignoreForIncompleteSentences("NO", Languages.getLanguageForShortCode("en")));
    assertFalse(RuleInformation.ignoreForIncompleteSentences("A_UNCOUNTABLE", Languages.getLanguageForShortCode("de")));
    
    assertTrue(RuleInformation.ignoreForIncompleteSentences("A_UNCOUNTABLE", Languages.getLanguageForShortCode("en")));
    assertTrue(RuleInformation.ignoreForIncompleteSentences("A_UNCOUNTABLE", Languages.getLanguageForShortCode("en-US")));
    assertTrue(RuleInformation.ignoreForIncompleteSentences("A_UNCOUNTABLE", Languages.getLanguageForShortCode("en-GB")));
    assertTrue(RuleInformation.ignoreForIncompleteSentences("FOLGENDES", Languages.getLanguageForShortCode("de")));
  }
}
