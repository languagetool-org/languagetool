/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (www.danielnaber.de)
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
import org.languagetool.language.BrazilianPortuguese;
import org.languagetool.language.PortugalPortuguese;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PortugueseDiacriticsRuleTest {

  @Test
  public void test() throws IOException {
    JLanguageTool lt_pt = new JLanguageTool(new PortugalPortuguese());
    JLanguageTool lt_br = new JLanguageTool(new BrazilianPortuguese());

    TestTools.disableAllRulesExcept(lt_pt, "DIACRITICS");
    TestTools.disableAllRulesExcept(lt_br, "DIACRITICS");

    String testSentence = "Carrinho de bebe.";
    assertThat(checkDiacritics(lt_pt, testSentence).get(0).getSuggestedReplacements().get(0), is("bebé"));
    assertThat(checkDiacritics(lt_br, testSentence).get(0).getSuggestedReplacements().get(0), is("bebê"));
  }

  private List<RuleMatch> checkDiacritics(JLanguageTool lt, String sentence) throws IOException {
    return lt.check(new AnnotatedTextBuilder().addText(sentence).build(), true,
      JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL, JLanguageTool.Level.PICKY);
  }
}
