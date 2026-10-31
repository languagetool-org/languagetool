/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sv;

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.patterns.PatternToken;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.posRegex;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.token;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.tokenRegex;

/**
 * @since 2.7
 */
public class SwedishConfusionProbabilityRule extends ConfusionProbabilityRule {

  private static final List<String> EXCEPTIONS = Arrays.asList(
      // Use all-lowercase, matches will be case-insensitive.
      "god sak"
    );

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      // "De små öronen" "Dessa små öron"
      tokenRegex("de|dessa|dom"),
      token("små"),
      posRegex("NN:PLU")
    )
  );

  public SwedishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }

  public SwedishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages, languageModel, language, grams, EXCEPTIONS, ANTI_PATTERNS);
    addExamplePair(Example.wrong("Ett <marker>streck</marker> mot horisonten."),
                   Example.fixed("Ett <marker>sträck</marker> mot horisonten."));
  }

  protected boolean isCommonWord(String token) {
    return token.matches("[\\wåäöüßÅÄÖÜ]+");
  }

}
