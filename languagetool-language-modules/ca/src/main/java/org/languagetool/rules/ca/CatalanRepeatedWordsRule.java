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
package org.languagetool.rules.ca;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.csRegex;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Catalan;
import org.languagetool.rules.AbstractRepeatedWordsRule;
import org.languagetool.rules.SynonymsData;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

public class CatalanRepeatedWordsRule extends AbstractRepeatedWordsRule {

  private static final CatalanSynthesizer synth = new CatalanSynthesizer(new Catalan());
  
private final Supplier<List<DisambiguationPatternRule>> antiPatterns;
  
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays
      .asList(
          Arrays.asList(csRegex("[Tt]ema|TEMA"), csRegex("\\d+|[IXVC]+"))
          );
  
  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }

  public CatalanRepeatedWordsRule(ResourceBundle messages) {
    super(messages, new Catalan());
    antiPatterns = cacheAntiPatterns(new Catalan(), ANTI_PATTERNS);
    // super.setDefaultTempOff();
  }

  private static final Map<String, SynonymsData> wordsToCheck = loadWords("/ca/synonyms.txt");

  @Override
  protected String getMessage() {
    return "Aquesta paraula apareix en una de les frases anteriors. Podeu substituir-la per un sinònim per a fer més variat el text, llevat que la repetició sigui intencionada.";
  }

  @Override
  public String getDescription() {
    return ("Sinònims per a paraules repetides.");
  }

  @Override
  protected Map<String, SynonymsData> getWordsToCheck() {
    return wordsToCheck;
  }

  @Override
  protected String getShortMessage() {
    return "Estil: paraula repetida";
  }

  @Override
  protected Synthesizer getSynthesizer() {
    return synth;
  }

  @Override
  protected String adjustPostag(String postag) {
    if (postag.contains("CN")) {
      return postag.replaceFirst("CN", "..");
    } else if (postag.contains("MS")) {
      return postag.replaceFirst("MS", "[MC][SN]");
    } else if (postag.contains("FS")) {
      return postag.replaceFirst("FS", "[FC][SN]");
    } else if (postag.contains("MP")) {
      return postag.replaceFirst("MP", "[MC][PN]");
    } else if (postag.contains("FP")) {
      return postag.replaceFirst("FP", "[FC][PN]");
    } else if (postag.contains("CS")) {
      return postag.replaceFirst("CS", "[MFC][SN]"); // also F ?
    } else if (postag.contains("CP")) {
      return postag.replaceFirst("CP", "[MFC][PN]"); // also F ?
    } else if (postag.contains("MN")) {
      return postag.replaceFirst("MN", "[MC][SPN]");
    } else if (postag.contains("FN")) {
      return postag.replaceFirst("FN", "[FC][SPN]");
    }
    return postag;
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int i, boolean sentStart, boolean isCapitalized,
      boolean isAllUppercase) {
    if (isAllUppercase || (isCapitalized && !sentStart)) {
      return true;
    }
    if (tokens[i].hasPosTagStartingWith("NP")) {
      return true;
    }
    return false;
  }

}
