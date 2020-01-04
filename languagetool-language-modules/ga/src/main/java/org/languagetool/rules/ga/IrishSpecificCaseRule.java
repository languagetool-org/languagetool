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
package org.languagetool.rules.ga;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.*;
import org.languagetool.tagging.ga.Utils;

import java.io.InputStream;
import java.util.*;

/**
 * A rule that matches words which are complex and suggests easier to understand alternatives. 
 * @since 4.8
 */
public class IrishSpecificCaseRule extends SpecificCaseRule {
  
  public IrishSpecificCaseRule(ResourceBundle messages) {
    super(messages, "/ga/specific_case.txt");
    super.setCategory(Categories.CASING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("Rugadh agus tógadh i <marker>mbéal Feirste</marker> é."),
                   Example.fixed("Rugadh agus tógadh i <marker>mBéal Feirste</marker> é."));
  }

  @Override
  public final String getId() {
    return "GA_SPECIFIC_CASE";
  }

  @Override
  public String getDescription() {
    return "Checks upper/lower case spelling of some proper nouns";
  }

  private boolean isAllUppercase(String str) {
    return Utils.isAllUppercase(str);
  }

  private boolean startsWithUppercase(String str) {
    return Utils.startsWithUppercase(str);
  }

}
