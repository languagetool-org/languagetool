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
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.es.SpanishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.es.SpanishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.es.SpanishWordTokenizer;

public class Spanish extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Synthesizer synthesizer;
  private Tagger tagger;
  private Disambiguator disambiguator;
  private String name = "Spanish";

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String getShortName() {
    return "es";
  }

  @Override
  public String[] getCountries() {
    return new String[]{
            "ES", "", "MX", "GT", "CR", "PA", "DO",
            "VE", "PE", "AR", "EC", "CL", "UY", "PY",
            "BO", "SV", "HN", "NI", "PR", "US", "CU"
    };
  }
  
  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "“", "«", "»", "¿", "¡" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "”", "»", "«", "?", "!" };
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new SpanishTagger();
    }
    return tagger;
  }
  
  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Spanish());
    }
    return disambiguator;
  }
  
  @Override
  public final Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new SpanishWordTokenizer();
    }
    return wordTokenizer;
  }
  
  @Override
  public final Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new SpanishSynthesizer();
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
  public Contributor[] getMaintainers() {
    final Contributor contributor = new Contributor("Juan Martorell");
    contributor.setUrl("http://languagetool-es.blogspot.com/");
    return new Contributor[] { contributor };
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
            MultipleWhitespaceRule.class
    );
  }

}
