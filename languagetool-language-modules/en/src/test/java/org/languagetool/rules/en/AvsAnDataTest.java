package org.languagetool.rules.en;

import static org.junit.Assert.assertNotEquals;

import java.util.Set;

import org.junit.Test;

public class AvsAnDataTest {
    @Test
    public void testLoadWordsForADet(){
        AvsAnData a = new AvsAnData();
        Set<String> aWords = a.loadWords("/en/det_a.txt");
        assertNotEquals(aWords.size(), 0);
    }

    @Test
    public void testLoadWordsForAnDet(){
        AvsAnData a = new AvsAnData();
        Set<String> anWords = a.loadWords("/en/det_an.txt");
        assertNotEquals(anWords.size(), 0);
    }
}
