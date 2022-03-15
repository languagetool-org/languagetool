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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

public class VagueSpellCheckerTest {
  
  @Test
  public void testIsValidWord() {
    Language de = Languages.getLanguageForShortCode("de-DE");
    Language en = Languages.getLanguageForShortCode("en-US");
    Language pt = Languages.getLanguageForShortCode("pt-PT");
    Language fr = Languages.getLanguageForShortCode("fr");
    VagueSpellChecker checker = new VagueSpellChecker();
    
    Assertions.assertTrue(checker.isValidWord("vacation", en));
    Assertions.assertTrue(checker.isValidWord("walks", en));
    Assertions.assertFalse(checker.isValidWord("vacationx", en));
    
    Assertions.assertTrue(checker.isValidWord("Hütte", de));
    Assertions.assertTrue(checker.isValidWord("Hütten", de));
    Assertions.assertFalse(checker.isValidWord("sdasfd", de));
    
    Assertions.assertTrue(checker.isValidWord("termo", pt));
    Assertions.assertFalse(checker.isValidWord("termoasq", pt));
    Assertions.assertFalse(checker.isValidWord("difdsf", pt));
    
    Assertions.assertTrue(checker.isValidWord("voiture", fr));
    Assertions.assertFalse(checker.isValidWord("sduiofhdf", fr));
  }

}
