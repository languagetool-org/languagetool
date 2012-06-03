/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

import java.util.*;

import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.rules.sv.CompoundRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.sv.SwedishTagger;

public class Swedish extends Language {

  private Tagger tagger;

  @Override
  public final Locale getLocale() {
    return new Locale(getShortName());
  }

  @Override
  public final String getName() {
    return "Swedish";
  }

  @Override
  public final String getShortName() {
    return "sv";
  }

  @Override
  public final String[] getCountryVariants() {
    return new String[]{"SE", "FI"};
  }
  
  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new SwedishTagger();
    }
    return tagger;
  }

  @Override
  public final Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Niklas Johansson")};
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,
            HunspellRule.class,
            UppercaseSentenceStartRule.class,
            WordRepeatRule.class,
            WhitespaceRule.class,
            CompoundRule.class
    );
  }

}
