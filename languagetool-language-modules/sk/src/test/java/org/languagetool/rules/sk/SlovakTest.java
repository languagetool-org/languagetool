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
package org.languagetool.rules.sk;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Slovak;

import java.io.IOException;

public class SlovakTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    //String s = "Toto je ukážkový vstup, na predvedenie funkčnosti LanguageTool. Pamätajte si si, že neobsahuje \"kontrolu\" preklepo."; -- commented out, as it's outdated
    Slovak lang = new Slovak();
    runTests(lang);
  }
}
