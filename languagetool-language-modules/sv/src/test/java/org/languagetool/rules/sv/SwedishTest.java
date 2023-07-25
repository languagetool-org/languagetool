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
package org.languagetool.rules.sv;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Swedish;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

public class SwedishTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    Language sv = new Swedish();
    JLanguageTool ltSwedish = sv.createDefaultJLanguageTool();
    File svNgramsIndex = new File("/data/ngram-index");
    ltSwedish.activateLanguageModelRules(svNgramsIndex);
    runTests(sv);
  }

  @Test
  public void testSpellingAndColon() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Swedish());
    assertThat(lt.check("Arbeta med var:").size(), is(0));
  }
}
