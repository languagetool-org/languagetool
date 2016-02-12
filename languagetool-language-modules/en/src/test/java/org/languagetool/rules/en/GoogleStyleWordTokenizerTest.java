/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class GoogleStyleWordTokenizerTest {

  @Test
  public void testTokenize() {
    GoogleStyleWordTokenizer tokenizer = new GoogleStyleWordTokenizer();
    assertThat(tokenizer.tokenize("foo bar"), is(Arrays.asList("foo", " ", "bar")));
    assertThat(tokenizer.tokenize("foo-bar"), is(Arrays.asList("foo", "-", "bar")));
    assertThat(tokenizer.tokenize("I'm here."), is(Arrays.asList("I", "'m", " ", "here", ".")));
    assertThat(tokenizer.tokenize("I'll do that"), is(Arrays.asList("I", "'ll", " ", "do", " " , "that")));
    assertThat(tokenizer.tokenize("You're here"), is(Arrays.asList("You", "'re", " ", "here")));
    assertThat(tokenizer.tokenize("You've done that"), is(Arrays.asList("You", "'ve", " ", "done", " " , "that")));
  }
  
}