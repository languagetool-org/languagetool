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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;

public class UkrainianTaggerTest {
    
  private UkrainianTagger tagger;
  private UkrainianWordTokenizer tokenizer;
      
  @Before
  public void setUp() {
    tagger = new UkrainianTagger();
    tokenizer = new UkrainianWordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Ukrainian());
  }

  @Test
  public void testTagger() throws IOException {

    // one-way case sensitivity
    TestTools.myAssert("києві", "києві/[кий]noun:inanim:m:v_dav|києві/[кий]noun:inanim:m:v_mis", tokenizer, tagger);
    TestTools.myAssert("Києві", "Києві/[Кий]noun:anim:m:v_dav:prop:fname|Києві/[Кий]noun:anim:m:v_mis:prop:fname|Києві/[Київ]noun:inanim:m:v_mis:prop|Києві/[кий]noun:inanim:m:v_dav|Києві/[кий]noun:inanim:m:v_mis", tokenizer, tagger);
    TestTools.myAssert("віл", "віл/[віл]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("Віл", "Віл/[віл]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("ВІЛ", "ВІЛ/[ВІЛ]noun:inanim:m:v_dav:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_kly:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_mis:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_naz:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_oru:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_rod:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_zna:nv:np:abbr|ВІЛ/[віл]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("далі", "далі/[даль]noun:inanim:f:v_dav|далі/[даль]noun:inanim:f:v_mis|далі/[даль]noun:inanim:f:v_rod|далі/[даль]noun:inanim:p:v_kly|далі/[даль]noun:inanim:p:v_naz|далі/[даль]noun:inanim:p:v_zna|далі/[далі]adv:compc:&predic", tokenizer, tagger);
    TestTools.myAssert("Далі", "Далі/[Даль]noun:anim:m:v_mis:prop:lname|Далі/[Далі]noun:anim:m:v_dav:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_kly:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_mis:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_naz:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_oru:nv:np:prop:lname"
        +"|Далі/[Далі]noun:anim:m:v_rod:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_zna:nv:np:prop:lname|Далі/[даль]noun:inanim:f:v_dav|Далі/[даль]noun:inanim:f:v_mis|Далі/[даль]noun:inanim:f:v_rod|Далі/[даль]noun:inanim:p:v_kly|Далі/[даль]noun:inanim:p:v_naz|Далі/[даль]noun:inanim:p:v_zna|Далі/[далі]adv:compc:&predic", tokenizer, tagger);
    TestTools.myAssert("Бен", "Бен/[Бен]noun:anim:m:v_naz:prop:fname|Бен/[бен]part:pers", tokenizer, tagger);
    TestTools.myAssert("бен", "бен/[бен]part:pers", tokenizer, tagger);


    TestTools.myAssert("Справу порушено судом", 
      "Справу/[справа]noun:inanim:f:v_zna -- порушено/[порушити]verb:perf:impers -- судом/[суд]noun:inanim:m:v_oru|судом/[судома]noun:inanim:p:v_rod",
       tokenizer, tagger);
       
    String expected = 
      "Майже/[майже]adv -- два/[два]numr:p:v_naz|два/[два]numr:p:v_zna -- роки/[рік]noun:inanim:p:v_kly|роки/[рік]noun:inanim:p:v_naz|роки/[рік]noun:inanim:p:v_zna"
    + " -- тому/[те]noun:inanim:n:v_dav:&pron:dem|тому/[те]noun:inanim:n:v_mis:&pron:dem|тому/[той]adj:m:v_dav:&pron:dem|тому/[той]adj:m:v_mis:&pron:dem|тому/[той]adj:n:v_dav:&pron:dem|тому/[той]adj:n:v_mis:&pron:dem|тому/[том]noun:inanim:m:v_dav|тому/[том]noun:inanim:m:v_mis|тому/[том]noun:inanim:m:v_rod|тому/[тому]adv|тому/[тому]conj:subord"
    + " -- Люба/[Люба]noun:anim:f:v_naz:prop:fname|Люба/[любий]adj:f:v_kly:compb|Люба/[любий]adj:f:v_naz:compb -- разом/[раз]noun:inanim:m:v_oru|разом/[разом]adv -- із/[із]prep"
    + " -- чоловіком/[чоловік]noun:anim:m:v_oru:xp1|чоловіком/[чоловік]noun:anim:m:v_oru:xp2 -- Степаном/[Степан]noun:anim:m:v_oru:prop:fname -- виїхали/[виїхати]verb:perf:past:p -- туди/[туди]adv:&pron:dem"
    + " -- на/[на]intj|на/[на]part|на/[на]prep -- "
    + "проживання/[проживання]noun:inanim:n:v_kly|проживання/[проживання]noun:inanim:n:v_naz|проживання/[проживання]noun:inanim:n:v_rod|проживання/[проживання]noun:inanim:n:v_zna"
    + "|проживання/[проживання]noun:inanim:p:v_kly|проживання/[проживання]noun:inanim:p:v_naz|проживання/[проживання]noun:inanim:p:v_zna";
  
    TestTools.myAssert("Майже два роки тому Люба разом із чоловіком Степаном виїхали туди на проживання.",
        expected, tokenizer, tagger);
        
    assertNotTagged("раза");
  }

  @Test
  public void testNumberTagging() throws IOException {
    TestTools.myAssert("101,234", "101,234/[101,234]number", tokenizer, tagger);
    TestTools.myAssert("101 234", "101 234/[101 234]number", tokenizer, tagger);
    TestTools.myAssert("3,5-5,6% 7° 7,4°С", "3,5-5,6%/[3,5-5,6%]number -- 7°/[7°]number -- 7,4°С/[7,4°С]number", tokenizer, tagger);
    TestTools.myAssert("XIX", "XIX/[XIX]number", tokenizer, tagger);
    TestTools.myAssert("10–15", "10–15/[10–15]number", tokenizer, tagger);

    TestTools.myAssert("14.07.2001", "14.07.2001/[14.07.2001]date", tokenizer, tagger);

    TestTools.myAssert("о 15.33", "о/[о]intj|о/[о]prep -- 15.33/[15.33]time", tokenizer, tagger);
    TestTools.myAssert("О 1:05", "О/[о]intj|О/[о]prep -- 1:05/[1:05]time", tokenizer, tagger);
  }


  @Test
  public void testTaggingWithDots() throws IOException {
    TestTools.myAssert("300 р. до н. е.", 
      "300/[300]number -- р./[р.]noun:inanim:f:v_dav:nv:np:abbr|р./[р.]noun:inanim:f:v_mis:nv:np:abbr|р./[р.]noun:inanim:f:v_naz:nv:np:abbr|р./[р.]noun:inanim:f:v_oru:nv:np:abbr"
        +"|р./[р.]noun:inanim:f:v_rod:nv:np:abbr|р./[р.]noun:inanim:f:v_zna:nv:np:abbr|р./[р.]noun:inanim:m:v_dav:nv:np:abbr|р./[р.]noun:inanim:m:v_mis:nv:np:abbr"
        +"|р./[р.]noun:inanim:m:v_naz:nv:np:abbr|р./[р.]noun:inanim:m:v_oru:nv:np:abbr|р./[р.]noun:inanim:m:v_rod:nv:np:abbr|р./[р.]noun:inanim:m:v_zna:nv:np:abbr"
        +" -- до/[до]noun:inanim:n:v_dav:nv|до/[до]noun:inanim:n:v_kly:nv|до/[до]noun:inanim:n:v_mis:nv|до/[до]noun:inanim:n:v_naz:nv|до/[до]noun:inanim:n:v_oru:nv"
        +"|до/[до]noun:inanim:n:v_rod:nv|до/[до]noun:inanim:n:v_zna:nv|до/[до]noun:inanim:p:v_dav:nv|до/[до]noun:inanim:p:v_kly:nv|до/[до]noun:inanim:p:v_mis:nv"
        +"|до/[до]noun:inanim:p:v_naz:nv|до/[до]noun:inanim:p:v_oru:nv|до/[до]noun:inanim:p:v_rod:nv|до/[до]noun:inanim:p:v_zna:nv|до/[до]prep"
        +" -- н./[н.]adj:f:v_dav:nv:abbr|н./[н.]adj:f:v_mis:nv:abbr|н./[н.]adj:f:v_naz:nv:abbr|н./[н.]adj:f:v_oru:nv:abbr|н./[н.]adj:f:v_rod:nv:abbr|н./[н.]adj:f:v_zna:nv:abbr"
        +"|н./[н.]adj:m:v_dav:nv:abbr|н./[н.]adj:m:v_mis:nv:abbr|н./[н.]adj:m:v_naz:nv:abbr|н./[н.]adj:m:v_oru:nv:abbr|н./[н.]adj:m:v_rod:nv:abbr|н./[н.]adj:m:v_zna:nv:abbr"
        +"|н./[н.]adj:n:v_dav:nv:abbr|н./[н.]adj:n:v_mis:nv:abbr|н./[н.]adj:n:v_naz:nv:abbr|н./[н.]adj:n:v_oru:nv:abbr|н./[н.]adj:n:v_rod:nv:abbr|н./[н.]adj:n:v_zna:nv:abbr"
        +"|н./[н.]adj:p:v_dav:nv:abbr|н./[н.]adj:p:v_mis:nv:abbr|н./[н.]adj:p:v_naz:nv:abbr|н./[н.]adj:p:v_oru:nv:abbr|н./[н.]adj:p:v_rod:nv:abbr|н./[н.]adj:p:v_zna:nv:abbr"
        +" -- е./[е.]noun:inanim:f:v_dav:nv:abbr|е./[е.]noun:inanim:f:v_mis:nv:abbr|е./[е.]noun:inanim:f:v_naz:nv:abbr|е./[е.]noun:inanim:f:v_oru:nv:abbr|е./[е.]noun:inanim:f:v_rod:nv:abbr"
        +"|е./[е.]noun:inanim:f:v_zna:nv:abbr|е./[е.]noun:inanim:p:v_dav:nv:abbr|е./[е.]noun:inanim:p:v_mis:nv:abbr|е./[е.]noun:inanim:p:v_naz:nv:abbr|е./[е.]noun:inanim:p:v_oru:nv:abbr"
        +"|е./[е.]noun:inanim:p:v_rod:nv:abbr|е./[е.]noun:inanim:p:v_zna:nv:abbr",
       tokenizer, tagger);

    TestTools.myAssert("300 тис. гривень", 
        "300/[300]number -- тис./[тис.]noun:inanim:f:v_dav:nv:&&numr:abbr|тис./[тис.]noun:inanim:f:v_mis:nv:&&numr:abbr|тис./[тис.]noun:inanim:f:v_naz:nv:&&numr:abbr|тис./[тис.]noun:inanim:f:v_oru:nv:&&numr:abbr|тис./[тис.]noun:inanim:f:v_rod:nv:&&numr:abbr|тис./[тис.]noun:inanim:f:v_zna:nv:&&numr:abbr|тис./[тис.]noun:inanim:p:v_dav:nv:&&numr:abbr|тис./[тис.]noun:inanim:p:v_mis:nv:&&numr:abbr|тис./[тис.]noun:inanim:p:v_naz:nv:&&numr:abbr|тис./[тис.]noun:inanim:p:v_oru:nv:&&numr:abbr|тис./[тис.]noun:inanim:p:v_rod:nv:&&numr:abbr|тис./[тис.]noun:inanim:p:v_zna:nv:&&numr:abbr -- гривень/[гривня]noun:inanim:p:v_rod",
         tokenizer, tagger);

    TestTools.myAssert("Валерій (міліціонер-пародист. – Авт.) стане пародистом.",
        "Валерій/[Валерій]noun:anim:m:v_naz:prop:fname -- міліціонер-пародист/[міліціонер-пародист]noun:anim:m:v_naz -- Авт./[авт.]noun:inanim:m:v_naz:abbr"
 //       "Валерій/[Валерій]noun:anim:m:v_naz:prop:fname -- міліціонер-пародист/[міліціонер-пародист]noun:anim:m:v_naz -- Авт./[null]null"
      + " -- стане/[стан]noun:inanim:m:v_kly:xp1|стане/[стан]noun:inanim:m:v_kly:xp2|стане/[станути]verb:perf:futr:s:3|стане/[стати]verb:perf:futr:s:3 -- пародистом/[пародист]noun:anim:m:v_oru",
         tokenizer, tagger);

//    TestTools.myAssert("Сьогодні (у четвер. - Ред.), вранці.",
//        "Сьогодні/[сьогодні]adv -- у/[у]prep -- четвер/[четвер]noun:inanim:m:v_naz|четвер/[четвер]noun:inanim:m:v_zna -- "
//        +"Ред./[ред.]noun:inanim:f:v_dav:nv:abbr|Ред./[ред.]noun:inanim:f:v_mis:nv:abbr|Ред./[ред.]noun:inanim:f:v_naz:nv:abbr|Ред./[ред.]noun:inanim:f:v_oru:nv:abbr|Ред./[ред.]noun:inanim:f:v_rod:nv:abbr|Ред./[ред.]noun:inanim:f:v_zna:nv:abbr|Ред./[ред.]noun:inanim:m:v_naz:abbr|Ред./[ред.]noun:inanim:p:v_dav:nv:abbr|Ред./[ред.]noun:inanim:p:v_mis:nv:abbr|Ред./[ред.]noun:inanim:p:v_naz:nv:abbr|Ред./[ред.]noun:inanim:p:v_oru:nv:abbr|Ред./[ред.]noun:inanim:p:v_rod:nv:abbr|Ред./[ред.]noun:inanim:p:v_zna:nv:abbr -- вранці/[вранці]adv",
//         tokenizer, tagger);

    TestTools.myAssert("Є. Бакуліна.",
      "Є./[null]null -- Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
       tokenizer, tagger);
  }

  @Test
  public void testProperNameAllCaps() throws IOException {
    TestTools.myAssert("УКРАЇНА", "УКРАЇНА/[Україна]noun:inanim:f:v_naz:prop", tokenizer, tagger);
  }

  @Test
  public void testDynamicTaggingNums() throws IOException {
    TestTools.myAssert("100-річному", "100-річному/[100-річний]adj:m:v_dav|100-річному/[100-річний]adj:m:v_mis|100-річному/[100-річний]adj:n:v_dav|100-річному/[100-річний]adj:n:v_mis", tokenizer, tagger);
    TestTools.myAssert("1-2-відсотковим", "1-2-відсотковим/[1-2-відсотковий]adj:m:v_oru|1-2-відсотковим/[1-2-відсотковий]adj:n:v_oru|1-2-відсотковим/[1-2-відсотковий]adj:p:v_dav", tokenizer, tagger);
    TestTools.myAssert("10-класників", "10-класників/[10-класник]noun:anim:p:v_rod|10-класників/[10-класник]noun:anim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("10-хвилинка", "10-хвилинка/[10-хвилинка]noun:inanim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("11-12-річний", "11-12-річний/[11-12-річний]adj:m:v_kly|11-12-річний/[11-12-річний]adj:m:v_naz|11-12-річний/[11-12-річний]adj:m:v_zna:rinanim", tokenizer, tagger);
    
    TestTools.myAssert("100-й", "100-й/[100-й]adj:f:v_dav:&numr|100-й/[100-й]adj:f:v_mis:&numr|100-й/[100-й]adj:m:v_naz:&numr|100-й/[100-й]adj:m:v_zna:rinanim:&numr", tokenizer, tagger);
    TestTools.myAssert("50-х", "50-х/[50-й]adj:p:v_mis:&numr|50-х/[50-й]adj:p:v_rod:&numr|50-х/[50-й]adj:p:v_zna:ranim:&numr", tokenizer, tagger);
    TestTools.myAssert("11-ту", "11-ту/[11-й]adj:f:v_zna:&numr", tokenizer, tagger);
    TestTools.myAssert("54–річна", "54–річна/[54-річний]adj:f:v_kly|54–річна/[54-річний]adj:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("3-ій", "3-ій/[3-й]adj:f:v_dav:&numr|3-ій/[3-й]adj:f:v_mis:&numr|3-ій/[3-й]adj:m:v_naz:&numr|3-ій/[3-й]adj:m:v_zna:rinanim:&numr", tokenizer, tagger);
    TestTools.myAssert("5-ій", "5-ій/[5-й]adj:f:v_dav:&numr|5-ій/[5-й]adj:f:v_mis:&numr", tokenizer, tagger);

    TestTools.myAssert("15-ти", "15-ти/[15]numr:p:v_dav:bad|15-ти/[15]numr:p:v_mis:bad|15-ти/[15]numr:p:v_rod:bad", tokenizer, tagger);

    TestTools.myAssert("100-річчя", "100-річчя/[100-річчя]noun:inanim:n:v_kly|100-річчя/[100-річчя]noun:inanim:n:v_naz|100-річчя/[100-річчя]noun:inanim:n:v_rod|100-річчя/[100-річчя]noun:inanim:n:v_zna|100-річчя/[100-річчя]noun:inanim:p:v_kly|100-річчя/[100-річчя]noun:inanim:p:v_naz|100-річчя/[100-річчя]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("100-метрівка", "100-метрівка/[100-метрівка]noun:inanim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("100-відсотково", "100-відсотково/[100-відсотково]adv", tokenizer, tagger);
    TestTools.myAssert("120-мм", "120-мм/[120-мм]adj:f:v_dav|120-мм/[120-мм]adj:f:v_mis|120-мм/[120-мм]adj:f:v_naz|120-мм/[120-мм]adj:f:v_oru|120-мм/[120-мм]adj:f:v_rod|120-мм/[120-мм]adj:f:v_zna"
        + "|120-мм/[120-мм]adj:m:v_dav|120-мм/[120-мм]adj:m:v_mis|120-мм/[120-мм]adj:m:v_naz|120-мм/[120-мм]adj:m:v_oru|120-мм/[120-мм]adj:m:v_rod|120-мм/[120-мм]adj:m:v_zna"
        + "|120-мм/[120-мм]adj:n:v_dav|120-мм/[120-мм]adj:n:v_mis|120-мм/[120-мм]adj:n:v_naz|120-мм/[120-мм]adj:n:v_oru|120-мм/[120-мм]adj:n:v_rod|120-мм/[120-мм]adj:n:v_zna"
        + "|120-мм/[120-мм]adj:p:v_dav|120-мм/[120-мм]adj:p:v_mis|120-мм/[120-мм]adj:p:v_naz|120-мм/[120-мм]adj:p:v_oru|120-мм/[120-мм]adj:p:v_rod|120-мм/[120-мм]adj:p:v_zna", tokenizer, tagger);

    TestTools.myAssert("Євро-2014", "Євро-2014/[Євро-2014]noun:inanim:m:v_dav:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_mis:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_naz:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_oru:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_rod:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_zna:nv:prop", tokenizer, tagger);
    TestTools.myAssert("Ігри-2014", "Ігри-2014/[Гра-2014]noun:inanim:p:v_naz:prop|Ігри-2014/[Гра-2014]noun:inanim:p:v_zna:prop", tokenizer, tagger);
    TestTools.myAssert("ЧЄ-2014", "ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_dav:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_mis:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_naz:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_oru:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_rod:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_zna:nv:np:prop:abbr", tokenizer, tagger);
    TestTools.myAssert("Лондон-2014", "Лондон-2014/[Лондон-2014]noun:inanim:m:v_naz:prop:xp2|Лондон-2014/[Лондон-2014]noun:inanim:m:v_zna:prop:xp2", tokenizer, tagger);
    TestTools.myAssert("Афіни-2014", "Афіни-2014/[Афіни-2014]noun:inanim:p:v_naz:prop:ns|Афіни-2014/[Афіни-2014]noun:inanim:p:v_zna:prop:ns", tokenizer, tagger);
    TestTools.myAssert("Замок-2002", "Замок-2002/[Замок-2002]noun:inanim:m:v_naz:prop|Замок-2002/[Замок-2002]noun:inanim:m:v_zna:prop", tokenizer, tagger);

    TestTools.myAssert("Ан-140", "Ан-140/[Ан-140]noun:m:v_dav:prop|Ан-140/[Ан-140]noun:m:v_mis:prop|Ан-140/[Ан-140]noun:m:v_naz:prop|Ан-140/[Ан-140]noun:m:v_oru:prop|Ан-140/[Ан-140]noun:m:v_rod:prop|Ан-140/[Ан-140]noun:m:v_zna:prop", tokenizer, tagger);
    TestTools.myAssert("ВАЗ-2104", "ВАЗ-2104/[ВАЗ-2104]noun:f:v_dav:prop|ВАЗ-2104/[ВАЗ-2104]noun:f:v_mis:prop|ВАЗ-2104/[ВАЗ-2104]noun:f:v_naz:prop|ВАЗ-2104/[ВАЗ-2104]noun:f:v_oru:prop|ВАЗ-2104/[ВАЗ-2104]noun:f:v_rod:prop|ВАЗ-2104/[ВАЗ-2104]noun:f:v_zna:prop|ВАЗ-2104/[ВАЗ-2104]noun:m:v_dav:prop|ВАЗ-2104/[ВАЗ-2104]noun:m:v_mis:prop|ВАЗ-2104/[ВАЗ-2104]noun:m:v_naz:prop|ВАЗ-2104/[ВАЗ-2104]noun:m:v_oru:prop|ВАЗ-2104/[ВАЗ-2104]noun:m:v_rod:prop|ВАЗ-2104/[ВАЗ-2104]noun:m:v_zna:prop", tokenizer, tagger);
//    TestTools.myAssert("Площі–2006", "Площі–2006/[Площа-2006]noun:inanim:p:v_dav:prop:ns|Площі–2006/[Площа-2006]noun:inanim:p:v_mis:prop:ns|Площі–2006/[Площа-2006]noun:inanim:p:v_rod:prop:ns", tokenizer, tagger);
  }

  @Test
  public void testDynamicTaggingParts() throws IOException {
    TestTools.myAssert("по-свинячому", "по-свинячому/[по-свинячому]adv", tokenizer, tagger);
    TestTools.myAssert("по-сибірськи", "по-сибірськи/[по-сибірськи]adv", tokenizer, tagger);

    TestTools.myAssert("давай-но", "давай-но/[давати]verb:imperf:impr:s:2", tokenizer, tagger);
    TestTools.myAssert("дивіться-но", "дивіться-но/[дивитися]verb:rev:imperf:impr:p:2", tokenizer, tagger);
    TestTools.myAssert("той-таки", "той-таки/[той-таки]adj:m:v_naz:&pron:dem|той-таки/[той-таки]adj:m:v_zna:rinanim:&pron:dem", tokenizer, tagger);
    TestTools.myAssert("буде-таки", "буде-таки/[бути]verb:imperf:futr:s:3", tokenizer, tagger);
    TestTools.myAssert("там-таки", "там-таки/[там]adv:&pron:dem|там-таки/[там]part", tokenizer, tagger);
    TestTools.myAssert("зразу-таки", "зразу-таки/[зразу]adv", tokenizer, tagger);
    TestTools.myAssert("оцей-от", "оцей-от/[оцей]adj:m:v_naz:&pron:dem|оцей-от/[оцей]adj:m:v_zna:rinanim:&pron:dem", tokenizer, tagger);
    TestTools.myAssert("ану-бо", "ану-бо/[ану]intj|ану-бо/[ану]part", tokenizer, tagger);
    TestTools.myAssert("годі-бо", "годі-бо/[годі]adv:&predic", tokenizer, tagger);
    TestTools.myAssert("гей-но", "гей-но/[гей]intj", tokenizer, tagger);
    TestTools.myAssert("цить-но", "цить-но/[цить]intj", tokenizer, tagger);

    TestTools.myAssert("оттакий-то", "оттакий-то/[оттакий]adj:m:v_naz:&pron:dem:rare|оттакий-то/[оттакий]adj:m:v_zna:rinanim:&pron:dem:rare", tokenizer, tagger);
//  TestTools.myAssert("геть-то", "геть-то/[геть]adv", tokenizer, tagger);
    TestTools.myAssert("дуже-то", "дуже-то/[дуже]adv:compb", tokenizer, tagger);
    TestTools.myAssert("десь-то", "десь-то/[десь-то]adv", tokenizer, tagger);   //TODO: :&pron:ind
    TestTools.myAssert("котрий-то", "котрий-то/[котрий]adj:m:v_naz:&pron:int:rel|котрий-то/[котрий]adj:m:v_zna:rinanim:&pron:int:rel", tokenizer, tagger);   //TODO: :&pron:ind
    TestTools.myAssert("ніби-то", "ніби-то/[ніби]conj:subord", tokenizer, tagger);
    
    assertNotTagged("хто-то");
  }

  @Test
  public void testDynamicTaggingPrefixes() throws IOException {
    TestTools.myAssert("VIP–будинок", "VIP–будинок/[VIP-будинок]noun:inanim:m:v_naz|VIP–будинок/[VIP-будинок]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("PR-департаменту", "PR-департаменту/[PR-департамент]noun:inanim:m:v_dav|PR-департаменту/[PR-департамент]noun:inanim:m:v_mis|PR-департаменту/[PR-департамент]noun:inanim:m:v_rod", tokenizer, tagger);
    TestTools.myAssert("3D-друк", "3D-друк/[3D-друк]noun:inanim:m:v_naz|3D-друк/[3D-друк]noun:inanim:m:v_zna", tokenizer, tagger);
//    TestTools.myAssert("POS-термінальна", "", tokenizer, tagger);
//    TestTools.myAssert("IT-Академії", "IT-Академії/[IT-академія]noun:inanim:f:v_dav|IT-Академії/[IT-академія]noun:inanim:f:v_mis|IT-Академії/[IT-академія]noun:inanim:f:v_rod|IT-Академії/[IT-академія]noun:inanim:p:v_naz|IT-Академії/[IT-академія]noun:inanim:p:v_zna", tokenizer, tagger);
  }
  
  @Test
  public void testDynamicTaggingFullTagMatch() throws IOException {
    TestTools.myAssert("пів-України", "пів-України/[пів-України]noun:inanim:f:v_dav:prop|пів-України/[пів-України]noun:inanim:f:v_mis:prop|пів-України/[пів-України]noun:inanim:f:v_naz:prop"
        +"|пів-України/[пів-України]noun:inanim:f:v_oru:prop|пів-України/[пів-України]noun:inanim:f:v_rod:prop|пів-України/[пів-України]noun:inanim:f:v_zna:prop", tokenizer, tagger);

    TestTools.myAssert("Пенсильванія-авеню", "Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_dav:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_mis:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_naz:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_oru:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_rod:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_zna:nv:prop", tokenizer, tagger);
    TestTools.myAssert("Уолл-стрит", "Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_dav:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_mis:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_naz:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_oru:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_rod:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_zna:nv:prop", tokenizer, tagger);
    

    // full tag match
    

    TestTools.myAssert("жило-було", "жило-було/[жити-бути]verb:imperf:past:n", tokenizer, tagger);
    TestTools.myAssert("учиш-учиш", "учиш-учиш/[учити-учити]verb:imperf:pres:s:2", tokenizer, tagger);

    TestTools.myAssert("а-а", "а-а/[а-а]intj", tokenizer, tagger);
    TestTools.myAssert("Га-га", "Га-га/[га-га]intj", tokenizer, tagger);
    TestTools.myAssert("ось\u2013ось", "ось\u2013ось/[ось-ось]adv", tokenizer, tagger);
    TestTools.myAssert("ось\u2011ось", "ось-ось/[ось-ось]adv", tokenizer, tagger);
    TestTools.myAssert("Івано\u2013Франківськ", "Івано–Франківськ/[Івано-Франківськ]noun:inanim:m:v_naz:prop|Івано–Франківськ/[Івано-Франківськ]noun:inanim:m:v_zna:prop", tokenizer, tagger);

    TestTools.myAssert("вгору-вниз", "вгору-вниз/[вгору-вниз]adv", tokenizer, tagger);

    TestTools.myAssert("годину-півтори", "годину-півтори/[година-півтори]noun:inanim:f:v_zna|годину-півтори/[година-півтори]noun:inanim:p:v_zna", tokenizer, tagger);

    TestTools.myAssert("вівторок-середа", "вівторок-середа/[вівторок-середа]noun:inanim:m:v_naz|вівторок-середа/[вівторок-середа]noun:inanim:p:v_naz", tokenizer, tagger);
    TestTools.myAssert("лютого-березня", "лютого-березня/[лютий-березень]noun:inanim:m:v_rod|лютого-березня/[лютий-березень]noun:inanim:p:v_rod", tokenizer, tagger);


    // numr-numr
    TestTools.myAssert("одним-двома", "одним-двома/[один-два]numr:p:v_oru", tokenizer, tagger);
    //TODO: бере іменник п’ята
//    TestTools.myAssert("п'яти-шести", "п'яти-шести/[п'ять-шість]numr:v_dav|п'яти-шести/[п'ять-шість]numr:v_mis|п'яти-шести/[п'ять-шість]numr:v_rod", tokenizer, tagger);
    TestTools.myAssert("п'яти-шести", "п'яти-шести/[п'ята-шість]noun:inanim:f:v_rod|п'яти-шести/[п'ята-шість]noun:inanim:p:v_rod|п'яти-шести/[п'ять-шість]numr:p:v_dav|п'яти-шести/[п'ять-шість]numr:p:v_mis|п'яти-шести/[п'ять-шість]numr:p:v_rod", tokenizer, tagger);
//    TestTools.myAssert("півтори-дві", "півтори-дві/[півтори-два]numr:f:v_naz|півтори-дві/[півтори-два]numr:f:v_zna", tokenizer, tagger);
    TestTools.myAssert("три-чотири", "три-чотири/[три-чотири]numr:p:v_naz|три-чотири/[три-чотири]numr:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("два-чотири", "два-чотири/[два-чотири]numr:p:v_naz|два-чотири/[два-чотири]numr:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("одному-двох", "одному-двох/[один-два]numr:p:v_mis", tokenizer, tagger);
    TestTools.myAssert("три–чотири", "три–чотири/[три-чотири]numr:p:v_naz|три–чотири/[три-чотири]numr:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("шість-сім", "шість-сім/[шість-сім]numr:p:v_naz|шість-сім/[шість-сім]numr:p:v_zna", tokenizer, tagger);

    // noun-numr
    TestTools.myAssert("абзац-два", "абзац-два/[абзац-два]noun:inanim:m:v_naz|абзац-два/[абзац-два]noun:inanim:m:v_zna|абзац-два/[абзац-два]noun:inanim:p:v_naz|абзац-два/[абзац-два]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("сотні-дві", "сотні-дві/[сотня-два]noun:inanim:p:v_naz|сотні-дві/[сотня-два]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("тисячею-трьома", "тисячею-трьома/[тисяча-три]noun:inanim:f:v_oru:&&numr|тисячею-трьома/[тисяча-три]noun:inanim:p:v_oru:&&numr|тисячею-трьома/[тисяча-троє]noun:inanim:f:v_oru:&&numr|тисячею-трьома/[тисяча-троє]noun:inanim:p:v_oru:&&numr", tokenizer, tagger);

    TestTools.myAssert("друге-третє", "друге-третє/[другий-третій]adj:n:v_kly:&numr|друге-третє/[другий-третій]adj:n:v_naz:&numr|друге-третє/[другий-третій]adj:n:v_zna:&numr", tokenizer, tagger);
    
    // others

    TestTools.myAssert("низенько-низенько", "низенько-низенько/[низенько-низенько]adv", tokenizer, tagger);
//    TestTools.myAssert("такого-сякого", "такого-сякого/[такий-сякий]adj:m:v_rod:&pron:def|такого-сякого/[такий-сякий]adj:m:v_zna:&pron:def|такого-сякого/[такий-сякий]adj:n:v_rod:&pron:def", tokenizer, tagger);
    TestTools.myAssert("великий-превеликий", "великий-превеликий/[великий-превеликий]adj:m:v_kly|великий-превеликий/[великий-превеликий]adj:m:v_naz|великий-превеликий/[великий-превеликий]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("чорній-чорній", "чорній-чорній/[чорний-чорний]adj:f:v_dav|чорній-чорній/[чорний-чорний]adj:f:v_mis|чорній-чорній/[чорніти-чорніти]verb:imperf:impr:s:2", tokenizer, tagger);


    TestTools.myAssert("етно-диско", "етно-диско/[null]null", tokenizer, tagger);
    TestTools.myAssert("екс-партнер", "екс-партнер/[екс-партнер]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("еспресо-машина", "еспресо-машина/[еспресо-машина]noun:inanim:f:v_naz", tokenizer, tagger);

    TestTools.myAssert("кава-еспресо", "кава-еспресо/[кава-еспресо]noun:inanim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("кави-еспресо", "кави-еспресо/[кава-еспресо]noun:inanim:f:v_rod|кави-еспресо/[кава-еспресо]noun:inanim:p:v_naz|кави-еспресо/[кава-еспресо]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("програмою-максимум", "програмою-максимум/[програма-максимум]noun:inanim:f:v_oru", tokenizer, tagger);

    // jr/sr

    TestTools.myAssert("Алієва-старшого", "Алієва-старшого/[Алієв-старший]noun:anim:m:v_rod:prop:lname|Алієва-старшого/[Алієв-старший]noun:anim:m:v_zna:prop:lname", tokenizer, tagger);
    // test ranim/rinanim
//    TestTools.myAssert("Алієва-старший", "Алієва-старший/[Алієва-старший]", tokenizer, tagger);
//    TestTools.myAssert("Алієв-старшого", "Алієва-старшого/[Алієва-старшого]", tokenizer, tagger);
    
    // noun-noun

    TestTools.myAssert("лікар-гомеопат", "лікар-гомеопат/[лікар-гомеопат]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("лікаря-гомеопата", "лікаря-гомеопата/[лікар-гомеопат]noun:anim:m:v_rod|лікаря-гомеопата/[лікар-гомеопат]noun:anim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("шмкр-гомеопат", "шмкр-гомеопат/[null]null", tokenizer, tagger);
    TestTools.myAssert("лікар-ткр", "лікар-ткр/[null]null", tokenizer, tagger);

    TestTools.myAssert("вчинок-приклад", "вчинок-приклад/[вчинок-приклад]noun:inanim:m:v_naz|вчинок-приклад/[вчинок-приклад]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("міста-фортеці", "міста-фортеці/[місто-фортеця]noun:inanim:n:v_rod|міста-фортеці/[місто-фортеця]noun:inanim:p:v_naz|міста-фортеці/[місто-фортеця]noun:inanim:p:v_zna", tokenizer, tagger);

    // inanim-anim
    TestTools.myAssert("вчених-новаторів", "вчених-новаторів/[вчений-новатор]noun:anim:p:v_rod|вчених-новаторів/[вчений-новатор]noun:anim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("країна-виробник", "країна-виробник/[країна-виробник]noun:inanim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("банк-виробник", "банк-виробник/[банк-виробник]noun:inanim:m:v_naz|банк-виробник/[банк-виробник]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("банки-агенти", "банки-агенти/[банк-агент]noun:inanim:p:v_naz|банки-агенти/[банк-агент]noun:inanim:p:v_zna|банки-агенти/[банка-агент]noun:inanim:p:v_naz|банки-агенти/[банка-агент]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("місто-гігант", "місто-гігант/[місто-гігант]noun:inanim:n:v_naz|місто-гігант/[місто-гігант]noun:inanim:n:v_zna", tokenizer, tagger);
    TestTools.myAssert("країни-агресори", "країни-агресори/[країна-агресор]noun:inanim:p:v_naz|країни-агресори/[країна-агресор]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("поселення-гігант", "поселення-гігант/[поселення-гігант]noun:inanim:n:v_naz|поселення-гігант/[поселення-гігант]noun:inanim:n:v_zna", tokenizer, tagger);
    
    TestTools.myAssert("сонях-красень", "сонях-красень/[сонях-красень]noun:inanim:m:v_naz|сонях-красень/[сонях-красень]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("красень-сонях", "красень-сонях/[красень-сонях]noun:inanim:m:v_naz|красень-сонях/[красень-сонях]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("депутатів-привидів", "депутатів-привидів/[депутат-привид]noun:anim:p:v_rod|депутатів-привидів/[депутат-привид]noun:anim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("дівчата-зірочки", "дівчата-зірочки/[дівчина-зірочка]noun:anim:p:v_naz", tokenizer, tagger);
    
    // TODO: істота-неістота
    // про місяця-місяченька
//    TestTools.myAssert("бабці-Австрії",  "", tokenizer, tagger);
    // змагання зі слалому-гіганту
    // голосувати за Тимошенко-прем’єра


    // TODO: unanim
    TestTools.myAssert("ворог-стафілокок", "ворог-стафілокок/[null]null", tokenizer, tagger);
    TestTools.myAssert("стафілокок-реагент", "стафілокок-реагент/[null]null", tokenizer, tagger);
    
//    TestTools.myAssert("капуджі-ага", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("Каладжі-бей", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("капудан-паша", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("кальфа-ефенді", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);

    TestTools.myAssert("Москви-ріки", "Москви-ріки/[Москва-ріка]noun:inanim:f:v_rod:prop", tokenizer, tagger);


    // adv-adj

    TestTools.myAssert("патолого-анатомічний", "патолого-анатомічний/[патолого-анатомічний]adj:m:v_kly|патолого-анатомічний/[патолого-анатомічний]adj:m:v_naz|патолого-анатомічний/[патолого-анатомічний]adj:m:v_zna:rinanim", tokenizer, tagger);

    TestTools.myAssert("паталого-анатомічний", "паталого-анатомічний/[null]null", tokenizer, tagger);
    TestTools.myAssert("патолога-анатомічний", "патолога-анатомічний/[null]null", tokenizer, tagger);
    TestTools.myAssert("патолого-гмкнх", "патолого-гмкнх/[null]null", tokenizer, tagger);
    TestTools.myAssert("патолого-голова", "патолого-голова/[null]null", tokenizer, tagger);

    TestTools.myAssert("Художньо-культурний", "Художньо-культурний/[художньо-культурний]adj:m:v_kly|Художньо-культурний/[художньо-культурний]adj:m:v_naz|Художньо-культурний/[художньо-культурний]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("сліпуче-яскравого", "сліпуче-яскравого/[сліпуче-яскравий]adj:m:v_rod|сліпуче-яскравого/[сліпуче-яскравий]adj:m:v_zna:ranim|сліпуче-яскравого/[сліпуче-яскравий]adj:n:v_rod", tokenizer, tagger);
    TestTools.myAssert("дво-триметровий", "дво-триметровий/[дво-триметровий]adj:m:v_kly|дво-триметровий/[дво-триметровий]adj:m:v_naz|дво-триметровий/[дво-триметровий]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("україно-болгарський", "україно-болгарський/[україно-болгарський]adj:m:v_kly|україно-болгарський/[україно-болгарський]adj:m:v_naz|україно-болгарський/[україно-болгарський]adj:m:v_zna:rinanim", tokenizer, tagger);

    TestTools.myAssert("бірмюково-блакитний", "бірмюково-блакитний/[null]null", tokenizer, tagger);

//    TestTools.myAssert("американо-блакитний", "американо-блакитний/[null]null", tokenizer, tagger);

    TestTools.myAssert("Дівчинка-першокласниця", "Дівчинка-першокласниця/[дівчинка-першокласниця]noun:anim:f:v_naz", tokenizer, tagger);

    TestTools.myAssert("RPM-пакунок", "RPM-пакунок/[RPM-пакунок]noun:inanim:m:v_naz|RPM-пакунок/[RPM-пакунок]noun:inanim:m:v_zna", tokenizer, tagger);

    TestTools.myAssert("учбово-спортивної", "учбово-спортивної/[учбово-спортивний]adj:f:v_rod:bad", tokenizer, tagger);

    //TODO:
//    TestTools.myAssert("ненависть-шоу", "", tokenizer, tagger);

    assertNotTagged("авто-салон");
    assertNotTagged("квазі-держави");
    assertNotTagged("мульти-візу");
    assertNotTagged("напів-люкс");
    assertNotTagged("контр-міри");
    assertNotTagged("кіно-критика");
    
    assertNotTagged("пів–качана");

    assertNotTagged("Малишко-це");
    assertNotTagged("відносини-коли");
    assertNotTagged("лісо-та");

    assertNotTagged("древньо-римський");
    assertNotTagged("давньо-римський");

    assertNotTagged("Ліс-наш");

    assertNotTagged("Нью-Париж");

    assertNotTagged("кохання-найщиріше"); // - мало би бути тире

    // don't allow dash when the words spelled together
    
    assertNotTagged("дер-жав");
    assertNotTagged("зовнішньо-економічний");
    assertNotTagged("високо-релевантною");
    assertNotTagged("всесвітньо-відомі");

    assertNotTagged("Донець-кий");
    assertNotTagged("мас-штаби");

    assertNotTagged("максимально-можливу"); // має бути "максимально можливу"

    assertNotTagged("відео-навчання");

    assertNotTagged("рибо-полювання");
    
//  assertNotTagged("льотно-посадкова"); - загубилося початкове "з". Але це не спинило тегувальника - чому?


//    TestTools.myAssert("дітей-сиріт", "дітей-сиріт/[діти-сироти]noun:anim:p:v_rod", tokenizer, tagger);
//    TestTools.myAssert("курей-бройлерів", "кури-бройлери", tokenizer, tagger);
  }

  //TODO:
//  @Test
//  public void testTaggingMultidash() throws IOException {
//    TestTools.myAssert("україно-румуно-болгарський", "", tokenizer, tagger);
//  TestTools.myAssert("синьо-біло-чорний", "", tokenizer, tagger);
//  TestTools.myAssert("хіп-хоп-гурту", "", tokenizer, tagger);
//  }

  @Test
  public void testDynamicTaggingNoDash() throws IOException {
    TestTools.myAssert("Лангштрассе", "Лангштрассе/[Лангштрассе]noun:inanim:f:v_dav:nv:prop|Лангштрассе/[Лангштрассе]noun:inanim:f:v_mis:nv:prop"
        + "|Лангштрассе/[Лангштрассе]noun:inanim:f:v_naz:nv:prop|Лангштрассе/[Лангштрассе]noun:inanim:f:v_oru:nv:prop"
        + "|Лангштрассе/[Лангштрассе]noun:inanim:f:v_rod:nv:prop|Лангштрассе/[Лангштрассе]noun:inanim:f:v_zna:nv:prop", tokenizer, tagger);
  }


  @Test
  public void testDynamicTaggingSkip() throws IOException {
    TestTools.myAssert("г-г-г", "г-г-г/[null]null", tokenizer, tagger);
    TestTools.myAssert("йо-га", "йо-га/[null]null", tokenizer, tagger);
    TestTools.myAssert("с-г", "с-г/[null]null", tokenizer, tagger);
    TestTools.myAssert("де-куди", "де-куди/[null]null", tokenizer, tagger);
    TestTools.myAssert("чи-то", "чи-то/[null]null", tokenizer, tagger);
//    TestTools.myAssert("як-то", "як-то/[як-то]conj:subord:bad", tokenizer, tagger);

    assertNotTagged("спа-салоне");

    

    // \n may happen in words when we have soft-hyphen wrap: \u00AD\n
    // in this case we strip \u00AD but leave \n in the word
    // but this may not happen often, need research if we need this
//    TestTools.myAssert("стін\nку", "", tokenizer, tagger);
  }

//  @Test
//  public void testSpecialChars() throws IOException {
//    AnalyzedSentence analyzedSentence = new JLanguageTool(new Ukrainian()).getAnalyzedSentence("і карт\u00ADками.");
//    String token = analyzedSentence.getTokens()[2].getToken();
//    System.err.println(": " +analyzedSentence);
//    System.err.println(": " +token);
//    TestTools.myAssert("і картками.", "", tokenizer, tagger);
//  }
  
  private void assertNotTagged(String word) throws IOException {
  	TestTools.myAssert(word, word+"/[null]null", tokenizer, tagger);
  }

}
