package org.languagetool.languagemodel;

import org.junit.Test;
import org.languagetool.rules.ngrams.Probability;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class BaseLanguageModelExtraTest {

  /**
   * A simple concrete subclass of BaseLanguageModel for testing purposes.
   */
  static class TestLanguageModel extends BaseLanguageModel {

    @Override
    public long getCount(String token1) {
      // For testing: return 10 for token "a", otherwise 0.
      if ("a".equals(token1)) {
        return 10;
      }
      return 0;
    }

    @Override
    public long getCount(java.util.List<String> tokens) {
      // For testing: if tokens equals ["a"], return 10; if tokens equals ["a", "b"], return 5; otherwise return 0.
      if (tokens.size() == 1 && tokens.get(0).equals("a")) {
        return 10;
      }
      if (tokens.size() == 2 && tokens.get(0).equals("a") && tokens.get(1).equals("b")) {
        return 5;
      }
      return 0;
    }

    @Override
    public long getTotalTokenCount() {
      // For testing, assume total token count is 100.
      return 100;
    }

    @Override
    public void close() {
      // No resources to close in this test implementation.
    }
  }

  /**
   * Test getPseudoProbabilityStupidBackoff with a context that yields a nonzero count.
   * For context ["a", "b"]:
   *   - getCount(["a", "b"]) returns 5 and getCount(["a"]) returns 10,
   * so probability = 5/10 = 0.5 and coverage should be 1.0.
   */
  @Test
  public void testPseudoProbabilityStupidBackoffSuccess() {
    TestLanguageModel model = new TestLanguageModel();
    Probability prob = model.getPseudoProbabilityStupidBackoff(Arrays.asList("a", "b"));
    assertEquals(0.5, prob.getProb(), 0.0001);
    assertEquals(1.0f, prob.getCoverage(), 0.0001f);
  }

  /**
   * Test getPseudoProbabilityStupidBackoff with a context that never yields a nonzero count.
   * For context ["x", "y", "z"], all counts are 0, so the method should return a probability of 0.0 and coverage 0.0.
   */
  @Test
  public void testPseudoProbabilityStupidBackoffFail() {
    TestLanguageModel model = new TestLanguageModel();
    Probability prob = model.getPseudoProbabilityStupidBackoff(Arrays.asList("x", "y", "z"));
    assertEquals(0.0, prob.getProb(), 0.0001);
    assertEquals(0.0f, prob.getCoverage(), 0.0001f);
  }

  /**
   * Test getPseudoProbability with a single-word context.
   * For context ["a"]:
   *   - firstWordCount = getCount("a") = 10,
   * so probability p = (10 + 1) / (100 + 1) and coverage = 1.0.
   */
  @Test
  public void testPseudoProbabilitySingleWord() {
    TestLanguageModel model = new TestLanguageModel();
    Probability prob = model.getPseudoProbability(Collections.singletonList("a"));
    double expectedP = 11.0 / 101.0;
    assertEquals(expectedP, prob.getProb(), 0.0001);
    assertEquals(1.0f, prob.getCoverage(), 0.0001f);
  }

  /**
   * Test getPseudoProbability with an empty context.
   * This should throw an IndexOutOfBoundsException.
   */
  @Test(expected = IndexOutOfBoundsException.class)
  public void testPseudoProbabilityEmptyContext() {
    TestLanguageModel model = new TestLanguageModel();
    model.getPseudoProbability(Collections.emptyList());
  }
}
