/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules.ngrams;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.tokenizers.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class LanguageModelUtils {
  private LanguageModelUtils(){
  }

  private static final Logger logger = LoggerFactory.getLogger(LanguageModelUtils.class);

  /**
   * Return a tokenizer that works more like Google does for its ngram index (which
   * doesn't seem to be properly documented).
   */
  static Tokenizer getGoogleStyleWordTokenizer(Language language) {
    return language.getWordTokenizer();
  }

  static List<String> getContext(GoogleToken token, List<GoogleToken> tokens, String newToken, int toLeft, int toRight) {
    return getContext(token, tokens, Collections.singletonList(new GoogleToken(newToken, 0, newToken.length())), toLeft, toRight);
  }


  static List<String> getContext(GoogleToken token, List<GoogleToken> tokens, List<GoogleToken> newTokens, int toLeft, int toRight) {
    List<GoogleToken> result = getContext(token, tokens, newTokens, toLeft, toRight,
      GoogleToken::isWhitespace, new GoogleToken(".", 0, 0));
    return result.stream().map(t -> t.token).collect(Collectors.toList());
  }

  public static <T> List<T> getContext(T token, List<T> tokens, List<T> newTokens, int toLeft, int toRight, Predicate<T> isWhitespace, T endToken) {
    // TODO: debug token not found sometimes
    //int pos = -1;
    //for (int i = 0; i < tokens.size(); i++) {
    //  if (tokens.get(i).token.s)
    //}
    int pos = tokens.indexOf(token);
    if (pos == -1) {
      throw new RuntimeException(String.format("Token not found: '%s' in tokens %s", token, tokens));
    }
    List<T> result = new ArrayList<T>();
    for (int i = 1, added = 0; added < toLeft; i++) {
      if (pos - i < 0) {
        // So if we're at the beginning of the sentence, just use the first tokens:
        result.clear();
        result.addAll(newTokens);
        for (int j = pos - 1; j >= 0; j--) {
          result.add(0, tokens.get(j));
        }
        return result;
      } else {
        if (!isWhitespace.test(tokens.get(pos - i))) {
          result.add(0, tokens.get(pos - i));
          added++;
        }
      }
    }
    result.addAll(newTokens);
    for (int i = 1, added = 0; added < toRight; i++) {
      if (pos + i >= tokens.size()) {
        // I'm not sure if we should use _END_ here instead. Evaluation on 2015-08-12
        // shows increase in recall for some pairs, decrease in others.
        result.add(endToken);
        added++;
      } else {
        if (!isWhitespace.test(tokens.get(pos + i))) {
          result.add(tokens.get(pos + i));
          added++;
        }
      }
    }
    return result;
  }

  public static double get3gramProbabilityFor(Language lang, LanguageModel lm, int position, AnalyzedSentence sentence, String candidate) {
    Tokenizer tokenizer = getGoogleStyleWordTokenizer(lang);
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(sentence, true, tokenizer);
    Optional<GoogleToken> token = tokens.stream()
      .filter(t -> t.startPos == position && !LanguageModel.GOOGLE_SENTENCE_START.equals(t.token))
      .findFirst();
    if (!token.isPresent()) {
      logger.warn(String.format("Could not find matching Google token in tokenizations '%s' / '%s'", sentence.getText(), tokens));
      return 0.0;
    }
    return get3gramProbabilityFor(lang, lm, token.get(), tokens, candidate);
  }

  public static double get4gramProbabilityFor(Language lang, LanguageModel lm, int position, AnalyzedSentence sentence, String candidate) {
    Tokenizer tokenizer = getGoogleStyleWordTokenizer(lang);
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(sentence, true, tokenizer);
    Optional<GoogleToken> token = tokens.stream()
      .filter(t -> t.startPos == position && !LanguageModel.GOOGLE_SENTENCE_START.equals(t.token))
      .findFirst();
    if (!token.isPresent()) {
      logger.warn(String.format("Could not find matching Google token in tokenizations '%s' / '%s'", sentence.getText(), tokens));
      return 0.0;
    }
    return get4gramProbabilityFor(lang, lm, token.get(), tokens, candidate);
  }


  static double get3gramProbabilityFor(Language lang, LanguageModel lm, GoogleToken token, List<GoogleToken> tokens, String term) {
    Tokenizer tokenizer = getGoogleStyleWordTokenizer(lang);
    List<GoogleToken> newTokens = GoogleToken.getGoogleTokens(term, false, tokenizer);
    Probability ngram3Left;
    Probability ngram3Middle;
    Probability ngram3Right;
    if (newTokens.size() == 1) {
      List<String> leftContext = getContext(token, tokens, term, 0, 2);
      ngram3Left = lm.getPseudoProbability(leftContext);
      logger.trace(String.format("Left  : %.90f %s\n", ngram3Left.getProb(), Arrays.asList(leftContext)));
      List<String> middleContext = getContext(token, tokens, term, 1, 1);
      ngram3Middle = lm.getPseudoProbability(middleContext);
      logger.trace(String.format("Middle: %.90f %s\n", ngram3Middle.getProb(), Arrays.asList(middleContext)));
      List<String> rightContext = getContext(token, tokens, term, 2, 0);
      ngram3Right = lm.getPseudoProbability(rightContext);
      logger.trace(String.format("Right : %.90f %s\n", ngram3Right.getProb(), Arrays.asList(rightContext)));
    } else if (newTokens.size() == 2) {
      // e.g. you're -> you 're
      ngram3Left = lm.getPseudoProbability(getContext(token, tokens, newTokens, 0, 1));
      ngram3Right = lm.getPseudoProbability(getContext(token, tokens, newTokens, 1, 0));
      // we cannot just use new Probability(1.0, 1.0f) as that would always produce higher
      // probabilities than in the case of one token (eg. "your"):
      ngram3Middle = new Probability((ngram3Left.getProb() + ngram3Right.getProb()) / 2, 1.0f);
    } else {
      logger.warn("Words that consists of more than 2 tokens (according to Google tokenization) are not supported yet: " + term + " -> " + newTokens);
      return 0.0;
    }
    if (ngram3Left.getCoverage() < ConfusionProbabilityRule.MIN_COVERAGE && ngram3Middle.getCoverage() < ConfusionProbabilityRule.MIN_COVERAGE && ngram3Right.getCoverage() < ConfusionProbabilityRule.MIN_COVERAGE) {
      logger.trace(String.format("  Min coverage of %.2f not reached: %.2f, %.2f, %.2f, assuming p=0\n", ConfusionProbabilityRule.MIN_COVERAGE, ngram3Left.getCoverage(), ngram3Middle.getCoverage(), ngram3Right.getCoverage()));
      return 0.0;
    } else {
      //logger.trace(String.format("  Min coverage of %.2f okay: %.2f, %.2f\n", MIN_COVERAGE, ngram3Left.getCoverage(), ngram3Right.getCoverage()));
      //return Math.exp(ngram3Left.getLogProb() + ngram3Middle.getLogProb() + ngram3Right.getLogProb());
      return ngram3Left.getProb() * ngram3Middle.getProb() * ngram3Right.getProb();
    }
  }

  static double get4gramProbabilityFor(Language lang, LanguageModel lm, GoogleToken token, List<GoogleToken> tokens, String term) {
    Tokenizer tokenizer = getGoogleStyleWordTokenizer(lang);
    List<GoogleToken> newTokens = GoogleToken.getGoogleTokens(term, false, tokenizer);

    Probability ngram4Left, ngram4MiddleLeft, ngram4MiddleRight, ngram4Right;

    if (newTokens.size() == 1) {
      ngram4Left = lm.getPseudoProbability(getContext(token, tokens, newTokens, 0, 3));
      ngram4MiddleLeft = lm.getPseudoProbability(getContext(token, tokens, newTokens, 2, 1));
      ngram4MiddleRight = lm.getPseudoProbability(getContext(token, tokens, newTokens, 1, 2));
      ngram4Right = lm.getPseudoProbability(getContext(token, tokens, newTokens, 3, 0));
    } else if (newTokens.size() == 2) {
      ngram4Left = lm.getPseudoProbability(getContext(token, tokens, newTokens, 0, 2));
      ngram4MiddleLeft = lm.getPseudoProbability(getContext(token, tokens, newTokens, 1, 1));
      ngram4MiddleRight = ngram4MiddleLeft; // TODO: is this okay?
      ngram4Right = lm.getPseudoProbability(getContext(token, tokens, newTokens, 2, 0));
    } else {
      logger.warn("Words that consists of more than 2 tokens (according to Google tokenization) are not supported yet: " + term + " -> " + newTokens);
      return 0.0;
    }
    if (ngram4Left.getCoverage() < ConfusionProbabilityRule.MIN_COVERAGE &&
      ngram4MiddleLeft.getCoverage() < ConfusionProbabilityRule.MIN_COVERAGE &&
      ngram4MiddleRight.getCoverage() < ConfusionProbabilityRule.MIN_COVERAGE &&
      ngram4Right.getCoverage() < ConfusionProbabilityRule.MIN_COVERAGE) {
      logger.trace(String.format("  Min coverage of %.2f not reached: %.2f, %.2f, %.2f, %.2f, assuming p=0\n",
        ConfusionProbabilityRule.MIN_COVERAGE, ngram4Left.getCoverage(), ngram4MiddleLeft.getCoverage(),
        ngram4MiddleRight.getCoverage(), ngram4Right.getCoverage()));
      return 0.0;
    } else {
      //logger.trace(String.format("  Min coverage of %.2f okay: %.2f, %.2f\n", MIN_COVERAGE, ngram4Left.getCoverage(), ngram4Right.getCoverage()));
      return Math.exp(ngram4Left.getLogProb() + ngram4MiddleLeft.getLogProb() +
        ngram4MiddleRight.getLogProb() + ngram4Right.getLogProb());
    }
  }
}
