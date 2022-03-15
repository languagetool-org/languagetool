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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TokenAgreementPrepNounRuleTest {

  private JLanguageTool lt;
  private TokenAgreementPrepNounRule rule;

  @BeforeEach
  public void setUp() throws IOException {
    rule = new TokenAgreementPrepNounRule(TestTools.getMessages("uk"));
    lt = new JLanguageTool(new Ukrainian());
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
    assertEmptyMatch("вивів в люде");

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
    assertEmptyMatch("А шляхом тим була");

    Assertions.assertEquals(1, ruleMatch("що, незважаючи стислі терміни візиту").length);

    assertEmptyMatch("залежно що вважати перемогою");

    assertEmptyMatch("окрім як українці");
    assertEmptyMatch("за двісті метрів");
    assertEmptyMatch("від мінус 1 до плюс 1");
    assertEmptyMatch("від мінус $1 до плюс 1");
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
    
    assertEmptyMatch("у святая святих");

    assertEmptyMatch("станом на зараз виконавча влада");
    
//    assertEmptyMatch("Імена від Андрій до Юрій");  // називний між від і до рідко зустрічається але такий виняток ховає багато помилок 

//    assertEmptyMatch("як у Конана Дойла")).length); //TODO
//    assertEmptyMatch("як у Конану Дойла")).length);
//    assertEmptyMatch("як у Конан Дойла")).length);
    
    //incorrect sentences:

    RuleMatch[] matches = ruleMatch("без небу");
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(Collections.singletonList("неба"), matches[0].getSuggestedReplacements());
    Assertions.assertTrue(matches[0].getMessage().contains("родовий"), "Не містить «родовий»: " + matches[0].getMessage());

    matches = ruleMatch("не в останню чергу через корупцією, міжрелігійну ворожнечу");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("по нервам");
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(3, matches[0].getFromPos());
    Assertions.assertEquals(9, matches[0].getToPos());
    Assertions.assertEquals(Arrays.asList("нервах", "нерви"), matches[0].getSuggestedReplacements());

    matches = ruleMatch("по швидко напруженим рукам");
    // check match positions:
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("до не властиву");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("до не дуже властиву");
    Assertions.assertEquals(1, matches.length);

    assertEmptyMatch("На сьогодні рослинна їжа");
    
    
    
    Assertions.assertEquals(1, ruleMatch("в п'ятьом людям").length);
    Assertions.assertEquals(1, ruleMatch("в понад п'ятьом людям").length);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("О дівчина!");
    RuleMatch[] match = rule.match(analyzedSentence);
    Assertions.assertEquals(1, match.length);
    List<String> suggestedReplacements = match[0].getSuggestedReplacements();
    Assertions.assertTrue(suggestedReplacements.contains("дівчино"), "Did not find кличний «дівчино»: " + suggestedReplacements);

    matches = ruleMatch("по церковним канонам");
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    List<String> replacements = matches[0].getSuggestedReplacements();
    Assertions.assertTrue(replacements.contains("церковних"), "Not found церковних among: " + replacements);
    
    // свята
    assertEmptyMatch("на Купала");
    assertEmptyMatch("на Явдохи");
    // вулиці
    assertEmptyMatch("на Мазепи");
    assertEmptyMatch("на Кульчицької");
    assertEmptyMatch("на Правди");
    assertEmptyMatch("на Ломоносова");
    assertEmptyMatch("переходить у Фрідріх Штрассе");
    // invert
    assertEmptyMatch("як на Кучми іменини");
    // ім'я, прізвище
    assertEmptyMatch("змінив ім'я на Фріц Ланг");
    assertEmptyMatch("Бо заміна прізвища Горбатий на Щербань передбачає i зміну ситуації.");
//    assertEmptyMatch("поміняв Юрій Володимирович на Георгій Вурдалакович.");
//  assertEmptyMatch("змінили з № 20 на 20-а");
//  assertEmptyMatch("парні номери від 84-а до 104 включно");

    assertEmptyMatch("З одного боку на щастя сім’я Ющенків нарешті з’їжджає з державної дачі.");

    
    assertEmptyMatch("До сьогодні українська влада намагалася");
    
    assertEmptyMatch("Так висловлюються про екс-першого віце-спікера.");

    matches = ruleMatch("спиралося на місячної давнини рішенням");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("Від стягу Ататюрка до піратського прапору");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("згідно з документа");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("згідно зі змінам");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("зацікавлених у ви користанні");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("без правда");
    Assertions.assertEquals(1, matches.length);

    // TODO: ignored due to adj:v_zna "мінський"
//    matches = ruleMatch("колега з Мінську");
//    assertEquals(1, matches.length);

    matches = ruleMatch("В йому заграла кров.");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("  В йому заграла кров.");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("І от «В йому заграла кров».");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("похвалила при йому вкраїнську мову");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("думає на тим, як");
    Assertions.assertEquals(1, matches.length);

    assertEmptyMatch("гепатитів В та С");
    
    matches = ruleMatch("— О пан Єзус, захисти їх!");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("до Ленінграду");
    Assertions.assertEquals(1, matches.length);

//    matches = ruleMatch("На фото: З Голлівуду Яринка Шуст привезла дві золоті медалі");
//    assertEquals(1, matches.length);
  }

  @Test
  public void testRulePronPos() throws IOException {
    assertEmptyMatch("повернесть до них-таки.");

    assertEmptyMatch("колега поруч сонним виглядає");
    
    //TODO: temporary until we have a better logic
    assertEmptyMatch("при їх опублікуванні");
    assertEmptyMatch("всупереч їх рекомендаціям");
    assertEmptyMatch("на їх користь стягнуто");
    assertEmptyMatch("не всупереч, а тому, що він має");
    assertEmptyMatch("у його (лікаря) присутності");
    Assertions.assertEquals(1, ruleMatch("до їх").length);
    
    assertEmptyMatch("Під його, без перебільшення, мудрим");
    assertEmptyMatch("до її, так би мовити, санітарного стану");
    assertEmptyMatch("в його, судячи з інтер’єру, службовому кабінеті");
    
    assertEmptyMatch("вплив на її або його здоров'я");
    assertEmptyMatch("щодо його \"лікування\":");
    
    Assertions.assertEquals(1, ruleMatch("вище за їх?").length);
//    assertEquals(1, ruleMatch("займався в їх помаленьку").length);
    Assertions.assertEquals(1, ruleMatch("про їх говорилося").length);

    //  AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("завдяки їх вдалим трюкам");
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("завдяки їх.");
    RuleMatch[] match = rule.match(analyzedSentence);
    Assertions.assertEquals(1, match.length);
    List<String> suggestedReplacements = match[0].getSuggestedReplacements();
    Assertions.assertTrue(suggestedReplacements.contains("їхнім"), "Did not find «їхній»: " + suggestedReplacements);

    assertEmptyMatch("при її, м’яко кажучи, невеликій популярності");
    assertEmptyMatch("через її, м’яко кажучи, невелику популярність");
    
    Assertions.assertEquals(1, ruleMatch("при його ж заняттів").length);
  }
  
  @Test
  public void testRuleFlexibleOrder() throws IOException {

    Assertions.assertEquals(1, ruleMatch("по бодай маленьким справам").length);
    Assertions.assertEquals(1, ruleMatch("по смішно маленьким справам").length);

    Assertions.assertEquals(1, ruleMatch("через, м’яко кажучи, невеликої популярності").length);

    assertEmptyMatch("спиралося на місячної давнини рішення");

    assertEmptyMatch("На середньої довжини шубу");

    assertEmptyMatch("При різного роду процесах");

    Assertions.assertEquals(1, ruleMatch("завдяки його прийомі").length);

    assertEmptyMatch("по лише їм цікавих місцях");
    Assertions.assertEquals(1, ruleMatch("по лише їм цікавим місцям").length);

    assertEmptyMatch("від дуже близьких людей");
    assertEmptyMatch("завдяки саме цим сімом голосам");
    assertEmptyMatch("на мохом стеленому дні");
    assertEmptyMatch("який до речі вони присягалися");
    
    assertEmptyMatch("на нічого не вартий папірець");
    //TODO:
//    assertEmptyMatch("до ледве що не членства");

    Assertions.assertEquals(1, ruleMatch("призвів до значною мірою демократичному середньому класу").length);

    Assertions.assertEquals(1, ruleMatch("це нас для дуже велика сума").length);

    assertEmptyMatch("для якої з мов воно первинне");

    Assertions.assertEquals(1, ruleMatch("у дуже обмеженим рамках").length);

    assertEmptyMatch("чи не проти я тієї церковної стройки");
    assertEmptyMatch("З точністю до навпаки ви все це побачите");
    
    assertEmptyMatch("Усупереч не те що лихим");
    assertEmptyMatch("весь світ замість спершу самому засвоїти");
    assertEmptyMatch("Йдеться про вже всім добре відому");
    
    Assertions.assertEquals(1, ruleMatch("кинулися до мені перші з них").length);
    Assertions.assertEquals(1, ruleMatch("Замість лимону можна брати").length);
    
    //TODO:
//    assertEmptyMatch("не завдяки, а всупереч політиці, яку проводила влада");
  }

  private RuleMatch[] ruleMatch(String text) throws IOException {
    return rule.match(lt.getAnalyzedSentence(text));
  }
  
  private void assertEmptyMatch(String text) throws IOException {
    Assertions.assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(ruleMatch(text)));
  }
  
  @Test
  public void testSpecialChars() throws IOException {
    assertEmptyMatch("до їм поді\u00ADбних");

    RuleMatch[] matches = ruleMatch("о справедли\u00ADвости.");
    Assertions.assertEquals(1, matches.length);

    matches = ruleMatch("по не́рвам, по мо\u00ADстам, по воротам");
    // check match positions:
    Assertions.assertEquals(3, matches.length);

    Assertions.assertEquals(3, matches[0].getFromPos());
    Assertions.assertEquals(10, matches[0].getToPos());
    Assertions.assertEquals(Arrays.asList("нервах", "нерви"), matches[0].getSuggestedReplacements());
//    assertEquals(3, matches[1].getFromPos());

    Assertions.assertEquals(15, matches[1].getFromPos());
    Assertions.assertEquals(Arrays.asList("мостах", "мости"), matches[1].getSuggestedReplacements());
//    assertEquals(1, matches[1].getFromPos());

    Assertions.assertEquals(27, matches[2].getFromPos());
    Assertions.assertEquals(Arrays.asList("воротах", "воротях", "ворота"), matches[2].getSuggestedReplacements());
  }

  @Test
  public void testUnusualCharacters() throws IOException {
    String txt = "о стін\u00AD\nку";

    RuleMatch[] matches = ruleMatch(txt);
    Assertions.assertEquals(0, matches.length);

  }

  @Test
  public void testWithAdv() throws IOException {
    RuleMatch[] match = ruleMatch("гречка у двічі дешевша ніж");
    Assertions.assertEquals(1, match.length);
    Assertions.assertTrue(match[0].getMessage().contains("Можливо, прийменник і прислівник"));
  }
  
  @Test
  public void testIsCapitalized() {
    Assertions.assertFalse(LemmaHelper.isCapitalized("боснія"));
    Assertions.assertTrue(LemmaHelper.isCapitalized("Боснія"));
    Assertions.assertTrue(LemmaHelper.isCapitalized("Боснія-Герцеговина"));
    Assertions.assertFalse(LemmaHelper.isCapitalized("По-перше"));
    Assertions.assertFalse(LemmaHelper.isCapitalized("ПаП"));
    Assertions.assertTrue(LemmaHelper.isCapitalized("П'ятниця"));
    Assertions.assertFalse(LemmaHelper.isCapitalized("П'ЯТНИЦЯ"));
    Assertions.assertTrue(LemmaHelper.isCapitalized("EuroGas"));
    Assertions.assertTrue(LemmaHelper.isCapitalized("Рясна-2"));
    Assertions.assertFalse(LemmaHelper.isCapitalized("ДБЗПТЛ"));
  }

}
