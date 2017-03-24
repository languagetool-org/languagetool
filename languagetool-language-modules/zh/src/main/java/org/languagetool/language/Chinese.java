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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.zh.ChineseConfusionProbabilityRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.zh.ChineseTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.ChineseSentenceTokenizer;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;

public class Chinese extends Language implements AutoCloseable {

  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private LuceneLanguageModel languageModel;

  @Override
  public String getShortCode() {
    return "zh";
  }

  @Override
  public String getName() {
    return "Chinese";
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
  public List<Rule> getRelevantRules(ResourceBundle messages) {
    return Arrays.asList(
            new DoublePunctuationRule(messages),
            new MultipleWhitespaceRule(messages, this)
    );
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new ChineseTagger();
    }
    return tagger;
  }

  @Override
  public Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new ChineseWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new ChineseSentenceTokenizer();
    }
    return sentenceTokenizer;
  }

  /** @since 3.1 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    if (languageModel == null) {
      languageModel = new LuceneLanguageModel(new File(indexDir, getShortCode()));
    }
    return languageModel;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.<Rule>asList(
            new ChineseConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /**
   * Closes the language model, if any. 
   * @since 3.1
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

}
