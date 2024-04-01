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
import org.languagetool.rules.en.LongSentenceRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
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
import org.languagetool.tools.Tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

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
            rules.addAll(loader.getRules(is, path, null));
          }
          return rules;
        }
      });
  private static final Language AMERICAN_ENGLISH = new AmericanEnglish();
  private static final Pattern FALSE_FRIENDS_PATTERN = Pattern.compile("EN_FOR_[A-Z]+_SPEAKERS_FALSE_FRIENDS.*");

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
    return EnglishSynthesizer.INSTANCE;
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new EnglishHybridDisambiguator(getDefaultLanguageVariant());
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
    allRules.addAll(asList(
        new CommaWhitespaceRule(messages,
                Example.wrong("We had coffee<marker> ,</marker> cheese and crackers and grapes."),
                Example.fixed("We had coffee<marker>,</marker> cheese and crackers and grapes.")),
        new DoublePunctuationRule(messages, Tools.getUrl("https://languagetool.org/insights/post/punctuation-guide/#what-are-periods")),
        new UppercaseSentenceStartRule(messages, this,
                Example.wrong("This house is old. <marker>it</marker> was built in 1950."),
                Example.fixed("This house is old. <marker>It</marker> was built in 1950."),
                Tools.getUrl("https://languagetool.org/insights/post/spelling-capital-letters/")),
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
        new EnglishUnpairedQuotesRule(messages, this),
        new EnglishWordRepeatRule(messages, this),
        new AvsAnRule(messages),
        new EnglishWordRepeatBeginningRule(messages, this),
        new CompoundRule(messages, this, userConfig),
        new ContractionSpellingRule(messages, this),
        new EnglishWrongWordInContextRule(messages, this),
        new EnglishDashRule(messages),
        new WordCoherencyRule(messages),
        new EnglishDiacriticsRule(messages),
        new EnglishPlainEnglishRule(messages),
        new EnglishRedundancyRule(messages),
        new SimpleReplaceRule(messages, this),
        new SimpleReplaceProfanityRule(messages, this),
        new ReadabilityRule(messages, this, userConfig, false),
        new ReadabilityRule(messages, this, userConfig, true), 
        new EnglishRepeatedWordsRule(messages),
        new StyleTooOftenUsedVerbRule(messages, this, userConfig),
        new StyleTooOftenUsedNounRule(messages, this, userConfig),
        new StyleTooOftenUsedAdjectiveRule(messages, this, userConfig)
    ));
    return allRules;
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return asList(
        new UpperCaseNgramRule(messages, languageModel, this, userConfig),
        new EnglishConfusionProbabilityRule(messages, languageModel, this),
        new EnglishNgramProbabilityRule(messages, languageModel, this)
    );
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel lm, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    if (lm != null && motherTongue != null) {
      if ("fr".equals(motherTongue.getShortCode())) {
        return asList(new EnglishForFrenchFalseFriendRule(messages, lm, motherTongue, this));
      } else if ("de".equals(motherTongue.getShortCode())) {
        return asList(new EnglishForGermansFalseFriendRule(messages, lm, motherTongue, this));
      } else if ("es".equals(motherTongue.getShortCode())) {
        return asList(new EnglishForSpaniardsFalseFriendRule(messages, lm, motherTongue, this));
      } else if ("nl".equals(motherTongue.getShortCode())) {
        return asList(new EnglishForDutchmenFalseFriendRule(messages, lm, motherTongue, this));
      }
    }
    return asList();
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
  public int getRulePriority(Rule rule) {
    int categoryPriority = this.getPriorityForId(rule.getCategory().getId().toString());
    int rulePriority = this.getPriorityForId(rule.getId());
    // if there is a priority defined for the rule,
    // it takes precedence over category priority
    if (rulePriority != 0) {
      return rulePriority;
    }
    if (categoryPriority != 0) {
      return categoryPriority;
    }
    if (rule.getLocQualityIssueType().equals(ITSIssueType.Style)) {
      // don't let style issues hide more important errors
      return -50;
    }
    return 0;
  }
  
  private final static Map<String, Integer> id2prio = new HashMap<>();
  static {
    id2prio.put("I_E", 10);  // needs higher prio than EN_COMPOUNDS ("i.e learning")
    id2prio.put("CHILDISH_LANGUAGE", 8);   // prefer over spell checker
    id2prio.put("RUDE_SARCASTIC", 6);   // prefer over spell checker
    id2prio.put("FOR_NOUN_SAKE", 6);   // prefer over PROFANITY (e.g. "for fuck sake")
    id2prio.put("YEAR_OLD_HYPHEN", 6);   // higher prio than MISSING_HYPHEN
    id2prio.put("MISSING_HYPHEN", 5);
    id2prio.put("WRONG_APOSTROPHE", 5);
    id2prio.put("YOU_GOOD", 3);   // prefer over AI_HYDRA_LEO_CP (YOU_YOURE etc.) // prefer over PRP_PAST_PART
    id2prio.put("DOS_AND_DONTS", 3);
    id2prio.put("IF_YOU_FURTHER_QUESTIONS", 3);   // higher prio than agreement rules and AI
    id2prio.put("ABBREVIATION_PUNCTUATION", 2);
    id2prio.put("READ_ONLY_ACCESS_HYPHEN", 2);   // higher priority than agreement rules
    id2prio.put("MAKE_OR_BREAK_HYPHEN", 2);   // higher priority than agreement rules
    id2prio.put("LINKED_IN", 2);   // higher prio than agreement rules
    id2prio.put("T_HE", 1);   // higher prio than agreement rules
    id2prio.put("I_A_M", 1);   // higher prio than EN_A_VS_AN
    id2prio.put("ACCESS_EXCESS", 1);   // higher prio than A_UNCOUNTABLE
    id2prio.put("PRP_ABLE_TO", 1);   // higher prio than AI_HYDRA_LEO_CP_YOU.*
    id2prio.put("WEE_WE", 1);   // higher prio than INTERJECTIONS_PUNCTUATION
    id2prio.put("CAN_MISSPELLING", 1);   // higher prio than COM_COME
    id2prio.put("FOR_THE_MOST_PART2", 1);   // higher prio than FOR_THE_MOST_PART
    id2prio.put("FACE_TO_FACE_HYPHEN", 1);   // higher prio than THIS_NNS
    id2prio.put("RUN_ON", 1);   // higher prio than TOO_LONG_SENTENCE
    id2prio.put("ON_THE_LOOK_OUT", 1);   // higher prio than VERB_NOUN_CONFUSION
    id2prio.put("APOSTROPHE_IN_DAYS", 1);   // higher prio than A_NNS
    id2prio.put("SAFE_GUARD_COMPOUND", 1);   // higher prio than some agreement rules
    id2prio.put("EVEN_HANDED_HYPHEN", 1);   // higher prio than some agreement rules
    id2prio.put("GET_TOGETHER_HYPHEN", 1);   // higher prio than some agreement rules
    id2prio.put("GOT_HERE", 1);   // higher prio than GET_VBN and HEAR_HERE
    id2prio.put("PICTURE_PERFECT_HYPHEN", 1);   // higher prio than some agreement rules
    id2prio.put("SEEM_SEEN", 1);   // higher prio than some agreement rules (e.g. PRP_HAVE_VB)
    id2prio.put("SAVE_SAFE", 1);   // higher prio than agreement rules
    id2prio.put("FEDEX", 2);   // higher prio than many verb rules (e.g. MD_BASEFORM)
    id2prio.put("DROP_DEAD_HYPHEN", 1);   // higher prio than agreement rules (e.g. I_AM_VB)
    id2prio.put("HEAR_HERE", 1);   // higher prio than agreement rules (e.g. I_AM_VB)
    id2prio.put("THE_FRENCH", 1);   // higher prio than agreement rules (e.g. I_AM_VB)
    id2prio.put("A_HEADS_UP", 1);   // higher prio than some plural agreement rules (e.g. THERE_S_MANY)
    id2prio.put("UNITES_UNITED", 1);   // higher prio than IS_VBZ
    id2prio.put("THIS_MISSING_VERB", 1);   // higher priority than A_MY
    id2prio.put("YOURE", 1);   // prefer over EN_CONTRACTION_SPELLING
    id2prio.put("LIFE_COMPOUNDS", 1);
    id2prio.put("DRIVE_THROUGH_HYPHEN", 1);   // higher prio than agreement rules
    id2prio.put("CAUSE_COURSE", 1);   // higher prio than CAUSE_BECAUSE
    id2prio.put("THANK_YOUR", 1);   // higher prio than POSSESSIVE_DETERMINER_SENT_END
    id2prio.put("AN_AND", 1);   // higher prio than A_MY and DT_PRP
    id2prio.put("HER_S", 1);   // higher prio than THEIR_S
    id2prio.put("ONE_TO_MANY_HYPHEN", 1);   // higher prio than TO_TOO
    id2prio.put("COVID_19", 1);
    id2prio.put("RATHER_NOT_VB", 1);   // higher prio than NOT_TO_DOES_NOT
    id2prio.put("PIECE_COMPOUNDS", 1);
    id2prio.put("OTHER_WISE_COMPOUND", 1);
    id2prio.put("ON_EXCEL", 1);
    id2prio.put("ALL_NN", 1);   // higher prio than MASS_AGREEMENT
    id2prio.put("SHOW_COMPOUNDS", 1);   // higher prio than agreement rules
    id2prio.put("PRP_AREA", 1);   // higher prio than you/your confusion rules
    id2prio.put("IF_VB_PCT", 1);   // higher prio than IF_VB
    id2prio.put("CAUSE_BECAUSE", 1);   // higher prio than MISSING_TO_BETWEEN_BE_AND_VB
    id2prio.put("MAY_MANY", 1);   // higher prio than MAY_MANY_MY
    id2prio.put("BOUT_TO", 1);   // higher prio than PRP_VB
    id2prio.put("HAVE_HAVE", 1);   // higher prio than HE_D_VBD
    id2prio.put("LUV", 1);   // higher prio than spell checker
    id2prio.put("DAT", 1);   // higher prio than spell checker
    id2prio.put("MAC_OS", 1);   // higher prio than spell checker
    id2prio.put("BESTEST", 1);   // higher prio than spell checker
    id2prio.put("OFF_OF", 1);   // higher prio than ADJECTIVE_ADVERB
    id2prio.put("SHELL_COMPOUNDS", 1);   // higher prio than HELL
    id2prio.put("HANDS_ON_HYPHEN", 1);   // higher prio than A_NNS
    id2prio.put("PROFITS_WARNINGS", 1);   // higher prio than A_NNS
    id2prio.put("QUIET_QUITE", 1);   // higher prio than A_QUITE_WHILE
    id2prio.put("A_OK", 1);   // prefer over A_AN
    id2prio.put("I_A", 1);   // higher prio than I_IF
    id2prio.put("PRP_NO_VB", 1);   // higher prio than I_IF
    id2prio.put("GAVE_HAVE", 1);   // higher prio than MD_BASEFORM
    id2prio.put("THERE_FORE", 1);   // higher prio than FORE_FOR
    id2prio.put("FOLLOW_UP", 1);   // higher prio than MANY_NN
    id2prio.put("IT_SOMETHING", 1);   // higher prio than IF_YOU_ANY and IT_THE_PRP
    id2prio.put("NO_KNOW", 1);   // higher prio than DOUBLE_NEGATIVE
    id2prio.put("WILL_BASED_ON", 1);   // higher prio than MD_BASEFORM / PRP_PAST_PART
    id2prio.put("DON_T_AREN_T", 1);   // higher prio than DID_BASEFORM
    id2prio.put("WILL_BECOMING", 1);   // higher prio than MD_BASEFORM
    id2prio.put("WOULD_NEVER_VBN", 1);   // higher prio than MD_BASEFORM
    id2prio.put("MONEY_BACK_HYPHEN", 1);   // higher prio than A_UNCOUNTABLE
    id2prio.put("WORLDS_BEST", 1);   // higher prio than THE_SUPERLATIVE
    id2prio.put("STEP_COMPOUNDS", 1);   // higher prio than STARS_AND_STEPS
    id2prio.put("WON_T_TO", 1);   // higher prio than DON_T_AREN_T
    id2prio.put("WAN_T", 1);   // higher prio than DON_T_AREN_T
    id2prio.put("THE_US", 1);   // higher prio than DT_PRP
    id2prio.put("THE_IT", 1);   // higher prio than DT_PRP
    id2prio.put("THANK_YOU_MUCH", 1);   // higher prio than other rules
    id2prio.put("TO_DO_HYPHEN", 1);   // higher prio than other rules
    id2prio.put("A_NUMBER_NNS", 1);   // higher prio than A_NNS
    id2prio.put("A_HUNDREDS", 1);   // higher prio than A_NNS
    id2prio.put("NOW_A_DAYS", 1);   // higher prio than A_NNS
    id2prio.put("COUPLE_OF_TIMES", 1);   // higher prio than A_NNS
    id2prio.put("A_WINDOWS", 1);   // higher prio than A_NNS
    id2prio.put("A_SCISSOR", 1);   // higher prio than A_NNS
    id2prio.put("A_SNICKERS", 1);   // higher prio than A_NNS
    id2prio.put("A_NNS_BEST_NN", 1);   // higher prio than A_NNS
    id2prio.put("BACHELORS", 1);   // higher prio than A_NNS
    id2prio.put("WERE_WEAR", 1);   // higher prio than agreement rules
    id2prio.put("NEITHER_NOR", 1);   // higher prio than COMMA_COMPOUND_SENTENCE
    id2prio.put("FOR_AWHILE", 1);   // higher prio than COMMA_COMPOUND_SENTENCE
    id2prio.put("A_BUT", 1);   // higher prio than COMMA_COMPOUND_SENTENCE
    id2prio.put("BORN_IN", 1);   // higher prio than PRP_PAST_PART
    id2prio.put("DO_TO", 1);   // higher prio than HAVE_PART_AGREEMENT
    id2prio.put("CURIOS_CURIOUS", 1);   // higher prio than A_NNS and POSSESSIVE_APOSTROPHE
    id2prio.put("INCORRECT_POSSESSIVE_APOSTROPHE", 1);  // higher prio than THIS_NNS
    id2prio.put("THIS_YEARS_POSSESSIVE_APOSTROPHE", 1);  // higher prio than THIS_NNS
    id2prio.put("SPURIOUS_APOSTROPHE", 1);   // higher prio than THIS_NNS
    id2prio.put("BE_NOT_BE_JJ", 1);   // higher prio than BEEN_PART_AGREEMENT
    id2prio.put("IN_THIS_REGARDS", 1);   // higher prio than THIS_NNS
    id2prio.put("IT_SEAMS", 1);   // higher prio than THIS_NNS_VB
    id2prio.put("NO_WHERE", 1);   // higher prio than NOW
    id2prio.put("APOSTROPHE_VS_QUOTE", 1);   // higher prio than EN_QUOTES
    id2prio.put("ALL_OF_SUDDEN", 1);   // higher prio than ALL_MOST_SOME_OF_NOUN
    id2prio.put("COMMA_PERIOD", 1);   // higher prio than COMMA_PARENTHESIS_WHITESPACE
    id2prio.put("COMMA_CLOSING_PARENTHESIS", 1);   // higher prio than COMMA_PARENTHESIS_WHITESPACE
    id2prio.put("ELLIPSIS", 1);   // higher prio than COMMA_PARENTHESIS_WHITESPACE
    id2prio.put("HERE_HEAR", 1);   // higher prio than ENGLISH_WORD_REPEAT_RULE
    id2prio.put("MISSING_POSS_APOS", 1);   // higher prio than SINGULAR_NOUN_VERB_AGREEMENT
    id2prio.put("DO_HE_VERB", 1);   // prefer over HE_VERB_AGR
    id2prio.put("LIGATURES", 1);   // prefer over spell checker
    id2prio.put("APPSTORE", 1);   // prefer over spell checker
    id2prio.put("INCORRECT_CONTRACTIONS", 1);   // prefer over EN_CONTRACTION_SPELLING
    id2prio.put("DONT_T", 1);   // prefer over EN_CONTRACTION_SPELLING
    id2prio.put("WHATS_APP", 1);   // prefer over EN_CONTRACTION_SPELLING
    id2prio.put("NON_STANDARD_COMMA", 1);   // prefer over spell checker
    id2prio.put("NON_ENGLISH_CHARACTER_IN_A_WORD", 1);  // prefer over spell checker
    id2prio.put("WONT_CONTRACTION", 1);   // prefer over WONT_WANT
    id2prio.put("THAN_THANK", 1);   // prefer over THAN_THEN
    id2prio.put("SPURIOUS_APOSTROPHE", 1);   // prefer over CD_NN and LOWERCASE_NAME_APOSTROPHE_S
    id2prio.put("IT_IF", 1);   // needs higher prio than PRP_COMMA and IF_YOU_ANY
    id2prio.put("FINE_TUNE_COMPOUNDS", 1);   // prefer over less specific rules
    id2prio.put("WHAT_IS_YOU", 1);   // prefer over HOW_DO_I_VB, NON3PRS_VERB
    id2prio.put("SUPPOSE_TO", 1);   // prefer over HOW_DO_I_VB and I_AM_VB and ARE_WE_HAVE
    id2prio.put("CONFUSION_GONG_GOING", 1);   // prefer over I_AM_VB
    id2prio.put("SEEN_SEEM", 1);   // prefer over PRP_PAST_PART
    id2prio.put("PROFANITY", 1);   // prefer over spell checker (less prio than EN_COMPOUNDS)
    id2prio.put("PROFANITY_XML", 1);
    id2prio.put("GOOD_FLUCK", 2);   // prefer over PROFANITY
    id2prio.put("PROFANITY_TYPOS", 2);   // prefer over PROFANITY
    id2prio.put("THE_THEM", 1);   // prefer over TO_TWO
    id2prio.put("THERE_THEIR", 1);   // prefer over GO_TO_HOME
    id2prio.put("TO_WORRIED_ABOUT", 1);   // higher prio than TO_TOO
    id2prio.put("IT_IS_DEPENDING_ON", 1);   // prefer over PROGRESSIVE_VERBS
    id2prio.put("TO_NIGHT_TO_DAY", 1);   // prefer over TOO_JJ_TO
    id2prio.put("IRREGARDLESS", 1);   // prefer over spell checker
    id2prio.put("MD_APOSTROPHE_VB", 1);   // prefer over typography rules
    id2prio.put("ULTRA_HYPHEN", 1);   // prefer over EN_COMPOUND_ADJECTIVE_INTERNAL
    id2prio.put("THINK_BELIEVE_THAT", 1);
    id2prio.put("HAS_TO_APPROVED_BY", 1);   // prefer over TO_NON_BASE
    id2prio.put("MD_VBD", -1);  // prefer LOWERCASE_MONTHS
    id2prio.put("PRP_PRP", -1);  // prefer other rules that give a suggestion
    id2prio.put("IS_LIKELY_TO_BE", -1);  // give IS_RB_BE precedence (more suggestions)
    id2prio.put("WANNA", 1);   // prefer over spell checker
    id2prio.put("LOOK_FORWARD_TO", 1);   // prefer over LOOK_FORWARD_NOT_FOLLOWED_BY_TO
    id2prio.put("LOOK_SLIKE", 1);   // higher prio than prem:SINGULAR_NOUN_VERB_AGREEMENT
    id2prio.put("A3FT", 1);   // higher prio than NUMBERS_IN_WORDS
    id2prio.put("HYPHEN_TO_EN", 1);   // higher prio than DASH_RULE (due to one picky subrule)
    id2prio.put("EVERY_NOW_AND_THEN", 0);
    id2prio.put("EN_DIACRITICS_REPLACE", -1);   // prefer over spell checker, less prio than ATTACHE_ATTACH
    id2prio.put("MISSING_COMMA_BETWEEN_DAY_AND_YEAR", -1);   // less priority than DATE_WEEKDAY
    id2prio.put("FASTLY", -1);   // higher prio than spell checker
    id2prio.put("WHO_NOUN", -1);   // prefer SPECIFIC_CASE
    id2prio.put("ANYWAYS", -1);   // higher prio than spell checker
    id2prio.put("MISSING_GENITIVE", -1);  // prefer over spell checker (like EN_SPECIFIC_CASE)
    id2prio.put("EN_UNPAIRED_BRACKETS", -1);  // less priority than rules that suggest the correct brackets
    id2prio.put("WAKED_UP", -1);  // less priority than other grammar rules
    id2prio.put("NEEDS_FIXED", -1);  // less priority than MISSING_TO_BEFORE_A_VERB
    id2prio.put("SENT_START_NNP_COMMA", -1);  // prefer other more specific rules
    id2prio.put("SENT_START_NN_DT", -1);  // prefer MISSING_PREPOSITION
    id2prio.put("DT_PDT", -1);  // prefer other more specific rules
    id2prio.put("MD_VB_AND_NOTVB", -1);  // prefer other more specific rules
    id2prio.put("BLACK_SEA", -1);  // less priority than SEA_COMPOUNDS
    id2prio.put("A_TO", -1);  // less priority than other rules that offer suggestions
    id2prio.put("MANY_NN", -1);  // less priority than PUSH_UP_HYPHEN, SOME_FACULTY
    id2prio.put("WE_BE", -1);
    id2prio.put("A_LOT_OF_NN", -1);
    id2prio.put("REPETITIONS_STYLE", -51);  // repetition style rules, usually with prefix REP_
    id2prio.put("ORDER_OF_WORDS_WITH_NOT", -1);  // less prio than punctuation rules
    id2prio.put("ADVERB_WORD_ORDER_10_TEMP", 1);
    id2prio.put("ADVERB_WORD_ORDER", -1);  // less prio than PRP_PAST_PART //
    id2prio.put("HAVE_VB_DT", -1);
    id2prio.put("MD_PRP", -1);  // prefer ME_BE
    id2prio.put("IT_IS_2", -1);  // needs higher prio than BEEN_PART_AGREEMENT
    id2prio.put("A_RB_NN", -1);  // prefer other more specific rules (e.g. QUIET_QUITE, A_QUITE_WHILE)
    id2prio.put("DT_RB_IN", -1);  // prefer other more specific rules
    id2prio.put("VERB_NOUN_CONFUSION", -1);  // prefer other more specific rules
    id2prio.put("NOUN_VERB_CONFUSION", -1);  // prefer other more specific rules
    id2prio.put("PLURAL_VERB_AFTER_THIS", -1);  // prefer other more specific rules (e.g. COMMA_TAG_QUESTION)
    id2prio.put("BE_RB_BE", -1);  // prefer other more specific rules
    id2prio.put("IT_ITS", -1);  // prefer other more specific rules
    id2prio.put("ENGLISH_WORD_REPEAT_RULE", -1);  // prefer other more specific rules (e.g. IT_IT)
    id2prio.put("DT_JJ_NO_NOUN", -1);  // prefer other more specific rules (e.g. THIRD_PARTY)
    id2prio.put("AGREEMENT_SENT_START", -1);  // prefer other more specific rules
    id2prio.put("PREPOSITION_VERB", -1);  // prefer other more specific rules
    id2prio.put("EN_A_VS_AN", -1);  // prefer other more specific rules (with suggestions, e.g. AN_ALSO)
    id2prio.put("CD_NN", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("CD_NNU", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("ATD_VERBS_TO_COLLOCATION", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("ORDINAL_NUMBER_MISSING_ORDINAL_INDICATOR", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("ADVERB_OR_HYPHENATED_ADJECTIVE", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("GOING_TO_VBD", -1);  // prefer other more specific rules (with suggestions, e.g. GOING_TO_JJ)
    id2prio.put("MISSING_PREPOSITION", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("CHARACTER_APOSTROPHE_WORD", -1);  // prefer other more specific rules
    id2prio.put("SINGLE_CHARACTER", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("BE_TO_VBG", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("NON3PRS_VERB", -1);  // prefer other more specific rules (with suggestions, e.g. DONS_T)
    id2prio.put("DT_NN_VBG", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("NNS_THAT_ARE_JJ", -1);  // prefer other more specific rules
    id2prio.put("DID_FOUND_AMBIGUOUS", -1);  // prefer other more specific rules (e.g. TWO_CONNECTED_MODAL_VERBS)
    id2prio.put("BE_I_BE_GERUND", -1);  // prefer other more specific rules (with suggestions)
    id2prio.put("VBZ_VBD", -1);  // prefer other more specific rules (e.g. IS_WAS)
    id2prio.put("SUPERLATIVE_THAN", -1);  // prefer other more specific rules
    id2prio.put("UNLIKELY_OPENING_PUNCTUATION", -1);  // prefer other more specific rules
    id2prio.put("MD_DT_JJ", -1);  // prefer other more specific rules
    id2prio.put("I_IF", -1);  // prefer other more specific rules
    id2prio.put("NOUNPHRASE_VB_RB_DT", -1);  // prefer other more specific rules
    id2prio.put("SENT_START_NN_NN_VB", -1);  // prefer other more specific rules
    id2prio.put("VB_A_JJ_NNS", -1);  // prefer other more specific rules (e.g. A_NNS)
    id2prio.put("DUPLICATION_OF_IS_VBZ", -1);  // prefer other more specific rules (e.g. A_NNS)
    id2prio.put("METRIC_UNITS_EN_IMPERIAL", -1);  // prefer MILE_HYPHEN
    id2prio.put("IF_THEN_COMMA", -1);  // prefer CONFUSION_OF_THEN_THAN
    id2prio.put("COMMA_COMPOUND_SENTENCE", -1);  // prefer other rules
    id2prio.put("COMMA_COMPOUND_SENTENCE_2", -1);  // prefer other rules
    id2prio.put("BE_VBG_BE", -1);  // prefer other more specific rules
    id2prio.put("PRP_VB_VB", -1);  // prefer other more specific rules
    id2prio.put("FOR_ANY_CLARIFICATIONS", -1);  // prefer SENT_START_ALL_CAPITALS and ALL_UPPERCASE
    id2prio.put("PLEASE_LET_ME_KNOW", -1);  // prefer SENT_START_ALL_CAPITALS and ALL_UPPERCASE
    id2prio.put("UNNECESSARY_CAPITALIZATION", -1);  // prefer other more specific rules
    id2prio.put("CONFUSION_OF_A_JJ_NNP_NNS_PRP", -1);  // prefer other more specific rules
    id2prio.put("PLURALITY_CONFUSION_OF_NNS_OF_NN", -1); // prefer several compound rules
    id2prio.put("NP_TO_IS", -1);  // prefer other more specific rules
    id2prio.put("REPEATED_VERBS", -1);  // prefer other rules
    id2prio.put("NNP_COMMA_QUESTION", -2);  // prefer other more specific rules
    id2prio.put("THE_CC", -2);  // prefer other more specific rules (with suggestions)
    id2prio.put("PRP_VBG", -2);  // prefer other more specific rules (with suggestions, prefer over HE_VERB_AGR)
    id2prio.put("CANT_JJ", -2);  // prefer other more specific rules
    id2prio.put("WOULD_A", -2);  // prefer other more specific rules
    id2prio.put("I_AM_VB", -2);  // prefer other rules
    id2prio.put("VBP_VBP", -2);  // prefer more specific rules
    id2prio.put("GONNA_TEMP", -3);
    id2prio.put("A_INFINITIVE", -3);  // prefer other more specific rules (with suggestions, e.g. PREPOSITION_VERB, THE_TO)
    id2prio.put("INDIAN_ENGLISH", -3);  // prefer grammar rules, but higher prio than spell checker
    id2prio.put("DO_PRP_NOTVB", -3);  // prefer other more specific rules (e.g. HOW_DO_I_VB)
    id2prio.put("GONNA", -4);  // prefer over spelling rules
    id2prio.put("WHATCHA", -4);  // prefer over spelling rules
    id2prio.put("DONTCHA", -4);  // prefer over spelling rules
    id2prio.put("GOTCHA", -4);  // prefer over spelling rules
    id2prio.put("OUTTA", -4);  // prefer over spelling rules
    id2prio.put("Y_ALL", -4);  // prefer over spelling rules
    id2prio.put("GIMME", -4);  // prefer over spelling rules
    id2prio.put("LEMME", -4);  // prefer over spelling rules
    id2prio.put("ID_CASING", -4);  // prefer over spelling rules but not over ID_IS
    id2prio.put("EN_GB_SIMPLE_REPLACE", -5);  // higher prio than Speller
    id2prio.put("EN_US_SIMPLE_REPLACE", -5);  // higher prio than Speller
    id2prio.put("MORFOLOGIK_RULE_EN_US", -10);  // more specific rules (e.g. L2 rules) have priority
    id2prio.put("MORFOLOGIK_RULE_EN_GB", -10);  // more specific rules (e.g. L2 rules) have priority
    id2prio.put("MORFOLOGIK_RULE_EN_CA", -10);  // more specific rules (e.g. L2 rules) have priority
    id2prio.put("MORFOLOGIK_RULE_EN_ZA", -10);  // more specific rules (e.g. L2 rules) have priority
    id2prio.put("MORFOLOGIK_RULE_EN_NZ", -10);  // more specific rules (e.g. L2 rules) have priority
    id2prio.put("MORFOLOGIK_RULE_EN_AU", -10);  // more specific rules (e.g. L2 rules) have priority
    id2prio.put("MD_PRP_QUESTION_MARK", -11);  // speller needs higher priority
    id2prio.put("PRP_RB_NO_VB", -12);  // prefer other more specific rules (with suggestions)
    id2prio.put("MD_JJ", -12);  // prefer other rules (e.g. NOUN_VERB_CONFUSION)
    id2prio.put("HE_VERB_AGR", -12);  // prefer other more specific rules (e.g. AI models, PRP_VBG)
    id2prio.put("MD_BASEFORM", -12);  // prefer other more specific rules (e.g. AI models)
    id2prio.put("IT_VBZ", -12);  // prefer other more specific rules (e.g. AI models)
    id2prio.put("PRP_THE", -12);  // prefer other rules (e.g. AI models, I_A, PRP_JJ, IF_YOU_ANY, I_AN)
    id2prio.put("PRP_JJ", -12);  // prefer other rules (e.g. AI models, PRP_VBG, IT_IT and ADJECTIVE_ADVERB, PRP_ABLE, PRP_NEW, MD_IT_JJ)
    id2prio.put("SINGULAR_NOUN_VERB_AGREEMENT", -12);  // prefer other rules (e.g. AI models, PRP_VBG, IT_IT and ADJECTIVE_ADVERB, PRP_ABLE, PRP_NEW, MD_IT_JJ)
    id2prio.put("SINGULAR_AGREEMENT_SENT_START", -12);    // prefer AI
    id2prio.put("VB_TO_NN_DT", -12);  // prefer AI and other more specific rules (e.g. NOUN_VERB_CONFUSION)
    id2prio.put("SUBJECTVERBAGREEMENT_2", -12);    // prefer AI
    id2prio.put("THE_SENT_END", -12);    // prefer AI
    id2prio.put("DT_NN_ARE_AME", -12);    // prefer AI
    id2prio.put("COLLECTIVE_NOUN_VERB_AGREEMENT_VBP", -12);    // prefer AI
    id2prio.put("SUBJECT_VERB_AGREEMENT", -12);    // prefer AI
    id2prio.put("VERB_APOSTROPHE_S", -12);    // prefer AI
    id2prio.put("WHERE_MD_VB", -12);    // prefer AI
    id2prio.put("SENT_START_PRPS_JJ_NN_VBP", -12);  // prefer AI
    id2prio.put("TO_AFTER_MODAL_VERBS", -12);  // prefer AI
    id2prio.put("SINGULAR_NOUN_ADV_AGREEMENT", -12);  // prefer AI
    id2prio.put("BE_VBP_IN", -12);  // prefer over BEEN_PART_AGREEMENT but not over AI_EN_LECTOR
    id2prio.put("BE_VBG_NN", -12);  // prefer other more specific rules and speller
    id2prio.put("THE_NNS_NN_IS", -12);  // prefer HYDRA_LEO
    id2prio.put("IF_DT_NN_VBZ", -12);  // prefer HYDRA_LEO and lector
    id2prio.put("PRP_MD_NN", -12);  // prefer other more specific rules (e.g. MD_ABLE, WONT_WANT)
    id2prio.put("HAVE_PART_AGREEMENT", -13);  // prefer HYDRA_LEO and lector
    id2prio.put("BEEN_PART_AGREEMENT", -13);  // prefer HYDRA_LEO and lector
    id2prio.put("BE_WITH_WRONG_VERB_FORM", -14);  // prefer HYDRA_LEO, BEEN_PART_AGREEMENT and other rules
    id2prio.put("TWO_CONNECTED_MODAL_VERBS", -15);
    id2prio.put("PRP_NO_ADVERB_VERB", -15);  // prefer other more specific rules (e.g. PRP_VBG, IT_ITS, ...)
    id2prio.put("MISSING_TO_BETWEEN_BE_AND_VB", -15); // prefer AI and comma rules
    id2prio.put("IN_DT_IN", -15); // prefer AI and comma rules
    id2prio.put("MISSING_SUBJECT", -15);  // prefer other more specific rules
    id2prio.put("HAVE_TO_NOTVB", -15); // prefer AI and comma rules
    id2prio.put("PLEASE_DO_NOT_THE_CAT", -15); // prefer AI and comma rules
    id2prio.put("VB_TO_JJ", -15); // prefer AI and comma rules
    id2prio.put("CC_PRP_ARTICLE", -15);  // prefer other more specific rules
    id2prio.put("BE_MD", -20);  // prefer other more specific rules (e.g. BEEN_PART_AGREEMENT, HYDRA_LEO)
    id2prio.put("POSSESSIVE_APOSTROPHE", -10);  // prefer over AI_HYDRA_LEO_APOSTROPHE_S_XS (again, temporarily)
    id2prio.put("WANT_TO_NN", -25);  // prefer more specific rules that give a suggestion
    id2prio.put("QUESTION_WITHOUT_VERB", -25);  // prefer more specific rules that give a suggestion
    id2prio.put("PRP_VB", -25);  // prefer other rules (with suggestions, e.g. confusion rules)
    id2prio.put("PRP_VB_NN", -25);  // prefer other more specific rules (e.g. HYDRA_LEO)
    id2prio.put("BE_NN", -26);  // prefer other more specific rules (e.g. PRP_VB_NN, BEEN_PART_AGREEMENT, HYDRA_LEO)
    id2prio.put("BE_VB_OR_NN", -26);  // prefer other more specific rules (e.g. PRP_VB_NN, BE_MD, BEEN_PART_AGREEMENT, HYDRA_LEO)
    id2prio.put("DO_DT_NN_BE", -26);  // prefer other more specific rules (e.g. PRP_VB_NN, BE_MD, BEEN_PART_AGREEMENT, HYDRA_LEO)
    id2prio.put("PRONOUN_NOUN", -26);  // prefer other rules (with suggestions, e.g. confusion rules)
    id2prio.put("ETC_PERIOD", -49);  // prefer over QB rules that are now style
    id2prio.put("COULD_YOU_NOT_NEEDED", -49);  // prefer over TAKE_A_LOOK
    id2prio.put("SENTENCE_FRAGMENT", -50);  // prefer other more important sentence start corrections.
    id2prio.put("SENTENCE_FRAGMENT", -51);  // prefer other more important sentence start corrections.
    id2prio.put("SEEMS_TO_BE", -51);  // prefer SEEM_APPEAR
    id2prio.put("MD_NN", -60);  // prefer PRP_MD_NN
    id2prio.put("I_THINK_FEEL", -60);
    id2prio.put("KNOW_AWARE_REDO", -60);
    id2prio.put("EN_REDUNDANCY_REPLACE", -510);  // style rules should always have the lowest priority.
    id2prio.put("EN_PLAIN_ENGLISH_REPLACE", -511);  // style rules should always have the lowest priority.
    id2prio.put("REP_PASSIVE_VOICE", -599);  // higher prio than PASSIVE_VOICE for testing purposes, but lower than other style rules
    id2prio.put("FOUR_NN", -599);  // higher prio than THREE_NN for testing purposes, but lower than other style rules
    id2prio.put("THREE_NN", -600);  // style rules should always have the lowest priority.
    id2prio.put("SENT_START_NUM", -600);  // style rules should always have the lowest priority.
    id2prio.put("PASSIVE_VOICE", -600);  // style rules should always have the lowest priority.
    id2prio.put("EG_NO_COMMA", -600);  // style rules should always have the lowest priority.
    id2prio.put("IE_NO_COMMA", -600);  // style rules should always have the lowest priority.
    id2prio.put("REASON_WHY", -600);  // style rules should always have the lowest priority.
    id2prio.put(LongSentenceRule.RULE_ID, -997);
    id2prio.put(LongParagraphRule.RULE_ID, -998);
    id2prio.put("ALL_UPPERCASE", -1000);  // do not hide spelling and grammar issues, when text is all upper case	  
  }

  @Override
  public Map<String, Integer> getPriorityMap() {
    return id2prio;
  }

  @Override
  protected int getPriorityForId(String id) {
    Integer prio = id2prio.get(id);
    if (prio != null) {
      return prio;
    }
    if (id.startsWith("EN_COMPOUNDS_")) {
      return 2;
    }
    if (id.equals("PRP_VBZ")) {
      return -2; // prefer other more specific rules (with suggestions)
    }
    if (id.startsWith("CONFUSION_RULE_")) {
      return -20;
    }
    if (id.equals("EN_UPPER_CASE_NGRAM")) {
      return -12; // prefer other more specific rules (e.g. AI models)
    }
    if (id.startsWith("AI_SPELLING_RULE")) {
      return -9; // higher than MORFOLOGIK_*, for testing
    }
    if (id.startsWith("EN_MULTITOKEN_SPELLING_")) {
      return -9; // higher than MORFOLOGIK_*
    }
    if (id.equals("QB_EN_OXFORD")) {
      return -51; // MISSING_COMMA_AFTER_YEAR
    }
    if (id.startsWith("AI_HYDRA_LEO")) { // prefer more specific rules (also speller)
      if (id.equals("AI_HYDRA_LEO_MISSING_COMMA")) {
        return -51; // prefer comma style rules.
      }
      if (id.startsWith("AI_HYDRA_LEO_CP_YOU_YOUARE")) {
        return -1;
      }
      if (id.startsWith("AI_HYDRA_LEO_CP")) {
        return 2;
      }
      if (id.startsWith("AI_HYDRA_LEO_MISSING_TO")) {
        return -14; // prefer lector, HAVE_PART_AGREEMENT and BEEN_PART_AGREEMENT
      }
      return -11;
    }
    if (id.startsWith("AI_EN_LECTOR")) { // prefer more specific rules (also speller)
      return -11;
    }
    if (FALSE_FRIENDS_PATTERN.matcher(id).matches()) {
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

  public boolean hasMinMatchesRules() {
    return true;
  }
  
  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
      return new MorfologikAmericanSpellerRule(messages, this, null, Collections.emptyList());
  }
  
  @Override
  public List<RuleMatch> adaptSuggestions(List<RuleMatch> ruleMatches, Set<String> enabledRules) {
    List<RuleMatch> newRuleMatches = new ArrayList<>();
    for (RuleMatch rm : ruleMatches) {
      String errorStr = rm.getOriginalErrorStr();
      List<SuggestedReplacement> suggestedReplacements = rm.getSuggestedReplacementObjects();
      List<SuggestedReplacement> newReplacements = new ArrayList<>();
      for (SuggestedReplacement suggestedReplacement : suggestedReplacements) {
        String newReplStr = suggestedReplacement.getReplacement();
        if (errorStr.length() > 2) {
          // add a whitespace when the error is in a contraction and the suggestion is not
          if (errorStr.startsWith("'") && !newReplStr.startsWith("'") && !newReplStr.startsWith("’")
              && !newReplStr.startsWith(" ")) {
            newReplStr = " " + newReplStr;
          }
          if (errorStr.startsWith("n't") && !newReplStr.startsWith("n't") && !newReplStr.startsWith("n’t")) {
            newReplStr = " " + newReplStr;
          }
        }
        SuggestedReplacement newSuggestedReplacement = new SuggestedReplacement(suggestedReplacement);
        newSuggestedReplacement.setReplacement(newReplStr);
        if (!newReplacements.contains(newSuggestedReplacement)) {
          newReplacements.add(newSuggestedReplacement);
        }
      }
      RuleMatch newMatch = new RuleMatch(rm, newReplacements);
      newRuleMatches.add(newMatch);
    }
    return newRuleMatches;
  }

  @Override
  public List<String> prepareLineForSpeller(String line) {
    String[] parts = line.split("#");
    if (parts.length == 0) {
      return Arrays.asList(line);
    }
    if (line.contains("+")) {
      // while the morfologik separator is "+", multiwords with '+' can cause undesired results.
      return Arrays.asList("");
    }
    String[] formTag = parts[0].split("\t");
    String form = formTag[0].trim();
    if (formTag.length > 1) {
      String tag = formTag[1].trim();
      if (tag.startsWith("NN") || tag.startsWith("JJ")) {
        return Arrays.asList(form);
      } else {
        return Arrays.asList("");
      }
    }
    return Arrays.asList(line);
  }

  public MultitokenSpeller getMultitokenSpeller() {
    return EnglishMultitokenSpeller.INSTANCE;
  }

}
