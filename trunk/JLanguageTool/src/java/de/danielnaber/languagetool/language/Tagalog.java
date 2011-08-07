/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.*;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.tl.TagalogTagger;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** 
 * @author Nathaniel Oco
 */
public class Tagalog extends Language {

  private final Tagger tagger = new TagalogTagger();

  @Override
  public Locale getLocale() {
    return new Locale("tl");
  }

  @Override
  public String getName() {
    return "Tagalog";
  }

  @Override
  public String getShortName() {
    return "tl";
  }

  @Override
  public String[] getCountryVariants() {
    return new String[] {"TL"};
  }
  
  @Override
  public Tagger getTagger() {
    return tagger;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Nathaniel Oco"), new Contributor("Allan Borra") };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class
    );
  }

}
