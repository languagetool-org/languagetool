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

import junit.framework.TestCase;

import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;

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

    // one-way case sensitivity
    TestTools.myAssert("києві", "києві/[кий]noun:m:v_dav|києві/[кий]noun:m:v_mis", tokenizer, tagger);
    TestTools.myAssert("Києві", "Києві/[Київ]noun:m:v_mis|Києві/[кий]noun:m:v_dav|Києві/[кий]noun:m:v_mis", tokenizer, tagger);
    TestTools.myAssert("віл", "віл/[віл]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("Віл", "Віл/[віл]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("ВІЛ", "ВІЛ/[ВІЛ]noun:m:v_dav:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_mis:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_naz:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_oru:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_rod:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_zna:nv:np:abbr|ВІЛ/[віл]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("далі", "далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Далі", "Далі/[Даль]noun:m:v_mis:anim:lname|Далі/[Далі]noun:m:v_dav:nv:np:anim:lname|Далі/[Далі]noun:m:v_mis:nv:np:anim:lname|Далі/[Далі]noun:m:v_naz:nv:np:anim:lname|Далі/[Далі]noun:m:v_oru:nv:np:anim:lname|Далі/[Далі]noun:m:v_rod:nv:np:anim:lname|Далі/[Далі]noun:m:v_zna:nv:np:anim:lname|Далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Бен", "Бен/[Бен]noun:m:v_naz:anim:fname|Бен/[бен]unknown", tokenizer, tagger);
    TestTools.myAssert("бен", "бен/[бен]unknown", tokenizer, tagger);


    TestTools.myAssert("Справу порушено судом", 
      "Справу/[справа]noun:f:v_zna -- порушено/[порушити]verb:impers:perf -- судом/[суд]noun:m:v_oru|судом/[судома]noun:p:v_rod",
       tokenizer, tagger);
       
    String expected = 
      "Майже/[майже]adv -- два/[два]numr:m:v_naz|два/[два]numr:m:v_zna|два/[два]numr:n:v_naz|два/[два]numr:n:v_zna -- роки/[рік]noun:p:v_naz|роки/[рік]noun:p:v_zna"
    + " -- тому/[той]adj:m:v_dav:&pron:dem|тому/[той]adj:m:v_mis:&pron:dem|тому/[той]adj:n:v_dav:&pron:dem|тому/[той]adj:n:v_mis:&pron:dem|тому/[том]noun:m:v_dav|тому/[том]noun:m:v_mis|тому/[том]noun:m:v_rod|тому/[тому]adv|тому/[тому]conj:subord"
    + " -- Люба/[Люба]noun:f:v_naz:anim:fname|Люба/[любий]adj:f:v_naz -- разом/[раз]noun:m:v_oru|разом/[разом]adv -- із/[із]prep:rv_rod:rv_zna:rv_oru"
    + " -- чоловіком/[чоловік]noun:m:v_oru:anim -- Степаном/[Степан]noun:m:v_oru:anim:fname -- виїхали/[виїхати]verb:past:m:perf -- туди/[туди]adv:&pron:dem"
    + " -- на/[на]excl|на/[на]part|на/[на]prep:rv_zna:rv_mis -- "
    + "проживання/[проживання]noun:n:v_naz|проживання/[проживання]noun:n:v_rod|проживання/[проживання]noun:n:v_zna|проживання/[проживання]noun:p:v_naz|проживання/[проживання]noun:p:v_zna";
  
    TestTools.myAssert("Майже два роки тому Люба разом із чоловіком Степаном виїхали туди на проживання.",
        expected, tokenizer, tagger);
  }

  public void testNumberTagging() throws IOException {
    TestTools.myAssert("101,234", "101,234/[101,234]number", tokenizer, tagger);
    TestTools.myAssert("3,5-5,6% 7° 7,4°С", "3,5-5,6%/[3,5-5,6%]number -- 7°/[7°]number -- 7,4°С/[7,4°С]number", tokenizer, tagger);
    TestTools.myAssert("XIX", "XIX/[XIX]number", tokenizer, tagger);

    TestTools.myAssert("14.07.2001", "14.07.2001/[14.07.2001]date", tokenizer, tagger);

    TestTools.myAssert("о 15.33", "о/[о]excl|о/[о]prep:rv_zna:rv_mis -- 15.33/[15.33]time", tokenizer, tagger);
    TestTools.myAssert("О 1:05", "О/[о]excl|О/[о]prep:rv_zna:rv_mis -- 1:05/[1:05]time", tokenizer, tagger);
  }
  
  public void testTaggingWithDots() throws IOException {
    TestTools.myAssert("300 р. до н. е.", 
      "300/[300]number -- р./[р.]noun:f:v_dav:nv:np:abbr|р./[р.]noun:f:v_mis:nv:np:abbr|р./[р.]noun:f:v_naz:nv:np:abbr|р./[р.]noun:f:v_oru:nv:np:abbr|р./[р.]noun:f:v_rod:nv:np:abbr|р./[р.]noun:f:v_zna:nv:np:abbr|р./[р.]noun:m:v_dav:nv:np:abbr|р./[р.]noun:m:v_mis:nv:np:abbr|р./[р.]noun:m:v_naz:nv:np:abbr|р./[р.]noun:m:v_oru:nv:np:abbr|р./[р.]noun:m:v_rod:nv:np:abbr|р./[р.]noun:m:v_zna:nv:np:abbr -- до/[до]noun:n:v_dav:nv|до/[до]noun:n:v_mis:nv|до/[до]noun:n:v_naz:nv|до/[до]noun:n:v_oru:nv|до/[до]noun:n:v_rod:nv|до/[до]noun:n:v_zna:nv|до/[до]noun:p:v_dav:nv|до/[до]noun:p:v_mis:nv|до/[до]noun:p:v_naz:nv|до/[до]noun:p:v_oru:nv|до/[до]noun:p:v_rod:nv|до/[до]noun:p:v_zna:nv|до/[до]prep:rv_rod -- "
       + "н./[н.]adj:f:v_dav:nv:abbr|н./[н.]adj:f:v_mis:nv:abbr|н./[н.]adj:f:v_naz:nv:abbr|н./[н.]adj:f:v_oru:nv:abbr|н./[н.]adj:f:v_rod:nv:abbr|н./[н.]adj:f:v_zna:nv:abbr|н./[н.]adj:m:v_dav:nv:abbr|н./[н.]adj:m:v_mis:nv:abbr|н./[н.]adj:m:v_naz:nv:abbr|н./[н.]adj:m:v_oru:nv:abbr|н./[н.]adj:m:v_rod:nv:abbr|н./[н.]adj:m:v_zna:nv:abbr|н./[н.]adj:n:v_dav:nv:abbr|н./[н.]adj:n:v_mis:nv:abbr|н./[н.]adj:n:v_naz:nv:abbr|н./[н.]adj:n:v_oru:nv:abbr|н./[н.]adj:n:v_rod:nv:abbr|н./[н.]adj:n:v_zna:nv:abbr|н./[н.]adj:p:v_dav:nv:abbr|н./[н.]adj:p:v_mis:nv:abbr|н./[н.]adj:p:v_naz:nv:abbr|н./[н.]adj:p:v_oru:nv:abbr|н./[н.]adj:p:v_rod:nv:abbr|н./[н.]adj:p:v_zna:nv:abbr -- "
       + "е./[е.]noun:f:v_dav:nv:abbr|е./[е.]noun:f:v_mis:nv:abbr|е./[е.]noun:f:v_naz:nv:abbr|е./[е.]noun:f:v_oru:nv:abbr|е./[е.]noun:f:v_rod:nv:abbr|е./[е.]noun:f:v_zna:nv:abbr|е./[е.]noun:p:v_dav:nv:abbr|е./[е.]noun:p:v_mis:nv:abbr|е./[е.]noun:p:v_naz:nv:abbr|е./[е.]noun:p:v_oru:nv:abbr|е./[е.]noun:p:v_rod:nv:abbr|е./[е.]noun:p:v_zna:nv:abbr",
       tokenizer, tagger);

    TestTools.myAssert("300 тис. гривень", 
        "300/[300]number -- тис./[тис.]numr:f:v_dav:nv:abbr|тис./[тис.]numr:f:v_mis:nv:abbr|тис./[тис.]numr:f:v_naz:nv:abbr|тис./[тис.]numr:f:v_oru:nv:abbr|тис./[тис.]numr:f:v_rod:nv:abbr|тис./[тис.]numr:f:v_zna:nv:abbr -- гривень/[гривня]noun:p:v_rod",
         tokenizer, tagger);

//    TestTools.myAssert("Є.Бакуліна.",
//      "Є.Бакуліна[Бакулін]noun:m:v_rod:anim:lname|Є.Бакуліна[Бакулін]noun:m:v_zna:anim:lname",
//       tokenizer, tagger);
  }
  
  public void testDynamicTagging() throws IOException {
    TestTools.myAssert("г-г-г", "г-г-г/[null]null", tokenizer, tagger);
    
    TestTools.myAssert("100-річному", "100-річному/[100-річний]adj:m:v_dav|100-річному/[100-річний]adj:m:v_mis|100-річному/[100-річний]adj:n:v_dav|100-річному/[100-річний]adj:n:v_mis", tokenizer, tagger);
    TestTools.myAssert("100-й", "100-й/[100-й]adj:m:v_naz|100-й/[100-й]adj:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("50-х", "50-х/[50-й]adj:p:v_rod|50-х/[50-й]adj:p:v_zna", tokenizer, tagger);

    TestTools.myAssert("по-свинячому", "по-свинячому/[по-свинячому]adv", tokenizer, tagger);
    TestTools.myAssert("по-сибірськи", "по-сибірськи/[по-сибірськи]adv", tokenizer, tagger);

    TestTools.myAssert("давай-но", "давай-но/[давати]verb:impr:s:2:imperf", tokenizer, tagger);
    TestTools.myAssert("дивіться-но", "дивіться-но/[дивитися]verb:rev:impr:p:2:imperf", tokenizer, tagger);
    TestTools.myAssert("той-таки", "той-таки/[той-таки]adj:m:v_naz:&pron:dem|той-таки/[той-таки]adj:m:v_zna:&pron:dem", tokenizer, tagger);
    TestTools.myAssert("буде-таки", "буде-таки/[бути]verb:futr:s:3:imperf", tokenizer, tagger);
    TestTools.myAssert("оцей-от", "оцей-от/[оцей]adj:m:v_naz:&pron:dem|оцей-от/[оцей]adj:m:v_zna:&pron:dem", tokenizer, tagger);
    TestTools.myAssert("оттакий-то", "оттакий-то/[оттакий]adj:m:v_naz:&pron:dem:rare|оттакий-то/[оттакий]adj:m:v_zna:&pron:dem:rare", tokenizer, tagger);
    TestTools.myAssert("геть-то", "геть-то/[геть]adv|геть-то/[геть]part", tokenizer, tagger);
    TestTools.myAssert("ану-бо", "ану-бо/[ану]excl|ану-бо/[ану]part", tokenizer, tagger);
    TestTools.myAssert("годі-бо", "годі-бо/[годі]predic", tokenizer, tagger);
    TestTools.myAssert("гей-но", "гей-но/[гей]excl", tokenizer, tagger);
    TestTools.myAssert("цить-но", "цить-но/[цить]excl", tokenizer, tagger);

    TestTools.myAssert("екс-партнер", "екс-партнер/[екс-партнер]noun:m:v_naz:anim", tokenizer, tagger);

    // TODO: старий -> старший
    TestTools.myAssert("Алієва-старшого", "Алієва-старшого/[Алієв-старий]noun:m:v_rod:anim:lname|Алієва-старшого/[Алієв-старий]noun:m:v_zna:anim:lname", tokenizer, tagger);

//    TestTools.myAssert("греко-уніятський", "", tokenizer, tagger);
    
    TestTools.myAssert("жило-було", "жило-було/[жити-бути]verb:past:n:imperf", tokenizer, tagger);
    TestTools.myAssert("учиш-учиш", "учиш-учиш/[учити-учити]verb:pres:s:2:imperf:v-u", tokenizer, tagger);

    TestTools.myAssert("вгору-вниз", "вгору-вниз/[вгору-вниз]adv:v-u", tokenizer, tagger);

    TestTools.myAssert("низенько-низенько", "низенько-низенько/[низенько-низенько]adv", tokenizer, tagger);
    TestTools.myAssert("такого-сякого", "такого-сякого/[такий-сякий]adj:m:v_rod:&pron:def|такого-сякого/[такий-сякий]adj:m:v_zna:&pron:def|такого-сякого/[такий-сякий]adj:n:v_rod:&pron:def", tokenizer, tagger);
    TestTools.myAssert("великий-превеликий", "великий-превеликий/[великий-превеликий]adj:m:v_naz|великий-превеликий/[великий-превеликий]adj:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("чорній-чорній", "чорній-чорній/[чорний-чорний]adj:f:v_dav|чорній-чорній/[чорний-чорний]adj:f:v_mis|чорній-чорній/[чорніти-чорніти]verb:impr:s:2:imperf", tokenizer, tagger);

    TestTools.myAssert("лікар-гомеопат", "лікар-гомеопат/[лікар-гомеопат]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("лікаря-гомеопата", "лікаря-гомеопата/[лікар-гомеопат]noun:m:v_rod:anim|лікаря-гомеопата/[лікар-гомеопат]noun:m:v_zna:anim", tokenizer, tagger);
    TestTools.myAssert("шмкр-гомеопат", "шмкр-гомеопат/[null]null", tokenizer, tagger);
    TestTools.myAssert("шмкр-ткр", "шмкр-ткр/[null]null", tokenizer, tagger);

    TestTools.myAssert("вчинок-приклад", "вчинок-приклад/[вчинок-приклад]noun:m:v_naz:v-u|вчинок-приклад/[вчинок-приклад]noun:m:v_zna:v-u", tokenizer, tagger);
    TestTools.myAssert("міста-фортеці", "міста-фортеці/[місто-фортеця]noun:n:v_rod|міста-фортеці/[місто-фортеця]noun:p:v_naz|міста-фортеці/[місто-фортеця]noun:p:v_zna", tokenizer, tagger);

    // inanim-anim
    TestTools.myAssert("вчених-новаторів", "вчених-новаторів/[вчений-новатор]noun:p:v_rod:anim:v-u|вчених-новаторів/[вчений-новатор]noun:p:v_zna:anim:v-u", tokenizer, tagger);
    TestTools.myAssert("країна-виробник", "країна-виробник/[країна-виробник]noun:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("банк-виробник", "банк-виробник/[банк-виробник]noun:m:v_naz|банк-виробник/[банк-виробник]noun:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("банки-агенти", "банки-агенти/[банк-агент]noun:p:v_naz|банки-агенти/[банк-агент]noun:p:v_zna|банки-агенти/[банка-агент]noun:p:v_naz|банки-агенти/[банка-агент]noun:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("місто-гігант", "місто-гігант/[місто-гігант]noun:n:v_naz|місто-гігант/[місто-гігант]noun:n:v_zna", tokenizer, tagger);
    TestTools.myAssert("країни-агресори", "країни-агресори/[країна-агресор]noun:p:v_naz|країни-агресори/[країна-агресор]noun:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("поселення-гігант", "поселення-гігант/[поселення-гігант]noun:n:v_naz|поселення-гігант/[поселення-гігант]noun:n:v_zna", tokenizer, tagger);
    
    TestTools.myAssert("сонях-красень", "сонях-красень/[сонях-красень]noun:m:v_naz|сонях-красень/[сонях-красень]noun:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("красень-сонях", "красень-сонях/[красень-сонях]noun:m:v_naz|красень-сонях/[красень-сонях]noun:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("депутатів-привидів", "депутатів-привидів/[депутат-привид]noun:p:v_rod:anim|депутатів-привидів/[депутат-привид]noun:p:v_zna:anim", tokenizer, tagger);
    TestTools.myAssert("дівчата-зірочки", "дівчата-зірочки/[дівча-зірочка]noun:p:v_naz:anim", tokenizer, tagger);

    TestTools.myAssert("абзац-два", "абзац-два/[абзац-два]noun:m:v_naz|абзац-два/[абзац-два]noun:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("сотні-дві", "сотні-дві/[сотня-два]noun:p:v_naz|сотні-дві/[сотня-два]noun:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("тисячею-трьома", "тисячею-трьома/[тисяча-три]noun:f:v_oru|тисячею-трьома/[тисяча-троє]noun:f:v_oru", tokenizer, tagger);

    TestTools.myAssert("одним-двома", "одним-двома/[один-два]numr:m:v_oru|одним-двома/[один-два]numr:n:v_oru|одним-двома/[один-двоє]numr:m:v_oru|одним-двома/[один-двоє]numr:n:v_oru", tokenizer, tagger);
    //TODO: бере іменник п’ята
//    TestTools.myAssert("п'яти-шести", "п'яти-шести/[п'ять-шість]numr:v_dav|п'яти-шести/[п'ять-шість]numr:v_mis|п'яти-шести/[п'ять-шість]numr:v_rod", tokenizer, tagger);
    TestTools.myAssert("п'яти-шести", "п'яти-шести/[п'ята-шість]noun:f:v_rod|п'яти-шести/[п'ять-шість]numr:p:v_dav|п'яти-шести/[п'ять-шість]numr:p:v_mis|п'яти-шести/[п'ять-шість]numr:p:v_rod", tokenizer, tagger);
    TestTools.myAssert("півтори-дві", "півтори-дві/[півтори-два]numr:f:v_naz|півтори-дві/[півтори-два]numr:f:v_zna", tokenizer, tagger);
    TestTools.myAssert("три-чотири", "три-чотири/[три-чотири]numr:p:v_naz|три-чотири/[три-чотири]numr:p:v_zna", tokenizer, tagger);
    TestTools.myAssert("два-чотири", "два-чотири/[два-чотири]numr:m:v_naz|два-чотири/[два-чотири]numr:m:v_zna|два-чотири/[два-чотири]numr:n:v_naz|два-чотири/[два-чотири]numr:n:v_zna", tokenizer, tagger);
    TestTools.myAssert("одному-двох", "одному-двох/[один-два]numr:m:v_mis|одному-двох/[один-два]numr:n:v_mis|одному-двох/[один-двоє]numr:m:v_mis|одному-двох/[один-двоє]numr:n:v_mis", tokenizer, tagger);
    // u2013
    TestTools.myAssert("три–чотири", "три–чотири/[три–чотири]numr:p:v_naz|три–чотири/[три–чотири]numr:p:v_zna", tokenizer, tagger);
    
//    "однією-єдиною"
//    TestTools.myAssert("капуджі-ага", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("Каладжі-бей", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("капудан-паша", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);
//    TestTools.myAssert("кальфа-ефенді", "два-чотири/[два-чотири]numr:v_naz|два-чотири/[два-чотири]numr:v_naz", tokenizer, tagger);

    TestTools.myAssert("а-а", "а-а/[а-а]excl", tokenizer, tagger);

    TestTools.myAssert("Москви-ріки", "Москви-ріки/[Москва-ріка]noun:f:v_rod", tokenizer, tagger);
    
    TestTools.myAssert("пів-України", "пів-України/[пів-України]noun:f:v_dav|пів-України/[пів-України]noun:f:v_mis|пів-України/[пів-України]noun:f:v_naz|пів-України/[пів-України]noun:f:v_oru|пів-України/[пів-України]noun:f:v_rod|пів-України/[пів-України]noun:f:v_zna", tokenizer, tagger);

    TestTools.myAssert("кава-еспресо", "кава-еспресо/[кава-еспресо]noun:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("кави-еспресо", "кави-еспресо/[кава-еспресо]noun:f:v_rod", tokenizer, tagger);
    TestTools.myAssert("еспресо-машина", "еспресо-машина/[еспресо-машина]noun:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("програмою-максимум", "програмою-максимум/[програма-максимум]noun:f:v_oru", tokenizer, tagger);

    TestTools.myAssert("Пенсильванія-авеню", "Пенсильванія-авеню/[Пенсильванія-авеню]noun:f:nv", tokenizer, tagger);

    TestTools.myAssert("патолого-анатомічний", "патолого-анатомічний/[патолого-анатомічний]adj:m:v_naz|патолого-анатомічний/[патолого-анатомічний]adj:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("паталого-анатомічний", "паталого-анатомічний/[null]null", tokenizer, tagger);
    //TODO: fix this case (now works like братів-православних)
//    TestTools.myAssert("патолога-анатомічний", "патолога-анатомічний/[null]null", tokenizer, tagger);
    TestTools.myAssert("патолого-гмкнх", "патолого-гмкнх/[null]null", tokenizer, tagger);
    TestTools.myAssert("патолого-голова", "патолого-голова/[null]null", tokenizer, tagger);
    //TODO: remove :compb ?
    TestTools.myAssert("освітньо-культурний", "освітньо-культурний/[освітньо-культурний]adj:m:v_naz:compb|освітньо-культурний/[освітньо-культурний]adj:m:v_zna:compb", tokenizer, tagger);
    TestTools.myAssert("бірмюково-блакитний", "бірмюково-блакитний/[null]null", tokenizer, tagger);
    TestTools.myAssert("сліпуче-яскравого", "сліпуче-яскравого/[сліпуче-яскравий]adj:m:v_rod:compb|сліпуче-яскравого/[сліпуче-яскравий]adj:m:v_zna:compb|сліпуче-яскравого/[сліпуче-яскравий]adj:n:v_rod:compb", tokenizer, tagger);
    TestTools.myAssert("дво-триметровий", "дво-триметровий/[дво-триметровий]adj:m:v_naz|дво-триметровий/[дво-триметровий]adj:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("україно-болгарський", "україно-болгарський/[україно-болгарський]adj:m:v_naz|україно-болгарський/[україно-болгарський]adj:m:v_zna", tokenizer, tagger);

//    TestTools.myAssert("американо-блакитний", "бірмюково-блакитний/[бірмюково-блакитний]adj:m:v_naz|бірмюково-блакитний/[бірмюково-блакитний]adj:m:v_zna", tokenizer, tagger);

    TestTools.myAssert("Дівчинка-першокласниця", "Дівчинка-першокласниця/[дівчинка-першокласниця]noun:f:v_naz:anim", tokenizer, tagger);

    TestTools.myAssert("RPM-пакунок", "RPM-пакунок/[RPM-пакунок]noun:m:v_naz|RPM-пакунок/[RPM-пакунок]noun:m:v_zna", tokenizer, tagger);

    // істота-неістота
    //TODO:
    // про місяця-місяченька
    // бабці-Австрії
    // змагання зі слалому-гіганту
    // голосувати за Тимошенко-прем’єра
  }

}
