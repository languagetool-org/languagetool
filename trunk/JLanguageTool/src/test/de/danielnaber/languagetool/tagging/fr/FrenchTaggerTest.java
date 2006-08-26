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
        myAssert("C'est la vie.", "C/N m sp est/V etre ind pres 3 s la/D f s vie/N f s");
        myAssert("Je ne parle pas français.", "Je/R pers suj 1 s ne/A parle/V sub pres 1 s pas/A français/J m p");
        myAssert("blablabla","blablabla/N m sp");
        myAssert("non_existing_word","non_existing_word/null");
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
