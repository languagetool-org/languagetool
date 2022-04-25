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
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Ignore;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;

@Ignore
public class LanguageToolFilterTest extends BaseTokenStreamTestCase {
  
  public void testFilter() throws Exception {
    String input = "How to?";

    Tokenizer stream = new AnyCharTokenizer();
    stream.setReader(new StringReader(input));
    LanguageToolFilter filter = new LanguageToolFilter(stream, new JLanguageTool(new English()), false);
    //displayTokensWithFullDetails(filter);

    String start = "_POS_SENT_START";
    assertTokenStreamContents(filter, 
        new String[] { start, "How", "_LEMMA_how", "_POS_WRB", "to",   "_LEMMA_to", "_POS_TO", "_LEMMA_to", "_POS_IN", "?", "_LEMMA_?", "_POS_PCT",  "_POS_SENT_END" },
        new int[]    { 0,     0,     0,            0,          4,      4,           4,         4,           4,         6,   6,           6,          6 }, 
        new int[]    { 0,     3,     3,            3,          6,      6,           6,         6,           6,         7,   7,           7,          7},
        new String[] { "pos", "word", "pos",       "pos",      "word", "pos",       "pos",     "pos",       "pos",     "word", "pos",   "pos",       "pos" },
        new int[]    { 1,     1,     0,            0,          1,      0,           0,         0,           0,         1,      0,        0,          0 },
        7);
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
      System.out.print("[" + term + ":" + offset.startOffset() + "->"
          + offset.endOffset() + ":" + type.type() + "] ");
    }
    System.out.println();
  }
}
