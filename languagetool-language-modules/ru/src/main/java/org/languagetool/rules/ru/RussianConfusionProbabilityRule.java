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
package org.languagetool.rules.ru;

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionString;
import org.languagetool.rules.Example;

import java.util.ResourceBundle;

/**
 * @since 3.1
 */
public class RussianConfusionProbabilityRule extends ConfusionProbabilityRule {

  public RussianConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    super(messages, languageModel, language, 3);
    // TODO: add example:
    //addExamplePair(Example.wrong("<marker>wrong</marker> word in sentence"),
    //               Example.fixed("<marker>correct</marker> word in sentence"));
  }

  @Override
  public String getDescription() {
    return "Statistically detect wrong use of words that are easily confused";  // TODO: translate
  }

  @Override
  public String getMessage(ConfusionString textString, ConfusionString suggestion) {
    // TODO: translate
    if (textString.getDescription() != null && suggestion.getDescription() != null) {
      return "Statistic suggests that '" + suggestion.getString() + "' (" + suggestion.getDescription() + ") might be the correct word here, not '"
              + textString.getString() + "' (" + textString.getDescription() + "). Please check.";
    } else if (suggestion.getDescription() != null) {
      return "Statistic suggests that '" + suggestion.getString() + "' (" + suggestion.getDescription() + ") might be the correct word here. Please check.";
    } else {
      return "Statistic suggests that '" + suggestion.getString() + "' might be the correct word here. Please check.";
    }
  }

}
