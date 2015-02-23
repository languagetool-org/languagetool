/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.SuggestionExtractor;

import java.io.*;
import java.util.*;

/**
 * Extract tokens from suggestions and write them to {@code ignore.txt} files.
 */
public class SuggestionExtractorTool {

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
      final File ignoreFile;
      if (language.isVariant()) {
        ignoreFile = new File(hunspellDir, "ignore-" + language.getCountries()[0] + ".txt");
      } else {
        ignoreFile = new File(hunspellDir, "ignore.txt");
      }
      final Set<String> tokens = entry.getValue();
      try (FileOutputStream fos = new FileOutputStream(ignoreFile)) {
        try (OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8")) {
          writeIntro(writer, language);
          for (String token : tokens) {
            writer.write(token);
            writer.write("\n");
          }
        }
      }
      System.out.println("Wrote " + tokens.size() + " words to " + ignoreFile);
    }
  }

  /**
   * We don't support sub-language resources yet, so collect all variants for one language.
   */
  private Map<Language, Set<String>> getLanguageToIgnoreTokensMapping() throws IOException {
    final Map<Language, Set<String>> langToIgnoreTokens = new HashMap<>();
    SuggestionExtractor extractor = new SuggestionExtractor();
    for (Language lang : Languages.get()) {
      final Set<String> suggestionTokens = new HashSet<>();
      final JLanguageTool languageTool = new JLanguageTool(lang);
      final Rule spellcheckRule = getSpellcheckRule(languageTool);
      if (spellcheckRule == null) {
        System.out.println("No spellchecker rule found for " + lang);
        continue;
      }
      final List<Rule> rules = languageTool.getAllRules();
      int tokenCount = 0;
      int noErrorCount = 0;
      for (Rule rule : rules) {
        final List<String> tokens = extractor.getSuggestionTokens(rule, lang);
        tokenCount += tokens.size();
        for (String token : tokens) {
          final AnalyzedSentence sentence = languageTool.getAnalyzedSentence(token);
          final RuleMatch[] matches = spellcheckRule.match(sentence);
          if (matches.length > 0) {
            suggestionTokens.add(token);
          } else {
            //System.out.println("No error matches for " + lang + ": " + token);
            noErrorCount++;
          }
        }
      }
      System.out.println(lang + ": " + noErrorCount + " out of " + tokenCount + " words ignored because they are known to spellchecker anyway");
      final Language noVariantLanguage = lang.getDefaultLanguageVariant() == null ? lang : lang.getDefaultLanguageVariant();
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
    final String langCode = language.getShortName();
    final File dir = new File("org/languagetool/resource", langCode);
    if (dir.exists()) {
      return dir;
    } else {
      // during development (in git):
      return new File(langCode + "/src/main/resources/org/languagetool/resource/", langCode);
    }
  }

  private Rule getSpellcheckRule(JLanguageTool languageTool) {
    final List<Rule> allActiveRules = languageTool.getAllActiveRules();
    for (Rule activeRule : allActiveRules) {
      if (activeRule instanceof SpellingCheckRule) {
        ((SpellingCheckRule) activeRule).setConsiderIgnoreWords(false);
        return activeRule;
      }
    }
    return null;
  }

  private void writeIntro(Writer writer, Language language) throws IOException {
    writer.write("# words to be ignored by the spellchecker (auto-generated)\n");
    writeArtificialTestCaseItems(writer, language);
  }

  private void writeArtificialTestCaseItems(Writer writer, Language language) throws IOException {
    if (language.getShortName().equals("en-US")) {
      writer.write("anArtificialTestWordForLanguageTool\n");
    } else if (language.getShortName().equals("de-DE")) {
      writer.write("einPseudoWortFürLanguageToolTests\n");
    }
  }

  public static void main(String[] args) throws IOException {
    if (Languages.get().size() < 5) {
      throw new RuntimeException("Found only " + Languages.get().size() + " languages in classpath. " +
              "Please run this class with the classpath of 'languagetool-standalone' to have access to all languages.");
    }
    final List<String> dirs = Arrays.asList(new File(".").list());
    if (!dirs.contains("en") || !dirs.contains("de")) {
      throw new RuntimeException("Please set the working directory to 'languagetool-language-modules' when running this class");
    }
    final SuggestionExtractorTool extractor = new SuggestionExtractorTool();
    extractor.writeIgnoreTokensForLanguages();
  }
  
}
