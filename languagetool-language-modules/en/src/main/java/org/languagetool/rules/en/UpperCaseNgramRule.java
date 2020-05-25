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
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.ngrams.Probability;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.token;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.pos;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.tokenRegex;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.csRegex;

/**
 * Finds some(!) words written uppercase that should be spelled lowercase.
 * @since 5.0
 */
public class UpperCaseNgramRule extends Rule {

  public static final int THRESHOLD = 50;
  private static MorfologikAmericanSpellerRule spellerRule;
  private static Set<String> exceptions = new HashSet<>(Arrays.asList(
    "Bin", "Spot",  // names
    "Go",           // common usage, as in "Go/No Go decision"
    "French", "Roman", "Hawking", "Square", "Japan", "Premier", "Allied"
  ));
  private static final AhoCorasickDoubleArrayTrie<String> exceptionTrie = new AhoCorasickDoubleArrayTrie<>();
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      token("Hugs"), token("and"), token("Kisses")
    ),
    Arrays.asList( // Please go to File and select Options. 
      token("go"),
      token("to"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      tokenRegex("[A-Z].+"),
      token(","),
      tokenRegex("[Aa]nd|[Oo]r|&"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList( // "The goal is to Develop, Discuss and Learn.""
      tokenRegex("[A-Z].+"),
      token(","),
      tokenRegex("[A-Z].+"),
      tokenRegex("[Aa]nd|[Oo]r|&"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      csRegex("[A-Z].+"),
      token(">"),
      csRegex("[A-Z].+")
    ),
    Arrays.asList(
      pos("SENT_START"), // Two-word phrases with "?" or "!": "What Happened?", "Catch Up!" (can be headlines)
      tokenRegex("[A-Z].+"),
      tokenRegex("[A-Z].+"),
      tokenRegex("[\\!\\?]")
    ),
    Arrays.asList(
      pos("SENT_START"), // Step1 - Watch the full episode.
      tokenRegex(".*\\w.*"),
      tokenRegex("-|–"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      pos("SENT_START"), // Step 1 - Watch the full episode.
      tokenRegex(".*\\w.*"),
      tokenRegex("[0-9]+"),
      tokenRegex("-|–"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      pos("SENT_START"), // Lesson #1 - Learn the alphabet.
      tokenRegex(".*\\w.*"),
      token("#"),
      tokenRegex("[0-9]+"),
      tokenRegex("-|–"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      token("BBC"),
      token("Culture")
    ),
    Arrays.asList( // name of TV series
      token("Dublin"),
      token("Murders")
    ),
    Arrays.asList(
      token("Amazon"),
      token("Live")
    ),
    Arrays.asList(
      csRegex("[A-Z].+"),
      token("/"),
      csRegex("[A-Z].+")
    ),
    Arrays.asList(
      tokenRegex("[A-Z].+"),  // e.g. "Top 10% Lunch Deals"
      tokenRegex("\\d+%?"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      tokenRegex("[0-9]+"),  // e.g. "6) Have a beer"
      tokenRegex("[)\\]]"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      tokenRegex("[a-z]"),  // e.g. "a) Have a beer"
      tokenRegex("[)\\]]"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      tokenRegex("[\\(\\]]"),  // e.g. "(b) Have a beer"
      tokenRegex("[a-z0-9]"),
      tokenRegex("[)\\]]")
    ),
    Arrays.asList(
      tokenRegex("[A-Z].+"),  // e.g. "Freelance 2.0"
      tokenRegex("[0-9]+"),
      tokenRegex("."),
      tokenRegex("[0-9]+")
    ),
    Arrays.asList(
      tokenRegex("[A-Z].+"),  // e.g. "You Don't Know" or "Kuiper’s Belt"
      tokenRegex("['’`´‘]"),
      tokenRegex("t|d|ve|s|re|m"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      tokenRegex("[A-Z].+"),  // e.g. "Culture, People , Nature", probably a title
      token(","),
      tokenRegex("[A-Z].+"),
      token(","),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      tokenRegex("The"),  // e.g. "The Sea is Watching", probably a title
      tokenRegex("[A-Z].+"),
      token("is"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      token("Professor"),
      tokenRegex("[A-Z].+")
    ),
    Arrays.asList(
      token("Time"),
      token("magazine")
    ),
    Arrays.asList(
      tokenRegex("Teams|Maps") // Microsoft Teams / Google Maps (not tagged as NNP)
    )
  );

  private final Language lang;
  private final LanguageModel lm;

  public UpperCaseNgramRule(ResourceBundle messages, LanguageModel lm, Language lang) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    this.lm = lm;
    this.lang = lang;
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("This <marker>Prototype</marker> was developed by Miller et al."),
                   Example.fixed("This <marker>prototype</marker> was developed by Miller et al."));
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
    return "EN_UPPER_CASE_NGRAM";
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
          && !maybeTitle(tokens, i)
      ) {
        if (i + 1 < tokens.length) {
          List<String> ucList = Arrays.asList(tokens[i - 1].getToken(), tokenStr, tokens[i + 1].getToken());
          List<String> lcList = Arrays.asList(tokens[i - 1].getToken(), StringTools.lowercaseFirstChar(tokenStr), tokens[i + 1].getToken());
          Probability ucProb = lm.getPseudoProbability(ucList);
          Probability lcProb = lm.getPseudoProbability(lcList);
          double ratio = lcProb.getProb() / ucProb.getProb();
          //System.out.println("-->" + ucProb + ", lc: " + lcProb + " ==> " + ratio);
          if (ratio > THRESHOLD) {
            String msg = "Only proper nouns start with an uppercase character (there are exceptions for headlines).";
            RuleMatch match = new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), msg);
            match.setSuggestedReplacement(StringTools.lowercaseFirstChar(tokenStr));
            matches.add(match);
          }
        }
      }
      if (!token.isSentenceStart() && !tokenStr.isEmpty() && !token.isNonWord()) {
        atSentStart = false;
      }
    }
    return toRuleMatchArray(matches);
  }

  // a very rough guess whether the word at the given position might be part of a title
  boolean maybeTitle(AnalyzedTokenReadings[] tokens, int i) {
    return firstLongWordToLeftIsUppercase(tokens, i) || firstLongWordToRightIsUppercase(tokens, i);
  }

  boolean firstLongWordToLeftIsUppercase(AnalyzedTokenReadings[] tokens, int pos) {
    for (int i = pos - 1; i > 1; i--) {
      if (isShortWord(tokens[i])) {
        continue;
      }
      return StringTools.startsWithUppercase(tokens[i].getToken());
    }
    return false;
  }

  boolean firstLongWordToRightIsUppercase(AnalyzedTokenReadings[] tokens, int pos) {
    for (int i = pos + 1; i < tokens.length; i++) {
      if (isShortWord(tokens[i])) {
        continue;
      }
      return StringTools.startsWithUppercase(tokens[i].getToken());
    }
    return false;
  }

  private boolean isShortWord(AnalyzedTokenReadings token) {
    // ignore words typically spelled lowercase even in titles
    return token.getToken().trim().isEmpty() || token.getToken().matches("and|or|the|of|on|with|to|it|in|for|as|at|his|her|its|into|&|/");
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
