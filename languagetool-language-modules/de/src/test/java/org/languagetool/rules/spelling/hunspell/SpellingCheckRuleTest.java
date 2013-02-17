/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling.hunspell;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SpellingCheckRuleTest extends TestCase {

  public void testIgnoreSuggestionsWithHunspell() throws IOException {
    final JLanguageTool langTool = new JLanguageTool(new GermanyGerman());

    final List<RuleMatch> matches = langTool.check("Das ist ein einPseudoWortFürLanguageToolTests");
    assertEquals(0, matches.size());   // no error, as this word is in ignore.txt

    final List<RuleMatch> matches2 = langTool.check("Das ist ein Tibbfehla");
    assertEquals(1, matches2.size());
    Assert.assertEquals(GermanSpellerRule.RULE_ID, matches2.get(0).getRule().getId());
  }

  public void testIgnoreSuggestionsWithDynamicHunspellRule() throws IOException {
    final JLanguageTool langTool = new JLanguageTool(new GermanyGerman());
    final SpellingCheckRule rule = new HunspellNoSuggestionRule(TestTools.getEnglishMessages(), new GermanyGerman());
    langTool.addRule(rule);
    langTool.disableRule(GermanSpellerRule.RULE_ID);
    final List<RuleMatch> matches = langTool.check("Das ist ein Tibbfehla.");
    assertEquals(1, matches.size());
    assertEquals(HunspellNoSuggestionRule.RULE_ID, matches.get(0).getRule().getId());

    final PatternRule ruleWithSuggestion = new PatternRule("TEST_ID", new GermanyGerman(),
            Collections.<Element>emptyList(), "description",
            "Meinten Sie <suggestion>Tibbfehla</suggestion>?", null);
    langTool.addRule(ruleWithSuggestion);
    final List<RuleMatch> matches2 = langTool.check("Das ist ein Tibbfehla.");
    assertEquals(0, matches2.size());   // no error anymore, as this is a suggestion

    langTool.disableRule("TEST_ID");
    final List<RuleMatch> matches3 = langTool.check("Das ist ein Tibbfehla.");
    assertEquals(1, matches3.size());   // an error again
  }

}
