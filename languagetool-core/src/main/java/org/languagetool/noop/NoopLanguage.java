/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.noop;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.chunking.Chunker;
import org.languagetool.language.Contributor;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A language that is part of languagetool-core but that hasn't any rules.
 */
public class NoopLanguage extends Language {

  private static final String SHORT_CODE = "zz";

  private Tagger tagger;
  private Chunker chunker;
  private Disambiguator disambiguator;

  @Override
  public Locale getLocale() {
    return new Locale("en");
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new NoopDisambiguator();
    }
    return disambiguator;
  }

  @Override
  public String getName() {
    return "NoopLanguage";
  }

  @Override
  public String getShortCode() {
    return SHORT_CODE;
  }

  @Override
  public String[] getCountries() {
    return new String[] {};
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new DemoTagger();
    }
    return tagger;
  }

  @Override
  public Chunker getChunker() {
    if (chunker == null) {
      chunker = new NoopChunker();
    }
    return chunker;
  }

  @Override
  public Contributor[] getMaintainers() {
    return null;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Collections.emptyList();
  }

  @Override
  protected synchronized List<AbstractPatternRule> getPatternRules() {
    return Collections.emptyList();
  }

  public SentenceTokenizer getSentenceTokenizer() {
    return new SentenceTokenizer() {
      @Override
      public List<String> tokenize(String text) {
        return Collections.singletonList(text);
      }
      @Override
      public void setSingleLineBreaksMarksParagraph(boolean lineBreakParagraphs) {}
      @Override
      public boolean singleLineBreaksMarksPara() {
        return false;
      }
    };
  }

  public Tokenizer getWordTokenizer() {
    return Collections::singletonList;
  }

}
