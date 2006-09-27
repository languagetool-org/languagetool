package de.danielnaber.languagetool.tagging.fr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

import junit.framework.TestCase;

public class FrenchTaggerTest extends TestCase {
    private FrenchTagger tagger;
      private WordTokenizer tokenizer;
      
      public void setUp() {
        tagger = new FrenchTagger();
        tokenizer = new WordTokenizer();
      }

      public void testTagger() throws IOException {
        myAssert("C'est la vie.", "C/[c]N m sp|C/[c]R dem e s est/[être]V etre ind pres 3 s|est/[est]J e p|est/[est]J e s|est/[est]N m s la/[le]D f s|la/[la]N m sp|la/[la]R pers obj 3 f s vie/[vie]N f s");
        myAssert("Je ne parle pas français.", "Je/[je]R pers suj 1 s ne/[ne]A parle/[parler]V sub pres 1 s|parle/[parler]V sub pres 3 s|parle/[parler]V imp pres 2 s|parle/[parler]V ind pres 1 s|parle/[parler]V ind pres 3 s pas/[pas]A|pas/[pas]N m sp français/[français]J m p|français/[français]J m s|français/[français]N m p|français/[français]N m s");
        myAssert("blablabla","blablabla/[blablabla]N m sp");
        myAssert("non_existing_word","non_existing_word/[null]null");
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
