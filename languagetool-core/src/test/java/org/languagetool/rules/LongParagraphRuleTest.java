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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LongParagraphRuleTest {

  @Test
  public void testRule() throws IOException {
    Demo lang = new Demo();
    LongParagraphRule rule = new LongParagraphRule(TestTools.getEnglishMessages(), lang, null, 6);
    JLanguageTool lt = new JLanguageTool(lang);
    assertThat(rule.match(lt.analyzeText("This is a short paragraph.")).length, is(0));
    assertThat(rule.match(lt.analyzeText("This is only almost long paragraph by unit test standards.")).length, is(0));
    assertThat(rule.match(lt.analyzeText("Here's some text as a filler. This is a long paragraph by unit test standards.")).length, is(1));
    assertThat(rule.match(lt.analyzeText("Here's some text as a filler.  A test. A long paragraph by unit test standards.")).length, is(1));
    assertThat(rule.match(lt.analyzeText("Here's some text as a filler.  A test.\nNot a long paragraph.\nBecause of the line breaks.\n")).length, is(0));
    assertThat(rule.match(lt.analyzeText("- [ ] A test.\n- [ ] Not a long paragraph.\n- [ ] Because of the line breaks.\n- [ ] More text even.\n")).length, is(0));

    String text1 = "This is a short paragraph.\n\nHere's some text as filler. This is a long paragraph by unit test standards.";
    RuleMatch[] matches1 = rule.match(lt.analyzeText(text1));
    assertThat(matches1.length, is(1));
    assertThat(matches1[0].getFromPos(), is(48));  // "filler"
    assertThat(matches1[0].getToPos(), is(54));

    String text2 = "Here's some text as filler. This is a long paragraph by unit test standards.\n\nAnother paragraph.\n\nHere's some text as morefiller - this is a long paragraph by unit test standards.";
    RuleMatch[] matches2 = rule.match(lt.analyzeText(text2));
    assertThat(matches2.length, is(2));
    assertThat(matches2[0].getFromPos(), is(20));  // "filler"
    assertThat(matches2[0].getToPos(), is(26));
    assertThat(matches2[1].getFromPos(), is(118));  // "morefiller"
    assertThat(matches2[1].getToPos(), is(128));
  }

}
