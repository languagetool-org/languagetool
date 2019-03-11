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
    return matches.toArray(new RuleMatch[0]);
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
