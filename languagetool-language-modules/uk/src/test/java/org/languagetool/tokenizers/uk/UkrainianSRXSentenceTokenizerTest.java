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

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class UkrainianSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Ukrainian());

  @Test
  public final void testTokenize() {
    testSplit("Це просте речення.");
    testSplit("Вони приїхали в Париж. ", "Але там їм геть не сподобалося.");
    testSplit("Панк-рок — напрям у рок-музиці, що виник у середині 1970-х рр. у США і Великобританії.");
    testSplit("Разом із втечами, вже у XV ст. почастішали збройні виступи селян.");
    testSplit("На початок 1994 р. державний борг України становив 4,8 млрд. дол.");
    testSplit("Київ, вул. Сагайдачного, буд. 43, кв. 4.");

    testSplit("Є.Бакуліна");
    testSplit("Є.В.Бакуліна");
    testSplit("Засідав І. П. Єрмолюк.");
    testSplit("І. П. Єрмолюк скликав нараду.");
    testSplit("Наша зустріч з А. Марчуком і Г. В. Тріскою відбулася в грудні минулого року.");
    testSplit("Наша зустріч з А.Марчуком і М.В.Хвилею відбулася в грудні минулого року.");
    testSplit("Комендант преподобний С.\u00A0Мокітімі");
    testSplit("Комендант преподобний С.\u00A0С.\u00A0Мокітімі 1.");
    testSplit("Комендант преподобний С.\u00A0С. Мокітімі 2.");
    testSplit("Склад: акад. Вернадський, проф. Харченко, доц. Семеняк.");

    testSplit("Опергрупа приїхала в с. Лісове.");
    testSplit("300 р. до н. е.");
    testSplit("З 300 р. до н.е., і по цей день.");
    testSplit("Пролісок (рос. пролесок) — маленька квітка.");
    testSplit("Квітка Цісик (англ. Kvitka Cisyk також Kacey Cisyk від ініціалів К.С.); 4 квітня 1953р., Квінз, Нью-Йорк — 29 березня 1998 р., Мангеттен, Нью-Йорк) — американська співачка українського походження.");
    testSplit("До Інституту ім. Глієра під'їжджає чорне авто."); 
    testSplit("До Інституту ім. акад. Вернадського."); 
    testSplit("До вулиці гетьмана Скоропадського під'їжджає чорне авто."); 
    testSplit("До табору «Артек».");
    testSplit("Спільні пральні й т. д.");
    testSplit("Спільні пральні й т. д. й т. п.");
    testSplit("див. стор. 24.");
    testSplit("Від англ.\n  File.");
    testSplit("Від фр.  \nparachute.");
    testSplit("В цих світлих просторих апартаментах...  м’які крісла, килими, дорогі статуетки");
    testSplit("(вони самі це визнали. - Ред.)");
    testSplit("Всього 33 тис. 356 особи");
    testSplit("Всього 33 тис. (за словами прораба)");
    testSplit("з яких приблизно   1,2 тис. – чоловіки.");
    testSplit("У с. Вижва");
    testSplit("Книжка (с. 200)");
    testSplit("позначені: «с. Вижва»");
    testSplit("Микола Васюк (с. Корнієнки, Полтавська обл.)");
    testSplit("U.S. Marine");
    testSplit("B.B. King");
    testSplit("Церква Св. Духа і церква св. Духа");
    testSplit("Валерій (міліціонер-пародист.  –  Авт.) стане пародистом.");
    testSplit("Сьогодні (у четвер.  - Ред.), вранці.");
    testSplit(" ([27]див. Тиждень № 9, 2008)");
  }

  @Test
  public void testTokenizeWithSplit() {
    testSplit("Всього 33 тис.", "А можей й більше");
    testSplit("Їх було 7,5 млн.", "В кожного була сорочка.");
    testSplit("Довжиною 30 с. ", "Поїхали.");
    testSplit("Швидкістю 30 м/с. ", "Поїхали.");
    testSplit("Останні 100 м. ", "І тут все пропало.");
    testSplit("Корисна площа 67 тис. кв.  м. ", "У 1954 році над Держпромом...");
    testSplit("На 0,6°C. ", "Але ми все маємо."); //лат С 
    testSplit("На 0,6°С. ", "Але ми все маємо."); //укр С
    testSplit("На 0,6 °C. ", "Але ми все маємо."); //лат С 
    testSplit("На 0,6 °С. ", "Але ми все маємо."); //укр С
    testSplit("Приїхав у США. ", "Проте на другий рік.");
    testSplit("Маємо страшне диво з див. ", "І кращого варіанту немає.");
    testSplit("Взяти бодай XIII—XIX ст.", "Раніше п’єса була домінантою.");
  }
  
  @Test
  public void testTokenizeWithSpecialChars() {
    testSplit("відбув у тюрмах.\u202fНещодавно письменник");
    testSplit("закрито бібліотеку української літератури.\u202f ", "Раніше відділ боротьби з екстремізмом...");
    // still no split for initials
    testSplit("З особливим обуренням сприймав С.\u202f Шелухин легітимізацію");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
