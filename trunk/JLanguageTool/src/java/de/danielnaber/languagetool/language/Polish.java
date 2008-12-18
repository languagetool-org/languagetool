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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.pl.PolishSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.pl.PolishHybridDisambiguator;
import de.danielnaber.languagetool.tagging.pl.PolishTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.pl.PolishSentenceTokenizer;

public class Polish extends Language {

  private Tagger tagger = new PolishTagger();
  private SentenceTokenizer sentenceTokenizer = new PolishSentenceTokenizer();
  private Disambiguator disambiguator = new PolishHybridDisambiguator();
  private Synthesizer synthesizer = new PolishSynthesizer();
  
  private static final String[] COUNTRIES = {"PL"}; 
  
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
    return COUNTRIES;
  }
  
  @Override
  public Tagger getTagger() {
    return tagger;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  @Override
  public Disambiguator getDisambiguator() {
    return disambiguator;
  }

  @Override
  public Synthesizer getSynthesizer() {
    return synthesizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Marcin Miłkowski")};
  }

  @Override
  public Set<String> getRelevantRuleIDs() {
    final Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS");
    ids.add("UPPERCASE_SENTENCE_START");
    ids.add("WORD_REPEAT_RULE");
    ids.add("WHITESPACE_RULE");
    // specific to Polish:
    ids.add("PL_WORD_REPEAT");
    ids.add("PL_COMPOUNDS");
    return ids;
  }

}
