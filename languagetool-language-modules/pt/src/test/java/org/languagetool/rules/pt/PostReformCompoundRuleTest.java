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

public class PostReformCompoundRuleTest {
  JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-BR"));
  @Test
  public void test() throws IOException {
    TestTools.disableAllRulesExcept(lt, "PT_COMPOUNDS_POST_REFORM");
    // in the compounds TXT as "super-herói*"
    assertThat(checkCompound("super-herói").size(), is(0));
    assertThat(checkCompound("super herói").size(), is(1));
    assertThat(checkCompound("super herói").get(0).getSuggestedReplacements().size(), is(1));
    assertThat(checkCompound("super herói").get(0).getSuggestedReplacements().get(0), is("super-herói"));
    // in the compounds TXT as "super-estrela?"
    assertThat(checkCompound("Super estrela").size(), is(1));
    assertThat(checkCompound("Super estrela").get(0).getSuggestedReplacements().size(), is(1));
    assertThat(checkCompound("Super estrela").get(0).getSuggestedReplacements().get(0), is("Superestrela"));
    // in the compounds TXT as "web-site+"
    assertThat(checkCompound("web-site").size(), is(1));
    assertThat(checkCompound("web-site").get(0).getSuggestedReplacements().size(), is(1));
    assertThat(checkCompound("web-site").get(0).getSuggestedReplacements().get(0), is("website"));
    // in the compounds TXT as "Grã-Bretanha*"
    assertThat(checkCompound("Grã Bretanha").size(), is(1));
    assertThat(checkCompound("Grã Bretanha").get(0).getSuggestedReplacements().size(), is(1));
    assertThat(checkCompound("Grã Bretanha").get(0).getSuggestedReplacements().get(0), is("Grã-Bretanha"));
    // in the compounds TXT as "ultra-som?"
    assertThat(checkCompound("ultra-som").size(), is(1));
    assertThat(checkCompound("ultra-som").get(0).getSuggestedReplacements().size(), is(1));
    assertThat(checkCompound("ultra-som").get(0).getSuggestedReplacements().get(0), is("ultrassom"));
    // in the compounds TXT as "ultra-realismo?"
    assertThat(checkCompound("ultra-realismo").size(), is(1));
    assertThat(checkCompound("ultra-realismo").get(0).getSuggestedReplacements().size(), is(1));
    assertThat(checkCompound("ultra-realismo").get(0).getSuggestedReplacements().get(0), is("ultrarrealismo"));
    // in the compounds TXT as "anti-semita?"
    assertThat(checkCompound("anti semita").size(), is(1));
    assertThat(checkCompound("anti semita").get(0).getSuggestedReplacements().size(), is(1));
    assertThat(checkCompound("anti semita").get(0).getSuggestedReplacements().get(0), is("antissemita"));
  }

  private List<RuleMatch> checkCompound(String text) throws IOException {
    return lt.check(new AnnotatedTextBuilder().addText(text).build(), true,
      JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL, JLanguageTool.Level.PICKY);
  }
}
