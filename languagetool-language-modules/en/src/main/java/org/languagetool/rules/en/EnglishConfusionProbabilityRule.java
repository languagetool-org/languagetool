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
package org.languagetool.rules.en;

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionString;
import org.languagetool.rules.Example;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;

import java.util.*;

/**
 * @since 2.7
 */
public class EnglishConfusionProbabilityRule extends ConfusionProbabilityRule {

  private final EnglishWordTokenizer tokenizer = new EnglishWordTokenizer() {
    @Override
    public String getTokenizingCharacters() {
      return super.getTokenizingCharacters() + "-";
    }
    @Override
    public List<String> tokenize(final String text) {
      List<String> tokens = super.tokenize(text);
      String prev = null;
      final Stack<String> l = new Stack<>();
      for (String token : tokens) {
        if ("'".equals(prev)) {
          // TODO: add more cases if needed:
          if (token.equals("m")) {
            l.pop();
            l.push("'m");
          } else if (token.equals("re")) {
            l.pop();
            l.push("'re");
          } else if (token.equals("ve")) {
            l.pop();
            l.push("'ve");
          } else if (token.equals("ll")) {
            l.pop();
            l.push("'ll");
          } else {
            l.push(token);
          }
        } else {
          l.push(token);
        }
        prev = token;
      }
      return l;
    }
  };

  public EnglishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }

  public EnglishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages, languageModel, language, grams);
    addExamplePair(Example.wrong("I didn't <marker>now</marker> where it came from."),
                   Example.fixed("I didn't <marker>know</marker> where it came from."));
  }

  @Override
  public String getDescription() {
    return "Statistically detect wrong use of words that are easily confused";
  }
  
  @Override
  public String getMessage(ConfusionString textString, ConfusionString suggestion) {
    if (textString.getDescription() != null && suggestion.getDescription() != null) {
      return "Statistic suggests that '" + suggestion.getString() + "' (" + suggestion.getDescription() + ") might be the correct word here, not '"
              + textString.getString() + "' (" + textString.getDescription() + "). Please check.";
    } else if (suggestion.getDescription() != null) {
      return "Statistic suggests that '" + suggestion.getString() + "' (" + suggestion.getDescription() + ") might be the correct word here. Please check.";
    } else {
      return "Statistic suggests that '" + suggestion.getString() + "' might be the correct word here. Please check.";
    }
  }
  
  @Override
  protected WordTokenizer getTokenizer() {
    return tokenizer;
  }
}
