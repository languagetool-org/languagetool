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
import de.danielnaber.languagetool.synthesis.nl.DutchSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.nl.DutchRuleDisambiguator;
import de.danielnaber.languagetool.tagging.nl.DutchTagger;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.nl.DutchSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.nl.DutchWordTokenizer;

public class Dutch extends Language {

  private Tagger tagger = new DutchTagger();
  private SentenceTokenizer sentenceTokenizer = new SRXSentenceTokenizer("nl");
  private Synthesizer synthesizer = new DutchSynthesizer();
  private Disambiguator disambiguator = new DutchRuleDisambiguator();
  private Tokenizer wdTokenizer = new DutchWordTokenizer();

  private static final String[] COUNTRIES = { "NL", "BE" };

  public final Locale getLocale() {
    return new Locale(getShortName());
  }

  public final String getName() {
    return "Dutch";
  }

  public final String getShortName() {
    return "nl";
  }

  public final String[] getCountryVariants() {
    return COUNTRIES;
  }

  public final Tagger getTagger() {
    return tagger;
  }

  public final Synthesizer getSynthesizer() {
    return synthesizer;
  }

  public final SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  public final Tokenizer getWordTokenizer() {
    return wdTokenizer;
  }

  public final Disambiguator getDisambiguator() {
    return disambiguator;
  }

  public final Contributor[] getMaintainers() {
    final Contributor contributor = new Contributor("Ruud Baars");
    contributor.setUrl("http://www.opentaal.org");
    return new Contributor[] { contributor };
  }

  public final Set<String> getRelevantRuleIDs() {
    final Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS");
    ids.add("UPPERCASE_SENTENCE_START");
    ids.add("WORD_REPEAT_RULE");
    ids.add("WHITESPACE_RULE");
    return ids;
  }

}
