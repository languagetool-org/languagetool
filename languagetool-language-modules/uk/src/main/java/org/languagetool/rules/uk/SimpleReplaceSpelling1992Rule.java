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

import org.apache.commons.lang3.StringUtils;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.ITSIssueType;

/**
 * A rule that matches words that are written by 1992 spelling rules and suggests 2019 spelling instead.
 * Loads the relevant words from <code>rules/uk/replace_spelling_2019.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceSpelling1992Rule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> WRONG_WORDS = loadFromPath("/uk/replace_spelling_2019.txt");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return WRONG_WORDS;
  }

  public SimpleReplaceSpelling1992Rule(ResourceBundle messages) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE_SPELLING_1992";
  }

  @Override
  public String getDescription() {
    return "Пошук слів, написаних за правописом 1992";
  }

  @Override
  public String getShort() {
    return "Слово, написане за правописом 1992";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " — написання не відповідає чинній версії правопису, виправлення: "
        + StringUtils.join(replacements, ",") + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }


}
