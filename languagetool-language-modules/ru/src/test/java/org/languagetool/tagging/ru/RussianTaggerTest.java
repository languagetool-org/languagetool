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
package org.languagetool.tagging.ru;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Russian;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class RussianTaggerTest {

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(RussianTagger.INSTANCE, new Russian());
  }

  @Test
  public void testTagger() throws IOException {
    WordTokenizer tokenizer = new WordTokenizer();
    RussianTagger tagger = RussianTagger.INSTANCE;
    TestTools.myAssert("Все счастливые семьи похожи друг на друга,  каждая  несчастливая  семья несчастлива по-своему.",
        "Все/[весь]ADJ:MPR:PL:Nom|Все/[весь]ADJ:MPR:PL:V|Все/[все]PNN:PL:Nom|Все/[все]PNN:PL:V|Все/[все]PNN:Sin:Nom|Все/[все]PNN:Sin:V -- счастливые/[счастливый]ADJ:Posit:PL:Nom|счастливые/[счастливый]ADJ:Posit:PL:V -- семьи/[семья]NN:Inanim:Fem:PL:Nom|семьи/[семья]NN:Inanim:Fem:PL:V|семьи/[семья]NN:Inanim:Fem:Sin:R -- похожи/[похожий]ADJ:Short:PL -- друг/[друг]NN:Anim:Masc:Sin:Nom -- на/[на]PREP -- друга/[друг]NN:Anim:Masc:Sin:R|друга/[друг]NN:Anim:Masc:Sin:V -- каждая/[каждый]ADJ:MPR:Fem:Nom -- несчастливая/[несчастливый]ADJ:Posit:Fem:Nom -- семья/[семья]NN:Inanim:Fem:Sin:Nom -- несчастлива/[несчастливый]ADJ:Short:Fem -- по-своему/[по-своему]ADV", tokenizer, tagger);
    TestTools.myAssert("Все смешалось в доме Облонских.",
        "Все/[весь]ADJ:MPR:PL:Nom|Все/[весь]ADJ:MPR:PL:V|Все/[все]PNN:PL:Nom|Все/[все]PNN:PL:V|Все/[все]PNN:Sin:Nom|Все/[все]PNN:Sin:V -- смешалось/[смешаться]VB:Past:INTR:PFV:Neut -- в/[в]PREP -- доме/[дом]NN:Inanim:Masc:Sin:P -- Облонских/[null]null", tokenizer, tagger);
    TestTools.myAssert("Абдуллаевы",
        "Абдуллаевы/[абдуллаев]NN:Fam:PL:Nom", tokenizer, tagger);
    TestTools.myAssert("блукать",
        "блукать/[блукать]VB:INF:", tokenizer, tagger);

  }

}
