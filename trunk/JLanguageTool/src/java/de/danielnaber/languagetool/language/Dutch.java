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
import de.danielnaber.languagetool.tagging.nl.DutchTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.nl.DutchSentenceTokenizer;

public class Dutch extends Language {

  private Tagger tagger = new DutchTagger();
  private SentenceTokenizer sentenceTokenizer = new DutchSentenceTokenizer();
  private Synthesizer synthesizer = new DutchSynthesizer();
  
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  public String getName() {
    return "Dutch";
  }

  public String getShortName() {
    return "nl";
  }

  public Tagger getTagger() {
    return tagger;
  }

  public Synthesizer getSynthesizer() {
    return synthesizer;
  }
  
  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Ruud Baars")};
  }

  public Set<String> getRelevantRuleIDs() {
    Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS");
    ids.add("UPPERCASE_SENTENCE_START");
    ids.add("WORD_REPEAT_RULE");
    return ids;
  }

}
