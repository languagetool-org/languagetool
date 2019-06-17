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
    TestTools.myAssert("Києві", "Києві/[Кий]noun:anim:m:v_dav:prop:fname|Києві/[Кий]noun:anim:m:v_mis:prop:fname|Києві/[Київ]noun:inanim:m:v_mis:prop:geo|Києві/[кий]noun:inanim:m:v_dav|Києві/[кий]noun:inanim:m:v_mis", tokenizer, tagger);
    TestTools.myAssert("віл", "віл/[віл]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("Віл", "Віл/[віл]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("ВІЛ", "ВІЛ/[ВІЛ]noun:inanim:m:v_dav:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_kly:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_mis:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_naz:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_oru:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_rod:nv:np:abbr|ВІЛ/[ВІЛ]noun:inanim:m:v_zna:nv:np:abbr|ВІЛ/[віл]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("далі", "далі/[даль]noun:inanim:f:v_dav|далі/[даль]noun:inanim:f:v_mis|далі/[даль]noun:inanim:f:v_rod|далі/[даль]noun:inanim:p:v_kly|далі/[даль]noun:inanim:p:v_naz|далі/[даль]noun:inanim:p:v_zna|далі/[далі]adv:compc:&predic", tokenizer, tagger);
    TestTools.myAssert("Далі", "Далі/[Даль]noun:anim:m:v_mis:prop:lname|Далі/[Даля]noun:anim:f:v_dav:prop:fname|Далі/[Даля]noun:anim:f:v_mis:prop:fname|Далі/[Даля]noun:anim:f:v_rod:prop:fname|Далі/[Даля]noun:anim:p:v_kly:prop:fname|Далі/[Даля]noun:anim:p:v_naz:prop:fname"
        + "|Далі/[Далі]noun:anim:m:v_dav:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_kly:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_mis:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_naz:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_oru:nv:np:prop:lname"
        + "|Далі/[Далі]noun:anim:m:v_rod:nv:np:prop:lname|Далі/[Далі]noun:anim:m:v_zna:nv:np:prop:lname|Далі/[даль]noun:inanim:f:v_dav|Далі/[даль]noun:inanim:f:v_mis|Далі/[даль]noun:inanim:f:v_rod|Далі/[даль]noun:inanim:p:v_kly|Далі/[даль]noun:inanim:p:v_naz|Далі/[даль]noun:inanim:p:v_zna|Далі/[далі]adv:compc:&predic", tokenizer, tagger);
    TestTools.myAssert("Бен", "Бен/[Бен]noun:anim:m:v_naz:prop:fname|Бен/[бен]part:pers", tokenizer, tagger);
    TestTools.myAssert("бен", "бен/[бен]part:pers", tokenizer, tagger);


    TestTools.myAssert("Справу порушено судом", 
      "Справу/[справа]noun:inanim:f:v_zna -- порушено/[порушити]verb:perf:impers -- судом/[суд]noun:inanim:m:v_oru|судом/[судома]noun:inanim:p:v_rod",
       tokenizer, tagger);
       
    String expected = 
      "Майже/[майже]adv -- два/[два]numr:p:v_naz|два/[два]numr:p:v_zna -- роки/[рік]noun:inanim:p:v_kly|роки/[рік]noun:inanim:p:v_naz|роки/[рік]noun:inanim:p:v_zna"
    + " -- тому/[те]noun:inanim:n:v_dav:&pron:dem|тому/[те]noun:inanim:n:v_mis:&pron:dem|тому/[той]adj:m:v_dav:&pron:dem|тому/[той]adj:m:v_mis:&pron:dem|тому/[той]adj:n:v_dav:&pron:dem|тому/[той]adj:n:v_mis:&pron:dem|тому/[том]noun:inanim:m:v_dav|тому/[том]noun:inanim:m:v_mis|тому/[том]noun:inanim:m:v_rod|тому/[тому]adv|тому/[тому]conj:subord"
    + " -- Люба/[Люба]noun:anim:f:v_naz:prop:fname|Люба/[любий]adj:f:v_kly:compb|Люба/[любий]adj:f:v_naz:compb -- разом/[раз]noun:inanim:m:v_oru|разом/[разом]adv -- із/[із]prep"
    + " -- чоловіком/[чоловік]noun:anim:m:v_oru -- Степаном/[Степан]noun:anim:m:v_oru:prop:fname -- виїхали/[виїхати]verb:perf:past:p -- туди/[туди]adv:&pron:dem"
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

    TestTools.myAssert("XIX", "XIX/[XIX]number:latin", tokenizer, tagger);
    // latin number with cyrillic
    TestTools.myAssert("ХІХ", "ХІХ/[ХІХ]number:latin:bad", tokenizer, tagger);
    TestTools.myAssert("ІV", "ІV/[ІV]number:latin:bad", tokenizer, tagger);

    TestTools.myAssert("10–15", "10–15/[10–15]number", tokenizer, tagger);

    TestTools.myAssert("14.07.2001", "14.07.2001/[14.07.2001]date", tokenizer, tagger);

    TestTools.myAssert("о 15.33", "о/[о]intj|о/[о]prep -- 15.33/[15.33]time", tokenizer, tagger);
    TestTools.myAssert("О 1:05", "О/[о]intj|О/[о]prep -- 1:05/[1:05]time", tokenizer, tagger);
  }

  @Test
  public void testSpecialSymbols() throws IOException {
    TestTools.myAssert("км²", "км²/[км]noun:inanim:m:v_dav:nv:abbr|км²/[км]noun:inanim:m:v_kly:nv:abbr|км²/[км]noun:inanim:m:v_mis:nv:abbr|км²/[км]noun:inanim:m:v_naz:nv:abbr|км²/[км]noun:inanim:m:v_oru:nv:abbr"
        + "|км²/[км]noun:inanim:m:v_rod:nv:abbr|км²/[км]noun:inanim:m:v_zna:nv:abbr|км²/[км]noun:inanim:p:v_dav:nv:abbr|км²/[км]noun:inanim:p:v_kly:nv:abbr"
        + "|км²/[км]noun:inanim:p:v_mis:nv:abbr|км²/[км]noun:inanim:p:v_naz:nv:abbr|км²/[км]noun:inanim:p:v_oru:nv:abbr|км²/[км]noun:inanim:p:v_rod:nv:abbr|км²/[км]noun:inanim:p:v_zna:nv:abbr", tokenizer, tagger);
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
    TestTools.myAssert("УКРАЇНА", "УКРАЇНА/[Україна]noun:inanim:f:v_naz:prop:geo", tokenizer, tagger);
    TestTools.myAssert("СИРІЮ", "СИРІЮ/[Сирія]noun:inanim:f:v_zna:prop:geo|СИРІЮ/[сиріти]verb:imperf:pres:s:1", tokenizer, tagger);
    assertNotTagged("УКРАЇ");
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
    TestTools.myAssert("3-ій", "3-ій/[3-й]adj:f:v_dav:&numr|3-ій/[3-й]adj:f:v_mis:&numr|3-ій/[3-й]adj:m:v_naz:&numr|3-ій/[3-й]adj:m:v_zna:rinanim:&numr", tokenizer, tagger);
    TestTools.myAssert("5-ій", "5-ій/[5-й]adj:f:v_dav:&numr|5-ій/[5-й]adj:f:v_mis:&numr", tokenizer, tagger);

    // n-dash
    TestTools.myAssert("54–річна", "54–річна/[54-річний]adj:f:v_kly|54–річна/[54-річний]adj:f:v_naz", tokenizer, tagger);
    assertNotTagged("54–бкх");

    TestTools.myAssert("15-ти", "15-ти/[15]numr:p:v_dav:bad|15-ти/[15]numr:p:v_mis:bad|15-ти/[15]numr:p:v_rod:bad", tokenizer, tagger);

    TestTools.myAssert("100-річчя", "100-річчя/[100-річчя]noun:inanim:n:v_kly|100-річчя/[100-річчя]noun:inanim:n:v_naz|100-річчя/[100-річчя]noun:inanim:n:v_rod|100-річчя/[100-річчя]noun:inanim:n:v_zna|100-річчя/[100-річчя]noun:inanim:p:v_kly|100-річчя/[100-річчя]noun:inanim:p:v_naz|100-річчя/[100-річчя]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("100-метрівка", "100-метрівка/[100-метрівка]noun:inanim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("100-відсотково", "100-відсотково/[100-відсотково]adv", tokenizer, tagger);
    TestTools.myAssert("120-мм", "120-мм/[120-мм]adj:f:v_dav|120-мм/[120-мм]adj:f:v_mis|120-мм/[120-мм]adj:f:v_naz|120-мм/[120-мм]adj:f:v_oru|120-мм/[120-мм]adj:f:v_rod|120-мм/[120-мм]adj:f:v_zna"
        + "|120-мм/[120-мм]adj:m:v_dav|120-мм/[120-мм]adj:m:v_mis|120-мм/[120-мм]adj:m:v_naz|120-мм/[120-мм]adj:m:v_oru|120-мм/[120-мм]adj:m:v_rod|120-мм/[120-мм]adj:m:v_zna"
        + "|120-мм/[120-мм]adj:n:v_dav|120-мм/[120-мм]adj:n:v_mis|120-мм/[120-мм]adj:n:v_naz|120-мм/[120-мм]adj:n:v_oru|120-мм/[120-мм]adj:n:v_rod|120-мм/[120-мм]adj:n:v_zna"
        + "|120-мм/[120-мм]adj:p:v_dav|120-мм/[120-мм]adj:p:v_mis|120-мм/[120-мм]adj:p:v_naz|120-мм/[120-мм]adj:p:v_oru|120-мм/[120-мм]adj:p:v_rod|120-мм/[120-мм]adj:p:v_zna", tokenizer, tagger);
  }

  @Test
  public void testNumberedEntities() throws IOException {
    TestTools.myAssert("Євро-2014", "Євро-2014/[Євро-2014]noun:inanim:m:v_dav:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_mis:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_naz:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_oru:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_rod:nv:prop|Євро-2014/[Євро-2014]noun:inanim:m:v_zna:nv:prop", tokenizer, tagger);
    TestTools.myAssert("Ігри-2014", "Ігри-2014/[Гра-2014]noun:inanim:p:v_naz:prop|Ігри-2014/[Гра-2014]noun:inanim:p:v_zna:prop", tokenizer, tagger);
    TestTools.myAssert("ЧЄ-2014", "ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_dav:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_mis:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_naz:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_oru:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_rod:nv:np:prop:abbr|ЧЄ-2014/[ЧЄ-2014]noun:inanim:m:v_zna:nv:np:prop:abbr", tokenizer, tagger);
    TestTools.myAssert("Лондон-2014", "Лондон-2014/[Лондон-2014]noun:inanim:m:v_naz:prop:xp2|Лондон-2014/[Лондон-2014]noun:inanim:m:v_zna:prop:xp2", tokenizer, tagger);
    TestTools.myAssert("Афіни-2014", "Афіни-2014/[Афіни-2014]noun:inanim:p:v_naz:prop:ns|Афіни-2014/[Афіни-2014]noun:inanim:p:v_zna:prop:ns", tokenizer, tagger);
    TestTools.myAssert("Замок-2002", "Замок-2002/[Замок-2002]noun:inanim:m:v_naz:prop|Замок-2002/[Замок-2002]noun:inanim:m:v_zna:prop", tokenizer, tagger);

    TestTools.myAssert("Ан-140", "Ан-140/[Ан-140]noun:inanim:m:v_dav:nv|Ан-140/[Ан-140]noun:inanim:m:v_mis:nv|Ан-140/[Ан-140]noun:inanim:m:v_naz:nv|Ан-140/[Ан-140]noun:inanim:m:v_oru:nv|Ан-140/[Ан-140]noun:inanim:m:v_rod:nv|Ан-140/[Ан-140]noun:inanim:m:v_zna:nv|Ан-140/[Ан-140]noun:inanim:p:v_dav:nv|Ан-140/[Ан-140]noun:inanim:p:v_mis:nv|Ан-140/[Ан-140]noun:inanim:p:v_naz:nv|Ан-140/[Ан-140]noun:inanim:p:v_oru:nv|Ан-140/[Ан-140]noun:inanim:p:v_rod:nv|Ан-140/[Ан-140]noun:inanim:p:v_zna:nv", tokenizer, tagger);
    TestTools.myAssert("ВАЗ-2104", "ВАЗ-2104/[ВАЗ-2104]noun:inanim:f:v_dav:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:f:v_mis:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:f:v_naz:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:f:v_oru:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:f:v_rod:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:f:v_zna:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:m:v_dav:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:m:v_mis:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:m:v_naz:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:m:v_oru:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:m:v_rod:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:m:v_zna:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:p:v_dav:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:p:v_mis:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:p:v_naz:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:p:v_oru:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:p:v_rod:nv|ВАЗ-2104/[ВАЗ-2104]noun:inanim:p:v_zna:nv", tokenizer, tagger);
    TestTools.myAssert("Міг-21М", "Міг-21М/[Міг-21М]noun:inanim:m:v_dav:nv|Міг-21М/[Міг-21М]noun:inanim:m:v_mis:nv|Міг-21М/[Міг-21М]noun:inanim:m:v_naz:nv|Міг-21М/[Міг-21М]noun:inanim:m:v_oru:nv|Міг-21М/[Міг-21М]noun:inanim:m:v_rod:nv|Міг-21М/[Міг-21М]noun:inanim:m:v_zna:nv|Міг-21М/[Міг-21М]noun:inanim:p:v_dav:nv|Міг-21М/[Міг-21М]noun:inanim:p:v_mis:nv|Міг-21М/[Міг-21М]noun:inanim:p:v_naz:nv|Міг-21М/[Міг-21М]noun:inanim:p:v_oru:nv|Міг-21М/[Міг-21М]noun:inanim:p:v_rod:nv|Міг-21М/[Міг-21М]noun:inanim:p:v_zna:nv", tokenizer, tagger);
//    TestTools.myAssert("Площі–2006", "Площі–2006/[Площа-2006]noun:inanim:p:v_dav:prop:ns|Площі–2006/[Площа-2006]noun:inanim:p:v_mis:prop:ns|Площі–2006/[Площа-2006]noun:inanim:p:v_rod:prop:ns", tokenizer, tagger);

    TestTools.myAssert("топ-10", "топ-10/[топ-10]noninfl", tokenizer, tagger);


    TestTools.myAssert("Формули-1", "Формули-1/[Формула-1]noun:inanim:f:v_rod:prop|Формули-1/[Формула-1]noun:inanim:p:v_naz:prop|Формули-1/[Формула-1]noun:inanim:p:v_zna:prop", tokenizer, tagger);
    TestTools.myAssert("Карпати-2", "Карпати-2/[Карпати-2]noun:inanim:p:v_naz:prop:geo:ns|Карпати-2/[Карпати-2]noun:inanim:p:v_zna:prop:geo:ns", tokenizer, tagger);
    TestTools.myAssert("ТЕЦ-1", "ТЕЦ-1/[ТЕЦ-1]noun:inanim:f:v_dav:nv|ТЕЦ-1/[ТЕЦ-1]noun:inanim:f:v_mis:nv|ТЕЦ-1/[ТЕЦ-1]noun:inanim:f:v_naz:nv|ТЕЦ-1/[ТЕЦ-1]noun:inanim:f:v_oru:nv|ТЕЦ-1/[ТЕЦ-1]noun:inanim:f:v_rod:nv|ТЕЦ-1/[ТЕЦ-1]noun:inanim:f:v_zna:nv", tokenizer, tagger);

    TestTools.myAssert("омега-3", "омега-3/[омега-3]noun:inanim:f:v_dav:nv|омега-3/[омега-3]noun:inanim:f:v_mis:nv|омега-3/[омега-3]noun:inanim:f:v_naz:nv|омега-3/[омега-3]noun:inanim:f:v_oru:nv|омега-3/[омега-3]noun:inanim:f:v_rod:nv|омега-3/[омега-3]noun:inanim:f:v_zna:nv|омега-3/[омега-3]noun:inanim:p:v_dav:nv|омега-3/[омега-3]noun:inanim:p:v_mis:nv|омега-3/[омега-3]noun:inanim:p:v_naz:nv|омега-3/[омега-3]noun:inanim:p:v_oru:nv|омега-3/[омега-3]noun:inanim:p:v_rod:nv|омега-3/[омега-3]noun:inanim:p:v_zna:nv", tokenizer, tagger);

    assertNotTagged("Берлін-7");
  }

  @Test
  public void testDynamicTaggingParts() throws IOException {
    TestTools.myAssert("по-свинячому", "по-свинячому/[по-свинячому]adv", tokenizer, tagger);
    TestTools.myAssert("по-сибірськи", "по-сибірськи/[по-сибірськи]adv", tokenizer, tagger);

    TestTools.myAssert("давай-но", "давай-но/[давати]verb:imperf:impr:s:2", tokenizer, tagger);
    TestTools.myAssert("дивіться-но", "дивіться-но/[дивитися]verb:rev:imperf:impr:p:2", tokenizer, tagger);
    TestTools.myAssert("гей-но", "гей-но/[гей]intj", tokenizer, tagger);
    TestTools.myAssert("цить-но", "цить-но/[цить]intj", tokenizer, tagger);
    TestTools.myAssert("бачиш-но", "бачиш-но/[бачити]verb:imperf:pres:s:2:&insert", tokenizer, tagger);
    
    TestTools.myAssert("той-таки", "той-таки/[той-таки]adj:m:v_naz:&pron:dem|той-таки/[той-таки]adj:m:v_zna:rinanim:&pron:dem", tokenizer, tagger);
    TestTools.myAssert("буде-таки", "буде-таки/[бути]verb:imperf:futr:s:3", tokenizer, tagger);
    TestTools.myAssert("там-таки", "там-таки/[там]adv:&pron:dem|там-таки/[там]part", tokenizer, tagger);
    TestTools.myAssert("зразу-таки", "зразу-таки/[зразу]adv", tokenizer, tagger);
    TestTools.myAssert("першого-таки", "першого-таки/[перший]adj:m:v_rod:compb:&numr|першого-таки/[перший]adj:m:v_zna:ranim:compb:&numr|першого-таки/[перший]adj:n:v_rod:compb:&numr", tokenizer, tagger);
    TestTools.myAssert("варто-таки", "варто-таки/[варто]noninfl:&predic", tokenizer, tagger);
//    TestTools.myAssert("добряче-таки", "добряче-таки/[добряче]adv", tokenizer, tagger);
    
    TestTools.myAssert("оцей-от", "оцей-от/[оцей]adj:m:v_naz:&pron:dem|оцей-от/[оцей]adj:m:v_zna:rinanim:&pron:dem", tokenizer, tagger);
    
    TestTools.myAssert("ану-бо", "ану-бо/[ану]intj|ану-бо/[ану]part", tokenizer, tagger);
    TestTools.myAssert("годі-бо", "годі-бо/[годі]adv:&predic", tokenizer, tagger);

    TestTools.myAssert("оттакий-то", "оттакий-то/[оттакий]adj:m:v_naz:&pron:dem:rare|оттакий-то/[оттакий]adj:m:v_zna:rinanim:&pron:dem:rare", tokenizer, tagger);
//  TestTools.myAssert("геть-то", "геть-то/[геть]adv", tokenizer, tagger);
//    TestTools.myAssert("дуже-то", "дуже-то/[дуже]adv:compb", tokenizer, tagger);
    TestTools.myAssert("дуже-то", "дуже-то/[дуже]adv:compb|дуже-то/[дужий]adj:n:v_kly:compb|дуже-то/[дужий]adj:n:v_naz:compb|дуже-то/[дужий]adj:n:v_zna:compb", tokenizer, tagger);
    TestTools.myAssert("десь-то", "десь-то/[десь-то]adv", tokenizer, tagger);   //TODO: :&pron:ind
    TestTools.myAssert("котрий-то", "котрий-то/[котрий]adj:m:v_naz:&pron:int:rel|котрий-то/[котрий]adj:m:v_zna:rinanim:&pron:int:rel", tokenizer, tagger);   //TODO: :&pron:ind
    TestTools.myAssert("ніби-то", "ніби-то/[ніби]conj:subord", tokenizer, tagger);
    TestTools.myAssert("вони-то", "вони-то/[вони]noun:unanim:p:v_naz:&pron:pers:3", tokenizer, tagger);
    TestTools.myAssert("права-то", "права-то/[правий]adj:f:v_kly:compb|права-то/[правий]adj:f:v_naz:compb|права-то/[право]noun:inanim:n:v_rod|права-то/[право]noun:inanim:p:v_kly|права-то/[право]noun:inanim:p:v_naz|права-то/[право]noun:inanim:p:v_zna", tokenizer, tagger);
    
    assertNotTagged("хто-то");
    assertNotTagged("що-то");
    assertNotTagged("чи-то");
    assertNotTagged("как-то");
    assertNotTagged("кто-то");
    assertNotTagged("до-пари");
  }

  @Test
  public void testDynamicTaggingXShaped() throws IOException {
    TestTools.myAssert("Ш-подібному", "Ш-подібному/[Ш-подібний]adj:m:v_dav:compb|Ш-подібному/[Ш-подібний]adj:m:v_mis:compb|Ш-подібному/[Ш-подібний]adj:n:v_dav:compb|Ш-подібному/[Ш-подібний]adj:n:v_mis:compb", tokenizer, tagger);
    TestTools.myAssert("S-подібної", "S-подібної/[S-подібний]adj:f:v_rod:compb", tokenizer, tagger);
  }
  
  @Test
  public void testDynamicTaggingPrefixes() throws IOException {
    TestTools.myAssert("VIP–будинок", "VIP–будинок/[VIP-будинок]noun:inanim:m:v_naz|VIP–будинок/[VIP-будинок]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("PR-департаменту", "PR-департаменту/[PR-департамент]noun:inanim:m:v_dav|PR-департаменту/[PR-департамент]noun:inanim:m:v_mis|PR-департаменту/[PR-департамент]noun:inanim:m:v_rod", tokenizer, tagger);
    TestTools.myAssert("3D-друк", "3D-друк/[3D-друк]noun:inanim:m:v_naz|3D-друк/[3D-друк]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("n-векторний", "n-векторний/[n-векторний]adj:m:v_naz|n-векторний/[n-векторний]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("α-векторний", "α-векторний/[α-векторний]adj:m:v_naz|α-векторний/[α-векторний]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("α-діапазон", "α-діапазон/[α-діапазон]noun:inanim:m:v_naz|α-діапазон/[α-діапазон]noun:inanim:m:v_zna", tokenizer, tagger);
//    TestTools.myAssert("POS-термінальна", "", tokenizer, tagger);
//    TestTools.myAssert("IT-Академії", "IT-Академії/[IT-академія]noun:inanim:f:v_dav|IT-Академії/[IT-академія]noun:inanim:f:v_mis|IT-Академії/[IT-академія]noun:inanim:f:v_rod|IT-Академії/[IT-академія]noun:inanim:p:v_naz|IT-Академії/[IT-академія]noun:inanim:p:v_zna", tokenizer, tagger);
  }

  @Test
  public void testTwoHypens() throws IOException {
    TestTools.myAssert("синьо-біло-жовтий", "синьо-біло-жовтий/[синьо-біло-жовтий]adj:m:v_kly|синьо-біло-жовтий/[синьо-біло-жовтий]adj:m:v_naz|синьо-біло-жовтий/[синьо-біло-жовтий]adj:m:v_zna:rinanim", tokenizer, tagger);      
    TestTools.myAssert("українсько-англійсько-французьким", "українсько-англійсько-французьким/[українсько-англійсько-французький]adj:m:v_oru|українсько-англійсько-французьким/[українсько-англійсько-французький]adj:n:v_oru|українсько-англійсько-французьким/[українсько-англійсько-французький]adj:p:v_dav", tokenizer, tagger);
    TestTools.myAssert("седативного-снодійного-антигістамінного", "седативного-снодійного-антигістамінного/[снодійний-антигістамінний]adj:m:v_rod|седативного-снодійного-антигістамінного/[снодійний-антигістамінний]adj:m:v_zna:ranim|седативного-снодійного-антигістамінного/[снодійний-антигістамінний]adj:n:v_rod", tokenizer, tagger);

    assertNotTagged("військо-во-політичний");

    // nouns are too complicated
    //    TestTools.myAssert("фолк-рок-групу", "фолк-рок-групу/[фолк-рок-група]noun:inanim:f:v_zna", tokenizer, tagger);
//    assertNotTagged("етно-джаз-рок");
    
    // dash-prefix2
    TestTools.myAssert("он-лайн-навчання", "он-лайн-навчання/[он-лайн-навчання]noun:inanim:n:v_naz|он-лайн-навчання/[он-лайн-навчання]noun:inanim:n:v_rod|он-лайн-навчання/[он-лайн-навчання]noun:inanim:n:v_zna|он-лайн-навчання/[он-лайн-навчання]noun:inanim:p:v_naz|он-лайн-навчання/[он-лайн-навчання]noun:inanim:p:v_zna", tokenizer, tagger);

    assertNotTagged("р--електронами");
  }

  @Test
  public void testHypenStretch() throws IOException {
//    TestTools.myAssert("ду-у-у-же", "ду-у-у-же/[дуже]adv:compb:coll|ду-у-у-же/[дужий]adj:n:v_kly:compb:coll|ду-у-у-же/[дужий]adj:n:v_naz:compb:coll|ду-у-у-же/[дужий]adj:n:v_zna:compb:coll", tokenizer, tagger);
//    TestTools.myAssert("ду-у-у-уже", "ду-у-у-уже/[дуже]adv:compb:coll|ду-у-у-уже/[дужий]adj:n:v_kly:compb:coll|ду-у-у-уже/[дужий]adj:n:v_naz:compb:coll|ду-у-у-уже/[дужий]adj:n:v_zna:compb:coll", tokenizer, tagger);
    TestTools.myAssert("Та-а-ак", "Та-а-ак/[так]adv:&pron:dem:coll|Та-а-ак/[так]conj:coord:subord:coll|Та-а-ак/[так]part:coll", tokenizer, tagger);
    TestTools.myAssert("Му-у-у", "Му-у-у/[му]intj:coll", tokenizer, tagger);
    TestTools.myAssert("С-с-с-лава", "С-с-с-лава/[слава]noun:inanim:f:v_naz:coll", tokenizer, tagger);
    //TODO: should technically tag both lowercase and uppercase to get :fname as well
//    TestTools.myAssert("Да-а-ри", "", tokenizer, tagger);
    TestTools.myAssert("т-то", "т-то/[null]null", tokenizer, tagger);
  }
  
  @Test
  public void testDynamicTaggingFullTagMatch() throws IOException {
    TestTools.myAssert("пів-України", "пів-України/[пів-України]noun:inanim:f:v_dav:prop:geo:ua_1992|пів-України/[пів-України]noun:inanim:f:v_mis:prop:geo:ua_1992|пів-України/[пів-України]noun:inanim:f:v_naz:prop:geo:ua_1992"
        + "|пів-України/[пів-України]noun:inanim:f:v_oru:prop:geo:ua_1992|пів-України/[пів-України]noun:inanim:f:v_rod:prop:geo:ua_1992|пів-України/[пів-України]noun:inanim:f:v_zna:prop:geo:ua_1992"
        , tokenizer, tagger);

    TestTools.myAssert("Пенсильванія-авеню", "Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_dav:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_mis:nv:prop"
        + "|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_naz:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_oru:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_rod:nv:prop|Пенсильванія-авеню/[Пенсильванія-авеню]noun:inanim:f:v_zna:nv:prop"
        , tokenizer, tagger);
    TestTools.myAssert("Уолл-стрит", "Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_dav:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_mis:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_naz:nv:prop"
        + "|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_oru:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_rod:nv:prop|Уолл-стрит/[Уолл-стрит]noun:inanim:f:v_zna:nv:prop"
        , tokenizer, tagger);
    

    // full tag match
    

    TestTools.myAssert("жило-було", "жило-було/[жити-бути]verb:imperf:past:n", tokenizer, tagger);
    TestTools.myAssert("учиш-учиш", "учиш-учиш/[учити-учити]verb:imperf:pres:s:2", tokenizer, tagger);

    TestTools.myAssert("а-а", "а-а/[а-а]intj", tokenizer, tagger);
    TestTools.myAssert("Га-га", "Га-га/[га-га]intj", tokenizer, tagger);
    TestTools.myAssert("ось\u2013ось", "ось\u2013ось/[ось-ось]adv", tokenizer, tagger);
    TestTools.myAssert("ось\u2011ось", "ось-ось/[ось-ось]adv", tokenizer, tagger);
    TestTools.myAssert("Івано\u2013Франківськ", "Івано–Франківськ/[Івано-Франківськ]noun:inanim:m:v_naz:prop:geo|Івано–Франківськ/[Івано-Франківськ]noun:inanim:m:v_zna:prop:geo", tokenizer, tagger);

    TestTools.myAssert("вгору-вниз", "вгору-вниз/[вгору-вниз]adv", tokenizer, tagger);

    TestTools.myAssert("годину-півтори", "годину-півтори/[година-півтори]noun:inanim:f:v_zna|годину-півтори/[година-півтори]noun:inanim:p:v_zna", tokenizer, tagger);

    TestTools.myAssert("вівторок-середа", "вівторок-середа/[вівторок-середа]noun:inanim:m:v_naz|вівторок-середа/[вівторок-середа]noun:inanim:p:v_naz", tokenizer, tagger);
    TestTools.myAssert("лютого-березня", "лютого-березня/[лютий-березень]noun:inanim:m:v_rod|лютого-березня/[лютий-березень]noun:inanim:p:v_rod", tokenizer, tagger);

    // adv-adv
    TestTools.myAssert("низенько-низенько", "низенько-низенько/[низенько-низенько]adv", tokenizer, tagger);
    
    // adj-adj
    TestTools.myAssert("такого-сякого", "такого-сякого/[такий-сякий]adj:m:v_rod:&pron:def|такого-сякого/[такий-сякий]adj:m:v_zna:ranim:&pron:def|такого-сякого/[такий-сякий]adj:n:v_rod:&pron:def", tokenizer, tagger);
    TestTools.myAssert("великий-превеликий", "великий-превеликий/[великий-превеликий]adj:m:v_kly|великий-превеликий/[великий-превеликий]adj:m:v_naz|великий-превеликий/[великий-превеликий]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("чорній-чорній", "чорній-чорній/[чорний-чорний]adj:f:v_dav|чорній-чорній/[чорний-чорний]adj:f:v_mis|чорній-чорній/[чорніти-чорніти]verb:imperf:impr:s:2", tokenizer, tagger);
    TestTools.myAssert("писаного-переписаного", "писаного-переписаного/[писаний-переписаний]adj:m:v_rod:&adjp:pasv:imperf|писаного-переписаного/[писаний-переписаний]adj:m:v_zna:ranim:&adjp:pasv:imperf|писаного-переписаного/[писаний-переписаний]adj:n:v_rod:&adjp:pasv:imperf", tokenizer, tagger);
    //TODO:
    TestTools.myAssert("минулого–позаминулого", "минулого–позаминулого/[минулий-позаминулий]adj:m:v_rod|минулого–позаминулого/[минулий-позаминулий]adj:m:v_zna:ranim|минулого–позаминулого/[минулий-позаминулий]adj:n:v_rod", tokenizer, tagger);
    
    
    // noun-noun

    TestTools.myAssert("лікар-гомеопат", "лікар-гомеопат/[лікар-гомеопат]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("лікаря-гомеопата", "лікаря-гомеопата/[лікар-гомеопат]noun:anim:m:v_rod|лікаря-гомеопата/[лікар-гомеопат]noun:anim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("шмкр-гомеопат", "шмкр-гомеопат/[null]null", tokenizer, tagger);
    TestTools.myAssert("лікар-ткр", "лікар-ткр/[null]null", tokenizer, tagger);

    TestTools.myAssert("пане-товаришу", "пане-товаришу/[пан-товариш]noun:anim:m:v_kly", tokenizer, tagger);

    TestTools.myAssert("вчинок-приклад", "вчинок-приклад/[вчинок-приклад]noun:inanim:m:v_naz|вчинок-приклад/[вчинок-приклад]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("міста-фортеці", "міста-фортеці/[місто-фортеця]noun:inanim:n:v_rod|міста-фортеці/[місто-фортеця]noun:inanim:p:v_naz|міста-фортеці/[місто-фортеця]noun:inanim:p:v_zna", tokenizer, tagger);


    // TODO: unanim
    TestTools.myAssert("ворог-стафілокок", "ворог-стафілокок/[null]null", tokenizer, tagger);
    TestTools.myAssert("стафілокок-реагент", "стафілокок-реагент/[null]null", tokenizer, tagger);
    
//    TestTools.myAssert("капуджі-ага", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("Каладжі-бей", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("капудан-паша", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("кальфа-ефенді", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);

    TestTools.myAssert("Москви-ріки", "Москви-ріки/[Москва-ріка]noun:inanim:f:v_rod:prop:geo", tokenizer, tagger);

    TestTools.myAssert("Дівчинка-першокласниця", "Дівчинка-першокласниця/[дівчинка-першокласниця]noun:anim:f:v_naz", tokenizer, tagger);

    TestTools.myAssert("RPM-пакунок", "RPM-пакунок/[RPM-пакунок]noun:inanim:m:v_naz|RPM-пакунок/[RPM-пакунок]noun:inanim:m:v_zna", tokenizer, tagger);

    //TODO:
//    TestTools.myAssert("ненависть-шоу", "", tokenizer, tagger);

    // handled by different logic
//    assertNotTagged("напів-люкс");
    assertNotTagged("пів–качана");
    assertNotTagged("Малишко-це");
    assertNotTagged("відносини-коли");
    assertNotTagged("лісо-та");
    assertNotTagged("Ліс-наш");
    assertNotTagged("Нью-Париж");
    assertNotTagged("кохання-найщиріше"); // - мало би бути тире
    assertNotTagged("Донець-кий");
    assertNotTagged("мас-штаби");
    assertNotTagged("рибо-полювання");
    assertNotTagged("вовіки-вічні");
    assertNotTagged("юре-юре");
  }

  @Test
  public void testDynamicTaggingIntj() throws IOException {
    TestTools.myAssert("Гей-гей-гей", "Гей-гей-гей/[гей-гей-гей]intj", tokenizer, tagger);
    TestTools.myAssert("Ого-го-го-го", "Ого-го-го-го/[ого-го-го-го]intj", tokenizer, tagger);
    //TODO:
    assertNotTagged("йо-йо-тусовки");
  }
  
  @Test
  public void testCompoundUpper() throws IOException {
    // dictionary only has "соціал-демократичний" and LT only uppercases first letter
    TestTools.myAssert("Соціал-Демократичний", "Соціал-Демократичний/[соціал-демократичний]adj:m:v_kly|Соціал-Демократичний/[соціал-демократичний]adj:m:v_naz|Соціал-Демократичний/[соціал-демократичний]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("Івано-Франківська", "Івано-Франківська/[Івано-Франківськ]noun:inanim:m:v_rod:prop:geo|Івано-Франківська/[івано-франківський]adj:f:v_kly|Івано-Франківська/[івано-франківський]adj:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("Переяслав-Хмельницького", "Переяслав-Хмельницького/[переяслав-хмельницький]adj:m:v_rod|Переяслав-Хмельницького/[переяслав-хмельницький]adj:m:v_zna:ranim|Переяслав-Хмельницького/[переяслав-хмельницький]adj:n:v_rod", tokenizer, tagger);
  }  

  @Test
  public void testDynamicTaggingNum() throws IOException {
    // numr-numr
    TestTools.myAssert("одним-двома", "одним-двома/[один-два]numr:p:v_oru", tokenizer, tagger);
    TestTools.myAssert("одного-другого", "одного-другого/[один-другий]numr:m:v_rod:&numr|одного-другого/[один-другий]numr:m:v_zna:ranim:&numr|одного-другого/[один-другий]numr:n:v_rod:&numr", tokenizer, tagger);
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
  }
  
  @Test
  public void testDynamicTaggingFullOthers() throws IOException {
    // others

//    TestTools.myAssert("етно-диско", "етно-диско/[null]null", tokenizer, tagger);
    TestTools.myAssert("екс-партнер", "екс-партнер/[екс-партнер]noun:anim:m:v_naz", tokenizer, tagger);
    TestTools.myAssert("еспресо-машина", "еспресо-машина/[еспресо-машина]noun:inanim:f:v_naz", tokenizer, tagger);

    TestTools.myAssert("кава-еспресо", "кава-еспресо/[кава-еспресо]noun:inanim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("кави-еспресо", "кави-еспресо/[кава-еспресо]noun:inanim:f:v_rod|кави-еспресо/[кава-еспресо]noun:inanim:p:v_naz|кави-еспресо/[кава-еспресо]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("програмою-максимум", "програмою-максимум/[програма-максимум]noun:inanim:f:v_oru", tokenizer, tagger);

    // jr/sr

    TestTools.myAssert("Алієва-старшого", "Алієва-старшого/[Алієв-старший]noun:anim:m:v_rod:prop:lname|Алієва-старшого/[Алієв-старший]noun:anim:m:v_zna:prop:lname", tokenizer, tagger);
    // test ranim/rinanim
//    assertNotTagged("Алієва-старший");
//    assertNotTagged("Алієв-старшого");
    
    assertNotTagged("дер-жав");
  }
  
  @Test
  public void testDynamicTaggingInvalidLeft() throws IOException {
    assertTagged("авіа-переліт", "авіа-переліт/[авіа-переліт]noun:inanim:m:v_naz:bad|авіа-переліт/[авіа-переліт]noun:inanim:m:v_zna:bad");
    assertTagged("авто-салон", "авто-салон/[авто-салон]noun:inanim:m:v_naz:bad|авто-салон/[авто-салон]noun:inanim:m:v_zna:bad");
    assertTagged("квазі-держави", "квазі-держави/[квазі-держава]noun:inanim:f:v_rod:bad|квазі-держави/[квазі-держава]noun:inanim:p:v_kly:bad|квазі-держави/[квазі-держава]noun:inanim:p:v_naz:bad|квазі-держави/[квазі-держава]noun:inanim:p:v_zna:bad");
    assertTagged("мульти-візу", "мульти-візу/[мульти-віза]noun:inanim:f:v_zna:bad");
    assertTagged("контр-міри", "контр-міри/[контр-міра]noun:inanim:f:v_rod:bad|контр-міри/[контр-міра]noun:inanim:p:v_kly:bad|контр-міри/[контр-міра]noun:inanim:p:v_naz:bad|контр-міри/[контр-міра]noun:inanim:p:v_zna:bad");
    assertTagged("кіно-критика", "кіно-критика/[кіно-критика]noun:anim:m:v_rod:bad|кіно-критика/[кіно-критика]noun:anim:m:v_zna:bad|кіно-критика/[кіно-критика]noun:inanim:f:v_naz:bad");
    assertTagged("древньо-римський", "древньо-римський/[древньо-римський]adj:m:v_kly:bad|древньо-римський/[древньо-римський]adj:m:v_naz:bad|древньо-римський/[древньо-римський]adj:m:v_zna:rinanim:bad");
    assertTagged("давньо-римський", "давньо-римський/[давньо-римський]adj:m:v_kly:bad|давньо-римський/[давньо-римський]adj:m:v_naz:bad|давньо-римський/[давньо-римський]adj:m:v_zna:rinanim:bad");
    // має бути "максимально можливу"
    assertTagged("максимально-можливу", "максимально-можливу/[максимально-можливий]adj:f:v_zna:bad");
    assertTagged("відео-навчання", "відео-навчання/[відео-навчання]noun:inanim:n:v_kly:bad|відео-навчання/[відео-навчання]noun:inanim:n:v_naz:bad|відео-навчання/[відео-навчання]noun:inanim:n:v_rod:bad|відео-навчання/[відео-навчання]noun:inanim:n:v_zna:bad|відео-навчання/[відео-навчання]noun:inanim:p:v_kly:bad|відео-навчання/[відео-навчання]noun:inanim:p:v_naz:bad|відео-навчання/[відео-навчання]noun:inanim:p:v_zna:bad");
    
    assertTagged("кіно-Європа", "кіно-Європа/[кіно-Європа]noun:inanim:f:v_naz:prop:geo");
    assertNotTagged("теле-та");
    assertNotTagged("квазі-я");
    assertNotTagged("макро-і");
  }

  @Test
  public void testNapiv() throws IOException {
    TestTools.myAssert("напів'японка", "напів'японка/[напів'японка]noun:anim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("напівяпонка", "напівяпонка/[напівяпонка]noun:anim:f:v_naz:bad", tokenizer, tagger);
    TestTools.myAssert("напів-японка", "напів-японка/[напів-японка]noun:anim:f:v_naz:bad", tokenizer, tagger);
    TestTools.myAssert("напів-Європа", "напів-Європа/[напів-Європа]noun:inanim:f:v_naz:prop:geo", tokenizer, tagger);
    TestTools.myAssert("напівсправедливий", "напівсправедливий/[напівсправедливий]adj:m:v_kly|напівсправедливий/[напівсправедливий]adj:m:v_naz|напівсправедливий/[напівсправедливий]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("напіврозслабленого", "напіврозслабленого/[напіврозслаблений]adj:m:v_rod:&&adjp:pasv:perf|напіврозслабленого/[напіврозслаблений]adj:m:v_zna:ranim:&&adjp:pasv:perf|напіврозслабленого/[напіврозслаблений]adj:n:v_rod:&&adjp:pasv:perf", tokenizer, tagger);
    TestTools.myAssert("напів\u2013фантастичних", "напів–фантастичних/[напів-фантастичний]adj:p:v_mis:bad|напів–фантастичних/[напів-фантастичний]adj:p:v_rod:bad|напів–фантастичних/[напів-фантастичний]adj:p:v_zna:ranim:bad", tokenizer, tagger);
    //TODO:
    TestTools.myAssert("напівпольської-напіванглійської", "напівпольської-напіванглійської/[польська-англійська]noun:inanim:f:v_rod|напівпольської-напіванглійської/[польський-англійський]adj:f:v_rod", tokenizer, tagger);
//    TestTools.myAssert("красунями-напівптахами", "", tokenizer, tagger);
    assertNotTagged("напіврозслабеному");   // typo
    assertNotTagged("напіви");
  }
  
  @Test
  public void testDynamicTaggingOWithAdj() throws IOException {
    // adv-adj

    TestTools.myAssert("патолого-анатомічний", "патолого-анатомічний/[патолого-анатомічний]adj:m:v_kly|патолого-анатомічний/[патолого-анатомічний]adj:m:v_naz|патолого-анатомічний/[патолого-анатомічний]adj:m:v_zna:rinanim", tokenizer, tagger);

    assertNotTagged("паталого-анатомічний");
    assertNotTagged("патолога-анатомічний");
    assertNotTagged("патолого-гмкнх");
    assertNotTagged("патолого-голова");

    TestTools.myAssert("дво-триметровий", "дво-триметровий/[дво-триметровий]adj:m:v_kly|дво-триметровий/[дво-триметровий]adj:m:v_naz|дво-триметровий/[дво-триметровий]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("три-чотириметровий", "три-чотириметровий/[три-чотириметровий]adj:m:v_kly|три-чотириметровий/[три-чотириметровий]adj:m:v_naz|три-чотириметровий/[три-чотириметровий]adj:m:v_zna:rinanim", tokenizer, tagger);

    // :bad
    TestTools.myAssert("трьох-чотирьохметровий", "трьох-чотирьохметровий/[трьох-чотирьохметровий]adj:m:v_kly:bad|трьох-чотирьохметровий/[трьох-чотирьохметровий]adj:m:v_naz:bad|трьох-чотирьохметровий/[трьох-чотирьохметровий]adj:m:v_zna:rinanim:bad", tokenizer, tagger);
//    TestTools.myAssert("двох-сторонній", "двох-сторонній/[двох-сторонній]adj:f:v_dav:bad|двох-сторонній/[двох-сторонній]adj:f:v_mis:bad|двох-сторонній/[двох-сторонній]adj:m:v_kly:bad|двох-сторонній/[двох-сторонній]adj:m:v_naz:bad|двох-сторонній/[двох-сторонній]adj:m:v_zna:rinanim:bad", tokenizer, tagger);

    TestTools.myAssert("Художньо-культурний", "Художньо-культурний/[художньо-культурний]adj:m:v_kly|Художньо-культурний/[художньо-культурний]adj:m:v_naz|Художньо-культурний/[художньо-культурний]adj:m:v_zna:rinanim", tokenizer, tagger);
    TestTools.myAssert("сліпуче-яскравого", "сліпуче-яскравого/[сліпуче-яскравий]adj:m:v_rod|сліпуче-яскравого/[сліпуче-яскравий]adj:m:v_zna:ranim|сліпуче-яскравого/[сліпуче-яскравий]adj:n:v_rod", tokenizer, tagger);
    TestTools.myAssert("україно-болгарський", "україно-болгарський/[україно-болгарський]adj:m:v_kly|україно-болгарський/[україно-болгарський]adj:m:v_naz|україно-болгарський/[україно-болгарський]adj:m:v_zna:rinanim", tokenizer, tagger);

    TestTools.myAssert("учбово-спортивної", "учбово-спортивної/[учбово-спортивний]adj:f:v_rod:bad", tokenizer, tagger);

    TestTools.myAssert("етико-філологічний", "етико-філологічний/[етико-філологічний]adj:m:v_kly|етико-філологічний/[етико-філологічний]adj:m:v_naz|етико-філологічний/[етико-філологічний]adj:m:v_zna:rinanim", tokenizer, tagger);

    // :bad - має бути без дефісу: "важконапрацьований"
    TestTools.myAssert("важко-напрацьований", "важко-напрацьований/[важко-напрацьований]adj:m:v_kly:&adjp:pasv:perf:bad|важко-напрацьований/[важко-напрацьований]adj:m:v_naz:&adjp:pasv:perf:bad|важко-напрацьований/[важко-напрацьований]adj:m:v_zna:rinanim:&adjp:pasv:perf:bad", tokenizer, tagger);

    TestTools.myAssert("Альпійсько-Карпатського", "Альпійсько-Карпатського/[альпійсько-карпатський]adj:m:v_rod|Альпійсько-Карпатського/[альпійсько-карпатський]adj:m:v_zna:ranim|Альпійсько-Карпатського/[альпійсько-карпатський]adj:n:v_rod", tokenizer, tagger);
    TestTools.myAssert("Азово-Чорноморського", "Азово-Чорноморського/[азово-чорноморський]adj:m:v_rod|Азово-Чорноморського/[азово-чорноморський]adj:m:v_zna:ranim|Азово-Чорноморського/[азово-чорноморський]adj:n:v_rod", tokenizer, tagger);
    
    assertNotTagged("бірмюково-блакитний");
    assertNotTagged("во-політичний");
    assertNotTagged("о-політичний");
    
    assertNotTagged("рово-часового");


//    TestTools.myAssert("американо-блакитний", "американо-блакитний/[null]null", tokenizer, tagger);

    // don't allow dash when the words spelled together
    assertNotTagged("зовнішньо-економічний");
    assertNotTagged("високо-релевантною");
    assertNotTagged("всесвітньо-відомі");

//  assertNotTagged("льотно-посадкова"); - загубилося початкове "з". Але це не спинило тегувальника - чому?


//    TestTools.myAssert("дітей-сиріт", "дітей-сиріт/[діти-сироти]noun:anim:p:v_rod", tokenizer, tagger);
//    TestTools.myAssert("курей-бройлерів", "кури-бройлери", tokenizer, tagger);

    TestTools.myAssert("еко-ресурсний", "еко-ресурсний/[еко-ресурсний]adj:m:v_kly|еко-ресурсний/[еко-ресурсний]adj:m:v_naz|еко-ресурсний/[еко-ресурсний]adj:m:v_zna:rinanim", tokenizer, tagger);
  }

  @Test
  public void testDynamicAnimInanim() throws IOException {
    TestTools.myAssert("вчених-новаторів", "вчених-новаторів/[вчений-новатор]noun:anim:p:v_rod|вчених-новаторів/[вчений-новатор]noun:anim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("країна-виробник", "країна-виробник/[країна-виробник]noun:inanim:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("банк-виробник", "банк-виробник/[банк-виробник]noun:inanim:m:v_naz|банк-виробник/[банк-виробник]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("банки-агенти", "банки-агенти/[банк-агент]noun:inanim:p:v_naz|банки-агенти/[банк-агент]noun:inanim:p:v_zna|банки-агенти/[банка-агент]noun:inanim:p:v_naz|банки-агенти/[банка-агент]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("місто-гігант", "місто-гігант/[місто-гігант]noun:inanim:n:v_naz|місто-гігант/[місто-гігант]noun:inanim:n:v_zna", tokenizer, tagger);
    TestTools.myAssert("країни-агресори", "країни-агресори/[країна-агресор]noun:inanim:p:v_naz|країни-агресори/[країна-агресор]noun:inanim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("поселення-гігант", "поселення-гігант/[поселення-гігант]noun:inanim:n:v_naz|поселення-гігант/[поселення-гігант]noun:inanim:n:v_zna", tokenizer, tagger);
    TestTools.myAssert("бот-учитель", "бот-учитель/[бот-учитель]noun:inanim:m:v_naz|бот-учитель/[бот-учитель]noun:inanim:m:v_zna", tokenizer, tagger);
    
    TestTools.myAssert("сонях-красень", "сонях-красень/[сонях-красень]noun:inanim:m:v_naz|сонях-красень/[сонях-красень]noun:inanim:m:v_naz|сонях-красень/[сонях-красень]noun:inanim:m:v_zna|сонях-красень/[сонях-красень]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("красень-сонях", "красень-сонях/[красень-сонях]noun:inanim:m:v_naz|красень-сонях/[красень-сонях]noun:inanim:m:v_naz|красень-сонях/[красень-сонях]noun:inanim:m:v_zna|красень-сонях/[красень-сонях]noun:inanim:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("депутатів-привидів", "депутатів-привидів/[депутат-привид]noun:anim:p:v_rod|депутатів-привидів/[депутат-привид]noun:anim:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("дівчата-зірочки", "дівчата-зірочки/[дівчина-зірочка]noun:anim:p:v_naz", tokenizer, tagger);

    // про місяця-місяченька
//  TestTools.myAssert("бабці-Австрії",  "", tokenizer, tagger);
  // змагання зі слалому-гіганту
  // голосувати за Тимошенко-прем’єра
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

    TestTools.myAssert("все-транс", "все-транс/[null]null", tokenizer, tagger);
    TestTools.myAssert("транс-все", "транс-все/[null]null", tokenizer, tagger);
    assertNotTagged("спа-салоне");


    // \n may happen in words when we have soft-hyphen wrap: \u00AD\n
    // in this case we strip \u00AD but leave \n in the word
    // but this may not happen often, need research if we need this
//    TestTools.myAssert("стін\nку", "", tokenizer, tagger);
  }

  @Test
  public void testAltSpelling() throws IOException {
    TestTools.myAssert("тренінґ", "тренінґ/[тренінґ]noun:inanim:m:v_naz:alt|тренінґ/[тренінґ]noun:inanim:m:v_zna:alt", tokenizer, tagger);
    TestTools.myAssert("антирадіяційно", "антирадіяційно/[антирадіяційно]adv:alt", tokenizer, tagger);
    TestTools.myAssert("фотометер", "фотометер/[фотометер]noun:inanim:m:v_naz:alt|фотометер/[фотометер]noun:inanim:m:v_zna:alt", tokenizer, tagger);

    assertNotTagged("австріях");
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

  private void assertTagged(String word, String tagged) {
    try {
      TestTools.myAssert(word, tagged, tokenizer, tagger);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
