/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Sohaib Afifi, Taha Zerrouki
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

package org.languagetool.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
  Used to generate and handle units for numbers(Tamyeez)

  The main task is to get agreement with numeric phrase
  like
  سبعة عشر رجلا
  ثلاثة رجال
 */
public class ArabicUnitsHelper {
  // map of unit cases
  static final Map<String, List<String>> unitsMap2 = new HashMap<>();
  static final Map<String, Map<String, String>> unitsMap = new HashMap<>();
  // cases will be indexed by numeric values
  final short ONE_POS_RAF3 = 0;
  final short ONE_POS_NASB = 1;
  final short ONE_POS_JAR = 2;

  final short TWO_POS_RAF3 = 3;
  final short TWO_POS_NASB = 4;
  final short TWO_POS_JAR = 5;

  final short PLURAL_POS_RAF3 = 6;
  final short PLURAL_POS_NASB = 7;
  final short PLURAL_POS_JAR = 8;

  static {
    Map<String, String> unitsEntry = new HashMap<>();
    unitsEntry.put("feminin", "no");
    unitsEntry.put("one_raf3", "دينار");
    unitsEntry.put("one_nasb", "دينارًا");
    unitsEntry.put("one_jar", "دينارٍ");
    unitsEntry.put("two_raf3", "ديناران");
    unitsEntry.put("two_nasb", "دينارين");
    unitsEntry.put("two_jar", "دينارين");
    unitsEntry.put("plural_raf3", "دنانيرُ");
    unitsEntry.put("plural_nasb", "دنانيرَ");
    unitsEntry.put("plural_jar", "دنانيرَ");
    unitsMap.put("دينار", unitsEntry);

    unitsEntry = new HashMap<>();
    unitsEntry.put("feminin", "no");
    unitsEntry.put("one_raf3", "درهم");
    unitsEntry.put("one_nasb", "درهمًا");
    unitsEntry.put("one_jar", "درهمٍ");
    unitsEntry.put("two_raf3", "درهمان");
    unitsEntry.put("two_nasb", "درهمين");
    unitsEntry.put("two_jar", "درهمين");
    unitsEntry.put("plural_raf3", "دراهمُ");
    unitsEntry.put("plural_nasb", "دراهمَ");
    unitsEntry.put("plural_jar", "دراهمَ");
    unitsMap.put("درهم", unitsEntry);

    unitsEntry = new HashMap<>();
    unitsEntry.put("feminin", "no");
    unitsEntry.put("one_raf3", "دولار");
    unitsEntry.put("one_nasb", "دولارًا");
    unitsEntry.put("one_jar", "دولارٍ");
    unitsEntry.put("two_raf3", "دولاران");
    unitsEntry.put("two_nasb", "دولارين");
    unitsEntry.put("two_jar", "دولارين");
    unitsEntry.put("plural_raf3", "دولاراتٌ");
    unitsEntry.put("plural_nasb", "دولاراتٍ");
    unitsEntry.put("plural_jar", "دولاراتٍ");
    unitsMap.put("دولار", unitsEntry);

    unitsEntry = new HashMap<>();
    unitsEntry.put("feminin", "yes");
    unitsEntry.put("one_raf3", "ليرة");
    unitsEntry.put("one_nasb", "ليرةً");
    unitsEntry.put("one_jar", "ليرةٍ");
    unitsEntry.put("two_raf3", "ليرتان");
    unitsEntry.put("two_nasb", "ليرتين");
    unitsEntry.put("two_jar", "ليرتين");
    unitsEntry.put("plural_raf3", "ليراتٌ");
    unitsEntry.put("plural_nasb", "ليراتٍ");
    unitsEntry.put("plural_jar", "ليراتٍ");
    unitsMap.put("ليرة", unitsEntry);
  }

  /* test if the unit is feminin */
  public static boolean isFeminin(String unit) {
    return (unitsMap.containsKey(unit) && unitsMap.get(unit).getOrDefault("feminin", "no").equals("yes"));
  }

  public static boolean isUnit(String unit) {
    return unitsMap.containsKey(unit);
  }

  /* return the suitable form of units according to inflection */
  public static String getForm(String unit, String category, String inflection) {
    if (inflection.isEmpty()) {
      inflection = "raf3";
    }
    String key = category + "_" + inflection;
    if (unitsMap.containsKey(unit)) {
      return unitsMap.get(unit).getOrDefault(key, "[" + unit + "]");
    }
    return "[[" + unit + "]]";
  }

  /* return the suitable form of units according to inflection */
  public static String getOneForm(String unit, String inflection) {
    return getForm(unit, "one", inflection);
  }

  /* return the suitable form of units according to inflection */
  public static String getTwoForm(String unit, String inflection) {
    return getForm(unit, "two", inflection);
  }  /* return the suitable form of units according to inflection */

  public static String getPluralForm(String unit, String inflection) {
    return getForm(unit, "plural", inflection);
  }
}