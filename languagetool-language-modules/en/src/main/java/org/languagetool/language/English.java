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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.en.EnglishHybridDisambiguator;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Support for English - use the sub classes {@link BritishEnglish}, {@link AmericanEnglish},
 * etc. if you need spell checking.
 * Make sure to call {@link #close()} after using this (currently only relevant if you make
 * use of {@link EnglishConfusionProbabilityRule}).
 */
public class English extends Language implements AutoCloseable {

  protected static final LoadingCache<String, List<Rule>> cache = CacheBuilder.newBuilder()
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
    return EnglishTagger.INSTANCE;
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
        new LongSentenceRule(messages, userConfig, 40),
        new LongParagraphRule(messages, this, userConfig),
        //new OpenNMTRule(),     // commented out because of #903
        new ParagraphRepeatBeginningRule(messages, this),
        new PunctuationMarkAtParagraphEnd(messages, this),
        new PunctuationMarkAtParagraphEnd2(messages, this),
        // specific to English:
        new ConsistentApostrophesRule(messages),
        new EnglishSpecificCaseRule(messages),
        new EnglishUnpairedBracketsRule(messages, this),
        new EnglishWordRepeatRule(messages, this),
        new AvsAnRule(messages),
        new EnglishWordRepeatBeginningRule(messages, this),
        new CompoundRule(messages, this, userConfig),
        new ContractionSpellingRule(messages),
        new EnglishWrongWordInContextRule(messages),
        new EnglishDashRule(messages),
        new WordCoherencyRule(messages),
        new EnglishDiacriticsRule(messages),
        new EnglishPlainEnglishRule(messages),
        new EnglishRedundancyRule(messages),
        new SimpleReplaceRule(messages, this),
        new ReadabilityRule(messages, this, userConfig, false),
        new ReadabilityRule(messages, this, userConfig, true), 
        new EnglishRepeatedWordsRule(messages)
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
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel lm, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    if (lm != null && motherTongue != null && "fr".equals(motherTongue.getShortCode())) {
      return Arrays.asList(
          new EnglishForFrenchFalseFriendRule(messages, lm, motherTongue, this)
      );
    }
    if (lm != null && motherTongue != null && "de".equals(motherTongue.getShortCode())) {
      return Arrays.asList(
          new EnglishForGermansFalseFriendRule(messages, lm, motherTongue, this)
      );
    }
    if (lm != null && motherTongue != null && "es".equals(motherTongue.getShortCode())) {
      return Arrays.asList(
          new EnglishForSpaniardsFalseFriendRule(messages, lm, motherTongue, this)
      );
    }
    if (lm != null && motherTongue != null && "nl".equals(motherTongue.getShortCode())) {
      return Arrays.asList(
          new EnglishForDutchmenFalseFriendRule(messages, lm, motherTongue, this)
      );
    }
    return Arrays.asList();
  }

  @Override
  public boolean hasNGramFalseFriendRule(Language motherTongue) {
    return motherTongue != null && (
      // Note: extend EnglishForL2SpeakersFalseFriendRuleTest.testMessageDetailData()
      // if you add a language here
      "de".equals(motherTongue.getShortCode()) ||
      "fr".equals(motherTongue.getShortCode()) ||
      "es".equals(motherTongue.getShortCode()) ||
      "nl".equals(motherTongue.getShortCode()));
  }

  @Override
  public List<Rule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws IOException {
    return NeuralNetworkRuleCreator.createRules(messages, this, word2vecModel);
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
      case "I_E":                       return 10;  // needs higher prio than EN_COMPOUNDS ("i.e learning")
      case "CHILDISH_LANGUAGE":         return 8;   // prefer over spell checker
      case "RUDE_SARCASTIC":            return 6;   // prefer over spell checker
      case "FOR_NOUN_SAKE":             return 6;   // prefer over PROFANITY (e.g. "for fuck sake")
      case "YEAR_OLD_HYPHEN":           return 6;   // higher prio than MISSING_HYPHEN
      case "MISSING_HYPHEN":            return 5;
      case "WRONG_APOSTROPHE":          return 5;
      case "YOU_GOOD":                  return 3;   // prefer over AI_HYDRA_LEO_CP (YOU_YOURE etc.) // prefer over PRP_PAST_PART
      case "DOS_AND_DONTS":             return 3;
      case "IF_YOU_FURTHER_QUESTIONS":  return 3;   // higher prio than agreement rules and AI
      case "EN_COMPOUNDS":              return 2;
      case "ABBREVIATION_PUNCTUATION":  return 2;
      case "FOR_THE_MOST_PART2":        return 1;   // higher prio than FOR_THE_MOST_PART
      case "FACE_TO_FACE_HYPHEN":       return 1;   // higher prio than THIS_NNS
      case "RUN_ON":                    return 1;   // higher prio than TOO_LONG_SENTENCE
      case "ON_THE_LOOK_OUT":           return 1;   // higher prio than VERB_NOUN_CONFUSION
      case "APOSTROPHE_IN_DAYS":        return 1;   // higher prio than A_NNS
      case "PICTURE_PERFECT_HYPHEN":    return 1;   // higher prio than some agreement rules
      case "SAVE_SAFE":                 return 1;   // higher prio than agreement rules
      case "FEDEX":                     return 2;   // higher prio than many verb rules (e.g. MD_BASEFORM)
      case "DROP_DEAD_HYPHEN":          return 1;   // higher prio than agreement rules (e.g. I_AM_VB)
      case "HEAR_HERE":                 return 1;   // higher prio than agreement rules (e.g. I_AM_VB)
      case "THE_FRENCH":                return 1;   // higher prio than agreement rules (e.g. I_AM_VB)
      case "A_HEADS_UP":                return 1;   // higher prio than some plural agreement rules (e.g. THERE_S_MANY)
      case "THIS_MISSING_VERB":         return 1;   // higher priority than A_MY
      case "YOURE":                     return 1;   // prefer over EN_CONTRACTION_SPELLING
      case "LIFE_COMPOUNDS":            return 1;
      case "DRIVE_THROUGH_HYPHEN":      return 1;   // higher prio than agreement rules
      case "CAUSE_COURSE":              return 1;   // higher prio than CAUSE_BECAUSE
      case "AN_AND":                    return 1;   // higher prio than A_MY and DT_PRP
      case "HER_S":                     return 1;   // higher prio than THEIR_S
      case "ONE_TO_MANY_HYPHEN":        return 1;   // higher prio than TO_TOO
      case "COVID_19":                  return 1;
      case "RATHER_NOT_VB":             return 1;   // higher prio than NOT_TO_DOES_NOT
      case "OTHER_WISE_COMPOUND":       return 1;
      case "ON_EXCEL":                  return 1;
      case "ALL_NN":                    return 1;   // higher prio than MASS_AGREEMENT
      case "SHOW_COMPOUNDS":            return 1;   // higher prio than agreement rules
      case "PRP_AREA":                  return 1;   // higher prio than you/your confusion rules
      case "IF_VB_PCT":                 return 1;   // higher prio than IF_VB
      case "CAUSE_BECAUSE":             return 1;   // higher prio than MISSING_TO_BETWEEN_BE_AND_VB
      case "MAY_MANY":                  return 1;   // higher prio than MAY_MANY_MY
      case "BOUT_TO":                   return 1;   // higher prio than PRP_VB
      case "HAVE_HAVE":                 return 1;   // higher prio than HE_D_VBD
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
      case "FINAL_THOUGH_HOWEVER_Q":    return 1;   // higher prio than THOUGH_COMMA
      case "WORLDS_BEST":               return 1;   // higher prio than THE_SUPERLATIVE
      case "STEP_COMPOUNDS":            return 1;   // higher prio than STARS_AND_STEPS
      case "WON_T_TO":                  return 1;   // higher prio than DON_T_AREN_T
      case "WAN_T":                     return 1;   // higher prio than DON_T_AREN_T
      case "THE_US":                    return 1;   // higher prio than DT_PRP
      case "THE_IT":                    return 1;   // higher prio than DT_PRP
      case "THANK_YOU_MUCH":            return 1;   // higher prio than other rules
      case "TO_DO_HYPHEN":              return 1;   // higher prio than other rules
      case "A_NUMBER_NNS":              return 1;   // higher prio than A_NNS
      case "A_HUNDREDS":                return 1;   // higher prio than A_NNS
      case "NOW_A_DAYS":                return 1;   // higher prio than A_NNS
      case "COUPLE_OF_TIMES":           return 1;   // higher prio than A_NNS
      case "A_WINDOWS":                 return 1;   // higher prio than A_NNS
      case "A_SCISSOR":                 return 1;   // higher prio than A_NNS
      case "A_SNICKERS":                return 1;   // higher prio than A_NNS
      case "ROUND_A_BOUT":              return 1;   // higher prio than A_NNS
      case "A_NNS_BEST_NN":             return 1;   // higher prio than A_NNS
      case "A_BACHELORS_IN":            return 1;   // higher prio than A_NNS
      case "NEITHER_NOR":               return 1;   // higher prio than COMMA_COMPOUND_SENTENCE
      case "FOR_AWHILE":                return 1;   // higher prio than COMMA_COMPOUND_SENTENCE
      case "A_BUT":                     return 1;   // higher prio than COMMA_COMPOUND_SENTENCE
      case "BORN_IN":                   return 1;   // higher prio than PRP_PAST_PART
      case "DO_TO":                     return 1;   // higher prio than HAVE_PART_AGREEMENT
      case "CURIOS_CURIOUS":            return 1;   // higher prio than A_NNS and POSSESSIVE_APOSTROPHE
      case "INCORRECT_POSSESSIVE_APOSTROPHE": return 1;  // higher prio than THIS_NNS
      case "THIS_YEARS_POSSESSIVE_APOSTROPHE": return 1;  // higher prio than THIS_NNS
      case "SPURIOUS_APOSTROPHE":       return 1;   // higher prio than THIS_NNS
      case "IN_THIS_REGARDS":           return 1;   // higher prio than THIS_NNS
      case "IT_SEAMS":                  return 1;   // higher prio than THIS_NNS_VB
      case "NO_WHERE":                  return 1;   // higher prio than NOW
      case "APOSTROPHE_VS_QUOTE":       return 1;   // higher prio than EN_QUOTES
      case "COMMA_PERIOD":              return 1;   // higher prio than COMMA_PARENTHESIS_WHITESPACE
      case "HERE_HEAR":                 return 1;   // higher prio than ENGLISH_WORD_REPEAT_RULE
      case "MISSING_POSS_APOS":         return 1;   // higher prio than SINGULAR_NOUN_VERB_AGREEMENT
      case "DO_HE_VERB":                return 1;   // prefer over HE_VERB_AGR
      case "LIGATURES":                 return 1;   // prefer over spell checker
      case "APPSTORE":                  return 1;   // prefer over spell checker
      case "INCORRECT_CONTRACTIONS":    return 1;   // prefer over EN_CONTRACTION_SPELLING
      case "CAUSED_BY":                 return 1;   // prefer over PASSIVE_VOICE_SIMPLE
      case "DONT_T":                    return 1;   // prefer over EN_CONTRACTION_SPELLING
      case "DONT_LIKE":                 return 1;   // prefer over EN_WORDINESS_PREMIUM
      case "WHATS_APP":                 return 1;   // prefer over EN_CONTRACTION_SPELLING
      case "NON_STANDARD_COMMA":        return 1;   // prefer over spell checker
      case "NON_STANDARD_ALPHABETIC_CHARACTERS": return 1;  // prefer over spell checker
      case "WONT_CONTRACTION":          return 1;   // prefer over WONT_WANT
      case "THAN_THANK":                return 1;   // prefer over THAN_THEN
      case "CD_NN_APOSTROPHE_S":        return 1;   // prefer over CD_NN and LOWERCASE_NAME_APOSTROPHE_S
      case "IT_IF":                     return 1;   // needs higher prio than PRP_COMMA and IF_YOU_ANY
      case "FINE_TUNE_COMPOUNDS":       return 1;   // prefer over less specific rules
      case "WHAT_IS_YOU":               return 1;   // prefer over HOW_DO_I_VB, NON3PRS_VERB
      case "SUPPOSE_TO":                return 1;   // prefer over HOW_DO_I_VB and I_AM_VB and ARE_WE_HAVE
      case "CONFUSION_GONG_GOING":      return 1;   // prefer over I_AM_VB
      case "SEEN_SEEM":                 return 1;   // prefer over PRP_PAST_PART
      case "PROFANITY":                 return 1;   // prefer over spell checker (less prio than EN_COMPOUNDS)
      case "GOOD_FLUCK":                return 2;   // prefer over PROFANITY
      case "THE_THEM":                  return 1;   // prefer over TO_TWO
      case "THERE_THEIR":               return 1;   // prefer over GO_TO_HOME
      case "IT_IS_DEPENDING_ON":        return 1;   // prefer over PROGRESSIVE_VERBS
      case "TO_NIGHT_TO_DAY":           return 1;   // prefer over TOO_JJ_TO
      case "IRREGARDLESS":              return 1;   // prefer over spell checker
      case "ULTRA_HYPHEN":              return 1;   // prefer over EN_COMPOUND_ADJECTIVE_INTERNAL
      case "THINK_BELIEVE_THAT":        return 1;
      case "IF_DT_NN_VBZ":              return -1;  // prefer IF_PLEASE_COMMA
      case "MD_VBD":                    return -1;  // prefer LOWERCASE_MONTHS
      case "PRP_PRP":                   return -1;  // prefer other rules that give a suggestion
      case "IS_LIKELY_TO_BE":           return -1;  // give IS_RB_BE precedence (more suggestions)
      case "FOCUS_IN_2":                return 1;   // prefer over FOCUS_IN (delete this setting and FOCUS_IN after testing)
      case "WANNA":                     return 1;   // prefer over spell checker
      case "LOOK_FORWARD_TO":           return 1;   // prefer over LOOK_FORWARD_NOT_FOLLOWED_BY_TO
      case "LOOK_SLIKE":                return 1;   // higher prio than prem:SINGULAR_NOUN_VERB_AGREEMENT
      case "A3FT":                      return 1;   // higher prio than NUMBERS_IN_WORDS
      case "EN_DIACRITICS_REPLACE":     return -1;   // prefer over spell checker
      case "MISSING_COMMA_BETWEEN_DAY_AND_YEAR":     return -1;   // less priority than DATE_WEEKDAY
      case "FASTLY":                    return -1;   // higher prio than spell checker
      case "ANYWAYS":                   return -1;   // higher prio than spell checker
      case "MISSING_GENITIVE":          return -1;  // prefer over spell checker (like EN_SPECIFIC_CASE)
      case "EN_UNPAIRED_BRACKETS":      return -1;  // less priority than rules that suggest the correct brackets
      case "WAKED_UP":                  return -1;  // less priority than other grammar rules
      case "NEEDS_FIXED":               return -1;  // less priority than MISSING_TO_BEFORE_A_VERB
      case "SENT_START_NNP_COMMA":      return -1;  // prefer other more specific rules
      case "SENT_START_NN_DT":          return -1;  // prefer MISSING_PREPOSITION
      case "DT_PDT":                    return -1;  // prefer other more specific rules
      case "MD_VB_AND_NOTVB":           return -1;  // prefer other more specific rules
      case "BLACK_SEA":                 return -1;  // less priority than SEA_COMPOUNDS
      case "A_TO":                      return -1;  // less priority than other rules that offer suggestions
      case "MANY_NN":                   return -1;  // less priority than PUSH_UP_HYPHEN, SOME_FACULTY
      case "WE_BE":                     return -1;
      case "A_LOT_OF_NN":               return -1;
      case "IT_VBZ":                    return -1;
      case "REPETITIONS_STYLE":         return -51;  // repetition style rules, usually with prefix REP_
      case "ORDER_OF_WORDS_WITH_NOT":   return -1;  // less prio than punctuation rules
      case "ADVERB_WORD_ORDER_10_TEMP": return 1;
      case "ADVERB_WORD_ORDER":         return -1;  // less prio than PRP_PAST_PART //
      case "HAVE_VB_DT":                return -1;
      case "MD_PRP":                    return -1;  // prefer ME_BE
      case "HAVE_TO_NOTVB":             return -1;
      case "IT_IS_2":                   return -1;  // needs higher prio than BEEN_PART_AGREEMENT
      case "A_RB_NN":                   return -1;  // prefer other more specific rules (e.g. QUIET_QUITE, A_QUITE_WHILE)
      case "DT_RB_IN":                  return -1;  // prefer other more specific rules
      case "VERB_NOUN_CONFUSION":       return -1;  // prefer other more specific rules
      case "NOUN_VERB_CONFUSION":       return -1;  // prefer other more specific rules
      case "PLURAL_VERB_AFTER_THIS":    return -1;  // prefer other more specific rules (e.g. COMMA_TAG_QUESTION)
      case "BE_RB_BE":                  return -1;  // prefer other more specific rules
      case "IT_ITS":                    return -1;  // prefer other more specific rules
      case "ENGLISH_WORD_REPEAT_RULE":  return -1;  // prefer other more specific rules (e.g. IT_IT)
      case "NON_ANTI_PRE_JJ":           return -1;  // prefer other more specific rules
      case "DT_JJ_NO_NOUN":             return -1;  // prefer other more specific rules (e.g. THIRD_PARTY)
      case "AGREEMENT_SENT_START":      return -1;  // prefer other more specific rules
      case "PREPOSITION_VERB":          return -1;  // prefer other more specific rules
      case "EN_A_VS_AN":                return -1;  // prefer other more specific rules (with suggestions, e.g. AN_ALSO)
      case "CD_NN":                     return -1;  // prefer other more specific rules (with suggestions)
      case "CD_NNU":                    return -1;  // prefer other more specific rules (with suggestions)
      case "ATD_VERBS_TO_COLLOCATION":  return -1;  // prefer other more specific rules (with suggestions)
      case "ORDINAL_NUMBER_MISSING_ORDINAL_INDICATOR": return -1;  // prefer other more specific rules (with suggestions)
      case "ADVERB_OR_HYPHENATED_ADJECTIVE": return -1;  // prefer other more specific rules (with suggestions)
      case "GOING_TO_VBD":              return -1;  // prefer other more specific rules (with suggestions, e.g. GOING_TO_JJ)
      case "MISSING_PREPOSITION":       return -1;  // prefer other more specific rules (with suggestions)
      case "CHARACTER_APOSTROPHE_WORD": return -1;  // prefer other more specific rules
      case "SINGLE_CHARACTER":          return -1;  // prefer other more specific rules (with suggestions)
      case "BE_TO_VBG":                 return -1;  // prefer other more specific rules (with suggestions)
      case "NON3PRS_VERB":              return -1;  // prefer other more specific rules (with suggestions, e.g. DONS_T)
      case "DT_NN_VBG":                 return -1;  // prefer other more specific rules (with suggestions)
      case "NNS_THAT_ARE_JJ":           return -1;  // prefer other more specific rules
      case "MISSING_SUBJECT":           return -1;  // prefer other more specific rules
      case "DID_FOUND_AMBIGUOUS":       return -1;  // prefer other more specific rules (e.g. TWO_CONNECTED_MODAL_VERBS)
      case "BE_I_BE_GERUND":            return -1;  // prefer other more specific rules (with suggestions)
      case "VBZ_VBD":                   return -1;  // prefer other more specific rules (e.g. IS_WAS)
      case "SUPERLATIVE_THAN":          return -1;  // prefer other more specific rules
      case "UNLIKELY_OPENING_PUNCTUATION": return -1;  // prefer other more specific rules
      case "MD_DT_JJ":                  return -1;  // prefer other more specific rules
      case "CC_PRP_ARTICLE":            return -1;  // prefer other more specific rules
      case "NOUNPHRASE_VB_RB_DT":       return -1;  // prefer other more specific rules
      case "SENT_START_NN_NN_VB":       return -1;  // prefer other more specific rules
      case "VB_A_JJ_NNS":               return -1;  // prefer other more specific rules (e.g. A_NNS)
      case "DUPLICATION_OF_IS_VBZ":     return -1;  // prefer other more specific rules (e.g. A_NNS)
      case "METRIC_UNITS_EN_IMPERIAL":  return -1;  // prefer MILE_HYPHEN
      case "METRIC_UNITS_EN_GB":        return -1;  // prefer MILE_HYPHEN
      case "IF_THEN_COMMA":             return -1;  // prefer CONFUSION_OF_THEN_THAN
      case "COMMA_COMPOUND_SENTENCE":   return -1;  // prefer other rules
      case "COMMA_COMPOUND_SENTENCE_2": return -1;  // prefer other rules
      case "BE_VBG_BE":                 return -1;  // prefer other more specific rules
      case "PRP_VB_VB":                 return -1;  // prefer other more specific rules
      case "FOR_ANY_CLARIFICATIONS":    return -1;  // prefer SENT_START_ALL_CAPITALS and ALL_UPPERCASE
      case "PLEASE_LET_ME_KNOW":        return -1;  // prefer SENT_START_ALL_CAPITALS and ALL_UPPERCASE
      case "UNNECESSARY_CAPITALIZATION": return -1;  // prefer other more specific rules
      case "CONFUSION_OF_A_JJ_NNP_NNS_PRP": return -1;  // prefer other more specific rules
      case "PLURALITY_CONFUSION_OF_NNS_OF_NN": return -1; // prefer several compound rules
      case "NP_TO_IS":                  return -1;  // prefer other more specific rules
      case "NNP_COMMA_QUESTION":        return -2;  // prefer other more specific rules
      case "VB_TO_NN_DT":               return -2;  // prefer other more specific rules (e.g. NOUN_VERB_CONFUSION)
      case "REPEATED_VERBS":            return -1;  // prefer other rules
      case "THE_CC":                    return -2;  // prefer other more specific rules (with suggestions)
      case "PRP_RB_NO_VB":              return -2;  // prefer other more specific rules (with suggestions)
      case "PRP_VBG":                   return -2;  // prefer other more specific rules (with suggestions, prefer over HE_VERB_AGR)
      case "PRP_VBZ":                   return -2;  // prefer other more specific rules (with suggestions)
      case "DT_NN_ARE_AME":             return -2;  // prefer other more specific rules
      case "CANT_JJ":                   return -2;  // prefer other more specific rules
      case "WOULD_A":                   return -2;  // prefer other more specific rules
      case "I_AM_VB":                   return -2;  // prefer other rules
      case "MD_PRP_QUESTION_MARK":      return -2;  // less prio than YOU_YOUR and YOU_YOUR_2
      case "BE_VBP_IN":                 return -2;  // prefer over BEEN_PART_AGREEMENT
      case "GONNA_TEMP":                return -3;
      case "A_INFINITIVE":              return -3;  // prefer other more specific rules (with suggestions, e.g. PREPOSITION_VERB, THE_TO)
      case "HE_VERB_AGR":               return -3;  // prefer other more specific rules (e.g. PRP_VBG)
      case "PRP_JJ":                    return -3;  // prefer other rules (e.g. PRP_VBG, IT_IT and ADJECTIVE_ADVERB, PRP_ABLE, PRP_NEW, MD_IT_JJ)
      case "INDIAN_ENGLISH":            return -3;  // prefer grammar rules, but higher prio than spell checker
      case "DO_PRP_NOTVB":              return -3;  // prefer other more specific rules (e.g. HOW_DO_I_VB)
      case "ARTICLE_VB":                return -3;  // prefer A_INFINITIVE and other more specific rules (with suggestions)
      case "PRP_A":                     return -4;  // prefer other more specific rules (e.g. I_AN, PRP_JJ)
      case "PRP_NO_ADVERB_VERB":        return -4;  // prefer other more specific rules (e.g. PRP_VBG, IT_ITS, ...)
      case "PRP_THE":                   return -4;  // prefer other rules (e.g. I_A, PRP_JJ, IF_YOU_ANY, I_AN)
      case "GONNA":                     return -4;  // prefer over spelling rules
      case "WHATCHA":                   return -4;  // prefer over spelling rules
      case "DONTCHA":                   return -4;  // prefer over spelling rules
      case "GOTCHA":                    return -4;  // prefer over spelling rules
      case "OUTTA":                     return -4;  // prefer over spelling rules
      case "Y_ALL":                     return -4;  // prefer over spelling rules
      case "GIMME":                     return -4;  // prefer over spelling rules
      case "LEMME":                     return -4;  // prefer over spelling rules
      case "POSSESSIVE_APOSTROPHE":     return -4;  // prefer some agreement rules
      case "HAVE_PART_AGREEMENT":       return -9;  // prefer HYDRA_LEO
      case "BEEN_PART_AGREEMENT":       return -9;  // prefer HYDRA_LEO
      case "MORFOLOGIK_RULE_EN_US":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_GB":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_CA":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_ZA":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_NZ":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "MORFOLOGIK_RULE_EN_AU":     return -10;  // more specific rules (e.g. L2 rules) have priority
      case "BE_VBG_NN":                 return -11;  // prefer other more specific rules and speller
      case "BE_WITH_WRONG_VERB_FORM":   return -11;  // prefer HYDRA_LEO, BEEN_PART_AGREEMENT and other rules
      case "THE_NNS_NN_IS":             return -12;  // prefer HYDRA_LEO
      case "PRP_MD_NN":                 return -12;  // prefer other more specific rules (e.g. MD_ABLE, WONT_WANT)
      case "TWO_CONNECTED_MODAL_VERBS": return -15;
      case "BE_MD":                     return -20;  // prefer other more specific rules (e.g. BEEN_PART_AGREEMENT, HYDRA_LEO)
      case "CONFUSION_RULE":            return -20;
      case "PRP_VB_IMPROVE":            return -24;  // higher prio than PRP_VB but prefer other rules (with suggestions, e.g. confusion rules)
      case "WANT_TO_NN":                return -25;  // prefer more specific rules that give a suggestion
      case "QUESTION_WITHOUT_VERB":     return -25;  // prefer more specific rules that give a suggestion
      case "PRP_VB":                    return -25;  // prefer other rules (with suggestions, e.g. confusion rules)
      case "PRP_VB_NN":                 return -25;  // prefer other more specific rules (e.g. HYDRA_LEO)
      case "BE_NN":                     return -26;  // prefer other more specific rules (e.g. PRP_VB_NN, BEEN_PART_AGREEMENT, HYDRA_LEO)
      case "BE_VB_OR_NN":               return -26;  // prefer other more specific rules (e.g. PRP_VB_NN, BE_MD, BEEN_PART_AGREEMENT, HYDRA_LEO)
      case "PRONOUN_NOUN":              return -26;  // prefer other rules (with suggestions, e.g. confusion rules)
      case "SENTENCE_FRAGMENT":         return -50;  // prefer other more important sentence start corrections.
      case "AI_HYDRA_LEO_MISSING_COMMA": return -51; // prefer comma style rules.
      case "SENTENCE_FRAGMENT_SINGLE_WORDS": return -51;  // prefer other more important sentence start corrections.
      case "MD_NN":                     return -60;  // prefer PRP_MD_NN
      case "I_THINK_FEEL":              return -60;
      case "KNOW_AWARE_REDO":           return -60;
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
      case "ALL_UPPERCASE":             return -1000;  // do not hide spelling and grammar issues, when text is all upper case
    }
    if (id.startsWith("CONFUSION_RULE_")) {
      return -20;
    }
    if (id.startsWith("AI_SPELLING_RULE")) {
      return -9; // higher than MORFOLOGIK_*, for testing
    }
    if (id.startsWith("AI_HYDRA_LEO")) { // prefer more specific rules (also speller)
      if (id.startsWith("AI_HYDRA_LEO_CP")) {
        return 2;
      }
      if (id.startsWith("AI_HYDRA_LEO_CP_YOU")) {
        return 1;
      }
      if (id.startsWith("AI_HYDRA_LEO_MISSING_A")) {
        return -8; // higher prio than BEEN_PART_AGREEMENT and HAVE_BEEN_AGREEMENT
      }
      return -11;
    }
    if (id.startsWith("AI_EN_G_")) { // prefer more specific rules (also speller)
      return -21;
    }
    if (id.matches("EN_FOR_[A-Z]+_SPEAKERS_FALSE_FRIENDS.*")) {
      return -21;
    }
    return super.getPriorityForId(id);
  }

  @Override
  public Function<Rule, Rule> getRemoteEnhancedRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    Function<Rule, Rule> fallback = super.getRemoteEnhancedRules(messageBundle, configs, userConfig, motherTongue, altLanguages, inputLogging);
    RemoteRuleConfig bert = RemoteRuleConfig.getRelevantConfig(BERTSuggestionRanking.RULE_ID, configs);

    return original -> {
      if (original.isDictionaryBasedSpellingRule() && original.getId().startsWith("MORFOLOGIK_RULE_EN")) {
        if (bert != null) {
          return new BERTSuggestionRanking(this, original, bert, inputLogging);
        }
      }
      return fallback.apply(original);
    };
  }

  @Override
  public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRemoteRules(
      messageBundle, configs, globalConfig, userConfig, motherTongue, altLanguages, inputLogging));

    // no description needed - matches based on automatically created rules with descriptions provided by remote server
    rules.addAll(GRPCRule.createAll(this, configs, inputLogging,
      "AI_EN_", "INTERNAL - dynamically loaded rule supported by remote server"));

    if (getCountries().length == 1) {
      // automatically load any existing AI spelling models for all variants
      String country = getCountries()[0];
      rules.addAll(GRPCRule.createAll(this, configs, inputLogging,
        "AI_SPELLING_RULE_EN_" + country, "INTERNAL - dynamically loaded rule supported by remote server"));
    }
    return rules;
  }
  
  public boolean hasMinMatchesRules() {
    return true;
  }
  
  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
      return new MorfologikAmericanSpellerRule(messages, this, null, Collections.emptyList());
  }

}
