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
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class TokenAgreementAdjNounRuleTest {

  private JLanguageTool langTool;
  private TokenAgreementAdjNounRule rule;

//  static {
//    System.setProperty("org.languagetool.rules.uk.TokenInflectionAgreementRule.debug", "true");
//  }
  
  @Before
  public void setUp() throws IOException {
    rule = new TokenAgreementAdjNounRule(TestTools.getMessages("uk"));
    langTool = new JLanguageTool(new Ukrainian());
//    TokenInflectionAgreementRule.DEBUG = true;
  }
  
  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEmptyMatch("холодний яр");
    assertEmptyMatch("страшне плацебо");
    assertEmptyMatch("військової продукції");

    assertEmptyMatch("Ім'я Мандели, дане йому при народженні");
    assertEmptyMatch("Я не бачив сенсу в тому, щоб виклика́ти свідків і захищатися.");
    assertEmptyMatch("погоджувальної комісії Інституту");
    assertEmptyMatch("відштовхнути нового колегу.");
    assertEmptyMatch("державну зраду.");
    assertEmptyMatch("(Пізніше Вальтер став першим");
    assertEmptyMatch("складовою успіху");
    assertEmptyMatch("про екс-першого віце-спікера.");


    assertEquals(1, rule.match(langTool.getAnalyzedSentence("скрутна справі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("район імпозантних віл")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("офіційний статистика")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("зелена яблуко.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("сп’янілі свободою")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("кволий депутата")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("кволого тюльпан")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("цинічна винахідливості")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("наступній рік свого життя")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("жодного кубічного метру в Україні не буде")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("складний рік на фондовим ринку")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("є найкращий засобом для очистки")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("має вчену ступінь з хімії")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("розкішні чорні коси й, засукавши широкі рукава своєї сорочки,")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("то коло стола її вишивані рукава мають, то коло печі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("валялись одірвані рукава кунтушів та поли жупанів")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("вчинили страшенний диплома тичний галас")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Раймон Бенжамен і керівник європейського філії")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("обвинувачення у вчинені злочину, передбаченого")).length);

    // не працює через іменник "французька" (мова)
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("французька політик")).length);

    RuleMatch[] matches0 = rule.match(langTool.getAnalyzedSentence("4 російських винищувача"));
    assertEquals(1, matches0.length);
    assertTrue("Message is wrong: " + matches0[0].getMessage(),
        matches0[0].getMessage().contains("[ч.р.: родовий, знахідний]"));
    assertEquals(Arrays.asList("російських винищувачів", "російських винищувачах", "російського винищувача"), matches0[0].getSuggestedReplacements());
    
    // from real examples
    
    // і-и
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("під зеківській нуль")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("у повітряній простір")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("в дитячий лікарні")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("у Київський філармонії")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("на керівні посаді")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("незворотній процес")).length);
    // taken care by xml rule
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("408 зниклих безвісті")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("нинішній російські владі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("заробітної платі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("сталеві панцирі")).length);
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("бейсбольною битою машини")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("У львівській та київський Книгарнях")).length);
    // relies on disambiguation
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("— робочій день.")).length);
    assertEmptyMatch("президентів Леонідів Кравчука та Кучму");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("угода на 30 років зі щорічного поставкою росіянами ...")).length);
    
    // missing/extra letter
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("сприймали так власні громадян")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("прокатні транспорті засоби")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ви не притягнене капіталу")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("потрібна змін поколінь")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Незадовільне забезпеченням паливом")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("сочиться коштовний камінням")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("будь-якої демократичної крани")).length);
    // case government
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("що найбільший досягненням")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Генеральній прокураторі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("була низька передумов")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("після смерті легендарного Фреді Меркюрі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("юна викрадача й не здогадувалася")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("По повернені кореспондентів")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("в очікувані експериментатора")).length);

    // wrong letter
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("певної мірою")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("люмпенізується дедалі більша частини")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Державна фіскальну служба")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Як правило, це чоловіки, годувальними сімей")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("з московською боку")).length);

//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("від войовничих хозар")).length);

    // wrong gender
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("з насиджених барліг")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("У польському Лодзі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("панування зимових сутінок")).length);
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("за наявною інформацію")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("асоціюється в нас із сучасною цивілізацію")).length);
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("які вимагалися за тендерною документацію")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("зловживання монопольних становищем")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("проживання та дворазове харчуванням")).length);

    // we don't care much about adjp:actv:imperf
    // FIXME: ignored by adjp:actv:imperf + noun.*v_naz 
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("нова правляча верстви")).length);
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("паралельно приймаючі пацієнтів")).length);

    // false v_rod with -у
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("кримського безсмертнику"));
    assertEquals(1, matches.length);
    assertTrue("Missing message for v_rod/v_dav -у/ю", matches[0].getMessage().contains("Можливо"));

    // false v_rod with -а
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("федерального округа")).length);

    // ne- together
    //TODO: fix later
//    matches = rule.match(langTool.getAnalyzedSentence("пропонує незламані ураганами сучасності"));
//    assertEquals(1, matches.length);
//    assertTrue("Missing message for «не» пишеться окремо", matches[0].getMessage().contains("писати окремо"));
     

    // false :nv
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("затверджений народним віче")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("На великому родинному віче")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("в листопаді 2015 року на народні віче до Кривого Рогу")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("як японські ніндзя")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("приталені пальто")).length);
    
    // missing/extra space, dash etc
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("соціальними мережа ми")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("принциповими країна ми")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("отримала мандатна ведення")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("по вуличному Копійчина")).length);

    // lowercase city
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("У мінську влада")).length);

    // barbarism
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("двометрові забори")).length);
    // will be caught by barbarism rule
    assertEmptyMatch("на пострадянський манер");

    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("вздовж дніпровської вісі")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("ніщо так не зближає приморських партизан")).length);
    //FIXME: FN due to ignoring adj.v_oru + noun.*v_naz/zna
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("що робить її найвищою будівля")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("як боротьбу сунітської більшість")).length);
  }
  
  @Test
  public void testExceptions() throws IOException {
    // adj not noun
    assertEmptyMatch("у могутні Максимові обійми");

    // adj as noun
    assertEmptyMatch("надання болгарській статусу");

    assertEmptyMatch("старший зміни");

    // pron
    assertEmptyMatch("одної шостої світу");
    assertEmptyMatch("Кожному наглядач кивав");

    // pron + adj:n:v_rod
    assertEmptyMatch("чогось схожого Європа");
    assertEmptyMatch("писав про щось подібне Юрій");
    
    // дріб
    assertEmptyMatch("дві мільярдних метра");
    assertEmptyMatch("п’ять шостих населення");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("п'ять шості світу")).length);

    assertEmptyMatch("1/8-ї фіналу");
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("1/8-ї фіналом")).length);

    assertEmptyMatch("з 11-ма годинами");
    assertEmptyMatch("вбивство 148-ми селян");

    
    // площею, об'ємом...
    assertEmptyMatch("чотирициліндровий об’ємом 1000 куб. см.");
    assertEmptyMatch("10 жовтих площею 1,5 ");
    assertEmptyMatch("безплатні довжиною від 100 до 1000 метрів");

    assertEmptyMatch("за метр кубічний води");

    // річних
    assertEmptyMatch("200% річних прибутку");
    
    // плюс
    assertEmptyMatch("муніципальна плюс виробнича");
    
    // головне
    assertEmptyMatch("Головне центр правильно вибити");
    assertEmptyMatch("вибране сучукрліту");
    
    // insert - має бути виділений комами
//    assertEmptyMatch("Та схоже суд таки вийшов");
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Та схоже суд таки вийшов")).length);
    
    // adv
    assertEmptyMatch("Менше народу – більше кисню");
    assertEmptyMatch("Найчастіше випадки прямого підкупу");
    assertEmptyMatch("– щонайперше олівець, простий, твердий");
    assertEmptyMatch("Найбільше звинувачень у відьомстві");
    assertEmptyMatch("— Раніше Україна неодноразово заявляла");

    // пара
    assertEmptyMatch("Від наступних пари десятків");
    // «низка» тут, як і пара, дозволяє множинний іменник
    assertEmptyMatch("Суд визнав неконституційними низку положень");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("від наступних пари")).length);
  }
  
  @Test
  public void testExceptionsNumbers() throws IOException {

    // два/три/чотири чоловіки
    assertEmptyMatch("33 народних обранці");
    assertEmptyMatch("ще троє автокефальних єпископи");
    assertEmptyMatch("два-три реальних кандидати");
    assertEmptyMatch("три жовтих обірваних чоловіки");
    assertEmptyMatch("обидва вітчизняних наукових ступені");
    assertEmptyMatch("3-4 реально хворих депутати");
    assertEmptyMatch("два–три колишніх кандидати");
    assertEmptyMatch("два (чи навіть три) різних завершення роману");

    assertEmptyMatch("два нових горнятка");
    assertEmptyMatch("два жіночих імені");
    assertEmptyMatch("два різних міста");
    assertEmptyMatch("два абсолютно різних міста");
    
    assertEmptyMatch("три предкові слов’янські племені");
    
    assertEmptyMatch("два «круглих столи»");
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("два високих депутат")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("дві високих дівчині")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("83,7 квадратних кілометра")).length);


    
    // 1–3-й класи
    assertEmptyMatch("1–3-й класи поснідали й побігли");
    assertEmptyMatch("у 5–8-му класах");
    assertEmptyMatch("на сьомому–восьмому поверхах");
    assertEmptyMatch("на 14—16-те місця");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("3-й класи поснідали")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("одному-два непоганих шанси")).length);
    
    RuleMatch[] match = rule.match(langTool.getAnalyzedSentence("залишилося сиротами 22-є дітей"));
    assertEquals(1, match.length);
    assertTrue(match[0].getMessage().contains("літерне нарощення після кількісного числівника"));
    
    assertEmptyMatch("восьмого – дев’ятого класів");
    assertEmptyMatch("перший — дев’ятий класи");

    // time
    assertEmptyMatch("і о 7-й ранку нас зустрічає");
    assertEmptyMatch("Призначений на 11-ту похід");
    assertEmptyMatch("о шостій ранку");
    assertEmptyMatch("дванадцята дня");

    // years
    assertEmptyMatch("Ставши 2003-го прем’єром");
    assertEmptyMatch("У 1990-х скрута змусила");
    assertEmptyMatch("за 2009-й відомство зобов’язало");
    assertEmptyMatch("підвів риску під 2011-м програмою «ТОП-100»");
    assertEmptyMatch("Лише в 1990-ті частину саду вдруге зробили доступною");
    assertEmptyMatch("У 2009–2010-му дефіцит бюджету сягав близько 1/3 видатків");
    assertEmptyMatch("в 1920–1930-х батько митця показав себе як український патріот");
    assertEmptyMatch("за часів «конфронтації» 2008–2009-го квота на них зросла");
    assertEmptyMatch("тільки за 1986–1988-й країна втратила близько 40 млрд крб");
    assertEmptyMatch("На початку двотисячних режисер зустрів двох людей");
    assertEmptyMatch("корифеї американської поезії 1950-60-х Лоренс Ферлінгетті");
    assertEmptyMatch("На зламі 80-90-их функціонери ...");
    
    assertEmptyMatch("щороку під Дев’яте травня");
    assertEmptyMatch("з четвертого по одинадцяте липня");
    
    assertEmptyMatch("замість звичного десятиліттями «Українського»");
    assertEmptyMatch("природний тисячею років підтверджений");
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("на 131-му хвилині")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("О 12–ї годині")).length);


    assertEmptyMatch("Анонсована тиждень тому домовленість");
    assertEmptyMatch("забули про популярні пару років тому");

    assertEmptyMatch("завищена разів у десять");
    assertEmptyMatch("інвестиція на найближчі років п’ять");
    assertEmptyMatch("до розташованого кілометрів за шість");
    assertEmptyMatch("заповнені відсотків на 80");
    assertEmptyMatch("лячно було перші хвилин 40");
    assertEmptyMatch("і посаджений років на 10–15");
    assertEmptyMatch("і піднятий відсотки на 3");
    assertEmptyMatch("Поховавши році в п'ятдесятому жінку");

    assertEmptyMatch("хвилини з 55-ї вірмени почали застосовувати пресинг");
    
    assertEmptyMatch("один 5-а клас");
    assertEmptyMatch("маршрутка номер 29-а фірми “Фіакр”");

    assertEmptyMatch("на вул. Рубчака, 17-а Тарас Стецьків");
    assertEmptyMatch("вулиці Володимира Великого, 35-а Юрій Борсук");
  }
  
  @Test
  public void testExceptionsOther() throws IOException {

    // case government
    assertEmptyMatch("Завдяки останнім бізнес");

    // reverse order
    assertEmptyMatch("порядок денний парламенту");
    assertEmptyMatch("зокрема статтю 6-ту закону");
    assertEmptyMatch("князівством Литовським подоляни");
    assertEmptyMatch("абзац перший частини другої");
    assertEmptyMatch("абзац другий частини першої");
    
    // мати рівних
    assertEmptyMatch("яких не мала рівних українка");
    
    // імені
    assertEmptyMatch("Київський імені Шевченка");
    assertEmptyMatch("і колишня Маяковського");
    assertEmptyMatch("Львівської ім. С. Крушельницької");
    assertEmptyMatch("4-й Запорізький ім. гетьмана Б. Хмельницького");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("у Великій Вітчизняній Війн")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("у Великій Вітчизняній війна")).length);
    assertEmptyMatch("Після Великої Вітчизняної будівництво істотно розширилося");

    // зразка
    assertEmptyMatch("польські зразка 1620—1650 років");

    // станом на
    assertEmptyMatch("чинних станом на 4 червня");

    assertEmptyMatch("а старший групи");

    // stable multiword
    assertEmptyMatch("Не пасли задніх міліціонери");
    assertEmptyMatch("сильних світу цього");
    assertEmptyMatch("найвпливовіших світу сього");
    assertEmptyMatch("усіх до єдиного");
    assertEmptyMatch("усі до єдиного депутати");
    assertEmptyMatch("Вольному воля");
    assertEmptyMatch("порядку денного засідань");
    assertEmptyMatch("лаву запасних партії");
    assertEmptyMatch("викладатися на повну артисти");
    assertEmptyMatch("молодшого гвардії сержанта");
    assertEmptyMatch("постійно на рівних міністри, президенти");

    assertEmptyMatch("під час Другої світової командири");

    assertEmptyMatch("до слова Божого людей");
    assertEmptyMatch("Різдва Христова вигнанець");
    assertEmptyMatch("ведуча Першого Національного Марія Орлова");

    assertEmptyMatch("дівоче Анна");

    // <adv>
    assertEmptyMatch("В середньому тривалість курсів для отримання");
    assertEmptyMatch("в цілому результатом задоволені");

    assertEmptyMatch("на червень поточного року $29,3 млрд");

    // перший
    assertEmptyMatch("Одним із перших бажання придбати");
    assertEmptyMatch("Першими голодування оголосили депутати");
    assertEmptyMatch("Перший людина проходить");
    assertEmptyMatch("Перший митців принуджував");
    assertEmptyMatch("вважаючи перших джерелом");
    
    //TODO: due to ignoring перший we missing these:
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Один із перший творів")).length);

    // нічого такого
    assertEmptyMatch("нічого протизаконного жінка не зробила");
    assertEmptyMatch("Нічого подібного Сергій не казав");
    assertEmptyMatch("Нічого поганого людям");
    assertEmptyMatch("що нічим дієвим ініціативи не завершаться");

    // TODO: streets
//    assertEmptyMatch("бачити на Різницький представника донецького регіону");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("на Західній України")).length);
    
    
    // присудок ж.р. + професія
    assertEmptyMatch("переконана психолог");
    
    // adj as noun
    assertEmptyMatch("Серед присутніх Микола");
    assertEmptyMatch("контраргументами рідних засновника структури");
    assertEmptyMatch("спіймали на гарячому хабарників");

    assertEmptyMatch("була б зовсім іншою динаміка");
    assertEmptyMatch("була такою жорстокою політика");
    assertEmptyMatch("стали архаїчними структури");
    assertEmptyMatch("назвав винним Юрія");

    assertEmptyMatch("відмінних від російської моделей"); 
    
    assertEmptyMatch("не перевищував кількох десятих відсотка");

  }
  
  @Test
  public void testExceptionsAdjp() throws IOException {
    
    //////// adjp ////////////

    // adjp:actv:imperf + noun (case government)
    // we ignore adjp:actv.*:bad - it's handled by simple replace rule
    assertEmptyMatch("обмежуючий власність");
    assertEmptyMatch("створивший історію");

    
    // adjp + (весь) в біле/чорне
    assertEmptyMatch("Помальована в біле кімната");
    assertEmptyMatch("Помальована в усе біле кімната");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("помальований в біле кімната")).length);

    // adjp + noun.*v_oru
    assertEmptyMatch("вкриті плющем будинки");
    assertEmptyMatch("всі вкриті плющем");
    assertEmptyMatch("оприлюднений депутатом Луценком");
    assertEmptyMatch("щойно оголошених спікером як відсутніх");
    assertEmptyMatch("групи захищені законом від образ");
    assertEmptyMatch("змучений тягарем життя");
    assertEmptyMatch("відправлені глядачами протягом 20 хвилин");
    assertEmptyMatch("здивований запалом, який проступав");
    assertEmptyMatch("охопленому насильством ваальському трикутнику");
    assertEmptyMatch("переданих заповідником церкві");
    assertEmptyMatch("більше занепокоєних захистом власних прав");

    //TODO:
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("вкриті плющем будинок")).length);


    // adjp + adj:v_oru + noun (case governed by adjp)
    assertEmptyMatch("підсвічений синім діамант");

    
    // adjp + тисячу
    assertEmptyMatch("повторена тисячу разів");
    
    // adjp + a:v_oru + noun: (case from adjp)
    assertEmptyMatch("підсвічений синім діамант");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("підсвічений синім діамантів")).length);


    // adjp + noun (case government)
    assertEmptyMatch("Нав’язаний Австрії нейтралітет");
    assertEmptyMatch("Нав’язаний Австрії коаліцією");
    assertEmptyMatch("Наймилішою українцеві залишається бронза");
    assertEmptyMatch("на цих загальновідомих американцям зразках");
    assertEmptyMatch("слід бути свідомими необхідності");
    assertEmptyMatch("влаштованою Мазепі Петром");
    assertEmptyMatch("будуть вдячні державі Україна");
    assertEmptyMatch("мають бути підпорядковані служінню чоловікові");
    assertEmptyMatch("більше відомої загалу як");   //TODO: теоретично має бути кома перед «як»
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Нав’язаний Австрії нейтралітеті")).length);
    //TODO:
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("змучений тягарем життю")).length);
    
  }
  
  @Test
  public void testExceptionsVerb() throws IOException {

    // бути/стати/лишитися + adj:v_oru + noun:v_dav (gender matches adj)
    assertEmptyMatch("слід бути обережними туристам у горах");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("слід бути обережною туристам у горах")).length);

    
    // modal
    assertEmptyMatch("зараз повинне ділом довести");
    assertEmptyMatch("Вони здатні екскаватором переорювати");
    assertEmptyMatch("яке готове матір рідну продати");
    assertEmptyMatch("Через якийсь час був змушений академію покинути");

    
    // бути/стати/лишитися + adj:v_oru + noun:v_naz (gender matches adj)
    assertEmptyMatch("Досі була чинною заборона");
    assertEmptyMatch("Досі була б чинною заборона");
    assertEmptyMatch("Стає очевидною наявність");
    assertEmptyMatch("було куди зрозумілішим гасло самостійності");
    assertEmptyMatch("є очевидною війна");
    assertEmptyMatch("була б такою ж суттєвою явка");
    assertEmptyMatch("і була б дещо абсурдною ситуація.");
    assertEmptyMatch("Стали дорожчими хліб чи бензин");

    //TODO: ignored by "визнали справедливою наставники" exception
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Стає очевидним наявність")).length);
    // adj + noun agrees, verb + adj/noun agreement will be different rule
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("була чинним закон")).length);

    // verb + a:v_oru + n:v_naz (case matches verb)
    assertEmptyMatch("дівчат не залишила байдужими інформація");
    assertEmptyMatch("визнали справедливою наставники обох команд");
    assertEmptyMatch("визнало незаконною Міністерство юстиції");
    assertEmptyMatch("яку роблять знаковою плями на мундирі");
    assertEmptyMatch("видається цілком стабільною демократія");
    assertEmptyMatch("може бути не ідеальною форма тістечок");
    assertEmptyMatch("не можуть бути толерантними ізраїльтяни");
    //TODO:
//    assertEmptyMatch("визнано справедливою наставниками обох команд");

    
    // verb + adj.v_oru + noun:v_zna (case matches adj)
    assertEmptyMatch("які зроблять неможливою ротацію влади");
    assertEmptyMatch("зробити відкритим доступ");
    assertEmptyMatch("визнають регіональними облради");
    assertEmptyMatch("залишивши незруйнованим Карфаген");
    assertEmptyMatch("зробить обтяжливим використання");
    assertEmptyMatch("На сьогодні залишається невідомою доля близько 200 людей");

    assertEmptyMatch("зробило можливою і необхідною появу нового гравця");
    
    // TODO:  чи має бути «доцільною участь»?
  
    //TODO:
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("партія вважає доцільним участь у списку осіб")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("вважають нелегітимними анексію")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("не залишили байдужими адміністрацію")).length);
    // ignored by verb + adj:v_oru + noun:v_naz
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("визнають регіональною облради")).length);

    
    // ння + adj:v_oru + noun:v_rod
    assertEmptyMatch("визнання неконституційним закону");
    assertEmptyMatch("визнання недійсним рішення");
    assertEmptyMatch("через визнання тут шкідливою орієнтацію на народну мову");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("визнання неконституційними закону")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("визнання недійсним рішенню")).length);
    //TODO: FN due to ignoring adj.v_oru + noun.*v_naz/zna
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("визнання неконституційним закон")).length);
    //TODO:
//  assertEmptyMatch("визнання легітимними президента і прем'єра");        // v_rod??

  }
  
  @Test
  public void testExceptionsAdj() throws IOException {
    //////////// adj ///////////////
    
    // adj + noun (case government)
    assertEmptyMatch("жадібна землі");
    assertEmptyMatch("вдячного батьку");
    assertEmptyMatch("Я вдячний редакторові Вільяму Філліпсу");
    assertEmptyMatch("радий присутності генерала");
    assertEmptyMatch("відомий мешканцям");
    assertEmptyMatch("менш незрозумілу киянам");
    assertEmptyMatch("найстарший віком із нас");
    assertEmptyMatch("таких немилих серцю Булгакова");
    assertEmptyMatch("експозиція, присвячена Леоніду Іллічу");
    assertEmptyMatch("печаткою та вручене платнику");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("жадібна землею")).length);

    // adj + (case government) adj + noun (case match 1st adj)
    assertEmptyMatch("протилежний очікуваному результат");
    assertEmptyMatch("альтернативну олігархічній модель");
    assertEmptyMatch("альтернативні газовому варіанти");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("альтернативну олігархічній порядку")).length);
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("альтернативну олігархічному модель")).length);

    // adj.*v_oru + noun.*v_naz (case matches adj) (+ verb)
    assertEmptyMatch("найчисленнішими цеховики були саме в Грузії");
    assertEmptyMatch("Дефіцитною торгівля США є");
    assertEmptyMatch("Не менш виснажливою війна є і для ворога");
    assertEmptyMatch("Так, відносно чеснішими новини, за даними соціологів, стали");
    assertEmptyMatch("Найнижчою частка таких є на Півдні");
    //     ... -verb
    assertEmptyMatch("то сильнішим інтерес до альтернативної думки");
    assertEmptyMatch("кількість визнаних недійсними бюлетенів");

    // adj.v_oru + noun.*v_naz (no case match) (+verb)
    assertEmptyMatch("Слабшою критики вважають");
    assertEmptyMatch("найбільш райдужною перспектива членства в ЄС залишається");

    // adj.v_oru + noun:v_zna (+ verb)
    assertEmptyMatch("Однак безлюдним місто також не назвеш");
    assertEmptyMatch("Вагомим експерти називають той факт");
    assertEmptyMatch("таким піднесеним президента не бачили давно");

    
    // adj:p: + риска
    assertEmptyMatch("Найближчі півроку-рік");
    assertEmptyMatch("найближчих тиждень-два");
    assertEmptyMatch("протягом минулих травня-липня");
    assertEmptyMatch("Перші рік-два влада відбивалася");
    assertEmptyMatch("суперкризовими січнем–лютим");

    assertHasError("найближчі тиждень");
  }
  
  @Test
  public void testExceptionsPrepAdj() throws IOException {
    // пропустити оборот prep+adj
    
    assertEmptyMatch("діє подібний до попереднього закон");
    assertEmptyMatch("з відмінним від їхнього набором цінностей");
    assertEmptyMatch("Про далеку від взірцевої поведінку");
    assertEmptyMatch("нижчими від ринкових цінами");
    assertEmptyMatch("протилежний до загальнодержавного процес");
    assertEmptyMatch("Схожої з тамтешньою концепції");
    assertEmptyMatch("відрізнялася від нинішньої ситуація");
    assertEmptyMatch("відрізнялася б від нинішньої ситуація");
    assertEmptyMatch("відрізнялося від російського способом");
    
    assertHasError("асоціюється в нас із сучасною цивілізацію");
    
    assertEmptyMatch("На відміну від європейських санкції США");
    assertEmptyMatch("поряд з енергетичними Москва висувала");
    assertEmptyMatch("тотожні із загальносоюзними герб і прапор");

    assertEmptyMatch("чотири подібних до естонських звіти.");
    
    assertEmptyMatch("порівняно з попереднім результат");
    assertEmptyMatch("порівняно із 1999-им доходи автопідприємств");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("він є одним із найстаріший амфітеатрів")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("подібний до попереднього закони")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("порівняний з попереднім результатів")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Схожої з тамтешньою концепція")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("протилежний до загальнодержавному процес")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("вдалися до збройної боротьбі")).length);

//  assertEmptyMatch("її острови відрізняються від Південної природою");
  }


  @Test
  public void testExceptionsPlural() throws IOException {
    /////////// plurals /////////
    
    // plural + пів...
    assertEmptyMatch("на довгих півстоліття");
    assertEmptyMatch("цілих півмісяця");
    assertEmptyMatch("на довгих чверть століття");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("на довгих місяця")).length);

    // plural
    
    // adj:.:p + multiple nouns
    assertEmptyMatch("щоб моїх маму й сестер");
    assertEmptyMatch("власними потом i кров’ю");        // latin i -  we want AlphabetMixedRule to take care of this
    assertEmptyMatch("Перші тиждень чи два");
    assertEmptyMatch("зазначені ім'я, прізвище та місто");
    assertEmptyMatch("Житомирська, Кіровоградська області");
    assertEmptyMatch("ані судова, ані правоохоронна системи");
    assertEmptyMatch("шиїтську та сунітську, а також курдську частини");
    assertEmptyMatch("Чорного і Азовського морів");
    assertEmptyMatch("називає й традиційні корупцію, «відкати», хабарі");
    assertEmptyMatch("державні Ощадбанк, «Укргазбанк»");
    assertEmptyMatch("коринфський з іонійським ордери");
    assertEmptyMatch("можуть зробити доступнішими фосфор чи калій");
    assertEmptyMatch("зв'язаних ченця з черницею");
    assertEmptyMatch("на зарубаних матір з двома синами");
    assertEmptyMatch("повоєнні Австрія з Фінляндією");
    assertEmptyMatch("Опозиційні Андрієвський і Черников");
    assertEmptyMatch("директори навчальної та середньої шкіл");
    assertEmptyMatch("протягом минулих травня – липня");
    assertEmptyMatch("практично відсутні транспорт, гомінкі базари");
    assertEmptyMatch("ВАЖЛИВІ МОТОРИКА І ВІДЧУТТЯ РІВНОВАГИ");
    assertEmptyMatch("канонізовані Іоанн XXIII і Іван Павло II");
    assertEmptyMatch("у дво- й тривимірному форматах");
    assertEmptyMatch("Однак ні паровий, ні електричний двигуни не могли");
    assertEmptyMatch("сміттєпереробного і/або сміттєспалювального заводів");
    assertEmptyMatch("130-те (мінус вісім позицій порівняно з 2009-м) та 145-те місця");
    assertEmptyMatch("ні у методологічному, ні у практичному аспектах.");
    assertEmptyMatch("Хоч в англомовній, хоч в україномовній версіях");
    
    // unknown words
    assertEmptyMatch("Большого та Маріїнського театрів");
    assertEmptyMatch("Пляжі 3, 4 і 5-ї категорій.");

    assertHasError("У львівській та київський Книгарнях");
    assertHasError("повоєнні Австрія з Фінляндію");
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("«Старий паразите...» І кокетлива інтонації ведучої не замасковує зовсім невиправдану")).length);

    assertHasError("Судячи з січневих продаж, 2009-й може стати");
    assertHasError("які наполягали на введені санкцій, будуть продовжуватися.");
    
    // multiple adj + noun:.*:p
    assertEmptyMatch("символізують творчий, оберігальний та руйнівний аспекти Вищої Сили");
    assertEmptyMatch("на місцевому, так і на центральному рівнях");
    assertEmptyMatch("передався повоєнним Відню та Парижу");

    // "long" plural
    assertEmptyMatch("найхарактерніші лояльність до влади й відданість місцевим лідерам.");
    assertEmptyMatch("і з першим, і з другим чоловіками");
    assertEmptyMatch("молодші Олександр Ірванець, Оксана Луцишина, Євгенія Кононенко");
    assertEmptyMatch("230 вчилися за старшинською і 120 за підстаршинською програмами");
  }

  @Test
  public void testExceptionsPluralConjAdv() throws IOException {

    assertEmptyMatch("уражені штаб ІДІЛ, а також збройний завод.");

    assertEmptyMatch("в соціальному, а згодом і в економічному аспектах");
    assertEmptyMatch("до апеляційного, а відтак і до конституційного судів");
    assertEmptyMatch("У переносному та навіть у прямому сенсах слова");
    assertEmptyMatch("в Чернівецькій і частково у Закарпатській областях");
    assertEmptyMatch("парламентської, а згодом і президентської кампаній");
    assertEmptyMatch("на західноєвропейському, а потім і на американському ринках");
    //TODO:
//    assertEmptyMatch("Дохідна, а за нею й видаткова частини держбюджету");
    assertEmptyMatch("навчався в реальному, потім у землемірному училищах");

    assertHasError("для того, щоб пожвавити культурне середовища села");

//    assertEmptyMatch("канонізованих царя Давида, і князя Володимира");    // unnecessary comma
  }

  @Test
  public void testExceptionsInsertPhrase() throws IOException {
    assertEmptyMatch("трагедію російського й, особливо, українського народів");
    assertEmptyMatch("Китай і, певною мірою, Росія зуміли поставити");

    assertHasError("що, однак, не змінюють загальної картин");

    //TODO: here single gender for plural would help, but we don't have that in POS tag
    // alternatively we could try to use synthesizer to guess singular gender
//    assertHasError("історичне рішення, доки у виборчий кампанії");
//    assertHasError("На останньому відрізку, вже на український землі");
//    assertHasError("почувався в ньому не гірше, ніж у парламентський президії");
//    assertHasError("у сільському господарстві (як, зокрема, сербська компанії «МК груп»)");
    assertHasError("про те, що в різних міста");
    assertHasError("Він додав, що у комунальних підприємства");
    assertHasError("наставник сказав, що на світовий першості");
    assertHasError("Аль-Каїда, і чи не найстрашніше терористичний об’єднання");
    assertHasError("буде очікувати, коли нова редакції");
    assertHasError("вважає Порошенко, одночасно закликаючи європейську коаліції");
    assertHasError("пішли вперед, хай і міліметровим кроками");
    assertHasError("Словом, у проблематиці подвійного громадянств");
    assertHasError("Правоохоронцями, зокрема, проведена масштабна операції");
    assertHasError("Він так і не прийняв односторонню капітуляції");
    assertHasError("Так, наприклад, косівським аматорами");
    assertHasError("Якщо третина чи навіть половинна населення");
    assertHasError("Думаю, це фейковий вкидання");
    assertHasError("Сьогодні, наприклад, часта машинобудування");
    //TODO:
    assertEmptyMatch("латиську і, здається, молдавську поезії");
    assertEmptyMatch("в Житомирській чи, скажімо, Миколаївській областях");
  }

  @Test
  // we ignore all pronouns now but this may be useful in the future
  public void testPronouns() throws IOException {
    // pron
    assertEmptyMatch("усі решта");
    assertEmptyMatch("єдину для всіх схему");
    
    assertEmptyMatch("без таких документів");
    assertEmptyMatch("згідно з якими африканцям");
    assertEmptyMatch("чиновників, яким доступ");
    
    assertEmptyMatch("так само");
    assertEmptyMatch("перед тим гарант");
    assertEmptyMatch("усього місяць тому");
    
    // this, that...
    assertEmptyMatch("це мова сото");
    assertEmptyMatch("без якої сім’я не проживе");
   
    assertEmptyMatch("ВО «Свобода», лідер котрої Олег Тягрибок");
    
    assertEmptyMatch("стільки само свідків");

    //TODO: turn back on when we can handle pron
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("із такою самого зневагою")).length);
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("на вибори само висуванцем")).length);
  }
  
  @Test
  public void testSpecialChars() throws IOException {
    assertEmptyMatch("зелений поді\u00ADум");

    assertHasError("зелений по\u00ADділка.");

    assertHasError("зе\u00ADлений поділка.");
  }
  
  private void assertEmptyMatch(String text) {
    try {
      assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(rule.match(langTool.getAnalyzedSentence(text))));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void assertHasError(String text) {
    try {
      assertEquals(1, rule.match(langTool.getAnalyzedSentence(text)).length);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
