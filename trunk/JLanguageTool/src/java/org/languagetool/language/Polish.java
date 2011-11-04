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

import java.util.*;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.*;
import de.danielnaber.languagetool.rules.patterns.Unifier;
import de.danielnaber.languagetool.rules.pl.CompoundRule;
import de.danielnaber.languagetool.rules.pl.PolishUnpairedBracketsRule;
import de.danielnaber.languagetool.rules.pl.PolishWordRepeatRule;
import de.danielnaber.languagetool.rules.pl.SimpleReplaceRule;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.pl.PolishSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.pl.PolishHybridDisambiguator;
import de.danielnaber.languagetool.tagging.pl.PolishTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;

public class Polish extends Language {

  private static final Unifier POLISH_UNIFIER = new Unifier();
  private static final Unifier POLISH_DISAMB_UNIFIER = new Unifier();

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Disambiguator disambiguator;
  private Synthesizer synthesizer;

  @Override
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  @Override
  public String getName() {
    return "Polish";
  }

  @Override
  public String getShortName() {
    return "pl";
  }
  
  @Override
  public String[] getCountryVariants() {
    return new String[]{"PL"};
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new PolishTagger();
    }
    return tagger;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(getShortName());
    }
    return sentenceTokenizer;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new PolishHybridDisambiguator();
    }
    return disambiguator;
  }
 
  @Override
  public Unifier getUnifier() {
    return POLISH_UNIFIER;
  }

  @Override
  public Unifier getDisambiguationUnifier() {
    return POLISH_DISAMB_UNIFIER;
  }
  
  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new PolishSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Marcin Miłkowski")};
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            UppercaseSentenceStartRule.class,
            WordRepeatRule.class,
            WhitespaceRule.class,
            // specific to Polish:
            PolishUnpairedBracketsRule.class,
            PolishWordRepeatRule.class,
            CompoundRule.class,
            SimpleReplaceRule.class
    );
  }

}
