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
import org.languagetool.rules.de.VerbAgreementRule;
import org.languagetool.rules.de.WiederVsWiderRule;
import org.languagetool.rules.de.WordCoherencyRule;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.de.GermanRuleDisambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

/**
 * Support for German - use the sub classes {@link GermanyGerman}, {@link SwissGerman}, or {@link AustrianGerman}
 * if you need spell checking.
 */
public class German extends Language {

  private Tagger tagger;
  private Synthesizer synthesizer;
  private SentenceTokenizer sentenceTokenizer;
  private Disambiguator disambiguator;

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
    return "German";
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
    if (tagger == null) {
      synchronized (this) {
        if (tagger == null) {
          tagger = new GermanTagger();
        }
      }
    }
    return tagger;
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
            VerbAgreementRule.class,
            WordCoherencyRule.class,
            WiederVsWiderRule.class
    );
  }

}
