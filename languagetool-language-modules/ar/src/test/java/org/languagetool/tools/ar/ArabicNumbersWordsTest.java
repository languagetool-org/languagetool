/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.tools.ar;

import org.junit.Test;
import org.languagetool.tools.ArabicNumbersWords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Mouamle (https://github.com/MouamleH) 07/09/2021
 * @author bluemix (https://github.com/bluemix) 23/08/15
 * @author Taha Zerrouki
 */


public class ArabicNumbersWordsTest {
  final boolean debug = false;

  @Test
  public void representHundreds() {
    Map<String, String> hundreds = new HashMap<>();

    hundreds.put("100", "مائة");
    hundreds.put("200", "مئتان");
    hundreds.put("300", "ثلاثمئة");
    hundreds.put("400", "أربعمئة");
    hundreds.put("500", "خمسمئة");
    hundreds.put("600", "ستمئة");
    hundreds.put("700", "سبعمئة");
    hundreds.put("800", "ثمانمئة");
    hundreds.put("900", "تسعمئة");

    validateNumbersMap(hundreds);
  }

  @Test
  public void representfeminin() {
    Map<String, String> thousands = new HashMap<>();
    thousands.put("11", "إحدى عشرة");
    thousands.put("12", "اثنتا عشرة");
    thousands.put("3", "ثلاث");
    thousands.put("23", "ثلاث وعشرون");


    validateNumbersMap(thousands, true);
  }

  @Test
  public void representInflection() {
    Map<String, String> thousands = new HashMap<>();
    thousands.put("11", "إحدى عشرة");
    thousands.put("12", "اثنتا عشرة");
    thousands.put("3", "ثلاث");
    thousands.put("2022", "ألفان واثنتان وعشرون");

    validateNumbersMap(thousands, true);
  }

  @Test
  public void printCases() {
    List<String> numberlist = new ArrayList<>();
    numberlist.add("2022");
    numberlist.add("1022");
    numberlist.add("3023");
    numberlist.add("12010");
    numberlist.add("0");
    numberlist.add("101");
    boolean feminin = true;
    boolean isAttached = true;
    String inflectCase = "jar";

    for (String nb : numberlist) {
      String words = ArabicNumbersWords.numberToArabicWords(nb, feminin, isAttached, inflectCase);
      if (debug)
        System.out.println("ArabicNumbersWordsTest.java: Number! " + nb + " " + words);
    }

  }

  @Test
  public void representThousands() {
    Map<String, String> thousands = new HashMap<>();
    thousands.put("1000", "ألف");
    thousands.put("2000", "ألفان");
    thousands.put("3000", "ثلاثة آلاف");
    thousands.put("4000", "أربعة آلاف");
    thousands.put("5000", "خمسة آلاف");
    thousands.put("6000", "ستة آلاف");
    thousands.put("7000", "سبعة آلاف");
    thousands.put("8000", "ثمانية آلاف");
    thousands.put("9000", "تسعة آلاف");

    validateNumbersMap(thousands);
  }

  @Test
  public void representHundredThousands() {
    Map<String, String> hundredThousands = new HashMap<>();
    hundredThousands.put("100000", "مائة ألف");
    hundredThousands.put("200000", "مئتا ألف");
    hundredThousands.put("300000", "ثلاثمئة ألف");

    validateNumbersMap(hundredThousands);
  }

  @Test
  public void representHundredMillions() {
    Map<String, String> hundredThousands = new HashMap<>();
    hundredThousands.put("100000000", "مائة مليون");
    hundredThousands.put("200000000", "مئتا مليون");
    hundredThousands.put("300000000", "ثلاثمئة مليون");

    validateNumbersMap(hundredThousands);
  }

  private void validateNumbersMap(Map<String, String> numbersMap) {
    validateNumbersMap(numbersMap, false);
  }

  private void validateNumbersMap(Map<String, String> numbersMap, boolean feminin) {
    validateNumbersMap(numbersMap, feminin, false, "");

  }

  private void validateNumbersMap(Map<String, String> numbersMap, boolean feminine, boolean isAttached, String inflectCase) {
    for (Map.Entry<String, String> entry : numbersMap.entrySet()) {
      String words = ArabicNumbersWords.numberToArabicWords(entry.getKey(), feminine, isAttached, inflectCase);
      assertEquals(entry.getValue(), words);
    }
  }
}
