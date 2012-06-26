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
package org.languagetool.dev.index;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

public class LanguageToolFilterTest extends BaseTokenStreamTestCase {
  
  public void testFilter() throws Exception {
    String input = "How do you thin?";

    TokenStream stream = new AnyCharTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
    LanguageToolFilter filter = new LanguageToolFilter(stream, new JLanguageTool(Language.ENGLISH));
    //displayTokensWithFullDetails(filter);

    stream = new AnyCharTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
    filter = new LanguageToolFilter(stream, new JLanguageTool(Language.ENGLISH));

    assertTokenStreamContents(filter, new String[] { "_POS_SENT_START", "How", "_POS_WRB", "do",
        "_POS_VBP", "_POS_VB", "you", "_POS_PRP", "thin", "_POS_VBP", "_POS_VB", "_POS_JJ", "?",
        "_POS_SENT_END" }, new int[] { 0, 0, 0, 4, 4, 4, 7, 7, 11, 11, 11, 11, 15, 15 }, new int[] {
        0, 3, 3, 6, 6, 6, 10, 10, 15, 15, 15, 15, 16, 16 }, new String[] { "pos", "word", "pos",
        "word", "pos", "pos", "word", "pos", "word", "pos", "pos", "pos", "word", "pos" },
        new int[] { 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0 }, 16);
  }

  private static void displayTokensWithFullDetails(TokenStream stream) throws IOException {
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);
    OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
    TypeAttribute type = stream.addAttribute(TypeAttribute.class);
    int position = 0;
    while (stream.incrementToken()) {
      int increment = posIncr.getPositionIncrement();
      if (increment > 0) {
        position = position + increment;
        System.out.println();
        System.out.print(position + ": ");
      }
      System.out.print("[" + term.toString() + ":" + offset.startOffset() + "->"
          + offset.endOffset() + ":" + type.type() + "] ");
    }
    System.out.println();
  }
}
