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
package org.languagetool.rules.nl;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.rules.WrongWordInContextRule;

public class DutchWrongWordInContextRule extends WrongWordInContextRule {
  
  public DutchWrongWordInContextRule(final ResourceBundle messages) throws IOException {
    super(messages);
  }
  
  @Override
  protected String getCategoryString() {
    return "Easily Confused Words";
  }
  
  @Override
  public String getId() {
    return "DUTCH_WRONG_WORD_IN_CONTEXT";
  }
  
  @Override
  public String getDescription() {
    return "Confusion of words";
  }
  
  @Override
  protected String getFilename() {
    return "/nl/wrongWordInContext.txt";
  }
  
  @Override
  protected String getMessageString() {
    return "Possible confusion of words: Did you mean <suggestion>$SUGGESTION</suggestion> instead of '$WRONGWORD'?";
  }
  
  @Override
  protected String getShortMessageString() {
    return "Possible confusion of words";
  }
  
  @Override
  protected String getLongMessageString() {
    return "Possible confusion of words: Did you mean <suggestion>$SUGGESTION</suggestion> (= $EXPLANATION_SUGGESTION) instead of '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?";
  }

}
