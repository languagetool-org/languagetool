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
    assertMatches(1, "вибирався Києві");
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
    assertMatches(1, "ще більше погрішать ситуацію");
    assertMatches(1, "поєднатися одне ціле");
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
  }
  
  @Test
  public void testRuleTN() throws IOException {

    assertEmptyMatch("вкрадено державою");
    
    // 
    assertEmptyMatch("спроєктувати проект");
    
    // case govt

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
    
    // numr
    assertEmptyMatch("купив три книги");
    assertEmptyMatch("входило двоє студентів");
    assertEmptyMatch("виповнилося шістнадцять");
    assertEmptyMatch("зобов'язав обох порушників");
    assertMatches(1, "зобов'язав обом порушникам", msg -> assertTrue(msg.contains("давальний")));
    assertEmptyMatch("бував декілька разів");
    assertEmptyMatch("передав решту шкіл");

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
    
    assertEmptyMatch("скільки отримує грошей");
    
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
  }

  @Test
  public void testRuleTnVdav() throws IOException {
    // rv_dav ??
    assertEmptyMatch("Не бачити вам цирку");
   
    // rv_dav: only inf
    assertMatches(1, "жити селянам");
    assertEmptyMatch("куди подітися селянам");
    assertEmptyMatch("тяжче стало жити селянам");

    assertEmptyMatch("слід реагувати Америці");
    assertEmptyMatch("дозволивши розвалитись імперії");

    assertEmptyMatch("Зупинятися мені вже не можна");
    assertEmptyMatch("зніматися йому доводилося рідко");
    assertEmptyMatch("маю тобі щось підказати");

    assertEmptyMatch("дав трохи передихнути бізнесу");

  
    assertEmptyMatch("приємно слухати вчителям");

    assertEmptyMatch("Квапитися їй нікуди");
    assertEmptyMatch("їхав їй назустріч");
    // варто також пити людям
    // не падає нам з неба
    // матюкаються нам в обличчя
  }
  
  // oru
  // скоюються незнайомими жертві злочинцями

  @Test
  public void testRuleTnNumr() throws IOException {
//  assertEmptyMatch("нарубав лісу"); // v_rod
    assertEmptyMatch("потребувала мільйон");
    assertEmptyMatch("нарубав неймовірну кількість вугілля");
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

    // verb + verb + v_naz
    assertEmptyMatch("має відбуватися ротація");
    assertEmptyMatch("має також народитися власна ідея");
    assertEmptyMatch("Почав різко зростати курс долара");
    
    assertEmptyMatch("мали змогу оцінити відвідувачі");
    
    assertEmptyMatch("Здатність ненавидіти прошита");
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
    assertEmptyMatch("працює більшу частину часу");
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
    assertEmptyMatch("не стане сили");

    // не + X + verb
    assertEmptyMatch("не можна зберігати ілюзій");

    assertEmptyMatch("скільки буде людей");

    assertEmptyMatch("трохи маю контактів");
    assertEmptyMatch("скільки загалом здійснили постановок");
    assertEmptyMatch("Багато в пресі з’явилося публікацій");
    assertEmptyMatch("небагато надходить книжок");
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
