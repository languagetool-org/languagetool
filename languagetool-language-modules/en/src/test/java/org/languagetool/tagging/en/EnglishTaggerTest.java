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
package org.languagetool.tagging.en;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.TestTools;
import org.languagetool.language.English;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EnglishTaggerTest {

  private EnglishTagger tagger;
  private WordTokenizer tokenizer;
  
  @Before
  public void setUp() {
    tagger = new EnglishTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new English());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("This is a big house.",
        "This/[this]DT|This/[this]PDT -- is/[be]VBZ -- a/[a]DT -- big/[big]JJ|big/[big]RB -- house/[house]NN|house/[house]VB|house/[house]VBP", tokenizer, tagger);
    TestTools.myAssert("Marketing do a lot of trouble.",
        "Marketing/[market]VBG|Marketing/[marketing]NN:U -- do/[do]VB|do/[do]VBP -- a/[a]DT -- lot/[lot]NN -- of/[of]IN -- trouble/[trouble]NN:UN|trouble/[trouble]VB|trouble/[trouble]VBP", tokenizer, tagger);
    TestTools.myAssert("Manager use his laptop every day.",
        "Manager/[manager]NN -- use/[use]NN:UN|use/[use]VB|use/[use]VBP -- his/[hi]NNS|his/[his]PRP$ -- laptop/[laptop]NN -- every/[every]DT -- day/[day]NN:UN", tokenizer, tagger);
    TestTools.myAssert("This is a bigger house.",
        "This/[this]DT|This/[this]PDT -- is/[be]VBZ -- a/[a]DT -- bigger/[big]JJR -- house/[house]NN|house/[house]VB|house/[house]VBP", tokenizer, tagger);
    TestTools.myAssert("He doesn't believe me.",
        "He/[he]PRP -- doesn/[do]VBZ -- t/[null]null -- believe/[believe]VB|believe/[believe]VBP -- me/[I]PRP", tokenizer, tagger);
    TestTools.myAssert("It has become difficult.",
        "It/[it]PRP -- has/[have]VBZ -- become/[become]VB|become/[become]VBN|become/[become]VBP -- difficult/[difficult]JJ", tokenizer, tagger); 
  }

  @Test
  public void testLemma() throws IOException {
    EnglishTagger tagger = new EnglishTagger();
    List<String> words = new ArrayList<>();
    words.add("Trump");
    words.add("works");
    List<AnalyzedTokenReadings> aToken = tagger.tag(words);
    
    assertEquals(2, aToken.size());
    assertEquals(4, aToken.get(0).getReadings().size());
    assertEquals(2, aToken.get(1).getReadings().size());

    assertEquals("Trump", aToken.get(0).getReadings().get(0).getLemma());
    assertEquals("trump", aToken.get(0).getReadings().get(1).getLemma());
    assertEquals("trump", aToken.get(0).getReadings().get(2).getLemma());
    assertEquals("trump", aToken.get(0).getReadings().get(3).getLemma());

    assertEquals("work", aToken.get(1).getReadings().get(0).getLemma());
    assertEquals("work", aToken.get(1).getReadings().get(1).getLemma());
  }

}
