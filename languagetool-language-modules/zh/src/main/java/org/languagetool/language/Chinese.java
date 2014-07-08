/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Arrays;
import java.util.List;

import org.languagetool.Language;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.zh.ChineseTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.ChineseSentenceTokenizer;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;

public class Chinese extends Language {

  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private String name = "Chinese";

  @Override
  public String getShortName() {
    return "zh";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String[] getCountries() {
    return new String[] { "CN" };
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Tao Lin") };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            DoublePunctuationRule.class,
            MultipleWhitespaceRule.class
    );
  }

  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new ChineseTagger();
    }
    return tagger;
  }

  @Override
  public final Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new ChineseWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new ChineseSentenceTokenizer();
    }
    return sentenceTokenizer;
  }

}
