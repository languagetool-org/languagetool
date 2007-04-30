package de.danielnaber.languagetool.tagging.disambiguation.rules.fr;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationRuleLoader;
import de.danielnaber.languagetool.tagging.disambiguation.rules.RuleDisambiguator;

public class FrenchRuleDisambiguator extends RuleDisambiguator {

  static final String DISAMB_FILE = "disambiguation.xml";
  private List<DisambiguationPatternRule> disambiguationRules = null;
  private Language language;
  
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
  
  public final AnalyzedSentence disambiguate(final AnalyzedSentence input) throws IOException {
    AnalyzedSentence sentence = input;
    try {
      if (disambiguationRules == null) {
        String defaultPatternFilename = 
          "/resource/fr/" + DISAMB_FILE;
        disambiguationRules = loadPatternRules(defaultPatternFilename);
      }
      if (language == null) {
        language = Language.FRENCH;
      }
      for (DisambiguationPatternRule dr : disambiguationRules) {
        sentence = dr.replace(sentence);
      }

    /*  if (!input.toString().equals(sentence.toString())) {
        System.err.println("INPUT:" + input.toString());
        System.err.println("OUTPUT:" + sentence.toString());
      } */   
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
