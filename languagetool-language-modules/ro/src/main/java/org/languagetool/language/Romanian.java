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
import org.languagetool.rules.ro.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ro.RomanianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.ro.RomanianTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.ro.RomanianWordTokenizer;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author Ionuț Păduraru
 * @since 24.02.2009 22:18:21
 */
public class Romanian extends Language {

  @Override
  public String getName() {
    return "Romanian";
  }

  @Override
  public String getShortCode() {
    return "ro";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"RO"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new RomanianTagger();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Ionuț Păduraru", "http://www.archeus.ro")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "„", "«", "»"),
                    Arrays.asList("]", ")", "}", "”", "»", "«")),
            new WordRepeatRule(messages, this),
            // specific to Romanian:
            new MorfologikRomanianSpellerRule(messages, this, userConfig, altLanguages),
            new RomanianWordRepeatBeginningRule(messages, this),
            new SimpleReplaceRule(messages),
            new CompoundRule(messages, this, userConfig)
    );
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new RomanianSynthesizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new RomanianWordTokenizer();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }
}
