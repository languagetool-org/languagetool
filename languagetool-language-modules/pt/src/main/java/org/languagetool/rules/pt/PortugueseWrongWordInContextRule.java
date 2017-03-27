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
 *
 * l18n from GermanWrongWordInContextRule by Tiago F. Santos
 * @since 3.6
 *
 */
package org.languagetool.rules.pt;

import java.util.ResourceBundle;

import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.WrongWordInContextRule;

public class PortugueseWrongWordInContextRule extends WrongWordInContextRule {
  
  public PortugueseWrongWordInContextRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.SEMANTICS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    addExamplePair(Example.wrong("O acidente <marker>infringiu</marker> grandes danos."),
                   Example.fixed("O acidente <marker>infligiu</marker> grandes danos."));
  }
  
  @Override
  protected String getCategoryString() {
    return "Confusão de Palavras";
  }
  
  @Override
  public String getId() {
    return "PORTUGUESE_WRONG_WORD_IN_CONTEXT";
  }
  
  @Override
  public String getDescription() {
    return "Confusão de palavra dentro do contexto (Infligir/Infringir, etc.)";
  }
  
  @Override
  protected String getFilename() {
    return "/pt/wrongWordInContext.txt";
  }
  
  @Override
  protected String getMessageString() {
    return "Pretende dizer <suggestion>$SUGGESTION</suggestion> em vez de $WRONGWORD?";
  }
  
  @Override
  protected String getShortMessageString() {
    return "Possível confusão";
  }
  
  @Override
  protected String getLongMessageString() {
    return "Pretende dizer <suggestion>$SUGGESTION</suggestion> (= $EXPLANATION_SUGGESTION) em vez de '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?";
  }

}
