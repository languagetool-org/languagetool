/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package de.danielnaber.languagetool.tokenizers;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;

/**
 * @author Daniel Naber
 */
public class SRXSentenceTokenizerTest extends TestCase {

  // accept \n as paragraph:
  private SentenceTokenizer stokenizer = new SRXSentenceTokenizer("en");
  // accept only \n\n as paragraph:
  private SentenceTokenizer stokenizer2 = new SRXSentenceTokenizer("en");
  
  public void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);  
    stokenizer2.setSingleLineBreaksMarksParagraph(false);  
  }

  // NOTE: sentences here need to end with a space character so they
  // have correct whitespace when appended:
  public void testTokenize() {
    // incomplete sentences, need to work for on-thy-fly checking of texts:
    testSplit(new String[] { "Here's a" });
    testSplit(new String[] { "Here's a sentence. ", "And here's one that's not comp" });

    testSplit(new String[] { "This is a sentence. " });
    testSplit(new String[] { "This is a sentence. ", "And this is another one." });
    testSplit(new String[] { "This is a sentence.", "Isn't it?", "Yes, it is." });
    testSplit(new String[] { "This is e.g. Mr. Smith, who talks slowly...",
        "But this is another sentence." });
    testSplit(new String[] { "Chanel no. 5 is blah." });
    testSplit(new String[] { "Mrs. Jones gave Peter $4.5, to buy Chanel No 5.",
        "He never came back." });
    testSplit(new String[] { "On p. 6 there's nothing. ", "Another sentence." });
    testSplit(new String[] { "Leave me alone!, he yelled. ", "Another sentence." });
    testSplit(new String[] { "\"Leave me alone!\", he yelled." });
    testSplit(new String[] { "'Leave me alone!', he yelled. ", "Another sentence." });
    testSplit(new String[] { "'Leave me alone!,' he yelled. ", "Another sentence." });
    testSplit(new String[] { "This works on the phrase level, i.e. not on the word level." });
    testSplit(new String[] { "Let's meet at 5 p.m. in the main street." });
    testSplit(new String[] { "James comes from the U.K. where he worked as a programmer." });
    testSplit(new String[] { "Don't split strings like U.S.A. please." });
    testSplit(new String[] { "Don't split strings like U. S. A. either." });
    testSplit(new String[] { "Don't split... ", "Well you know. ", "Here comes more text." });
    testSplit(new String[] { "Don't split... well you know. ", "Here comes more text." });
    testSplit(new String[] { "The \".\" should not be a delimiter in quotes." });
    testSplit(new String[] { "\"Here he comes!\" she said." });
    testSplit(new String[] { "\"Here he comes!\", she said." });
    testSplit(new String[] { "\"Here he comes.\" ", "But this is another sentence." });
    testSplit(new String[] { "\"Here he comes!\". ", "That's what he said." });
    testSplit(new String[] { "The sentence ends here. ", "(Another sentence.)" });
    testSplit(new String[] { "The sentence (...) ends here." });
    testSplit(new String[] { "The sentence [...] ends here." });
    testSplit(new String[] { "The sentence ends here (...). ", "Another sentence." });
    // previously known failed but not now :)
    testSplit(new String[]{"He won't. ", "Really."});
    testSplit(new String[]{"He will not. ", "Really."});
    testSplit(new String[] { "He won't go. ", "Really." });
    testSplit(new String[] { "He won't say no.", "Not really." });
    testSplit(new String[] { "He won't say No.", "Not really." });
    testSplit(new String[] { "He won't say no. 5 is better. ", "Not really." });
    testSplit(new String[] { "He won't say No. 5 is better. ", "Not really." });
    testSplit(new String[] { "They met at 5 p.m. on Thursday." });
    testSplit(new String[] { "They met at 5 p.m. ", "It was Thursday." });
    testSplit(new String[] { "This is it: a test." });
    // one/two returns = paragraph = new sentence:
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\n", "Really." }, stokenizer);
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\nReally." }, stokenizer2);
    // Missing space after sentence end:
    testSplit(new String[] { "James is from the Ireland!", "He lives in Spain now." });
    // From the abbreviation list:
    testSplit(new String[] { "Jones Bros. have built a succesful company." });
    // parentheses:
    testSplit(new String[] { "It (really!) works." });
    testSplit(new String[] { "It [really!] works." });
    testSplit(new String[] { "It works (really!). ", "No doubt." });
    testSplit(new String[] { "It works [really!]. ", "No doubt." });
    testSplit(new String[] { "It really(!) works well." });
    testSplit(new String[] { "It really[!] works well." });
  }

  private void testSplit(String[] sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
