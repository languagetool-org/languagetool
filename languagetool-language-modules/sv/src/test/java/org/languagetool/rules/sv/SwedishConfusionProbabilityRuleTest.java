/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sv;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SwedishConfusionProbabilityRuleTest {

  private final Language swedish = Languages.getLanguageForShortCode("sv");
  private final JLanguageTool lt = new JLanguageTool(swedish);
  
  private SwedishConfusionProbabilityRule rule;

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  @Test
  public void testConstructor() {
    new SwedishConfusionProbabilityRule(TestTools.getEnglishMessages(), new FakeLanguageModel(), swedish);
  }

  @Test
  @Ignore
  public void testRule() throws IOException {
    File indexDir = new File("/data/ngram-index/sv");
    if (!indexDir.exists()) {
      throw new RuntimeException("ngram data not found at " + indexDir + ", get more info at https://dev.languagetool.org/finding-errors-using-n-gram-data");
    }
    rule = new SwedishConfusionProbabilityRule(TestTools.getMessages("sv"), new LuceneLanguageModel(indexDir), swedish);
    
    Replacement majMaj = new Replacement("Maj", "maj");
    assertMatch("Från 15 Maj är det eldningsförbud.", majMaj);
    assertMatch("15 Maj är sista dag för antagningen.", majMaj);
    
    Replacement mmajMaj = new Replacement("majs", "Majs");
    assertMatch("Det är majs födelsedag idag.", mmajMaj);

    Replacement streStra = new Replacement("Sträck", "Streck");
    assertMatch("Sträcket är rakt.", streStra);
    
    Replacement straStre = new Replacement("streck", "sträck");
    assertMatch("I ett streck.", straStre);
        
    Replacement sanktSankta = new Replacement("S:t", "Sankt");
    assertMatch("S:t Johannesgatan är mitt i stan.", sanktSankta);

    Replacement sanktaStora = new Replacement("S:a", "Stora");
    assertMatch("S:a Karlsö blev naturreservat 1970.", sanktaStora);

    Replacement saSankta = new Replacement("S:a", "Sankta");
    assertMatch("S:a Klara kloster.", saSankta);

  }

  private void assertMatch(String errorInput, Replacement rep) throws IOException {
    assertMatch(errorInput, 1);
    String fixedInput = StringUtils.replaceOnce(errorInput, rep.oldString, rep.newsString);
    if (fixedInput.equals(errorInput)) {
      throw new RuntimeException("Could not fix sentence: '" + errorInput  + "' with " + rep);
    }
    assertMatch(fixedInput, 0);
  }

  private void assertMatch(String input, int expectedMatches) throws IOException {
    AnalyzedSentence errorSentence = lt.getAnalyzedSentence(input);
    RuleMatch[] matches = rule.match(errorSentence);
    assertThat("Got " + matches.length + " match(es) for: " + input, matches.length, is(expectedMatches));
  }

  static class Replacement {
    String oldString;
    String newsString;
    Replacement(String oldString, String newsString) {
      this.oldString = oldString;
      this.newsString = newsString;
    }
    @Override
    public String toString() {
      return oldString + "/" + newsString;
    }
  }
}
