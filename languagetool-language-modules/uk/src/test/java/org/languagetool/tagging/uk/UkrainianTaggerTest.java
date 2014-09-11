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
package org.languagetool.tagging.uk;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;

import java.io.IOException;

public class UkrainianTaggerTest extends TestCase {
    
  private UkrainianTagger tagger;
  private UkrainianWordTokenizer tokenizer;
      
  @Override
  public void setUp() {
    tagger = new UkrainianTagger();
    tokenizer = new UkrainianWordTokenizer();
  }

  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Ukrainian());
  }
  
  public void testTagger() throws IOException {
    TestTools.myAssert("300 р. до н. е.", 
      "300/[300]number -- р/[р]unknown:abbr -- до/[до]noun:n:nv|до/[до]pryim:rv_rod -- н/[null]null -- е/[null]null",
       tokenizer, tagger);
    
    // one-way case sensitivity
    TestTools.myAssert("києві", "києві/[кий]noun:m:v_dav", tokenizer, tagger);
    TestTools.myAssert("Києві", "Києві/[Київ]noun:m:v_mis|Києві/[кий]noun:m:v_dav", tokenizer, tagger);
    TestTools.myAssert("віл", "віл/[віл]noun:m:v_naz:ist", tokenizer, tagger);
    TestTools.myAssert("Віл", "Віл/[віл]noun:m:v_naz:ist", tokenizer, tagger);
    TestTools.myAssert("ВІЛ", "ВІЛ/[ВІЛ]noun:nv:abbr|ВІЛ/[віл]noun:m:v_naz:ist", tokenizer, tagger);
    TestTools.myAssert("далі", "далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Далі", "Далі/[Даль]noun:m:v_mis:ist|Далі/[Далі]noun:m:nv|Далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Бен", "Бен/[Бен]noun:m:v_naz:ist|Бен/[бен]unknown", tokenizer, tagger);
    TestTools.myAssert("бен", "бен/[бен]unknown", tokenizer, tagger);

    TestTools.myAssert("101,234", 
        "101,234/[101,234]number",
         tokenizer, tagger);

    TestTools.myAssert("Справу порушено судом", 
      "Справу/[справа]noun:f:v_zna -- порушено/[порушено]impers -- судом/[суд]noun:m:v_oru|судом/[судома]noun:p:v_rod",
       tokenizer, tagger);
       
    String expected = 
      "Майже/[майже]adv -- два/[два]numr:m:v_naz|два/[два]numr:m:v_zna|два/[два]numr:n:v_naz|два/[два]numr:n:v_zna -- роки/[рік]noun:p:v_naz|роки/[рік]noun:p:v_zna"
    + " -- тому/[той]pron:m:v_dav|тому/[той]pron:m:v_mis|тому/[той]pron:n:v_dav|тому/[той]pron:n:v_mis|тому/[том]noun:m:v_dav|тому/[том]noun:m:v_mis|тому/[том]noun:m:v_rod"
    + " -- Люба/[Люба]noun:f:v_naz|Люба/[любий]adj:f:v_naz -- разом/[раз]noun:m:v_oru -- із/[із]pryim:rv_rod:rv_zna:rv_oru"
    + " -- чоловіком/[чоловік]noun:m:v_oru:ist -- Степаном/[Степан]noun:m:v_oru:ist -- виїхали/[виїхати]verb:past:m:perf -- туди/[туди]adv"
    + " -- на/[на]excl|на/[на]part|на/[на]pryim:rv_zna:rv_mis -- "
    + "проживання/[проживання]noun:n:v_naz|проживання/[проживання]noun:n:v_rod|проживання/[проживання]noun:n:v_zna|проживання/[проживання]noun:p:v_naz|проживання/[проживання]noun:p:v_zna";
  
    TestTools.myAssert("Майже два роки тому Люба разом із чоловіком Степаном виїхали туди на проживання.",
        expected, tokenizer, tagger);
  }

}
