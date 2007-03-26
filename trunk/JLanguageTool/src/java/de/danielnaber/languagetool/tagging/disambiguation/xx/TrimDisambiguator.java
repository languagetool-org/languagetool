package de.danielnaber.languagetool.tagging.disambiguation.xx;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;

/**
 * Trivial disambiguator.
 * Just cuts out tags from the token. It leaves only the first tag.   
 * 
 * @author Jozef Licko
 */
public class TrimDisambiguator implements Disambiguator {

	public final AnalyzedSentence disambiguate(final AnalyzedSentence input) {
		
		AnalyzedTokenReadings[] anTokens = input.getTokens();
		AnalyzedTokenReadings[] output = 
          new AnalyzedTokenReadings[anTokens.length];
		
		for (int i = 0; i < anTokens.length; i++) {
			
			if (anTokens[i].getReadingsLength() > 1) {
				AnalyzedToken[] firstToken = new AnalyzedToken[1];
				firstToken[0] = anTokens[i].getAnalyzedToken(0);
				
				output[i] = new AnalyzedTokenReadings(firstToken);
			} else {
				output[i] = anTokens[i];
			}
		}
		return new AnalyzedSentence(output);
	}

}
