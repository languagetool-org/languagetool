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
	    myAssert("To jest duży dom.", "To/conj jest/verb:fin:sg:ter:imperf duży/adj:sg:nom.acc:m+adj:sg:nom:m dom/subst:sg:nom.acc:m3+subst:sg:nom:m");
	    myAssert("Krowa pasie się na pastwisku.", "Krowa/subst:sg:nom:f pasie/subst:pl:nom.voc:m1+subst:sg:loc.voc:m+subst:sg:loc.voc:m3 się/qub na/prep:loc pastwisku/subst:sg:dat:m+subst:sg:dat:n+subst:sg:loc.voc:m+subst:sg:loc:n");
	    myAssert("blablabla","blablabla/null");
	  }

	  private void myAssert(String input, String expected) throws IOException {
	    List tokens = tokenizer.tokenize(input);
	    List noWhitespaceTokens = new ArrayList();
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
	      //FIXME: check for multiple readings
	      outputStr.append(token.getAnalyzedToken(0));
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
