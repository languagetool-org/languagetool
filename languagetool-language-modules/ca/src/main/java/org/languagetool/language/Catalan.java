/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.*;
import org.languagetool.rules.ca.AccentuationCheckRule;
import org.languagetool.rules.ca.CatalanUnpairedBracketsRule;
import org.languagetool.rules.ca.CatalanUnpairedExclamationMarksRule;
import org.languagetool.rules.ca.CatalanUnpairedQuestionMarksRule;
import org.languagetool.rules.ca.CatalanWordRepeatRule;
import org.languagetool.rules.ca.CatalanWrongWordInContextRule;
import org.languagetool.rules.ca.ComplexAdjectiveConcordanceRule;
import org.languagetool.rules.ca.MorfologikCatalanSpellerRule;
import org.languagetool.rules.ca.ReflexiveVerbsRule;
import org.languagetool.rules.ca.SimpleReplaceRule;
import org.languagetool.rules.ca.SimpleReplaceVerbsRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ca.CatalanHybridDisambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;

public class Catalan extends Language {
  
  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
  private String name = "Catalan";

  private static final Language GENERAL_CATALAN = new GeneralCatalan();
  
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String[] getCountries() {
    return new String[]{};
  }
  
  @Override
  public String getShortName() {
    return "ca";
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return GENERAL_CATALAN;
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Ricard Roca"), new Contributor("Jaume Ortolà") };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            CatalanUnpairedBracketsRule.class,
            UppercaseSentenceStartRule.class,
            MultipleWhitespaceRule.class,            
            LongSentenceRule.class,
            // specific to Catalan:
            CatalanWordRepeatRule.class,
            MorfologikCatalanSpellerRule.class,
            CatalanUnpairedQuestionMarksRule.class,
            CatalanUnpairedExclamationMarksRule.class,
            AccentuationCheckRule.class,
            ComplexAdjectiveConcordanceRule.class,
            CatalanWrongWordInContextRule.class,
            ReflexiveVerbsRule.class,
            SimpleReplaceVerbsRule.class,
            SimpleReplaceRule.class
            //CastellanismesReplaceRule.class,
            //AccentuacioReplaceRule.class
    );
  }

  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new CatalanTagger();
    }
    return tagger;
  }

  @Override
  public final Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new CatalanSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }
  
  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new CatalanHybridDisambiguator();
    }
    return disambiguator;
  }  
  
  @Override
  public final Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new CatalanWordTokenizer();
    }
    return wordTokenizer;
  }

}
