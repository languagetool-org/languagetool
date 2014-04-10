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

import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.ml.MorfologikMalayalamSpellerRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ml.MalayalamTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ml.MalayalamWordTokenizer;

import java.util.Arrays;
import java.util.List;

public class Malayalam extends Language {

  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private String name = "Malayalam";

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public final String getShortName() {
    return "ml";
  }

  @Override
  public final Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new MalayalamWordTokenizer();
    }
    return wordTokenizer;
  }
  
  @Override
  public final String[] getCountries() {
    return new String[]{"IN"};
  }
  
  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new MalayalamTagger();
    }
    return tagger;
  }
    
  @Override
  public final Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Jithesh.V.S") };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,
            MorfologikMalayalamSpellerRule.class,
            UppercaseSentenceStartRule.class,
            WordRepeatRule.class,
            WhitespaceRule.class
    );
  }

}
