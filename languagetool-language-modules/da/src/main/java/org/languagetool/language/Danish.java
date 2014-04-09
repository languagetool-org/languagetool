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

import java.util.Arrays;
import java.util.List;

import org.languagetool.Language;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;
import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.da.DanishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class Danish extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Disambiguator disambiguator;
  private String name = "Danish";

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
    return "da";
  }

  @Override
  public final String[] getCountries() {
    return new String[]{"DK"};
  }
  
  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "\"", "”" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "\"", "”" };
  }
  
  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new DanishTagger();
    }
    return tagger;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }
  
  @Override
  public final Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Danish());
    }
    return disambiguator;
  }

  @Override
  public final Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Esben Aaberg"), new Contributor("Henrik Bendt") };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,  // correction for genitive apostrophes eg. "Lis' hund" made in UnpairedQuotesBracketsRule
            HunspellNoSuggestionRule.class,
            UppercaseSentenceStartRule.class,  // abbreviation exceptions, done in DanishSentenceTokenizer
            // "WORD_REPEAT_RULE" implemented in grammar.xml
            WhitespaceRule.class
    );
  }

}
