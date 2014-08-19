/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fa;

import org.apache.commons.lang.StringUtils;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Category;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @since 2.7
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final String FILE_NAME = "/fa/replace.txt";

  public SimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(new Category("Possible Typo"));  // TODO: translate
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("We <marker>havent</marker> earned anything."),    // TODO: translate
                   Example.fixed("We <marker>haven't</marker> earned anything."));  // TODO: translate
  }

  @Override
  public String getFileName() {
    return FILE_NAME;
  }

  @Override
  public final String getId() {
    return "FA_SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Possible spelling mistake";  // TODO: translate
  }

  @Override
  public String getShort() {
    return "Possible spelling mistake";  // TODO: translate
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Possible spelling mistake found: "
            + StringUtils.join(replacements, ", ") + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return Locale.getDefault();
  }

}
