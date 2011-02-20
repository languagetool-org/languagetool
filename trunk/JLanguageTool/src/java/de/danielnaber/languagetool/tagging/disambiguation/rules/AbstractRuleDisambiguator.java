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

package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import org.xml.sax.SAXException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tools.Tools;

/**
 * Rule-based disambiguator.
 * Implements an idea by Agnes Souque.   
 * 
 * @author Marcin Miłkowski
 *
 */
public abstract class AbstractRuleDisambiguator implements Disambiguator {

  protected static final String DISAMBIGUATION_FILE = "disambiguation.xml";
  protected List<DisambiguationPatternRule> disambiguationRules;

  protected abstract Language getLanguage();

  @Override
  public AnalyzedSentence disambiguate(final AnalyzedSentence input) throws IOException {
    AnalyzedSentence sentence = input;
    if (disambiguationRules == null) {
      final String disambiguationFile =
        JLanguageTool.getDataBroker().getResourceDir() + "/" + getLanguage().getShortName() + "/" + DISAMBIGUATION_FILE;
      try {
        disambiguationRules = loadPatternRules(disambiguationFile);
      } catch (final Exception e) {
        throw new RuntimeException("Problems with parsing disambiguation file: "
            + disambiguationFile, e);
      }
    }
    for (final DisambiguationPatternRule patternRule : disambiguationRules) {
      sentence = patternRule.replace(sentence);
    }
    return sentence;
  }

  /**
   * Load disambiguation rules from an XML file. Use {@link de.danielnaber.languagetool.JLanguageTool#addRule} to add
   * these rules to the checking process.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @return a List of {@link DisambiguationPatternRule} objects
   */
  protected List<DisambiguationPatternRule> loadPatternRules(final String filename) throws ParserConfigurationException, SAXException, IOException {
    final DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();    
    return ruleLoader.getRules(Tools.getStream(filename));
  }


}
