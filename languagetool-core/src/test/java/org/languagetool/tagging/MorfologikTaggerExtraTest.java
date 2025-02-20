package org.languagetool.tagging;

import morfologik.stemming.Dictionary;
import org.junit.Test;
import static org.junit.Assert.*;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MorfologikTaggerExtraTest {

  /**
   * Helper method to load a test dictionary from resource.
   * This assumes that the resource "/org/languagetool/tagging/test.dict" exists.
   */
  private Dictionary loadTestDictionary() throws IOException {
    URL url = MorfologikTaggerTest.class.getResource("/org/languagetool/tagging/test.dict");
    assertNotNull("Test dictionary resource should exist", url);
    return Dictionary.read(url);
  }

  /**
   * Test the getter and setter for internTags.
   * Initially, internTags should be false; after setting to true, getInternTags should return true.
   */
  @Test
  public void testSetAndGetInternTags() throws IOException {
    Dictionary dict = loadTestDictionary();
    // Create a tagger using the constructor with Dictionary and internTags flag.
    MorfologikTagger tagger = new MorfologikTagger(dict, false);
    assertFalse("Initial internTags should be false", tagger.getInternTags());
    tagger.setInternTags(true);
    assertTrue("After setting, internTags should be true", tagger.getInternTags());
  }

  /**
   * Test that when internTags is enabled, the POS tag returned is interned.
   */
  @Test
  public void testTagWithInternTags() throws IOException {
    Dictionary dict = loadTestDictionary();
    // Create a tagger with internTags set to true.
    MorfologikTagger tagger = new MorfologikTagger(dict, true);
    List<TaggedWord> result = tagger.tag("lowercase");
    // If result is not empty, check that the POS tag is interned.
    if (!result.isEmpty()) {
      String posTag = result.get(0).getPosTag();
      assertNotNull("POS tag should not be null", posTag);
      // Check that the POS tag instance is the same as its interned version.
      assertTrue("POS tag should be interned when internTags is true", posTag == posTag.intern());
    }
  }

  /**
   * Test that an invalid dictionary URL causes a RuntimeException when calling tag().
   * This simulates a failure in reading the dictionary.
   */
  @Test(expected = RuntimeException.class)
  public void testTagThrowsRuntimeExceptionForInvalidDictionary() throws IOException {
    // Create a tagger with an invalid dictionary URL to force an IOException.
    URL invalidUrl = new URL("file:///nonexistent.dict");
    MorfologikTagger tagger = new MorfologikTagger(invalidUrl);
    // Calling tag() should trigger an IOException in getDictionary(), which is wrapped as a RuntimeException.
    tagger.tag("test");
  }
}
