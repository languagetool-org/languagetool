/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.rules.spelling.VagueSpellChecker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Detect language per token and add that to the analysis.
 * @since 4.9
 */
public class LanguageAnnotator {

  public LanguageAnnotator() {
  }

  public List<FragmentWithLanguage> detectLanguages(String input, Language mainLang, List<Language> secondLangs) {
    List<FragmentWithLanguage> result = new ArrayList<>();
    List<TokenWithLanguages> tokens = getTokensWithPotentialLanguages(input, mainLang, secondLangs);
    List<List<TokenWithLanguages>> tokenRanges = getTokenRanges(tokens);   // split at boundaries like "."
    List<TokenRangeWithLanguage> tokenRangesWithLang = getTokenRangesWithLang(tokenRanges, mainLang, secondLangs);
    Language curLang;
    Language prevLang = mainLang;
    int curPos = 0;
    int fromPos = 0;
    for (TokenRangeWithLanguage tokenRange : tokenRangesWithLang) {
      curLang = tokenRange.lang;
      if (curLang != prevLang) {
        result.add(new FragmentWithLanguage(prevLang.getShortCodeWithCountryAndVariant(), input.substring(fromPos, curPos)));
        fromPos = curPos;
      }
      prevLang = curLang;
      curPos += tokenRange.tokens.stream().mapToInt(String::length).sum();
    }
    result.add(new FragmentWithLanguage(prevLang.getShortCodeWithCountryAndVariant(), input.substring(fromPos)));
    return result;
  }

  List<TokenWithLanguages> getTokensWithPotentialLanguages(String input, Language mainLang, List<Language> secondLangs) {
    List<TokenWithLanguages> tokens = new ArrayList<>();
    long t1 = System.nanoTime();
    // TODO: tokenizing might be different for languages...
    VagueSpellChecker speller = new VagueSpellChecker();
    List<String> mainLangTokens = mainLang.getWordTokenizer().tokenize(input);
    for (String token : mainLangTokens) {
      if (isWord(token) && speller.isValidWord(token, mainLang)) {
        tokens.add(new TokenWithLanguages(token, mainLang));
      } else {
        tokens.add(new TokenWithLanguages(token));
      }
    }
    for (Language secondLang : secondLangs) {
      int i = 0;
      for (TokenWithLanguages token : tokens) {
        if (isWord(token.token) && speller.isValidWord(token.token, secondLang)) {
          List<Language> langs = new ArrayList<>(token.langs);
          langs.add(secondLang);
          tokens.set(i, new TokenWithLanguages(token.token, langs));
        }
        i++;
      }
    }
    long t2 = System.nanoTime();
    long runTime = t2 - t1;
    //System.out.printf(Locale.ENGLISH, "%.0fms, %.2f ms/lookup, %d lookups [%s, %s]\n", runTime/1000.0/1000.0,
    //  (float)runTime/mainLangTokens.size()/1000.0/1000.0,
    //  mainLangTokens.size(), mainLang.getShortCodeWithCountryAndVariant(), secondLangs);
    return tokens;
  }

  List<List<TokenWithLanguages>> getTokenRanges(List<TokenWithLanguages> tokens) {
    List<List<TokenWithLanguages>> result = new ArrayList<>();
    List<TokenWithLanguages> l = new ArrayList<>();
    for (TokenWithLanguages token : tokens) {
      if (isBoundary(token)) {
        l.add(token);
        result.add(l);
        l = new ArrayList<>();
      } else {
        l.add(token);
      }
    }
    if (l.size() > 0) {
      result.add(l);
    }
    return result;
  }

  // For each range, assign the language that has most tokens there.
  List<TokenRangeWithLanguage> getTokenRangesWithLang(List<List<TokenWithLanguages>> tokenRanges, Language mainLang, List<Language> secondLangs) {
    List<TokenRangeWithLanguage> result = new ArrayList<>();
    for (List<TokenWithLanguages> tokens : tokenRanges) {
      Map<Language, Integer> langToCount = new HashMap<>();
      langToCount.put(mainLang, tokens.stream().mapToInt(k -> k.langs.contains(mainLang) ? 1 : 0).sum());
      for (Language lang : secondLangs) {
        int langCount = tokens.stream().mapToInt(k -> k.langs.contains(lang) ? 1 : 0).sum();
        langToCount.put(lang, langCount);
      }
      int max = 0;
      Language topLang = mainLang;
      for (Map.Entry<Language, Integer> entry : langToCount.entrySet()) {
        if (entry.getValue() > max) {
          topLang = entry.getKey();
          max = entry.getValue();
        }
      }
      List<String> tokenList = tokens.stream().map(k -> k.token).collect(Collectors.toList());
      result.add(new TokenRangeWithLanguage(tokenList, topLang));
    }
    return result;
  }

  private boolean isBoundary(TokenWithLanguages token) {
    return token.token.matches("[.?!;:\"„“»«()\\[\\]\n]");   // TODO: " - "
  }

  private static boolean isWord(String s) {
    return !s.trim().isEmpty() && s.matches("\\w+");
  }

  static class TokenWithLanguages {
    private final String token;
    private final List<Language> langs;
    TokenWithLanguages(String token, Language... langs) {
      this.token = Objects.requireNonNull(token);
      this.langs = new ArrayList<>(Arrays.asList(Objects.requireNonNull(langs)));
    }
    TokenWithLanguages(String token, List<Language> langs) {
      this.token = Objects.requireNonNull(token);
      this.langs = Objects.requireNonNull(langs);
    }
    @Override
    public String toString() {
      //return token + "/" + langs.stream().map(k -> k.getShortCodeWithCountryAndVariant()).collect(Collectors.joining("/"));
      if (isWord(token)) {
        return token + "/" + langs.stream().map(k -> k.getShortCodeWithCountryAndVariant()).collect(Collectors.joining("/"));
      } else {
        return token;
      }
    }
    String getToken() {
      return token;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TokenWithLanguages that = (TokenWithLanguages) o;
      return token.equals(that.token) && langs.equals(that.langs);
    }
    @Override
    public int hashCode() {
      return Objects.hash(token, langs);
    }
  }
  
  static class TokenRangeWithLanguage {
    private final List<String> tokens;
    private final Language lang;
    TokenRangeWithLanguage(List<String> tokens, Language lang) {
      this.tokens = Objects.requireNonNull(tokens);
      this.lang = Objects.requireNonNull(lang);
    }
    @Override
    public String toString() {
      return lang.getShortCodeWithCountryAndVariant() + ": " + tokens;
    }
  }
  
}
