/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.ga;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Irish;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class IrishTaggerTest extends TestCase {
  
  private IrishTagger tagger;
  private WordTokenizer tokenizer;

  @Override
  public void setUp() {
    tagger = new IrishTagger();
    tokenizer = new WordTokenizer();
  }

  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Irish());
  }
  
  public void testTagger() throws IOException {
    TestTools.myAssert("nonexistingword","nonexistingword/[null]null", tokenizer, tagger);
    TestTools.myAssert("bhí sé anseo","bhí/[bí]Noun:Fem:Com:Sg:DefArt|bhí/[bí]Noun:Fem:Com:Sg:Len|bhí/[bí]Noun:Fem:Gen:Sg:Len|bhí/[bí]Noun:Fem:Voc:Sg:Len|bhí/[bí]Verb:VI:PastInd:Len -- sé/[is]Cop:Pres:Pron:Pers:3P:Sg:Masc|sé/[sé]Noun:Masc:Com:Sg|sé/[sé]Noun:Masc:Com:Sg:DefArt|sé/[sé]Noun:Masc:Com:Sg:Ecl|sé/[sé]Noun:Masc:Gen:Sg|sé/[sé]Noun:Masc:Gen:Sg:Ecl|sé/[sé]Num:Card|sé/[sé]Num:Card:Ecl|sé/[sé]Pron:Pers:3P:Sg:Masc:Sbj -- anseo/[anseo]Adv:Loc", tokenizer, tagger);
    TestTools.myAssert("t-athair","t-athair/[athair]Noun:Masc:Com:Sg:DefArt", tokenizer, tagger);
    TestTools.myAssert("tAthair","tAthair/[athair]Noun:Masc:Com:Sg:DefArt", tokenizer, tagger);
    TestTools.myAssert("nAthair","nAthair/[athair]Noun:Fem:Com:Sg:Ecl|nAthair/[athair]Noun:Masc:Com:Sg:Ecl", tokenizer, tagger);
    TestTools.myAssert("t-seomra","t-seomra/[null]null|t-seomra/[seomra]Noun:Masc:Com:Sg:DefArt:MorphError", tokenizer, tagger);
  }

}
