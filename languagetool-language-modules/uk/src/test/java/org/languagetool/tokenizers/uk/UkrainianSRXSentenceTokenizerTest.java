/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tokenizers.uk;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class UkrainianSRXSentenceTokenizerTest extends TestCase {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Ukrainian());

  public final void testTokenize() {
    testSplit("Це просте речення.");
    testSplit("Вони приїхали в Париж. ", "Але там їм геть не сподобалося.");
    testSplit("Панк-рок — напрям у рок-музиці, що виник у середині 1970-х рр. у США і Великобританії.");
    testSplit("Разом із втечами, вже у XV ст. почастішали збройні виступи селян.");
    testSplit("На початок 1994 р. державний борг України становив 4,8 млрд. дол.");
    testSplit("Київ, вул. Сагайдачного, буд. 43, кв. 4.");
    testSplit("Наша зустріч з А. Марчуком і Г. В. Тріскою відбулася в грудні минулого року.");
    testSplit("Наша зустріч з А.Марчуком і М.В.Хвилею відбулася в грудні минулого року.");
    testSplit("Комендант преподобний С.\u00A0Мокітімі");
    testSplit("Комендант преподобний С.\u00A0С.\u00A0Мокітімі 1.");
    testSplit("Комендант преподобний С.\u00A0С. Мокітімі 2.");
    testSplit("Склад: акад. Вернадський, проф. Харченко, доц. Семеняк.");
    testSplit("Опергрупа приїхала в с. Лісове.");
    testSplit("300 р. до н. е.");
    testSplit("Пролісок (рос. пролесок) — маленька квітка.");
    testSplit("Квітка Цісик (англ. Kvitka Cisyk також Kacey Cisyk від ініціалів К.С.); 4 квітня 1953р., Квінз, Нью-Йорк — 29 березня 1998 р., Мангеттен, Нью-Йорк) — американська співачка українського походження.");
    testSplit("До Інституту ім. Глієра під'їжджає чорне авто."); 
    testSplit("До табору «Артек».");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
