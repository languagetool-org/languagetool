/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.Experimental;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Category;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tokenizers.Tokenizer;

import java.io.IOException;
import java.util.*;

/**
 * LanguageTool's probability check that uses ngram lookups
 * to decide if an ngram of the input text is so rare in our
 * ngram index that it should be considered an error.
 * Also see <a href="http://wiki.languagetool.org/finding-errors-using-n-gram-data">http://wiki.languagetool.org/finding-errors-using-n-gram-data</a>.
 * @since 3.2
 */
@Experimental
public class NgramProbabilityRule extends Rule {

  /** @since 3.2 */
  public static final String RULE_ID = "NGRAM_RULE";
  
  private static final boolean DEBUG = false;
  private static final List<Replacement> REPLACEMENTS = Collections.unmodifiableList(Arrays.asList(
    new Replacement("VBG", "VB"),
    new Replacement("VBG", "VBN"),
    new Replacement("VB", "VBG"),
    new Replacement("VB", "VBZ"),
    new Replacement("VB", "VBN"),
    new Replacement("VBZ", "VB"),
    new Replacement("VBZ", "VBP"),
    //TODO: this might improve results in general, but on our evaluation set, it makes results worse:
    /*new Replacement("VB.?", "VB"),
    new Replacement("VB.?", "VBZ"),
    new Replacement("VB.?", "VBP"),
    new Replacement("VB.?", "VBD"),
    new Replacement("VB.?", "VBN"),
    new Replacement("VB.?", "VBG"),*/
    new Replacement("NNS", "NN"),
    new Replacement("NN", "NNS")
  ));

  private final LanguageModel lm;
  private final Language language;

  private double minProbability = 0.00000000000001;

  public NgramProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    super(messages);
    setCategory(new Category(messages.getString("category_typo")));
    setLocQualityIssueType(ITSIssueType.NonConformance);
    this.lm = Objects.requireNonNull(languageModel);
    this.language = Objects.requireNonNull(language);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Experimental
  public void setMinProbability(double minProbability) {
    this.minProbability = minProbability;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(sentence, true, getGoogleStyleWordTokenizer());
    List<RuleMatch> matches = new ArrayList<>();
    GoogleToken prevPrevToken = null;
    GoogleToken prevToken = null;
    int i = 0;
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      if (prevPrevToken != null && prevToken != null) {
        if (i < tokens.size()-1) {
          GoogleToken next = tokens.get(i+1);
          // 2grams:
          //Probability p = lm.getPseudoProbability(Arrays.asList(prevToken.token, token));
          //Probability p = lm.getPseudoProbability(Arrays.asList(token, next.token));
          // 3grams:
          Probability p = lm.getPseudoProbability(Arrays.asList(prevToken.token, token, next.token));
          // a test with 4grams with fallback:
          /*Probability p = lm.getPseudoProbability(Arrays.asList(prevPrevToken.token, prevToken.token, token, next.token));
          if (p.getOccurrences() == 0) {
            p = lm.getPseudoProbability(Arrays.asList(prevToken.token, token, next.token));
            minProbability = 1.0E-14;
            if (p.getOccurrences() == 0) {
              p = lm.getPseudoProbability(Arrays.asList(prevToken.token, token));
              minProbability = 1.0E-8;
            }
          }*/
          //System.out.println("P=" + p + " for " + Arrays.asList(prevToken.token, token, next.token));
          String ngram = prevToken + " " + token + " " + next.token;
          // without bigrams:
          double prob = p.getProb();
          // with bigrams:
          //Probability bigramLeftP = getPseudoProbability(Arrays.asList(prevToken.token, token));
          //Probability bigramRightP = getPseudoProbability(Arrays.asList(token, next.token));
          //double prob = p.getProb() + bigramLeftP.getProb() + bigramRightP.getProb();
          //System.out.printf("%.20f for " + prevToken.token + " " + token + " " + next.token + "\n", prob);
          //System.out.printf("%.20f is minProbability\n", minProbability);
          if (prob < minProbability) {
            Alternatives betterAlternatives = getBetterAlternatives(prevToken, token, next, googleToken, p);
            if (!betterAlternatives.alternativesConsidered || betterAlternatives.alternatives.size() > 0) {
              String message = "The phrase '" + ngram + "' rarely occurs in the reference corpus (" + p.getOccurrences() + " times)";
              RuleMatch match = new RuleMatch(this, prevToken.startPos, next.endPos, message);
              List<String> suggestions = new ArrayList<>();
              for (Alternative betterAlternative : betterAlternatives.alternatives) {
                suggestions.add(prevToken.token + " " + betterAlternative.token + " " + next.token);
              }
              match.setSuggestedReplacements(suggestions);
              matches.add(match);
            } else {
              debug("Ignoring match as all alternatives are less probable: '%s' in '%s'\n", ngram, sentence.getText());
            }
          }
        }
      }
      prevPrevToken = prevToken;
      prevToken = googleToken;
      i++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  private Alternatives getBetterAlternatives(GoogleToken prevToken, String token, GoogleToken next, GoogleToken googleToken, Probability p) throws IOException {
    List<Alternative> betterAlternatives = new ArrayList<>();
    boolean alternativesConsidered = false;
    for (Replacement replacement : REPLACEMENTS) {
      Optional<List<Alternative>> alternatives = getBetterAlternatives(replacement, prevToken, token, next, googleToken, p);
      if (alternatives.isPresent()) {
        betterAlternatives.addAll(alternatives.get());
        alternativesConsidered = true;
      }
    }
    return new Alternatives(betterAlternatives, alternativesConsidered);
  }
  
  private Optional<List<Alternative>> getBetterAlternatives(Replacement replacement, GoogleToken prevToken, String token, GoogleToken next, GoogleToken googleToken, Probability p) throws IOException {
    Optional<AnalyzedToken> reading = getByPosTag(googleToken.getPosTags(), replacement.tag);
    List<Alternative> betterAlternatives = new ArrayList<>();
    if (reading.isPresent()) {
      Synthesizer synthesizer = language.getSynthesizer();
      if (synthesizer != null) {
        String[] forms = synthesizer.synthesize(new AnalyzedToken(token, "not_used", reading.get().getLemma()), replacement.alternativeTag);
        for (String alternativeToken : forms) {
          if (alternativeToken.equals(token)) {
            continue;
          }
          List<String> ngram = Arrays.asList(prevToken.token, token, next.token);
          List<String> alternativeNgram = Arrays.asList(prevToken.token, alternativeToken, next.token);
          Probability alternativeProbability = lm.getPseudoProbability(alternativeNgram);
          if (alternativeProbability.getProb() >= p.getProb()) {  // TODO: consider a factor?
            debug("More probable alternative to '%s': %s\n", ngram, alternativeNgram);
            betterAlternatives.add(new Alternative(alternativeToken, alternativeProbability));
          } else {
            debug("Less probable alternative to '%s': %s\n", ngram, alternativeNgram);
          }
        }
        return Optional.of(betterAlternatives);
      }
    }
    return Optional.empty();
  }

  private Optional<AnalyzedToken> getByPosTag(Set<AnalyzedToken> tokens, String wantedPosTagRegex) {
    for (AnalyzedToken token : tokens) {
      if (token.getPOSTag() != null && token.getPOSTag().matches(wantedPosTagRegex)) {
        return Optional.of(token);
      }
    }
    return Optional.empty();
  }

  @Override
  public String getDescription() {
    return "Assume errors for phrases (ngrams) that occur rarely in a reference index";
  }

  @Override
  public void reset() {
  }

  protected Tokenizer getGoogleStyleWordTokenizer() {
    return language.getWordTokenizer();
  }
  
  private void debug(String message, Object... vars) {
    if (DEBUG) {
      System.out.printf(Locale.ENGLISH, message, vars);
    }
  }
  
  static class Replacement {
    final String tag;
    final String alternativeTag;
    Replacement(String tag, String alternativeTag) {
      this.tag = tag;
      this.alternativeTag = alternativeTag;
    }
  }

  class Alternative {
    final String token;
    final Probability p;
    Alternative(String token, Probability p) {
      this.token = token;
      this.p = p;
    }
  }
  
  class Alternatives {
    final List<Alternative> alternatives;
    final boolean alternativesConsidered;
    Alternatives(List<Alternative> alternatives, boolean alternativesConsidered) {
      this.alternatives = alternatives;
      this.alternativesConsidered = alternativesConsidered;
    }
  }
  
}
