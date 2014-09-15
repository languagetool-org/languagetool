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
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.languagetool.rules.AbstractSimpleReplaceRule;

/**
 * A rule that matches words which should not be used and suggests correct ones
 * instead.
 * 
 * Ukrainian implementations. Loads the relevant words from
 * <code>rules/uk/replace.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final String FILE_NAME = "/uk/replace.txt";

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public SimpleReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Пошук помилкових слів";
  }

  @Override
  public String getShort() {
    return "Помилка?";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " - помилкове слово, виправлення: "
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
