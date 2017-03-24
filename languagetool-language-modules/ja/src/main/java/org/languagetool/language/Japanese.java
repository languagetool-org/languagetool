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
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ja.JapaneseTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ja.JapaneseWordTokenizer;

public class Japanese extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;

  @Override
  public String getShortCode() {
    return "ja";
  }

  @Override
  public String getName() {
    return "Japanese";
  }

  @Override
  public String[] getCountries() {
    return new String[] { "JP" };
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Takahiro Shinkai")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) {
    return Arrays.asList(
            new DoublePunctuationRule(messages),
            new MultipleWhitespaceRule(messages, this)
    );
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new JapaneseTagger();
    }
    return tagger;
  }

  @Override
  public Tokenizer getWordTokenizer() {
    return new JapaneseWordTokenizer();
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

}
