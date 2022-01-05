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

import com.google.common.cache.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
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
 * Also see <a href="https://dev.languagetool.org/finding-errors-using-n-gram-data">https://dev.languagetool.org/finding-errors-using-n-gram-data</a>.
 * @since 2.7
 */
public abstract class ConfusionProbabilityRule extends Rule {

  /**
   * @since 3.1
   * @deprecated not used anymore, the id is now more specific (like {@code CONFUSION_RULE_TERM1_TERM2})
   */
  public static final String RULE_ID = "CONFUSION_RULE";
  // probability is only used then at least these many of the occurrence lookups succeeded, 
  // i.e. returned a value > 0:
  public static final float MIN_COVERAGE = 0.5f;
  // the minimum value the more probable variant needs to have to be considered:
  private static final double MIN_PROB = 0.0;  // try values > 0 to avoid false alarms

  private static final boolean DEBUG = false;  // also see DEBUG in BaseLanguageModel.java

  // Speed up the server use case, where rules get initialized for every call:
  private static final LoadingCache<PathAndLanguage, Map<String, List<ConfusionPair>>> confSetCache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<PathAndLanguage, Map<String, List<ConfusionPair>>>() {
        @Override
        public Map<String, List<ConfusionPair>> load(@NotNull PathAndLanguage pathAndLanguage) throws IOException {
          ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader(pathAndLanguage.lang);
          ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
          try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(pathAndLanguage.path)) {
            return confusionSetLoader.loadConfusionPairs(confusionSetStream);
          }
        }
      });

  private final Map<String,List<ConfusionPair>> wordToPairs = new HashMap<>();
  private final LanguageModel lm;
  private final int grams;
  private final Language language;
  private final List<String> exceptions;
  private final List<DisambiguationPatternRule> antiPatterns;

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }
  
  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    this(messages, languageModel, language, grams, Arrays.asList());
  }

  /**
   * @since 4.7
   */
  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams, List<String> exceptions) {
    this(messages, languageModel, language, grams, exceptions, Collections.emptyList());
  }

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams,
                                  List<String> exceptions, List<List<PatternToken>> antiPatterns) {
    super(messages);
    setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.NonConformance);
    for (String filename : getFilenames()) {
      String path = "/" + language.getShortCode() + "/" + filename;
      this.wordToPairs.putAll(confSetCache.getUnchecked(new PathAndLanguage(path, language)));
    }
    this.lm = Objects.requireNonNull(languageModel);
    this.language = Objects.requireNonNull(language);
    if (grams < 1 || grams > 5) {
      throw new IllegalArgumentException("grams must be between 1 and 5: " + grams);
    }
    this.grams = grams;
    this.exceptions = exceptions;
    this.antiPatterns = makeAntiPatterns(antiPatterns, language);
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
    if (tokens.size() == 2) {
      // 2 tokens: first is always _START_ so there's no "real" context. Ignore these cases.
      return matches.toArray(new RuleMatch[0]);
    }
    int pos = 0;
    boolean realWordBefore = false;  // more advanced than simple checking for sentence start, as it skips quotes etc.
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      List<ConfusionPair> confusionPairs = wordToPairs.get(token);
      boolean uppercase = false;
      if (confusionPairs == null && token.length() > 0 && Character.isUpperCase(token.charAt(0)) && !realWordBefore && isRealWord(token)) {
        // do a lowercase lookup only at sentence start
        confusionPairs = wordToPairs.get(StringTools.lowercaseFirstChar(token));
        uppercase = true;
      }
      if (isRealWord(token)) {
        realWordBefore = true;
      }
      if (confusionPairs != null) {
        for (ConfusionPair confusionPair : confusionPairs) {
          boolean isEasilyConfused = confusionPair != null;
          if (isEasilyConfused) {
            List<ConfusionString> pairs = uppercase ? confusionPair.getUppercaseFirstCharTerms() : confusionPair.getTerms();
            ConfusionString betterAlternative = getBetterAlternativeOrNull(tokens.get(pos), tokens, pairs, confusionPair.getFactor());
            if (betterAlternative != null && !isException(text, googleToken.startPos, googleToken.endPos)) {
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
              if (pos > 0 && "_START_".equals(tokens.get(pos-1).token) && tokens.size() > pos+1 && tokens.get(pos+1).token != null && !isCommonWord(tokens.get(pos+1).token)) {
                // Let's assume there is not enough data for this. The original problem was a false alarm for
                // "Resolves:" (-> "Resolved:")
                continue;
              }
              if (isCoveredByAntiPattern(sentence, googleToken)) {
                continue;
              }
              if (!isLocalException(sentence, googleToken)) {
                String term1 = confusionPair.getTerms().get(0).getString();
                String term2 = confusionPair.getTerms().get(1).getString();
                String id = getId() + "_" + cleanId(term1) +  "_" + cleanId(term2);
                String desc = getDescription(term1, term2);
                String shortDesc = Tools.i18n(messages, "statistics_suggest_short_desc");
                RuleMatch match = new RuleMatch(new SpecificIdRule(id, desc, messages, lm, language), sentence, googleToken.startPos, googleToken.endPos, message, shortDesc);
                match.setSuggestedReplacements(suggestions);
                matches.add(match);
              }
            }
          }
        }
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[0]);
  }

  protected boolean isCommonWord(String token) {
    return token.matches("\\w+");
  }

  private boolean isCoveredByAntiPattern(AnalyzedSentence sentence, GoogleToken googleToken) {
    AnalyzedTokenReadings[] tmpTokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    for (AnalyzedTokenReadings tmpToken : tmpTokens) {
      if (tmpToken.isImmunized() && covers(tmpToken.getStartPos(), tmpToken.getEndPos(), googleToken.startPos, googleToken.endPos)) {
        return true;
      }
    }
    return false;
  }

  private String cleanId(String id) {
    return id.toUpperCase().replace("Ä", "AE").replace("Ü", "UE").replace("Ö", "OE");
  }

  private boolean isRealWord(String token) {
    return token.matches("[\\p{L}]+");
  }

  private boolean isLocalException(AnalyzedSentence sentence, GoogleToken googleToken) {
    for (String exception : exceptions) {
      int exStartPos = sentence.getText().toLowerCase().indexOf(exception);
      while (exStartPos != -1) {
        int exEndPos = exStartPos + exception.length();
        if (exEndPos == exStartPos) {   // just a protection against "" as exceptions
          return false;
        } else if (covers(exStartPos, exEndPos, googleToken.startPos, googleToken.endPos)) {
          return true;
        } else {
          exStartPos = sentence.getText().indexOf(exception, exEndPos);
        }
      }
    }
    return false;
  }

  private boolean covers(int exceptionStartPos, int exceptionEndPos, int startPos, int endPos) {
    return exceptionStartPos <= startPos && exceptionEndPos >= endPos;
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
  protected boolean isException(String sentenceText, int startPos, int endPos) {
    return false;
  }

  @Override
  public String getDescription() {
    return Tools.i18n(messages, "statistics_rule_description");  // the one from SpecificIdRule is used
  }

  private String getDescription(String word1, String word2) {
    return Tools.i18n(messages, "statistics_rule_description", word1, word2);
  }
  
  protected String getMessage(ConfusionString textString, ConfusionString suggestion) {
    if (textString.getDescription() != null && suggestion.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest1_new", suggestion.getString(), suggestion.getDescription(), textString.getString(), textString.getDescription());
    } else if (textString.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest4_new", suggestion.getString(), textString, textString.getDescription());
    } else if (suggestion.getDescription() != null) {
      return Tools.i18n(messages, "statistics_suggest2_new", suggestion.getString(), suggestion.getDescription(), textString.getString());
    } else {
      return Tools.i18n(messages, "statistics_suggest3_new", suggestion.getString(), textString.getString());
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

  private void debug(String message, Object... vars) {
    if (DEBUG) {
      System.out.printf(Locale.ENGLISH, message, vars);
    }
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns;
  }

  private static class PathAndLanguage {
    private final String path;
    private final Language lang;
    PathAndLanguage(String path, Language lang) {
      this.path = Objects.requireNonNull(path);
      this.lang = Objects.requireNonNull(lang);
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PathAndLanguage that = (PathAndLanguage) o;
      return path.equals(that.path) &&
        lang.equals(that.lang);
    }
    @Override
    public int hashCode() {
      return Objects.hash(path, lang);
    }
  }

  static class SpecificIdRule extends ConfusionProbabilityRule {
    private final String id;
    private final String desc;
    SpecificIdRule(String id, String desc, ResourceBundle messages, LanguageModel lm, Language lang) {
      super(messages, lm, lang);
      this.id = Objects.requireNonNull(id);
      this.desc = desc;
    }
    @Override
    public String getId() {
      return id;
    }
    @Override
    public String getDescription() {
      return desc;
    }
  }

}
