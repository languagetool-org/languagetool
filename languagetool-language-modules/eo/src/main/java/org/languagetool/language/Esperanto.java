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
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.eo.EsperantoTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.eo.EsperantoWordTokenizer;

import java.io.IOException;
import java.util.*;

public class Esperanto extends Language {

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new EsperantoWordTokenizer();
  }

  @Override
  public String getName() {
    return "Esperanto";
  }
  
  @Override
  public String getShortCode() {
    return "eo";
  }

  @Override
  public String[] getCountries() {
    return new String[]{};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new EsperantoTagger();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(new Esperanto());
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { Contributors.DOMINIQUE_PELLE };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages),
            new HunspellRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this),
            new WordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages)
    );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new HunspellRule(messages, this, null,null);
  }
}
