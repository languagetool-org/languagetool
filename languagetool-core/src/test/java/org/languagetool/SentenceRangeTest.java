package org.languagetool;

import org.junit.Test;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/* This tests the */
public class SentenceRangeTest {
  @Test
  public void testCorrectSentenceRange() {
    // Create a sentence list.
    List<String> sentences = Arrays.asList(
      "Hello,\n\n",
      "Where art thou.",
      "\n\nI'm in \uFEFF Halifax.",
      "\n\n\n\n\nIt's snowing today.",
      " Would love to have a ice cream from black bear.\n\n\n",
      "\n\n\n\uFeFFOk bye.\n\n\n\n");

    // Add the sentences to create a text
    String text = String.join("", sentences);

    // Generate sentence ranges
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(text).build();
    List<SentenceRange> ranges = SentenceRange.getRangesFromSentences(annotatedText, sentences);

    // Check if the correct number of ranges is generated
    assertEquals(6, ranges.size());

    // Test each sentence range
    SentenceRange sr1 = ranges.get(0);
    assertEquals(0, sr1.getFromPos());
    assertEquals(6, sr1.getToPos());

    SentenceRange sr2 = ranges.get(1);
    assertEquals(8, sr2.getFromPos());
    assertEquals(23, sr2.getToPos());

    SentenceRange sr3 = ranges.get(2);
    assertEquals(25, sr3.getFromPos());
    assertEquals(42, sr3.getToPos());

    SentenceRange sr4 = ranges.get(3);
    assertEquals(47, sr4.getFromPos());
    assertEquals(66, sr4.getToPos());

    SentenceRange sr5 = ranges.get(4);
    assertEquals(67, sr5.getFromPos());
    assertEquals(114, sr5.getToPos());

    SentenceRange sr6 = ranges.get(5);
    assertEquals(120, sr6.getFromPos());
    assertEquals(128, sr6.getToPos());

    StringBuilder sb = new StringBuilder();

    //Check if we get the trimmed sentences with the ranges from text
    for (SentenceRange sr : ranges) {
      sb.append(text, sr.getFromPos(), sr.getToPos());
    }

    // Check if the text extracted from the ranges matches the expected result
    assertEquals("Hello,Where art thou.I'm in \uFEFF Halifax.It's snowing today.Would love to have a ice cream from black bear.\uFEFFOk bye.", sb.toString());
  }

  @Test
  public void testNotEquals() {
    // Create two SentenceRange objects with different fromPos and toPos
    SentenceRange range1 = new SentenceRange(10, 20);
    SentenceRange range2 = new SentenceRange(5, 15);

    // Test if the equals method correctly identifies them as not equal
    assertNotEquals(range1, range2);
  }

  @Test
  public void testHashCode() {
    // Create a SentenceRange object
    SentenceRange range = new SentenceRange(10, 20);

    // Calculate the expected hash code
    int expectedHashCode = 31 * (31 + 10) + 20;

    // Test if the hashCode method returns the expected hash code
    assertEquals(expectedHashCode, range.hashCode());
  }
}
