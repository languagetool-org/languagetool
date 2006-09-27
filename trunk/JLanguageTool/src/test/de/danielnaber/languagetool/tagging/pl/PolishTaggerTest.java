package de.danielnaber.languagetool.tagging.pl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

import junit.framework.TestCase;

public class PolishTaggerTest extends TestCase {
	private PolishTagger tagger;
	  private WordTokenizer tokenizer;
	  
	  public void setUp() {
	    tagger = new PolishTagger();
	    tokenizer = new WordTokenizer();
	  }

	  public void testTagger() throws IOException {
	    myAssert("To jest duży dom.", "To/[to]conj|To/[ten]adj:sg:nom.acc.voc:n1.n2 jest/[być]verb:fin:sg:ter:imperf duży/[duży]adj:sg:nom:m:pneg dom/[dom]subst:sg:nom.acc:m3");
        myAssert("Krowa pasie się na pastwisku.", "Krowa/[krowa]subst:sg:nom:f pasie/[pas]subst:sg:loc.voc:m3|pasie/[paść]verb:irreg się/[siebie]qub na/[na]prep:acc.loc pastwisku/[pastwisko]subst:sg:dat:n+subst:sg:loc:n");
        myAssert("blablabla","blablabla/[null]null");
	  }

      private void myAssert(String input, String expected) throws IOException {
        List tokens = tokenizer.tokenize(input);
        List<String> noWhitespaceTokens = new ArrayList<String>();
        // whitespace confuses tagger, so give it the tokens but no whitespace tokens:
        for (Iterator iterator = tokens.iterator(); iterator.hasNext();) {
          String token = (String) iterator.next();
          if (isWord(token)) {
            noWhitespaceTokens.add(token);
          }
        }
        List output = tagger.tag(noWhitespaceTokens);
        StringBuffer outputStr = new StringBuffer();
        for (Iterator iter = output.iterator(); iter.hasNext();) {
          AnalyzedTokenReadings token = (AnalyzedTokenReadings) iter.next();
          int readingsNumber = token.getReadingsLength();
          for (int j = 0; j < readingsNumber; j++) {
          outputStr.append(token.getAnalyzedToken(j).getToken());
          outputStr.append("/[");
          outputStr.append(token.getAnalyzedToken(j).getLemma());
          outputStr.append("]");
          outputStr.append(token.getAnalyzedToken(j).getPOSTag());
          if (readingsNumber > 1 && j < readingsNumber - 1) {
          outputStr.append("|");
          }
          }
          if (iter.hasNext())
            outputStr.append(" ");
        }
        assertEquals(expected, outputStr.toString());
      }

	  private boolean isWord(String token) {
	    for (int i = 0; i < token.length(); i++) {
	      char c = token.charAt(i);
	      if (Character.isLetter(c) || Character.isDigit(c))
	        return true;
	    }
	    return false;
	  }

}
