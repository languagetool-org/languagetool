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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class TokenAgreementRuleTest {

  @Test
  public void testRule() throws IOException {
    TokenAgreementRule rule = new TokenAgreementRule(TestTools.getMessages("Ukrainian"));

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

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("що то була за людина")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("що за людина")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("що балотувався за цім округом")).length);

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("окрім як українці"))));

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("за двісті метрів"))));

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("переходить у Фрідріх Штрассе"))));

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("від мінус 1 до плюс 1"))));

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("через років 10"))));
    
    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("через років 10"))));

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("співпрацювати із собі подібними"))));

    assertEquals(new ArrayList<RuleMatch>(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("через усім відомі причини"))));
    

//    assertEquals(0, rule.match(langTool.getAnalyzedSentence("як у Конана Дойла")).length); //TODO
//    assertEquals(0, rule.match(langTool.getAnalyzedSentence("як у Конану Дойла")).length);
//    assertEquals(0, rule.match(langTool.getAnalyzedSentence("як у Конан Дойла")).length);
    
    //incorrect sentences:

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("без небу"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("неба"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("по нервам"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("нервах", "нерви"), matches[0].getSuggestedReplacements());
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("в п'ятьом людям")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("в понад п'ятьом людям")).length);
  }

}
