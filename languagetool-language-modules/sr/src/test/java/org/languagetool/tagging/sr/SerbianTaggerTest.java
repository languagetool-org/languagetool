package org.languagetool.tagging.sr;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Serbian;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class SerbianTaggerTest {

  private SerbianTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() throws Exception {
    tagger = new SerbianTagger();
    tokenizer = new WordTokenizer();
  }


  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Serbian());
  }


  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("најтабеларнијега", "најтабеларнијега/[табеларан]Agsmsayy|најтабеларнијега/[табеларан]Agsmsgy|најтабеларнијега/[табеларан]Agsnsgy", tokenizer, tagger);
  }
}