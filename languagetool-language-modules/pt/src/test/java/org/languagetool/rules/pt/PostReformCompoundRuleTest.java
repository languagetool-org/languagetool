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
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PostReformCompoundRuleTest {
  JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-BR"));
  @Test
  public void testPostReformCompounds() throws IOException {
    TestTools.disableAllRulesExcept(lt, "PT_COMPOUNDS_POST_REFORM");
    assertValidCompound("super-herói");
    assert checkCompound("super herói").size() == 1;
    assert Objects.equals(checkCompound("super herói").get(0).getSpecificRuleId(),
      "PT_COMPOUNDS_POST_REFORM_SUPER_HERÓI");
    assert Objects.equals(checkCompound("super herói").get(0).getRule().getDescription(),
      "Erro na formação da palavra composta \"super herói\"");

    assertInvalidCompound("Super estrela", "Superestrela");
    assertInvalidCompound("web-site", "website");
    assertInvalidCompound("Grã Bretanha", "Grã-Bretanha");
    assertInvalidCompound("ultra-som", "ultrassom");
    assertInvalidCompound("ultra-realismo", "ultrarrealismo");
    assertInvalidCompound("anti semita", "antissemita");
    assertInvalidCompound("arqui-rabino", "arquirrabino");
    assertInvalidCompound("ópera rock", "ópera-rock");
  }

  private List<RuleMatch> checkCompound(String text) throws IOException {
    return lt.check(new AnnotatedTextBuilder().addText(text).build(), true,
      JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL, JLanguageTool.Level.PICKY);
  }

  private void assertValidCompound(String text) throws IOException {
    assert checkCompound(text).isEmpty();
  }

  private void assertInvalidCompound(String text, String suggestion) throws IOException {
    List<RuleMatch> checkedCompound = checkCompound(text);
    assert checkedCompound.size() == 1;
    List<String> suggestions = checkedCompound.get(0).getSuggestedReplacements();
    assert suggestions.size() == 1;
    assert Objects.equals(suggestions.get(0), suggestion);
  }
}
