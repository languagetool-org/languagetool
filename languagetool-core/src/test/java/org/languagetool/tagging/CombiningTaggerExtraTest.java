package org.languagetool.tagging;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.languagetool.JLanguageTool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CombiningTaggerExtraTest {

  /**
   * Test that getRemovalTagger returns the expected removal tagger.
   */
  @Test
  public void testGetRemovalTagger() throws IOException {
    CombiningTagger tagger = getCombiningTagger(false, "/xx/removed.txt");
    assertNotNull("Removal tagger should not be null", tagger.getRemovalTagger());
  }

  /**
   * Test fallback behavior when the second tagger returns an empty result.
   * In this case, only the first tagger's result should be used.
   */
  @Test
  public void testFallbackToTagger1() throws Exception {
    // Create a stub for tagger2 that always returns an empty list.
    WordTagger emptyTagger = new WordTagger() {
      @Override
      public List<TaggedWord> tag(String word) {
        return java.util.Collections.emptyList();
      }
    };
    // Use a ManualTagger for tagger1 based on a simple input.
    ManualTagger tagger1 = new ManualTagger(new ByteArrayInputStream("fullform\nbaseform1/POSTAG1".getBytes(StandardCharsets.UTF_8)));
    CombiningTagger tagger = new CombiningTagger(tagger1, emptyTagger, false);
    List<TaggedWord> result = tagger.tag("fullform");
    String asString = getAsString(result);
    assertTrue("Fallback to tagger1 should yield tag 'baseform1/POSTAG1'", asString.contains("baseform1/POSTAG1"));
  }

  /**
   * Test overwrite behavior: when overwriteWithSecondTagger is true and both taggers return results,
   * only the second tagger's result should be used.
   */
  @Test
  public void testOverwriteBehaviorWithBothResults() throws Exception {
    CombiningTagger tagger = getCombiningTagger(true, null);
    List<TaggedWord> result = tagger.tag("fullform");
    // With overwrite true, even if both taggers produce output, only tagger2's output should remain.
    assertEquals("Overwrite behavior should yield one result", 1, result.size());
    String asString = getAsString(result);
    assertTrue("Result should only contain tagger2's output", asString.contains("baseform2/POSTAG2"));
  }

  /**
   * Test removal behavior: when a removal tagger is provided, its results should be removed from the combined output.
   */
  @Test
  public void testRemovalRemovesResults() throws Exception {
    // Create a removal tagger that returns a tag that should be removed.
    ManualTagger removalTagger = new ManualTagger(new ByteArrayInputStream("fullform\nbaseform2/POSTAG2".getBytes(StandardCharsets.UTF_8)));
    // Use standard ManualTagger instances for tagger1 and tagger2.
    ManualTagger tagger1 = new ManualTagger(new ByteArrayInputStream("fullform\nbaseform1/POSTAG1".getBytes(StandardCharsets.UTF_8)));
    ManualTagger tagger2 = new ManualTagger(new ByteArrayInputStream("fullform\nbaseform2/POSTAG2".getBytes(StandardCharsets.UTF_8)));
    CombiningTagger tagger = new CombiningTagger(tagger1, tagger2, removalTagger, false);
    List<TaggedWord> result = tagger.tag("fullform");
    String asString = getAsString(result);
    // Expect that removalTagger's result ("baseform2/POSTAG2") is removed, leaving tagger1's result.
    assertFalse("Result should not contain the removed tag", asString.contains("baseform2/POSTAG2"));
    assertTrue("Result should contain tagger1's output", asString.contains("baseform1/POSTAG1"));
  }

  // Utility method similar to the one in CombiningTaggerTest.
  private CombiningTagger getCombiningTagger(boolean overwrite, String removalPath) throws IOException {
    ManualTagger tagger1 = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added1.txt"));
    ManualTagger tagger2 = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added2.txt"));
    ManualTagger removalTagger = null;
    if (removalPath != null) {
      removalTagger = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream(removalPath));
    }
    return new CombiningTagger(tagger1, tagger2, removalTagger, overwrite);
  }

  private String getAsString(List<TaggedWord> result) {
    StringBuilder sb = new StringBuilder();
    for (TaggedWord taggedWord : result) {
      sb.append(taggedWord.getLemma());
      sb.append('/');
      sb.append(taggedWord.getPosTag());
      sb.append('\n');
    }
    return sb.toString();
  }
}
