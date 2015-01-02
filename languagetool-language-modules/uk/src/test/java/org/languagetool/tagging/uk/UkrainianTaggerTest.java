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
      "300/[300]number -- р/[р]unknown:abbr -- до/[до]noun:n:nv|до/[до]prep:rv_rod -- н/[null]null -- е/[е]excl",
       tokenizer, tagger);

    TestTools.myAssert("101,234", "101,234/[101,234]number", tokenizer, tagger);
    TestTools.myAssert("3,5-5,6% 7° 7,4°С", "3,5-5,6%/[3,5-5,6%]number -- 7°/[7°]number -- 7,4°С/[7,4°С]number", tokenizer, tagger);

    TestTools.myAssert("14.07.2001", "14.07.2001/[14.07.2001]date", tokenizer, tagger);

    // one-way case sensitivity
    TestTools.myAssert("києві", "києві/[кий]noun:m:v_dav", tokenizer, tagger);
    TestTools.myAssert("Києві", "Києві/[Київ]noun:m:v_mis|Києві/[кий]noun:m:v_dav", tokenizer, tagger);
    TestTools.myAssert("віл", "віл/[віл]noun:m:v_naz:ist", tokenizer, tagger);
    TestTools.myAssert("Віл", "Віл/[віл]noun:m:v_naz:ist", tokenizer, tagger);
    TestTools.myAssert("ВІЛ", "ВІЛ/[ВІЛ]noun:m:nv:abbr|ВІЛ/[віл]noun:m:v_naz:ist", tokenizer, tagger);
    TestTools.myAssert("далі", "далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Далі", "Далі/[Даль]noun:m:v_mis:ist|Далі/[Далі]noun:m:nv|Далі/[далі]adv", tokenizer, tagger);
    TestTools.myAssert("Бен", "Бен/[Бен]noun:m:v_naz:ist|Бен/[бен]unknown", tokenizer, tagger);
    TestTools.myAssert("бен", "бен/[бен]unknown", tokenizer, tagger);


    TestTools.myAssert("Справу порушено судом", 
      "Справу/[справа]noun:f:v_zna -- порушено/[порушити]verb:impers -- судом/[суд]noun:m:v_oru|судом/[судома]noun:p:v_rod",
       tokenizer, tagger);
       
    String expected = 
      "Майже/[майже]adv -- два/[два]numr:m:v_naz|два/[два]numr:m:v_zna|два/[два]numr:n:v_naz|два/[два]numr:n:v_zna -- роки/[рік]noun:p:v_naz|роки/[рік]noun:p:v_zna"
    + " -- тому/[той]pron:m:v_dav|тому/[той]pron:m:v_mis|тому/[той]pron:n:v_dav|тому/[той]pron:n:v_mis|тому/[том]noun:m:v_dav|тому/[том]noun:m:v_mis|тому/[том]noun:m:v_rod"
    + " -- Люба/[Люба]noun:f:v_naz:ist|Люба/[любий]adj:f:v_naz -- разом/[раз]noun:m:v_oru|разом/[разом]adv -- із/[із]prep:rv_rod:rv_zna:rv_oru"
    + " -- чоловіком/[чоловік]noun:m:v_oru:ist -- Степаном/[Степан]noun:m:v_oru:ist -- виїхали/[виїхати]verb:past:m:perf -- туди/[туди]adv"
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

    TestTools.myAssert("екс-партнер", "екс-партнер/[екс-партнер]noun:m:v_naz:ist", tokenizer, tagger);

    TestTools.myAssert("низенько-низенько", "низенько-низенько/[низенько-низенько]adv", tokenizer, tagger);
    TestTools.myAssert("такого-сякого", "такого-сякого/[такий-сякий]adj:m:v_rod:&pron|такого-сякого/[такий-сякий]adj:m:v_zna:&pron|такого-сякого/[такий-сякий]adj:n:v_rod:&pron", tokenizer, tagger);

    TestTools.myAssert("лікар-гомеопат", "лікар-гомеопат/[лікар-гомеопат]noun:m:v_naz:ist", tokenizer, tagger);
    TestTools.myAssert("лікаря-гомеопата", "лікаря-гомеопата/[лікар-гомеопат]noun:m:v_rod:ist|лікаря-гомеопата/[лікар-гомеопат]noun:m:v_zna:ist", tokenizer, tagger);
    TestTools.myAssert("шмкр-гомеопат", "шмкр-гомеопат/[null]null", tokenizer, tagger);
    TestTools.myAssert("шмкр-ткр", "шмкр-ткр/[null]null", tokenizer, tagger);

    TestTools.myAssert("п'яти-шести", "п'яти-шести/[п'ять-шість]numr:v_dav|п'яти-шести/[п'ять-шість]numr:v_mis|п'яти-шести/[п'ять-шість]numr:v_rod", tokenizer, tagger);

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

    // істота-неістота, неістота-істота
//    TestTools.myAssert("бабці-Австрії", "бабці-Австрії/[null]null", tokenizer, tagger);

//    TestTools.myAssert("красень-гол", "красень-гол/[красень-гол]noun:m:v_naz|красень-гол/[красень-гол]noun:m:v_zna", tokenizer, tagger);
//    TestTools.myAssert("місто-побратим", "місто-побратим/[місто-побратим]noun:n:v_naz|місто-побратим/[місто-побратим]noun:n:v_zna", tokenizer, tagger);
//    TestTools.myAssert("країни-господарі", "країни-господарі/[країна-господар]noun:p:v_naz|країни-господарі/[країна-господар]noun:p:v_zna", tokenizer, tagger);
//    TestTools.myAssert("країну-господар", "країну-господар/[null]null", tokenizer, tagger);
//    TestTools.myAssert("депутатів-привидів", "депутатів-привидів/[депутат-привид]noun:p:v_rod:ist|депутатів-привидів/[депутат-привид]noun:p:v_zna:ist", tokenizer, tagger);
    
    // про місяця-місяченька
    // бабці-Австрії
    // про рослин-людожерів, про соняха-гіганта
    // змагання зі слалому-гіганту
    // голосувати за Тимошенко-прем’єра
    // рок-дідусів, інтернет-глядачів, флеш-інтерв’ю
  }

}
