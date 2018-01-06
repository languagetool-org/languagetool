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
package org.languagetool.tagging.nl;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Dutch;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class DutchTaggerTest {
    
  private DutchTagger tagger;
  private WordTokenizer tokenizer;
      
  @Before
  public void setUp() {
    tagger = new DutchTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Dutch());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Dit is een Nederlandse zin om het programma'tje te testen.",
        "Dit/[null]null -- is/[is]ZNW:EKV|is/[zijn]WKW:TGW:3EP -- een/[een]GET|een/[een]ZNW:EKV:DE_ -- Nederlandse/[Nederlandse]ZNW:EKV -- zin/[zin]ZNW:EKV:DE_|zin/[zinnen]WKW:TGW:1EP -- om/[om]VRZ -- het/[null]null -- programma/[programma]ZNW:EKV:HET -- tje/[null]null -- te/[te]VRZ -- testen/[test]ZNW:MRV:DE_|testen/[testen]WKW:TGW:INF", tokenizer, tagger);        
    TestTools.myAssert("zwijnden", "zwijnden/[zwijnen]WKW:VLT:INF", tokenizer, tagger);        
  }

}
