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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.uk.HiddenCharacterRule;
import org.languagetool.rules.uk.MissingHyphenRule;
import org.languagetool.rules.uk.MixedAlphabetsRule;
import org.languagetool.rules.uk.MorfologikUkrainianSpellerRule;
import org.languagetool.rules.uk.SimpleReplaceRule;
import org.languagetool.rules.uk.SimpleReplaceSoftRule;
import org.languagetool.rules.uk.SimpleReplaceSpelling2019Rule;
import org.languagetool.rules.uk.SimpleReplaceSpelling1992Rule;
import org.languagetool.rules.uk.SimpleReplaceRenamedRule;
import org.languagetool.rules.uk.TokenAgreementPrepNounRule;
import org.languagetool.rules.uk.TokenAgreementAdjNounRule;
import org.languagetool.rules.uk.TokenAgreementNounVerbRule;
import org.languagetool.rules.uk.UkrainianWordRepeatRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.uk.UkrainianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.uk.UkrainianHybridDisambiguator;
import org.languagetool.tagging.uk.UkrainianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;

public class Ukrainian extends Language {
  private static final List<String> RULE_FILES = Arrays.asList(
      "grammar-spelling.xml",
      "grammar-grammar.xml",
      "grammar-barbarism.xml",
      "grammar-style.xml",
      "grammar-punctuation.xml"
      );

  public static final Ukrainian DEFAULT_VARIANT = new Ukrainian();
  private Tagger tagger;
  private SRXSentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;


  public Ukrainian() {
  }

  @Override
  public Pattern getIgnoredCharactersRegex() {
    return Pattern.compile("[\u00AD\u0301]");
  }

  @Override
  public Locale getLocale() {
    return new Locale(getShortCode());
  }

  @Override
  public String getName() {
    return "Ukrainian";
  }

  @Override
  public String getShortCode() {
    return "uk";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"UA"};
  }

//  @Override
//  public String getVariant() {
//    return "2019";
//  }
//
//  @Override
//  public Language getDefaultLanguageVariant() {
//    return DEFAULT_VARIANT;
//  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new UkrainianTagger();
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new UkrainianSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new UkrainianHybridDisambiguator();
    }
    return disambiguator;
  }

  @Override
  public Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new UkrainianWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public SRXSentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Andriy Rysin"),
        new Contributor("Maksym Davydov")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    MorfologikUkrainianSpellerRule morfologikSpellerRule = new MorfologikUkrainianSpellerRule(messages, this, userConfig, altLanguages);

    return Arrays.asList(
        new CommaWhitespaceRule(messages,
            Example.wrong("Ми обідали борщем<marker> ,</marker> пловом і салатом."),
            Example.fixed("Ми обідали борщем<marker>,</marker> пловом і салатом")),

        // TODO: does not handle dot in abbreviations in the middle of the sentence, and also !.., ?..
        //            new UppercaseSentenceStartRule(messages),
        new MultipleWhitespaceRule(messages, this),
        new UkrainianWordRepeatRule(messages, this),

        // TODO: does not handle !.. and ?..
        //            new DoublePunctuationRule(messages),
        morfologikSpellerRule,

        new MissingHyphenRule(messages, ((UkrainianTagger)getTagger()).getWordTagger()),

        new TokenAgreementNounVerbRule(messages),
        new TokenAgreementAdjNounRule(messages),
        new TokenAgreementPrepNounRule(messages),

        new MixedAlphabetsRule(messages),

        new SimpleReplaceSoftRule(messages),
        new SimpleReplaceRenamedRule(messages),
        getSpellingReplacementRule(messages),
        new SimpleReplaceRule(messages, morfologikSpellerRule),

        new HiddenCharacterRule(messages)
    );
  }

  protected Rule getSpellingReplacementRule(ResourceBundle messages) throws IOException {
    return new SimpleReplaceSpelling1992Rule(messages);
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String dirBase = dataBroker.getRulesDir() + "/" + getShortCode() + "/";
    for (String ruleFile : RULE_FILES) {
      ruleFileNames.add(dirBase + ruleFile);
    }
    return ruleFileNames;
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    return Arrays.asList("piv_before_iotized_1992", "piv_with_proper_noun_1992");
  }

}
