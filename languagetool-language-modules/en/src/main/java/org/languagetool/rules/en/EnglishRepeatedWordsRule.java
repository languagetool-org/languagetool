/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.AbstractRepeatedWordsRule;
import org.languagetool.rules.SynonymsData;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.en.EnglishSynthesizer;

public class EnglishRepeatedWordsRule extends AbstractRepeatedWordsRule{
  
  private static final EnglishSynthesizer synth = new EnglishSynthesizer(new AmericanEnglish());

  public EnglishRepeatedWordsRule(ResourceBundle messages) {
    super(messages, new AmericanEnglish());
    //super.setDefaultTempOff();
  }
  
  private static final Map<String, SynonymsData> wordsToCheck = loadWords("/en/synonyms.txt");
  
  @Override
  protected String getMessage() {
    return "This word has been used in one of the immediately preceding sentences. Using a synonym could make your text more interesting to read, unless the repetition is intentional.";
  }

  @Override
  public String getDescription() {
    return ("Suggest synonyms for repeated words.");
  }

  @Override
  protected Map<String, SynonymsData> getWordsToCheck() {
    return wordsToCheck;
  }

  @Override
  protected String getShortMessage() {
    return "Repeated word";
  }

  @Override
  protected Synthesizer getSynthesizer() {
    return synth;
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int i, boolean sentStart, boolean isCapitalized,
      boolean isAllUppercase) {
    if (isAllUppercase || (isCapitalized && !sentStart)) {
      return true;
    }
    if (tokens[i].hasPosTagStartingWith("NNP")) {
      return true;
    }
    return false;
  }

}
