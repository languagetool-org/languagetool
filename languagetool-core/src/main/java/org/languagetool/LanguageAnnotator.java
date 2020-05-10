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

  private static final int MIN_TOKENS = 4;  // fragments shorter than this will get the previous fragment's language

  public LanguageAnnotator() {
  }

  public List<FragmentWithLanguage> detectLanguages(String input, Language mainLang, List<Language> secondLangs) {
    List<TokenWithLanguages> tokens = getTokensWithPotentialLanguages(input, mainLang, secondLangs);
    List<List<TokenWithLanguages>> tokenRanges = getTokenRanges(tokens);   // split at boundaries like "."
    //System.out.println("tokenRanges: " + tokenRanges);
    List<TokenRangeWithLanguage> tokenRangesWithLang = getTokenRangesWithLang(tokenRanges, mainLang, secondLangs);
    //System.out.println("tokenRangesWithLang: " + tokenRangesWithLang);
    Language curLang;
    Language prevLang = mainLang;
    int curPos = 0;
    int fromPos = 0;
    List<FragmentWithLanguage> result = new ArrayList<>();
    for (TokenRangeWithLanguage tokenRange : tokenRangesWithLang) {
      curLang = tokenRange.lang;
      if (tokenRange.tokens.size() == 1 && isQuote(tokenRange.tokens.get(0))) {
        // assign single quote the language of the previous element, assuming it belongs there
        curLang = prevLang;
      } else if (curLang != prevLang) {
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
    boolean inQuote = false;
    for (TokenWithLanguages token : tokens) {
      if (isQuote(token.token) && !inQuote) {
        // this is an opening quote - add to next range
        if (l.size() > 0) {
          result.add(l);
        }
        l = new ArrayList<>();
        l.add(token);
      } else if (isBoundary(token)) {
        l.add(token);
        result.add(l);
        l = new ArrayList<>();
      } else {
        l.add(token);
      }
      if (isQuote(token.token)) {
        inQuote = !inQuote;
      }
    }
    if (l.size() > 0) {
      result.add(l);
    }
    return result;
  }

  // For each range, assign the language that has most tokens there. For ambiguous parts
  // (no winning language), use the top language of the next non-ambiguous part:
  List<TokenRangeWithLanguage> getTokenRangesWithLang(List<List<TokenWithLanguages>> tokenRanges, Language mainLang, List<Language> secondLangs) {
    List<TokenRangeWithLanguage> result = new ArrayList<>();
    int i = 0;
    Language prevTopLang = null;
    for (List<TokenWithLanguages> tokens : tokenRanges) {
      //System.out.println(">"+tokens);
      Language topLang = null;
      boolean allAmbiguous = tokens.stream().allMatch(k -> k.ambiguous());
      if (allAmbiguous) {
        //System.out.println("ALL AMBIG: " + tokens);
        for (int j = i + 1; j < tokenRanges.size(); j++) {
          //System.out.println("testing " + j);
          List<TokenWithLanguages> nextTokens = tokenRanges.get(j);
          boolean nextAllAmbiguous = nextTokens.stream().allMatch(k -> k.ambiguous());
          if (!nextAllAmbiguous) {
            topLang = getTopLang(mainLang, secondLangs, nextTokens);
            //System.out.println("not ambig at " + j + " (" + nextTokens + ")" +  ", top lang  + " + topLang);
            break;
          }
        }
      }
      if (topLang == null) {
        if (tokens.size() < MIN_TOKENS && prevTopLang != null) {
          topLang = prevTopLang;
        } else {
          topLang = getTopLang(mainLang, secondLangs, tokens);
        }
      }
      List<String> tokenList = tokens.stream().map(k -> k.token).collect(Collectors.toList());
      result.add(new TokenRangeWithLanguage(tokenList, topLang));
      prevTopLang = topLang;
      i++;
    }
    return result;
  }

  private Language getTopLang(Language mainLang, List<Language> secondLangs, List<TokenWithLanguages> tokens) {
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
    return topLang;
  }

  private boolean isBoundary(TokenWithLanguages token) {
    return token.token.matches("[.?!;:\"„“»«()\\[\\]\n]");   // TODO: " - "
  }

  private boolean isQuote(String token) {
    return token.matches("[\"„“”»«]");  }

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
    boolean ambiguous() {
      return langs.size() != 1;
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
