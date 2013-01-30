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
package org.languagetool.tagging.ca;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class CatalanTaggerTest extends TestCase {

  private CatalanTagger tagger;
  private WordTokenizer tokenizer;

  @Override
  public void setUp() {
    tagger = new CatalanTagger();
    tokenizer = new WordTokenizer();
  }
  
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Catalan());
  }

  public void testTagger() throws IOException {
    TestTools.myAssert("Sóc un home molt honrat.",
        "Sóc/[ser]VSIP1S0 -- un/[un]DI0MS0|un/[un]PI0MS000 -- home/[home]I|home/[home]NCMS000 -- molt/[molt]DI0MS0|molt/[molt]PI0MS000|molt/[molt]RG -- honrat/[honrar]VMP00SM", tokenizer, tagger);
// Need to fix the separator character: al - a+el+SP+DA
//    TestTools.myAssert("Frase recitada al matí.",
//        "Frase/[frase]NCFS000 -- recitada/[recitar]VMP00SF -- al/[a]el+SP+DA -- matí/[matar]VMIS1S0|[matí]NCMS000", tokenizer, tagger);
    TestTools.myAssert("blablabla","blablabla/[null]null", tokenizer, tagger);        
  }
}
