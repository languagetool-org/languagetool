/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski
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
package org.languagetool.rules.br;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Breton;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikBretonSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    final MorfologikBretonSpellerRule rule =
            new MorfologikBretonSpellerRule (TestTools.getMessages("br"), new Breton(), null, Collections.emptyList());

    RuleMatch[] matches;
    final JLanguageTool lt = new JLanguageTool(new Breton());

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Penaos emañ kont ganit?")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("C'hwerc'h merc'h gwerc'h war c'hwerc'h marc'h kalloc'h")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("C’hwerc’h merc’h gwerc‘h war c‘hwerc‘h marc'h kalloc‘h")).length);

    //words with hyphens are tokenized internally...
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Evel-just")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Barrek-tre eo LanguageTool")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("C'hwerc'h merc'h gwerc'h war c'hwerc'h marc'h kalloc'h")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("C’hwerc’h merc’h gwerc‘h war c‘hwerc‘h marc'h kalloc‘h")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Evel-just")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Evel-juste")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Barrek-tre eo LanguageTool")).length);

    //test for "LanguageTool":
    assertEquals(0, rule.match(lt.getAnalyzedSentence("LanguageTool!")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);

    //incorrect sentences:

    assertEquals(1, rule.match(lt.getAnalyzedSentence("Evel-juste")).length);

    matches = rule.match(lt.getAnalyzedSentence("Evel-juste"));

    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(5, matches[0].getFromPos());
    assertEquals(10, matches[0].getToPos());

    matches = rule.match(lt.getAnalyzedSentence("C’hreizhig-don"));

    assertEquals(1, matches.length);

    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(10, matches[0].getToPos());

    assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("a")).length);
  }

}
