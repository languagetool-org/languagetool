package de.danielnaber.languagetool.tagging.pl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedToken;
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
    myAssert(
        "To jest duży dom.",
        "To/INDECL|ADJ:IRREG|ADJ:SG:NOM:N+PPRON:NOM.ACC.VOC:SG:N+PPRON:NOM.ACC.VOC:PL:N jest/INDECL|QUB duży/ADJ:SG:NOM.ACC:M+ADJ:SG:NOM:M dom/SUBST:SG:NOM.ACC:M3+SUBST:SG:NOM:M");
    myAssert(
        "Krowa pasie się na pastwisku.",
        "Krowa/SUBST:SG:NOM:F pasie/INDECL|SUBST:IRREG|SUBST:PL:NOM.VOC:M1+SUBST:SG:LOC.VOC:M+SUBST:SG:LOC.VOC:M3|SUBST:SG:LOC.VOC:M+SUBST:SG:LOC.VOC:M3|QUB się/INDECL|QUB na/INDECL pastwisku/SUBST:SG:DAT:M+SUBST:SG:DAT:N+SUBST:SG:LOC.VOC:M+SUBST:SG:LOC:N|SUBST:SG:DAT:N+SUBST:SG:LOC:N");
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
      AnalyzedToken token = (AnalyzedToken) iter.next();
      outputStr.append(token);
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
