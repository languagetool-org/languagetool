/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.AbstractRepeatedWordsRule;
import org.languagetool.rules.SynonymsData;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

public class EnglishRepeatedWordsRule extends AbstractRepeatedWordsRule{
  
  private static final EnglishSynthesizer synth = new EnglishSynthesizer(new AmericanEnglish());

  private final Supplier<List<DisambiguationPatternRule>> antiPatterns;

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      new PatternTokenBuilder().csToken("need").matchInflectedForms().build(),   // "I still need -> require to sign in"
      token("to")
    ),

    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("solve(s|d|ing)?").setSkip(3).build(),   // "solve the problem" is a unique collocation
      tokenRegex("problems?")                                                    // "solve the issue|concern|difficulty" sounds bizarre
    ),                                                                             // lots of disables in Matomo

    Arrays.asList(
      posRegex("SENT_START|PCT"),       // "No problem, I'm not in a rush."
      token("no"),
      token("problem"),
      pos("PCT")
    ),

    Arrays.asList(
      tokenRegex("math|word"),       // "math/word problem"
      tokenRegex("problems?")
    ),

    Arrays.asList(
      token("more"),
      token("often"),
      token("than"),
      token("not")
    ),

    Arrays.asList(
      token("often"),
      token("times")
    ),

    Arrays.asList(
      tokenRegex("details?|facts?|it|journals?|questions?|research|results?|study|studies|this|these|those|which"),
      new PatternTokenBuilder().pos("RB").min(0).build(),
      new PatternTokenBuilder().csToken("suggest").matchInflectedForms().build()
    ),

    Arrays.asList(
      new PatternTokenBuilder().csToken("form").matchInflectedForms().build(),   // "form in the bloodstream"
      posRegex("IN|PCT|RP|TO|SENT_END")
    ),

    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("bonds?|crystals?|ions?|rocks?|.*valence").setSkip(10).build(),
      new PatternTokenBuilder().csToken("form").matchInflectedForms().build()
    ),

    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("form(s|ed|ing)?").setSkip(10).build(),
      tokenRegex("bonds?|crystals?|ions?|rocks?|.*valence")
    ),

    Arrays.asList(
      token("interesting"),
      tokenRegex("facts?|things?")
    ),

    Arrays.asList(
      token("several"),
      tokenRegex("hundreds?|thousands?|millions?")
    ),

    Arrays.asList(
      token("must"),
      token("be"),
      token("nice")
    ),

    Arrays.asList(
      token("nice"),
      token("day")
    ),

    Arrays.asList(
      token("nice"),
      token("to"),
      new PatternTokenBuilder().token("meet").min(0).build(),
      posRegex("PRP_O.*")
    ),

    Arrays.asList(
      new PatternTokenBuilder().csToken("be").matchInflectedForms().build(),  // nice and plump
      token("nice"),
      token("and"),
      pos("JJ"),
      posRegex("PCT|SENT_END")
    ),

    Arrays.asList(
      posRegex("P?DT|PRP$.*"),  // the proposed agreement
      token("proposed"),
      posRegex("N.*")
    ),

    Arrays.asList(
      new PatternTokenBuilder().csToken("propose").matchInflectedForms().build(),
      tokenRegex("to|marriage")
    ),

    Arrays.asList(
      token("too"),
      token("literally")
    ),

    Arrays.asList(
      token("literally"),
      token("and"),
      token("figuratively")
    ),

    Arrays.asList(
      token("literally"),
      token("everything")
    ),

    Arrays.asList(
      token("literally"),
      posRegex("PCT|SENT_END")
    )

  );

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }

  public EnglishRepeatedWordsRule(ResourceBundle messages) {
    super(messages, new AmericanEnglish());
    antiPatterns = cacheAntiPatterns(new AmericanEnglish(), ANTI_PATTERNS);
    //super.setDefaultTempOff();
  }
  
  private static final Map<String, SynonymsData> wordsToCheck = loadWords("/en/synonyms.txt");
  
  @Override
  protected String getMessage() {
    return "This word has been used in one of the immediately preceding sentences. Using a synonym could make your text more interesting to read, unless the repetition is intentional.";
  }

  @Override
  public String getDescription() {
    return ("Suggest synonyms for repeated words.");
  }

  @Override
  protected Map<String, SynonymsData> getWordsToCheck() {
    return wordsToCheck;
  }

  @Override
  protected String getShortMessage() {
    return "Style: repeated word";
  }

  @Override
  protected Synthesizer getSynthesizer() {
    return synth;
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int i, boolean sentStart, boolean isCapitalized,
      boolean isAllUppercase) {
    if (isAllUppercase || (isCapitalized && !sentStart)) {
      return true;
    }
    if (tokens[i].hasPosTagStartingWith("NNP")) {
      return true;
    }
    return false;
  }

}
