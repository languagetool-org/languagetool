/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Vincent Maubert
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.languagetool.AnalyzedSentence;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleLoader;

class TestFrenchDisambiguator extends AbstractDisambiguator {

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    AnalyzedSentence sentence = input;
    String filePath = "/disambiguator.xml";
    try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
      final DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
      List<DisambiguationPatternRule> disambiguationRules = ruleLoader.getRules(inputStream);
      for (final DisambiguationPatternRule patternRule : disambiguationRules) {
        sentence = patternRule.replace(sentence);
      }
    } catch (Exception e) {
      throw new RuntimeException("Problems with loading disambiguation file: " + filePath, e);
    }
    return sentence;
  }

}
