/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.pl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class PolishWordRepeatRuleTest {

  @Test
  public void testRule() throws IOException {
    final PolishWordRepeatRule rule = new PolishWordRepeatRule(TestTools.getEnglishMessages());
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Polish());
    //correct
    matches = rule.match(lt.getAnalyzedSentence("To jest zdanie próbne."));
    assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("On tak się bardzo nie martwił, bo przecież musiał się umyć."));
    assertEquals(0, matches.length);
    //repeated prepositions, don't count'em
    matches = rule.match(lt.getAnalyzedSentence("Na dyskotece tańczył jeszcze, choć był na bani."));
    assertEquals(0, matches.length);
    // sf bug report:
    matches = rule.match(lt.getAnalyzedSentence("Żadnych „ale”."));
    assertEquals(0, matches.length);
    //incorrect
    matches = rule.match(lt.getAnalyzedSentence("Był on bowiem pięknym strzelcem bowiem."));
    assertEquals(1, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("Mówiła długo, żeby tylko mówić długo."));
    assertEquals(2, matches.length);
  }

}
