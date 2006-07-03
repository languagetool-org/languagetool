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

  public void testTagger() {
    myAssert("This is a big house.", "This/DT is/VBZ a/DT big/JJ house/NN");
    myAssert("This is a bigger house.", "This/DT is/VBZ a/DT bigger/JJR house/NN");
    myAssert("He doesn't believe me.", "He/PRP doesn/VBZ t/RB believe/VBP me/PRP");
  }

  private void myAssert(String input, String expected) {
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
      AnalyzedTokenReadings atr = (AnalyzedTokenReadings) iter.next();
      outputStr.append(atr);
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
