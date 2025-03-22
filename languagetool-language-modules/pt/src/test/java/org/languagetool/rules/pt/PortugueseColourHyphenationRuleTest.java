/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PortugueseColourHyphenationRuleTest {
  JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-BR"));
  @Test
  public void test() throws IOException {
    TestTools.disableAllRulesExcept(lt, "PT_COLOUR_HYPHENATION");
    assertThat(checkCompound("azul-claro").size(), is(0));
    assertThat(checkCompound("azul claro").size(), is(1));
    assertThat(checkCompound("azul claro").get(0).getSpecificRuleId(),
      is("PT_COLOUR_HYPHENATION_AZUL_CLARO"));
  }

  private List<RuleMatch> checkCompound(String text) throws IOException {
    return lt.check(new AnnotatedTextBuilder().addText(text).build(), true,
      JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL, JLanguageTool.Level.PICKY);
  }
}
