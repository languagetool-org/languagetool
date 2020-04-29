/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://danielnaber.de)
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
package org.languagetool.rules.spelling;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class VagueSpellCheckerTest {
  
  @Test
  public void testIsValidWord() {
    Language de = Languages.getLanguageForShortCode("de-DE");
    Language en = Languages.getLanguageForShortCode("en-US");
    Language pt = Languages.getLanguageForShortCode("pt-PT");
    Language fr = Languages.getLanguageForShortCode("fr");
    VagueSpellChecker checker = new VagueSpellChecker();
    
    assertTrue(checker.isValidWord("vacation", en));
    assertTrue(checker.isValidWord("walks", en));
    assertFalse(checker.isValidWord("vacationx", en));
    
    assertTrue(checker.isValidWord("Hütte", de));
    assertTrue(checker.isValidWord("Hütten", de));
    assertFalse(checker.isValidWord("sdasfd", de));
    
    assertTrue(checker.isValidWord("termo", pt));
    assertFalse(checker.isValidWord("termoasq", pt));
    assertFalse(checker.isValidWord("difdsf", pt));
    
    assertTrue(checker.isValidWord("voiture", fr));
    assertFalse(checker.isValidWord("sduiofhdf", fr));
  }

}
