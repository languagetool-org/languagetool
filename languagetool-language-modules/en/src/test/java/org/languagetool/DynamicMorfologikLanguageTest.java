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
package org.languagetool;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DynamicMorfologikLanguageTest {

  // in English package as DynamicMorfologikLanguage needs the English messages
  @Test
  public void test() throws IOException {
    URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/en/hunspell/en_US.dict");
    DynamicMorfologikLanguage lang = new DynamicMorfologikLanguage("Testlang", "zz", new File(url.getFile()));
    JLanguageTool lt = new JLanguageTool(lang);
    lt.check("test");  // just make sure we don't crash
  }

}
