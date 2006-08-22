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
        myAssert("C'est la vie.", "C/N+z1:ms:mp est/V+z1:P3s la/N+z1:ms:mp vie/N+z1:fs");
        myAssert("Je ne parle pas français.", "Je/PRO+z1:1s ne/ADV+z1 parle/V+z1:P1s:P3s:S1s:S3s:Y2s pas/ADV+z1 français/A+z1:ms:mp");
        myAssert("blablabla","blablabla/N+z1:ms:mp");
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
