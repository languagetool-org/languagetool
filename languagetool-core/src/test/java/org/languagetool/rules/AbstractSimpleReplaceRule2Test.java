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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AbstractSimpleReplaceRule2Test {
  
  @Test
  public void testRule() throws IOException {
    Demo lang = new Demo();
    MyCaseSensitiveRule csRule = new MyCaseSensitiveRule(JLanguageTool.getMessageBundle(), lang);
    JLanguageTool lt = new JLanguageTool(lang);
    assertThat(csRule.match(lt.getAnalyzedSentence("But a propos")).length, is(1));
    assertThat(csRule.match(lt.getAnalyzedSentence("But A propos")).length, is(0));
    assertThat(csRule.match(lt.getAnalyzedSentence("A propos")).length, is(0));
    assertThat(csRule.match(lt.getAnalyzedSentence("a propos")).length, is(1));
    assertThat(csRule.match(lt.getAnalyzedSentence("A Pokemon")).length, is(1));
    assertThat(csRule.match(lt.getAnalyzedSentence("A pokemon")).length, is(0));

    MyCaseInsensitiveRule ciRule = new MyCaseInsensitiveRule(JLanguageTool.getMessageBundle(), lang);
    assertThat(ciRule.match(lt.getAnalyzedSentence("But a propos")).length, is(1));
    assertThat(ciRule.match(lt.getAnalyzedSentence("But A propos")).length, is(1));
    assertThat(ciRule.match(lt.getAnalyzedSentence("A propos")).length, is(1));
    assertThat(ciRule.match(lt.getAnalyzedSentence("a propos")).length, is(1));
    assertThat(ciRule.match(lt.getAnalyzedSentence("A Pokemon")).length, is(1));
    assertThat(ciRule.match(lt.getAnalyzedSentence("A pokemon")).length, is(1));
  }
  
  static class MyCaseSensitiveRule extends AbstractSimpleReplaceRule2 {
    MyCaseSensitiveRule(ResourceBundle messages, Language language) {
      super(messages, language);
    }
    @Override
    public List<String> getFileNames() {
      return Collections.singletonList("/xx/abstract_simple_replace2.txt");
    }
    @Override
    public String getId() {
      return "ABSTRACT_TEST_RULE";
    }
    @Override
    public String getDescription() {
      return "internal test rule";
    }
    @Override
    public String getShort() {
      return "internal test rule";
    }
    @Override
    public String getMessage() {
      return "fake suggestion";
    }
    @Override
    public String getSuggestionsSeparator() {
      return ",";
    }
    @Override
    public Locale getLocale() {
      return Locale.ENGLISH;
    }
    @Override
    public CaseSensitivy getCaseSensitivy() {
      return CaseSensitivy.CS;
    }
  }

  static class MyCaseInsensitiveRule extends MyCaseSensitiveRule {
    MyCaseInsensitiveRule(ResourceBundle messages, Language language) {
      super(messages, language);
    }
    public CaseSensitivy getCaseSensitivy() {
      return CaseSensitivy.CI;
    }
  }
}
