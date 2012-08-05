/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.PatternRule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract tokens from suggestions.
 */
public class SuggestionExtractor {

  private final static Pattern SUGGESTION_PATTERN = Pattern.compile("<suggestion.*?>(.*?)</suggestion>");
  private final static Pattern BACK_REFERENCE_PATTERN = Pattern.compile("\\\\" + "\\d+");
  private final Language language;

  public SuggestionExtractor(Language language) {
    this.language = language;
  }

  /**
   * Get the tokens of simple suggestions, i.e. those that don't use back references.
   */
  public List<String> getSuggestionTokens(Rule rule) {
    final List<String> wordsToBeIgnored = new ArrayList<String>();
    if (rule instanceof PatternRule) {
      final PatternRule patternRule = (PatternRule) rule;
      final String message = patternRule.getMessage();
      final List<String> suggestions = getSimpleSuggestions(message);
      final List<String> tokens = getSuggestionTokens(suggestions);
      wordsToBeIgnored.addAll(tokens);
    }
    return wordsToBeIgnored;
  }

  /**
   * Get suggestions that don't use back references or regular
   * @param message
   * @return
   */
  List<String> getSimpleSuggestions(String message) {
    final Matcher matcher = SUGGESTION_PATTERN.matcher(message);
    int startPos = 0;
    final List<String> suggestions = new ArrayList<String>();
    while (matcher.find(startPos)) {
      final String suggestion = matcher.group(1);
      startPos = matcher.end();
      if (isSimpleSuggestion(suggestion)) {
        suggestions.add(suggestion);
      }
    }
    return suggestions;
  }

  private boolean isSimpleSuggestion(String suggestion) {
    if (suggestion.contains("<match")) {
      return false;
    }
    final Matcher matcher = BACK_REFERENCE_PATTERN.matcher(suggestion);
    return !matcher.find();
  }

  private List<String> getSuggestionTokens(List<String> suggestions) {
    final List<String> tokens = new ArrayList<String>();
    for (String suggestion : suggestions) {
      final List<String> suggestionTokens = language.getWordTokenizer().tokenize(suggestion);
      for (String suggestionToken : suggestionTokens) {
        if (!suggestionToken.trim().isEmpty()) {
          tokens.add(suggestionToken);
        }
      }
    }
    return tokens;
  }

  private void writeIgnoreTokensForLanguages() throws IOException {
    final Map<Language, Set<String>> map = getLanguageToIgnoreTokensMapping();
    for (Map.Entry<Language, Set<String>> entry : map.entrySet()) {
      final Language language = entry.getKey();
      final File langDir = getLanguageDir(language);
      final File hunspellDir = new File(langDir, "hunspell");
      if (!hunspellDir.exists()) {
        System.out.println("No directory " + hunspellDir + " found, ignoring language " + language);
        continue;
      }
      final File ignoreFile = new File(hunspellDir, "ignore.txt");
      final Set<String> tokens = entry.getValue();
      if (tokens.size() > 0) {
        final FileWriter writer = new FileWriter(ignoreFile);
        try {
          writeIntro(writer, language);
          for (String token : tokens) {
            writer.write(token);
            writer.write("\n");
          }
        } finally {
          writer.close();
        }
        System.out.println("Wrote " + tokens.size() + " words to " + ignoreFile);
      }
    }
  }

  private void writeIntro(FileWriter writer, Language language) throws IOException {
    writer.write("# words to be ignored by the spellchecker (auto-generated " + new Date() + ")\n");
    writeArtificialTestCaseItems(writer, language);
  }

  private void writeArtificialTestCaseItems(FileWriter writer, Language language) throws IOException {
    if (language == Language.AMERICAN_ENGLISH) {
      writer.write("anArtificialTestWordForLanguageTool\n");
    } else if (language == Language.GERMANY_GERMAN) {
      writer.write("einPseudoWortFürLanguageToolTests\n");
    }
  }

  /**
   * We don't support sub-language resources yet, so collect all variants for one language.
   */
  private Map<Language, Set<String>> getLanguageToIgnoreTokensMapping() throws IOException {
    final Map<Language, Set<String>> langToIgnoreTokens = new HashMap<Language, Set<String>>();
    for (Language lang : Language.REAL_LANGUAGES) {
      final Set<String> suggestionTokens = new HashSet<String>();
      final JLanguageTool languageTool = new JLanguageTool(lang);
      languageTool.activateDefaultPatternRules();
      final List<Rule> rules = languageTool.getAllRules();
      for (Rule rule : rules) {
        suggestionTokens.addAll(getSuggestionTokens(rule));
      }
      final Language noVariantLanguage = lang.getDefaultVariant() == null ? lang : lang.getDefaultVariant();
      final Set<String> existingTokens = langToIgnoreTokens.get(noVariantLanguage);
      if (existingTokens != null) {
        existingTokens.addAll(suggestionTokens);
      } else {
        langToIgnoreTokens.put(noVariantLanguage, suggestionTokens);
      }
    }
    return langToIgnoreTokens;
  }

  private File getLanguageDir(Language language) {
    final File dir = new File("resource", language.getShortName());
    if (dir.exists()) {
      return dir;
    } else {
      // during development (in SVN):
      final File sourceDir = new File("src", "resource");
      return new File(sourceDir, language.getShortName());
    }
  }

  public static void main(String[] args) throws IOException {
    final SuggestionExtractor extractor = new SuggestionExtractor(/*not used:*/Language.ENGLISH);
    extractor.writeIgnoreTokensForLanguages();
  }

}
