package org.languagetool;

import org.junit.Test;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SentenceRangeTest {

    @Test
    public void testCorrectSentenceRange() {
        List<String> sentences = Arrays.asList(
                "Hallo \n\n",
                "Das ist ein neuer Satz.",
                "\n\n Mit \uFEFFSonderzeichen und allem.",
                "\n\n\n\n\n Satz mehreren Leerzeichen.",
                "Hier sind die Zeichen mal am Ende. \n\n\n");
        List<SentenceRange> ranges = SentenceRange.getRangesFromSentences(sentences);
        assertEquals(5, ranges.size() );
        SentenceRange sentenceRange1 = ranges.get(0);
        assertEquals(0, sentenceRange1.getFromPos());
        assertEquals(5, sentenceRange1.getToPos());
        SentenceRange sentenceRange2 = ranges.get(1);
        assertEquals(8, sentenceRange2.getFromPos());
    }
}
