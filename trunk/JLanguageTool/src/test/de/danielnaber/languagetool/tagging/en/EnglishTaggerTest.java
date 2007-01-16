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

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
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
    TestTools.myAssert("This is a big house.", "This/[this]DT|This/[this]PDT is/[be]VBZ a/[a]DT big/[big]JJ house/[house]NN|house/[house]VB|house/[house]VBP", tokenizer, tagger);
    TestTools.myAssert("Marketing do a lot of trouble.", "Marketing/[marketing]NN:U|Marketing/[market]VBG do/[do]VB|do/[do]VBP a/[a]DT lot/[lot]NN of/[of]IN trouble/[trouble]NN:UN|trouble/[trouble]VB|trouble/[trouble]VBP", tokenizer, tagger);
    TestTools.myAssert("Manager use his laptop every day.", "Manager/[manager]NN use/[use]NN:UN|use/[use]VB|use/[use]VBP his/[his]PRP$|his/[hi]NNS laptop/[laptop]NN every/[every]DT day/[day]NN:UN", tokenizer, tagger);
    TestTools.myAssert("This is a bigger house.", "This/[this]DT|This/[this]PDT is/[be]VBZ a/[a]DT bigger/[big]JJR house/[house]NN|house/[house]VB|house/[house]VBP", tokenizer, tagger);
    TestTools.myAssert("He doesn't believe me.", "He/[he]PRP doesn/[do]VBZ t/[t]RB believe/[believe]VB|believe/[believe]VBP me/[I]PRP", tokenizer, tagger);
    TestTools.myAssert("It has become difficult.", "It/[it]PRP has/[have]VBZ become/[become]VB|become/[become]VBN|become/[become]VBP difficult/[difficult]JJ", tokenizer, tagger); 
  }

}
