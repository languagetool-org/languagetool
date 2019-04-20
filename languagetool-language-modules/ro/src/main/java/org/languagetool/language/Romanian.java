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
import org.languagetool.rules.*;
import org.languagetool.rules.ro.CompoundRule;
import org.languagetool.rules.ro.MorfologikRomanianSpellerRule;
import org.languagetool.rules.ro.RomanianWordRepeatBeginningRule;
import org.languagetool.rules.ro.SimpleReplaceRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ro.RomanianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.ro.RomanianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ro.RomanianWordTokenizer;

/**
 *
 * @author Ionuț Păduraru
 * @since 24.02.2009 22:18:21
 */
public class Romanian extends Language {

  private Tagger tagger;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
  private Tokenizer wordTokenizer;
  private SentenceTokenizer sentenceTokenizer;

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

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new RomanianTagger();
    }
    return tagger;
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
            new CompoundRule(messages)
    );
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new RomanianSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Romanian());
    }
    return disambiguator;
  }

  @Override
  public Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new RomanianWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }
}
