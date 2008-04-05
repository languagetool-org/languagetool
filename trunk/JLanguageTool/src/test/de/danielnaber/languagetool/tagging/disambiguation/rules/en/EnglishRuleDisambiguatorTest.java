package de.danielnaber.languagetool.tagging.disambiguation.rules.en;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import de.danielnaber.languagetool.tagging.en.EnglishTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class EnglishRuleDisambiguatorTest extends TestCase {
  private EnglishTagger tagger;
  private WordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private EnglishRuleDisambiguator disambiguator;
  private DemoDisambiguator disamb2;
  
  public void setUp() {
    tagger = new EnglishTagger();
    tokenizer = new WordTokenizer();
    sentenceTokenizer = new SentenceTokenizer();
    disambiguator = new EnglishRuleDisambiguator();
    disamb2 = new DemoDisambiguator(); 
  }

  public void testChunker() throws IOException {
    TestTools.myAssert("I cannot have it.",
        "/[null]SENT_START I/[I]PRP  /[null]null cannot/[can]MD  /[null]null have/[have]VB  /[null]null it/[it]PRP ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("I cannot have it.",
        "/[null]SENT_START I/[I]PRP  /[null]null cannot/[can]MD  /[null]null have/[have]NN|have/[have]VB|have/[have]VBP  /[null]null it/[it]PRP ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("He is to blame.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null to/[to]IN|to/[to]TO  /[null]null blame/[blame]VB ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("He is to blame.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null to/[to]IN|to/[to]TO  /[null]null blame/[blame]JJ|blame/[blame]NN:UN|blame/[blame]VB|blame/[blame]VBP ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("He is well known.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null well/[well]RB  /[null]null known/[known]JJ ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("He is well known.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null well/[well]NN|well/[well]RB|well/[well]VB|well/[well]VBP  /[null]null known/[known]NN|known/[know]VBN ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disamb2);
    
  }

}


