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
      "300/[300]number -- р/[р]unknown:abbr -- до/[до]noun:n:v_dav:nv|до/[до]noun:n:v_mis:nv|до/[до]noun:n:v_naz:nv|до/[до]noun:n:v_oru:nv|до/[до]noun:n:v_rod:nv|до/[до]noun:n:v_zna:nv|до/[до]noun:p:v_dav:nv|до/[до]noun:p:v_mis:nv|до/[до]noun:p:v_naz:nv|до/[до]noun:p:v_oru:nv|до/[до]noun:p:v_rod:nv|до/[до]noun:p:v_zna:nv|до/[до]prep:rv_rod -- н/[null]null -- е/[е]excl",
       tokenizer, tagger);

    TestTools.myAssert("101,234", "101,234/[101,234]number", tokenizer, tagger);
    TestTools.myAssert("3,5-5,6% 7° 7,4°С", "3,5-5,6%/[3,5-5,6%]number -- 7°/[7°]number -- 7,4°С/[7,4°С]number", tokenizer, tagger);
    TestTools.myAssert("XIX", "XIX/[XIX]number", tokenizer, tagger);

    TestTools.myAssert("14.07.2001", "14.07.2001/[14.07.2001]date", tokenizer, tagger);

    // one-way case sensitivity
    TestTools.myAssert("києві", "києві/[кий]noun:m:v_dav", tokenizer, tagger);
    TestTools.myAssert("Києві", "Києві/[Київ]noun:m:v_mis|Києві/[кий]noun:m:v_dav", tokenizer, tagger);
    TestTools.myAssert("віл", "віл/[віл]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("Віл", "Віл/[віл]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("ВІЛ", "ВІЛ/[ВІЛ]noun:m:v_dav:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_mis:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_naz:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_oru:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_rod:nv:np:abbr|ВІЛ/[ВІЛ]noun:m:v_zna:nv:np:abbr|ВІЛ/[віл]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("далі", "далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Далі", "Далі/[Даль]noun:m:v_mis:anim|Далі/[Далі]noun:m:v_dav:nv:np|Далі/[Далі]noun:m:v_mis:nv:np|Далі/[Далі]noun:m:v_naz:nv:np|Далі/[Далі]noun:m:v_oru:nv:np|Далі/[Далі]noun:m:v_rod:nv:np|Далі/[Далі]noun:m:v_zna:nv:np|Далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Бен", "Бен/[Бен]noun:m:v_naz:anim|Бен/[бен]unknown", tokenizer, tagger);
    TestTools.myAssert("бен", "бен/[бен]unknown", tokenizer, tagger);


    TestTools.myAssert("Справу порушено судом", 
      "Справу/[справа]noun:f:v_zna -- порушено/[порушити]verb:impers:perf -- судом/[суд]noun:m:v_oru|судом/[судома]noun:p:v_rod",
       tokenizer, tagger);
       
    String expected = 
      "Майже/[майже]adv -- два/[два]numr:m:v_naz|два/[два]numr:m:v_zna|два/[два]numr:n:v_naz|два/[два]numr:n:v_zna -- роки/[рік]noun:p:v_naz|роки/[рік]noun:p:v_zna"
    + " -- тому/[той]pron:m:v_dav|тому/[той]pron:m:v_mis|тому/[той]pron:n:v_dav|тому/[той]pron:n:v_mis|тому/[том]noun:m:v_dav|тому/[том]noun:m:v_mis|тому/[том]noun:m:v_rod"
    + " -- Люба/[Люба]noun:f:v_naz:anim|Люба/[любий]adj:f:v_naz -- разом/[раз]noun:m:v_oru|разом/[разом]adv -- із/[із]prep:rv_rod:rv_zna:rv_oru"
    + " -- чоловіком/[чоловік]noun:m:v_oru:anim -- Степаном/[Степан]noun:m:v_oru:anim -- виїхали/[виїхати]verb:past:m:perf -- туди/[туди]adv"
    + " -- на/[на]excl|на/[на]part|на/[на]prep:rv_zna:rv_mis -- "
    + "проживання/[проживання]noun:n:v_naz|проживання/[проживання]noun:n:v_rod|проживання/[проживання]noun:n:v_zna|проживання/[проживання]noun:p:v_naz|проживання/[проживання]noun:p:v_zna";
  
    TestTools.myAssert("Майже два роки тому Люба разом із чоловіком Степаном виїхали туди на проживання.",
        expected, tokenizer, tagger);
  }
  
  public void testDynamicTagging() throws IOException {
    TestTools.myAssert("по-свинячому", "по-свинячому/[по-свинячому]adv", tokenizer, tagger);
    TestTools.myAssert("по-сибірськи", "по-сибірськи/[по-сибірськи]adv", tokenizer, tagger);

    TestTools.myAssert("давай-но", "давай-но/[давати]verb:impr:s:2:imperf", tokenizer, tagger);
    TestTools.myAssert("дивіться-но", "дивіться-но/[дивитися]verb:rev:impr:p:2:imperf", tokenizer, tagger);

    TestTools.myAssert("100-річному", "100-річному/[100-річний]adj:m:v_dav|100-річному/[100-річний]adj:m:v_mis|100-річному/[100-річний]adj:n:v_dav|100-річному/[100-річний]adj:n:v_mis", tokenizer, tagger);
    TestTools.myAssert("100-й", "100-й/[100-й]adj:m:v_naz|100-й/[100-й]adj:m:v_zna", tokenizer, tagger);

    TestTools.myAssert("той-таки", "той-таки/[той-таки]pron:m:v_naz|той-таки/[той-таки]pron:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("буде-таки", "буде-таки/[бути]verb:pres:s:3:imperf", tokenizer, tagger);

    TestTools.myAssert("екс-партнер", "екс-партнер/[екс-партнер]noun:m:v_naz:anim", tokenizer, tagger);

    // TODO: старий -> старший
    TestTools.myAssert("Алієва-старшого", "Алієва-старшого/[Алієв-старий]noun:m:v_rod:anim|Алієва-старшого/[Алієв-старий]noun:m:v_zna:anim", tokenizer, tagger);

    TestTools.myAssert("абзац-два", "абзац-два/[абзац-два]noun:m:v_naz|абзац-два/[абзац-два]noun:m:v_zna", tokenizer, tagger);

//    TestTools.myAssert("жило-було", "низенько-низенько/[низенько-низенько]adv", tokenizer, tagger);

    TestTools.myAssert("низенько-низенько", "низенько-низенько/[низенько-низенько]adv", tokenizer, tagger);
    TestTools.myAssert("такого-сякого", "такого-сякого/[такий-сякий]adj:m:v_rod:&pron|такого-сякого/[такий-сякий]adj:m:v_zna:&pron|такого-сякого/[такий-сякий]adj:n:v_rod:&pron", tokenizer, tagger);

    TestTools.myAssert("лікар-гомеопат", "лікар-гомеопат/[лікар-гомеопат]noun:m:v_naz:anim", tokenizer, tagger);
    TestTools.myAssert("лікаря-гомеопата", "лікаря-гомеопата/[лікар-гомеопат]noun:m:v_rod:anim|лікаря-гомеопата/[лікар-гомеопат]noun:m:v_zna:anim", tokenizer, tagger);
    TestTools.myAssert("шмкр-гомеопат", "шмкр-гомеопат/[null]null", tokenizer, tagger);
    TestTools.myAssert("шмкр-ткр", "шмкр-ткр/[null]null", tokenizer, tagger);

    TestTools.myAssert("п'яти-шести", "п'яти-шести/[п'ять-шість]numr:v_dav|п'яти-шести/[п'ять-шість]numr:v_mis|п'яти-шести/[п'ять-шість]numr:v_rod", tokenizer, tagger);

    TestTools.myAssert("пів-України", "пів-України/[пів-України]noun:f:v_dav|пів-України/[пів-України]noun:f:v_mis|пів-України/[пів-України]noun:f:v_naz|пів-України/[пів-України]noun:f:v_oru|пів-України/[пів-України]noun:f:v_rod|пів-України/[пів-України]noun:f:v_zna", tokenizer, tagger);

    TestTools.myAssert("кава-еспресо", "кава-еспресо/[кава-еспресо]noun:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("кави-еспресо", "кави-еспресо/[кава-еспресо]noun:f:v_rod", tokenizer, tagger);
    TestTools.myAssert("еспресо-машина", "еспресо-машина/[еспресо-машина]noun:f:v_naz", tokenizer, tagger);
    TestTools.myAssert("програмою-максимум", "програмою-максимум/[програма-максимум]noun:f:v_oru", tokenizer, tagger);

    TestTools.myAssert("міста-фортеці", "міста-фортеці/[місто-фортеця]noun:n:v_rod|міста-фортеці/[місто-фортеця]noun:p:v_naz|міста-фортеці/[місто-фортеця]noun:p:v_zna", tokenizer, tagger);

    TestTools.myAssert("Пенсильванія-авеню", "Пенсильванія-авеню/[Пенсильванія-авеню]noun:f:nv", tokenizer, tagger);
//    TestTools.myAssert("Кубань-ріки", "Кубань-ріки/[Кубань-ріка]noun:f:v_rod", tokenizer, tagger);

    TestTools.myAssert("паталого-анатомічний", "паталого-анатомічний/[паталого-анатомічний]adj:m:v_naz|паталого-анатомічний/[паталого-анатомічний]adj:m:v_zna", tokenizer, tagger);
    TestTools.myAssert("паталого-гмкнх", "паталого-гмкнх/[null]null", tokenizer, tagger);
    TestTools.myAssert("паталого-голова", "паталого-голова/[null]null", tokenizer, tagger);
    TestTools.myAssert("паталога-анатомічний", "паталога-анатомічний/[null]null", tokenizer, tagger);
    TestTools.myAssert("бірмюково-блакитний", "бірмюково-блакитний/[бірмюково-блакитний]adj:m:v_naz|бірмюково-блакитний/[бірмюково-блакитний]adj:m:v_zna", tokenizer, tagger);

    TestTools.myAssert("Дівчинка-першокласниця", "Дівчинка-першокласниця/[дівчинка-першокласниця]noun:f:v_naz:anim", tokenizer, tagger);

    // істота-неістота, неістота-істота
//    TestTools.myAssert("бабці-Австрії", "бабці-Австрії/[null]null", tokenizer, tagger);

//    TestTools.myAssert("красень-гол", "красень-гол/[красень-гол]noun:m:v_naz|красень-гол/[красень-гол]noun:m:v_zna", tokenizer, tagger);
//    TestTools.myAssert("місто-побратим", "місто-побратим/[місто-побратим]noun:n:v_naz|місто-побратим/[місто-побратим]noun:n:v_zna", tokenizer, tagger);
//    TestTools.myAssert("країни-господарі", "країни-господарі/[країна-господар]noun:p:v_naz|країни-господарі/[країна-господар]noun:p:v_zna", tokenizer, tagger);
//    TestTools.myAssert("країну-господар", "країну-господар/[null]null", tokenizer, tagger);
//    TestTools.myAssert("депутатів-привидів", "депутатів-привидів/[депутат-привид]noun:p:v_rod:anim|депутатів-привидів/[депутат-привид]noun:p:v_zna:anim", tokenizer, tagger);
    
    // про місяця-місяченька
    // бабці-Австрії
    // про рослин-людожерів, про соняха-гіганта
    // змагання зі слалому-гіганту
    // голосувати за Тимошенко-прем’єра
    // рок-дідусів, інтернет-глядачів, флеш-інтерв’ю
  }

}
