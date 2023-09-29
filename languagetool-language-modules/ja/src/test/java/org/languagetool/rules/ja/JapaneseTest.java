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
package org.languagetool.rules.ja;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.ja.Japanese;

import java.io.IOException;

public class JapaneseTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    Japanese lang = new Japanese();
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    /*String s = "これわ文章を入力して'Check Text'をクリックすると、誤記を探すことができる。着色した文字をクリックすると、間違いの詳細の表示する。";
    testDemoText(lang, s,
      Arrays.asList("KOREWA", "DOUSI_KOTOGADEKIRU", "NO_SURU")
    );*/
    runTests(lang);
  }
}
