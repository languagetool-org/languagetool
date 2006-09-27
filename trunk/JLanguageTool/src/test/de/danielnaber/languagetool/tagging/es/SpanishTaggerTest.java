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
        myAssert("Soy un hombre muy honrado.", "Soy/[ser]VSIP1S0 un/[uno]DI0MS0|un/[1]Z hombre/[hombre]I|hombre/[hombre]NCMS000 muy/[mucho]RG honrado/[honrar]VMP00SM");
        myAssert("Tengo que ir a mi casa.", "Tengo/[tener]VMIP1S0 que/[que]PR0CN000|que/[que]CS ir/[ir]VMN0000 a/[a]NCFS000|a/[a]SPS00 mi/[mi]DP1CSS casa/[casa]NCFS000|casa/[casar]VMIP3S0|casa/[casar]VMM02S0");
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
