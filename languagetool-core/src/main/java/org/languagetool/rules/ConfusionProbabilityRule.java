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
 * Also see <a href="http://wiki.languagetool.org/finding-errors-using-n-gram-data">http://wiki.languagetool.org/finding-errors-using-n-gram-data</a>.
 * @since 2.7
 */
public abstract class ConfusionProbabilityRule extends Rule {

  // probability is only used then at least these many of the occurrence lookups succeeded, 
  // i.e. returned a value > 0:
  public static final float MIN_COVERAGE = 0.5f;
  // the minimum value the more probable variant needs to have to be considered:
  private static final double MIN_PROB = 0.0;  // try values > 0 to avoid false alarms

  private static final boolean DEBUG = false;

  private final Map<String,List<ConfusionSet>> wordToSets;
  private final LanguageModel lm;
  private final long totalTokenCount;
  private final int grams;

  public abstract String getMessage(ConfusionString textString, ConfusionString suggestion);

  protected abstract WordTokenizer getTokenizer();

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }
  
  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages);
    setCategory(new Category(messages.getString("category_typo")));
    setLocQualityIssueType(ITSIssueType.NonConformance);
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String path = "/" + language.getShortName() + "/confusion_sets.txt";
    try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(path)) {
      ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
      this.wordToSets = confusionSetLoader.loadConfusionSet(confusionSetStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.lm = Objects.requireNonNull(languageModel);
    if (grams < 1 || grams > 5) {
      throw new IllegalArgumentException("grams must be between 1 and 5: " + grams);
    }
    this.grams = grams;
    totalTokenCount = languageModel.getTotalTokenCount();
  }

  @Override
  public String getId() {
    return "CONFUSION_RULE";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<GoogleToken> tokens = getGoogleTokens(sentence.getText(), true);
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
            if (betterAlternative != null) {
              ConfusionString stringFromText = getConfusionString(set, tokens.get(pos));
              String message = getMessage(stringFromText, betterAlternative);
              RuleMatch match = new RuleMatch(this, googleToken.startPos, googleToken.endPos, message);
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

  // Tokenization in google ngram corpus is different from LT tokenization (e.g. {@code you ' re} -> {@code you 're}),
  // so we use getTokenizer() and simple ignore the LT tokens.
  private List<GoogleToken> getGoogleTokens(String sentence, boolean addStartToken) {
    List<GoogleToken> result = new ArrayList<>();
    if (addStartToken) {
      result.add(new GoogleToken(LanguageModel.GOOGLE_SENTENCE_START, 0, 0));
    }
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
    wordToSets.clear();
    for (ConfusionString word : set.getSet()) {
      wordToSets.put(word.getString(), Collections.singletonList(set));
    }
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
      if (s.getString().equals(token.token)) {
        return s;
      }
    }
    throw new RuntimeException("Not found in set: " + token);
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

  private double get3gramProbabilityForOLD(GoogleToken token, List<GoogleToken> tokens, String term) {
    List<GoogleToken> newTokens = getGoogleTokens(term, false);
    Probability ngram3Left;
    Probability ngram3Middle;
    Probability ngram3Right;
    if (newTokens.size() == 1) {
      ngram3Left = getPseudoProbability(getContext(token, tokens, term, 0, 2));
      ngram3Middle = getPseudoProbability(getContext(token, tokens, term, 1, 1));
      ngram3Right = getPseudoProbability(getContext(token, tokens, term, 2, 0));
    } else if (newTokens.size() == 2) {
      // e.g. you're -> you 're
      ngram3Left = getPseudoProbability(getContext(token, tokens, newTokens, 0, 1));
      ngram3Right = getPseudoProbability(getContext(token, tokens, newTokens, 1, 0));
      // we cannot just use new Probability(1.0, 1.0f) as that would always produce higher
      // probabilities than in the case of one token (eg. "your"):
      ngram3Middle = new Probability((ngram3Left.prob + ngram3Right.prob) / 2, 1.0f); 
    } else {
      throw new RuntimeException("Words that consists of more than 2 tokens (according to Google tokenization) are not supported yet: " + term + " -> " + newTokens);
    }
    if (ngram3Left.coverage < MIN_COVERAGE && ngram3Middle.coverage < MIN_COVERAGE && ngram3Right.coverage < MIN_COVERAGE) {
      debug("  Min coverage of %.2f not reached: %.2f, %.2f, %.2f, assuming p=0\n", MIN_COVERAGE, ngram3Left.coverage, ngram3Middle.coverage, ngram3Right.coverage);
      return 0.0;
    } else {
      //debug("  Min coverage of %.2f okay: %.2f, %.2f\n", MIN_COVERAGE, ngram3Left.coverage, ngram3Right.coverage);
      return ngram3Left.prob * ngram3Middle.prob * ngram3Right.prob;
    }
  }

  // testing a variant...
  private double get3gramProbabilityFor(GoogleToken token, List<GoogleToken> tokens, String term) {
    List<String> leftContext = getContext(token, tokens, term, 2, 0);
    List<String> leftContextSub = new ArrayList<>();
    if (leftContext.size() == 1) {
      leftContextSub = Arrays.asList();
    } else if (leftContext.size() == 2) {
      leftContextSub = leftContext.subList(0, 1);
    } else if (leftContext.size() == 3) {
      leftContextSub = leftContext.subList(0, 2);
    }
    List<String> rightContext = getContext(token, tokens, term, 0, 2);
    List<String> rightContextSub = new ArrayList<>();
    if (rightContext.size() == 3) {
      rightContextSub = rightContext.subList(1, 3);
    } else {
      System.err.println("?? " + rightContext);
    }
    double res;
    if (leftContextSub.size() > 0 && rightContextSub.size() > 0) {
      res = ((double) lm.getCount(leftContext) / (lm.getCount(leftContextSub) + 1) + (double) lm.getCount(rightContext) / (lm.getCount(rightContextSub) + 1)) / 2;
      //System.out.println("(1) "+ term +": " + leftContext + " / " + leftContextSub + " + " + rightContext + " / " + rightContextSub + " => " + res);
    } else if (leftContextSub.size() > 0 && rightContextSub.size() == 0) {
      res = (double) lm.getCount(leftContext) / (lm.getCount(leftContextSub) + 1);
      //System.out.println("(2) " + term + ": " + leftContext + " / " + rightContextSub + " => " + res);
    } else if (leftContextSub.size() == 0 && rightContextSub.size() > 0) {
      res = (double) lm.getCount(rightContext) / (lm.getCount(rightContextSub) + 1);
      //System.out.println("(3) "+ term +": " + rightContext + " / " + rightContextSub + " => " + res);
    } else {
      //System.err.println("(4) "+ term +": fallback for " + leftContext + " / " + rightContext);
      return (lm.getCount(leftContext) + lm.getCount(rightContext)) / 2.0f;
    }
    return res;
  }

  private double get4gramProbabilityFor(GoogleToken token, List<GoogleToken> tokens, String term) {
    Probability ngram4Left = getPseudoProbability(getContext(token, tokens, term, 0, 3));
    Probability ngram4Middle = getPseudoProbability(getContext(token, tokens, term, 1, 2));
    Probability ngram4Right = getPseudoProbability(getContext(token, tokens, term, 3, 0));
    if (ngram4Left.coverage < MIN_COVERAGE && ngram4Middle.coverage < MIN_COVERAGE && ngram4Right.coverage < MIN_COVERAGE) {
      debug("  Min coverage of %.2f not reached: %.2f, %.2f, %.2f, assuming p=0\n", MIN_COVERAGE, ngram4Left.coverage, ngram4Middle.coverage, ngram4Right.coverage);
      return 0.0;
    } else {
      //debug("  Min coverage of %.2f okay: %.2f, %.2f\n", MIN_COVERAGE, ngram3Left.coverage, ngram3Right.coverage);
      return ngram4Left.prob * ngram4Middle.prob * ngram4Right.prob;
    }
  }

  // This is not always guaranteed to be a real probability (0.0 to 1.0)
  Probability getPseudoProbability(List<String> context) {
    int maxCoverage = 0;
    int coverage = 0;
    long firstWordCount = lm.getCount(context.get(0));
    maxCoverage++;
    if (firstWordCount > 0) {
      coverage++;
    }
    // chain rule of probability (https://www.coursera.org/course/nlp, "Introduction to N-grams" and "Estimating N-gram Probabilities"),
    // https://www.ibm.com/developerworks/community/blogs/nlp/entry/the_chain_rule_of_probability?lang=en
    double p = (double) (firstWordCount + 1) / (totalTokenCount + 1);
    debug("    P for %s: %.20f (%d)\n", context.get(0), p, firstWordCount);
    for (int i = 2; i <= context.size(); i++) {
      List<String> subList = context.subList(0, i);
      long phraseCount = lm.getCount(subList);
      double thisP = (double) (phraseCount + 1) / (firstWordCount + 1);
      // Variant:
      //long prevPhraseCount = lm.getCount(subList.subList(0, subList.size()-1));
      //double thisP = (double) (phraseCount + 1) / (prevPhraseCount + 1);
      maxCoverage++;
      debug("    P for " + subList + ": %.20f (%d)\n", thisP, phraseCount);
      //debug("    (%s)\n", subList.subList(0, subList.size()-1));
      if (phraseCount > 0) {
        coverage++;
      }
      p *= thisP;
    }
    debug("  " + StringTools.listToString(context, " ") + " => %.20f\n", p);
    return new Probability(p, (float)coverage/maxCoverage);
  }

  // a simplified version of getPseudoProbability() for clarity:
  double getPseudoProbability2(List<String> context) {  // three tokens, e.g. [this, is, their] and [this, is, there]  
    long firstWordCount = lm.getCount(context.get(0));  // this gets absolute ngram count
    // chain rule:
    double p = (double) (firstWordCount + 1) / (totalTokenCount + 1);
    for (int i = 2; i <= context.size(); i++) {
      List<String> subList = context.subList(0, i);  // e.g. [this, is], then [this, is, their]
      long phraseCount = lm.getCount(subList);
      double thisP = (double) (phraseCount + 1) / (firstWordCount + 1);
      p *= thisP;
    }
    return p;
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
    @Override
    public String toString() {
      return token;
    }
  }

}
