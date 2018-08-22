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
package org.languagetool.language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.km.KhmerSimpleReplaceRule;
import org.languagetool.rules.km.KhmerUnpairedBracketsRule;
import org.languagetool.rules.km.KhmerWordRepeatRule;
import org.languagetool.rules.km.KhmerSpaceBeforeRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.km.KhmerTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.km.KhmerWordTokenizer;

public class Khmer extends Language {

  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private Disambiguator disambiguator;

  @Override
  public String getName() {
    return "Khmer";
  }

  @Override
  public String getShortCode() {
    return "km";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"KH"};
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new KhmerTagger();
    }
    return tagger;
  }
  
  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new KhmerWordTokenizer();
    }
    return wordTokenizer;
  }
  
  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Khmer());
    }
    return disambiguator;
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Nathan Wells")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
      new HunspellRule(messages, this, userConfig),
      // specific to Khmer:
      new KhmerSimpleReplaceRule(messages),
      new KhmerWordRepeatRule(messages, this),
      new KhmerUnpairedBracketsRule(messages, this),
      new KhmerSpaceBeforeRule(messages, this)
    );
  }

}
