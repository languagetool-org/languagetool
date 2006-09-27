package de.danielnaber.languagetool.tagging.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class ItalianTaggerTest extends TestCase {
    private ItalianTagger tagger;
      private WordTokenizer tokenizer;
      
      public void setUp() {
        tagger = new ItalianTagger();
        tokenizer = new WordTokenizer();
      }

      public void testTagger() throws IOException {
        myAssert("Non c'è linguaggio senza inganno.", "Non/[non]ADV c/[null]null è/[essere]VER:ind+pres+3+s|è/[essere]AUX:ind+pres+3+s linguaggio/[linguaggio]NOUN-M:s senza/[senza]CON|senza/[senza]PRE inganno/[inganno]NOUN-M:s|inganno/[ingannare]VER:ind+pres+1+s");
        myAssert("Amo quelli che desiderano l'impossibile.", "Amo/[amare]VER:ind+pres+1+s quelli/[quello]DET-DEMO:m+p|quelli/[quelli]PRO-DEMO-M-P che/[che]CON|che/[che]DET-WH:m+p|che/[che]DET-WH:m+s|che/[che]DET-WH:f+p|che/[che]DET-WH:f+s|che/[che]WH-CHE desiderano/[desiderare]VER:ind+pres+3+p l/[null]null impossibile/[impossibile]ADJ:pos+f+s|impossibile/[impossibile]ADJ:pos+m+s");
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
