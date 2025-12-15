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

  /**
   * Patterns for apostrophation
   **/
  private static final Pattern pMascYes = Pattern.compile("h?[aeiouàèéíòóú].*",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern pMascNo = Pattern.compile("h?[ui][aeioàèéóò].+",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern pFemYes = Pattern.compile("h?[aeoàèéíòóú].*|h?[ui][^aeiouàèéíòóúüï]+[aeiou][ns]?|urbs"
    , Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern pFemNo = Pattern.compile("host|ira|inxa",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  // From mots_HAC_ASPIRADA2 and mots_HAC_ASPIRADA in entities.ent
  private static final Pattern pHacAspirada = Pattern.compile("Higgs|high|Hildesheim|Hill|hijabs?|Hillary|Himmler|hip" +
    "-hop|hippies|hippy|hipsters?|Hirado|His|hits?|Hubei|Hudson|Hunter|Husserl|Huygens|husky|Utah|hides?|honey" +
    ".*|Hartle.*|happy|happi.*|Hulk|Heart.*|Haakon|Halberstadt|Harley|Huck" +
    ".*|Hanna|haka|hakes|Hama|Hornbostel|Heidi|Hayao|Hansi|Haas|Hindemith|user|users|one|head|history|Human|Hampshire" +
    "|Hovedstaden|Handmade|Helm|Hahnem.*|hikimor.*|Houdini|Hugging|Heritage|hardcore|hancock|hender.*|h[ei][zs]b[ou]l" +
    ".*|harira|Hawth.*|Henk.*|Humphry|Hohle|Höhle|Hooke|hajj.*|Hochschule|Hoch" +
    ".*|Hutt|Hansel|Henley|hook|Handstand|Hull|Hatshepsut|Hatchepsut|Hana|Hamri|Hanley|Halis|Huxley|Hess|Hatteras" +
    "|Herzberg|Hanlon|Harriet|hawl.*|hard|hip|herderi" +
    ".*|Hangouts|Hayes|hostings?|Hal|hajj|Hermann|Hannah|Hertzsprung|Hotmail|Homrani|Harris|Harvey|Hunspell|Hassan" +
    "|Haddock|Haarle[mn].*|Hainan|haendel.*|händel.*|habermas.*|hadits?|Hanuk?kà|hack" +
    ".*|Harlem|Harper|Hartford|Haifa|haikus?|haima|haimes|Haikou|halal|halar|Halifax|Halmstad|halls?|Halle|Halley" +
    "|Hallstatt|Hallstein|Halloweens?|Hals|herr|Herut|Hamadan|Hamas|Hamàs|hamilton.*|Hamlet" +
    ".*|hammams?|Hammond|Hampton|hàmsters?|h[aà]ndicaps?|Hangzhou|Hannover|Hanoi|Hans|Hansa|hanseàti[cq]" +
    ".*|happenings?|Harbin|hardware|Haneke|harolds?|Hatay|Hamleigh " +
    "|Harrisburg|Harrison|harrods?|harry|Hartley|Hartmann?|Hartree|Haruki|Har[td]?vard|Harz|hash" +
    ".*|Hastings|Havel|Havilland|hawai.*|hawk.*|Hayek|Haydn.*|Hayworth|Heard|hearst|Heathrow|heav.*|hegel" +
    ".*|Hebei|Hedmark|Heerenveen|Hedw.*|Heerlen|Hefei|Heidelberg|Heide[gn].*|Heilbronn|Heilongjiang|Heilig.*|hei[nk]" +
    ".*|Heisen.*|Heitz|Helmand|Helmholtz|Helen|Helsingborg|Hèlsinki|Heming.*|Henan|henna|hennes|Henry|Hepburn|herbert" +
    ".*|Herder|Hereford|Herford|Herning|Hertfordshire|Herzog|Hesse|Hessen.*|Hewlett.*|H[ie]zbol·?l.+|high.*|hilbert" +
    ".*|Hilda|Hillingdon|hinden.*|Hilton|hinterlands?|Hirsch.*|Hitch.*|hitler" +
    ".*|Hilversum|Hobart|Hockenheim|Hodeida|Hohhot|Hokkaido|hobbes.*|hobby|Hogw.*|hobbies|Hodgkin|Hohen" +
    ".*|Hölderlin|h[òo]ldings?|holy.*|hollywood.*|Holmes.*|Holstein|Hong|Hong-Kong|hongk.+|Honolu" +
    ".+|Honsh[uū]|h[òo]bbits?|hooligan.*|hoover.*|hopkins|Hork.*|Horowitz|horst|H[ou]f" +
    ".*|Houla|house|Houston|Howard|Hoyerswerda|Hunan|Huddersfield|Hunedoara|huskys?|huskies|hubs?|Hubble|humbold" +
    ".*|Hume|hunting.*|Hussein|husseinit.+|Unity|university|united.*|European|OneDrive",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  // |(?-i)Hagen|Hamada|Haber

  public static String getPrepositionAndDeterminer(String newForm, String genderNumber, String preposition) {
    String apos = ""; // s'apostrofa o no
    if (!preposition.isEmpty()) {
      //a=a d=de per=p
      preposition = preposition.substring(0, 1).toLowerCase();
    }
    if (!pHacAspirada.matcher(newForm).matches()) {
      if (genderNumber.equals("MS")) {
        if (pMascYes.matcher(newForm).matches() && !pMascNo.matcher(newForm).matches()) {
          apos = "apos";
        }
      } else if (genderNumber.equals("FS")) {
        if (pFemYes.matcher(newForm).matches() && !pFemNo.matcher(newForm).matches()) {
          apos = "apos";
        }
      }
    }
    String suggestion = prepDet.get(preposition + genderNumber + apos);
    return suggestion;
  }
}
