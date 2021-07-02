/* LanguageTool, a natural language style checker 
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.rules.*;

import java.util.*;

/**
 * A rule that matches words which need a specific upper/lowercase spelling.
 * @since 4.8
 */
public class EnglishSpecificCaseRule extends AbstractSpecificCaseRule {
  
  @Override
  public String getPhrasesPath() {
    return "/en/specific_case.txt";
  }
  
  @Override
  public String getInitialCapitalMessage() {
    return "If the term is a proper noun, use initial capitals.";
  }

  @Override
  public String getOtherCapitalizationMessage() { 
    return "If the term is a proper noun, use the suggested capitalization.";
  }
  
  @Override
  public String getShortMessage() {
    return "Proper noun";
  }

  public EnglishSpecificCaseRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("I really like <marker>Harry potter</marker>."),
                   Example.fixed("I really like <marker>Harry Potter</marker>."));
  }

  @Override
  public final String getId() {
    return "EN_SPECIFIC_CASE";
  }

  @Override
  public String getDescription() {
    return "Checks upper/lower case spelling of some proper nouns";
  }

}
