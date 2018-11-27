package org.languagetool.tagging.de;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.AnalyzedTokenReadings;

public class SwissGermanTaggerTest {

	@Test
  public void testTagger() throws IOException {
    GermanTagger tagger = new SwissGermanTagger();

    AnalyzedTokenReadings aToken = tagger.lookup("gross");
    assertEquals("gross[groß/ADJ:PRD:GRU]", GermanTaggerTest.toSortedString(aToken));
    assertEquals("groß", aToken.getReadings().get(0).getLemma());

    AnalyzedTokenReadings aToken2 = tagger.lookup("Anmassung");
    assertEquals("Anmassung[Anmaßung/SUB:AKK:SIN:FEM, "
    		+ "Anmaßung/SUB:DAT:SIN:FEM, "
    		+ "Anmaßung/SUB:GEN:SIN:FEM, "
    		+ "Anmaßung/SUB:NOM:SIN:FEM]", GermanTaggerTest.toSortedString(aToken2));
    assertEquals("Anmaßung", aToken2.getReadings().get(0).getLemma());
  }
}
