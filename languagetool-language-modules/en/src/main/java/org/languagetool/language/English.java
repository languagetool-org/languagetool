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
        new LongSentenceRule(messages, userConfig, 33, true, true),
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
        new SimpleReplaceRule(messages, this),
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
  
  /** @since 5.1 */
  @Override
  public String getOpeningDoubleQuote() {
    return "“";
  }

  /** @since 5.1 */
  @Override
  public String getClosingDoubleQuote() {
    return "”";
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
    return true;
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
      case "I_E":                       return 10; // needs higher prio than EN_COMPOUNDS ("i.e learning")
      case "YEAR_OLD_HYPHEN":           return 6;  // higher prio than MISSING_HYPHEN
      case "MISSING_HYPHEN":            return 5;
      case "TRANSLATION_RULE":          return 5;   // Premium
      case "WRONG_APOSTROPHE":          return 5;
      case "DOS_AND_DONTS":             return 3;
      case "EN_COMPOUNDS":              return 2;
      case "ABBREVIATION_PUNCTUATION":  return 2;
      case "FEDEX":                     return 2;   // higher prio than many verb rules (e.g. MD_BASEFORM)
      case "COVID_19":                  return 1;
      case "OTHER_WISE_COMPOUND":       return 1;
      case "ON_EXCEL":                  return 1;
      case "LUV":                       return 1;   // higher prio than spell checker
      case "DAT":                       return 1;   // higher prio than spell checker
      case "MAC_OS":                    return 1;   // higher prio than spell checker
      case "BESTEST":                   return 1;   // higher prio than spell checker
      case "OFF_OF":                    return 1;   // higher prio than ADJECTIVE_ADVERB
      case "SHELL_COMPOUNDS":           return 1;   // higher prio than HELL
      case "HANDS_ON_HYPHEN":           return 1;   // higher prio than A_NNS
      case "QUIET_QUITE":               return 1;   // higher prio than A_QUITE_WHILE
      case "A_OK":                      return 1;   // prefer over A_AN
      case "I_A":                       return 1;   // higher prio than I_IF
      case "GOT_GO":                    return 1;   // higher prio than MD_BASEFORM
      case "UPPERCASE_SENTENCE_START":  return 1;   // higher prio than AI_MISSING_THE_*
      case "THERE_FORE":                return 1;   // higher prio than FORE_FOR
      case "PRP_NO_VB":                 return 1;   // higher prio than I_IF
      case "FOLLOW_UP":                 return 1;   // higher prio than MANY_NN
      case "IT_SOMETHING":              return 1;   // higher prio than IF_YOU_ANY and IT_THE_PRP
      case "NO_KNOW":                   return 1;   // higher prio than DOUBLE_NEGATIVE
      case "WILL_BASED_ON":             return 1;   // higher prio than MD_BASEFORM / PRP_PAST_PART
      case "DON_T_AREN_T":              return 1;   // higher prio than DID_BASEFORM
      case "WILL_BECOMING":             return 1;   // higher prio than MD_BASEFORM
      case "WOULD_NEVER_VBN":           return 1;   // higher prio than MD_BASEFORM
      case "MD_APPRECIATED":            return 1;   // higher prio than MD_BASEFORM
      case "MONEY_BACK_HYPHEN":         return 1;   // higher prio than A_UNCOUNTABLE
      case "WORLDS_BEST":               return 1;   // higher prio than THE_SUPERLATIVE
      case "STEP_COMPOUNDS":            return 1;   // higher prio than STARS_AND_STEPS
      case "WON_T_TO":                  return 1;   // higher prio than DON_T_AREN_T
      case "WAN_T":                     return 1;   // higher prio than DON_T_AREN_T
      case "THE_US":                    return 1;   // higher prio than DT_PRP
      case "THE_IT":                    return 1;   // higher prio than DT_PRP
      case "A_NUMBER_NNS":              return 1;   // higher prio than A_NNS
      case "A_HUNDREDS":                return 1;   // higher prio than A_NNS
      case "NOW_A_DAYS":                return 1;   // higher prio than A_NNS
      case "COUPLE_OF_TIMES":           return 1;   // higher prio than A_NNS
      case "A_WINDOWS":                 return 1;   // higher prio than A_NNS
      case "A_SCISSOR":                 return 1;   // higher prio than A_NNS
      case "A_SNICKERS":                return 1;   // higher prio than A_NNS
      case "ROUND_A_BOUT":              return 1;   // higher prio than A_NNS
      case "SEEM_SEEN":                 return 1;   // higher prio than HAVE_PART_AGREEMENT, PRP_HAVE_VB, MD_BASEFORM and PRP_PAST_PART
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
      case "YOU_GOOD":                  return 1;   // prefer over PRP_PAST_PART
      case "THAN_THANK":                return 1;   // prefer over THAN_THEN
      case "CD_NN_APOSTROPHE_S":        return 1;   // prefer over CD_NN and LOWERCASE_NAME_APOSTROPHE_S
      case "IT_IF":                     return 1;   // needs higher prio than PRP_COMMA and IF_YOU_ANY
      case "FINE_TUNE_COMPOUNDS":       return 1;   // prefer over less specific rules
      case "WHAT_IS_YOU":               return 1;   // prefer over HOW_DO_I_VB, NON3PRS_VERB
      case "SUPPOSE_TO":                return 1;   // prefer over HOW_DO_I_VB
      case "PROFANITY":                 return 1;   // prefer over spell checker (less prio than EN_COMPOUNDS)
      case "FOR_NOUN_SAKE":             return 6;   // prefer over PROFANITY (e.g. "for fuck sake")
      case "RUDE_SARCASTIC":            return 6;   // prefer over spell checker
      case "CHILDISH_LANGUAGE":         return 8;   // prefer over spell checker
      case "EN_DIACRITICS_REPLACE":     return 9;   // prefer over spell checker (like PHRASE_REPETITION)
      case "BLACK_SEA":                 return -1;  // less priority than SEA_COMPOUNDS
      case "A_TO":                      return -1;  // less priority than other rules that offer suggestions
      case "MANY_NN":                   return -1;  // less priority than PUSH_UP_HYPHEN, SOME_FACULTY
      case "WE_BE":                     return -1;
      case "A_LOT_OF_NN":               return -1;
      case "IT_VBZ":                    return -1;
      case "IT_IS_2":                   return -1;  // needs higher prio than BEEN_PART_AGREEMENT
      case "A_RB_NN":                   return -1;  // prefer other more specific rules (e.g. QUIET_QUITE, A_QUITE_WHILE)
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
      case "NON3PRS_VERB":              return -1;  // prefer other more specific rules (with suggestions, e.g. DONS_T)
      case "DID_FOUND_AMBIGUOUS":       return -1;  // prefer other more specific rules (e.g. TWO_CONNECTED_MODAL_VERBS)
      case "BE_I_BE_GERUND":            return -1;  // prefer other more specific rules (with suggestions)
      case "VBZ_VBD":                   return -1;  // prefer other more specific rules (e.g. IS_WAS)
      case "SUPERLATIVE_THAN":          return -1;  // prefer other more specific rules
      case "UNLIKELY_OPENING_PUNCTUATION": return -1; // prefer other more specific rules
      case "METRIC_UNITS_EN_IMPERIAL":  return -1;  // prefer MILE_HYPHEN
      case "METRIC_UNITS_EN_GB":        return -1;  // prefer MILE_HYPHEN
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
      case "PRP_THE":                   return -4;  // prefer other rules (e.g. I_A, PRP_JJ, IF_YOU_ANY, I_AN)
      case "MORFOLOGIK_RULE_EN_US":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_GB":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_CA":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_ZA":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_NZ":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_AU":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "TWO_CONNECTED_MODAL_VERBS": return -15;
      case "SENTENCE_FRAGMENT":         return -50; // prefer other more important sentence start corrections.
      case "SENTENCE_FRAGMENT_SINGLE_WORDS": return -51;  // prefer other more important sentence start corrections.
      case "EN_REDUNDANCY_REPLACE":     return -510;  // style rules should always have the lowest priority.
      case "EN_PLAIN_ENGLISH_REPLACE":  return -511;  // style rules should always have the lowest priority.
      case "THREE_NN":                  return -600;  // style rules should always have the lowest priority.
      case "SENT_START_NUM":            return -600;  // style rules should always have the lowest priority.
      case "PASSIVE_VOICE":             return -600;  // style rules should always have the lowest priority.
      case "EG_NO_COMMA":               return -600;  // style rules should always have the lowest priority.
      case "IE_NO_COMMA":               return -600;  // style rules should always have the lowest priority.
      case "REASON_WHY":                return -600;  // style rules should always have the lowest priority.
      case LongSentenceRule.RULE_ID:    return -997;
      case LongParagraphRule.RULE_ID:   return -998;
    }
    if (id.startsWith("CONFUSION_RULE_")) {
      return -20;
    }
    return super.getPriorityForId(id);
  }

  @Override
  public Function<Rule, Rule> getRemoteEnhancedRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    Function<Rule, Rule> fallback = super.getRemoteEnhancedRules(messageBundle, configs, userConfig, motherTongue, altLanguages, inputLogging);
    RemoteRuleConfig bert = RemoteRuleConfig.getRelevantConfig(BERTSuggestionRanking.RULE_ID, configs);

    return original -> {
      if (original.isDictionaryBasedSpellingRule() && original.getId().startsWith("MORFOLOGIK_RULE_EN")) {
        if (UserConfig.hasABTestsEnabled() && bert != null) {
          return new BERTSuggestionRanking(original, bert, userConfig, inputLogging);
        }
      }
      return fallback.apply(original);
    };
  }

  @Override
  public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRemoteRules(
      messageBundle, configs, globalConfig, userConfig, motherTongue, altLanguages, inputLogging));
    String theInsertionID = "AI_THE_INS_RULE";
    RemoteRuleConfig theInsertionConfig = RemoteRuleConfig.getRelevantConfig(theInsertionID, configs);
    final String missingTheDescription = "This rule identifies whether the article 'the' is missing in a sentence.";
    final String missingWordDescription = "This rule identifies whether the articles 'a' or 'an' are missing in a sentence.";
    final String variantsDescription = "Identifies confusion between if, of, off and a misspelling";
    final String delMessage = "This article might not be necessary here.";
    final String insMessage = "You might be missing an article here.";
    if (theInsertionConfig != null) {
      Map<String, String> theInsertionMessages = new HashMap<>();
      theInsertionMessages.put("THE_INS", delMessage);
      theInsertionMessages.put("INS_THE", insMessage);
      Rule theInsertionRule = GRPCRule.create(theInsertionConfig, inputLogging, theInsertionID,
                                              missingTheDescription, theInsertionMessages);
      rules.add(theInsertionRule);
    }
    String missingTheID = "AI_MISSING_THE";
    RemoteRuleConfig missingTheConfig = RemoteRuleConfig.getRelevantConfig(missingTheID, configs);
    if (missingTheConfig != null) {
      Map<String, String> missingTheMessages = new HashMap<>();
      missingTheMessages.put("MISSING_THE", insMessage);
      Rule missingTheRule = GRPCRule.create(missingTheConfig, inputLogging, missingTheID,
                                            missingTheDescription, missingTheMessages);
      rules.add(missingTheRule);
    }
    String missingWordID = "AI_MISSING_WORD";
    RemoteRuleConfig missingWordConfig = RemoteRuleConfig.getRelevantConfig(missingWordID, configs);
    if (missingWordConfig != null) {
      Rule missingWordRule = GRPCRule.create(missingWordConfig, inputLogging, missingWordID, missingWordDescription,
                                             Collections.emptyMap());// provided by server
      rules.add(missingWordRule);
    }
    List<String> confpairRules = Arrays.asList("AI_CONFPAIRS_EN_GPT2", "AI_CONFPAIRS_EN_GPT2_L", "AI_CONFPAIRS_EN_GPT2_XL");
    for (String confpairID : confpairRules) {
      RemoteRuleConfig confpairConfig = RemoteRuleConfig.getRelevantConfig(confpairID, configs);
      if (confpairConfig != null) {
        Rule confpairRule = new GRPCConfusionRule(messageBundle, confpairConfig, inputLogging);
        rules.add(confpairRule);
      }
    }
    String variantsID = "AI_EN_VAR";
    RemoteRuleConfig variantsConfig = RemoteRuleConfig.getRelevantConfig(variantsID, configs);
    if (variantsConfig != null) {
      Rule variantsRule = GRPCRule.create(variantsConfig, inputLogging, variantsID,
                                          variantsDescription, Collections.emptyMap());
      rules.add(variantsRule);
    }
    return rules;
  }
}
