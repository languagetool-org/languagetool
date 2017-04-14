/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Markus Brenneis
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

import java.util.ResourceBundle;

import org.languagetool.rules.Example;
import org.languagetool.rules.WrongWordInContextRule;

public class EnglishWrongWordInContextRule extends WrongWordInContextRule {

  public EnglishWrongWordInContextRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("I have <marker>proscribed</marker> you a course of antibiotics."),
                   Example.fixed("I have <marker>prescribed</marker> you a course of antibiotics."));
  }
  
  @Override
  protected String getCategoryString() {
    return "Commonly Confused Words";
  }
  
  @Override
  public String getId() {
    return "ENGLISH_WRONG_WORD_IN_CONTEXT";
  }
  
  @Override
  public String getDescription() {
    return "commonly confused words (proscribe/prescribe, heroine/heroin etc.)";
  }
  
  @Override
  protected String getFilename() {
    return "/en/wrongWordInContext.txt";
  }
  
  @Override
  protected String getMessageString() {
    return "Possibly confused word: Did you mean <suggestion>$SUGGESTION</suggestion> instead of '$WRONGWORD'?";
  }
  
  @Override
  protected String getShortMessageString() {
    return "Possibly confused word";
  }
  
  @Override
  protected String getLongMessageString() {
    return "Possibly confused word: Did you mean <suggestion>$SUGGESTION</suggestion> (= $EXPLANATION_SUGGESTION) instead of '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?";
  }
}
