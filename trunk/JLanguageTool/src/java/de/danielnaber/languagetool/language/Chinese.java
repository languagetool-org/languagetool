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
package de.danielnaber.languagetool.language;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.DoublePunctuationRule;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.WhitespaceRule;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.zh.ChineseTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.zh.ChineseSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.zh.ChineseWordTokenizer;

public class Chinese extends Language {

  private final Tagger tagger = new ChineseTagger();

  private final Tokenizer wordTokenizer = new ChineseWordTokenizer();

  private final SentenceTokenizer sentenceTokenizer = new ChineseSentenceTokenizer();

  @Override
  public String getShortName() {
    return "zh";
  }

  @Override
  public String getName() {
    return "Chinese";
  }

  @Override
  public String[] getCountryVariants() {
    return new String[] { "CN" };
  }

  @Override
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Tao Lin") };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(DoublePunctuationRule.class, WhitespaceRule.class);
  }

  @Override
  public final Tagger getTagger() {
    return tagger;
  }

  @Override
  public final Tokenizer getWordTokenizer() {
    return wordTokenizer;
  }

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

}