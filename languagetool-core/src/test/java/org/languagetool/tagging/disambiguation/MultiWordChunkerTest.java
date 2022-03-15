package org.languagetool.tagging.disambiguation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.*;
import org.languagetool.tagging.xx.DemoTagger;

import java.io.IOException;
import java.util.List;



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

  @BeforeEach
  public void setUp() throws Exception {
  }

  @Test
  public void testDisambiguate1() throws IOException {
    MultiWordChunker multiWordChunker = new MultiWordChunker("/yy/multiwords.txt", true, true);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    Assertions.assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    Assertions.assertFalse(tokens[3].getReadings().toString().contains("adv"));
    Assertions.assertTrue(tokens[5].getReadings().toString().contains("</adv>"));

    Assertions.assertTrue(tokens[1].getReadings().toString().contains("FakePosTag"));
    Assertions.assertTrue(tokens[3].getReadings().toString().contains("FakePosTag"));
    Assertions.assertTrue(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

  @Test
  public void testDisambiguate2() throws IOException {
    MultiWordChunker2 multiWordChunker = new MultiWordChunker2("/yy/multiwords.txt", true);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("Ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    Assertions.assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    Assertions.assertTrue(tokens[3].getReadings().toString().contains("<adv>"));
    Assertions.assertTrue(tokens[5].getReadings().toString().contains("<adv>"));

    Assertions.assertTrue(tokens[1].getReadings().toString().contains("FakePosTag"));
    Assertions.assertTrue(tokens[3].getReadings().toString().contains("FakePosTag"));
    Assertions.assertTrue(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

  @Test
  public void testDisambiguate2NoMatch() throws IOException {
    MultiWordChunker2 multiWordChunker = new MultiWordChunker2("/yy/multiwords.txt", true);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ahh for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    Assertions.assertFalse(tokens[1].getReadings().toString().contains("<adv>"));
  }
  
  @Test
  public void testDisambiguate2RemoveOtherReadings() throws IOException {
    MultiWordChunker2 multiWordChunker = new MultiWordChunker2("/yy/multiwords.txt", true);
    multiWordChunker.setRemoveOtherReadings(true);
    multiWordChunker.setWrapTag(false);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    Assertions.assertTrue(tokens[1].getReadings().toString().contains("adv"));
    Assertions.assertTrue(tokens[3].getReadings().toString().contains("adv"));
    Assertions.assertTrue(tokens[5].getReadings().toString().contains("adv"));

    Assertions.assertFalse(tokens[1].getReadings().toString().contains("FakePosTag"));
    Assertions.assertFalse(tokens[3].getReadings().toString().contains("FakePosTag"));
    Assertions.assertFalse(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

}
