/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.FakeLanguage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LocalSRXSentenceTokenizerTest {

  private final LocalSRXSentenceTokenizer tokenizer =
          new LocalSRXSentenceTokenizer(new FakeLanguage("yy"), "/org/languagetool/tokenizers/segment-test.srx");

  @Test
  public void testTokenize() {
    assertTokenize("A sentence. Another one.", "[A sentence. , Another one.]");
    assertTokenize("A fooabbr. doesn't end a sentence.", "[A fooabbr. doesn't end a sentence.]");
    assertTokenize("A barabbr. doesn't end a sentence.", "[A barabbr. doesn't end a sentence.]");

    tokenizer.setSingleLineBreaksMarksParagraph(true);
    assertTokenize("A sentence.\nAnother one.", "[A sentence.\n, Another one.]");
    assertTokenize("A sentence.\n\nAnother one.", "[A sentence.\n, \n, Another one.]");
    
    tokenizer.setSingleLineBreaksMarksParagraph(false);
    assertTokenize("A sentence\nwhich goes on here.", "[A sentence\nwhich goes on here.]");
    assertTokenize("A sentence.\n\nAnother one.", "[A sentence.\n, \nAnother one.]");
  }

  private void assertTokenize(String input, String output) {
    assertThat(tokenizer.tokenize(input).toString(), is(output));
  }

}
