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
import org.languagetool.rules.patterns.Unifier;
import org.languagetool.rules.ro.CompoundRule;
import org.languagetool.rules.ro.RomanianWordRepeatBeginningRule;
import org.languagetool.rules.ro.SimpleReplaceRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ro.RomanianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.ro.RomanianRuleDisambiguator;
import org.languagetool.tagging.ro.RomanianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ro.RomanianWordTokenizer;

/**
 *
 * @author Ionuț Păduraru
 * @since 24.02.2009 22:18:21
 */
public class Romanian extends Language {

  private Tagger tagger;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
  private Tokenizer wordTokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private Unifier unifier;
  private Unifier disambiguationUnifier;

  @Override
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  @Override
  public String getName() {
    return "Romanian";
  }

  @Override
  public String getShortName() {
    return "ro";
  }

  @Override
  public String[] getCountryVariants() {
    return new String[]{"RO"};
  }

  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "„", "«", "»" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "”", "»", "«" };
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new RomanianTagger();
    }
    return tagger;
  }

  @Override
  public Contributor[] getMaintainers() {
    final Contributor contributor = new Contributor("Ionuț Păduraru");
    contributor.setUrl("http://www.archeus.ro");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class,
            GenericUnpairedBracketsRule.class,
            HunspellRule.class,
            WordRepeatRule.class,
            // specific to Romanian:
            RomanianWordRepeatBeginningRule.class,
            SimpleReplaceRule.class,
            CompoundRule.class
    );
  }

  @Override
  public final Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new RomanianSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public final Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new RomanianRuleDisambiguator();
    }
    return disambiguator;
  }

  @Override
  public final Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new RomanianWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }
  
  @Override
  public Unifier getUnifier() {
    if (unifier == null) {
      unifier = new Unifier();
    }
    return unifier;
  }

  @Override
  public Unifier getDisambiguationUnifier() {
    if (disambiguationUnifier == null) {
    	disambiguationUnifier = new Unifier();
    }
    return disambiguationUnifier; 
  }

}
