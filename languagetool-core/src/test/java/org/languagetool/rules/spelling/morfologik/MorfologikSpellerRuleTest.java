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
package org.languagetool.rules.spelling.morfologik;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MorfologikSpellerRuleTest {

  @Test
  public void testCleanTranslationForSuffix() throws IOException {
    MorfologikSpellerRule rule = new MorfologikSpellerRule(JLanguageTool.getMessageBundle(), new Demo()) {
      @Override
      public String getFileName() { return null; }
      @Override
      public String getId() { return null; }
    };
    assertThat(rule.cleanTranslationForSuffix(""), is(""));
    assertThat(rule.cleanTranslationForSuffix(" "), is(""));
    assertThat(rule.cleanTranslationForSuffix("foo bar"), is(""));
    assertThat(rule.cleanTranslationForSuffix("foo bar [Br.]"), is("[Br.]"));
    assertThat(rule.cleanTranslationForSuffix("foo bar {ugs} [Br.]"), is("{ugs} [Br.]"));
    assertThat(rule.cleanTranslationForSuffix("foo bar {ugs} [Br.] (Blah)"), is("{ugs} [Br.] (Blah)"));
    //assertThat(rule.cleanTranslationForAddition("foo (Blah {m})"), is("(Blah {m})"));  // nesting not supported yet
  }

}
