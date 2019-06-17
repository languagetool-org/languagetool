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
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  private static final LoadingCache<String, Map<String, List<ConfusionPair>>> confSetCache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Map<String, List<ConfusionPair>>>() {
        @Override
        public Map<String, List<ConfusionPair>> load(@NotNull String fileInClassPath) throws IOException {
          ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
          ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
          try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(fileInClassPath)) {
            return confusionSetLoader.loadConfusionPairs(confusionSetStream);
          }
        }
      });

  private final Map<String,List<ConfusionPair>> wordToPairs = new HashMap<>();
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
      this.wordToPairs.putAll(confSetCache.getUnchecked(path));
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
  public int estimateContextForSureMatch() {
    return grams;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    String text = sentence.getText();
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(text, true, LanguageModelUtils.getGoogleStyleWordTokenizer(language));
    List<RuleMatch> matches = new ArrayList<>();
    int pos = 0;
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      List<ConfusionPair> confusionPairs = wordToPairs.get(token);
      boolean uppercase = false;
      if (confusionPairs == null && token.length() > 0 && Character.isUpperCase(token.charAt(0))) {
        confusionPairs = wordToPairs.get(StringTools.lowercaseFirstChar(token));
        uppercase = true;
      }
      if (confusionPairs != null) {
        for (ConfusionPair confusionPair : confusionPairs) {
          boolean isEasilyConfused = confusionPair != null;
          if (isEasilyConfused) {
            List<ConfusionString> pairs = uppercase ? confusionPair.getUppercaseFirstCharTerms() : confusionPair.getTerms();
            ConfusionString betterAlternative = getBetterAlternativeOrNull(tokens.get(pos), tokens, pairs, confusionPair.getFactor());
            if (betterAlternative != null && !isException(text)) {
              if (!confusionPair.isBidirectional() && betterAlternative.getString().equals(pairs.get(0).getString())) {
                // only direction A -> B is possible, i.e. if A is used incorrectly, B is suggested - not vice versa
                continue;
              }
              ConfusionString stringFromText = getConfusionString(pairs, tokens.get(pos));
              String message = getMessage(stringFromText, betterAlternative);
              List<String> suggestions = new ArrayList<>(getSuggestions(message));
              if (!suggestions.contains(betterAlternative.getString())) {
                suggestions.add(betterAlternative.getString());
              }
              RuleMatch match = new RuleMatch(this, sentence, googleToken.startPos, googleToken.endPos, message);
              match.setSuggestedReplacements(suggestions);
              matches.add(match);
            }
          }
        }
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[0]);
  }

  private List<String> getSuggestions(String message) {
    Matcher matcher = Pattern.compile("<suggestion>(.*?)</suggestion>").matcher(message);
    List<String> result = new ArrayList<>();
    while (matcher.find()) {
      result.add(matcher.group(1));
    }
    return result;
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
  
  protected String getMessage(ConfusionString textString, ConfusionString suggestion) {
    if (textString.getDescription() != null && suggestion.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest1", suggestion.getString(), suggestion.getDescription(), textString.getString(), textString.getDescription());
    } else if (textString.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest4", suggestion.getString(), textString, textString.getDescription());
    } else if (suggestion.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest2", suggestion.getString(), suggestion.getDescription());
    } else {
      return Tools.i18n(messages, "statistics_suggest3", suggestion.getString());
    }
  }
  
  /** @deprecated used only for tests */
  public void setConfusionPair(ConfusionPair pair) {
    wordToPairs.clear();
    for (ConfusionString word : pair.getTerms()) {
      wordToPairs.put(word.getString(), Collections.singletonList(pair));
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
  private ConfusionString getBetterAlternativeOrNull(GoogleToken token, List<GoogleToken> tokens, List<ConfusionString> confusionSet, long factor) {
    if (confusionSet.size() != 2) {
      throw new RuntimeException("Confusion set must be of size 2: " + confusionSet);
    }
    ConfusionString other = getAlternativeTerm(confusionSet, token);
    return getBetterAlternativeOrNull(token, tokens, other, factor);
  }

  private ConfusionString getAlternativeTerm(List<ConfusionString> confusionSet, GoogleToken token) {
    for (ConfusionString s : confusionSet) {
      if (!s.getString().equals(token.token)) {
        return s;
      }
    }
    throw new RuntimeException("No alternative found for: " + token);
  }

  private ConfusionString getConfusionString(List<ConfusionString> confusionSet, GoogleToken token) {
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
      p1 = LanguageModelUtils.get3gramProbabilityFor(language, lm, token, tokens, word);
      p2 = LanguageModelUtils.get3gramProbabilityFor(language, lm, token, tokens, otherWord.getString());
    } else if (grams == 4) {
      p1 = LanguageModelUtils.get4gramProbabilityFor(language, lm, token, tokens, word);
      p2 = LanguageModelUtils.get4gramProbabilityFor(language, lm, token, tokens, otherWord.getString());
    } else {
      throw new RuntimeException("Only 3grams and 4grams are supported");
    }
    debug("%.90f <- P(" + word + ") \n", p1);
    debug("%.90f <- P(" + otherWord + ")\n", p2);
    return p2 >= MIN_PROB && p2 > p1 * factor ? otherWord : null;
  }

  List<String> getContext(GoogleToken token, List<GoogleToken> tokens, String newToken, int toLeft, int toRight) {
    return LanguageModelUtils.getContext(token, tokens, newToken, toLeft, toRight);
  }
  
  private void debug(String message, Object... vars) {
    if (DEBUG) {
      System.out.printf(Locale.ENGLISH, message, vars);
    }
  }
  
}
