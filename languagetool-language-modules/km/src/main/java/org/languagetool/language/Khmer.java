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

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.km.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.km.KhmerTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.km.KhmerWordTokenizer;

import java.io.IOException;
import java.util.*;

public class Khmer extends Language {

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
  public String getCommonWordsPath() {
    // TODO: provide common words file
    return null;
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new KhmerTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new KhmerWordTokenizer();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(this);
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Nathan Wells")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
      new KhmerHunspellRule(messages, userConfig),
      // specific to Khmer:
      new KhmerSimpleReplaceRule(messages),
      new KhmerWordRepeatRule(messages, this),
      new KhmerUnpairedBracketsRule(messages, this),
      new KhmerSpaceBeforeRule(messages, this)
    );
  }

}
