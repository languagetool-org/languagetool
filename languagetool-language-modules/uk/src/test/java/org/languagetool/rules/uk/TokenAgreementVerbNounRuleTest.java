/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Andriy Rysin
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
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class TokenAgreementVerbNounRuleTest {

  private JLanguageTool lt;
  private TokenAgreementVerbNounRule rule;

  @Before
  public void setUp() throws IOException {
    rule = new TokenAgreementVerbNounRule(TestTools.getMessages("uk"));
    lt = new JLanguageTool(new Ukrainian());
  }

  @Test
  public void testRuleTP() throws IOException {

//    assertMatches(1, "виграло війську");
    assertMatches(1, "вибирався Києві", c -> assertTrue(c.contains("орудний")));
    assertMatches(1, "вповільнятися підлоги");
    assertMatches(1, "вповільнятися дерев'яні підлоги");
    assertMatches(1, "встановити електроні датчики");
    assertMatches(1, "пройтися трьом книгам");
    assertMatches(1, "Існує Західноєвропейській союз");
    assertMatches(1, "почався справжнісінькій абстинентний синдром");
    assertMatches(1, "досягнув піку");
    assertMatches(1, "сягне піку");
    assertMatches(1, "побачив озброєнні формування");
    assertMatches(1, "втрапили халепу");
    assertMatches(1, "доведено світовім досвідом");
    assertMatches(1, "боятися закордоном");
    assertMatches(1, "з точки зори антилатинської");
//    assertMatches(1, "поєднатися одне ціле");
    assertMatches(1, "не вірить свої очам");
//    assertMatches(1, "Якщо вірити складеними львівськими митниками документам");
    assertMatches(1, "зменшити впив країні");
    assertMatches(1, "займатися модернізацію закладів");
    assertMatches(1, "вважать засобами масової інформації");
    assertMatches(1, "вийшли фотку");
    assertMatches(1, "визнання догорів недійсними");
    assertMatches(1, "що купуються нову техніку");
    assertMatches(1, "наживаються дільці");
    assertMatches(1, "зусилля наблизити перемовин");
    assertMatches(1, "витіснено протестувальники");
    assertMatches(1, "фіктивних догорів оренди");
    assertMatches(1, "Крим залишається територію України");
    assertMatches(1, "Навожу відомі слова");
    assertMatches(1, "розвиток рикну житла");
    assertMatches(1, "покращення епідемічного станув країні");
    assertMatches(1, "вчасно приходить всі профілактичні огляди");
    assertMatches(1, "із наймеш благополучної частини");
    assertMatches(1, "почуватиметься вправі");
    assertMatches(1, "важко уявити  країн Балтії");
//    assertMatches(1, "все залежить нас");
    assertMatches(1, "користується попи том");
    assertMatches(1, "ще більше погрішать ситуацію");
    assertMatches(1, "стався також вибув метану");
    assertMatches(1, "від спати єдиного податку");
    assertMatches(1, "прочитати сааме цю книжку");
    assertMatches(1, "сиплять дуст");
    assertMatches(1, "Охочих навчитися цьому ремеслу");
    assertMatches(1, "поступилася португальці");
    //TODO:
//    assertMatches(1, "планується провесні церемонію");
//    assertMatches(1, "Відчувається, що тримаєте рук на пульсі часу");
    // ADJ + PLURAL
//      assertEmptyMatch("повинні складати нежирна їжа, білкові продукти");
  }
  
  @Test
  public void testRuleTN() throws IOException {

    assertEmptyMatch("вкрадено державою");
    
    // case govt

    assertEmptyMatch("спроєктувати проект");
    assertEmptyMatch("купив книгу");
    assertEmptyMatch("позбавляло людину");
    assertEmptyMatch("залишати межі");
    assertEmptyMatch("залишаються джерелом");
    assertEmptyMatch("відкидати атрибути");

    assertEmptyMatch("належати людині");
    assertEmptyMatch("належати руку");
    assertEmptyMatch("забракло вмінь");
    assertEmptyMatch("Може видатися парадоксальним твердження");

    // impr + oru
    assertEmptyMatch("запропоновано урядом");

    // disambig
    assertEmptyMatch("мало часу");
    assertEmptyMatch("з’явитися перед ним");
    
    // exceptions
    assertEmptyMatch("не було меблів");
//    assertEmptyMatch("не можуть укладати угоди");

    assertEmptyMatch("оплачуватися повинна відповідно");
    
    assertEmptyMatch("відрізнятись один від одного");
    assertEmptyMatch("співпрацювати один із одним");
    assertEmptyMatch("допомагати одне одному");

    // prep + numr 
    assertEmptyMatch("залучити інвестицій на 20—30 мільйонів");
    assertEmptyMatch("збереться людей зо 200");
    
    // question
    assertEmptyMatch("як боротися підприємцям");
    
    // rv_inf
    assertEmptyMatch("захиститися неспроможні");
    
    // insert
    assertEmptyMatch("висміювати такого роду забобони");
    assertEmptyMatch("вважається свого роду психологічним");
    assertEmptyMatch("хто мітингуватиме таким чином");
    
    // 2nd verb
    assertEmptyMatch("міг хитрістю змусити");
    assertEmptyMatch("могли займатися структури");
    assertEmptyMatch("могли б займатися структури");

    assertEmptyMatch("мусить чимось перекрити");
    assertEmptyMatch("довелося її розбирати");
    
    assertEmptyMatch("повинні існувати такі");
    assertEmptyMatch("робити здатна");
    
    // :n: + numr
    assertEmptyMatch("боротиметься кілька однопартійців");

    assertEmptyMatch("вийшла 1987-го");
    
    // його
    assertEmptyMatch("зігріває його серце");
    
    // should have comma?
//    assertEmptyMatch("прокидайся країно");
    assertEmptyMatch("дай Боже");
    assertEmptyMatch("спробуймо йому відповісти");
    assertEmptyMatch("прокинься Тарасе");
    
    // abbr
    assertEmptyMatch("див. новинні");
    
    assertEmptyMatch("повинен відбудватися процес");
    // вірити одне одному
    
    // нікому
    assertEmptyMatch("виявилося нікому не потрібним");
    
    // самому
    assertEmptyMatch("покататися самому");
    
    // compound
    assertEmptyMatch("віддати-відрізати Донбас");
    
//    assertEmptyMatch("серйозно каже Вадо");
    
    // називатися + н.в.
    assertEmptyMatch("вона називалася Оперативний злам");
    
    assertEmptyMatch("підклали дров");
    
    // femin
    assertEmptyMatch("наголосила політик");
    
    assertEmptyMatch("тривав довгих десять раундів");
    
    assertEmptyMatch("лежали всю дорогу");
    
    // v:n + inf
    assertEmptyMatch("сподобалося гуляти");
    assertEmptyMatch("належить пройтися");
    
    // color
    assertEmptyMatch("стали каламутного кольору");
    
    assertEmptyMatch("ні сіло ні впало Комітет держбезпеки");
    
    assertEmptyMatch("звичайна, якщо не сказати слабка, людина");
  }

  @Test
  public void testRuleTnVdav() throws IOException {
    // rv_dav ??
    assertEmptyMatch("Не бачити вам цирку");
    
    assertEmptyMatch("розсміявся йому в обличчя");
    assertEmptyMatch("закружляли мені десь у тьмі");
    assertEmptyMatch("ірже вам у вічі");
    assertEmptyMatch("умоститися господареві на рамена");
    assertEmptyMatch("прилетів йому від посла");
    assertEmptyMatch("пробирається людині під шкіру");
    assertEmptyMatch("їхав їй назустріч");
    assertEmptyMatch("біжать йому навперейми");
    assertMatches(1, "Фабрика Миколая вперше запрацювала Львові у 2001 році");

    assertEmptyMatch("Квапитися їй нікуди");
    assertEmptyMatch("хворіти їй ніколи");
    assertEmptyMatch("Жити родині нема де.");
    assertEmptyMatch("Евакуюватися нам не було куди");

    assertMatches(1, "жити селянам");

    assertEmptyMatch("нічим пишатися жителям");
    assertEmptyMatch("куди подітися селянам");
    //TODO:
//    assertMatches(1, "не вчить нічому поганому");
//    assertEmptyMatch("як тепер жити нам");
//    assertEmptyMatch("у разі неможливості зібратися і працювати Верховній Раді");
  }

  @Test
  public void testRuleTn_V_N_Vinf() throws IOException {
    assertEmptyMatch("маю тобі щось підказати");
    assertEmptyMatch("вміємо цим зазвичай користуватися");
    assertMatches(1, "вміємо цьому зазвичай користуватися");
//    assertMatches(1, "заскрегоіти цим зазвичай користуватися");
    assertEmptyMatch("вони воліли мені якнайбільш ефективно допомогти");
    assertEmptyMatch("воліли заворушень не допускати");
//    assertEmptyMatch("не втомлюються десятиріччями боротися Берлін і Венеція");
    assertEmptyMatch("постарається таку двозначність усунути");
    assertEmptyMatch("розпорядився частину зарплати примусово видавати");
    assertEmptyMatch("не схотіли нам про це казати");
    
    // не
    assertEmptyMatch("Самі респонденти пояснити причин не можуть");
    
    assertEmptyMatch("довелося план «Б» застосовувати");
    
    //TODO: too long
//    assertEmptyMatch("уміє одним жестом, одним нюансом інтонації сказати");
  }

  @Test
  public void testRuleTn_V_Vinf_N() throws IOException {
    assertEmptyMatch("має відбуватися ротація");
    assertEmptyMatch("має також народитися власна ідея");
    assertEmptyMatch("мали змогу оцінити відвідувачі");
    assertEmptyMatch("має ж десь поміститися двигун");
    assertEmptyMatch("дав трохи передихнути бізнесу");
    assertEmptyMatch("дають змогу з комфортом мандрувати чотирьом пасажирам");
    assertEmptyMatch("дали б змогу розвиватися національному");
    assertEmptyMatch("дозволила на початку 1990-х узагалі виникнути такому інституту");
    assertEmptyMatch("заважає і далі нестримно поширюватися багатьом міфам");
    assertEmptyMatch("люблять у нас кричати панікери");
    assertEmptyMatch("Почав різко зростати курс долара");
    assertEmptyMatch("пропонує «об’єднатися патріотам»");
    assertEmptyMatch("став формуватися прошарок");
    // advp
    assertEmptyMatch("не даючи виїхати ванатжівці");
    assertEmptyMatch("даючи можливість висловлюватися радикалам");
    assertEmptyMatch("дозволяючи рухатися російському");
    
    // plural
    assertEmptyMatch("заважають розвиватися погане управління, війна");
    // TODO: 2 verbs
//    assertEmptyMatch(" починає швидко жовтіти й опадати листя");
    // TODO: advp ending is not straight
//    assertEmptyMatch("поклавши спати старого Якима");
//    assertEmptyMatch("став все частіше згадуватися незвичайний наслідок");
//    assertMatches(1, "не має змоги на сто відсотків реалізуватися себе в ролі");
    // TODO: 2 inf verbs
//    assertEmptyMatch("перестають діяти й розвиватися демократичні");
  }

  @Test
  public void testRuleTn_ADV_Vinf_N() throws IOException {
    assertEmptyMatch("важко розібратися багатьом людям.");
    assertEmptyMatch("незручно займатися президентові");
    assertEmptyMatch("пізно готуватися нам");
    assertEmptyMatch("найлегше ігнорувати людям із категорій");
    assertEmptyMatch("треба всіма силами берегти кожному");
    assertEmptyMatch("треба дуже уважно прислухатися владі");
    assertEmptyMatch("слід реагувати Америці");
    assertEmptyMatch("слід з обережністю їсти людям");
    assertEmptyMatch("варто сюди прилетіти людині");
    assertEmptyMatch("неможливо засвоїти одній людині");
    assertEmptyMatch("тяжче стало жити селянам");
    assertEmptyMatch("приємно слухати вчителям");
    assertEmptyMatch("повинно боротися суспільство");
    assertEmptyMatch("слід реально готуватися суспільству");
  }
  
  @Test
  public void testRuleTn_ADJ_Vinf_N() throws IOException {
    assertEmptyMatch("змушені ночувати пасажири");
    assertEmptyMatch("повинен усього добитися сам");
    assertEmptyMatch("схильна лякати така пропаганда");
    assertEmptyMatch("зацікавлена перейняти угорська сторона");
  }

  @Test
  public void testRuleTn_NOUN_Vinf_N() throws IOException {
    assertEmptyMatch("Пора дорослішати всім"); // пора does not have predic :(
    assertEmptyMatch("можливість висловитися серійному вбивці?");
    assertEmptyMatch("рішення про можливість балотуватися Кучмі");
    assertEmptyMatch("гріх уже зараз зайнятися Генеральній прокуратурі...");
    assertEmptyMatch("готовність спілкуватися людини");
    
    assertEmptyMatch("небажання вибачатися пов’язане з національною гордістю");
    assertMatches(1, "бажання постійно вчитися новому");
    assertMatches(1, "Кіно вчить вмінню простими словами");
    assertMatches(1, "Черга вчитися мистецтву мовчання");
    assertMatches(1, "у своєму виступі на конференції порадив української владі звернуть увагу");
    //TODO:
//    assertEmptyMatch("про потребу покаятися представникам влади");
//    assertMatches(1, "Це зумовлює необхідність формувати резервів");
//    assertMatches(1, "пацієнтів будь-якої можливості одужати неприпустима");
  }
  
  @Test
  public void testRuleTn_Vinf_N_V() throws IOException {
    //advp
    assertEmptyMatch("дозволивши розвалитись імперії");
    assertEmptyMatch("зніматися йому доводилося рідко");
    assertEmptyMatch("Гуляти мешканки гуртожитку тепер можуть");
    assertEmptyMatch("працювати ці люди не вміють");
    assertEmptyMatch("реагувати Майдан має");
    assertEmptyMatch("працювати українці будуть");
    assertEmptyMatch("влаштуватися їй не вдається");
    assertEmptyMatch("працювати цьому політикові доводиться");
    // не -> v_rod
    assertEmptyMatch("Робити прогнозів не буду");
    assertEmptyMatch("панькатися наміру не має");
    
    //TODO:
//    assertEmptyMatch("вижити шансів у нього не було");
  }
  
  @Test
  public void testRuleTn_Vinf_N_ADV() throws IOException {
    assertEmptyMatch("літати тобі не можна");
    assertEmptyMatch("Порозумітися обом сторонам було важко");
    assertEmptyMatch("втихомирюватися нам зарано");
    assertEmptyMatch("Зупинятися мені вже не можна");
    assertEmptyMatch("працювати правоохоронцям складно");
    assertEmptyMatch("працювати правоохоронцям досить складно");
    assertEmptyMatch("звертатися пенсіонерам не потрібно");
    assertEmptyMatch("Їхати мені було не страшно");
    assertMatches(1, "навчитися цьому досить легко");
  }
  
  @Test
  public void testRuleTn_Vinf_N_ADJ() throws IOException {
    assertEmptyMatch("замислитися нащадки повинні");
    assertEmptyMatch("працювати студенти готові");
    assertEmptyMatch("платити Рига згодна");
    assertEmptyMatch("Владі не потрібен мир, хоча і війну вести вона не здатна.");
    // TODO:
//    assertEmptyMatch("буваю йому вдячна"); // v_dav
  }
  
  @Test
  public void testRuleTn_Vinf_V_N() throws IOException {
    assertEmptyMatch("Заспокоювати стали найагресивнішого");
    assertEmptyMatch("платити доведеться повну вартість");
    assertEmptyMatch("закінчити цей огляд хочеться словами");
    assertMatches(1, "все досліджуйте і постійно навчайтеся новому.");
    //TODO: їм also verb
//    assertEmptyMatch("їсти їм доведеться траву");
  }

  @Ignore
  @Test
  public void testRuleTn_N_Vinf_ADJ() throws IOException {
    //TODO:
    assertEmptyMatch("Здатність ненавидіти прошита");
    assertEmptyMatch("рішення балотуватися продиктоване");
    assertEmptyMatch("Вони обманюватись раді");
  }

  @Test
  public void testRuleTnNumr() throws IOException {
    
    // numr
    assertEmptyMatch("купив три книги");
    assertEmptyMatch("входило двоє студентів");
    assertEmptyMatch("виповнилося шістнадцять");
    assertEmptyMatch("зобов'язав обох порушників");
    assertMatches(1, "зобов'язав обом порушникам", msg -> assertTrue(msg.contains("давальний")));
    assertEmptyMatch("передав решту шкіл");

    assertEmptyMatch("одержав хабарів на суму 10");
    assertEmptyMatch("одержав хабарів на загальну суму");
    assertEmptyMatch("уклали договорів страхування на загальну суму");
    assertEmptyMatch("и втратили капіталовкладень нерезидентів замалим не на $13,6 млрд.");
    assertEmptyMatch("приготували сортів десять");
    assertEmptyMatch("зібрали сортів 10");
    assertEmptyMatch("зібрали сортів — 10");
    
    assertEmptyMatch("вчинено порушень обсягом на 27");
    
    assertEmptyMatch("завдав кілька ударів руками");
    assertEmptyMatch("завдав кількох ударів руками");
    assertEmptyMatch("потребувала мільйон");
    assertEmptyMatch("нарубав неймовірну кількість вугілля");
    assertEmptyMatch("відбувся чверть століття тому");
    //TODO:
    //  assertEmptyMatch("нарубав лісу"); // v_rod
//    assertMatches(1, "Про це повідомляє Української правди із посланням на співрозмовників");
//    assertEmptyMatch("не виїхало більшість автобусів");
  }
  
  @Test
  public void testRuleTNvNaz() throws IOException {
    // v_naz
    assertEmptyMatch("прийшов Тарас");
    assertEmptyMatch("було пасмо");
    assertEmptyMatch("сміялися смішні гієни");
    assertEmptyMatch("в мені наростали впевненість і сила");
    
    // зватися + prop
    assertEmptyMatch("звалося Подєбради");
  }
  
  @Test
  public void testRuleTNTime() throws IOException {
    // time
    assertEmptyMatch("тренувалися годину");
    assertEmptyMatch("відбудуться наступного дня");
    assertEmptyMatch("їхав цілу ніч");
    assertEmptyMatch("сколихнула минулого року");
    assertEmptyMatch("збираються цього вечора");
    assertEmptyMatch("чекати годинами");
    assertEmptyMatch("спостерігається останнім часом");
    assertEmptyMatch("відійшли метрів п'ять");
    assertEmptyMatch("публікується кожні два роки");
    assertEmptyMatch("відбувся минулої неділі");
    assertEmptyMatch("закінчилося 18-го ввечері");
    assertEmptyMatch("не знімався останні 10 років");
    assertEmptyMatch("розпочнеться того ж дня");
    assertEmptyMatch("помер цього вересня");
    assertEmptyMatch("з’являться наступного ранку на суд");
    assertEmptyMatch("мучитися три з половиною роки");
    assertEmptyMatch("попрацювала місяць-півтора");

    assertEmptyMatch("вщухнуть протягом кількох днів");
    
    // TODO: noun inside
//  assertEmptyMatch("мучитися довжелезних три з половиною роки");
//    assertEmptyMatch("працює більшу частину часу");
//    assertEmptyMatch("доведеться наступні 6−8 років");
//    assertEmptyMatch("які стартували цього та минулого року");
  }
  
  @Test
  public void testRuleTnVrod() throws IOException {
    assertEmptyMatch("не мав меблів");
//    assertMatches(1, "не мав меблі");
    assertEmptyMatch("не повинен пропускати жодного звуку");
    assertEmptyMatch("не став витрачати грошей");
    assertEmptyMatch("казала цього не робити");
    assertEmptyMatch("не повинно перевищувати граничної величини");
    assertEmptyMatch("здаватися коаліціанти не збираються");

    assertEmptyMatch("не існувало конкуренції");

    // не + X + verb
    assertEmptyMatch("не можна зберігати ілюзій");

    assertEmptyMatch("скільки отримує грошей");
    assertEmptyMatch("скільки буде людей");
    assertEmptyMatch("трохи маю контактів");
    assertEmptyMatch("скільки загалом здійснили постановок");
    assertEmptyMatch("стільки заплановано постановок");
    assertEmptyMatch("Багато в пресі з’явилося публікацій");
    assertEmptyMatch("небагато надходить книжок");
    assertEmptyMatch("ніж поставили стільців");
    
    assertEmptyMatch("Росія будує трубопроводів більше");

    assertEmptyMatch("не стане сили");
    //TODO:
//    assertMatches(1, "не відбувалися якихось серйозних");
  }

  
  @Test
  public void testRuleTnInsertPhrase() throws IOException {
    assertEmptyMatch("змалював дивовижної краси церкву");
    assertEmptyMatch("Піднявся страшенної сили крижаний шторм");
    assertEmptyMatch("поводиться дивним чином");
    //TODO:
//    assertEmptyMatch("триває повним ходом.");
//    assertEmptyMatch("скоюються незнайомими жертві злочинцями");
  }
  
  
  // TODO: reuse from other rules
  private void assertEmptyMatch(String text) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(text);
    try {
      assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(rule.match(analyzedSentence)));
    }
    catch (AssertionError e) {
      System.out.println("Sentence: " + analyzedSentence);
      throw e;
    }
  }

  private void assertMatches(int num, String text) throws IOException {
    assertMatches(num, text, null);
  }

  private void assertMatches(int num, String text, Consumer<String> c) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(text));
    assertEquals("Unexpected: " + Arrays.asList(matches), num, matches.length);

    if( c != null ) {
    for(RuleMatch match: matches) {
      c.accept(match.getMessage());
    }
    }
  }
}
