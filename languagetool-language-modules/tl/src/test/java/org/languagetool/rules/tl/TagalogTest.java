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
package org.languagetool.rules.tl;

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Tagalog;

import java.io.IOException;
import java.util.Arrays;

public class TagalogTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Ginagamit ang LanguageTool para sa pagwawasto ng gramatika at ispeling, na ginagamit arawaraw ng maraming tao. Ipinapakita ng talatang ito kung paano nito mahuhuli ang mga pagkakamali, tulad ng maling baybay ng kwento. Ang pang ulo ng bansa din ay maaaring makinabang sa kakayahan ng LanguageTool.";
    Tagalog lang = new Tagalog();
    testDemoText(lang, s,
      Arrays.asList("TL_REDUPLICATION", "KAMBAL_PATINIG_KUWENTO", "TL_PANG_VOWEL_WORD", "KASONG_DIN")
    );
    runTests(lang);
  }
}
