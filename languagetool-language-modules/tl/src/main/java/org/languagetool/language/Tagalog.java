/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.language.tl.MorfologikTagalogSpellerRule;
import org.languagetool.language.tokenizers.TagalogWordTokenizer;
import org.languagetool.rules.*;
import org.languagetool.rules.tl.PangVowelWordRule;
import org.languagetool.rules.tl.ReduplicationRule;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.tl.TagalogTagger;
import org.languagetool.tokenizers.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Nathaniel Oco
 */
public class Tagalog extends Language {

  @Override
  public String getName() {
    return "Tagalog";
  }

  @Override
  public String getShortCode() {
    return "tl";
  }

  @Override
  public String[] getCountries() {
    return new String[] {"PH"};
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new TagalogWordTokenizer();
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new TagalogTagger();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Luis Miguel Robles"),
            new Contributor("Anton Luis Vale"),
            new Contributor("Ian Gabriel De Jesus"),
            new Contributor("Nathaniel Oco"),
            new Contributor("Allan Borra"),
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new PangVowelWordRule(messages, this),
            new MorfologikTagalogSpellerRule(messages, this, userConfig, altLanguages),
            new ReduplicationRule(messages, this)
    );
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikTagalogSpellerRule(messages, this, null, null);
  }
}
