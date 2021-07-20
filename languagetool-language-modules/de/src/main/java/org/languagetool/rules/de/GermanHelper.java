/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.de.AnalyzedGermanToken;
import org.languagetool.tagging.de.GermanToken;

/**
 * Helper methods for working with German POS tags.
 * @since 2.3
 */
public final class GermanHelper {

  private GermanHelper() {
  }

  public static boolean hasReadingOfType(AnalyzedTokenReadings tokenReadings, GermanToken.POSType type) {
    if (tokenReadings == null) {
      return false;
    }
    for (AnalyzedToken token : tokenReadings) {
      if (token.getPOSTag() != null &&
          (token.getPOSTag().equals(JLanguageTool.SENTENCE_END_TAGNAME) ||
           token.getPOSTag().equals(JLanguageTool.PARAGRAPH_END_TAGNAME))) {
        return false;
      }
      AnalyzedGermanToken germanToken = new AnalyzedGermanToken(token);
      if (germanToken.getType() == type) {
        return true;
      }
    }
    return false;
  }

  /**
   * @since 2.4
   */
  public static String getNounCase(String posTag) {
    // input e.g. SUB:AKK:SIN:NEU
    return getIndexOrEmptyString(posTag, 1);
  }

  /**
   * @since 2.4
   */
  public static String getNounNumber(String posTag) {
    return getIndexOrEmptyString(posTag, 2);
  }

  /**
   * @since 2.4
   */
  public static String getNounGender(String posTag) {
    return getIndexOrEmptyString(posTag, 3);
  }

  /**
   * @since 2.4
   */
  public static String getDeterminerDefiniteness(String posTag) {
    // input e.g. ART:DEF:DAT:SIN:FEM
    return getIndexOrEmptyString(posTag, 1);
  }

  /**
   * @return GRU, KOM, or SUP
   * @since 5.4
   */
  public static String getComparison(String posTag) {
    // input e.g. ADJ:AKK:PLU:MAS:KOM:SOL
    String cmp = getIndexOrEmptyString(posTag, 4);
    if (!cmp.equals("GRU") && !cmp.equals("KOM") && !cmp.equals("SUP")) {
      // for cases like "PA2:PRD:GRU:VER"
      cmp = getIndexOrEmptyString(posTag, 2);
    }
    return cmp;
  }

  /**
   * @since 2.4
   */
  public static String getDeterminerCase(String posTag) {
    return getIndexOrEmptyString(posTag, 2);
  }

  /**
   * @since 2.4
   */
  public static String getDeterminerNumber(String posTag) {
    return getIndexOrEmptyString(posTag, 3);
  }

  /**
   * @since 2.4
   */
  public static String getDeterminerGender(String posTag) {
    return getIndexOrEmptyString(posTag, 4);
  }

  private static String getIndexOrEmptyString(String posTag, int idx) {
    if (posTag == null) {
      return "";
    }
    String[] array = posTag.split(":");
    if (array.length > idx) {
      return array[idx];
    } else {
      return "";
    }
  }

}
