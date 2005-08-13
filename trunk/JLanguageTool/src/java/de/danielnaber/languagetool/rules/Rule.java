/* JLanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.Language;

/**
 * Abstract rule class. A Rule drescribes a language error and can
 * test whether a given pre-analyzed text contains that error using the
 * {@link Rule#match} method. 
 *
 * @author Daniel Naber
 */
public abstract class Rule {

  private String correctExample;
  private String incorrectExample;

  public abstract String getId();
  
  public abstract String getDescription();

  public abstract Language[] getLanguages();

  /**
   * Whether this rule can be used for text in the given language.
   */
  public boolean supportsLanguage(Language language) {
    Language[] languages = getLanguages();
    for (int i = 0; i < languages.length; i++) {
      if (language == languages[i])
        return true;
    }
    return false;
  }

  /**
   * TODO: Return the number of false positives to be expected.
   * @return
   *
  public int getFalsePositives() {
    return -1;
  }*/
  
  /**
   * Check whether the given text matche this error rule, i.e. whether the
   * text contains this error.
   * 
   * @param text a pre-analyzed sentence
   * @return an array of RuleMatch object for each match.
   */
  public abstract RuleMatch[] match(AnalyzedSentence text);

  public void setCorrectExample(String correctExample) {
    this.correctExample = correctExample;
  }

  /**
   * Get a sentence with an example sentence that is correct and thus will not 
   * match this rule.
   */
  public String getCorrectExample() {
    return correctExample;
  }
  
  public void setIncorrectExample(String incorrectExample) {
    this.incorrectExample = incorrectExample;
  }

  /**
   * Get a sentence with an example sentence that is incorrect and thus will 
   * match this rule.
   */
  public String getIncorrectExample() {
    return incorrectExample;
  }

}
