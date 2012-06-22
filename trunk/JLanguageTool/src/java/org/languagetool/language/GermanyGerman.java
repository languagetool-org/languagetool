/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import java.util.Arrays;
import java.util.List;

import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;
import org.languagetool.rules.de.AgreementRule;
import org.languagetool.rules.de.CaseRule;
import org.languagetool.rules.de.CompoundRule;
import org.languagetool.rules.de.DashRule;
import org.languagetool.rules.de.GermanDoublePunctuationRule;
import org.languagetool.rules.de.GermanWordRepeatBeginningRule;
import org.languagetool.rules.de.GermanWordRepeatRule;
import org.languagetool.rules.de.GermanWrongWordInContextRule;
import org.languagetool.rules.de.WiederVsWiderRule;
import org.languagetool.rules.de.WordCoherencyRule;
import org.languagetool.rules.de.MorfologikGermanyGermanSpellerRule;

public class GermanyGerman extends German {

  @Override
  public final String[] getCountryVariants() {
    return new String[]{"DE"};
  }

  @Override
  public final String getName() {
    return "German (Germany)";
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            GermanDoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,            
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class,
            // specific to German:
            GermanWordRepeatRule.class,
            GermanWordRepeatBeginningRule.class,
            GermanWrongWordInContextRule.class,
            AgreementRule.class,
            CaseRule.class,
            CompoundRule.class,
            DashRule.class,
            WordCoherencyRule.class,
            WiederVsWiderRule.class,
            //specific to Germany: speller
            MorfologikGermanyGermanSpellerRule.class
    );
  }

  
}
