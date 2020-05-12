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
package org.languagetool.rules;

import org.junit.jupiter.api.Test;
import org.languagetool.*;
import org.languagetool.language.Demo;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CustomSpellingTest {
  
  @Test
  public void testSpellingCustomTxt() throws IOException {
    Demo lang = new Demo();
    HunspellRule rule = new HunspellRule(TestTools.getEnglishMessages(), lang, null);
    JLanguageTool lt = new JLanguageTool(lang);
    assertThat(rule.match(lt.getAnalyzedSentence("das ist richtig")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("das ist ihfsdsdfi")).length, is(1));
    assertThat(rule.match(lt.getAnalyzedSentence("das ist auchokay")).length, is(0));  // listed in spelling_custom.txt
    assertThat(rule.match(lt.getAnalyzedSentence("das ist falsch")).length, is(1));    // listed in prohibit_custom.txt
  }
  
}
