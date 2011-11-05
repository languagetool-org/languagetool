/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.dev.wikipedia;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.dev.tools.RomanianDiacriticsModifier;

/**
 * Helper class.
 */
class TextFilterTools {

  private TextFilterTools() {
  }

  static WikipediaTextFilter getTextFilter(Language lang) {
    final WikipediaTextFilter textFilter;
    if (Language.ROMANIAN == lang) {
      textFilter = new WikipediaTextFilter() {
        @Override
        public String filter(String arg0) {
          final String tmp = super.filter(arg0);
          // diacritics correction (comma-bellow instead of sedilla for ș and ț)
          return RomanianDiacriticsModifier.correctDiacritrics(tmp);
        }
      };
    } else {
      textFilter = new WikipediaTextFilter();
    }
    return textFilter;
  }

}
