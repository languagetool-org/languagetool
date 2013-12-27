/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.pl;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.Language;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikPolishSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/pl/hunspell/pl_PL.dict";

  private static final Pattern POLISH_TOKENIZING_CHARS = Pattern.compile("(?:[Qq]uasi|[Nn]iby)-");

  public MorfologikPolishSpellerRule(ResourceBundle messages,
      Language language) throws IOException {
    super(messages, language);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_PL_PL";
  }

  @Override
  public Pattern tokenizingPattern() {
    return POLISH_TOKENIZING_CHARS;
  }


}
