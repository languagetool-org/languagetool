/* LanguageTool, a natural language style checker 
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
package org.languagetool.rules.de;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractStyleRepeatedWordRule;
import org.languagetool.rules.Categories;

/**
 * A rule checks the appearance of same words in a sentence or in two consecutive sentences.
 * Only substantive, verbs and adjectives are checked.
 * This rule detects no grammar error but a stylistic problem (default off)
 * @author Fred Kruse
 */

public class GermanStyleRepeatedWordRule  extends AbstractStyleRepeatedWordRule {
  
  public GermanStyleRepeatedWordRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
//    addExamplePair(Example.wrong("Der alte Mann wohnte in einem <marker>großen</marker> Haus. Es stand in einem <marker>großen</marker> Garten."),
//                   Example.fixed("Der alte Mann wohnte in einem <marker>großen</marker> Haus. Es stand in einem <marker>weitläufigen</marker> Garten."));
  }

  @Override
  public String getId() {
    return "STYLE_REPEATED_WORD_RULE_DE";
  }

  @Override
  public String getDescription() {
    return "Wiederholte Worte in aufeinanderfolgenden Sätzen";
  }
  
  @Override
  protected String messageSameSentence() {
    return "Stilproblem: Das Wort wird bereits im selben Satz verwendet!";
  }
  
  @Override
  protected String messageSentenceBefore() {
    return "Stilproblem: Das Wort wird bereits in einem vorhergehenden Satz verwendet!";
  }
  
  @Override
  protected String messageSentenceAfter() {
    return "Stilproblem: Das Wort wird bereits in einem nachfolgenden Satz verwendet!";
  }

  /*
   * Only substantive, names, verbs and adjectives are checked
   */
  protected boolean isTokenToCheck(AnalyzedTokenReadings token) {
    return token.matchesPosTagRegex("(SUB|EIG|VER|ADJ):.*") && !token.matchesPosTagRegex("ART:.*|ADV:.*|VER:(AUX|MOD):.*");
  }

  /*
   * Pairs of substantive are excluded like "Arm in Arm", "Seite an Seite", etc.
   */
  protected boolean isTokenPair(AnalyzedTokenReadings[] tokens, int n, boolean before) {
    if (before) {
      if (tokens[n-2].matchesPosTagRegex("SUB:.*") && tokens[n-1].matchesPosTagRegex("PRP:.*")
              && tokens[n].matchesPosTagRegex("SUB:.*")) {
        return true;
      }
    } else {
      if (tokens[n].matchesPosTagRegex("SUB:.*") && tokens[n+1].matchesPosTagRegex("PRP:.*")
              && tokens[n+2].matchesPosTagRegex("SUB:.*")) {
        return true;
      }
    }
    return false;
  }
  
}
