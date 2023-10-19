/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://danielnaber.de/)
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.UserConfig;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LongSentenceRuleTest extends org.languagetool.rules.LongSentenceRuleTest {

  @Test
  public void testMatch() throws Exception {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    LongSentenceRule rule = new LongSentenceRule(TestTools.getMessages("de"), new UserConfig(), 6);
    
    assertNoMatch("Eins zwei drei vier fünf sechs.", rule, lt);
    //  Words after colon are treated like a separate sentence
    assertNoMatch("Ich zähle jetzt: \"Eins zwei drei vier fünf sechs.\"", rule, lt);
    
    assertMatch("Eins zwei drei vier fünf sechs sieben.", 0, 37, rule, lt);
    assertMatch("Eins zwei drei vier fünf (sechs sieben) acht.", 0, 44, rule, lt);
    assertMatch("Ich zähle jetzt: Eins zwei drei vier fünf sechs sieben.", 0, 54, rule, lt);
    assertMatch("Ein Satz. Eins zwei drei vier fünf sechs sieben.", 10, 47, rule, lt);
  }

  protected void assertNoMatch(String input, TextLevelRule rule, JLanguageTool lt) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(input).build();
    assertThat(rule.match(lt.analyzeText(input), aText).length, is(0));
  }

  protected void assertMatch(String input, int from, int to, TextLevelRule rule, JLanguageTool lt) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(input).build();
    RuleMatch[] matches = rule.match(lt.analyzeText(input), aText);
    assertThat(matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(from));
    assertThat(matches[0].getToPos(), is(to));
  }
  
}
