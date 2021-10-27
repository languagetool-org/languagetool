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
package org.languagetool.tokenizers;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.English;

public class EnglishSRXSentenceTokenizerTest {

  // accept \n as paragraph:
  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(new English());
  // accept only \n\n as paragraph:
  private final SentenceTokenizer stokenizer2 = new SRXSentenceTokenizer(new English());

  @Before
  public void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);  
    stokenizer2.setSingleLineBreaksMarksParagraph(false);  
  }

  // NOTE: sentences here need to end with a space character so they
  // have correct whitespace when appended:
  @Test
  public void testTokenize() {
    // incomplete sentences, need to work for on-thy-fly checking of texts:
    testSplit("What is the I.S?");
    testSplit("Where are the I.S and the M.Z notes? ");
    testSplit("Here's a");
    testSplit("Here's a sentence. ", "And here's one that's not comp");
    testSplit("Or did you install it (i.e. MS Word) yourself?");

    testSplit("This is a sentence. ");
    testSplit("This is a sentence. ", "And this is another one.");
    testSplit("This is it. ", "and this is another sentence.");
    testSplit("This is a sentence. ", "and this is another sentence.");
    testSplit("This is a sentence.", "Isn't it?", "Yes, it is.");
    testSplit("This is e.g. Mr. Smith, who talks slowly...",
            "But this is another sentence.");
    testSplit("Chanel no. 5 is blah.");
    testSplit("Mrs. Jones gave Peter $4.5, to buy Chanel No 5.",
            "He never came back.");
    testSplit("On p. 6 there's nothing. ", "Another sentence.");
    testSplit("Leave me alone!, he yelled. ", "Another sentence.");
    testSplit("\"Leave me alone!\", he yelled.");
    testSplit("'Leave me alone!', he yelled. ", "Another sentence.");
    testSplit("'Leave me alone!,' he yelled. ", "Another sentence.");
    testSplit("This works on the phrase level, i.e. not on the word level.");
    testSplit("Let's meet at 5 p.m. in the main street.");
    testSplit("James comes from the U.K. where he worked as a programmer.");
    testSplit("Don't split strings like U.S.A. please.");
    testSplit("Hello ( Hi! ) my name is Chris.");
    testSplit("Don't split strings like U. S. A. either.");
    testSplit("Don't split strings like U.S.A either.");
    testSplit("Don't split... ", "Well you know. ", "Here comes more text.");
    testSplit("Don't split... well you know. ", "Here comes more text.");
    testSplit("The \".\" should not be a delimiter in quotes.");
    testSplit("\"Here he comes!\" she said.");
    testSplit("\"Here he comes!\", she said.");
    testSplit("\"Here he comes.\" ", "But this is another sentence.");
    testSplit("\"Here he comes!\". ", "That's what he said.");
    testSplit("The sentence ends here. ", "(Another sentence.)");
    testSplit("The sentence (...) ends here.");
    testSplit("The sentence [...] ends here.");
    testSplit("The sentence ends here (...). ", "Another sentence.");
    // previously known failed but not now :)
    testSplit("He won't. ", "Really.");
    testSplit("He will not. ", "Really.");
    testSplit("He won't go. ", "Really.");
    testSplit("He won't say no.", "Not really.");
    testSplit("He won't say No.", "Not really.");
    testSplit("He won't say no. 5 is better. ", "Not really.");
    testSplit("He won't say No. 5 is better. ", "Not really.");
    testSplit("They met at 5 p.m. on Thursday.");
    testSplit("They met at 5 p.m. ", "It was Thursday.");
    testSplit("This is it: a test.");
    testSplit("12) Make sure that the lamp is on. ", "12) Make sure that the lamp is on. ");
    testSplit("He also offers a conversion table (see Cohen, 1988, p. 123). ");
    // one/two returns = paragraph = new sentence:
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\n", "Really." }, stokenizer);
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\nReally." }, stokenizer2);
    // Missing space after sentence end:
    testSplit("James is from the Ireland!", "He lives in Spain now.");
    // From the abbreviation list:
    testSplit("Jones Bros. have built a successful company.");
    // parentheses:
    testSplit("It (really!) works.");
    testSplit("It [really!] works.");
    testSplit("It works (really!). ", "No doubt.");
    testSplit("It works [really!]. ", "No doubt.");
    testSplit("It really(!) works well.");
    testSplit("It really[!] works well.");
    testSplit("A test.\u00A0\n", "Another test.");  // try to deal with at least some nbsp that appear in strange places (e.g. Google Docs, web editors)
    testSplit("A test.\u00A0", "Another test.");  // not clear whether this is the best behavior...
    testSplit("A test.\n", "Another test.");
    testSplit("A test. \n", "Another test.");
    testSplit("A test. \n", "\n", "Another test.");
    testSplit("\"Here he comes.\"\u00a0", "But this is another sentence.");

    testSplit("The new Yahoo! product is nice.");
    testSplit("Yahoo!, what is it?");
    testSplit("Yahoo!", "What is it?");
    
    testSplit("This is a sentence.\u0002 ", "And this is another one.");  // footnotes in LibOO/OOo look like this
    
    testSplit("Other good editions are in vol. 4.");
    testSplit("Other good editions are in vol. IX.");
    testSplit("Other good editions are in vol. I think."); // ambiguous
    testSplit("Who Shall I Say is Calling & Other Stories S. Deziemianowicz, ed. (2009)");
    testSplit("Who Shall I Say is Calling & Other Stories S. Deziemianowicz, ed. ", "And this is another one.");
    testSplit("This is a sentence written by Ed. ", "And this is another one.");

  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}

