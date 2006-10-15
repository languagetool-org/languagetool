/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package de.danielnaber.languagetool.tagging.en;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

/**
 * @author Daniel Naber
 */
public class EnglishTaggerTest extends TestCase {

  private EnglishTagger tagger;
  private WordTokenizer tokenizer;
  
  public void setUp() {
    tagger = new EnglishTagger();
    tokenizer = new WordTokenizer();
  }

  public void testTagger() throws IOException {
    myAssert("This is a big house.", "This/[this]DT|This/[this]PDT is/[be]VBZ a/[a]DT big/[big]JJ house/[house]NN|house/[house]VB|house/[house]VBP");
    myAssert("Marketing do a lot of trouble.", "Marketing/[marketing]NN:U|Marketing/[market]VBG do/[do]VB|do/[do]VBP a/[a]DT lot/[lot]JJ|lot/[lot]NN:UN of/[of]IN trouble/[trouble]NN:UN|trouble/[trouble]VB|trouble/[trouble]VBP");
    myAssert("Manager use his laptop every day.", "Manager/[manager]NN use/[use]NN:UN|use/[use]VB|use/[use]VBP his/[his]PRP$|his/[hi]NNS laptop/[laptop]NN every/[every]DT|every/[every]JJ day/[day]NN:UN");
    myAssert("This is a bigger house.", "This/[this]DT|This/[this]PDT is/[be]VBZ a/[a]DT bigger/[big]JJR house/[house]NN|house/[house]VB|house/[house]VBP");
    myAssert("He doesn't believe me.", "He/[he]PRP doesn/[do]VBZ t/[t]JJ|t/[t]NN|t/[t]RB believe/[believe]VB|believe/[believe]VBP me/[I]PRP");
    myAssert("It has become difficult.", "It/[it]PRP has/[have]VBZ become/[become]VB|become/[become]VBN|become/[become]VBP difficult/[difficult]JJ"); 
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
