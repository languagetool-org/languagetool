/*
 * Created on 23.12.2005
 */
package de.danielnaber.languagetool.tagging.sv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * A trivial tagger that does nothing than assign null
 * tags to words.
 * 
 * @author Daniel Naber
 */
public class SwedishTagger implements Tagger {

  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {

    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    for (Iterator<String> iter = sentenceTokens.iterator(); iter.hasNext();) {
    	String word = iter.next();
        List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      // a real tagger would need to assign a POS tag
      // in the next line instead of null:
      l.add(new AnalyzedToken(word, null, pos));
      pos += word.length();
      tokenReadings.add(new AnalyzedTokenReadings((AnalyzedToken[])l.toArray(new AnalyzedToken[0])));
    }
    return tokenReadings;
  }

  public Object createNullToken(String token, int startPos) {
	  return new AnalyzedTokenReadings(new AnalyzedToken(token, null, startPos));
  }

}
