/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class DemoPatternRuleTest extends PatternRuleTest {

  private static final Language language = TestTools.getDemoLanguage();

  @Test
  public void testRules() throws IOException {
    runTestForLanguage(new Demo());
  }

  @Test
  public void testGrammarRulesFromXML2() throws IOException {
    new PatternRule("-1", language, Collections.emptyList(), "", "", "");
  }

  @Test
  public void testMakeSuggestionUppercase() throws IOException {
    JLanguageTool lt = new JLanguageTool(language);

    PatternToken patternToken = new PatternToken("Were", false, false, false);
    String message = "Did you mean: <suggestion>where</suggestion> or <suggestion>we</suggestion>?";
    PatternRule rule = new PatternRule("MY_ID", language, Collections.singletonList(patternToken), "desc", message, "msg");
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Were are in the process of ..."));

    Assertions.assertEquals(1, matches.length);
    RuleMatch match = matches[0];
    List<String> replacements = match.getSuggestedReplacements();
    Assertions.assertEquals(2, replacements.size());
    Assertions.assertEquals("Where", replacements.get(0));
    Assertions.assertEquals("We", replacements.get(1));
  }

  @Test
  public void testRule() throws IOException {
    PatternRule pr;
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(language);

    pr = makePatternRule("one");
    matches = pr.match(lt.getAnalyzedSentence("A non-matching sentence."));
    Assertions.assertEquals(0, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("A matching sentence with one match."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(25, matches[0].getFromPos());
    Assertions.assertEquals(28, matches[0].getToPos());
    // these two are not set if the rule is called standalone (not via
    // JLanguageTool):
    Assertions.assertEquals(-1, matches[0].getColumn());
    Assertions.assertEquals(-1, matches[0].getLine());
    Assertions.assertEquals("ID1", matches[0].getRule().getId());
    Assertions.assertEquals("user visible message", matches[0].getMessage());
    Assertions.assertEquals("short comment", matches[0].getShortMessage());
    matches = pr.match(lt
            .getAnalyzedSentence("one one and one: three matches"));
    Assertions.assertEquals(3, matches.length);

    pr = makePatternRule("one two");
    matches = pr.match(lt.getAnalyzedSentence("this is one not two"));
    Assertions.assertEquals(0, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("this is two one"));
    Assertions.assertEquals(0, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("this is one two three"));
    Assertions.assertEquals(1, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("one two"));
    Assertions.assertEquals(1, matches.length);

    pr = makePatternRule("one|foo|xxxx two", false, true);
    matches = pr.match(lt.getAnalyzedSentence("one foo three"));
    Assertions.assertEquals(0, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("one two"));
    Assertions.assertEquals(1, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("foo two"));
    Assertions.assertEquals(1, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("one foo two"));
    Assertions.assertEquals(1, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("y x z one two blah foo"));
    Assertions.assertEquals(1, matches.length);

    pr = makePatternRule("one|foo|xxxx two|yyy", false, true);
    matches = pr.match(lt.getAnalyzedSentence("one, yyy"));
    Assertions.assertEquals(0, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("one yyy"));
    Assertions.assertEquals(1, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("xxxx two"));
    Assertions.assertEquals(1, matches.length);
    matches = pr.match(lt.getAnalyzedSentence("xxxx yyy"));
    Assertions.assertEquals(1, matches.length);
  }

  @Test
  public void testSentenceStart() throws IOException {
    JLanguageTool lt = new JLanguageTool(language);
    PatternRule pr = makePatternRule("SENT_START One");
    Assertions.assertEquals(0, pr.match(lt.getAnalyzedSentence("Not One word.")).length);
    Assertions.assertEquals(1, pr.match(lt.getAnalyzedSentence("One word.")).length);
  }

  @Test
  public void testFormatMultipleSynthesis() throws Exception {
    String[] suggestions1 = { "blah blah", "foo bar" };
    Assertions.assertEquals("This is how you should write: <suggestion>blah blah</suggestion>, <suggestion>foo bar</suggestion>.", PatternRuleMatcher.formatMultipleSynthesis(suggestions1,
            "This is how you should write: <suggestion>", "</suggestion>."));

    String[] suggestions2 = { "test", " " };
    Assertions.assertEquals("This is how you should write: <suggestion>test</suggestion>, <suggestion> </suggestion>.", PatternRuleMatcher.formatMultipleSynthesis(suggestions2,
            "This is how you should write: <suggestion>", "</suggestion>."));
  }

  private PatternRule makePatternRule(String s) {
    return makePatternRule(s, false, false);
  }

}
