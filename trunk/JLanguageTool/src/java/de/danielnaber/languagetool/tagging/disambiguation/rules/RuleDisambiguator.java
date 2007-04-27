package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;

/**
 * Rule-based disambiguator.
 * Implements an idea by Agnes Souque.   
 * 
 * @author Marcin Miłkowski
 *
 */
public class RuleDisambiguator implements Disambiguator {
  
  private static String DISAMB_FILE = "disambiguation.xml";
  private List<DisambiguationPatternRule> disambiguationRules = null;
  public Language language = null; 
  
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
    DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();    
    return ruleLoader.getRules(this.getClass().getResourceAsStream(filename));
  }
  
  public void setLanguage(final Language l) {
    language = l;
  }
  
  public AnalyzedSentence disambiguate(final AnalyzedSentence input) throws IOException {
    AnalyzedSentence sentence = input;
    try {
    if (disambiguationRules == null) {
   String defaultPatternFilename = 
        "/resource/" + language.getShortName() + "/" + DISAMB_FILE;
    disambiguationRules = loadPatternRules(defaultPatternFilename);
    }
        
    for (DisambiguationPatternRule dr : disambiguationRules) {
      sentence = dr.replace(sentence);
    }
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Problems with parsing disambiguation file: " 
          + language.getShortName()+ "/" + DISAMB_FILE + e.getMessage(), e);
    } catch (SAXException e) {
      throw new RuntimeException("Problems with parsing disambiguation file: " 
          + e.getMessage(), e);
    }
    return sentence;
  }

}
