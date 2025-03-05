/**
 * SWE 261P Software Testing Project By Kenny Chen, Haitong Yan, Jiacheng Zhuo
 */
package org.languagetool.rules.en;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import org.junit.Test;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.mockito.Mockito;

public class AdverbFilterTest {

   @Test
   public void testAcceptRuleMatch() {
      AdverbFilter adverbFilter = new AdverbFilter();
      RuleMatch mockRuleMatch = mock(RuleMatch.class);
      Map<String, String> arguments = new HashMap<>();
      arguments.put("adverb", "cozily");
      arguments.put("noun", "space");

      AnalyzedTokenReadings[] patternTokens = new AnalyzedTokenReadings[0];
      List<Integer> tokenPositions = List.of();

      RuleMatch result = adverbFilter.acceptRuleMatch(mockRuleMatch, arguments, 0, patternTokens,
            tokenPositions);

      Mockito.verify(mockRuleMatch).setSuggestedReplacement("cozy space");
      assertEquals(mockRuleMatch, result);
   }
}
