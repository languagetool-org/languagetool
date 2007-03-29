package de.danielnaber.languagetool.tagging.disambiguation.pl;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.Language;

/**
 * Mainly punctuation chunker for Polish. 
 * Probably could be made more universal.
 *   
 * 
 * @author Marcin Miłkowski
 */
public class PolishChunker implements Disambiguator {
      
    /**
     * Implements punctuation chunking: 
     * SENT_END for sentence end,
     * PARA_END for paragraph end, 
     * &lt;ELLIPSIS&gt; for ellipsis (...) start, 
     * and &lt;/ELLIPSIS&gt; for ellipsis end.
     * @param input The tokens to be chunked.
     * @return AnalyzedSentence with additional markers.
     */
    public final AnalyzedSentence disambiguate(final AnalyzedSentence input) {
          
        AnalyzedTokenReadings[] anTokens = input.getTokens();
        AnalyzedTokenReadings[] output = 
          new AnalyzedTokenReadings[anTokens.length];
        
        output = anTokens;        
        
        /** FIXME: this is a very naive implementation
         * change into constants array?
         */
        for (int i = 0; i < anTokens.length; i++) {
        if (output[i].getToken().equals(".") 
            && i + 2 < anTokens.length) {
              if (anTokens[i + 1].getToken().equals(".") 
                && anTokens[i + 2].getToken().equals(".")) {
               AnalyzedToken ellipsisStart = 
                 new AnalyzedToken(".", "<ELLIPSIS>", "...");
               output[i].addReading(ellipsisStart);
               AnalyzedToken ellipsisEnd = 
                 new AnalyzedToken(".", "</ELLIPSIS>", "...");
               output[i + 2].addReading(ellipsisEnd);
          }
        }
        }
        
        return new AnalyzedSentence(output);
    }

}
