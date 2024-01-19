/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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
package org.languagetool.rules.de;

import org.languagetool.Languages;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;

import java.util.Arrays;

public class GermanMultitokenSpeller extends MultitokenSpeller {

  public static final GermanMultitokenSpeller INSTANCE = new GermanMultitokenSpeller();

  protected GermanMultitokenSpeller() {
    super(Languages.getLanguageForShortCode("de-DE"),
      Arrays.asList("/de/multitoken-suggest.txt", "/spelling_global.txt", "de/hunspell/spelling.txt"));
  }

  @Override
  protected boolean isException(String original, String candidate) {
    if (original.substring(0, original.length()-1).equals(candidate)) {
      if (original.endsWith("s") || original.endsWith("-")) {
        return true;
      }
    }
    return false;
  }

}
