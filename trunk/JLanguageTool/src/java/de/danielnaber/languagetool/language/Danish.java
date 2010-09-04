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
import de.danielnaber.languagetool.tagging.da.DanishTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.da.DanishSentenceTokenizer;

public class Danish extends Language {

  private final Tagger tagger = new DanishTagger();
  private final SentenceTokenizer sentenceTokenizer = new DanishSentenceTokenizer();

  private static final String[] COUNTRIES = {"DK"};
  
  public final Locale getLocale() {
    return new Locale(getShortName());
  }

  public final String getName() {
    return "Danish";
  }

  public final String getShortName() {
    return "da";
  }

  public final String[] getCountryVariants() {
    return COUNTRIES;
  }
  
  public final Tagger getTagger() {
    return tagger;
  }

  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  public final Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Esben Aaberg")};
  }

  public final Set<String> getRelevantRuleIDs() {
    final Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS"); // correction for genitive apostrophes eg. "Lis' hund" made in UnpairedQuotesBracketsRule
    ids.add("UPPERCASE_SENTENCE_START"); // abbreviation exceptions, done in DanishSentenceTokenizer 
    // "WORD_REPEAT_RULE" implemented in grammar.xml
    ids.add("WHITESPACE_RULE");    
    // specific to Danish:
    return ids;
  }

}
