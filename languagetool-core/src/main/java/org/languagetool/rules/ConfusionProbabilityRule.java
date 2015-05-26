/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * LanguageTool's homophone confusion check that uses ngram lookups
 * to decide which word in a confusion set (from {@code confusion_sets.txt}) suits best.
 * @since 2.7
 */
public abstract class ConfusionProbabilityRule extends Rule {

  // probability is only used then at least these many of the occurrence lookups succeeded, 
  // i.e. returned a value > 0:
  public static final float MIN_COVERAGE = 0.5f;

  private static final boolean DEBUG = false;

  private final Map<String,ConfusionSet> wordToSet;
  private final LanguageModel lm;
  private final long totalTokenCount;

  public abstract String getMessage(String suggestion, String description);

  protected abstract WordTokenizer getTokenizer();

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    super(messages);
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String path = "/" + language.getShortName() + "/confusion_sets.txt";
    try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(path)) {
      ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
      this.wordToSet = confusionSetLoader.loadConfusionSet(confusionSetStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.lm = Objects.requireNonNull(languageModel);
    totalTokenCount = languageModel.getTotalTokenCount();
  }

  @Override
  public String getId() {
    return "CONFUSION_RULE";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<GoogleToken> tokens = getGoogleTokens(sentence.getText());
    List<RuleMatch> matches = new ArrayList<>();
    int pos = 0;
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      ConfusionSet confusionSet = wordToSet.get(token);
      boolean uppercase = false;
      if (confusionSet == null && token.length() > 0 && Character.isUpperCase(token.charAt(0))) {
        confusionSet = wordToSet.get(StringTools.lowercaseFirstChar(token));
        uppercase = true;
      }
      boolean isEasilyConfused = confusionSet != null;
      if (isEasilyConfused) {
        Set<ConfusionString> set = uppercase ? confusionSet.getUppercaseFirstCharSet() : confusionSet.getSet();
        ConfusionString betterAlternative = getBetterAlternativeOrNull(tokens.get(pos), tokens, set, confusionSet.getFactor());
        if (betterAlternative != null) {
          String message = getMessage(betterAlternative.getString(), betterAlternative.getDescription());
          RuleMatch match = new RuleMatch(this, googleToken.startPos, googleToken.endPos, message);
          match.setSuggestedReplacement(betterAlternative.getString());
          matches.add(match);
        }
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  // Tokenization in google ngram corpus is different from LT tokenization (e.g. {@code you ' re} -> {@code you 're}),
  // so we use getTokenizer() and simple ignore the LT tokens.
  private List<GoogleToken> getGoogleTokens(String sentence) {
    List<GoogleToken> result = new ArrayList<>();
    List<String> tokens = getTokenizer().tokenize(sentence);
    int startPos = 0;
    for (String token : tokens) {
      if (!StringTools.isWhitespace(token)) {
        result.add(new GoogleToken(token, startPos, startPos+token.length()));
      }
      startPos += token.length();
    }
    return result;
  }
  
  @Override
  public void reset() {
  }

  /** @deprecated used only for tests */
  public void setConfusionSet(ConfusionSet set) {
    wordToSet.clear();
    for (ConfusionString word : set.getSet()) {
      wordToSet.put(word.getString(), set);
    }
  }

  @Nullable
  private ConfusionString getBetterAlternativeOrNull(GoogleToken token, List<GoogleToken> tokens, Set<ConfusionString> confusionSet, int factor) {
    if (confusionSet.size() != 2) {
      throw new RuntimeException("Confusion set must be of size 2: " + confusionSet);
    }
    ConfusionString other = getAlternativeTerm(confusionSet, token);
    return getBetterAlternativeOrNull(token, tokens, other, factor);
  }

  private ConfusionString getAlternativeTerm(Set<ConfusionString> confusionSet, GoogleToken token) {
    ConfusionString other = null;
    for (ConfusionString s : confusionSet) {
      if (!s.getString().equals(token.token)) {
        other = s;
      }
    }
    if (other == null) {
      throw new RuntimeException("No alternative found for: " + token);
    }
    return other;
  }

  private ConfusionString getBetterAlternativeOrNull(GoogleToken token, List<GoogleToken> tokens, ConfusionString otherWord, int factor) {
    String word = token.token;
    double p1 = getProbabilityFor(token, tokens, word);
    double p2 = getProbabilityFor(token, tokens, otherWord.getString());
    debug("P(" + word + ") = %.50f\n", p1);
    debug("P(" + otherWord + ") = %.50f\n", p2);
    return p2 > p1 * factor ? otherWord : null;
  }

  List<String> getContext(GoogleToken token, List<GoogleToken> tokens, String newToken, int toLeft, int toRight) {
    int pos = tokens.indexOf(token);
    if (pos == -1) {
      throw new RuntimeException("Token not found: " + token);
    }
    List<String> result = new ArrayList<>();
    for (int i = 1, added = 0; added < toLeft; i++) {
      if (pos-i < 0) {
        // We don't use v2 of the Google data everywhere, so we don't always have the "_START_"
        // marker. So if we're at the beginning of the sentence, just use the first tokens
        // without an artificial start marker:
        result.clear();
        result.add(newToken);
        for (int j = pos-1; j >= 0; j--) {
          result.add(0, tokens.get(j).token);
        }
        return result;
      } else {
        if (!tokens.get(pos-i).isWhitespace()) {
          result.add(0, tokens.get(pos - i).token);
          added++;
        }
      }
    }
    result.add(newToken);
    for (int i = 1, added = 0; added < toRight; i++) {
      if (pos+i >= tokens.size()) {
        result.add(".");
        added++;
      } else {
        if (!tokens.get(pos+i).isWhitespace()) {
          result.add(tokens.get(pos + i).token);
          added++;
        }
      }
    }
    return result;
  }

  private List<String> toStrings(List<GoogleToken> analyzedTokenReadings) {
    List<String> result = new ArrayList<>();
    for (GoogleToken reading : analyzedTokenReadings) {
      result.add(reading.token);
    }
    return result;
  }

  private double getProbabilityFor(GoogleToken token, List<GoogleToken> tokens, String term) {
    Probability ngram3Left = getPseudoProbability(getContext(token, tokens, term, 0, 2));
    Probability ngram3Middle = getPseudoProbability(getContext(token, tokens, term, 1, 1));
    Probability ngram3Right = getPseudoProbability(getContext(token, tokens, term, 2, 0));
    if (ngram3Left.coverage < MIN_COVERAGE && ngram3Middle.coverage < MIN_COVERAGE && ngram3Right.coverage < MIN_COVERAGE) {
      debug("  Min coverage of %.2f not reached: %.2f, %.2f, %.2f, assuming p=0\n", MIN_COVERAGE, ngram3Left.coverage, ngram3Middle.coverage, ngram3Right.coverage);
      return 0.0;
    } else {
      //debug("  Min coverage of %.2f okay: %.2f, %.2f\n", MIN_COVERAGE, ngram3Left.coverage, ngram3Right.coverage);
      return ngram3Left.prob * ngram3Middle.prob * ngram3Right.prob;
    }
  }

  // This is not always guaranteed to be a real probability (0.0 to 1.0)
  Probability getPseudoProbability(List<String> context) {
    int maxCoverage = 0;
    int coverage = 0;
    long firstWordCount = lm.getCount(context.get(0));
    maxCoverage++;
    if (firstWordCount == 0) {
      debug("    # zero matches for '%s'\n", context.get(0));
    } else {
      coverage++;
    }
    // chain rule:
    double p = (double) (firstWordCount + 1) / (totalTokenCount + 1);
    debug("    P for %s: %.20f\n", context.get(0), p);
    for (int i = 2; i <= context.size(); i++) {
      List<String> subList = context.subList(0, i);
      long phraseCount = lm.getCount(subList);
      double thisP = (double) (phraseCount + 1) / (firstWordCount + 1);
      maxCoverage++;
      debug("    P for " + subList + ": %.20f\n", thisP);
      if (phraseCount == 0) {
        debug("    # zero matches for '%s'\n", subList);
      } else {
        coverage++;
      }
      p *= thisP;
    }
    debug("  " + StringTools.listToString(context, " ") + " => %.20f\n", p);
    return new Probability(p, (float)coverage/maxCoverage);
  }

  private void debug(String message, Object... vars) {
    if (DEBUG) {
      System.out.printf(Locale.ENGLISH, message, vars);
    }
  }
  
  static class Probability {
    final double prob;
    final float coverage;
    Probability(double prob, float coverage) {
      this.prob = prob;
      this.coverage = coverage;
    }
  }

  static class GoogleToken {
    String token;
    int startPos;
    int endPos;
    GoogleToken(String token, int startPos, int endPos) {
      this.token = token;
      this.startPos = startPos;
      this.endPos = endPos;
    }
    boolean isWhitespace() {
      return StringTools.isWhitespace(token);
    }
  }

}
