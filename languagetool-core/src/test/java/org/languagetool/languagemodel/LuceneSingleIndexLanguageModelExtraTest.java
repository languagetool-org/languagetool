package org.languagetool.languagemodel;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LuceneSingleIndexLanguageModelExtraTest {

  /**
   * Test validateDirectory with a non-existent directory.
   * Expect a RuntimeException indicating that the directory is not found.
   */
  @Test(expected = RuntimeException.class)
  public void testValidateDirectoryNonExistent() {
    File fakeDir = new File("nonexistent_directory");
    LuceneSingleIndexLanguageModel.validateDirectory(fakeDir);
  }

  /**
   * Test validateDirectory with a directory that exists but has no "1grams", "2grams", "3grams" subdirectories.
   * Expect a RuntimeException.
   */
  @Test(expected = RuntimeException.class)
  public void testValidateDirectoryMissingSubDirs() {
    // Create a temporary directory without required subdirectories
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "emptyTestDir");
    tempDir.mkdir();
    try {
      LuceneSingleIndexLanguageModel.validateDirectory(tempDir);
    } finally {
      tempDir.delete();
    }
  }

  /**
   * Test getCount(List<String>) with a context whose size exceeds maxNgram.
   * Using the constructor that accepts maxNgram, an exception is expected.
   */
  @Test(expected = RuntimeException.class)
  public void testGetCountExceedingMaxNgram() {
    // Create an instance with maxNgram = 3.
    LuceneSingleIndexLanguageModel model = new LuceneSingleIndexLanguageModel(3);
    // Provide a list with 4 tokens, which should exceed maxNgram.
    model.getCount(Arrays.asList("a", "b", "c", "d"));
  }

  /**
   * Test getCount(String) with a null token.
   * Expect a NullPointerException due to Objects.requireNonNull.
   */
  @Test(expected = NullPointerException.class)
  public void testGetCountNullToken() {
    LuceneSingleIndexLanguageModel model = new LuceneSingleIndexLanguageModel(3);
    model.getCount((String) null);
  }

  /**
   * Test toString method.
   * When constructed with the int constructor, no indexes are added so the internal list is empty.
   * Expected output is "[]".
   */
  @Test
  public void testToStringReturnsEmptyIndexList() {
    LuceneSingleIndexLanguageModel model = new LuceneSingleIndexLanguageModel(3);
    String str = model.toString();
    assertEquals("[]", str);
  }

  /**
   * Test the clearCaches method.
   * Ensure that calling clearCaches() does not throw any exceptions.
   */
  @Test
  public void testClearCaches() {
    LuceneSingleIndexLanguageModel.clearCaches();
    // If no exception is thrown, the test passes.
    assertTrue(true);
  }
}
