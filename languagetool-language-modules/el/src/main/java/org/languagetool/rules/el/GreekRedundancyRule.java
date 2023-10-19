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
package org.languagetool.rules.el;

import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches redundant expressions. 
 *
 * @author giorgossideris 
 */
public class GreekRedundancyRule extends AbstractSimpleReplaceRule2 {

  public static final String EL_REDUNDANCY_REPLACE = "EL_REDUNDANCY_REPLACE";

  private static final String FILE_NAME = "/el/redundancies.txt";
  private static final Locale EL_LOCALE = new Locale("el");  // locale used on case-conversion

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  public GreekRedundancyRule(ResourceBundle messages, Language language) throws IOException {
    super(messages, language);
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("Μου αρέσει να <marker>ανεβαίνω πάνω</marker> σε δέντρα."),
                   Example.fixed("Μου αρέσει να <marker>ανεβαίνω</marker> σε δέντρα."));
  }

  @Override
  public final String getId() {
    return EL_REDUNDANCY_REPLACE;
  }

  @Override
  public String getDescription() {
    return "Έλεγχος για χρήση πλεονασμού σε μια πρόταση.";
  }

  @Override
  public String getShort() {
    return "Πλεονασμός";
  }

  @Override
  public String getMessage() {
    return "'$match' είναι πλεονασμός. Γενικά, είναι προτιμότερο το: $suggestions";
  }

  @Override
  public Locale getLocale() {
    return EL_LOCALE;
  }

}
