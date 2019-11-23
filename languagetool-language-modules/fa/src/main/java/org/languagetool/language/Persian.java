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
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.fa.*;
import org.languagetool.tokenizers.PersianWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Persian.
 * @since 2.7
 */
public class Persian extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;

  @Override
  public String getName() {
    return "Persian";
  }

  @Override
  public String getShortCode() {
    return "fa";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"IR", "AF"};
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new PersianWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Reza1615"),
        new Contributor("Alireza Eskandarpour Shoferi"),
        new Contributor("Ebrahim Byagowi")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
        new CommaWhitespaceRule(messages),
        new DoublePunctuationRule(messages),
        new MultipleWhitespaceRule(messages, this),
        new LongSentenceRule(messages, userConfig),
        // specific to Persian:
        new PersianCommaWhitespaceRule(messages),
        new PersianDoublePunctuationRule(messages),
        new PersianWordRepeatBeginningRule(messages, this),
        new PersianWordRepeatRule(messages, this),
        new SimpleReplaceRule(messages),
        new PersianSpaceBeforeRule(messages, this),
        new WordCoherencyRule(messages)
    );
  }

}
