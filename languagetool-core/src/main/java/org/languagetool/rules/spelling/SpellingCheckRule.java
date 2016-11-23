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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

/**
 * An abstract rule for spellchecking rules.
 *
 * @author Marcin Miłkowski
 */
public abstract class SpellingCheckRule extends Rule {

  /**
   * The string {@code LanguageTool}.
   * @since 2.3
   */
  public static final String LANGUAGETOOL = "LanguageTool";
  /**
   * The name of the LanguageTool Firefox extension, {@code LanguageToolFx}.
   * @since 2.3
   */
  public static final String LANGUAGETOOL_FX = "LanguageToolFx";

  protected final Language language;

  private static final String SPELLING_IGNORE_FILE = "/hunspell/ignore.txt";
  private static final String SPELLING_FILE = "/hunspell/spelling.txt";
  private static final String SPELLING_PROHIBIT_FILE = "/hunspell/prohibit.txt";

  private final Set<String> wordsToBeIgnored = new HashSet<>();
  private final Set<String> wordsToBeProhibited = new HashSet<>();
  private final CachingWordListLoader wordListLoader = new CachingWordListLoader();
  
  private List<DisambiguationPatternRule> antiPatterns = new ArrayList<>();
  private boolean considerIgnoreWords = true;
  private boolean convertsCase = false;

  public SpellingCheckRule(ResourceBundle messages, Language language) {
    super(messages);
    this.language = language;
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public abstract String getId();

  @Override
  public abstract String getDescription();

  @Override
  public abstract RuleMatch[] match(AnalyzedSentence sentence) throws IOException;

  @Override
  public boolean isDictionaryBasedSpellingRule() {
    return true;
  }

  @Override
  public void reset() {
  }

  /**
   * Add the given words to the list of words to be ignored during spell check.
   * You might want to use {@link #acceptPhrases(List)} instead, as only that
   * can also deal with phrases.
   */
  public void addIgnoreTokens(List<String> tokens) {
    wordsToBeIgnored.addAll(tokens);
  }

  /**
   * Set whether the list of words to be explicitly ignored (set with {@link #addIgnoreTokens(List)}) is considered at all.
   */
  public void setConsiderIgnoreWords(boolean considerIgnoreWords) {
    this.considerIgnoreWords = considerIgnoreWords;
  }

  /**
   * Get additional suggestions added before other suggestions (note the rule may choose to
   * re-order the suggestions anyway).
   */
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) throws IOException {
    List<String> moreSuggestions = new ArrayList<>();
    if (("Languagetool".equals(word) || "languagetool".equals(word)) && !suggestions.contains(LANGUAGETOOL)) {
      moreSuggestions.add(LANGUAGETOOL);
    }
    return moreSuggestions;
  }

  /**
   * Get additional suggestions added after other suggestions (note the rule may choose to
   * re-order the suggestions anyway).
   */
  protected List<String> getAdditionalSuggestions(List<String> suggestions, String word) {
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
    if (!considerIgnoreWords) {
      return false;
    }
    if (word.endsWith(".") && !wordsToBeIgnored.contains(word)) {
      return isIgnoredNoCase(word.substring(0, word.length()-1));  // e.g. word at end of sentence
    }
    return isIgnoredNoCase(word);
  }

  private boolean isIgnoredNoCase(String word) {
    return wordsToBeIgnored.contains(word) ||
           (convertsCase && wordsToBeIgnored.contains(word.toLowerCase(language.getLocale())));
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
   * Used to check whether the dictionary will use case conversions for
   * spell checking.
   * @return true if the dictionary converts case
   * @since 2.5
   */
  public boolean isConvertsCase() {
    return convertsCase;
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


  protected boolean isUrl(String token) {
    return WordTokenizer.isUrl(token);
  }

  protected boolean isEMail(String token) {
    return WordTokenizer.isEMail(token);
  }
  
  protected void init() throws IOException {
    for (String ignoreWord : wordListLoader.loadWords(getIgnoreFileName())) {
      addIgnoreWords(ignoreWord, wordsToBeIgnored);
    }
    for (String ignoreWord : wordListLoader.loadWords(getSpellingFileName())) {
      addIgnoreWords(ignoreWord, wordsToBeIgnored);
    }
    for (String prohibitedWord : wordListLoader.loadWords(getProhibitFileName())) {
      wordsToBeProhibited.addAll(expandLine(prohibitedWord));
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
   * Get the name of the prohibit file, which lists words not to be accepted, even
   * when the spell checker would accept them.
   * @since 2.8
   */
  protected String getProhibitFileName() {
    return language.getShortCode() + SPELLING_PROHIBIT_FILE;
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
  protected void filterSuggestions(List<String> suggestions) {
    for (int i = 0; i < suggestions.size(); i++) {
      if (isProhibited(suggestions.get(i))) {
        suggestions.remove(i);
      }
    }
  }

  /**
   * @param line the line as read from {@code spelling.txt}.
   * @param wordsToBeIgnored the set of words to be ignored
   * @since 2.9
   */
  protected void addIgnoreWords(String line, Set<String> wordsToBeIgnored) {
    wordsToBeIgnored.add(line);
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
        if (i == 0) {
          String uppercased = StringTools.uppercaseFirstChar(part);
          if (!uppercased.equals(part)) {
            startsLowercase = true;
          }
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

  private List<PatternToken> getTokensForSentenceStart(String[] parts) {
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
   * Checks whether a <code>word</code> starts with an ignored word
   * @param word - entire word
   * @param caseSensitive - determines whether the check is case-sensitive
   * @return length of the ignored word (i.e., return value is 0, if the word does not start with an ignored word).
   * If there are several matches from the set of ignored words, the length of the longest matching word is returned.
   * @since 3.5
   */
  protected int startsWithIgnoredWord(String word, boolean caseSensitive) {
    Optional<String> match;
    if(caseSensitive) {
      match = wordsToBeIgnored.stream().filter(s -> word.startsWith(s)).max(Comparator.naturalOrder()); 
    } else {
      String lowerCaseWord = word.toLowerCase();
      match = wordsToBeIgnored.stream().filter(s -> lowerCaseWord.startsWith(s.toLowerCase())).max(Comparator.naturalOrder());
    }
    return match.isPresent() ? match.get().length() : 0;
  }

}
