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

  private final Set<String> wordsToBeIgnored = new HashSet<>();

  private boolean wordsWithDotsPresent = false;
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
   * Reset the list of words to be ignored, by re-loading it from the "ignore.txt" file.
   */
  public void resetIgnoreTokens() {
    wordsToBeIgnored.clear();
    try {
      init();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected List<String> getAdditionalSuggestions(List<String> suggestions, String word) {
    List<String> moreSuggestions = new ArrayList<>();
    if ("Languagetool".equals(word) && !suggestions.contains(LANGUAGETOOL)) {
      moreSuggestions.add(LANGUAGETOOL);
    }
    return moreSuggestions;
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
   * If possible, use {@link #ignoreToken(org.languagetool.AnalyzedTokenReadings[], int)} instead.
   */
  protected boolean ignoreWord(String word) throws IOException {
    if (!considerIgnoreWords) {
      return false;
    }
    if (!wordsWithDotsPresent) {
      // TODO?: this is needed at least for German as Hunspell tokenization includes the dot:
      word = word.endsWith(".") ? word.substring(0, word.length() - 1) : word;
    }
    return (wordsToBeIgnored.contains(word)
        || (convertsCase &&
        wordsToBeIgnored.contains(word.toLowerCase(language.getLocale()))));
  }

  /**
   * Returns true iff the word at the given position should be ignored by the spell checker.
   * If possible, use {@link #ignoreToken(org.languagetool.AnalyzedTokenReadings[], int)} instead.
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
    for (String protocol : WordTokenizer.getProtocols()) {
      if (token.startsWith(protocol + "://")) {
        return true;
      }
    }
    return false;
  }
  
  protected void init() throws IOException {
    loadFileIfExists(language.getShortName() + SPELLING_IGNORE_FILE);
  }

  private void loadFileIfExists(String filename) throws IOException {
    final boolean ignoreFileExists = JLanguageTool.getDataBroker().resourceExists(filename);
    if (!ignoreFileExists) {
      return;
    }
    loadWordsToBeIgnored(filename);
  }

  private void loadWordsToBeIgnored(String ignoreFile) throws IOException {
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(ignoreFile)) {
      try (Scanner scanner = new Scanner(inputStream, "utf-8")) {
        while (scanner.hasNextLine()) {
          final String line = scanner.nextLine();
          final boolean isComment = line.startsWith("#");
          if (isComment) {
            continue;
          }
          if (language.getShortNameWithCountryAndVariant().equals("de-CH")) {
            // hack: Swiss German doesn't use "ß" but always "ss" - replace this, otherwise
            // misspellings (from Swiss point-of-view) like "äußere" wouldn't be found:
            wordsToBeIgnored.add(line.replace("ß", "ss"));
          } else {
            wordsToBeIgnored.add(line);
          }
          if (line.endsWith(".")) {
            wordsWithDotsPresent = true;
          }
        }
      }
    }
  }

}
