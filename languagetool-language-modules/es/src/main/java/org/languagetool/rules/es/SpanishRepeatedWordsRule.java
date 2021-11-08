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
package org.languagetool.rules.es;

import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Spanish;
import org.languagetool.rules.AbstractRepeatedWordsRule;
import org.languagetool.rules.SynonymsData;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.es.SpanishSynthesizer;

public class SpanishRepeatedWordsRule extends AbstractRepeatedWordsRule{

  private static final SpanishSynthesizer synth = new SpanishSynthesizer(new Spanish());

  
  public SpanishRepeatedWordsRule(ResourceBundle messages) {
    super(messages, new Spanish());
    super.setDefaultTempOff();
  }
  
  private static final Map<String, SynonymsData> wordsToCheck = loadWords("/es/synonyms.txt");
  
  @Override
  protected String getMessage() {
    return "Esta palabra ya ha aparecido en una de las frases inmediatamente anteriores. Puede usar un sinónimo para hacer más interesante el texto, excepto si la repetición es intencionada.";
  }

  @Override
  public String getDescription() {
    return ("Sinónimos para palabras repetidas.");
  }

  @Override
  protected Map<String, SynonymsData> getWordsToCheck() {
    return wordsToCheck;
  }

  @Override
  protected String getShortMessage() {
    return "Palabra repetida";
  }
  
  @Override
  protected Synthesizer getSynthesizer() {
    return synth;
  }
  
  @Override
  protected String adjustPostag(String postag) {
    if (postag.contains("CN")) {
      return postag.replaceFirst("CN", "..");
    } else if (postag.contains("MS")) {
      return postag.replaceFirst("MS", "[MC][SN]");
    } else if (postag.contains("FS")) {
      return postag.replaceFirst("FS", "[FC][SN]");
    } else if (postag.contains("MP")) {
      return postag.replaceFirst("MP", "[MC][PN]");
    } else if (postag.contains("FP")) {
      return postag.replaceFirst("FP", "[FC][PN]");
    } else if (postag.contains("CS")) {
      return postag.replaceFirst("CS", "[MC][SN]"); // also F ?
    } else if (postag.contains("CP")) {
      return postag.replaceFirst("CP", "[MC][PN]"); // also F ?
    } else if (postag.contains("MN")) {
      return postag.replaceFirst("MN", "[MC][SPN]");
    } else if (postag.contains("FN")) {
      return postag.replaceFirst("FN", "[FC][SPN]");
    }
    return postag; 
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int i, boolean sentStart, boolean isCapitalized,
      boolean isAllUppercase) {
    if (isAllUppercase || (isCapitalized && !sentStart)) {
      return true;
    }
    if (tokens[i].hasPosTagStartingWith("NP")) {
      return true;
    }
    return false;
  }
}
