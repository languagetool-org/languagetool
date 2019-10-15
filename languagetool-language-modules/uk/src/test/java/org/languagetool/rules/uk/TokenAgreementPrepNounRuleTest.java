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
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class TokenAgreementPrepNounRuleTest {

  private JLanguageTool langTool;
  private TokenAgreementPrepNounRule rule;

  @Before
  public void setUp() throws IOException {
    rule = new TokenAgreementPrepNounRule(TestTools.getMessages("uk"));
    langTool = new JLanguageTool(new Ukrainian());
  }
  
  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEmptyMatch("без повного");
    assertEmptyMatch("без неба");

    assertEmptyMatch("по авеню");

    assertEmptyMatch("що за ганебна непослідовність?");

    assertEmptyMatch("щодо власне людини");
    assertEmptyMatch("у загалом симпатичній повістині");

    assertEmptyMatch("понад половина людей");
    assertEmptyMatch("з понад ста людей");

    assertEmptyMatch("по нервах");
    assertEmptyMatch("з особливою увагою");

    assertEmptyMatch("щодо бодай гіпотетичної здатності");
    assertEmptyMatch("хто їде на заробітки за кордон");

    assertEmptyMatch("піти в президенти");
    assertEmptyMatch("піти межі люди");

    assertEmptyMatch("що то була за людина");
    assertEmptyMatch("що за людина");
    assertEmptyMatch("що балотувався за цім округом");

    assertEmptyMatch("на дому");
    assertEmptyMatch("на біс");

    assertEmptyMatch("згідно з документом");
    assertEmptyMatch("Серед святкових товарів");
    assertEmptyMatch("зовсім не святкові товари Серед святкових товарів");

    assertEmptyMatch("при кому знайдено вогнепальну");
    assertEmptyMatch("За його словами Україна – це країна...");
    
    assertEmptyMatch("славетних од цареві");

    assertEquals(1, ruleMatch("що, незважаючи стислі терміни візиту").length);

    //TODO:
    assertEmptyMatch("залежно що вважати перемогою");

    //TODO: temporary until we have a better logic
    assertEmptyMatch("при їх опублікуванні");
    assertEquals(1, ruleMatch("при їх опублікування").length);

    assertEmptyMatch("окрім як українці");
    assertEmptyMatch("за двісті метрів");
    assertEmptyMatch("переходить у Фрідріх Штрассе");
    assertEmptyMatch("від мінус 1 до плюс 1");
    assertEmptyMatch("до мінус сорока град");
    assertEmptyMatch("до мінус шістдесяти");
    assertEmptyMatch("через років 10");
    assertEmptyMatch("через років зо два");
    assertEmptyMatch("на хвилин 9-10");
    assertEmptyMatch("співпрацювати із собі подібними");
    assertEmptyMatch("через усім відомі причини");
    assertEmptyMatch("через нікому не відомі причини");
    assertEmptyMatch("прийшли до ВАТ «Кривий Ріг цемент»");
    assertEmptyMatch("від А до Я");
    assertEmptyMatch("до та після");
    assertEmptyMatch("до схід сонця");
    assertEmptyMatch("з рана до вечора, від рана до ночі");
    assertEmptyMatch("до НАК «Надра України»");
    assertEmptyMatch("призвів до значною мірою демократичного середнього класу");
    assertEmptyMatch("Вони замість Андрій вибрали Юрій");
    assertEmptyMatch("час від часу нам доводилось");
    assertEmptyMatch("ні до чого доброго силові дії не призведуть");
//    assertEmptyMatch("Імена від Андрій до Юрій");  // називний між від і до рідко зустрічається але такий виняток ховає багато помилок 

//    assertEmptyMatch("як у Конана Дойла")).length); //TODO
//    assertEmptyMatch("як у Конану Дойла")).length);
//    assertEmptyMatch("як у Конан Дойла")).length);
    
    //incorrect sentences:

    RuleMatch[] matches = ruleMatch("без небу");
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("неба"), matches[0].getSuggestedReplacements());
    assertTrue("Не містить «родовий»: " + matches[0].getMessage(), matches[0].getMessage().contains("родовий"));

    matches = ruleMatch("не в останню чергу через корупцією, міжрелігійну ворожнечу");
    assertEquals(1, matches.length);

    matches = ruleMatch("по нервам");
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(3, matches[0].getFromPos());
    assertEquals(9, matches[0].getToPos());
    assertEquals(Arrays.asList("нервах", "нерви"), matches[0].getSuggestedReplacements());
    
    assertEquals(1, ruleMatch("в п'ятьом людям").length);
    assertEquals(1, ruleMatch("в понад п'ятьом людям").length);

    AnalyzedSentence analyzedSentence = langTool.getAnalyzedSentence("завдяки їх вдалим трюкам");
    RuleMatch[] match = rule.match(analyzedSentence);
    assertEquals(1, match.length);
    List<String> suggestedReplacements = match[0].getSuggestedReplacements();
    assertTrue("Did not find «їхній»: " + suggestedReplacements, suggestedReplacements.contains("їхнім"));

    analyzedSentence = langTool.getAnalyzedSentence("О дівчина!");
    match = rule.match(analyzedSentence);
    assertEquals(1, match.length);
    suggestedReplacements = match[0].getSuggestedReplacements();
    assertTrue("Did not find кличний «дівчино»: " + suggestedReplacements, suggestedReplacements.contains("дівчино"));

    matches = ruleMatch("по церковним канонам");
    // check match positions:
    assertEquals(1, matches.length);
    List<String> replacements = matches[0].getSuggestedReplacements();
    assertTrue("Not found церковних among: " + replacements, replacements.contains("церковних"));
    
    // свята
    assertEmptyMatch("на Купала");
    assertEmptyMatch("на Явдохи");
    // вулиці
    assertEmptyMatch("на Мазепи");
    assertEmptyMatch("на Кульчицької");
    assertEmptyMatch("на Правди");
    assertEmptyMatch("на Ломоносова");
    // invert
    assertEmptyMatch("як на Кучми іменини");
    // ім'я, прізвище
    assertEmptyMatch("змінив ім'я на Фріц Ланг");
    assertEmptyMatch("Бо заміна прізвища Горбатий на Щербань передбачає i зміну ситуації.");
//    assertEmptyMatch("поміняв Юрій Володимирович на Георгій Вурдалакович.");

    assertEmptyMatch("З одного боку на щастя сім’я Ющенків нарешті з’їжджає з державної дачі.");

//    assertEmptyMatch("змінили з № 20 на 20-а");
//    assertEmptyMatch("парні номери від 84-а до 104 включно");
    
    assertEmptyMatch("До сьогодні українська влада намагалася");
    
    
    //TODO:
//    assertEmptyMatch("Так висловлюються про екс-першого віце-спікера.");

    
    matches = ruleMatch("спиралося на місячної давнини рішенням");
    assertEquals(1, matches.length);

    matches = ruleMatch("Від стягу Ататюрка до піратського прапору");
    assertEquals(1, matches.length);

    matches = ruleMatch("згідно з документа");
    assertEquals(1, matches.length);

    matches = ruleMatch("зацікавлених у ви користанні");
    assertEquals(1, matches.length);

//    matches = ruleMatch("колега з Мінську");
//    System.out.println(langTool.getAnalyzedSentence("колега з Мінську"));
//    // check match positions:
//    assertEquals(1, matches.length);

    matches = ruleMatch("В йому заграла кров.");
    assertEquals(1, matches.length);

    matches = ruleMatch("  В йому заграла кров.");
    assertEquals(1, matches.length);

    matches = ruleMatch("І от «В йому заграла кров».");
    assertEquals(1, matches.length);

    matches = ruleMatch("думає на тим, як");
    assertEquals(1, matches.length);

    assertEmptyMatch("гепатитів В та С");
    
    matches = ruleMatch("— О пан Єзус, захисти їх!");
    assertEquals(1, matches.length);
    
//    matches = ruleMatch("На фото: З Голлівуду Яринка Шуст привезла дві золоті медалі");
//    assertEquals(1, matches.length);
  }

  @Test
  public void testRuleFlexibleOrder() throws IOException {

    assertEquals(1, ruleMatch("по бодай маленьким справам").length);

    // we ignore adv, otherwise logic gets too complicated
//    assertEquals(1, ruleMatch("по смішно маленьким справам").length);

    assertEmptyMatch("спиралося на місячної давнини рішення");

    assertEmptyMatch("На середньої довжини шубу");

    assertEmptyMatch("При різного роду процесах");

    assertEmptyMatch("по лише їм цікавих місцях");
    assertEquals(1, ruleMatch("по лише їм цікавим місцям").length);

    assertEmptyMatch("від дуже близьких людей");
    assertEmptyMatch("завдяки саме цим сімом голосам");
    assertEmptyMatch("на мохом стеленому дні");
    assertEmptyMatch("який до речі вони присягалися");
    //TODO:
//    assertEmptyMatch("до ледве що не членства");

    assertEquals(1, ruleMatch("призвів до значною мірою демократичному середньому класу").length);

    assertEquals(1, ruleMatch("це нас для дуже велика сума").length);

    assertEmptyMatch("для якої з мов воно первинне");

    assertEquals(1, ruleMatch("у дуже обмеженим рамках").length);

    assertEmptyMatch("чи не проти я тієї церковної стройки");
    assertEmptyMatch("З точністю до навпаки ви все це побачите");
  }

  private RuleMatch[] ruleMatch(String text) throws IOException {
    return rule.match(langTool.getAnalyzedSentence(text));
  }
  
  private void assertEmptyMatch(String text) throws IOException {
    assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(ruleMatch(text)));
  }
  
  @Test
  public void testSpecialChars() throws IOException {
    assertEmptyMatch("до їм поді\u00ADбних");

    RuleMatch[] matches = ruleMatch("о справедли\u00ADвости.");
    assertEquals(1, matches.length);

    matches = ruleMatch("по не́рвам, по мо\u00ADстам, по воротам");
    // check match positions:
    assertEquals(3, matches.length);

    assertEquals(3, matches[0].getFromPos());
    assertEquals(10, matches[0].getToPos());
    assertEquals(Arrays.asList("нервах", "нерви"), matches[0].getSuggestedReplacements());
//    assertEquals(3, matches[1].getFromPos());

    assertEquals(15, matches[1].getFromPos());
    assertEquals(Arrays.asList("мостах", "мости"), matches[1].getSuggestedReplacements());
//    assertEquals(1, matches[1].getFromPos());

    assertEquals(27, matches[2].getFromPos());
    assertEquals(Arrays.asList("воротах", "воротях", "ворота"), matches[2].getSuggestedReplacements());
  }

  @Test
  public void testUnusualCharacters() throws IOException {
    String txt = "о стін\u00AD\nку";

    RuleMatch[] matches = ruleMatch(txt);
    assertEquals(0, matches.length);

  }

}
