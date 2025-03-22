/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Jaume Ortolà
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

package org.languagetool.rules.ca;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ApostophationHelper {

  private static Map<String, String> prepDet = new HashMap<>();
  static {
    prepDet.put("MS", "el ");
    prepDet.put("FS", "la ");
    prepDet.put("MP", "els ");
    prepDet.put("FP", "les ");
    prepDet.put("MSapos", "l'");
    prepDet.put("FSapos", "l'");
    prepDet.put("aMS", "al ");
    prepDet.put("aFS", "a la ");
    prepDet.put("aMP", "als ");
    prepDet.put("aFP", "a les ");
    prepDet.put("aMSapos", "a l'");
    prepDet.put("aFSapos", "a l'");
    prepDet.put("dMS", "del ");
    prepDet.put("dFS", "de la ");
    prepDet.put("dMP", "dels ");
    prepDet.put("dFP", "de les ");
    prepDet.put("dMSapos", "de l'");
    prepDet.put("dFSapos", "de l'");
    prepDet.put("pMS", "pel ");
    prepDet.put("pFS", "per la ");
    prepDet.put("pMP", "pels ");
    prepDet.put("pFP", "per les ");
    prepDet.put("pMSapos", "per l'");
    prepDet.put("pFSapos", "per l'");
  }

  /** Patterns for apostrophation **/
  private static final Pattern pMascYes = Pattern.compile("h?[aeiouàèéíòóú].*",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pMascNo = Pattern.compile("h?[ui][aeioàèéóò].+",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pFemYes = Pattern.compile("h?[aeoàèéíòóú].*|h?[ui][^aeiouàèéíòóúüï]+[aeiou][ns]?|urbs",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pFemNo = Pattern.compile("host|ira|inxa",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  public static String getPrepositionAndDeterminer(String newForm, String genderNumber, String preposition) {
    String apos = ""; // s'apostrofa o no
    if (!preposition.isEmpty()) {
      //a=a d=de per=p
      preposition = preposition.substring(0, 1).toLowerCase();
    }
    if (genderNumber.equals("MS")) {
      if (pMascYes.matcher(newForm).matches() && !pMascNo.matcher(newForm).matches()) {
        apos = "apos";
      }
    } else if (genderNumber.equals("FS")) {
      if (pFemYes.matcher(newForm).matches() && !pFemNo.matcher(newForm).matches()) {
        apos = "apos";
      }
    }
    String suggestion = prepDet.get(preposition + genderNumber + apos);
    return suggestion;
  }
}
