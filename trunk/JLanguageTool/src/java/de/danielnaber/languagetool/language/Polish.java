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

import java.util.Locale;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.pl.PolishSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.pl.PolishChunker;
import de.danielnaber.languagetool.tagging.pl.PolishTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.pl.PolishSentenceTokenizer;

public class Polish extends Language {

  private Tagger tagger = new PolishTagger();
  private SentenceTokenizer sentenceTokenizer = new PolishSentenceTokenizer();
  private Disambiguator disambiguator = new PolishChunker();
  private Synthesizer synthesizer = new PolishSynthesizer();
  
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  public String getName() {
    return "Polish";
  }

  public String getShortName() {
    return "pl";
  }

  public Tagger getTagger() {
    return tagger;
  }

  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  public Disambiguator getDisambiguator() {
    return disambiguator;
  }

  public Synthesizer getSynthesizer() {
    return synthesizer;
  }

  public String[] getMaintainers() {
    return new String[]{"Marcin Miłkowski"};
  }

}
