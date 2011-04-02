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
package de.danielnaber.languagetool.language;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.Unifier;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.fr.FrenchRuleDisambiguator;
import de.danielnaber.languagetool.tagging.fr.FrenchTagger;

public class French extends Language {

  private final Tagger tagger = new FrenchTagger();
  private final Disambiguator disambiguator = new FrenchRuleDisambiguator();
  private static final Unifier FRENCH_UNIFIER = new Unifier();
  
  private static final String[] COUNTRIES = {"FR", "", "BE", "CH", "CA", 
    "LU", "MC", "CM", "CI", "HI", "ML", "SN", "CD", "MA", "RE"
  };
  
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  public String getName() {
    return "French";
  }

  public String getShortName() {
    return "fr";
  }
  
  public String[] getCountryVariants() {
    return COUNTRIES;
  }

  public Tagger getTagger() {
    return tagger;
  }

  public Disambiguator getDisambiguator() {
    return disambiguator;
  }
  
  public Unifier getUnifier() {
    return FRENCH_UNIFIER;
  }

  public Contributor[] getMaintainers() {
    final Contributor hVoisard = new Contributor("Hugo Voisard");
    hVoisard.setRemark("2006-2007");
    return new Contributor[] {
        new Contributor("Agnes Souque"),
        hVoisard,
        new Contributor("Dominique Pellé"),
    };
  }

  public Set<String> getRelevantRuleIDs() {
    final Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS");
    ids.add("UPPERCASE_SENTENCE_START");    
    ids.add("WHITESPACE_RULE");
    ids.add("FRENCH_WHITESPACE");
    return ids;
  }

}
