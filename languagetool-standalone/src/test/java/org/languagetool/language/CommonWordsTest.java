/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CommonWordsTest {

  @Test
  public void test() throws IOException {
    Language de = Languages.getLanguageForShortCode("de");
    Language en = Languages.getLanguageForShortCode("en");
    Language es = Languages.getLanguageForShortCode("es");
    Language pt = Languages.getLanguageForShortCode("pt");
    Language ca = Languages.getLanguageForShortCode("ca");
    CommonWords cw = new CommonWords();

    Map<Language, Integer> res1 = cw.getKnownWordsPerLanguage("Das ist bequem");
    assertNull(res1.get(en));
    assertThat(res1.get(de), is(2));

    Map<Language, Integer> res2 = cw.getKnownWordsPerLanguage("Das ist bequem ");
    assertNull(res2.get(en));
    assertThat(res2.get(de), is(3));

    Map<Language, Integer> res3 = cw.getKnownWordsPerLanguage("bequem");
    assertNull(res3.get(en));
    assertThat(res3.get(de), is(1));

    Map<Language, Integer> res4 = cw.getKnownWordsPerLanguage("this is a test");
    assertThat(res4.get(en), is(3));
    assertThat(res4.get(de), is(1));
    
    Map<Language, Integer> res5 = cw.getKnownWordsPerLanguage("Ideábamos una declaracion con el.");
    assertThat(res5.get(es), is(5));
    
    Map<Language, Integer> res6 = cw.getKnownWordsPerLanguage("Ideábamos una declaracion con el; desassigna mencions.");
    assertThat(res6.get(es), is(3));
    
    Map<Language, Integer> res7 = cw.getKnownWordsPerLanguage("Estagiário de desenvolvedor 'web' ou relacionados a programador.");
    assertThat(res7.get(es), is(3));
    assertThat(res7.get(pt), is(4));
    assertThat(res7.get(ca), is(2));

    Map<Language, Integer> res8 = cw.getKnownWordsPerLanguage("Autohaus-Wirklichkeit");  // "Wirklichkeit" is in common_words.txt
    assertNull(res8.get(en));
    assertThat(res8.get(de), is(1));

    Map<Language, Integer> res9 = cw.getKnownWordsPerLanguage("Costum de certes cultures que imposa a un pare l’adopció d’un comportament idèntic al de la mare en el període anterior o posterior al part");
    assertThat(res9.get(ca), is(20));
    assertThat(res9.get(es), is(10));
  }

}
