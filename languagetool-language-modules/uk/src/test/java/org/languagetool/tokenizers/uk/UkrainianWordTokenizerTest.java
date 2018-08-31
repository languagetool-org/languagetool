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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UkrainianWordTokenizerTest {
  private final UkrainianWordTokenizer w = new UkrainianWordTokenizer();

  @Test
  public void testTokenizeUrl() {
    String url = "http://youtube.com:80/herewego?start=11&quality=high%3F";
    List<String> testList = w.tokenize(url);
    assertEquals(Arrays.asList(url), testList);
  }

  @Test
  public void testNumbers() {
    List<String> testList = w.tokenize("300 грн на балансі");
    assertEquals(Arrays.asList("300", " ", "грн", " ", "на", " ", "балансі"), testList);

    testList = w.tokenize("надійшло 2,2 мільйона");
    assertEquals(Arrays.asList("надійшло", " ", "2,2", " ", "мільйона"), testList);

    testList = w.tokenize("надійшло 84,46 мільйона");
    assertEquals(Arrays.asList("надійшло", " ", "84,46", " ", "мільйона"), testList);

    //TODO:
//    testList = w.tokenize("в 1996,1997,1998");
//    assertEquals(Arrays.asList("в", " ", "1996,1997,1998"), testList);

    testList = w.tokenize("2 000 тон з 12 000 відер");
    assertEquals(Arrays.asList("2 000", " ", "тон", " ", "з", " ", "12 000", " ", "відер"), testList);

    testList = w.tokenize("надійшло 12 000 000 тон");
    assertEquals(Arrays.asList("надійшло", " ", "12 000 000", " ", "тон"), testList);

    testList = w.tokenize("до 01.01.42 400 000 шт.");
    assertEquals(Arrays.asList("до", " ", "01.01.42", " ", "400 000", " ", "шт."), testList);

    
    // should not merge these numbers
    testList = w.tokenize("2 15 мільярдів");
    assertEquals(Arrays.asList("2", " ", "15", " ", "мільярдів"), testList);

    testList = w.tokenize("у 2004 200 мільярдів");
    assertEquals(Arrays.asList("у", " ", "2004", " ", "200", " ", "мільярдів"), testList);

    testList = w.tokenize("в бюджеті-2004 200 мільярдів");
    assertEquals(Arrays.asList("в", " ", "бюджеті-2004", " ", "200", " ", "мільярдів"), testList);

    testList = w.tokenize("з 12 0001 відер");
    assertEquals(Arrays.asList("з", " ", "12", " ", "0001", " ", "відер"), testList);

    
    testList = w.tokenize("сталося 14.07.2001 вночі");
    assertEquals(Arrays.asList("сталося", " ", "14.07.2001", " ", "вночі"), testList);

    testList = w.tokenize("вчора о 7.30 ранку");
    assertEquals(Arrays.asList("вчора", " ", "о", " ", "7.30", " ", "ранку"), testList);

    testList = w.tokenize("вчора о 7:30 ранку");
    assertEquals(Arrays.asList("вчора", " ", "о", " ", "7:30", " ", "ранку"), testList);
  }

  @Test
  public void testTokenize() {
    List<String> testList = w.tokenize("Вони прийшли додому.");
    assertEquals(Arrays.asList("Вони", " ", "прийшли", " ", "додому", "."), testList);

    testList = w.tokenize("Вони прийшли пʼятими зів’ялими.");
    assertEquals(Arrays.asList("Вони", " ", "прийшли", " ", "п'ятими", " ", "зів'ялими", "."), testList);

//    testList = w.tokenize("Вони\u0301 при\u00ADйшли пʼя\u0301тими зів’я\u00ADлими.");
//    assertEquals(Arrays.asList("Вони", " ", "прийшли", " ", "п'ятими", " ", "зів'ялими", "."), testList);

    testList = w.tokenize("я українець(сміється");
    assertEquals(Arrays.asList("я", " ", "українець", "(", "сміється"), testList);
        
    testList = w.tokenize("ОУН(б) та КП(б)У");
    assertEquals(Arrays.asList("ОУН(б)", " ", "та", " ", "КП(б)У"), testList);

    testList = w.tokenize("Негода є... заступником");
    assertEquals(Arrays.asList("Негода", " ", "є", "...", " ", "заступником"), testList);

    testList = w.tokenize("Запагубили!.. також");
    assertEquals(Arrays.asList("Запагубили", "!..", " ", "також"), testList);

    testList = w.tokenize("Цей графин.");
    assertEquals(Arrays.asList("Цей", " ", "графин", "."), testList);

    testList = w.tokenize("— Гм.");
    assertEquals(Arrays.asList("—", " ", "Гм", "."), testList);

    testList = w.tokenize("стін\u00ADку");
    assertEquals(Arrays.asList("стін\u00ADку"), testList);

    testList = w.tokenize("стін\u00AD\nку");
    assertEquals(Arrays.asList("стін\u00AD\nку"), testList);

    testList = w.tokenize("п\"яний");
    assertEquals(Arrays.asList("п'яний"), testList);
  }

  @Test
  public void testAbbreviations() {
    List<String> testList = w.tokenize("Засідав І.Єрмолюк.");
//    assertEquals(Arrays.asList("Засідав", " ", "І.", "Єрмолюк", "."), testList);

    testList = w.tokenize("Засідав І. П. Єрмолюк.");
    assertEquals(Arrays.asList("Засідав", " ", "І.", " ", "П.", " ", "Єрмолюк", "."), testList);

    testList = w.tokenize("Засідав І.П.Єрмолюк.");
    assertEquals(Arrays.asList("Засідав", " ", "І.", "П.", "Єрмолюк", "."), testList);

    testList = w.tokenize("І.\u00A0Єрмолюк.");
    assertEquals(Arrays.asList("І.", "\u00A0", "Єрмолюк", "."), testList);

    testList = w.tokenize("Засідав Єрмолюк І.");
    assertEquals(Arrays.asList("Засідав", " ", "Єрмолюк", " ", "І."), testList);

    testList = w.tokenize("Засідав Єрмолюк І. П.");
    assertEquals(Arrays.asList("Засідав", " ", "Єрмолюк", " ", "І.", " ", "П."), testList);

    testList = w.tokenize("Засідав Єрмолюк І. та інші");
    assertEquals(Arrays.asList("Засідав", " ", "Єрмолюк", " ", "І.", " ", "та", " ", "інші"), testList);

    // скорочення

    testList = w.tokenize("140 тис. працівників");
    assertEquals(Arrays.asList("140", " ", "тис.", " ", "працівників"), testList);

    testList = w.tokenize("450 тис. 297 грн");
    assertEquals(Arrays.asList("450", " ", "тис.", " ", "297", " ", "грн"), testList);

    testList = w.tokenize("450 тис.");
    assertEquals(Arrays.asList("450", " ", "тис."), testList);

    testList = w.tokenize("354\u202Fтис.");
    assertEquals(Arrays.asList("354", "\u202F", "тис."), testList);

    testList = w.tokenize("911 тис.грн. з бюджету");
    assertEquals(Arrays.asList("911", " ", "тис.", "грн", ".", " ", "з", " ", "бюджету"), testList);

    testList = w.tokenize("за $400\n  тис., здавалося б");
    assertEquals(Arrays.asList("за", " ", "$400", "\n", " ", " ", "тис.", ",", " ", "здавалося", " ", "б"), testList);


    testList = w.tokenize("проф. Артюхов");
    assertEquals(Arrays.asList("проф.", " ", "Артюхов"), testList);

    testList = w.tokenize("проф.\u00A0Артюхов");
    assertEquals(Arrays.asList("проф.", "\u00A0", "Артюхов"), testList);

    testList = w.tokenize("також зав. відділом");
    assertEquals(Arrays.asList("також", " ", "зав.", " ", "відділом"), testList);

    testList = w.tokenize("до н. е.");
    assertEquals(Arrays.asList("до", " ", "н.", " ", "е."), testList);
 
    testList = w.tokenize("до н.е.");
    assertEquals(Arrays.asList("до", " ", "н.", "е."), testList);

    testList = w.tokenize("в. о. начальника");
    assertEquals(Arrays.asList("в.", " ", "о.", " ", "начальника"), testList);

    testList = w.tokenize("в.о. начальника");
    assertEquals(Arrays.asList("в.", "о.", " ", "начальника"), testList);

    testList = w.tokenize("100 к.с.");
    assertEquals(Arrays.asList("100", " ", "к.", "с."), testList);

    testList = w.tokenize("1998 р.н.");
    assertEquals(Arrays.asList("1998", " ", "р.", "н."), testList);

    testList = w.tokenize("22 коп.");
    assertEquals(Arrays.asList("22", " ", "коп."), testList);

    testList = w.tokenize("18-19 ст.ст. були");
    assertEquals(Arrays.asList("18-19", " ", "ст.", "ст.", " ", "були"), testList);
    
    testList = w.tokenize("І ст. 11");
    assertEquals(Arrays.asList("І", " ", "ст.", " ", "11"), testList);

    testList = w.tokenize("куб. м");
    assertEquals(Arrays.asList("куб.", " ", "м"), testList);

    testList = w.tokenize("куб.м");
    assertEquals(Arrays.asList("куб.", "м"), testList);

    testList = w.tokenize("У с. Вижва");
    assertEquals(Arrays.asList("У", " ", "с.", " ", "Вижва"), testList);

    testList = w.tokenize("Довжиною 30 см. з гаком.");
    assertEquals(Arrays.asList("Довжиною", " ", "30", " ", "см", ".", " ", "з", " ", "гаком", "."), testList);

    testList = w.tokenize("Довжиною 30 см. Поїхали.");
    assertEquals(Arrays.asList("Довжиною", " ", "30", " ", "см", ".", " ", "Поїхали", "."), testList);

    testList = w.tokenize("100 м. дороги.");
    assertEquals(Arrays.asList("100", " ", "м", ".", " ", "дороги", "."), testList);

    testList = w.tokenize("На висоті 4000 м...");
    assertEquals(Arrays.asList("На", " ", "висоті", " ", "4000", " ", "м", "..."), testList);

    testList = w.tokenize("№47 (м. Слов'янськ)");
    assertEquals(Arrays.asList("№47", " ", "(", "м.", " ", "Слов'янськ", ")"), testList);

    testList = w.tokenize("с.-г.");
    assertEquals(Arrays.asList("с.-г."), testList);

    testList = w.tokenize("100 грн. в банк");
    assertEquals(Arrays.asList("100", " ", "грн", ".", " ", "в", " ", "банк"), testList);
    
    testList = w.tokenize("таке та ін.");
    assertEquals(Arrays.asList("таке", " ", "та", " ", "ін."), testList);

    testList = w.tokenize("і т. ін.");
    assertEquals(Arrays.asList("і", " ", "т.", " ", "ін."), testList);

    testList = w.tokenize("Інститут ім. акад. Вернадського.");
    assertEquals(Arrays.asList("Інститут", " ", "ім.", " ", "акад.", " ", "Вернадського", "."), testList);

    testList = w.tokenize("Палац ім. гетьмана Скоропадського.");
    assertEquals(Arrays.asList("Палац", " ", "ім.", " ", "гетьмана", " ", "Скоропадського", "."), testList);

    testList = w.tokenize("від лат. momento");
    assertEquals(Arrays.asList("від", " ", "лат.", " ", "momento"), testList);

    testList = w.tokenize("на 1-кімн. кв. в центрі");
    assertEquals(Arrays.asList("на", " " , "1-кімн.", " ", "кв.", " ", "в", " ", "центрі"), testList);
    
    testList = w.tokenize("Валерій (міліціонер-пародист.\n–  Авт.) стане пародистом.");
    assertEquals(Arrays.asList("Валерій", " ", "(", "міліціонер-пародист", ".", "\n", "–", " ", " ", "Авт.", ")", " ", "стане", " ", "пародистом", "."), testList);

    testList = w.tokenize("Сьогодні (у четвер.  — Ред.), вранці.");
    assertEquals(Arrays.asList("Сьогодні", " ", "(", "у", " ", "четвер", ".", " ", " ", "—", " ", "Ред.", ")", ",", " ", "вранці", "."), testList);
 
    testList = w.tokenize("Fair trade [«Справедлива торгівля». –    Авт.], який стежить за тим, щоб у країнах");
    assertTrue(testList.toString(), testList.contains("Авт."));
    
    testList = w.tokenize("диво з див.");
    assertEquals(Arrays.asList("диво", " ", "з", " ", "див", "."), testList);
    
    testList = w.tokenize("диво з див...");
    assertEquals(Arrays.asList("диво", " ", "з", " ", "див", "..."), testList);

    testList = w.tokenize("тел.: 044-425-20-63");
    assertEquals(Arrays.asList("тел.", ":", " ", "044-425-20-63"), testList);
  }

}
