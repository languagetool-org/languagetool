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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class WordRepeatRuleTest {

  private final Demo demoLanguage = new Demo();
  private final JLanguageTool lt = new JLanguageTool(demoLanguage);
  private final WordRepeatRule rule = new WordRepeatRule(TestTools.getEnglishMessages(), demoLanguage);

  @Test
  public void test() throws IOException {
    assertGood("A test");
    assertGood("A test.");
    assertGood("A test...");
    assertGood("1 000 000 years");
    assertGood("010 020 030");

    assertBad("A A test");
    assertBad("A a test");
    assertBad("This is is a test");
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(0));
  }

  private void assertBad(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(1));
  }

}