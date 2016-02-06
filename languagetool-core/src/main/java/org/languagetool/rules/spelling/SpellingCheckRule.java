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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tokenizers.WordTokenizer;

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

  private boolean considerIgnoreWords = true;
  private boolean convertsCase = false;

  public SpellingCheckRule(final ResourceBundle messages, final Language language) {
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
   */
  public void addIgnoreTokens(List<String> tokens) {
    wordsToBeIgnored.addAll(tokens);
  }

  /**
   * Set whether the list of words to be explicitly ignored is considered at all.
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
    if ("Languagetool".equals(word) && !suggestions.contains(LANGUAGETOOL)) {
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
  
  protected void init() throws IOException {
    for (String ignoreWord : loadWords(getIgnoreFileName())) {
      addIgnoreWords(ignoreWord, wordsToBeIgnored);
    }
    for (String ignoreWord : loadWords(getSpellingFileName())) {
      addIgnoreWords(ignoreWord, wordsToBeIgnored);
    }
    for (String prohibitedWord : loadWords(getProhibitFileName())) {
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
    return language.getShortName() + SPELLING_IGNORE_FILE;
  }

  /**
   * Get the name of the spelling file, which lists words to be accepted
   * and used for suggestions, even when the spell checker would not accept them.
   * @since 2.9
   */
  protected String getSpellingFileName() {
    return language.getShortName() + SPELLING_FILE;
  }

  /**
   * Get the name of the prohibit file, which lists words not to be accepted, even
   * when the spell checker would accept them.
   * @since 2.8
   */
  protected String getProhibitFileName() {
    return language.getShortName() + SPELLING_PROHIBIT_FILE;
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

  private List<String> loadWords(String filePath) throws IOException {
    List<String> result = new ArrayList<>();
    if (!JLanguageTool.getDataBroker().resourceExists(filePath)) {
      return result;
    }
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filePath);
         Scanner scanner = new Scanner(inputStream, "utf-8")) {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        if (isComment(line)) {
          continue;
        }
        if (line.contains(" ")) {
          throw new RuntimeException("No space expected in " + filePath + ": '" + line + "'");
        }
        result.add(line);
      }
    }
    return result;
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

  private boolean isComment(String line) {
    return line.startsWith("#");
  }

}
