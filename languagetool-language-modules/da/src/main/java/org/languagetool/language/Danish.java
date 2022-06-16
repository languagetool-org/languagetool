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
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.da.DanishTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.io.IOException;
import java.util.*;

/**
 * @deprecated this language is unmaintained in LT and might be removed in a future release if we cannot find contributors for it (deprecated since 3.6)
 */
@Deprecated
public class Danish extends Language {

  @Override
  public String getName() {
    return "Danish";
  }

  @Override
  public String getShortCode() {
    return "da";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"DK"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new DanishTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Esben Aaberg"), new Contributor("Henrik Bendt") };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "\"", "”"),
                    Arrays.asList("]", ")", "}", "\"", "”")),
            new HunspellRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this),  // abbreviation exceptions, done in DanishSentenceTokenizer
            // "WORD_REPEAT_RULE" implemented in grammar.xml
            new MultipleWhitespaceRule(messages, this)
    );
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new HunspellRule(messages, this, null,null);
  }
}
