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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class TokenVerbAgreementRuleTest {

  private JLanguageTool langTool;
  private TokenVerbAgreementRule rule;

  @Before
  public void setUp() throws IOException {
    rule = new TokenVerbAgreementRule(TestTools.getMessages("uk"));
    langTool = new JLanguageTool(new Ukrainian());
  }
  
  @Test
  public void testRule() throws IOException {

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("чоловік прибігла")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("вони прибіг")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("я прибіжиш")).length);

    // correct sentences:
    assertEmptyMatch("чоловік прибіг");
    assertEmptyMatch("я прибіг");
    assertEmptyMatch("я прибігла");


    // plural
    assertEmptyMatch("моя мама й сестра мешкали");
    assertEmptyMatch("чи то Вальтер, чи я вжили фразу");
    assertEmptyMatch("Кожен чоловік і кожна жінка мають");
    assertEmptyMatch("Європейський Союз і моя рідна дочка переживуть це збурення");
    assertEmptyMatch("Бразилія, Мексика, Індія збувають");
    assertEmptyMatch("Бережна й «мирна» тусовка перебувають");

    // modal verb + noun + verb:inf
    assertEmptyMatch("не встиг я отямитися");
    assertEmptyMatch("що я зробити встиг");
    assertEmptyMatch("що ми зробити не зможемо");
    assertEmptyMatch("ми розраховувати не повинні");
    assertEmptyMatch("Хотів би я подивитися");
    
    // rv_inf
    assertEmptyMatch("чи готові ми сидіти без світла");
//    assertEmptyMatch("Чи повинен я просити");
    
    // plural "semi-numeric"
    assertEmptyMatch("решта забороняються");

    // як
    assertEmptyMatch("тому, що як австрієць маєте");
    
    //TODO:
//    assertEmptyMatch("містечко Баришівка потрапило");
    
    //TODO: need to adjust TokenInflectionAgreementRule for this
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("не встиг вона отямитися")).length);


    assertEmptyMatch(text);
  }
  
  private void assertEmptyMatch(String text) throws IOException {
    assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(rule.match(langTool.getAnalyzedSentence(text))));
  }

  String text = "Хоча упродовж десятиліть ширилися численні історії про те, що я був у ряду наступників трону Тембу, щойно наведений простий генеалогічний екскурс викриває міфічність таких тверджень. Я був членом королівської родини, проте не належав до небагатьох привілейованих, що їх виховували на правителів. Натомість мене як нащадка Лівого дому навчали — так само, як і раніше мого батька — бути радником правителів племені. Мій батько був високим темношкірим чоловіком із прямою й величною поставою, яку я, хочеться думати, успадкував. У батька було пасмо білого волосся якраз над чолом, і хлопчиком я бувало брав сірий попіл і втирав його у своє волосся, щоб воно було таке саме, як у тата. Батько мій мав сувору вдачу й не шкодував різки, виховуючи дітей. Він міг бути дивовижно впертим, і це ще одна риса, яка, на жаль, теж могла перейти від батька до сина. Мого батька інколи називали прем’єр-міністром Тембуленду за врядування Далінд’єбо, батька Сабати, який правив на початку 1900-х років, та його сина й наступника Джонгінтаба. Насправді ж такого титулу не існувало, але мій батько справді відіграв роль, яка не надто відрізнялася від функції прем’єра. Як шанований і високо цінований радник обох королів, він супроводжував їх у подорожах і зазвичай перебував поруч із ними на важливих зустрічах із урядовими чиновниками. Він був також визнаним хоронителем історії коса, і частково через це його порадами так дорожили. Моє власне зацікавлення історією прокинулося рано, і батько його підживлював. Він не вмів ні читати, ні писати, але мав репутацію чудового оратора, який захоплював слухачів, розважаючи й водночас повчаючи їх.";

}
