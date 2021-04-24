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
import org.languagetool.rules.ngrams.NgramProbabilityRule;
import org.languagetool.tokenizers.WordTokenizer;

import java.util.ResourceBundle;

/**
 * @since 3.2
 */
public class EnglishNgramProbabilityRule extends NgramProbabilityRule {

  private final WordTokenizer tokenizer = new GoogleStyleWordTokenizer();

  public EnglishNgramProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    super(messages, languageModel, language);
    setDefaultOff();  // too many false alarms for now (2015-12)
    //addExamplePair(Example.wrong("I didn't <marker>now</marker> where it came from."),
    //               Example.fixed("I didn't <marker>know</marker> where it came from."));
  }

  @Override
  protected WordTokenizer getGoogleStyleWordTokenizer() {
    return tokenizer;
  }
}
