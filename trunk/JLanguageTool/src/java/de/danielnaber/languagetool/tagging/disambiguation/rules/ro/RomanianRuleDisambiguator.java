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

package de.danielnaber.languagetool.tagging.disambiguation.rules.ro;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.tagging.disambiguation.rules.AbstractRuleDisambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationRuleLoader;
import de.danielnaber.languagetool.tools.Tools;

public class RomanianRuleDisambiguator extends AbstractRuleDisambiguator {

  private static final String DISAMB_FILE = "disambiguation.xml";
  private List<DisambiguationPatternRule> disambiguationRules;

  /**
   * Load disambiguation rules from an XML file. Use {@link #addRule} to add
   * these rules to the checking process.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @return a List of {@link PatternRule} objects
   */
  private List<DisambiguationPatternRule> loadPatternRules(final String filename) throws ParserConfigurationException, SAXException, IOException {
    final DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();    
    return ruleLoader.getRules(Tools.getStream(filename));
  }

  @Override
  public final AnalyzedSentence disambiguate(final AnalyzedSentence input) throws IOException {
    AnalyzedSentence sentence = input;
    try {
      if (disambiguationRules == null) {
        final String defaultPatternFilename = 
          "/resource/ro/" + DISAMB_FILE;
        disambiguationRules = loadPatternRules(defaultPatternFilename);
      }
      for (final DisambiguationPatternRule dr : disambiguationRules) {
        sentence = dr.replace(sentence);
      }
    } catch (final ParserConfigurationException e) {
      throw new RuntimeException("Problems with parsing disambiguation file: " 
          + Language.ROMANIAN.getShortName() + "/" + DISAMB_FILE + e.getMessage(), e);
    } catch (final SAXException e) {
      throw new RuntimeException("Problems with parsing disambiguation file: " 
          + e.getMessage(), e);
    }
    return sentence; 
  }
}
