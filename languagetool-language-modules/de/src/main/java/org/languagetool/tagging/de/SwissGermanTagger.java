package org.languagetool.tagging.de;

import java.io.IOException;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * @since 4.4
 */
public class SwissGermanTagger extends GermanTagger {

  /* (non-Javadoc)
   * @see org.languagetool.tagging.de.GermanTagger#tag(java.util.List, boolean)
   */
	@Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens, boolean ignoreCase) throws IOException {
  	List<AnalyzedTokenReadings> tokens = super.tag(sentenceTokens, ignoreCase);
    for (int i = 0; i < tokens.size(); i++) {
    	AnalyzedTokenReadings reading = tokens.get(i);
      if (reading != null &&
      		reading.getToken() != null &&
      		reading.getToken().contains("ss") &&
      		!reading.isTagged()) {
        AnalyzedTokenReadings replacementReading = lookup(reading.getToken().replace("ss", "ß"));
        if(replacementReading != null) {
          for(AnalyzedToken at : replacementReading.getReadings()) {
          	reading.addReading(new AnalyzedToken(reading.getToken(), at.getPOSTag(), at.getLemma()));
          }
        }
      }
    }
    return tokens;
  }
}
