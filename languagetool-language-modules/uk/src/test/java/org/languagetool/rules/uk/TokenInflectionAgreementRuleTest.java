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

public class TokenInflectionAgreementRuleTest {

  private JLanguageTool langTool;
  private TokenInflectionAgreementRule rule;

  @Before
  public void setUp() throws IOException {
    rule = new TokenInflectionAgreementRule(TestTools.getMessages("uk"));
    langTool = new JLanguageTool(new Ukrainian());
  }
  
  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEmptyMatch("холодний яр");
    assertEmptyMatch("страшне плацебо");

    assertEmptyMatch("Ім'я Мандели, дане йому при народженні");
    assertEmptyMatch("Я не бачив сенсу в тому, щоб виклика́ти свідків і захищатися.");
    assertEmptyMatch("погоджувальної комісії Інституту");
    assertEmptyMatch("відштовхнути нового колегу.");
    assertEmptyMatch("державну зраду.");
    assertEmptyMatch("(Пізніше Вальтер став першим");
//    assertEmptyMatch("складовою успіху");
    
    
    // case government
    assertEmptyMatch("жадібна землі");
    assertEmptyMatch("вдячного батьку");
    assertEmptyMatch("радий присутності генерала");
    assertEmptyMatch("відомий мешканцям");
    assertEmptyMatch("військової продукції");
    
    // exceptions
    
    //TODO:
    assertEmptyMatch("одної шостої світу");
    assertEmptyMatch("усі решта");
    assertEmptyMatch("єдину для всіх схему");
    
    assertEmptyMatch("без таких документів");
    assertEmptyMatch("згідно з якими африканцям");
    assertEmptyMatch("чиновників, яким доступ");

    assertEmptyMatch("надання болгарській статусу");
    
    assertEmptyMatch("порядку денного засідань");
    assertEmptyMatch("так само");
    assertEmptyMatch("перед тим гарант");
    assertEmptyMatch("усього місяць тому");
// до того; одне ціле ...; в цілому; віддати належне; у подальшому; ; в основному зустріччю
    assertEmptyMatch("Серед присутніх Микола");
    assertEmptyMatch("контраргументами рідних засновника структури");
    assertEmptyMatch("спіймали на гарячому хабарників");

// була б зовсім іншою    динаміка
    assertEmptyMatch("стали архаїчними структури");
// зробить    обтяжливим використання
    assertEmptyMatch("назвав винним Юрія");
//практично відсутні транспорт, гомінкі базари

    assertEmptyMatch("відмінних від російської моделей"); 
    
    assertEmptyMatch("не перевищував кількох десятих відсотка");
    
    // adjp
    assertEmptyMatch("вкриті плющем будинки");
    assertEmptyMatch("більше занепокоєних захистом власних прав");

    // time
    assertEmptyMatch("о шостій ранку");
    assertEmptyMatch("дванадцята дня");
    assertEmptyMatch("Ставши 2003-го прем’єром");
    
    // this, that...
    assertEmptyMatch("це мова сото");
    assertEmptyMatch("без якої сім’я не проживе");
   
    assertEmptyMatch("ВО «Свобода», лідер котрої Олег Тягрибок");
    
    assertEmptyMatch("найстарший віком із нас");
    assertEmptyMatch("стільки само свідків");
    
    // plural
    assertEmptyMatch("щоб моїх маму й сестер");
    assertEmptyMatch("директори навчальної та середньої шкіл");
    assertEmptyMatch("Перші тиждень чи два");
    assertEmptyMatch("зазначені ім'я, прізвище та місто");
    assertEmptyMatch("Житомирська, Кіровоградська області");
    assertEmptyMatch("ані судова, ані правоохоронна системи");
    assertEmptyMatch("а також курдську частини");
    // "long" plural
    assertEmptyMatch("так і на центральному рівнях");
    assertEmptyMatch("і з першим, і з другим чоловіками");
    assertEmptyMatch("молодші Олександр Ірванець, Оксана Луцишина, Євгенія Кононенко");

    assertEmptyMatch("Завдяки останнім бізнес");
    
    // two, three, four
    assertEmptyMatch("два нових горнятка");
    
    // time, years etc
    assertEmptyMatch("Призначений на 11-ту похід");
    assertEmptyMatch("У 1990-х скрута змусила");
    
    // reverse order
    assertEmptyMatch("порядок денний парламенту");
    
    // 2-4 numr
    assertEmptyMatch("33 народних обранці");
    
    // бути + adj
    assertEmptyMatch("Досі була чинною заборона");
    
    // other pos
    assertEmptyMatch("— Раніше Україна неодноразово заявляла");
    
    // overlap
    assertEmptyMatch("в цілому результатом задоволені");
    
    assertEmptyMatch("Львівської ім. С. Крушельницької");
  
    assertEmptyMatch("Кожному наглядач кивав");
    
    assertEmptyMatch("про екс-першого віце-спікера.");
    
    // verb-adj government
    //TODO: "визнати цілком прийнятною більшість"
    assertEmptyMatch("зробити відкритим доступ");
    assertEmptyMatch("визнають регіональними облради");

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("визнають регіональною облради")).length);

    // conflicts with the case when plural is spread out across the sentence 
//    assertEquals(1, rule.match(langTool.getAnalyzedSentence("директор та середньої шкіл")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("скрутна справі")).length);
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("район імпозантних віл")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("із такою самого зневагою")).length);
    
    //TODO
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("на вибори само висуванцем")).length);
    

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("офіційний статистика"));
    assertEquals(1, matches.length);
//    assertEquals(Arrays.asList("неба"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("зелена яблуко."));
    assertEquals(1, matches.length);

// not disambiguated yet
//    matches = rule.match(langTool.getAnalyzedSentence("— робочій день."));
//    assertEquals(1, matches.length);

  }

  private void assertEmptyMatch(String text) throws IOException {
    assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(rule.match(langTool.getAnalyzedSentence(text))));
  }
  
}
