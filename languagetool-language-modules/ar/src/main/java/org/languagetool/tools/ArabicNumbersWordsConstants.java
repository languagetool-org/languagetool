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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArabicNumbersWordsConstants {

  static final List<String> arabicOnes = new ArrayList<>();
  static final List<String> arabicFeminineOnes = new ArrayList<>();

  static final List<String> arabicTens = new ArrayList<>();

  static final List<String> arabicHundreds = new ArrayList<>();
  static final List<String> arabicTwos = new ArrayList<>();
  static final List<String> arabicAppendedTwos = new ArrayList<>();

  static final List<String> arabicGroup = new ArrayList<>();
  static final List<String> arabicAppendedGroup = new ArrayList<>();
  static final List<String> arabicPluralGroups = new ArrayList<>();

  // Jar ones
  static final List<String> arabicJarOnes = new ArrayList<>();
  static final List<String> arabicJarFeminineOnes = new ArrayList<>();
  static final List<String> arabicJarTens = new ArrayList<>();
  static final List<String> arabicJarHundreds = new ArrayList<>();
  static final List<String> arabicJarTwos = new ArrayList<>();
  static final List<String> arabicJarAppendedTwos = new ArrayList<>();


  // Words of numbers
  static final Map<String, Integer> NUMBER_WORDS = new HashMap<>();

  static {
    /* Ones */
    arabicOnes.add("");
    arabicOnes.add("واحد");
    arabicOnes.add("اثنان");
    arabicOnes.add("ثلاثة");
    arabicOnes.add("أربعة");
    arabicOnes.add("خمسة");
    arabicOnes.add("ستة");
    arabicOnes.add("سبعة");
    arabicOnes.add("ثمانية");
    arabicOnes.add("تسعة");
    arabicOnes.add("عشرة");
    arabicOnes.add("أحد عشر");
    arabicOnes.add("اثنا عشر");
    arabicOnes.add("ثلاثة عشر");
    arabicOnes.add("أربعة عشر");
    arabicOnes.add("خمسة عشر");
    arabicOnes.add("ستة عشر");
    arabicOnes.add("سبعة عشر");
    arabicOnes.add("ثمانية عشر");
    arabicOnes.add("تسعة عشر");

    arabicFeminineOnes.add("");
    arabicFeminineOnes.add("إحدى");
    arabicFeminineOnes.add("اثنتان");
    arabicFeminineOnes.add("ثلاث");
    arabicFeminineOnes.add("أربع");
    arabicFeminineOnes.add("خمس");
    arabicFeminineOnes.add("ست");
    arabicFeminineOnes.add("سبع");
    arabicFeminineOnes.add("ثمان");
    arabicFeminineOnes.add("تسع");
    arabicFeminineOnes.add("عشر");
    arabicFeminineOnes.add("إحدى عشرة");
    arabicFeminineOnes.add("اثنتا عشرة");
    arabicFeminineOnes.add("ثلاث عشرة");
    arabicFeminineOnes.add("أربع عشرة");
    arabicFeminineOnes.add("خمس عشرة");
    arabicFeminineOnes.add("ست عشرة");
    arabicFeminineOnes.add("سبع عشرة");
    arabicFeminineOnes.add("ثماني عشرة");
    arabicFeminineOnes.add("تسع عشرة");
    /* Ones */

    /* Tens */
    arabicTens.add("عشرون");
    arabicTens.add("ثلاثون");
    arabicTens.add("أربعون");
    arabicTens.add("خمسون");
    arabicTens.add("ستون");
    arabicTens.add("سبعون");
    arabicTens.add("ثمانون");
    arabicTens.add("تسعون");
    /* Tens */

    /* Hundreds */
    arabicHundreds.add("");
    arabicHundreds.add("مائة");
    arabicHundreds.add("مئتان");
    arabicHundreds.add("ثلاثمئة");
    arabicHundreds.add("أربعمئة");
    arabicHundreds.add("خمسمئة");
    arabicHundreds.add("ستمئة");
    arabicHundreds.add("سبعمئة");
    arabicHundreds.add("ثمانمئة");
    arabicHundreds.add("تسعمئة");
    /* Hundreds */

    /* Twos */
    arabicTwos.add("مئتان");
    arabicTwos.add("ألفان");
    arabicTwos.add("مليونان");
    arabicTwos.add("ملياران");
    arabicTwos.add("تريليونان");
    arabicTwos.add("كوادريليونان");
    arabicTwos.add("كوينتليونان");
    arabicTwos.add("سكستيليونان");

    /* Appended */
    {
      arabicAppendedTwos.add("مئتا");
      arabicAppendedTwos.add("ألفا");
      arabicAppendedTwos.add("مليونا");
      arabicAppendedTwos.add("مليارا");
      arabicAppendedTwos.add("تريليونا");
      arabicAppendedTwos.add("كوادريليونا");
      arabicAppendedTwos.add("كوينتليونا");
      arabicAppendedTwos.add("سكستيليونا");
    }
    /* Appended */
    /* Twos */

    /* Group */
    arabicGroup.add("مائة");
    arabicGroup.add("ألف");
    arabicGroup.add("مليون");
    arabicGroup.add("مليار");
    arabicGroup.add("تريليون");
    arabicGroup.add("كوادريليون");
    arabicGroup.add("كوينتليون");
    arabicGroup.add("سكستيليون");
    /* Appended */
    {
      arabicAppendedGroup.add("");
      arabicAppendedGroup.add("ألفاً");
      arabicAppendedGroup.add("مليوناً");
      arabicAppendedGroup.add("ملياراً");
      arabicAppendedGroup.add("تريليوناً");
      arabicAppendedGroup.add("كوادريليوناً");
      arabicAppendedGroup.add("كوينتليوناً");
      arabicAppendedGroup.add("سكستيليوناً");
    }
    /* Appended */
    /* Group */

    /* Plural groups*/
    arabicPluralGroups.add("");
    arabicPluralGroups.add("آلاف");
    arabicPluralGroups.add("ملايين");
    arabicPluralGroups.add("مليارات");
    arabicPluralGroups.add("تريليونات");
    arabicPluralGroups.add("كوادريليونات");
    arabicPluralGroups.add("كوينتليونات");
    arabicPluralGroups.add("سكستيليونات");
    /* Plural groups*/

    /* inflected forms */
    /* Tens */
    arabicJarTens.add("عشرين");
    arabicJarTens.add("ثلاثين");
    arabicJarTens.add("أربعين");
    arabicJarTens.add("خمسين");
    arabicJarTens.add("ستين");
    arabicJarTens.add("سبعين");
    arabicJarTens.add("ثمانين");
    arabicJarTens.add("تسعين");

    /* Twos */
    arabicJarTwos.add("مئتين");
    arabicJarTwos.add("ألفين");
    arabicJarTwos.add("مليونين");
    arabicJarTwos.add("مليارين");
    arabicJarTwos.add("تريليونين");
    arabicJarTwos.add("كوادريليونين");
    arabicJarTwos.add("كوينتليونين");
    arabicJarTwos.add("سكستيليونين");

    /* Appended */
    {
      arabicJarAppendedTwos.add("مئتي");
      arabicJarAppendedTwos.add("ألفي");
      arabicJarAppendedTwos.add("مليوني");
      arabicJarAppendedTwos.add("ملياري");
      arabicJarAppendedTwos.add("تريليوني");
      arabicJarAppendedTwos.add("كوادريليوني");
      arabicJarAppendedTwos.add("كوينتليوني");
      arabicJarAppendedTwos.add("سكستيليوني");
    }
    arabicJarOnes.add("");
    arabicJarOnes.add("واحد");
    arabicJarOnes.add("اثنين");
    arabicJarOnes.add("ثلاثة");
    arabicJarOnes.add("أربعة");
    arabicJarOnes.add("خمسة");
    arabicJarOnes.add("ستة");
    arabicJarOnes.add("سبعة");
    arabicJarOnes.add("ثمانية");
    arabicJarOnes.add("تسعة");
    arabicJarOnes.add("عشرة");
    arabicJarOnes.add("أحد عشر");
    arabicJarOnes.add("اثني عشر");
    arabicJarOnes.add("ثلاثة عشر");
    arabicJarOnes.add("أربعة عشر");
    arabicJarOnes.add("خمسة عشر");
    arabicJarOnes.add("ستة عشر");
    arabicJarOnes.add("سبعة عشر");
    arabicJarOnes.add("ثمانية عشر");
    arabicJarOnes.add("تسعة عشر");

    arabicJarFeminineOnes.add("");
    arabicJarFeminineOnes.add("إحدى");
    arabicJarFeminineOnes.add("اثنتين");
    arabicJarFeminineOnes.add("ثلاث");
    arabicJarFeminineOnes.add("أربع");
    arabicJarFeminineOnes.add("خمس");
    arabicJarFeminineOnes.add("ست");
    arabicJarFeminineOnes.add("سبع");
    arabicJarFeminineOnes.add("ثمان");
    arabicJarFeminineOnes.add("تسع");
    arabicJarFeminineOnes.add("عشر");
    arabicJarFeminineOnes.add("إحدى عشرة");
    arabicJarFeminineOnes.add("اثنتي عشرة");
    arabicJarFeminineOnes.add("ثلاث عشرة");
    arabicJarFeminineOnes.add("أربع عشرة");
    arabicJarFeminineOnes.add("خمس عشرة");
    arabicJarFeminineOnes.add("ست عشرة");
    arabicJarFeminineOnes.add("سبع عشرة");
    arabicJarFeminineOnes.add("ثماني عشرة");
    arabicJarFeminineOnes.add("تسع عشرة");

    /* Hundreds */
    arabicJarHundreds.add("");
    arabicJarHundreds.add("مائة");
    arabicJarHundreds.add("مئتين");
    arabicJarHundreds.add("ثلاثمائة");
    arabicJarHundreds.add("أربعمائة");
    arabicJarHundreds.add("خمسمائة");
    arabicJarHundreds.add("ستمائة");
    arabicJarHundreds.add("سبعمائة");
    arabicJarHundreds.add("ثمانمائة");
    arabicJarHundreds.add("تسعمائة");
  }

  static {
    NUMBER_WORDS.put("صفر", 0);
    NUMBER_WORDS.put("واحد", 1);
    NUMBER_WORDS.put("واحدة", 1);
    NUMBER_WORDS.put("اثنان", 2);
    NUMBER_WORDS.put("ثلاثة", 3);
    NUMBER_WORDS.put("أربعة", 4);
    NUMBER_WORDS.put("خمسة", 5);
    NUMBER_WORDS.put("ستة", 6);
    NUMBER_WORDS.put("سبعة", 7);
    NUMBER_WORDS.put("ثمانية", 8);
    NUMBER_WORDS.put("تسعة", 9);
    NUMBER_WORDS.put("عشرة", 10);
    NUMBER_WORDS.put("عشرون", 20);
    NUMBER_WORDS.put("ثلاثون", 30);
    NUMBER_WORDS.put("أربعون", 40);
    NUMBER_WORDS.put("خمسون", 50);
    NUMBER_WORDS.put("ستون", 60);
    NUMBER_WORDS.put("سبعون", 70);
    NUMBER_WORDS.put("ثمانون", 80);
    NUMBER_WORDS.put("تسعون", 90);
    NUMBER_WORDS.put("مئة", 100);
    NUMBER_WORDS.put("مئتان", 200);
    NUMBER_WORDS.put("ثلاثمئة", 300);
    NUMBER_WORDS.put("أربعمئة", 400);
    NUMBER_WORDS.put("خمسمئة", 500);
    NUMBER_WORDS.put("ستمئة", 600);
    NUMBER_WORDS.put("سبعمئة", 700);
    NUMBER_WORDS.put("ثمانمئة", 800);
    NUMBER_WORDS.put("تسعمئة", 900);
    NUMBER_WORDS.put("ثلاثمائة", 300);
    NUMBER_WORDS.put("أربعمائة", 400);
    NUMBER_WORDS.put("خمسمائة", 500);
    NUMBER_WORDS.put("ستمائة", 600);
    NUMBER_WORDS.put("سبعمائة", 700);
    NUMBER_WORDS.put("ثمانمائة", 800);
    NUMBER_WORDS.put("تسعمائة", 900);
    NUMBER_WORDS.put("ألف", 1000);
    NUMBER_WORDS.put("ألفا", 1000);
    NUMBER_WORDS.put("مليون", 1000000);
    NUMBER_WORDS.put("مليونا", 1000000);
    NUMBER_WORDS.put("مليار", 1000000000);
    NUMBER_WORDS.put("ألفان", 2000);
    NUMBER_WORDS.put("ألفين", 2000);
    NUMBER_WORDS.put("مليونان", 2000000);
    NUMBER_WORDS.put("مليونين", 2000000);
    NUMBER_WORDS.put("ملياران", 2000000000);
    NUMBER_WORDS.put("مليارين", 2000000000);
    NUMBER_WORDS.put("أحد", 1);
    NUMBER_WORDS.put("إحدى", 1);
    NUMBER_WORDS.put("اثنين", 2);
    NUMBER_WORDS.put("إثنين", 2);
    NUMBER_WORDS.put("إثنان", 2);
    NUMBER_WORDS.put("اثني", 2);
    NUMBER_WORDS.put("اثنتي", 2);
    NUMBER_WORDS.put("اثنا", 2);
    NUMBER_WORDS.put("إثني", 2);
    NUMBER_WORDS.put("إثنتي", 2);
    NUMBER_WORDS.put("إثنا", 2);
    NUMBER_WORDS.put("ثلاث", 3);
    NUMBER_WORDS.put("أربع", 4);
    NUMBER_WORDS.put("خمس", 5);
    NUMBER_WORDS.put("ست", 6);
    NUMBER_WORDS.put("سبع", 7);
    NUMBER_WORDS.put("ثمان", 8);
    NUMBER_WORDS.put("ثماني", 8);
    NUMBER_WORDS.put("تسع", 9);
    NUMBER_WORDS.put("عشر", 10);
    NUMBER_WORDS.put("ثلاثا", 3);
    NUMBER_WORDS.put("أربعا", 4);
    NUMBER_WORDS.put("خمسا", 5);
    NUMBER_WORDS.put("ستا", 6);
    NUMBER_WORDS.put("سبعا", 7);
    NUMBER_WORDS.put("تسعا", 9);
    NUMBER_WORDS.put("عشرا", 10);
    NUMBER_WORDS.put("عشرين", 20);
    NUMBER_WORDS.put("ثلاثين", 30);
    NUMBER_WORDS.put("أربعين", 40);
    NUMBER_WORDS.put("خمسين", 50);
    NUMBER_WORDS.put("ستين", 60);
    NUMBER_WORDS.put("سبعين", 70);
    NUMBER_WORDS.put("ثمانين", 80);
    NUMBER_WORDS.put("تسعين", 90);
    NUMBER_WORDS.put("مائة", 100);
    NUMBER_WORDS.put("مئتين", 200);
    NUMBER_WORDS.put("آلاف", 1000);
    NUMBER_WORDS.put("ملايين", 1000000);
    NUMBER_WORDS.put("مليارات", 1000000000);
  }
}