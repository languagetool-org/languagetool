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

import org.languagetool.Language;
import org.languagetool.rules.AbstractDashRule;

import java.io.IOException;

/**
 * Check for compounds written with dashes instead of hyphens.
 * @since 3.8
 */
public class EnglishDashRule extends AbstractDashRule {

  public EnglishDashRule(Language lang) throws IOException {
    super("/en/compounds.txt",
        "A dash was used instead of a hyphen. Did you mean: ", lang);
  }
  
  @Override
  public String getDescription() {
    return "Checks if hyphenated words were spelled with dashes (e.g., 'T — shirt' instead 'T-shirt').";
  }

}
