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
package org.languagetool.rules.be;

import org.languagetool.rules.*;
import org.languagetool.tools.Tools;

import java.util.*;

/**
 * A rule that matches words which need a specific upper/lowercase spelling.
 * Based on English and Russian SpecificCaseRule
 * @since 6.2
 */
public class BelarusianSpecificCaseRule extends AbstractSpecificCaseRule {
  
  @Override
  public String getPhrasesPath() {
    return "/be/specific_case.txt";
  }
  
  @Override
  public String getInitialCapitalMessage() {
    return "Уласныя імёны і назвы пішуцца з вялікай літары.";
  }

  @Override
  public String getOtherCapitalizationMessage() { 
    return "Калі гэта уласнае імя або назва, выкарыстоўвайце прапанаванае напісанне.";
  }
  
  @Override
  public String getShortMessage() {
    return "Proper noun";
  }

  public BelarusianSpecificCaseRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("<marker>Вялікая айчынная Вайна</marker> — гэта спецыфічны тэрмін савецкай гістарыяграфіі."),
                   Example.fixed("<marker>Вялікая Айчынная вайна</marker> — гэта спецыфічны тэрмін савецкай гістарыяграфіі."));
  }

  @Override
  public final String getId() {
    return "BE_SPECIFIC_CASE";
  }

  @Override
  public String getDescription() {
    return "Напісанне спецыяльных найменняў у верхнім або ніжнім рэгістры";
  }
}
