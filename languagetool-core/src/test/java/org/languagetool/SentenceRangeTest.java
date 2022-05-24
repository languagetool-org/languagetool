package org.languagetool;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SentenceRangeTest {

    @Test
    public void testCorrectSentenceRange() {
        // An sentence list as it would come from a sentenceTokenizer        
        List<String> sentences = Arrays.asList(
                "Hallo,\n\n",
                "Das ist ein neuer Satz.",
                "\n\nEin Satz mit \uFEFFSonderzeichen.",
                "\n\n\n\n\nSatz mehreren Leerzeichen.",
                " Hier sind die Zeichen mal am Ende.\n\n\n",
                "\n\n\n\uFeFFNoch ein Satz.\n\n\n\n");
        String text = String.join("", sentences);
        List<SentenceRange> ranges = SentenceRange.getRangesFromSentences(sentences);
        assertEquals(6, ranges.size());

        SentenceRange sr1 = ranges.get(0);
        assertEquals(0, sr1.getFromPos());
        assertEquals(6, sr1.getToPos());

        SentenceRange sr2 = ranges.get(1);
        assertEquals(8, sr2.getFromPos());
        assertEquals(31, sr2.getToPos());

        SentenceRange sr3 = ranges.get(2);
        assertEquals(33, sr3.getFromPos());
        assertEquals(61, sr3.getToPos());

        SentenceRange sr4 = ranges.get(3);
        assertEquals(66, sr4.getFromPos());
        assertEquals(92, sr4.getToPos());

        SentenceRange sr5 = ranges.get(4);
        assertEquals(93, sr5.getFromPos());
        assertEquals(127, sr5.getToPos());

        SentenceRange sr6 = ranges.get(5);
        assertEquals(133, sr6.getFromPos());
        assertEquals(148, sr6.getToPos());
        
        StringBuilder sb = new StringBuilder();
        
        //Check if we get the trimmed sentences with the ranges from text
        for (SentenceRange sr :ranges) {
            sb.append(text, sr.getFromPos(), sr.getToPos());
        }
        assertEquals("Hallo,Das ist ein neuer Satz.Ein Satz mit \uFEFFSonderzeichen.Satz mehreren Leerzeichen.Hier sind die Zeichen mal am Ende.\uFEFFNoch ein Satz.", sb.toString());
    }
}
