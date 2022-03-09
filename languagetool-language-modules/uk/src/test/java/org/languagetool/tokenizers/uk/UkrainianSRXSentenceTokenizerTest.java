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
    testSplit("На початок 1994 р. державний борг України становив 4,8 млрд. дол. США");
    testSplit("4,8 млрд. дол. США. ", "Але наступного року...");
    testSplit("Київ, вул. Сагайдачного, буд. 43, кв. 4.");
    testSplit("на вул.\n  Сагайдачного.");

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
    testSplit("Ів. Франко.");
    testSplit("Алисов Н. В. , Хореев Б. С.");
    testSplit("і Г.-К. Андерсена");
    testSplit(" — К. : Наук. думка, 1990.");
    testSplit("Маркс К. «Показова держава»");
    
    // latin I
    testSplit("М. Л. Гончарука, I. О. Денисюка");
    testSplit("I. I. Дорошенко");

    testSplit("елементів множини A. Отже, нехай");
    
    testSplit("Опергрупа приїхала в с. Лісове.");
    testSplit("300 р. до н. е.");
    testSplit("З 300 р. до н.е., і по цей день.");
    testSplit("Пролісок (рос. пролесок) — маленька квітка.");
    testSplit("Квітка Цісик (англ. Kvitka Cisyk також Kacey Cisyk від ініціалів К.С.); 4 квітня 1953р., Квінз, Нью-Йорк — 29 березня 1998 р., Мангеттен, Нью-Йорк) — американська співачка українського походження.");
    testSplit("До Інституту ім. Глієра під'їжджає чорне авто."); 
    testSplit("До Інституту ім. акад. Вернадського."); 
    testSplit("До вулиці гетьмана Скоропадського під'їжджає чорне авто."); 
    testSplit("До табору «Артек».");
//    testSplit("Спільні пральні й т. д.", "Перемогли!");
    testSplit("Спільні пральні й т. д. й т. п. ", "Перемогли!");
    testSplit("в Хоролі з п. Кушніренком договорилися");
    testSplit("і п. 10 від 23.1.33 р.");
    testSplit("і т. п. ", "10 від 23.1.33 р.");
    testSplit("і т.п. ", "10 від 23.1.33 р.");
    testSplit("див. стор. 24.");
    testSplit("Від англ.\n  File.");
    testSplit("Від фр.  \nparachute.");
    testSplit("фільму\nС. Ейзенштейна");

    testSplit("Від р. Дніпро.");
    testSplit("В 1941 р. Конрад Цузе побудував.");
    testSplit("Наприкінці 1254 р. Данило почав");
    testSplit("У травні 1949 р. Грушківський район");
    testSplit("У травні 1949 р. \nГрушківський район");
    testSplit("Упродовж 2011–2014 р. Швейцарія надасть");
    testSplit("15 вересня 1995 р. Україною було підписано");
    testSplit("Але закінчилося аж у січні 2013 р. ", "Як бачимо");

    testSplit("інкримінують ч. 1 ст. 11");

    testSplit("В цих світлих просторих апартаментах...  м’які крісла, килими, дорогі статуетки");
    testSplit("А та — навперейми... «давайте мені!»");
    testSplit("слугував ...    «витяг з протоколу зустрічі");
    testSplit("на... Луганському");
    testSplit("(вони самі це визнали. - Ред.)");

    testSplit("Всього 33 тис. 356 особи");
    testSplit("Всього 33 тис. (за словами прораба)");
    testSplit("з яких приблизно   1,2 тис. – чоловіки.");
    testSplit("У с. Вижва");
    testSplit("Книжка (с. 200)");
    testSplit("позначені: «с. Вижва»");
    testSplit("в м.Києві");
    testSplit("Микола Васюк (с. Корнієнки, Полтавська обл.)");
    testSplit("U.S. Marine");
    testSplit("B.B. King");
    testSplit("Церква Св. Духа і церква св. Духа");
    testSplit("Валерій (міліціонер-пародист.  –  Авт.) стане пародистом.");
    testSplit("Сьогодні (у четвер.  - Ред.), вранці.");
    testSplit(" ([27]див. Тиждень № 9, 2008)");

    testSplit("і «Р. Б. К.»");
    testSplit("У. Т: ");
    testSplit("Іван Ч. (1914 р. н.)");
    testSplit("альбом “Сніжність” (2006 р.) – разом із Юрієм");
    testSplit("СК “Слон” (2008 р.) ", "У минулому харків’янка");

    testSplit("рис. 14, Мал. 5; Арт. 88-99");
  }

  @Test
  public void testTokenizeWithSplit() {
    testSplit("Всього 33 тис.", "А можей й більше");
    testSplit("Їх було 7,5 млн.", "В кожного була сорочка.");
    testSplit("Довжиною 30 с. ", "Поїхали.");
    testSplit("Швидкістю 30 м/с. ", "Поїхали.");
    testSplit("до 0,64 г/куб. дм. ", "Найчистіша");
    testSplit("Останні 100 м. ", "І тут все пропало.");
    testSplit("всього 20 м. ", "Почалося");
    testSplit("Корисна площа 67 тис. кв.  м. ", "У 1954 році над Держпромом...");
    testSplit("На 0,6°C. ", "Проте ми все маємо."); //лат С 
    testSplit("На 0,6°С. ", "Проте ми все маємо."); //укр С
    testSplit("На 0,6 °C. ", "Проте ми все маємо."); //лат С 
    testSplit("На 0,6 °С. ", "Проте ми все маємо."); //укр С
    testSplit("Приїхав у США. ", "Проте на другий рік.");
    testSplit("Маємо страшне диво з див. ", "І кращого варіанту немає.");
    testSplit("Взяти бодай XIII—XIX ст.", "Раніше п’єса була домінантою.");
    testSplit("...Після перемоги у кваліфікації у стрільбі");
    testSplit("...Somebody else");
    testSplit("доволі стабільну стрільбу. ", "...Після перемоги у кваліфікації у стрільбі");
    testSplit("доволі стабільну стрільбу\n ", "...Після перемоги у кваліфікації у стрільбі");
    //testSplit("Категорії А. ", "Від якої");
    testSplit("Лі Куан Ю. ", "Наприклад");
    testSplit("король Георг V. ", "А нині");
    testSplit("цар Петро I. ", "Він ухвалив");
    testSplit("група В. ", "Усі віруси");
  }

  @Test
  public void testTokenizeWithSpecialChars() {
    testSplit("– С.\u202f5-7.");
    // still no split for initials
    testSplit("товариш С.\u202fОхримович.");
    testSplit("З особливим обуренням сприймав С.\u202f Шелухин легітимізацію");
    testSplit("відбув у тюрмах.\u202f", "Нещодавно письменник");
    testSplit("закрито бібліотеку української літератури.\u202f ", "Раніше відділ боротьби з екстремізмом...");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
