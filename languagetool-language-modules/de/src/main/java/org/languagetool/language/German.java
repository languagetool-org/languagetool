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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.GermanChunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
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
public class German extends Language implements AutoCloseable {

  private static final Language GERMANY_GERMAN = new GermanyGerman();
  
  private Tagger tagger;
  private Synthesizer synthesizer;
  private SentenceTokenizer sentenceTokenizer;
  private Disambiguator disambiguator;
  private GermanChunker chunker;
  private CompoundWordTokenizer compoundTokenizer;
  private GermanCompoundTokenizer strictCompoundTokenizer;
  private LanguageModel languageModel;

  /**
   * @deprecated use {@link GermanyGerman}, {@link AustrianGerman}, or {@link SwissGerman} instead -
   *  they have rules for spell checking, this class doesn't (deprecated since 3.2)
   */
  @Deprecated
  public German() {
  }
  
  @Override
  public Language getDefaultLanguageVariant() {
    return GERMANY_GERMAN;
  }
  
  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new GermanRuleDisambiguator();
    }
    return disambiguator;
  }

  /**
   * @since 2.9
   */
  @Override
  public Chunker getPostDisambiguationChunker() {
    if (chunker == null) {
      chunker = new GermanChunker();
    }
    return chunker;
  }

  @Override
  public String getName() {
    return "German";
  }

  @Override
  public String getShortCode() {
    return "de";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"LU", "LI", "BE"};
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
  @NotNull
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
        Contributors.DANIEL_NABER,
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                    Example.wrong("Die Partei<marker> ,</marker> die die letzte Wahl gewann."),
                    Example.fixed("Die Partei<marker>,</marker> die die letzte Wahl gewann.")),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "„", "»", "«"),
                    Arrays.asList("]", ")", "}", "“", "«", "»")),
            new UppercaseSentenceStartRule(messages, this,
                    Example.wrong("Das Haus ist alt. <marker>es</marker> wurde 1950 gebaut."),
                    Example.fixed("Das Haus ist alt. <marker>Es</marker> wurde 1950 gebaut.")),
            new MultipleWhitespaceRule(messages, this),
            // specific to German:
            new OldSpellingRule(messages),
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
            new SubjectVerbAgreementRule(messages, this),
            new WordCoherencyRule(messages),
            new SimilarNameRule(messages),
            new WiederVsWiderRule(messages)
    );
  }

  /**
   * @since 2.7
   */
  public CompoundWordTokenizer getNonStrictCompoundSplitter() {
    if (compoundTokenizer == null) {
      try {
        GermanCompoundTokenizer tokenizer = new GermanCompoundTokenizer(false);  // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
        compoundTokenizer = word -> new ArrayList<>(tokenizer.tokenize(word));
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

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    if (languageModel == null) {
      languageModel = new LuceneLanguageModel(new File(indexDir, getShortCode()));
      // for testing:
      //languageModel = new BerkeleyRawLanguageModel(new File("/media/Data/berkeleylm/google_books_binaries/ger.blm.gz"));
      //languageModel = new BerkeleyLanguageModel(new File("/media/Data/berkeleylm/google_books_binaries/ger.blm.gz"));
    }
    return languageModel;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.asList(
            new GermanConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /**
   * Closes the language model, if any. 
   * @since 3.1 
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ": return -10;
      case "OLD_SPELLING_INTERNAL": return 10;
      case "CONFUSION_RULE": return -1;  // probably less specific than the rules from grammar.xml
    }
    return 0;
  }

}
