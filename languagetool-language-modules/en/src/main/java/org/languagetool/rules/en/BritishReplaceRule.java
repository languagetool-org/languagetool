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
package org.languagetool.rules.en;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Marcin Miłkowski
 */
public class BritishReplaceRule extends AbstractSimpleReplaceRule {

  public static final String BRITISH_SIMPLE_REPLACE_RULE = "EN_GB_SIMPLE_REPLACE";

  private static final String FILE_NAME = "/en/en-GB/replace.txt";
  // locale used on case-conversion
  private static final Locale EN_GB_LOCALE = new Locale("en-GB");

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public BritishReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("Where's the <marker>bathroom</marker> on the Enterprise?"),
                   Example.fixed("Where's the <marker>toilet</marker> on the Enterprise?"));
  }

  @Override
  public final String getId() {
    return BRITISH_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "American words easily confused in British English";
  }

  @Override
  public String getShort() {
    return "American word";
  }
  
  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " is a common American expression, in British English it is more common to use: "
        + StringUtils.join(replacements, ", ") + ".";
  }

  /**
   * use case-insensitive matching.
   */
  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  /**
   * locale used on case-conversion
   */
  @Override
  public Locale getLocale() {
    return EN_GB_LOCALE;
  }

}
