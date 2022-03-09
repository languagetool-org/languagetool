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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class UkrainianWordTokenizerTest {
  private final UkrainianWordTokenizer w = new UkrainianWordTokenizer();

  @Test
  public void testTokenizeUrl() {
    String url = "http://youtube.com:80/herewego?start=11&quality=high%3F";
    List<String> testList = w.tokenize(url + " ");
    assertEquals(Arrays.asList(url, " "), testList);

    url = "http://example.org";
    testList = w.tokenize(" " + url);
    assertEquals(Arrays.asList(" ", url), testList);

    url = "www.example.org";
    testList = w.tokenize(url);
    assertEquals(Arrays.asList(url), testList);

    url = "elect@ombudsman.gov.ua";
    testList = w.tokenize(url);
    assertEquals(Arrays.asList(url), testList);

    List<String> parts = Arrays.asList("https://www.foo.com/foo", " ", "https://youtube.com", " ", "Зе");
    testList = w.tokenize(StringUtils.join(parts, ""));
    assertEquals(parts, testList);

    parts = Arrays.asList("https://www.phpbb.com/downloads/", "\"", ">", "сторінку");
    testList = w.tokenize(StringUtils.join(parts, ""));
    assertEquals(parts, testList);
  }
  
  @Test
  public void testTokenizeTags() {
    String txt = "<sup>3</sup>";
    List<String> testList = w.tokenize(txt);
    assertEquals(Arrays.asList("<sup>", "3", "</sup>"), testList);
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

    testList = w.tokenize("надійшло 12\u202F000\u202F000 тон");
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

    testList = w.tokenize("3,5-5,6% 7° 7,4°С");
    assertEquals(Arrays.asList("3,5-5,6", "%", " ", "7", "°", " ", "7,4", "°", "С"), testList);
  }

  @Test
  public void testNumbersMissingSpace() {
    List<String> testList = w.tokenize("від 12 до14 років");
    assertEquals(Arrays.asList("від", " ", "12", " ", "до", "14", " ", "років"), testList);

    testList = w.tokenize("до14-15");
    assertEquals(Arrays.asList("до", "14-15"), testList);

    testList = w.tokenize("Т.Шевченка53");
    assertEquals(Arrays.asList("Т.", "Шевченка", "53"), testList);

//    testList = w.tokenize("«Тен»103.");
//    assertEquals(Arrays.asList("«", "Тен", "»", "103", "."), testList);

    testList = w.tokenize("«Мак2»");
    assertEquals(Arrays.asList("«", "Мак2", "»"), testList);

    testList = w.tokenize("км2");
    assertEquals(Arrays.asList("км2"), testList);

    testList = w.tokenize("000ххх000");
    assertEquals(Arrays.asList("000ххх000"), testList);
  }

  @Test
  public void testPlus() {
    List<String> testList = w.tokenize("+20");
    assertEquals(Arrays.asList("+20"), testList);

    testList = w.tokenize("прислівник+займенник");
    assertEquals(Arrays.asList("прислівник", "+", "займенник"), testList);

    testList = w.tokenize("+займенник");
    assertEquals(Arrays.asList("+", "займенник"), testList);

    testList = w.tokenize("Роттердам+ ");
    assertEquals(Arrays.asList("Роттердам+", " "), testList);
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
    assertEquals(Arrays.asList("п\"яний"), testList);

    testList = w.tokenize("Веретениця**");
    assertEquals(Arrays.asList("Веретениця", "**"), testList);

    testList = w.tokenize("мові***,");
    assertEquals(Arrays.asList("мові", "***", ","), testList);

    testList = w.tokenize("*Оренбург");
    assertEquals(Arrays.asList("*", "Оренбург"), testList);

    testList = w.tokenize("▶Трансформація");
    assertEquals(Arrays.asList("▶", "Трансформація"), testList);

    testList = w.tokenize("усмішку😁");
    assertEquals(Arrays.asList("усмішку", "😁"), testList);

    testList = w.tokenize("з*ясував");
    assertEquals(Arrays.asList("з*ясував"), testList);
  }

  @Test
  public void testInitials() {
    List<String> testList = w.tokenize("Засідав І.Єрмолюк.");
    assertEquals(Arrays.asList("Засідав", " ", "І.", "Єрмолюк", "."), testList);

    testList = w.tokenize("Засідав І.   Єрмолюк.");
    assertEquals(Arrays.asList("Засідав", " ", "І.", " ", " ", " ", "Єрмолюк", "."), testList);

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
  }

  @Test
  public void testAbbreviations() {
    // скорочення
    List<String> testList = w.tokenize("140 тис. працівників");
    assertEquals(Arrays.asList("140", " ", "тис.", " ", "працівників"), testList);

    testList = w.tokenize("450 тис. 297 грн");
    assertEquals(Arrays.asList("450", " ", "тис.", " ", "297", " ", "грн"), testList);

    testList = w.tokenize("297 грн...");
    assertEquals(Arrays.asList("297", " ", "грн", "..."), testList);

    testList = w.tokenize("297 грн.");
    assertEquals(Arrays.asList("297", " ", "грн", "."), testList);

//    testList = w.tokenize("297 грн.!!!");
//    assertEquals(Arrays.asList("297", " ", "грн.", "!!!"), testList);

//    testList = w.tokenize("297 грн.??");
//    assertEquals(Arrays.asList("297", " ", "грн.", "??"), testList);

    testList = w.tokenize("450 тис.");
    assertEquals(Arrays.asList("450", " ", "тис."), testList);

    testList = w.tokenize("450 тис.\n");
    assertEquals(Arrays.asList("450", " ", "тис.", "\n"), testList);

    testList = w.tokenize("354\u202Fтис.");
    assertEquals(Arrays.asList("354", "\u202F", "тис."), testList);

    testList = w.tokenize("911 тис.грн. з бюджету");
    assertEquals(Arrays.asList("911", " ", "тис.", "грн", ".", " ", "з", " ", "бюджету"), testList);

    testList = w.tokenize("за $400\n  тис., здавалося б");
    assertEquals(Arrays.asList("за", " ", "$", "400", "\n", " ", " ", "тис.", ",", " ", "здавалося", " ", "б"), testList);

    testList = w.tokenize("найважчого жанру— оповідання");
    assertEquals(Arrays.asList("найважчого", " ", "жанру", "—", " ", "оповідання"), testList);

    testList = w.tokenize("проф. Артюхов");
    assertEquals(Arrays.asList("проф.", " ", "Артюхов"), testList);

    testList = w.tokenize("проф.\u00A0Артюхов");
    assertEquals(Arrays.asList("проф.", "\u00A0", "Артюхов"), testList);

    testList = w.tokenize("Ів. Франко");
    assertEquals(Arrays.asList("Ів.", " ", "Франко"), testList);

    testList = w.tokenize("кутю\u00A0— щедру");
    assertEquals(Arrays.asList("кутю", "\u00A0", "—", " ", "щедру"), testList);

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

    testList = w.tokenize("800 гр. м'яса");
    assertEquals(Arrays.asList("800", " ", "гр.", " ", "м'яса"), testList);

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

    testList = w.tokenize("в м.Київ");
    assertEquals(Arrays.asList("в", " ", "м.", "Київ"), testList);

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

    testList = w.tokenize("і т.д.");
    assertEquals(Arrays.asList("і", " ", "т.", "д."), testList);

    testList = w.tokenize("в т. ч.");
    assertEquals(Arrays.asList("в", " ", "т.", " ", "ч."), testList);

    testList = w.tokenize("до т. зв. сальону");
    assertEquals(Arrays.asList("до", " ", "т.", " ", "зв.", " ", "сальону"), testList);

    testList = w.tokenize(" і под.");
    assertEquals(Arrays.asList(" ", "і", " ", "под."), testList);

    testList = w.tokenize("Інститут ім. акад. Вернадського.");
    assertEquals(Arrays.asList("Інститут", " ", "ім.", " ", "акад.", " ", "Вернадського", "."), testList);

    testList = w.tokenize("Палац ім. гетьмана Скоропадського.");
    assertEquals(Arrays.asList("Палац", " ", "ім.", " ", "гетьмана", " ", "Скоропадського", "."), testList);

    testList = w.tokenize("від лат. momento");
    assertEquals(Arrays.asList("від", " ", "лат.", " ", "momento"), testList);

    testList = w.tokenize("на 1-кімн. кв. в центрі");
    assertEquals(Arrays.asList("на", " " , "1-кімн.", " ", "кв.", " ", "в", " ", "центрі"), testList);

    testList = w.tokenize("1 кв. км.");
    assertEquals(Arrays.asList("1", " ", "кв.", " ", "км", "."), testList);

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

    testList = w.tokenize("с/г");
    assertEquals(Arrays.asList("с/г"), testList);

    testList = w.tokenize("ім.Василя");
    assertEquals(Arrays.asList("ім.", "Василя"), testList);

    testList = w.tokenize("ст.231");
    assertEquals(Arrays.asList("ст.", "231"), testList);

    testList = w.tokenize("2016-2017рр.");
    assertEquals(Arrays.asList("2016-2017", "рр."), testList);

    testList = w.tokenize("30.04.2010р.");
    assertEquals(Arrays.asList("30.04.2010", "р."), testList);

    testList = w.tokenize("ні могили 6в. ");
    assertEquals(Arrays.asList("ні", " ", "могили", " ", "6в", ".", " "), testList);

    testList = w.tokenize("в... одягненому");
    assertEquals(Arrays.asList("в", "...", " ", "одягненому"), testList);

    // invaild but happens
    testList = w.tokenize("10 млн. чоловік");
    assertEquals(Arrays.asList("10", " ", "млн.", " ", "чоловік"), testList);

    testList = w.tokenize("від Таврійської губ.5");
    assertEquals(Arrays.asList("від", " ", "Таврійської", " ", "губ.", "5"), testList);

    testList = w.tokenize("від червоних губ.");
    assertEquals(Arrays.asList("від", " ", "червоних", " ", "губ", "."), testList);

    testList = w.tokenize("К.-Святошинський");
    assertEquals(Arrays.asList("К.-Святошинський"), testList);

    testList = w.tokenize("К.-Г. Руффман");
    assertEquals(Arrays.asList("К.-Г.", " ", "Руффман"), testList);

    testList = w.tokenize("Рис. 10");
    assertEquals(Arrays.asList("Рис.", " ", "10"), testList);

    testList = w.tokenize("худ. фільм");
    assertEquals(Arrays.asList("худ.", " ", "фільм"), testList);

    // not too frequent
//    testList = w.tokenize("30.04.10р.");
//    assertEquals(Arrays.asList("30.04.10", "р."), testList);
  }

  @Test
  public void testBrackets() {
    // скорочення
    List<String> testList = w.tokenize("д[окто]р[ом]");
    assertEquals(Arrays.asList("д[окто]р[ом]"), testList);
  }

  @Test
  public void testApostrophe() {
    List<String> testList = w.tokenize("’продукти харчування’");
    assertEquals(Arrays.asList("'", "продукти", " ", "харчування", "'"), testList);

    testList = w.tokenize("схема 'гроші'");
    assertEquals(Arrays.asList("схема", " ", "'", "гроші", "'"), testList);

    testList = w.tokenize("(‘дзеркало’)");
    assertEquals(Arrays.asList("(", "'", "дзеркало", "'", ")"), testList);

    testList = w.tokenize("все 'дно піду");
    assertEquals(Arrays.asList("все", " ", "'дно", " ", "піду"), testList);

    testList = w.tokenize("трохи 'дно 'дному сказано");
    assertEquals(Arrays.asList("трохи", " ", "'дно", " ", "'дному", " ", "сказано"), testList);

    testList = w.tokenize("а мо',");
    assertEquals(Arrays.asList("а", " ", "мо'", ","), testList);

    testList = w.tokenize("підемо'");
    assertEquals(Arrays.asList("підемо", "'"), testList);

    testList = w.tokenize("ЗДОРОВ’Я.");
    assertEquals(Arrays.asList("ЗДОРОВ'Я", "."), testList);

    testList = w.tokenize("''український''");
    assertEquals(Arrays.asList("''", "український", "''"), testList);

    // 'тсе, 'ддати  'го
    
    testList = w.tokenize("'є");
    assertEquals(Arrays.asList("'", "є"), testList);

    testList = w.tokenize("'(є)");
    assertEquals(Arrays.asList("'", "(", "є", ")"), testList);
  }


  @Test
  public void testDash() {
    List<String> testList = w.tokenize("Кан’-Ка Но Рей");
    assertEquals(Arrays.asList("Кан'-Ка", " ", "Но", " ", "Рей"), testList);

    testList = w.tokenize("і екс-«депутат» вибув");
    assertEquals(Arrays.asList("і", " ", "екс-«депутат»", " ", "вибув"), testList);

    testList = w.tokenize("тих \"200\"-х багато");
    assertEquals(Arrays.asList("тих", " ", "\"200\"-х", " ", "багато"), testList);

    testList = w.tokenize("«діди»-українці");
    assertEquals(Arrays.asList("«діди»-українці"), testList);

//    testList = w.tokenize("«краб»-переросток");
//    assertEquals(Arrays.asList("«", "краб", "»", "-", "переросток"), testList);

    testList = w.tokenize("вересні--жовтні");
    assertEquals(Arrays.asList("вересні","--","жовтні"), testList);

    testList = w.tokenize("—У певному");
    assertEquals(Arrays.asList("—", "У", " ", "певному"), testList);

    testList = w.tokenize("-У певному");
    assertEquals(Arrays.asList("-", "У", " ", "певному"), testList);

    testList = w.tokenize("праця—голова");
    assertEquals(Arrays.asList("праця", "—", "голова"), testList);

    testList = w.tokenize("Людина—");
    assertEquals(Arrays.asList("Людина", "—"), testList);
    
    testList = w.tokenize("Х–ХІ");
    assertEquals(Arrays.asList("Х", "–", "ХІ"), testList);
    
    testList = w.tokenize("VII-VIII");
    assertEquals(Arrays.asList("VII", "-", "VIII"), testList);
    
    testList = w.tokenize("Стрий– ");
    assertEquals(Arrays.asList("Стрий", "–", " "), testList);

    testList = w.tokenize("фіто– та термотерапії");
    assertEquals(Arrays.asList("фіто–", " ", "та", " ", "термотерапії"), testList);

    testList = w.tokenize(" –Виділено");
    assertEquals(Arrays.asList(" ", "–", "Виділено"), testList);

    testList = w.tokenize("так,\u2013так");
    assertEquals(Arrays.asList("так", ",", "\u2013", "так"), testList);
  }
  
  @Test
  public void testSpecialChars() {
    String text = "РЕАЛІЗАЦІЇ \u00AD\n" + "СІЛЬСЬКОГОСПОДАРСЬКОЇ";

    List<String> testList = w.tokenize(text).stream()
        .map(s -> s.replace("\n", "\\n").replace("\u00AD", "\\xAD"))
        .collect(Collectors.toList());
    assertEquals(Arrays.asList("РЕАЛІЗАЦІЇ", " ", "\\xAD", "\\n", "СІЛЬСЬКОГОСПОДАРСЬКОЇ"), testList);

    testList = w.tokenize("а%його");
    assertEquals(Arrays.asList("а", "%", "його"), testList);

    testList = w.tokenize("5%-го");
    assertEquals(Arrays.asList("5%-го"), testList);
  }
}
