/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 LanguageTool contributors
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
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WordRepeatRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Azerbaijani (Latin script).
 */
public class Azerbaijani extends Language {

  @Override
  public String getName() {
    return "Azerbaijani";
  }

  @Override
  public String getShortCode() {
    return "az";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"AZ"};
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Umud Hasanli") };
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new DemoTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig,
                                     Language motherTongue, List<Language> altLanguages) {
    return Arrays.asList(
        new CommaWhitespaceRule(messages),
        new DoublePunctuationRule(messages),
        new GenericUnpairedBracketsRule(messages),
        new UppercaseSentenceStartRule(messages, this),
        new WordRepeatRule(messages, this),
        new MultipleWhitespaceRule(messages, this)
    );
  }
}
