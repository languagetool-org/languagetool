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
package org.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.tagging.disambiguation.uk.UkrainianHybridDisambiguator;

/**
 * Calls a test from core, because only here in stand-alone all languages 
 * are available.
 */
public class StandaloneDisambiguationRuleTest {

  private static final List<String> RULES_REQUIRING_JAVA_LOGIC = 
      Arrays.asList("SINGLE_CHAR_ABBR_NO_CAPS", "DIS_PREP_OR_NOUN_DO", "DIS_PREP_AND_INTJ_O", "RIK_VS_ROK_1", "RIK_VS_ROK_2");

  @Test
  public void testDisambiguationRuleTest() throws Exception {
    DisambiguationRuleTest test = new DisambiguationRuleTest() {
      @Override
      protected AnalyzedSentence disambiguateUntil(List<DisambiguationPatternRule> rules, String ruleID,
          AnalyzedSentence sentence) throws IOException {

        // TODO: temporary (ugly) workaround for rules that require Java disambiguation logic to be run first
        // remove this when we find the right solution 
        if( RULES_REQUIRING_JAVA_LOGIC.contains(ruleID) ) {
          new UkrainianHybridDisambiguator().firstPassDisambig(sentence);
        }

        return super.disambiguateUntil(rules, ruleID, sentence);
      }
    };
    test.testDisambiguationRulesFromXML();
  }
  
}
