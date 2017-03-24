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
package org.languagetool.rules.de;

import java.util.ResourceBundle;

import org.languagetool.rules.Example;
import org.languagetool.rules.WrongWordInContextRule;

public class GermanWrongWordInContextRule extends WrongWordInContextRule {
  
  public GermanWrongWordInContextRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("Eine Gitarre hat sechs <marker>Seiten</marker>."),
                   Example.fixed("Eine Gitarre hat sechs <marker>Saiten</marker>."));
  }
  
  @Override
  protected String getCategoryString() {
    return "Leicht zu verwechselnde Wörter";
  }
  
  @Override
  public String getId() {
    return "GERMAN_WRONG_WORD_IN_CONTEXT";
  }
  
  @Override
  public String getDescription() {
    return "Wortverwechslungen (Mine/Miene, Saite/Seite etc.)";
  }
  
  @Override
  protected String getFilename() {
    return "/de/wrongWordInContext.txt";
  }
  
  @Override
  protected String getMessageString() {
    return "Mögliche Wortverwechslung: Meinten Sie <suggestion>$SUGGESTION</suggestion> anstatt '$WRONGWORD'?";
  }
  
  @Override
  protected String getShortMessageString() {
    return "Mögliche Wortverwechslung";
  }
  
  @Override
  protected String getLongMessageString() {
    return "Mögliche Wortverwechslung: Meinten Sie <suggestion>$SUGGESTION</suggestion> (= $EXPLANATION_SUGGESTION) anstatt '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?";
  }

}
