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
import java.util.List;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class TokenAgreementRuleTest {

  @Test
  public void testRule() throws IOException {
    TokenAgreementRule rule = new TokenAgreementRule(TestTools.getMessages("uk"));

    JLanguageTool langTool = new JLanguageTool(new Ukrainian());

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("без повного")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("без неба")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("по авеню")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("що за ганебна непослідовність?")).length);

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("щодо власне людини"))));
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("у загалом симпатичній повістині")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("понад половина людей")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("з понад ста людей")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("по нервах")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("з особливою увагою")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("щодо бодай гіпотетичної здатності")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("хто їде на заробітки за кордон")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("піти в президенти")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("піти межі люди")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("що то була за людина")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("що за людина")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("що балотувався за цім округом")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("на дому")).length);

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("окрім як українці"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("за двісті метрів"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("переходить у Фрідріх Штрассе"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("від мінус 1 до плюс 1"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("до мінус сорока градусів"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("до мінус шістдесяти"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("через років 10"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("на хвилин 9-10"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("співпрацювати із собі подібними"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("через усім відомі причини"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("через нікому не відомі причини"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("прийшли до ВАТ «Кривий Ріг цемент»"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("від А до Я"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("до та після"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("до схід сонця"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("до НАК «Надра України»"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("призвів до значною мірою демократичного середнього класу"))));
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("Вони замість Андрій вибрали Юрій"))));

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("призвів до значною мірою демократичному середньому класу")).length);

//    assertEquals(0, rule.match(langTool.getAnalyzedSentence("як у Конана Дойла")).length); //TODO
//    assertEquals(0, rule.match(langTool.getAnalyzedSentence("як у Конану Дойла")).length);
//    assertEquals(0, rule.match(langTool.getAnalyzedSentence("як у Конан Дойла")).length);
    
    //incorrect sentences:

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("без небу"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("неба"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("не в останню чергу через    корупцією, міжрелігійну ворожнечу"));
    assertEquals(1, matches.length);

    matches = rule.match(langTool.getAnalyzedSentence("по нервам"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(3, matches[0].getFromPos());
    assertEquals(9, matches[0].getToPos());
    assertEquals(Arrays.asList("нервах", "нерви"), matches[0].getSuggestedReplacements());
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("в п'ятьом людям")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("в понад п'ятьом людям")).length);

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

    matches = rule.match(langTool.getAnalyzedSentence("по церковним канонам"));
    // check match positions:
    assertEquals(1, matches.length);

//    matches = rule.match(langTool.getAnalyzedSentence("колега з Мінську"));
//    System.out.println(langTool.getAnalyzedSentence("колега з Мінську"));
//    // check match positions:
//    assertEquals(1, matches.length);

  }
  
  @Test
  public void testSpecialChars() throws IOException {
    TokenAgreementRule rule = new TokenAgreementRule(TestTools.getMessages("uk"));

    JLanguageTool langTool = new JLanguageTool(new Ukrainian());

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("по не́рвам, по мо\u00ADстам, по воротам"));
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
    assertEquals(Arrays.asList("воротах", "ворота"), matches[2].getSuggestedReplacements());
  }

}
