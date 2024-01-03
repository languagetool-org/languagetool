/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.it;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.it.Italian;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class ItalianWordRepeatRuleTest {

  @Test
  public void testRule() throws IOException {
    Italian lang = new Italian();
    ItalianWordRepeatRule rule = new ItalianWordRepeatRule(TestTools.getEnglishMessages(), lang);
    JLanguageTool lt = new JLanguageTool(lang);
    assertThat(rule.match(lt.getAnalyzedSentence("Mi è sembrato così così")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Duran Duran")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Mi mi è sembrato così")).length, is(1));
  }

}
