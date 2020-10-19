/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.rules;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

/**
 * Rule-based disambiguator.
 * Implements an idea by Agnes Souque.
 * 
 * @author Marcin Miłkowski
 */
public class XmlRuleDisambiguator extends AbstractDisambiguator {

  private static final String DISAMBIGUATION_FILE = "disambiguation.xml";

  private final BitSet unhintedRules = new BitSet();
  private final Map<String, BitSet> hintedRulesSensitive = new HashMap<>();
  private final Map<String, BitSet> hintedRulesInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private final List<DisambiguationPatternRule> disambiguationRules;

  public XmlRuleDisambiguator(Language language) {
    Objects.requireNonNull(language);
    String disambiguationFile = language.getShortCode() + "/" + DISAMBIGUATION_FILE;
    try {
      disambiguationRules = loadPatternRules(disambiguationFile);
    } catch (Exception e) {
      throw new RuntimeException("Problems with loading disambiguation file: " + disambiguationFile, e);
    }
    for (int i = 0; i < disambiguationRules.size(); i++) {
      registerHints(disambiguationRules.get(i), i);
    }
  }

  private void registerHints(DisambiguationPatternRule rule, int index) {
    for (PatternToken token : rule.getPatternTokens()) {
      Set<String> hints = token.calcFormHints();
      if (hints != null) {
        Map<String, BitSet> map = token.isCaseSensitive() ? hintedRulesSensitive : hintedRulesInsensitive;
        for (String hint : hints) {
          map.computeIfAbsent(hint, __ -> new BitSet()).set(index);
        }
        return;
      }
    }
    unhintedRules.set(index);
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    BitSet toCheck = getRelevantRules(input);
    AnalyzedSentence sentence = input;
    int i = -1;
    while (true) {
      i = toCheck.nextSetBit(i + 1);
      if (i < 0) break;
      sentence = disambiguationRules.get(i).replace(sentence);
    }
    return sentence;
  }

  @NotNull
  private BitSet getRelevantRules(AnalyzedSentence input) {
    BitSet toCheck = unhintedRules;
    for (AnalyzedTokenReadings readings : input.getTokensWithoutWhitespace()) {
      BitSet rules = hintedRulesSensitive.get(readings.getToken());
      if (rules != null) {
        if (toCheck == unhintedRules) {
          toCheck = (BitSet) toCheck.clone();
        }
        toCheck.or(rules);
      }
      rules = hintedRulesInsensitive.get(readings.getToken());
      if (rules != null) {
        if (toCheck == unhintedRules) {
          toCheck = (BitSet) toCheck.clone();
        }
        toCheck.or(rules);
      }
    }
    return toCheck;
  }

  /**
   * Load disambiguation rules from an XML file. Use {@link JLanguageTool#addRule} to add
   * these rules to the checking process.
   * @return a List of {@link DisambiguationPatternRule} objects
   */
  protected List<DisambiguationPatternRule> loadPatternRules(String filename) throws ParserConfigurationException, SAXException, IOException {
    DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
    return ruleLoader.getRules(JLanguageTool.getDataBroker().getFromResourceDirAsStream(filename));
  }

}
