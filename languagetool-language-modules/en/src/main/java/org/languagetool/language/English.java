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

import com.google.common.cache.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.EnglishChunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.en.*;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRuleCreator;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.en.EnglishHybridDisambiguator;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Support for English - use the sub classes {@link BritishEnglish}, {@link AmericanEnglish},
 * etc. if you need spell checking.
 * Make sure to call {@link #close()} after using this (currently only relevant if you make
 * use of {@link EnglishConfusionProbabilityRule}).
 */
public class English extends Language implements AutoCloseable {

  private static final LoadingCache<String, List<Rule>> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(30, TimeUnit.MINUTES)
      .build(new CacheLoader<String, List<Rule>>() {
        @Override
        public List<Rule> load(@NotNull String path) throws IOException {
          List<Rule> rules = new ArrayList<>();
          PatternRuleLoader loader = new PatternRuleLoader();
          try (InputStream is = JLanguageTool.getDataBroker().getAsStream(path)) {
            rules.addAll(loader.getRules(is, path));
          }
          return rules;
        }
      });
  private static final Language AMERICAN_ENGLISH = new AmericanEnglish();

  private LanguageModel languageModel;

  /**
   * @deprecated use {@link AmericanEnglish} or {@link BritishEnglish} etc. instead -
   *  they have rules for spell checking, this class doesn't (deprecated since 3.2)
   */
  @Deprecated
  public English() {
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return AMERICAN_ENGLISH;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public String getName() {
    return "English";
  }

  @Override
  public String getShortCode() {
    return "en";
  }

  @Override
  public String[] getCountries() {
    return new String[]{};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new EnglishTagger();
  }

  @Nullable
  @Override
  public Chunker createDefaultChunker() {
    return new EnglishChunker();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new EnglishSynthesizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new EnglishHybridDisambiguator();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new EnglishWordTokenizer();
  }

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  @Override
  public synchronized Word2VecModel getWord2VecModel(File indexDir) throws IOException {
    return new Word2VecModel(indexDir + File.separator + getShortCode());
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Mike Unwalla"), Contributors.MARCIN_MILKOWSKI, Contributors.DANIEL_NABER };
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> allRules = new ArrayList<>();
    if (motherTongue != null) {
      if ("de".equals(motherTongue.getShortCode())) {
        allRules.addAll(cache.getUnchecked("/org/languagetool/rules/en/grammar-l2-de.xml"));
      } else if ("fr".equals(motherTongue.getShortCode())) {
        allRules.addAll(cache.getUnchecked("/org/languagetool/rules/en/grammar-l2-fr.xml"));
      }
    }
    allRules.addAll(Arrays.asList(
        new CommaWhitespaceRule(messages,
                Example.wrong("We had coffee<marker> ,</marker> cheese and crackers and grapes."),
                Example.fixed("We had coffee<marker>,</marker> cheese and crackers and grapes.")),
        new DoublePunctuationRule(messages),
        new UppercaseSentenceStartRule(messages, this,
                Example.wrong("This house is old. <marker>it</marker> was built in 1950."),
                Example.fixed("This house is old. <marker>It</marker> was built in 1950.")),
        new MultipleWhitespaceRule(messages, this),
        new SentenceWhitespaceRule(messages),
        new WhiteSpaceBeforeParagraphEnd(messages, this),
        new WhiteSpaceAtBeginOfParagraph(messages),
        new EmptyLineRule(messages, this),
        new LongSentenceRule(messages, userConfig),
        new LongParagraphRule(messages, this, userConfig),
        new ParagraphRepeatBeginningRule(messages, this),
        new PunctuationMarkAtParagraphEnd(messages, this),
        new PunctuationMarkAtParagraphEnd2(messages, this),
        // specific to English:
        new SpecificCaseRule(messages),
        new EnglishUnpairedBracketsRule(messages, this),
        new EnglishWordRepeatRule(messages, this),
        new AvsAnRule(messages),
        new EnglishWordRepeatBeginningRule(messages, this),
        new CompoundRule(messages),
        new ContractionSpellingRule(messages),
        new EnglishWrongWordInContextRule(messages),
        new EnglishDashRule(messages),
        new WordCoherencyRule(messages),
        new EnglishDiacriticsRule(messages),
        new EnglishPlainEnglishRule(messages),
        new EnglishRedundancyRule(messages),
        new SimpleReplaceRule(messages),
        new ReadabilityRule(messages, this, userConfig, false),
        new ReadabilityRule(messages, this, userConfig, true)
    ));
    return allRules;
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Arrays.asList(
        new UpperCaseNgramRule(messages, languageModel, this, userConfig),
        new EnglishConfusionProbabilityRule(messages, languageModel, this),
        new EnglishNgramProbabilityRule(messages, languageModel, this)
    );
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    if (languageModel != null && motherTongue != null && "fr".equals(motherTongue.getShortCode())) {
      return Arrays.asList(
          new EnglishForFrenchFalseFriendRule(messages, languageModel, motherTongue, this)
      );
    }
    if (languageModel != null && motherTongue != null && "de".equals(motherTongue.getShortCode())) {
      return Arrays.asList(
          new EnglishForGermansFalseFriendRule(messages, languageModel, motherTongue, this)
      );
    }
    return Arrays.asList();
  }

  @Override
  public List<Rule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws IOException {
    return NeuralNetworkRuleCreator.createRules(messages, this, word2vecModel);
  }

  @Override
  public boolean hasNGramFalseFriendRule(Language motherTongue) {
    return motherTongue != null && ("de".equals(motherTongue.getShortCode()) || "fr".equals(motherTongue.getShortCode()));
  }

  /**
   * Closes the language model, if any. 
   * @since 2.7 
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "THE_INS_RULE": return 50; // higher priority for testing/evaluation; only activated by configuring remote rule
      case "CONFPAIRS_EN_GPT2": return 50; // higher priority for testing/evaluation; only activated by configuring remote rule
      case "CONFPAIRS_EN_GPT2_L": return 50; // higher priority for testing/evaluation; only activated by configuring remote rule
      case "CONFPAIRS_EN_GPT2_XL": return 50; // higher priority for testing/evaluation; only activated by configuring remote rule
      case "I_E":                       return 10; // needs higher prio than EN_COMPOUNDS ("i.e learning")
      case "MISSING_HYPHEN":            return 5;
      case "TRANSLATION_RULE":          return 5;   // Premium
      case "WRONG_APOSTROPHE":          return 5;
      case "DOS_AND_DONTS":             return 3;
      case "EN_COMPOUNDS":              return 2;
      case "ABBREVIATION_PUNCTUATION":  return 2;
      case "COVID_19":                  return 1;
      case "DON_T_AREN_T":              return 1;   // higher prio than DID_BASEFORM
      case "MONEY_BACK_HYPHEN":         return 1;   // higher prio than A_UNCOUNTABLE
      case "WORLDS_BEST":               return 1;   // higher prio than THE_SUPERLATIVE
      case "STEP_COMPOUNDS":            return 1;   // higher prio than STARS_AND_STEPS
      case "WON_T_TO":                  return 1;   // higher prio than DON_T_AREN_T
      case "WAN_T":                     return 1;   // higher prio than DON_T_AREN_T
      case "THE_US":                    return 1;   // higher prio than DT_PRP
      case "THE_IT":                    return 1;   // higher prio than DT_PRP
      case "A_HUNDREDS":                return 1;   // higher prio than A_NNS
      case "NOW_A_DAYS":                return 1;   // higher prio than A_NNS
      case "COUPLE_OF_TIMES":           return 1;   // higher prio than A_NNS
      case "A_WINDOWS":                 return 1;   // higher prio than A_NNS
      case "A_SCISSOR":                 return 1;   // higher prio than A_NNS
      case "A_SNICKERS":                return 1;   // higher prio than A_NNS
      case "ROUND_A_BOUT":              return 1;   // higher prio than A_NNS
      case "SEEM_SEEN":                 return 1;   // higher prio than HAVE_PART_AGREEMENT and PRP_HAVE_VB
      case "BORN_IN":                   return 1;   // higher prio than PRP_PAST_PART
      case "DO_TO":                     return 1;   // higher prio than HAVE_PART_AGREEMENT
      case "IN_THIS_REGARDS":           return 1;   // higher prio than THIS_NNS
      case "NO_WHERE":                  return 1;   // higher prio than NOW
      case "APOSTROPHE_VS_QUOTE":       return 1;   // higher prio than EN_QUOTES
      case "COMMA_PERIOD":              return 1;   // higher prio than COMMA_PARENTHESIS_WHITESPACE
      case "HERE_HEAR":                 return 1;   // higher prio than ENGLISH_WORD_REPEAT_RULE
      case "LIGATURES":                 return 1;   // prefer over spell checker
      case "APPSTORE":                  return 1;   // prefer over spell checker
      case "INCORRECT_CONTRACTIONS":    return 1;   // prefer over EN_CONTRACTION_SPELLING
      case "DONT_T":                    return 1;   // prefer over EN_CONTRACTION_SPELLING
      case "WHATS_APP":                 return 1;   // prefer over EN_CONTRACTION_SPELLING
      case "NON_STANDARD_COMMA":        return 1;   // prefer over spell checker
      case "NON_STANDARD_ALPHABETIC_CHARACTERS":        return 1;   // prefer over spell checker
      case "WONT_CONTRACTION":          return 1;   // prefer over WONT_WANT
      case "THAN_THANK":                return 1;   // prefer over THAN_THEN
      case "CD_NN_APOSTROPHE_S":        return 1;   // prefer over CD_NN and LOWERCASE_NAME_APOSTROPHE_S
      case "IT_IF":                     return 1;   // needs higher prio than PRP_COMMA
      case "PROFANITY":                 return 5;   // prefer over spell checker
      case "RUDE_SARCASTIC":            return 6;   // prefer over spell checker
      case "CHILDISH_LANGUAGE":         return 8;   // prefer over spell checker
      case "EN_DIACRITICS_REPLACE":     return 9;   // prefer over spell checker (like PHRASE_REPETITION)
      case "BLACK_SEA":                 return -1;  // less priority than SEA_COMPOUNDS
      case "WE_BE":                     return -1;
      case "A_LOT_OF_NN":               return -1;
      case "IT_VBZ":                    return -1;
      case "IT_IS_2":                   return -1;
      case "A_RB_NN":                   return -1;  // prefer other more specific rules (e.g. QUIET_QUITE)
      case "PLURAL_VERB_AFTER_THIS":    return -1;  // prefer other more specific rules (e.g. COMMA_TAG_QUESTION)
      case "BE_RB_BE":                  return -1;  // prefer other more specific rules
      case "IT_ITS":                    return -1;  // prefer other more specific rules
      case "ENGLISH_WORD_REPEAT_RULE":  return -1;  // prefer other more specific rules (e.g. IT_IT)
      case "PRP_MD_NN":                 return -1;  // prefer other more specific rules (e.g. MD_ABLE, WONT_WANT)
      case "NON_ANTI_PRE_JJ":           return -1;  // prefer other more specific rules
      case "DT_JJ_NO_NOUN":             return -1;  // prefer other more specific rules (e.g. THIRD_PARTY)
      case "AGREEMENT_SENT_START":      return -1;  // prefer other more specific rules
      case "HAVE_PART_AGREEMENT":       return -1;  // prefer other more specific rules
      case "PREPOSITION_VERB":          return -1;  // prefer other more specific rules
      case "EN_A_VS_AN":                return -1;  // prefer other more specific rules (with suggestions, e.g. AN_ALSO)
      case "CD_NN":                     return -1;  // prefer other more specific rules (with suggestions)
      case "ATD_VERBS_TO_COLLOCATION":  return -1;  // prefer other more specific rules (with suggestions)
      case "ADVERB_OR_HYPHENATED_ADJECTIVE": return -1; // prefer other more specific rules (with suggestions)
      case "GOING_TO_VBD":              return -1;  // prefer other more specific rules (with suggestions, e.g. GOING_TO_JJ)
      case "MISSING_PREPOSITION":       return -1;  // prefer other more specific rules (with suggestions)
      case "BE_TO_VBG":                 return -1;  // prefer other more specific rules (with suggestions)
      case "NON3PRS_VERB":              return -1;  // prefer other more specific rules (with suggestions)
      case "DID_FOUND_AMBIGUOUS":       return -1;  // prefer other more specific rules (e.g. TWO_CONNECTED_MODAL_VERBS)
      case "BE_I_BE_GERUND":            return -1;  // prefer other more specific rules (with suggestions)
      case "VBZ_VBD":                   return -1;  // prefer other more specific rules (e.g. IS_WAS)
      case "SUPERLATIVE_THAN":          return -1;  // prefer other more specific rules
      case "UNLIKELY_OPENING_PUNCTUATION": return -1; // prefer other more specific rules
      case "PRP_RB_NO_VB":              return -2;  // prefer other more specific rules (with suggestions)
      case "PRP_VBG":                   return -2;  // prefer other more specific rules (with suggestions, prefer over HE_VERB_AGR)
      case "PRP_VBZ":                   return -2;  // prefer other more specific rules (with suggestions)
      case "PRP_VB":                    return -2;  // prefer other more specific rules (with suggestions)
      case "BEEN_PART_AGREEMENT":       return -3;  // prefer other more specific rules (e.g. VARY_VERY, VB_NN)
      case "A_INFINITIVE":              return -3;  // prefer other more specific rules (with suggestions, e.g. PREPOSITION_VERB)
      case "HE_VERB_AGR":               return -3;  // prefer other more specific rules (e.g. PRP_VBG)
      case "PRP_JJ":                    return -3;  // prefer other rules (e.g. PRP_VBG, IT_IT and ADJECTIVE_ADVERB, PRP_ABLE, PRP_NEW, MD_IT_JJ)
      case "PRONOUN_NOUN":              return -3;  // prefer other rules (e.g. PRP_VB, PRP_JJ)
      case "INDIAN_ENGLISH":            return -3;  // prefer grammar rules, but higher prio than spell checker
      case "MORFOLOGIK_RULE_EN_US":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_GB":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_CA":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_ZA":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_NZ":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_AU":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "TWO_CONNECTED_MODAL_VERBS": return -15;
      case "CONFUSION_RULE":            return -20;
      case "SENTENCE_FRAGMENT":         return -50; // prefer other more important sentence start corrections.
      case "SENTENCE_FRAGMENT_SINGLE_WORDS": return -51;  // prefer other more important sentence start corrections.
      case "EN_REDUNDANCY_REPLACE":     return -510;  // style rules should always have the lowest priority.
      case "EN_PLAIN_ENGLISH_REPLACE":  return -511;  // style rules should always have the lowest priority.
      case LongSentenceRule.RULE_ID:    return -997;
      case LongParagraphRule.RULE_ID:   return -998;
    }
    return super.getPriorityForId(id);
  }

  @Override
  public Function<Rule, Rule> getRemoteEnhancedRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    Function<Rule, Rule> fallback = super.getRemoteEnhancedRules(messageBundle, configs, userConfig, motherTongue, altLanguages);
    RemoteRuleConfig bert = RemoteRuleConfig.getRelevantConfig(BERTSuggestionRanking.RULE_ID, configs);

    return original -> {
      if (original.isDictionaryBasedSpellingRule() && original.getId().startsWith("MORFOLOGIK_RULE_EN")) {
        if (UserConfig.hasABTestsEnabled() && bert != null) {
          return new BERTSuggestionRanking(original, bert, userConfig);
        }
      }
      return fallback.apply(original);
    };
  }

  @Override
  public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRemoteRules(
      messageBundle, configs, globalConfig, userConfig, motherTongue, altLanguages));
    String theInsertionID = "THE_INS_RULE";
    RemoteRuleConfig theInsertionConfig = RemoteRuleConfig.getRelevantConfig(theInsertionID, configs);
    if (theInsertionConfig != null) {
      Map<String, String> theInsertionMessages = new HashMap<>();
      theInsertionMessages.put("THE_INS", "the_ins_rule_del_the");
      theInsertionMessages.put("INS_THE", "the_ins_rule_ins_the");
      Rule theInsertionRule = GRPCRule.create(messageBundle,
        theInsertionConfig,
        theInsertionID, "the_ins_rule_description", theInsertionMessages);
      rules.add(theInsertionRule);
    }
    String missingTheID = "MISSING_THE";
    RemoteRuleConfig missingTheConfig = RemoteRuleConfig.getRelevantConfig(missingTheID, configs);
    if (missingTheConfig != null) {
      Map<String, String> missingTheMessages = new HashMap<>();
      missingTheMessages.put("MISSING_THE", "the_ins_rule_ins_the");
      Rule missingTheRule = GRPCRule.create(messageBundle,
        missingTheConfig,
        missingTheID, "the_ins_rule_description", missingTheMessages);
      rules.add(missingTheRule);
    }
    List<String> confpairRules = Arrays.asList("CONFPAIRS_EN_GPT2", "CONFPAIRS_EN_GPT2_L", "CONFPAIRS_EN_GPT2_XL");
    for (String confpairID : confpairRules) {
      RemoteRuleConfig confpairConfig = RemoteRuleConfig.getRelevantConfig(confpairID, configs);
      if (confpairConfig != null) {
        Rule confpairRule = new GRPCConfusionRule(messageBundle, confpairConfig);
        rules.add(confpairRule);
      }
    }
    return rules;
  }
}
