/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Milkowski (http://www.languagetool.org)
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
package org.languagetool.rules.spelling;

import com.google.common.base.Strings;
import gnu.trove.THashSet;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.rules.spelling.suggestions.*;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An abstract rule for spellchecking rules.
 *
 * @author Marcin Miłkowski
 */
public abstract class SpellingCheckRule extends Rule {

  /**
   * The confidence value for a suggestion with high confidence. Not 1.0, as even with a high confidence,
   * we might still be wrong.
   */
  public static final float HIGH_CONFIDENCE = 0.99f;

  /**
   * The string {@code LanguageTool}.
   * @since 2.3
   */
  public static final String LANGUAGETOOL = "LanguageTool";
  /**
   * The string {@code LanguageTooler}.
   * @since 4.4
   */
  public static final String LANGUAGETOOLER = "LanguageTooler";
  public static final int MAX_TOKEN_LENGTH = 200;

  protected final Language language;

  /**
   * @since 4.5
   * For rules from @see Language.getRelevantLanguageModelCapableRules
   * Optional, allows e.g. better suggestions when set
   */
  @Nullable
  protected LanguageModel languageModel;
  protected final CachingWordListLoader wordListLoader = new CachingWordListLoader();

  private static final String SPELLING_IGNORE_FILE = "/hunspell/ignore.txt";
  private static final String SPELLING_FILE = "/hunspell/spelling.txt";
  private static final String CUSTOM_SPELLING_FILE = "/hunspell/spelling_custom.txt";
  private static final String GLOBAL_SPELLING_FILE = "spelling_global.txt";
  private static final String SPELLING_PROHIBIT_FILE = "/hunspell/prohibit.txt";
  private static final String CUSTOM_SPELLING_PROHIBIT_FILE = "/hunspell/prohibit_custom.txt";
  private static final String SPELLING_FILE_VARIANT = null;

  private final Set<String> wordsToBeProhibited = new THashSet<>();

  private volatile String[] wordsToBeIgnoredDictionary = null;
  private volatile String[] wordsToBeIgnoredDictionaryIgnoreCase = null;

  private List<DisambiguationPatternRule> antiPatterns = new ArrayList<>();
  private boolean considerIgnoreWords = true;
  private boolean convertsCase = false;
  protected final Set<String> wordsToBeIgnored = new THashSet<>();
  protected int ignoreWordsWithLength = 0;
  
  private final Pattern pHasNoLetterLatin = Pattern.compile("^[^\\p{script=latin}]+$");
  private final Pattern pHasNoLetter = Pattern.compile("^[^\\p{L}]+$");

  public SpellingCheckRule(ResourceBundle messages, Language language, UserConfig userConfig) {
    this(messages, language, userConfig, Collections.emptyList());
  }

  /**
   * @since 4.4
   */
  public SpellingCheckRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) {
    this(messages, language, userConfig, altLanguages, null);
  }

  /**
   * @since 4.5
   */
  public SpellingCheckRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages, @Nullable LanguageModel languageModel) {
    super(messages);
    this.language = language;
    this.languageModel = languageModel;
    if (userConfig != null) {
      wordsToBeIgnored.addAll(userConfig.getAcceptedWords());
    }
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  /**
   * @param word misspelled word that suggestions should be generated for
   * @param userCandidatesList candidates from personal dictionary
   * @param candidatesList candidates from default dictionary
   * @param orderer model to rank suggestions / extract features, or null
   * @param match rule match to add suggestions to
   */
  protected static void addSuggestionsToRuleMatch(String word, List<SuggestedReplacement> userCandidatesList, List<SuggestedReplacement> candidatesList,
                                                  @Nullable SuggestionsOrderer orderer, RuleMatch match) {
    AnalyzedSentence sentence = match.getSentence();
    List<String> userCandidates = userCandidatesList.stream().map(SuggestedReplacement::getReplacement).collect(Collectors.toList());
    List<String> candidates = candidatesList.stream().map(SuggestedReplacement::getReplacement).collect(Collectors.toList());
    int startPos = match.getFromPos();
    //long startTime = System.currentTimeMillis();
    if (orderer != null && orderer.isMlAvailable()) {
      if (orderer instanceof SuggestionsRanker) {
        // don't rank words form user dictionary, assign confidence 0.0, but add at start
        // hard to ensure performance on unknown words
        SuggestionsRanker ranker = (SuggestionsRanker) orderer;
        List<SuggestedReplacement> defaultSuggestions = ranker.orderSuggestions(
          candidates, word, sentence, startPos);
        if (defaultSuggestions.isEmpty()) {
          // could not rank for some reason
        } else {
          if (userCandidates.isEmpty()) {
            match.setAutoCorrect(ranker.shouldAutoCorrect(defaultSuggestions));
            match.setSuggestedReplacementObjects(defaultSuggestions);
          } else {
            List<SuggestedReplacement> combinedSuggestions = new ArrayList<>();
            for (String wordFromUserDict : userCandidates) {
              SuggestedReplacement s = new SuggestedReplacement(wordFromUserDict);
              // confidence is null
              combinedSuggestions.add(s);
            }
            combinedSuggestions.addAll(defaultSuggestions);
            match.setSuggestedReplacementObjects(combinedSuggestions);
            // no auto correct when words from personal dictionaries are included
            match.setAutoCorrect(false);
          }
        }
      } else if (orderer instanceof SuggestionsOrdererFeatureExtractor) {
        // disable user suggestions here
        // problem: how to merge match features when ranking default and user suggestions separately?
        if (userCandidates.size() != 0) {
          throw new IllegalStateException(
            "SuggestionsOrdererFeatureExtractor does not support suggestions from personal dictionaries at the moment.");
        }
        SuggestionsOrdererFeatureExtractor featureExtractor = (SuggestionsOrdererFeatureExtractor) orderer;
        Pair<List<SuggestedReplacement>, SortedMap<String, Float>> suggestions =
          featureExtractor.computeFeatures(candidates, word, sentence, startPos);

        match.setSuggestedReplacementObjects(suggestions.getLeft());
        match.setFeatures(suggestions.getRight());
      } else {
        List<SuggestedReplacement> combinedSuggestions = new ArrayList<>();
        combinedSuggestions.addAll(orderer.orderSuggestions(userCandidates, word, sentence, startPos));
        combinedSuggestions.addAll(orderer.orderSuggestions(candidates, word, sentence, startPos));
        match.setSuggestedReplacementObjects(combinedSuggestions);
      }
    } else { // no reranking
      List<SuggestedReplacement> combinedSuggestions = new ArrayList<>(match.getSuggestedReplacementObjects());
      combinedSuggestions.addAll(userCandidatesList);
      combinedSuggestions.addAll(candidatesList);
      match.setSuggestedReplacementObjects(combinedSuggestions);
    }
    /*long timeDelta = System.currentTimeMillis() - startTime;
    System.out.printf("Reordering %d suggestions took %d ms.%n", result.getSuggestedReplacements().size(), timeDelta);*/
  }

  protected RuleMatch createWrongSplitMatch(AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar, int pos, String coveredWord, String suggestion1, String suggestion2, int prevPos) {
    if (ruleMatchesSoFar.size() > 0) {
      RuleMatch prevMatch = ruleMatchesSoFar.get(ruleMatchesSoFar.size() - 1);
      if (prevMatch.getFromPos() == prevPos) {
        // we'll later create a new match that covers the previous misspelled word and the current one:
        ruleMatchesSoFar.remove(ruleMatchesSoFar.size()-1);
      }
    }
    RuleMatch ruleMatch = new RuleMatch(this, sentence, prevPos, pos + coveredWord.length(),
            messages.getString("spelling"), messages.getString("desc_spelling_short"));
    ruleMatch.setSuggestedReplacement((suggestion1 + " " + suggestion2).trim());
    return ruleMatch;
  }

  @Override
  public abstract String getId();

  @Override
  public abstract String getDescription();

  @Override
  public abstract RuleMatch[] match(AnalyzedSentence sentence) throws IOException;

  /**
   * @since 4.8
   */
  @Experimental
  public abstract boolean isMisspelled(String word) throws IOException;

  @Override
  public boolean isDictionaryBasedSpellingRule() {
    return true;
  }

  /**
   * Add the given words to the list of words to be ignored during spell check.
   * You might want to use {@link #acceptPhrases(List)} instead, as only that
   * can also deal with phrases.
   */
  public void addIgnoreTokens(List<String> tokens) {
    wordsToBeIgnored.addAll(tokens);
  }

  private void updateIgnoredWordDictionary() {
    wordsToBeIgnoredDictionaryIgnoreCase = null;
    wordsToBeIgnoredDictionary = null;
  }

  /**
   * Set whether the list of words to be explicitly ignored (set with {@link #addIgnoreTokens(List)}) is considered at all.
   */
  public void setConsiderIgnoreWords(boolean considerIgnoreWords) {
    this.considerIgnoreWords = considerIgnoreWords;
  }

  /**
   * Get additional suggestions added before other suggestions (note the rule may choose to
   * re-order the suggestions anyway). Only add suggestions here that you know are spelled correctly,
   * they will not be checked again before being shown to the user.
   */
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word) throws IOException {
    List<String> moreSuggestions = new ArrayList<>();
    if (("Languagetool".equals(word) || "languagetool".equals(word)) && suggestions.stream().noneMatch(k -> k.getReplacement().equals(LANGUAGETOOL))) {
      moreSuggestions.add(LANGUAGETOOL);
    }
    if (("Languagetooler".equals(word) || "languagetooler".equals(word)) && suggestions.stream().noneMatch(k -> k.getReplacement().equals(LANGUAGETOOLER))) {
      moreSuggestions.add(LANGUAGETOOLER);
    }
    return SuggestedReplacement.convert(moreSuggestions);
  }

  /**
   * Get suggestions that will replace all other suggestions.
   * Only add suggestions here that you know are spelled correctly,
   * they will not be checked again before being shown to the user.
   */
  protected List<SuggestedReplacement> getOnlySuggestions(String word) {
    return Collections.emptyList();
  }

  /**
   * Get additional suggestions added after other suggestions (note the rule may choose to
   * re-order the suggestions anyway).
   */
  protected List<SuggestedReplacement> getAdditionalSuggestions(List<SuggestedReplacement> suggestions, String word) {
    return Collections.emptyList();
  }

  /**
   * Returns true iff the token at the given position should be ignored by the spell checker.
   */
  protected boolean ignoreToken(AnalyzedTokenReadings[] tokens, int idx) throws IOException {
    List<String> words = new ArrayList<>();
    for (AnalyzedTokenReadings token : tokens) {
      words.add(token.getToken());
    }
    return ignoreWord(words, idx);
  }

  /**
   * Returns true iff the word should be ignored by the spell checker.
   * If possible, use {@link #ignoreToken(AnalyzedTokenReadings[], int)} instead.
   */
  protected boolean ignoreWord(String word) throws IOException {
    if (word.length() > MAX_TOKEN_LENGTH) {
      return true;
    }
    if (!considerIgnoreWords) {
      return false;
    } 
    // Tokens with no letters cannot have spelling errors. So ignore them. 
    Matcher mHasNoLetter;
    if (isLatinScript()) {
      mHasNoLetter = pHasNoLetterLatin.matcher(word);
    } else {
      mHasNoLetter = pHasNoLetter.matcher(word);
    }
    if (mHasNoLetter.matches()) {
      return true;
    }
    if (word.endsWith(".") && !isInIgnoredSet(word)) {
      return isIgnoredNoCase(word.substring(0, word.length()-1));  // e.g. word at end of sentence
    }
    return isIgnoredNoCase(word);
  }

  protected boolean isInIgnoredSet(String word) {
    return wordsToBeIgnored.contains(word);
  }

  protected boolean isIgnoredNoCase(String word) {
    return isInIgnoredSet(word) ||
           (convertsCase && isInIgnoredSet(word.toLowerCase(language.getLocale()))) ||
           (ignoreWordsWithLength > 0 && word.length() <= ignoreWordsWithLength);
  }

  /**
   * Returns true iff the word at the given position should be ignored by the spell checker.
   * If possible, use {@link #ignoreToken(AnalyzedTokenReadings[], int)} instead.
   * @since 2.6
   */
  protected boolean ignoreWord(List<String> words, int idx) throws IOException {
    return ignoreWord(words.get(idx));
  }

  /**
   * Used to determine whether the dictionary will use case conversions for
   * spell checking.
   * @param convertsCase if true, then conversions are used.
   * @since 2.5
   */
  public void setConvertsCase(boolean convertsCase) {
    this.convertsCase = convertsCase;
  }


  protected static boolean isUrl(String token) {
    return WordTokenizer.isUrl(token);
  }

  protected static boolean isEMail(String token) {
    return WordTokenizer.isEMail(token);
  }

  protected static <T> List<T> filterDupes(List<T> words) {
    return words.stream().distinct().collect(Collectors.toList());
  }

  protected synchronized void init() throws IOException {
    for (String ignoreWord : wordListLoader.loadWords(getIgnoreFileName())) {
      addIgnoreWords(ignoreWord);
    }
    if (getSpellingFileName() != null) {
      for (String ignoreWord : wordListLoader.loadWords(getSpellingFileName())) {
        addIgnoreWords(ignoreWord);
      }
    }
    for (String fileName : getAdditionalSpellingFileNames()) {
      if (JLanguageTool.getDataBroker().resourceExists(fileName)) {
        for (String ignoreWord : wordListLoader.loadWords(fileName)) {
          addIgnoreWords(ignoreWord);
        }
      }
    }
    updateIgnoredWordDictionary();
    for (String prohibitedWord : wordListLoader.loadWords(getProhibitFileName())) {
      addProhibitedWords(expandLine(prohibitedWord));
    }
    for (String fileName : getAdditionalProhibitFileNames()) {
      for (String prohibitedWord : wordListLoader.loadWords(fileName)) {
        addProhibitedWords(expandLine(prohibitedWord));
      }
    }
  }

  /**
   * Get the name of the ignore file, which lists words to be accepted, even
   * when the spell checker would not accept them. Unlike with {@link #getSpellingFileName()}
   * the words in this file will not be used for creating suggestions for misspelled words.
   * @since 2.7
   */
  protected String getIgnoreFileName() {
    return language.getShortCode() + SPELLING_IGNORE_FILE;
  }

  /**
   * Get the name of the spelling file, which lists words to be accepted
   * and used for suggestions, even when the spell checker would not accept them.
   * @since 2.9, public since 3.5
   */
  public String getSpellingFileName() {
    return language.getShortCode() + SPELLING_FILE;
  }

  /**
   * Get the name of additional spelling file, which lists words to be accepted
   * and used for suggestions, even when the spell checker would not accept them.
   * @since 4.8
   */
  public List<String> getAdditionalSpellingFileNames() {
    // NOTE: also add to GermanSpellerRule.getSpeller() when adding items here:
    return Arrays.asList(language.getShortCode() + CUSTOM_SPELLING_FILE, GLOBAL_SPELLING_FILE);
  }

  /**
   * 
   * Get the name of the spelling file for a language variant (e.g., en-US or de-AT), 
   * which lists words to be accepted and used for suggestions, even when the spell
   * checker would not accept them.
   * @since 4.3
   */
  public String getLanguageVariantSpellingFileName() {
    return SPELLING_FILE_VARIANT;
  }

  /**
   * Get the name of the prohibit file, which lists words not to be accepted, even
   * when the spell checker would accept them.
   * @since 2.8
   */
  protected String getProhibitFileName() {
    return language.getShortCode() + SPELLING_PROHIBIT_FILE;
  }

  /**
   * Get the name of the prohibit file, which lists words not to be accepted, even
   * when the spell checker would accept them.
   * @since 2.8
   */
  protected List<String> getAdditionalProhibitFileNames() {
    return Collections.singletonList(language.getShortCode() + CUSTOM_SPELLING_PROHIBIT_FILE);
  }

  /**
   * Whether the word is prohibited, i.e. whether it should be marked as a spelling
   * error even if the spell checker would accept it. (This is useful to improve our spell
   * checker without waiting for the upstream checker to be updated.)
   * @since 2.8
   */
  protected boolean isProhibited(String word) {
    return wordsToBeProhibited.contains(word);
  }

  /**
   * Remove prohibited words from suggestions.
   * @since 2.8
   */
  protected List<SuggestedReplacement> filterSuggestions(List<SuggestedReplacement> suggestions) {
    suggestions.removeIf(suggestion -> isProhibited(suggestion.getReplacement()));
    List<SuggestedReplacement> newSuggestions = new ArrayList<>();
    for (SuggestedReplacement suggestion : suggestions) {
      String replacement = suggestion.getReplacement();
      String suggestionWithoutS = replacement.length() > 3 ? replacement.substring(0, replacement.length() - 2) : "";
      if (replacement.endsWith(" s") && isProperNoun(suggestionWithoutS)) {
        // "Michael s" -> "Michael's"
        //System.out.println("### " + suggestion + " => " + sentence.getText().replaceAll(suggestionWithoutS + "s", "**" + suggestionWithoutS + "s**"));
        SuggestedReplacement sugg1 = new SuggestedReplacement(suggestionWithoutS);
        sugg1.setType(SuggestedReplacement.SuggestionType.Curated);
        newSuggestions.add(0, sugg1);
        SuggestedReplacement sugg2 = new SuggestedReplacement(suggestionWithoutS + "'s");
        sugg2.setType(SuggestedReplacement.SuggestionType.Curated);
        newSuggestions.add(0, sugg2);
      } else {
        newSuggestions.add(suggestion);
      }
    }
    newSuggestions = filterDupes(newSuggestions);
    newSuggestions = filterNoSuggestWords(newSuggestions);
    return newSuggestions;
  }

  protected List<SuggestedReplacement> filterNoSuggestWords(List<SuggestedReplacement> l) {
    return l;
  }

  private boolean isProperNoun(String wordWithoutS) {
    try {
      List<AnalyzedTokenReadings> tags = language.getTagger().tag(Collections.singletonList(wordWithoutS));
      return tags.stream().anyMatch(k -> k.hasPosTag("NNP"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param line the line as read from {@code spelling.txt}.
   * @since 2.9, signature modified in 3.9
   */
  protected void addIgnoreWords(String line) {
    if (!tokenizeNewWords()) {
      wordsToBeIgnored.add(line);
    }
    else {
      // if line consists of several words (separated by " "), a DisambiguationPatternRule
      // will be created where each words serves as a case-sensitive and non-inflected PatternToken
      // so that the entire multi-word entry is ignored by the spell checker
      List<String> tokens = language.getWordTokenizer().tokenize(line);
      if (tokens.size() > 1) {
        //System.out.println("Tokenized multi-token: " + line);
        List<PatternToken> patternTokens = new ArrayList<>(tokens.size());
        for(String token : tokens) {
          if (token.trim().isEmpty()) {
            continue;
          }
          patternTokens.add(new PatternToken(token, true, false, false));
        }
        antiPatterns.add(new DisambiguationPatternRule("INTERNAL_ANTIPATTERN", "(no description)", language,
          patternTokens, null, null, DisambiguationPatternRule.DisambiguatorAction.IGNORE_SPELLING));
      } else {
        wordsToBeIgnored.add(line);
      }
    }
  }

  /**
   * @param words list of words to be prohibited.
   * @since 4.2
   */
  protected void addProhibitedWords(List<String> words) {
    wordsToBeProhibited.addAll(words);
  }

  /**
   * Expand suffixes in a line. By default, the line is not expanded.
   * Implementations might e.g. turn {@code bicycle/S} into {@code [bicycle, bicycles]}.
   * @since 3.0
   */
  protected List<String> expandLine(String line) {
    return Collections.singletonList(line);
  }

  /**
   * Accept (case-sensitively, unless at the start of a sentence) the given phrases even though they
   * are not in the built-in dictionary.
   * Use this to avoid false alarms on e.g. names and technical terms. Unlike {@link #addIgnoreTokens(List)}
   * this can deal with phrases. A way to call this is like this:
   * <code>rule.acceptPhrases(Arrays.asList("duodenal atresia"))</code>
   * This way, checking would not create an error for "duodenal atresia", but it would still
   * create and error for "duodenal" or "atresia" if they appear on their own.
   * @since 3.3
   */
  public void acceptPhrases(List<String> phrases) {
    List<List<PatternToken>> antiPatterns = new ArrayList<>();
    for (String phrase : phrases) {
      String[] parts = phrase.split(" ");
      List<PatternToken> patternTokens = new ArrayList<>();
      int i = 0;
      boolean startsLowercase = false;
      for (String part : parts) {
        if (i == 0 && !part.equals(StringTools.uppercaseFirstChar(part))) {
          startsLowercase = true;
        }
        patternTokens.add(new PatternTokenBuilder().csToken(part).build());
        i++;
      }
      antiPatterns.add(patternTokens);
      if (startsLowercase) {
        antiPatterns.add(getTokensForSentenceStart(parts));
      }
    }
    this.antiPatterns = makeAntiPatterns(antiPatterns, language);
  }

  private static List<PatternToken> getTokensForSentenceStart(String[] parts) {
    List<PatternToken> ucPatternTokens = new ArrayList<>();
    int j = 0;
    for (String part : parts) {
      if (j == 0) {
        // at sentence start, we also need to accept a phrase that starts with an uppercase char:
        String uppercased = StringTools.uppercaseFirstChar(part);
        ucPatternTokens.add(new PatternTokenBuilder().posRegex(JLanguageTool.SENTENCE_START_TAGNAME).build());
        ucPatternTokens.add(new PatternTokenBuilder().csToken(uppercased).build());
      } else {
        ucPatternTokens.add(new PatternTokenBuilder().csToken(part).build());
      }
      j++;
    }
    return ucPatternTokens;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns;
  }
  
  /**
   * Checks whether a <code>word</code> starts with an ignored word.
   * Note that a minimum <code>word</code>-length of 4 characters is expected.
   * (This is for better performance. Moreover, such short words are most likely contained in the dictionary.)
   * @param word - entire word
   * @param caseSensitive - determines whether the check is case-sensitive
   * @return length of the ignored word (i.e., return value is 0, if the word does not start with an ignored word).
   * If there are several matches from the set of ignored words, the length of the longest matching word is returned.
   * @since 3.5
   */
  protected int startsWithIgnoredWord(String word, boolean caseSensitive) {
    if (word.length() < 4) {
      return 0;
    }
    Comparator<String> comparator = caseSensitive ? Comparator.naturalOrder() : String.CASE_INSENSITIVE_ORDER;
    String[] array = caseSensitive ? wordsToBeIgnoredDictionary : wordsToBeIgnoredDictionaryIgnoreCase;
    if (array == null) {
      array = wordsToBeIgnored.stream().sorted(comparator).toArray(String[]::new);
      if (caseSensitive) {
        wordsToBeIgnoredDictionary = array;
      } else {
        wordsToBeIgnoredDictionaryIgnoreCase = array;
      }
    }

    while (!word.isEmpty()) {
      int result = Arrays.binarySearch(array, word, comparator);
      if (result >= 0) break;

      int prev = -result - 2;
      if (prev < 0) return 0;

      String commonPrefix = caseSensitive
                            ? Strings.commonPrefix(word, array[prev])
                            : Strings.commonPrefix(word.toLowerCase(Locale.ROOT), array[prev].toLowerCase(Locale.ROOT));
      assert commonPrefix.length() < word.length();
      word = caseSensitive ? commonPrefix : word.substring(0, commonPrefix.length());
    }
    return word.length();
  }
  
  // tokenize words from files spelling.txt, prohibit.txt...
  protected boolean tokenizeNewWords() {
    return true;
  }

  protected boolean isLatinScript() {
    return true;
  }

}
