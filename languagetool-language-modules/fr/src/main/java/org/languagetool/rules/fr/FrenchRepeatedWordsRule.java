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
package org.languagetool.rules.fr;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.French;
import org.languagetool.rules.AbstractRepeatedWordsRule;
import org.languagetool.rules.SynonymsData;
import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.synthesis.Synthesizer;

import java.util.Map;
import java.util.ResourceBundle;

public class FrenchRepeatedWordsRule extends AbstractRepeatedWordsRule {

  public FrenchRepeatedWordsRule(ResourceBundle messages) {
    super(messages, French.getInstance());
    //super.setDefaultTempOff();
  }
  
  private static final Map<String, SynonymsData> wordsToCheck = loadWords("/fr/synonyms.txt");
  
  @Override
  protected String getMessage() {
    return "Ce mot apparaît déjà dans l'une des phrases précédant immédiatement celle-ci. Utilisez un synonyme pour apporter plus de variété à votre texte, excepté si la répétition est intentionnelle.";
  }

  @Override
  public String getDescription() {
    return ("Synonymes de mots répétés.");
  }

  @Override
  protected Map<String, SynonymsData> getWordsToCheck() {
    return wordsToCheck;
  }

  @Override
  protected String getShortMessage() {
    return "Style : Mot répété";
  }
  
  @Override
  protected Synthesizer getSynthesizer() {
    return FrenchSynthesizer.INSTANCE;
  }
  
  @Override
  protected String adjustPostag(String postag) {
    if (postag.endsWith("e sp")) {
      return StringUtils.replaceOnce(postag,"e sp", ". .*");
    } else if (postag.endsWith("m s")) {
      return StringUtils.replaceOnce(postag,"m s", "[me] sp?");
    } else if (postag.endsWith("f s")) {
      return StringUtils.replaceOnce(postag,"f s", "[fe] sp?");
    } else if (postag.endsWith("m p")) {
      return StringUtils.replaceOnce(postag,"m p", "[me] s?p");
    } else if (postag.endsWith("f p")) {
      return StringUtils.replaceOnce(postag,"f p", "[fe] s?p");
    } else if (postag.endsWith("e s")) {
      return StringUtils.replaceOnce(postag,"e s", "[me] sp?"); // also F ?
    } else if (postag.endsWith("e p")) {
      return StringUtils.replaceOnce(postag,"e p", "[me] s?p"); // also F ?
    } else if (postag.endsWith("m sp")) {
      return StringUtils.replaceOnce(postag,"m sp", "[me] s?p?");
    } else if (postag.endsWith("f sp")) {
      return StringUtils.replaceOnce(postag,"f sp", "[fe] s?p?");
    }
    return postag; 
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int i, boolean sentStart, boolean isCapitalized,
      boolean isAllUppercase) {
    if (isAllUppercase || (isCapitalized && !sentStart)) {
      return true;
    }
    return tokens[i].hasPosTagStartingWith("Z");
  }
}
