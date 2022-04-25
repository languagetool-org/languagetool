/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.de.AgreementRule.GrammarCategory;
import org.languagetool.tagging.de.AnalyzedGermanToken;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.Determination;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.languagetool.tagging.de.GermanToken.Genus.*;

class AgreementTools {

  private AgreementTools() {
  }

  /** Return Kasus, Numerus, Genus of those forms with a determiner. */
  static Set<String> getAgreementCategories(AnalyzedTokenReadings aToken, Set<GrammarCategory> omit, boolean skipSol) {
    Set<String> set = new HashSet<>();
    List<AnalyzedToken> readings = aToken.getReadings();
    for (AnalyzedToken tmpReading : readings) {
      if (skipSol && tmpReading.getPOSTag() != null && tmpReading.getPOSTag().endsWith(":SOL")) {
        // SOL = alleinstehend - needs to be skipped so we find errors like "An der roter Ampel."
        continue;
      }
      AnalyzedGermanToken reading = new AnalyzedGermanToken(tmpReading);
      if (reading.getCasus() == null && reading.getNumerus() == null &&
        reading.getGenus() == null) {
        continue;
      }
      if (reading.getGenus() == ALLGEMEIN &&
        tmpReading.getPOSTag() != null && !tmpReading.getPOSTag().endsWith(":STV") &&  // STV: stellvertretend (!= begleitend)
        !possessiveSpecialCase(aToken, tmpReading)) {
        // genus=ALG in the original data. Not sure if this is allowed, but expand this so
        // e.g. "Ich Arbeiter" doesn't get flagged as incorrect:
        if (reading.getDetermination() == null) {
          // Nouns don't have the determination property (definite/indefinite), and as we don't want to
          // introduce a special case for that, we just pretend they always fulfill both properties:
          set.add(makeString(reading.getCasus(), reading.getNumerus(), MASKULINUM, Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), MASKULINUM, Determination.INDEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), FEMININUM, Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), FEMININUM, Determination.INDEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), NEUTRUM, Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), NEUTRUM, Determination.INDEFINITE, omit));
        } else {
          set.add(makeString(reading.getCasus(), reading.getNumerus(), MASKULINUM, reading.getDetermination(), omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), FEMININUM, reading.getDetermination(), omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), NEUTRUM, reading.getDetermination(), omit));
        }
      } else {
        if (reading.getDetermination() == null || "jed".equals(tmpReading.getLemma()) || "manch".equals(tmpReading.getLemma())) {  // "jeder" etc. needs a special case to avoid false alarm
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), Determination.INDEFINITE, omit));
        } else {
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), reading.getDetermination(), omit));
        }
      }
    }
    return set;
  }

  private static boolean possessiveSpecialCase(AnalyzedTokenReadings aToken, AnalyzedToken tmpReading) {
    // would cause error misses as it contains 'ALG', e.g. in "Der Zustand meiner Gehirns."
    return aToken.hasPosTagStartingWith("PRO:POS") && StringUtils.equalsAny(tmpReading.getLemma(), "ich", "sich");
  }

  private static String makeString(GermanToken.Kasus casus, GermanToken.Numerus num, GermanToken.Genus gen,
                            Determination determination, Set<GrammarCategory> omit) {
    List<String> l = new ArrayList<>();
    if (casus != null && !omit.contains(GrammarCategory.KASUS)) {
      l.add(casus.toString());
    }
    if (num != null && !omit.contains(GrammarCategory.NUMERUS)) {
      l.add(num.toString());
    }
    if (gen != null && !omit.contains(GrammarCategory.GENUS)) {
      l.add(gen.toString());
    }
    if (determination != null) {
      l.add(determination.toString());
    }
    return String.join("/", l);
  }

}
