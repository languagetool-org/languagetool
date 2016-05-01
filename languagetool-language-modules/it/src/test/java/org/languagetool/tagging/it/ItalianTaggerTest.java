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
package org.languagetool.tagging.it;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Italian;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class ItalianTaggerTest {

  private ItalianTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new ItalianTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Italian());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Non c'è linguaggio senza inganno.",
        "Non/[non]ADV -- c/[C]NPR -- è/[essere]AUX:ind+pres+3+s|è/[essere]VER:ind+pres+3+s -- linguaggio/[linguaggio]NOUN-M:s -- senza/[senza]CON|senza/[senza]PRE -- inganno/[ingannare]VER:ind+pres+1+s|inganno/[inganno]NOUN-M:s", tokenizer, tagger);
    TestTools.myAssert("Amo quelli che desiderano l'impossibile.",
        "Amo/[amare]VER:ind+pres+1+s -- quelli/[quelli]PRO-DEMO-M-P|quelli/[quello]DET-DEMO:m+p -- che/[che]CON|che/[che]DET-WH:f+p|che/[che]DET-WH:f+s|che/[che]DET-WH:m+p|che/[che]DET-WH:m+s|che/[che]WH-CHE -- desiderano/[desiderare]VER:ind+pres+3+p -- l/[null]null -- impossibile/[impossibile]ADJ:pos+f+s|impossibile/[impossibile]ADJ:pos+m+s", tokenizer, tagger);
    TestTools.myAssert("blablabla", "blablabla/[null]null", tokenizer, tagger);        
  }

}
