/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleTest;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * base class to test rules in remote-rule-filters.xml, like PatternRuleTest for grammar.xml
 */
public class RemoteRuleFilterTest extends PatternRuleTest {

  @Override
  protected List<String> getGrammarFileNames(Language lang) {
    return Collections.singletonList(RemoteRuleFilters.getFilename(lang));
  }

  @Override
  public void runTestForLanguage(Language lang) throws IOException {
    // looser requirements for rules, e.g. empty name;
    // skip validation, if invalid, RemoteRuleFilters.load will fail anyway
    //validatePatternFile(lang);
    //
    System.out.println("Running remote rule filter tests for " + lang.getName() + "... ");
    MultiThreadedJLanguageTool lt = createToolForTesting(lang);

    List<AbstractPatternRule> rules = RemoteRuleFilters.load(lang)
      .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    MultiThreadedJLanguageTool allRulesLt = new MultiThreadedJLanguageTool(lang);
    allRulesLt.disableRules(allRulesLt.getAllRules().stream().map(Rule::getId).collect(Collectors.toList()));
    rules.forEach(allRulesLt::addRule);

    validateRuleIds(lang, allRulesLt);
    validateSentenceStartNotInMarker(allRulesLt);
    testRegexSyntax(lang, rules);
    testExamplesExist(rules);
    testGrammarRulesFromXML(rules, allRulesLt, lang);
    System.out.println(rules.size() + " rules tested.");
    allRulesLt.shutdown();
    lt.shutdown();
  }

}
