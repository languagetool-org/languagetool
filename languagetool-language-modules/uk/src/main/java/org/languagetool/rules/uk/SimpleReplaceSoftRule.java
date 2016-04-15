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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.ITSIssueType;

/**
 * A rule that matches words for which better alternatives exist and suggests them instead.
 * 
 * Loads the relevant words from <code>rules/uk/replace_soft.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceSoftRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = load("/uk/replace_soft.txt");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public SimpleReplaceSoftRule(ResourceBundle messages) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.Style);
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE_SOFT";
  }

  @Override
  public String getDescription() {
    return "Пошук нерекомендованих слів";
  }

  @Override
  public String getShort() {
    return "Нерекомендоване слово";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " - нерекомендоване слово, кращий варіант: "
        + StringUtils.join(replacements, ", ") + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

}
