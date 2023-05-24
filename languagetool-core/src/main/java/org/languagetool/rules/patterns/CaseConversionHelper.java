/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.languagetool.Language;
import org.languagetool.tools.StringTools;

import java.util.Locale;

/**
 * @since 3.2
 */
public final class CaseConversionHelper {

  private CaseConversionHelper() {
  }

  /**
   * Converts case of the string token according to match element attributes.
   * @param s Token to be converted.
   * @param sample the sample string used to determine how the original string looks like (used only on case preservation)
   * @return Converted string.
   */
  public static String convertCase(Match.CaseConversion conversion, String s, String sample, Language lang) {
    if (StringTools.isEmpty(s)) {
      return s;
    }
    String token = s;
    switch (conversion) {
      case NONE:
        break;
      case PRESERVE:
        if (StringTools.startsWithUppercase(sample)) {
          if (StringTools.isAllUppercase(sample)) {
            token = token.toUpperCase(Locale.ENGLISH);
          } else {
            token = StringTools.uppercaseFirstChar(token, lang);
          }
        }
        break;
      case STARTLOWER:
        token = token.substring(0, 1).toLowerCase() + token.substring(1);
        break;
      case STARTUPPER:
        token = StringTools.uppercaseFirstChar(token, lang);
        break;
      case ALLUPPER:
        token = token.toUpperCase(Locale.ENGLISH);
        break;
      case FIRSTUPPER:
        token = token.toLowerCase();
        token = StringTools.uppercaseFirstChar(token, lang);
        break;
      case ALLLOWER:
        token = token.toLowerCase();
        break;
      case NOTASHKEEL:
        token = StringTools.removeTashkeel(token);
        break;
      default:
        break;
    }
    return token;
  }

}
