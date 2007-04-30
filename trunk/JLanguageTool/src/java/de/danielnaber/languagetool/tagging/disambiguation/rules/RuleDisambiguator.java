package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.io.IOException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;

/**
 * Rule-based disambiguator.
 * Implements an idea by Agnes Souque.   
 * 
 * @author Marcin Miłkowski
 *
 */
public abstract class RuleDisambiguator implements Disambiguator {
        
  public abstract AnalyzedSentence disambiguate(final AnalyzedSentence input) throws IOException; 

}
