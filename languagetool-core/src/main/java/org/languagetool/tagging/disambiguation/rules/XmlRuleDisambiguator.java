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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.RuleSet;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Rule-based disambiguator. Implements an idea by Agnes Souque.
 * 
 * @author Marcin Miłkowski
 */
public class XmlRuleDisambiguator extends AbstractDisambiguator {

  private static final String DISAMBIGUATION_FILE = "disambiguation.xml";
  private static final String GLOBAL_DISAMBIGUATION_FILE = "disambiguation-global.xml";

  private final RuleSet disambiguationRules;
  
  public XmlRuleDisambiguator(Language language) {
    // by default, don't use global disambiguation (for now)
    this(language, false);
  }

  public XmlRuleDisambiguator(Language language, boolean useGlobalDisambiguation) {
    Objects.requireNonNull(language);
    String disambiguationFile = language.getShortCode() + "/" + DISAMBIGUATION_FILE;
    List<DisambiguationPatternRule> disambiguationRulesList;
    try {
      disambiguationRulesList = loadPatternRules(disambiguationFile);
    } catch (Exception e) {
      throw new RuntimeException("Problems with loading disambiguation file: " + disambiguationFile, e);
    }
    if (useGlobalDisambiguation) {
      // disambiguation-global.xml
      try {
        disambiguationRulesList.addAll(loadPatternRules(GLOBAL_DISAMBIGUATION_FILE));
      } catch (Exception e) {
        throw new RuntimeException("Problems with loading global disambiguation file: " + GLOBAL_DISAMBIGUATION_FILE, e);
      }
    }
    disambiguationRules = RuleSet.textHinted(disambiguationRulesList);
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    return disambiguate(input, null);
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence sentence,
      @Nullable JLanguageTool.CheckCancelledCallback checkCanceled) throws IOException {
    for (Rule rule : disambiguationRules.rulesForSentence(sentence)) {
      if (checkCanceled != null && checkCanceled.checkCancelled()) {
        break;
      }
      sentence = ((DisambiguationPatternRule) rule).replace(sentence);
    }
    return sentence;
  }

  /**
   * Load disambiguation rules from an XML file. Use {@link JLanguageTool#addRule}
   * to add these rules to the checking process.
   * 
   * @return a List of {@link DisambiguationPatternRule} objects
   */
  protected List<DisambiguationPatternRule> loadPatternRules(String filename)
      throws ParserConfigurationException, SAXException, IOException {
    DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
    return ruleLoader.getRules(JLanguageTool.getDataBroker().getFromResourceDirAsStream(filename));
  }

}
