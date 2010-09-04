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
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.ml.MalayalamTagger;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.ml.MalayalamWordTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;

public class Malayalam extends Language {

  private final Tagger tagger = new MalayalamTagger();
  private final SentenceTokenizer sentenceTokenizer = new SRXSentenceTokenizer("en");
  private final Tokenizer wordTokenizer = new MalayalamWordTokenizer();

  private static final String[] COUNTRIES = {"IN"};
  
  public final Locale getLocale() {
    return new Locale(getShortName());
  }

  public final SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }
  
  public final String getName() {
    return "Malayalam";
  }

  public final String getShortName() {
    return "ml";
  }

  public final Tokenizer getWordTokenizer() {
    return wordTokenizer;
  }
  
  public final String[] getCountryVariants() {
    return COUNTRIES;
  }
  
  public final Tagger getTagger() {
    return tagger;
  }
    
  public final Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Jithesh.V.S")
        };
  }

  public final Set<String> getRelevantRuleIDs() {
    final Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS");
    ids.add("UPPERCASE_SENTENCE_START");
    ids.add("WORD_REPEAT_RULE");
    ids.add("WHITESPACE_RULE");    
    // specific to Malayalam...:
    return ids;
  }

}
