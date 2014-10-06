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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import de.abelssoft.wordtools.jwordsplitter.AbstractWordSplitter;
import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.de.*;
import org.languagetool.rules.de.SentenceWhitespaceRule;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.de.GermanRuleDisambiguator;
import org.languagetool.tokenizers.CompoundWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;

/**
 * Support for German - use the sub classes {@link GermanyGerman}, {@link SwissGerman}, or {@link AustrianGerman}
 * if you need spell checking.
 */
public class German extends Language {

  private Tagger tagger;
  private Synthesizer synthesizer;
  private SentenceTokenizer sentenceTokenizer;
  private Disambiguator disambiguator;
  private String name = "German";
  private CompoundWordTokenizer compoundTokenizer;
  private GermanCompoundTokenizer strictCompoundTokenizer;

  @Override
  public Language getDefaultLanguageVariant() {
    return new GermanyGerman();
  }
  
  @Override
  public final Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new GermanRuleDisambiguator();
    }
    return disambiguator;
  }

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
    return "de";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"LU", "LI", "BE"};
  }

  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "„", "»", "«" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "“", "«", "»" };
  }

  @Override
  public Tagger getTagger() {
    Tagger t = tagger;
    if (t == null) {
      synchronized (this) {
        t = tagger;
        if (t == null) {
          tagger = t = new GermanTagger();
        }
      }
    }
    return t;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new GermanSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Jan Schreiber"),
        new Contributor("Markus Brenneis"),
        Contributors.DANIEL_NABER,
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new GenericUnpairedBracketsRule(messages, this),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            // specific to German:
            new SentenceWhitespaceRule(messages),
            new GermanDoublePunctuationRule(messages),
            new MissingVerbRule(messages, this),
            new GermanWordRepeatRule(messages, this),
            new GermanWordRepeatBeginningRule(messages, this),
            new GermanWrongWordInContextRule(messages),
            new AgreementRule(messages, this),
            new CaseRule(messages, this),
            new CompoundRule(messages),
            new DashRule(messages),
            new VerbAgreementRule(messages, this),
            new WordCoherencyRule(messages),
            new WiederVsWiderRule(messages)
    );
  }

  /**
   * @since 2.7
   */
  public CompoundWordTokenizer getNonStrictCompoundSplitter() {
    if (compoundTokenizer == null) {
      try {
        final AbstractWordSplitter wordSplitter = new GermanWordSplitter(false);
        wordSplitter.setStrictMode(false); // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
        ((GermanWordSplitter)wordSplitter).setMinimumWordLength(3);
        compoundTokenizer = new CompoundWordTokenizer() {
          @Override
          public List<String> tokenize(String word) {
            return new ArrayList<>(wordSplitter.splitWord(word));
          }
        };
      } catch (IOException e) {
        throw new RuntimeException("Could not set up German compound splitter", e);
      }
    }
    return compoundTokenizer;
  }

  /**
   * @since 2.7
   */
  public GermanCompoundTokenizer getStrictCompoundTokenizer() {
    if (strictCompoundTokenizer == null) {
      try {
        strictCompoundTokenizer = new GermanCompoundTokenizer();
      } catch (IOException e) {
        throw new RuntimeException("Could not set up strict German compound splitter", e);
      }
    }
    return strictCompoundTokenizer;
  }
  
}
