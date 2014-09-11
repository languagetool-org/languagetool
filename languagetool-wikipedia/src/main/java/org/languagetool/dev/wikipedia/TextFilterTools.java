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
package org.languagetool.dev.wikipedia;

import org.languagetool.Language;

/**
 * Helper class.
 * @deprecated deprecated since 2.7 (not used anymore)
 */
@Deprecated
class TextFilterTools {

  private TextFilterTools() {
  }

  static SwebleWikipediaTextFilter getTextFilter(Language lang) {
    final SwebleWikipediaTextFilter textFilter;
    if (lang.getShortName().equals("ro")) {
      textFilter = new SwebleWikipediaTextFilter() {
        @Override
        public PlainTextMapping filter(String arg0) {
          final PlainTextMapping tmp = super.filter(arg0);
          // diacritics correction (comma-bellow instead of sedilla for ș and ț)
          final String text = RomanianDiacriticsModifier.correctDiacritics(tmp.getPlainText());
          return new PlainTextMapping(text, tmp.getMapping());
        }
      };
    } else {
      textFilter = new SwebleWikipediaTextFilter();
    }
    return textFilter;
  }

}
