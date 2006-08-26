package de.danielnaber.languagetool.tagging.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

import junit.framework.TestCase;

public class SpanishTaggerTest extends TestCase {
    private SpanishTagger tagger;
      private WordTokenizer tokenizer;
      
      public void setUp() {
        tagger = new SpanishTagger();
        tokenizer = new WordTokenizer();
      }

      public void testTagger() throws IOException {
        myAssert("Soy un hombre muy honrado.", "Soy/VSIP1S0 un/DI0MS0 hombre/I muy/RG honrado/VMP00SM");
        myAssert("Tengo que ir a mi casa.", "Tengo/VMIP1S0 que/PR0CN000 ir/VMN0000 a/NCFS000 mi/DP1CSS casa/NCFS000");
        myAssert("blablabla","blablabla/null");
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
