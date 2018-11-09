/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Marcin Miłkowski (http://www.languagetool.org)
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

import org.languagetool.Languages;
import org.languagetool.rules.AbstractDashRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.patterns.PatternRule;

import java.io.IOException;
import java.util.List;

/**
 * Check for compounds written with dashes instead of hyphens.
 * @since 3.8
 */
public class EnglishDashRule extends AbstractDashRule {

  private static final List<PatternRule> dashRules = loadCompoundFile("/en/compounds.txt",
          "A dash was used instead of a hyphen. Did you mean: ", Languages.getLanguageForShortCode("en"));

  public EnglishDashRule() throws IOException {
    super(dashRules);
    addExamplePair(Example.wrong("I'll buy a new <marker>T—shirt</marker>."),
                   Example.fixed("I'll buy a new <marker>T-shirt</marker>."));
  }

  @Override
  public String getId() {
    return "EN_DASH_RULE";
  }

  @Override
  public String getDescription() {
    return "Checks if hyphenated words were spelled with dashes (e.g., 'T — shirt' instead 'T-shirt').";
  }

}
