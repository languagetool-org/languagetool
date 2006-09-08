package de.danielnaber.languagetool.tagging.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.it.ItalianTagger;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class ItalianTaggerTest extends TestCase {
    private ItalianTagger tagger;
      private WordTokenizer tokenizer;
      
      public void setUp() {
        tagger = new ItalianTagger();
        tokenizer = new WordTokenizer();
      }

      public void testTagger() throws IOException {
        myAssert("Non c'è linguaggio senza inganno.", "Non/ADV c/null è/VER:ind+pres+3+s linguaggio/NOUN-M:s senza/CON inganno/NOUN-M:s");
        myAssert("Amo quelli che desiderano l'impossibile.", "Amo/VER:ind+pres+1+s quelli/DET-DEMO:m+p che/CON desiderano/VER:ind+pres+3+p l/null impossibile/ADJ:pos+f+s");
        myAssert("blablabla","blablabla/null");        
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
