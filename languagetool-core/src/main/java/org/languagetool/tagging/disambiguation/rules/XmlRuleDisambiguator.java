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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.xml.sax.SAXException;

/**
 * Rule-based disambiguator.
 * Implements an idea by Agnes Souque.
 * 
 * @author Marcin Miłkowski
 */
public class XmlRuleDisambiguator extends AbstractDisambiguator {

  private static final String DISAMBIGUATION_FILE = "disambiguation.xml";

  private final List<DisambiguationPatternRule> disambiguationRules;

  public XmlRuleDisambiguator(Language language) {
    Objects.requireNonNull(language);
    String disambiguationFile = language.getShortCode() + "/" + DISAMBIGUATION_FILE;
    try {
      disambiguationRules = loadPatternRules(disambiguationFile);
    } catch (Exception e) {
      throw new RuntimeException("Problems with loading disambiguation file: " + disambiguationFile, e);
    }
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    AnalyzedSentence sentence = input;
    for (DisambiguationPatternRule patternRule : disambiguationRules) {
      sentence = patternRule.replace(sentence);
    }
    return sentence;
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
