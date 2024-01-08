package org.languagetool.tagging.disambiguation;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.tagging.xx.DemoTagger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class MultiWordChunkerTest {

  private final JLanguageTool lt = new JLanguageTool(new FakeLanguage() {
    public org.languagetool.tagging.Tagger createDefaultTagger() {
      return new DemoTagger() {
        public java.util.List<AnalyzedTokenReadings> tag(java.util.List<String> sentenceTokens) {
          List<AnalyzedTokenReadings> tokenReadings = super.tag(sentenceTokens);
          for (AnalyzedTokenReadings readings : tokenReadings) {
            if( readings.isWhitespace() )
              continue;
            
            readings.addReading(new AnalyzedToken(readings.getToken(), "FakePosTag", readings.getToken()), "");
          }
          return tokenReadings;
        }
      };
    }
  });

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testDisambiguate1() throws IOException {
    MultiWordChunker multiWordChunker = new MultiWordChunker("/yy/multiwords.txt", true, true, true);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    assertFalse(tokens[3].getReadings().toString().contains("adv"));
    assertTrue(tokens[5].getReadings().toString().contains("</adv>"));

    assertTrue(tokens[1].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[3].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

  @Test
  public void testDisambiguate2() throws IOException {
    MultiWordChunker2 multiWordChunker = new MultiWordChunker2("/yy/multiwords.txt", true);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("Ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    assertTrue(tokens[3].getReadings().toString().contains("<adv>"));
    assertTrue(tokens[5].getReadings().toString().contains("<adv>"));

    assertTrue(tokens[1].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[3].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

  @Test
  public void testDisambiguate2NoMatch() throws IOException {
    MultiWordChunker2 multiWordChunker = new MultiWordChunker2("/yy/multiwords.txt", true);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ahh for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    assertFalse(tokens[1].getReadings().toString().contains("<adv>"));
  }
  
  @Test
  public void testDisambiguate2RemoveOtherReadings() throws IOException {
    MultiWordChunker2 multiWordChunker = new MultiWordChunker2("/yy/multiwords.txt", true);
    multiWordChunker.setRemoveOtherReadings(true);
    multiWordChunker.setWrapTag(false);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    assertTrue(tokens[1].getReadings().toString().contains("adv"));
    assertTrue(tokens[3].getReadings().toString().contains("adv"));
    assertTrue(tokens[5].getReadings().toString().contains("adv"));

    assertFalse(tokens[1].getReadings().toString().contains("FakePosTag"));
    assertFalse(tokens[3].getReadings().toString().contains("FakePosTag"));
    assertFalse(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

  @Test
  public void testLettercaseVariants() throws IOException {
    MultiWordChunker multiWordChunker = new MultiWordChunker("/yy/multiwords.txt", true, true, true);
    Map<String, AnalyzedToken> map = new HashMap<>();
    map.put("rhythm and blues", new AnalyzedToken("rhythm and blues", "NCMS000_", "rhythm and blues"));
    map.put("Vênus de Milo", new AnalyzedToken("Vênus de Milo", "NCFSS00_", "Vênus de Milo"));
    List<String> tokenVariantsRnB = multiWordChunker.getTokenLettercaseVariants("rhythm and blues", map);
    assertTrue(tokenVariantsRnB.contains("Rhythm and blues"));  // simple upcase of first word
    assertTrue(tokenVariantsRnB.contains("Rhythm And Blues"));  // naïve titlecase
    assertTrue(tokenVariantsRnB.contains("Rhythm and Blues"));  // smarter titlecase
    assertTrue(tokenVariantsRnB.contains("RHYTHM AND BLUES"));  // all caps
    List<String> tokenVariantsVenus = multiWordChunker.getTokenLettercaseVariants("Vênus de Milo", map);
    assertFalse(tokenVariantsVenus.contains("Vênus De Milo"));  // naïve titlecase
    assertFalse(tokenVariantsVenus.contains("vênus de milo"));  // downcased
    assertTrue(tokenVariantsVenus.contains("VÊNUS DE MILO"));   // all caps
  }

}
