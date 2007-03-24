package de.danielnaber.languagetool.tagging.disambiguation.xx;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;

/**
 * Trivial disambiguator.
 * Does nothing at all. Just copies input to output.   
 * 
 * @author Jozef Licko
 *
 */
public class DemoDisambiguator implements Disambiguator {

	public AnalyzedSentence disambiguate(AnalyzedSentence input) {
		return input;
	}

}
