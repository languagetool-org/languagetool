/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Andriy Rysin
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
package org.languagetool.rules.uk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class TokenAgreementNumrNounRuleTest extends AbstractRuleTest {

//  static {
//    System.setProperty("org.languagetool.rules.uk.TokenInflectionAgreementRule.debug", "true");
//  }
  
  @Before
  public void setUp() throws IOException {
    rule = new TokenAgreementNumrNounRule(TestTools.getMessages("uk"), lt.getLanguage());
//    TokenInflectionAgreementRule.DEBUG = true;
  }
  
  @Test
  public void testRuleTN() throws IOException {

    // correct sentences:
    assertEmptyMatch("два пацани");
    assertEmptyMatch("два паркани");
    assertEmptyMatch("двох пацанів");
    assertEmptyMatch("двох парканів");
    assertEmptyMatch("трьома пацанами");
    assertEmptyMatch("трьома парканами");
    assertEmptyMatch("шість пацанів");
    assertEmptyMatch("шість парканів");
    assertEmptyMatch("двадцять пацанів");
    assertEmptyMatch("восьми пацанів");
    assertEmptyMatch("восьми парканів");
    assertEmptyMatch("вісьма пацанами");
    assertEmptyMatch("вісьма парканами");
    assertEmptyMatch("декілька пацанів");
    assertEmptyMatch("декілька парканів");
    assertEmptyMatch("кілька ложок цукру");
    assertEmptyMatch("декільком пацанам");
    assertEmptyMatch("декільком парканам");
    assertEmptyMatch("вісім-дев'ять місяців");

    assertEmptyMatch("протягом шістьох місяців");
    assertEmptyMatch("за чотирма категоріями");
    assertEmptyMatch("за кількома торгівельними центрами");
    
    assertEmptyMatch("двоє дверей");
    assertEmptyMatch("трьох людей");

    assertEmptyMatch("22 червня");
    assertEmptyMatch("2 лютого їжака забрали");
    assertEmptyMatch("Ту-154 президента");
    
    // двоїна
    assertEmptyMatch("2 сонця");
    // different apostrophes
    assertEmptyMatch("було дев’ять років");
    

    assertEmptyMatch("багато часу");
    assertEmptyMatch("багато заліза");
    assertHasError("як багато білку", "багато білка", "багато білки", "багато білок", "багато білків");

    assertEmptyMatch("пів ковбаси");
    
    assertEmptyMatch("Вісімдесят Геннадію Терентійовичу");


    // odd plurals
    assertEmptyMatch("дві Франції");
    assertEmptyMatch("дві СБУ");
    assertEmptyMatch("усі три Петра");
    assertEmptyMatch("у три погибелі");

    //TODO:
      // два майбуття
//      assertEmptyMatch("Обидва тіста");
//      assertEmptyMatch("в десяти мерами стали");

    // exceptions
    assertEmptyMatch("багато хто");
    assertEmptyMatch("багато які");
    assertEmptyMatch("мало наслідком");
    assertEmptyMatch("все одно йому");
    assertEmptyMatch("один одному руки");
    assertEmptyMatch("за день чи два мене");
    assertEmptyMatch("сьома ранку");
    assertEmptyMatch("години півтори");
    assertEmptyMatch("хвилини за півтори поїзд приїхав");
    assertEmptyMatch("1972 року");
    assertEmptyMatch("1970-1971 рр.");
    assertEmptyMatch("ст. 2 пункта 1");
    assertEmptyMatch("3 / 5 вугілля світу");
    assertEmptyMatch("3 / 4 понеділка");
    assertEmptyMatch("1992 рік");
    assertEmptyMatch("обоє винні");
    assertEmptyMatch("скільки-небудь осяжний");
    assertEmptyMatch("обоє режисери");
    assertEmptyMatch("сотні дві персон");
    // reverse order
    assertEmptyMatch("метрів п’ять килиму");
    assertEmptyMatch("років з п’ять в'язниці");
    assertEmptyMatch("рази може у два більший");
    assertEmptyMatch("десятка півтора списочників");
    // v_rod insert
    assertEmptyMatch("два провінційного вигляду персонажі");
    
    assertEmptyMatch("У свої вісімдесят пан Василь");
    
    assertEmptyMatch("№ 4 Тижня");

    assertEmptyMatch("57-ма вулиця");
    
    //TODO:
//      assertEmptyMatch("в 10,5 розділах");
//      assertEmptyMatch("З п’ятьма розмови не вийшло");
//      // DO THIS
//      assertEmptyMatch("сті-і-і-льки реформ");
//      assertEmptyMatch("з 15 блоків дев'ять зав'язані на дуже");
//      assertEmptyMatch("місяць або два декретної відпустки");
//      // pron
//      assertEmptyMatch("двісті наш бюджет");

    assertEmptyMatch("мати не стільки слух");
    assertEmptyMatch("$300 тис. кредиту");
    assertEmptyMatch("20 мм кредиту");
    // близько 2,5 млн збіжжя
    // 18,1 тис. медперсоналу
    assertEmptyMatch("три нікому не відомих");
    // too many FP
    assertEmptyMatch("несподіваного для багатьох висновку");
    assertEmptyMatch("чотири десятих відсотка");
    
    
    // special for пів...
    assertEmptyMatch("останніх півроку");
    
    assertEmptyMatch("дві англійською");
    assertEmptyMatch("за два злотих (15 грн)");

    //TODO: adj+numr
    assertEmptyMatch("Дев'яноста річниця");
    assertEmptyMatch("сьома вода на киселі");
    assertEmptyMatch("років п'ять люди");
    assertEmptyMatch("років через десять Литва");
  }

  @Test
  public void testRuleTP() throws IOException {
    assertHasError("два пацана", "два пацани");
    assertHasError("дві пацани", "два пацани");
    assertHasError("дві сонця", "два сонця");
    assertHasError("обидва дівчини", "обидві дівчини");
    assertEmptyMatch("дві крайнощі");
    assertEmptyMatch("дві угорські фракції");
    assertEmptyMatch("два місцеві вожді");
    assertEmptyMatch("дві білі групи");
    assertEmptyMatch("дві наглядачки-африканерки");
    assertEmptyMatch("дві турбопрофесії");
    assertEmptyMatch("два імені");
    assertEmptyMatch("один-два громадянини");
    assertEmptyMatch("Обидві ходи");
    assertEmptyMatch("обидва атентати");
    // TOOD:
//  assertEmptyMatch("місяців зо два заготовки для них роблять");
    // по гектарів два капусти
    // через рік або два дороги стануть
    // Обидва провини не визнали

    RuleMatch[] matches00 = rule.match(lt.getAnalyzedSentence("обидві боки"));
    assertEquals(1, matches00.length);
    assertTrue("Message is wrong: " + matches00[0].getMessage(),
        matches00[0].getMessage().contains("Можливо, не збігається рід однини для множинної форми?"));

    // too many FP
//    assertHasError("двох пацанам");
    assertHasError("восьми пацани", "восьми пацанів", "восьми пацанам", "восьми пацанах");
    assertHasError("вісьма пацанах");
    assertHasError("декілька пацани", "декілька пацанів");
//    assertHasError("багато дарами");
    assertHasError("двоє двері", "двоє дверей");

    assertEmptyMatch("на один-півтора відсотка");
    assertEmptyMatch("півтора року");
    assertEmptyMatch("півтори сотні");
    //TODO:
//      assertEmptyMatch("надання півтора мільйонам школярів");
//      assertEmptyMatch("до півтора-двох років");

    // TODO: we don't know if singular is fem or masc so can't catch this
//    assertHasError("півтори антисемітських інциденти");

    assertHasError("півтора роки", "півтора року");
    assertHasError("півтора разу", "півтора раза");
    assertHasError("іспиту півтора роки тому");
    
    // special suggestions
    RuleMatch[] matches0 = rule.match(lt.getAnalyzedSentence("у півтора рази"));
    assertEquals(1, matches0.length);
    assertTrue("Message is wrong: " + matches0[0].getMessage(),
        matches0[0].getMessage().contains("«раза»"));
    assertEquals(Arrays.asList("півтора раза"), matches0[0].getSuggestedReplacements());

    assertHasError("півтора рублі", "півтора рубля");
    assertHasError("півтори рублі", new String[0]);
    assertHasError("за півтора місяці", "півтора місяця");
    assertEmptyMatch("за півтора довгих місяці");
    assertEmptyMatch("півтори місячні норми");

    assertHasError("пів ковбаса", "пів ковбаси");

    assertHasError("Останні три розділі", "три розділи");
    
//    assertHasError("4, 7 мільйона");
    
    assertEmptyMatch("0,5 курки");
    assertHasError("5,5 градуси", "5,5 градуса", "5,5 градусів");
    assertHasError("10,5 ніч", "10,5 ночі", "10,5 ночей");
    
    assertHasError("2,5 мільйонів", "2,5 мільйона", "2,5 мільйони");
    assertHasError("3,5 ночей", "3,5 ночі");
    assertEmptyMatch("14,5 років");
    assertEmptyMatch("протягом 3,5 років");
//    assertEmptyMatch("протягом останніх 3,5 років");
    assertHasError("2-2,5 метрів", "2-2,5 метра", "2-2,5 метри");
    assertEmptyMatch("1,5 метра");
    assertHasError("1,5 метри", "1,5 метра");
    
//    assertHasError("2 ковбаса");


    // adj
    assertEmptyMatch("стільки різноманітних об'єктів");
    assertEmptyMatch("два нових горнятка");
    assertEmptyMatch("два нових веселих горнятка");
    assertEmptyMatch("три десятих");
    assertEmptyMatch("три вихідних");
    assertEmptyMatch("243 хворих");
    assertEmptyMatch("2-3 підозрюваних.");
    assertEmptyMatch("п'ять нових горняток");
    assertEmptyMatch("двадцять першого століття");
    assertEmptyMatch("чотири одиночних та 12 парних титулів");
    assertEmptyMatch(", чотири попередніх канули в Лету");

    // should be cauth by adj-noun rule
    assertEmptyMatch("п'ять нових горнятка");
    
    assertHasError("два великих автобуса", "два великих автобуси");
    assertHasError("434,5 злоякісних новоутворень"); //, "434,5 злоякісних новоутворення");
    assertHasError("на 3 метра за останні", "3 метри", "3 метрів");
    assertHasError("обидва ймовірних кандидата", "обидва ймовірних кандидати");
    assertHasError("4 маленьких єнота", "4 маленьких єноти", "4 маленьких єнотів");
    assertHasError("два легкових автомобіля", "два легкових автомобілі");

    assertEmptyMatch("на мільйон населення");
    assertEmptyMatch("тисячі люду");
  }

  @Test
  public void testRuleForceNoun() throws IOException {
    assertHasError("фронтові сто грам", "сто грамів");
    assertHasError("10—12 мегават", "12 мегаватів");
    assertHasError("12 німецьких солдат");
//  assertHasError("тисячу петабайт", "тисячу петабайтів");
//    assertHasError("30 РЕНТГЕН", "30 РЕНТГЕНІВ");
    assertHasError("десятки тисяч своїх солдат");
    
    assertHasError("п'ять байт", "п'ять байтів");
    assertHasError("5 байт", "5 байтів");
    assertHasError("мільйон байт", "мільйон байтів");
    assertEmptyMatch("мільйон байтів");
    assertEmptyMatch("тримали п'ять бейсбольних біт");
//    assertEmptyMatch("мільйони потрібні");

    // біт також мн. від біта
//    assertHasError("це 16 біт,");
    assertHasError("60 мікрорентген", "60 мікрорентгенів");
    
    assertHasError("10 чоловік і жінок", "10 чоловіків");
    // handled by another rule: cholovik_with_numr
    assertEmptyMatch("10 чоловік.");
  }
  
  @Test
  public void testRuleTon() throws IOException {
    assertHasError("на 200 тон", "тонн");
    assertHasError("2 млн тон", "тонн");
    assertHasError("кілька тон", "тонн");
    assertHasError("вісім тисяч тон", "тонн");
    assertHasError("22 тон", "тонн");
    assertHasError("8,5 тон", "тонн");
  }
  
 
  @Test
  public void testRuleFract() throws IOException {
    assertEmptyMatch("дві з половиною кулі");
    
    assertHasError("два з половиною автобуса", "два з половиною автобуси");
  }
  
  @Test
  public void testRuleFractionals() {
    assertHasError("2,4 кілограми", "2,4 кілограма");
    assertHasError("2,4 мільйони", "2,4 мільйона");
    assertHasError("2,54 мільйони", "2,54 мільйона");
    assertEmptyMatch("33,77 тонни");
    assertEmptyMatch("2,19 бала");
    assertEmptyMatch("33,77 мм");
    assertEmptyMatch("33,77 тис");
    assertEmptyMatch("33,5 роки");
    assertEmptyMatch("33,5 Терещенко");
    assertEmptyMatch("1998,1999 рр."); //<!-- should have space after comma, see next rule -->
    assertEmptyMatch("із результатом 28,91 вона посіла третє місце");
    assertEmptyMatch("Становлять відповідно 0,5 і 0,3 її оцінки.");
    assertEmptyMatch("це на 0,21 краще за попередній рекорд світу.");
    
    assertHasError("або у 2,2 рази.", "2,2 раза");
    assertHasError("або у 2,2 раз", "2,2 раза");
    assertEmptyMatch("або у 2,2 раза.");
    assertEmptyMatch("або у 2 рази.");
  }

  
  @Test
  public void testRuleDisambigVZna() throws IOException {
    assertHasError("два додаткових років");
    
    ArrayList<AnalyzedTokenReadings> readings = new ArrayList<>();
    
    readings.add(new AnalyzedTokenReadings(new AnalyzedToken("", JLanguageTool.SENTENCE_START_TAGNAME, ""), 0));
    readings.add(new AnalyzedTokenReadings(new AnalyzedToken("три", "numr:p:v_zna", "три"), 0));
    readings.add(new AnalyzedTokenReadings(new AnalyzedToken("села", "noun:inanim:p:v_zna", "село"), 0));
    
    AnalyzedSentence sent = new AnalyzedSentence(readings.toArray(new AnalyzedTokenReadings[0]));

    assertEquals(0, rule.match(sent).length);

    readings.clear();
    
    readings.add(new AnalyzedTokenReadings(new AnalyzedToken("", JLanguageTool.SENTENCE_START_TAGNAME, ""), 0));
    readings.add(new AnalyzedTokenReadings(new AnalyzedToken("три", "numr:p:v_zna", "три"), 0));
    readings.add(new AnalyzedTokenReadings(new AnalyzedToken("основні", "adj:p:v_zna:rinanim:compb", "основний"), 0));
    readings.add(new AnalyzedTokenReadings(new AnalyzedToken("села", "noun:inanim:p:v_zna", "село"), 0));
    
    sent = new AnalyzedSentence(readings.toArray(new AnalyzedTokenReadings[0]));

    assertEquals(0, rule.match(sent).length);
  }
  
}
