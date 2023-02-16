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
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.uk.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.uk.UkrainianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.uk.UkrainianHybridDisambiguator;
import org.languagetool.tagging.uk.UkrainianTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Ukrainian extends Language {
  public static final Pattern IGNORED_CHARS = Pattern.compile("[\u00AD\u0301]");

  private static final List<String> RULE_FILES = Arrays.asList(
      "grammar-spelling.xml",
      "grammar-grammar.xml",
      "grammar-barbarism.xml",
      "grammar-style.xml",
      "grammar-punctuation.xml"
      );

  public static final Ukrainian DEFAULT_VARIANT = new Ukrainian();

  public Ukrainian() {
  }

  @Override
  public Pattern getIgnoredCharactersRegex() {
    return IGNORED_CHARS;
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


  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new UkrainianTagger();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return UkrainianSynthesizer.INSTANCE;
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new UkrainianHybridDisambiguator();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new UkrainianWordTokenizer();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Andriy Rysin"),
        new Contributor("Maksym Davydov")
    };
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikUkrainianSpellerRule(messages, this, null, null);
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    MorfologikUkrainianSpellerRule morfologikSpellerRule = new MorfologikUkrainianSpellerRule(messages, this, userConfig, altLanguages);

    return Arrays.asList(
        // lower priority rules

        new UkrainianCommaWhitespaceRule(messages,
            Example.wrong("Ми обідали борщем<marker> ,</marker> пловом і салатом,— все смачне."),
            Example.fixed("Ми обідали борщем<marker>,</marker> пловом і салатом,— все смачне")),

        // TODO: does not handle dot in abbreviations in the middle of the sentence, and also !.., ?..
        new UkrainianUppercaseSentenceStartRule(messages, this,
            Example.wrong("<marker>речення</marker> має починатися з великої."),
            Example.fixed("<marker>Речення</marker> має починатися з великої")),

        new MultipleWhitespaceRule(messages, this),
        new UkrainianWordRepeatRule(messages, this),

        new TypographyRule(messages),
        new HiddenCharacterRule(messages),


        // medium priority rules
        
        // TODO: does not handle !.. and ?..
        //            new DoublePunctuationRule(messages),
        morfologikSpellerRule,

        // high priority rules

        new MissingHyphenRule(messages, ((UkrainianTagger)getTagger()).getWordTagger()),

        new TokenAgreementVerbNounRule(messages),
        new TokenAgreementNounVerbRule(messages),
        new TokenAgreementAdjNounRule(messages, this),
        new TokenAgreementPrepNounRule(messages, this),
        new TokenAgreementNumrNounRule(messages, this),

        new MixedAlphabetsRule(messages),

        new SimpleReplaceSoftRule(messages),
        new SimpleReplaceRenamedRule(messages),
        getSpellingReplacementRule(messages),
        new SimpleReplaceRule(messages, morfologikSpellerRule)
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
  
  /** @since 5.1 */
  @Override
  public String getOpeningDoubleQuote() {
    return "«";
  }

  /** @since 5.1 */
  @Override
  public String getClosingDoubleQuote() {
    return "»";
  }
  
  /** @since 5.1 */
  @Override
  public String getOpeningSingleQuote() {
    return "‘";
  }

  /** @since 5.1 */
  @Override
  public String getClosingSingleQuote() {
    return "’";
  }
  
  /** @since 5.1 */
  @Override
  public boolean isAdvancedTypographyEnabled() {
    return false;  //DISABLED!
  }

}
