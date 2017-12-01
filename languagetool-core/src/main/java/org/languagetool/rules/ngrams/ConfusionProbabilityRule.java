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
package org.languagetool.rules.ngrams;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * LanguageTool's homophone confusion check that uses ngram lookups
 * to decide which word in a confusion set (from {@code confusion_sets.txt}) suits best.
 * Also see <a href="http://wiki.languagetool.org/finding-errors-using-n-gram-data">http://wiki.languagetool.org/finding-errors-using-n-gram-data</a>.
 * @since 2.7
 */
public abstract class ConfusionProbabilityRule extends Rule {

  /** @since 3.1 */
  public static final String RULE_ID = "CONFUSION_RULE";
  // probability is only used then at least these many of the occurrence lookups succeeded, 
  // i.e. returned a value > 0:
  public static final float MIN_COVERAGE = 0.5f;
  // the minimum value the more probable variant needs to have to be considered:
  private static final double MIN_PROB = 0.0;  // try values > 0 to avoid false alarms

  private static final boolean DEBUG = false;

  // Speed up the server use case, where rules get initialized for every call:
  private static final LoadingCache<String, Map<String, List<ConfusionSet>>> confSetCache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Map<String, List<ConfusionSet>>>() {
        @Override
        public Map<String, List<ConfusionSet>> load(@NotNull String fileInClassPath) throws IOException {
          ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
          ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
          try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(fileInClassPath)) {
            return confusionSetLoader.loadConfusionSet(confusionSetStream);
          }
        }
      });

  private final Map<String,List<ConfusionSet>> wordToSets = new HashMap<>();
  private final LanguageModel lm;
  private final int grams;
  private final Language language;

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }
  
  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages);
    setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.NonConformance);
    for (String filename : getFilenames()) {
      String path = "/" + language.getShortCode() + "/" + filename;
      this.wordToSets.putAll(confSetCache.getUnchecked(path));
    }
    this.lm = Objects.requireNonNull(languageModel);
    this.language = Objects.requireNonNull(language);
    if (grams < 1 || grams > 5) {
      throw new IllegalArgumentException("grams must be between 1 and 5: " + grams);
    }
    this.grams = grams;
  }

  @NotNull
  protected List<String> getFilenames() {
    return Arrays.asList("confusion_sets.txt");
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    String text = sentence.getText();
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(text, true, getGoogleStyleWordTokenizer());
    List<RuleMatch> matches = new ArrayList<>();
    int pos = 0;
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      List<ConfusionSet> confusionSets = wordToSets.get(token);
      boolean uppercase = false;
      if (confusionSets == null && token.length() > 0 && Character.isUpperCase(token.charAt(0))) {
        confusionSets = wordToSets.get(StringTools.lowercaseFirstChar(token));
        uppercase = true;
      }
      if (confusionSets != null) {
        for (ConfusionSet confusionSet : confusionSets) {
          boolean isEasilyConfused = confusionSet != null;
          if (isEasilyConfused) {
            Set<ConfusionString> set = uppercase ? confusionSet.getUppercaseFirstCharSet() : confusionSet.getSet();
            ConfusionString betterAlternative = getBetterAlternativeOrNull(tokens.get(pos), tokens, set, confusionSet.getFactor());
            if (betterAlternative != null && !isException(text)) {
              ConfusionString stringFromText = getConfusionString(set, tokens.get(pos));
              String message = getMessage(stringFromText, betterAlternative);
              RuleMatch match = new RuleMatch(this, sentence, googleToken.startPos, googleToken.endPos, message);
              match.setSuggestedReplacement(betterAlternative.getString());
              matches.add(match);
            }
          }
        }
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  /**
   * Return true to prevent a match.
   */
  protected boolean isException(String sentenceText) {
    return false;
  }

  @Override
  public String getDescription() {
    return Tools.i18n(messages, "statistics_rule_description");
  }

  /**
   * Return a tokenizer that works more like Google does for its ngram index (which
   * doesn't seem to be properly documented).
   */
  protected Tokenizer getGoogleStyleWordTokenizer() {
    return language.getWordTokenizer();
  }

  private String getMessage(ConfusionString textString, ConfusionString suggestion) {
    if (textString.getDescription() != null && suggestion.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest1", suggestion.getString(), suggestion.getDescription(), textString.getString(), textString.getDescription());
    } else if (suggestion.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest2", suggestion.getString(), suggestion.getDescription());
    } else {
      return Tools.i18n(messages, "statistics_suggest3", suggestion.getString());
    }
  }
  
  /** @deprecated used only for tests */
  public void setConfusionSet(ConfusionSet set) {
    wordToSets.clear();
    for (ConfusionString word : set.getSet()) {
      wordToSets.put(word.getString(), Collections.singletonList(set));
    }
  }

  /**
   * Returns the ngram level used, typically 3.
   * @since 3.1
   */
  public int getNGrams() {
    return grams;
  }

  @Nullable
  private ConfusionString getBetterAlternativeOrNull(GoogleToken token, List<GoogleToken> tokens, Set<ConfusionString> confusionSet, long factor) {
    if (confusionSet.size() != 2) {
      throw new RuntimeException("Confusion set must be of size 2: " + confusionSet);
    }
    ConfusionString other = getAlternativeTerm(confusionSet, token);
    return getBetterAlternativeOrNull(token, tokens, other, factor);
  }

  private ConfusionString getAlternativeTerm(Set<ConfusionString> confusionSet, GoogleToken token) {
    for (ConfusionString s : confusionSet) {
      if (!s.getString().equals(token.token)) {
        return s;
      }
    }
    throw new RuntimeException("No alternative found for: " + token);
  }

  private ConfusionString getConfusionString(Set<ConfusionString> confusionSet, GoogleToken token) {
    for (ConfusionString s : confusionSet) {
      if (s.getString().equalsIgnoreCase(token.token)) {
        return s;
      }
    }
    throw new RuntimeException("Not found in set '" + confusionSet + "': " + token);
  }

  private ConfusionString getBetterAlternativeOrNull(GoogleToken token, List<GoogleToken> tokens, ConfusionString otherWord, long factor) {
    String word = token.token;
    double p1;
    double p2;
    if (grams == 3) {
      p1 = get3gramProbabilityFor(token, tokens, word);
      p2 = get3gramProbabilityFor(token, tokens, otherWord.getString());
    } else if (grams == 4) {
      p1 = get4gramProbabilityFor(token, tokens, word);
      p2 = get4gramProbabilityFor(token, tokens, otherWord.getString());
    } else {
      throw new RuntimeException("Only 3grams and 4grams are supported");
    }
    debug("P(" + word + ") = %.90f\n", p1);
    debug("P(" + otherWord + ") = %.90f\n", p2);
    return p2 >= MIN_PROB && p2 > p1 * factor ? otherWord : null;
  }

  List<String> getContext(GoogleToken token, List<GoogleToken> tokens, String newToken, int toLeft, int toRight) {
    return getContext(token, tokens, Collections.singletonList(new GoogleToken(newToken, 0, newToken.length())), toLeft, toRight);
  }
  
  private List<String> getContext(GoogleToken token, List<GoogleToken> tokens, List<GoogleToken> newTokens, int toLeft, int toRight) {
    int pos = tokens.indexOf(token);
    if (pos == -1) {
      throw new RuntimeException("Token not found: " + token);
    }
    List<String> result = new ArrayList<>();
    for (int i = 1, added = 0; added < toLeft; i++) {
      if (pos-i < 0) {
        // So if we're at the beginning of the sentence, just use the first tokens:
        result.clear();
        for (GoogleToken googleToken : newTokens) {
          result.add(googleToken.token);
        }
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
    for (GoogleToken googleToken : newTokens) {
      result.add(googleToken.token);
    }
    for (int i = 1, added = 0; added < toRight; i++) {
      if (pos+i >= tokens.size()) {
        // I'm not sure if we should use _END_ here instead. Evaluation on 2015-08-12
        // shows increase in recall for some pairs, decrease in others.
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

  private double get3gramProbabilityFor(GoogleToken token, List<GoogleToken> tokens, String term) {
    List<GoogleToken> newTokens = GoogleToken.getGoogleTokens(term, false, getGoogleStyleWordTokenizer());
    Probability ngram3Left;
    Probability ngram3Middle;
    Probability ngram3Right;
    if (newTokens.size() == 1) {
      ngram3Left = lm.getPseudoProbability(getContext(token, tokens, term, 0, 2));
      ngram3Middle = lm.getPseudoProbability(getContext(token, tokens, term, 1, 1));
      ngram3Right = lm.getPseudoProbability(getContext(token, tokens, term, 2, 0));
    } else if (newTokens.size() == 2) {
      // e.g. you're -> you 're
      ngram3Left = lm.getPseudoProbability(getContext(token, tokens, newTokens, 0, 1));
      ngram3Right = lm.getPseudoProbability(getContext(token, tokens, newTokens, 1, 0));
      // we cannot just use new Probability(1.0, 1.0f) as that would always produce higher
      // probabilities than in the case of one token (eg. "your"):
      ngram3Middle = new Probability((ngram3Left.getProb() + ngram3Right.getProb()) / 2, 1.0f); 
    } else {
      throw new RuntimeException("Words that consists of more than 2 tokens (according to Google tokenization) are not supported yet: " + term + " -> " + newTokens);
    }
    if (ngram3Left.getCoverage() < MIN_COVERAGE && ngram3Middle.getCoverage() < MIN_COVERAGE && ngram3Right.getCoverage() < MIN_COVERAGE) {
      debug("  Min coverage of %.2f not reached: %.2f, %.2f, %.2f, assuming p=0\n", MIN_COVERAGE, ngram3Left.getCoverage(), ngram3Middle.getCoverage(), ngram3Right.getCoverage());
      return 0.0;
    } else {
      //debug("  Min coverage of %.2f okay: %.2f, %.2f\n", MIN_COVERAGE, ngram3Left.getCoverage(), ngram3Right.getCoverage());
      return ngram3Left.getProb() * ngram3Middle.getProb() * ngram3Right.getProb();
    }
  }

  private double get4gramProbabilityFor(GoogleToken token, List<GoogleToken> tokens, String term) {
    Probability ngram4Left = lm.getPseudoProbability(getContext(token, tokens, term, 0, 3));
    Probability ngram4Middle = lm.getPseudoProbability(getContext(token, tokens, term, 1, 2));
    Probability ngram4Right = lm.getPseudoProbability(getContext(token, tokens, term, 3, 0));
    if (ngram4Left.getCoverage() < MIN_COVERAGE && ngram4Middle.getCoverage() < MIN_COVERAGE && ngram4Right.getCoverage() < MIN_COVERAGE) {
      debug("  Min coverage of %.2f not reached: %.2f, %.2f, %.2f, assuming p=0\n", MIN_COVERAGE, ngram4Left.getCoverage(), ngram4Middle.getCoverage(), ngram4Right.getCoverage());
      return 0.0;
    } else {
      //debug("  Min coverage of %.2f okay: %.2f, %.2f\n", MIN_COVERAGE, ngram3Left.coverage, ngram3Right.coverage);
      return ngram4Left.getProb() * ngram4Middle.getProb() * ngram4Right.getProb();
    }
  }

  private void debug(String message, Object... vars) {
    if (DEBUG) {
      System.out.printf(Locale.ENGLISH, message, vars);
    }
  }
  
}
