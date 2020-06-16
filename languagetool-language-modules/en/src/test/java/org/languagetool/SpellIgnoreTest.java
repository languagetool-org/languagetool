/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SpellIgnoreTest {
  
  // code also used in http://wiki.languagetool.org/java-api
  @Test
  public void testIgnore() throws IOException {
    String text = "This is a text with specialword and myotherword";
    JLanguageTool lt = new JLanguageTool(new AmericanEnglish());
    assertThat(lt.check(text).size(), is(2));
    for (Rule rule : lt.getAllActiveRules()) {
      if (rule instanceof SpellingCheckRule) {
        List<String> wordsToIgnore = Arrays.asList("specialword", "myotherword");
        ((SpellingCheckRule)rule).addIgnoreTokens(wordsToIgnore);
      }
    }
    assertThat(lt.check(text).size(), is(0));
  }

}
