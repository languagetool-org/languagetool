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
import de.danielnaber.languagetool.synthesis.ru.RussianSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.ru.RussianRuleDisambiguator;
import de.danielnaber.languagetool.tagging.ru.RussianTagger;
//import de.danielnaber.languagetool.tokenizers.Tokenizer;
//import de.danielnaber.languagetool.tokenizers.ru.RussianWordTokenizer;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;  // new Tokenizer 
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
// import de.danielnaber.languagetool.tokenizers.ru.RussianSentenceTokenizer;  // old Tokenizer


public class Russian extends Language {

  private static final String[] COUNTRIES = {
    "RU"
  };
  
  private Tagger tagger = new RussianTagger();
  private Disambiguator disambiguator = new RussianRuleDisambiguator();
  
//  private Tokenizer wordTokenizer = new RussianWordTokenizer();
  private Synthesizer synthesizer = new RussianSynthesizer();
//  private SentenceTokenizer sentenceTokenizer = new RussianSentenceTokenizer();   // old Tokenizer
  private SentenceTokenizer sentenceTokenizer = new SRXSentenceTokenizer("ru"); // new Tokenizer 
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  public String getName() {
    return "Russian";
  }

  public String getShortName() {
    return "ru";
  }

  @Override
  public String[] getCountryVariants() {
    return COUNTRIES;
  }
  
  public Tagger getTagger() {
    return tagger;
  }

   public Disambiguator getDisambiguator() {
    return disambiguator;
  }
  
//  public Tokenizer getWordTokenizer() {
//    return wordTokenizer;
//  }

  public Synthesizer getSynthesizer() {
    return synthesizer;
  }

   public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  
  
  public Contributor[] getMaintainers() {
     return new Contributor[] {new Contributor("Yakov Reztsov")};
  }

  public Set<String> getRelevantRuleIDs() {
    Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS");
    ids.add("UPPERCASE_SENTENCE_START");
    ids.add("WORD_REPEAT_RULE");
    ids.add("WHITESPACE_RULE");    
    // specific to Russian :
    ids.add("RU_COMPOUNDS");    
    ids.add("RU_SIMPLE_REPLACE");
    return ids;
	
  }

}