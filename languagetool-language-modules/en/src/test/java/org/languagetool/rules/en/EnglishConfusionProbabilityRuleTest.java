/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.English;
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EnglishConfusionProbabilityRuleTest {

  @Test
  public void testRule() throws IOException, ClassNotFoundException {
    JLanguageTool langTool = new JLanguageTool(new English());
    File languageModelFile = new File("src/test/resources/org/languagetool/languagemodel/frequency.dict");
    ConfusionProbabilityRule rule = new EnglishConfusionProbabilityRule(languageModelFile, TestTools.getEnglishMessages());
    langTool.addRule(rule);
    List<RuleMatch> matches = langTool.check("A portray of me");
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getSuggestedReplacements().get(0), is("portrait"));
  }

}
