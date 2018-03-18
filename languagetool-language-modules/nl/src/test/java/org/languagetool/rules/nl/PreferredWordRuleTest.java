/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.nl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

public class PreferredWordRuleTest {
  
  @Test
  public void test() throws IOException {
    Language dutch = Languages.getLanguageForShortCode("nl");
    PreferredWordRule rule = new PreferredWordRule(JLanguageTool.getMessageBundle());
    JLanguageTool lt = new JLanguageTool(dutch);

    AnalyzedSentence sentence1 = lt.getAnalyzedSentence("rijwiel");
    assertThat(rule.match(sentence1).length, is(1));
    assertThat(rule.match(sentence1)[0].getSuggestedReplacements().toString(), is("[fiets]"));
  }

}
