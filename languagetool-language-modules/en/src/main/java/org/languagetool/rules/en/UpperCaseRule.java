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
package org.languagetool.rules.en;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

/**
 * Finds some(!) words written uppercase that should be spelled lowercase.
 * @since 5.0
 */
public class UpperCaseRule extends Rule {

  private static MorfologikAmericanSpellerRule spellerRule;
  private static Set<String> exceptions = new HashSet<>(Arrays.asList(
    "Bin", "Spot",  // names
    "French", "Roman", "Hawking", "Square", "Japan", "Premier"
  ));
  private static final AhoCorasickDoubleArrayTrie<String> exceptionTrie = new AhoCorasickDoubleArrayTrie<>();
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      token("Professor"),
      tokenRegex("[A-Z].+")
    )
  );

  private final Language lang;

  public UpperCaseRule(ResourceBundle messages, Language lang) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    this.lang = lang;
    setDefaultTempOff();
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("I really <marker>Like</marker> spaghetti."),
                   Example.fixed("I really <marker>like</marker> spaghetti"));
    if (spellerRule == null) {
      initTrie();
      try {
        spellerRule = new MorfologikAmericanSpellerRule(messages, lang);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void initTrie() {
    CachingWordListLoader cachingWordListLoader = new CachingWordListLoader();
    List<String> words = new ArrayList<>();
    words.addAll(cachingWordListLoader.loadWords("en/specific_case.txt"));
    words.addAll(cachingWordListLoader.loadWords("spelling_global.txt"));
    Map<String,String> map = new HashMap<>();
    for (String word : words) {
      map.put(word, word);
    }
    synchronized (exceptionTrie) {
      exceptionTrie.build(map);
    }
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, lang);
  }

  @Override
  public final String getId() {
    return "EN_UPPER_CASE";
  }

  @Override
  public String getDescription() {
    return "Checks wrong uppercase spelling of words that are not proper nouns";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    boolean atSentStart = true;

    boolean isSentence = isSentence(tokens);
    if (!isSentence) {
      // might be a headline, so skip it
      return toRuleMatchArray(matches);
    }

    for (int i = 0; i < tokens.length; i++) {
      AnalyzedTokenReadings token = tokens[i];
      String tokenStr = token.getToken();
      //System.out.println(i + ": " + prevIsUpperCase(tokens, i) + " - " + tokenStr);
      if (tokenStr.length() > 0
          && !token.isImmunized()
          && Character.isUpperCase(tokenStr.charAt(0))
          && !StringTools.isAllUppercase(tokenStr)
          && !atSentStart
          && token.hasPosTagStartingWith("VB")   // start only with these to avoid false alarms. TODO: extend
          && !token.hasPosTagStartingWith("NNP")
          && token.isTagged()
          && (!prevIsUpperCase(tokens, i) || (prevIsUpperCase(tokens, i) && i == 2))  // probably a name, like "Sex Pistols", but not c
          && !nextIsUpperCase(tokens, i)
          && !prevIsOneOf(tokens, i, Arrays.asList(":", "née", "of", "\"", "'"))  // probably a title like "The history of XYZ"
          && !nextIsOneOfThenUppercase(tokens, i, Arrays.asList("of"))
          && !tokenStr.matches("I")
          && !exceptions.contains(tokenStr)
          && !spellerRule.isMisspelled(StringTools.lowercaseFirstChar(tokenStr))    // e.g. "German" is correct, "german" isn't
          && !trieMatches(sentence.getText(), token)
      ) {
        String msg = "Only proper nouns start with an uppercase character (there are exceptions for headlines).";
        RuleMatch match = new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), msg);
        match.setSuggestedReplacement(StringTools.lowercaseFirstChar(tokenStr));
        matches.add(match);
      }
      if (!token.isSentenceStart() && !tokenStr.isEmpty() && !token.isNonWord()) {
        atSentStart = false;
      }
    }
    return toRuleMatchArray(matches);
  }

  private boolean trieMatches(String text, AnalyzedTokenReadings token) {
    List<AhoCorasickDoubleArrayTrie.Hit<String>> hits = exceptionTrie.parseText(text);
    for (AhoCorasickDoubleArrayTrie.Hit<String> hit : hits) {
      if (hit.begin <= token.getStartPos() && hit.end >= token.getEndPos()) {
        return true;
      }
    }
    return false;
  }

  private boolean prevIsOneOf(AnalyzedTokenReadings[] tokens, int i, List<String> strings) {
    return i > 0 && strings.contains(tokens[i-1].getToken());
  }

  // e.g. "The history of Xyz"
  //           ^^^^^^^
  private boolean nextIsOneOfThenUppercase(AnalyzedTokenReadings[] tokens, int i, List<String> strings) {
    return i + 2 < tokens.length && strings.contains(tokens[i+1].getToken()) && StringTools.startsWithUppercase(tokens[i+2].getToken());
  }

  private boolean prevIsUpperCase(AnalyzedTokenReadings[] tokens, int i) {
    return i > 0 && StringTools.startsWithUppercase(tokens[i-1].getToken());
  }

  private boolean nextIsUpperCase(AnalyzedTokenReadings[] tokens, int i) {
    return i + 1 < tokens.length && StringTools.startsWithUppercase(tokens[i+1].getToken());
  }

  private boolean isSentence(AnalyzedTokenReadings[] tokens) {
    boolean isSentence = false;
    for (int i = tokens.length - 1; i > 0; i--) {
      if (tokens[i].getToken().matches("[.!?:]")) {
        isSentence = true;
        break;
      }
      if (!tokens[i].isParagraphEnd() && !tokens[i].isNonWord()) {
        break;
      }
    }
    return isSentence;
  }

}
