package de.danielnaber.languagetool.tagging.disambiguation.rules.en;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationRuleLoader;
import de.danielnaber.languagetool.tagging.disambiguation.rules.RuleDisambiguator;
import de.danielnaber.languagetool.tools.Tools;

public class EnglishRuleDisambiguator extends RuleDisambiguator {

  static final String DISAMB_FILE = "disambiguation.xml";
  private List<DisambiguationPatternRule> disambiguationRules = null;

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
          "/resource/en/" + DISAMB_FILE;
        disambiguationRules = loadPatternRules(defaultPatternFilename);
      }
      for (final DisambiguationPatternRule dr : disambiguationRules) {
        sentence = dr.replace(sentence);
      }
    } catch (final ParserConfigurationException e) {
      throw new RuntimeException("Problems with parsing disambiguation file: " 
          + Language.ENGLISH.getShortName() + "/" + DISAMB_FILE + e.getMessage(), e);
    } catch (final SAXException e) {
      throw new RuntimeException("Problems with parsing disambiguation file: " 
          + e.getMessage(), e);
    }
    return sentence; 
  }
}
