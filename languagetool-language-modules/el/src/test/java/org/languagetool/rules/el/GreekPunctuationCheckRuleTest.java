/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.el;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Greek;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GreekPunctuationCheckRuleTest {

  @Test
  public void testRule() throws IOException {
		final GreekPunctuationCheckRule rule = new GreekPunctuationCheckRule();
		
		RuleMatch[] matches;
		JLanguageTool lt = new JLanguageTool(new Greek());
    
		matches = rule.match(lt.getAnalyzedSentence("Καλημέρα, χρόνια πολλά!"));
		assertEquals(0, matches.length);

		matches = rule.match(lt.getAnalyzedSentence("Κάνω μία επεξήγηση - η εξήγηση είναι μέσα στις παύλες - και συνεχίζω. "));
		assertEquals(0, matches.length);

		matches = rule.match(lt.getAnalyzedSentence("Μετά από αυτό, ησυχία... "));
		assertEquals(0, matches.length);

		matches = rule.match(lt.getAnalyzedSentence("Θα περάσεις από το σπίτι; "));
		assertEquals(0, matches.length);

		matches = rule.match(lt.getAnalyzedSentence("Τα φρούτα που μου αρέσουν είναι: μήλα, αχλάδια, πορτοκάλια. Εσένα; ")); 
		assertEquals(0, matches.length);

		matches = rule.match(lt.getAnalyzedSentence("Κάνω μία παύση· και συνεχίζω. "));  
		assertEquals(0, matches.length);

		matches = rule.match(lt.getAnalyzedSentence("Μετά από αυτό, ησυχία.."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals(".", matches[0].getSuggestedReplacements().get(0));

		matches = rule.match(lt.getAnalyzedSentence("Μετά από αυτό,, ησυχία... "));
		assertEquals(1, matches.length);

		matches = rule.match(lt.getAnalyzedSentence("Κάνω μία επεξήγηση-η εξήγηση είναι μέσα στις παύλες - και συνεχίζω. "));
		assertEquals(1, matches.length);
		
		matches = rule.match(lt.getAnalyzedSentence("Κανώ μία επεξήγηση - η εξήγηση είναι μέσα στις παύλες, και συνεχίζω. "));
		assertEquals(1, matches.length);
		
		matches = rule.match(lt.getAnalyzedSentence("Θα περάσεις από το σπίτι;;"));
		assertEquals(1, matches.length);
		
		matches = rule.match(lt.getAnalyzedSentence("Κάνω μία παύση·και συνεχίζω. "));  
		assertEquals(1, matches.length);
  }
}