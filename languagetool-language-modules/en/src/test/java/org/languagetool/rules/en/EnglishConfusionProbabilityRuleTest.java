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
package org.languagetool.rules.en;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EnglishConfusionProbabilityRuleTest {

  private final English english = new English();
  private final JLanguageTool lt = new JLanguageTool(english);
  
  private EnglishConfusionProbabilityRule rule;

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  @Test
  public void testConstructor() {
    new EnglishConfusionProbabilityRule(TestTools.getEnglishMessages(), new FakeLanguageModel(), Languages.getLanguageForShortCode("en"));
  }

  @Test
  @Ignore
  public void testRule() throws IOException {
    File indexDir = new File("/data/google-ngram-index");
    if (!indexDir.exists()) {
      throw new RuntimeException("ngram data not found at " + indexDir + ", get it at http://wiki.languagetool.org/finding-errors-using-big-data");
    }
    rule = new EnglishConfusionProbabilityRule(TestTools.getEnglishMessages(), new LuceneLanguageModel(indexDir), english);
    
    Replacement theirThere = new Replacement("there", "their");
    assertMatch("Is their a telephone anywhere?", theirThere);
    assertMatch("I can't remember how to go their.", theirThere);
    assertMatch("Can you please tell me why their seems to be two churches in every village?", theirThere);
    assertMatch("Why do American parents praise there children?", theirThere);
    assertMatch("The British supplied there native allies with muskets, gunpowder and advice.", theirThere);
    
    Replacement knowNow = new Replacement("know", "now");
    assertMatch("From know on let us study in the morning.", knowNow);
    assertMatch("I am from Hiroshima, but know I live in Tokyo.", knowNow);
    assertMatch("I didn't now where it came from.", knowNow);
    assertMatch("Let me now if I need to make any changes.", knowNow);
    
    Replacement fourFor = new Replacement("four", "for");
    assertMatch("This gives us a minimum date four the age of Afroasiatic.", fourFor);
    assertMatch("Agassi admitted that he used and tested positive four methamphetamine in 1997.", fourFor);
    assertMatch("Alabama has for of the world's largest stadiums.", fourFor);
    assertMatch("There are no male actors and the for leading actresses dubbed themselves in the Castilian version.", fourFor);
  }

  private void assertMatch(String errorInput, Replacement rep) throws IOException {
    assertMatch(errorInput, 1);
    String fixedInput;
    if (errorInput.matches(".*\\b" + rep.newsString + "\\b.*")) {
      fixedInput = errorInput.replaceFirst("\\b" + rep.newsString + "\\b", rep.oldString);
    } else {
      fixedInput = errorInput.replaceFirst("\\b" + rep.oldString + "\\b", rep.newsString);
    }
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
