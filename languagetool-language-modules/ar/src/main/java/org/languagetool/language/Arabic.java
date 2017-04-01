/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.ar.ArabicCommaWhitespaceRule;
import org.languagetool.rules.ar.ArabicContractionSpellingRule;
import org.languagetool.rules.ar.ArabicDoublePunctuationRule;
import org.languagetool.rules.ar.ArabicLongSentenceRule;
import org.languagetool.rules.ar.ArabicWordRepeatRule;
import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.ArabicSentenceTokenizer;
import org.languagetool.tokenizers.ArabicWordTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Arabic.
 */
public class Arabic extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;
  private Tagger tagger;

  @Override
  public String getName() {
    return "Arabic";
  }

  @Override
  public String getShortCode() {
    return "ar";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"DZ"};
  }
  
  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer =  new ArabicSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new ArabicWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new ArabicTagger();
    }
    return tagger;
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Taha Zerrouki"),
            new Contributor("Sohaib Afifi"),
            new Contributor("Imen Kali"),
            new Contributor("Karima Tchoketch"),
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
        new MultipleWhitespaceRule(messages, this),
        new SentenceWhitespaceRule(messages),
        new GenericUnpairedBracketsRule(messages,
                Arrays.asList("[", "(", "{" , "«", "﴾"), 
                Arrays.asList("]", ")", "}" , "»", "﴿")),
        // specific to Arabic :
        new HunspellNoSuggestionRule(messages, this),
        new ArabicCommaWhitespaceRule(messages),
        new ArabicDoublePunctuationRule(messages),
        new ArabicLongSentenceRule(messages, 40),
        new ArabicWordRepeatRule(messages, this),
        new ArabicContractionSpellingRule(messages)
    );
  }

}
