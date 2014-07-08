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

package org.languagetool.rules;

import org.languagetool.Language;

import java.util.ResourceBundle;

/**
 * Check if there is duplicated whitespace in a sentence.
 * Considers two spaces as incorrect, and proposes a single space instead.
 * 
 * @author Marcin Miłkowski
 * @deprecated this has been renamed to {@link MultipleWhitespaceRule} (deprecated since 2.7)
 */
public class WhitespaceRule extends MultipleWhitespaceRule {

  public WhitespaceRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
    super.setCategory(new Category(messages.getString("category_misc")));
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }

  @Override
  public final String getId() {
    return "OLD_WHITESPACE_RULE";
  }

}
