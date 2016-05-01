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
package org.languagetool.dev.errorcorpus;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PedlerCorpusTest {

  @Test
  public void testCorpusAccess() throws IOException {
    URL errors = PedlerCorpusTest.class.getResource("/org/languagetool/dev/eval");
    PedlerCorpus corpus = new PedlerCorpus(new File(errors.getFile()));
    Iterator<ErrorSentence> iterator = corpus.iterator();
    assertTrue(iterator.hasNext());
    ErrorSentence sentence1 = iterator.next();
    assertThat(sentence1.getAnnotatedText().getPlainText(),
            is("But also please not that grammar checkers aren't perfect."));
    assertThat(sentence1.getMarkupText(),
            is("But <ERR targ=foo>also</ERR> please <ERR targ=note>not</ERR> that grammar checkers aren't perfect."));

    ErrorSentence sentence2 = iterator.next();
    assertThat(sentence2.getAnnotatedText().getPlainText(),
            is("But also also please note note that grammar checkers aren't perfect."));
    assertThat(sentence2.getMarkupText(),
            is("But <ERR targ=bad suggestion>also also</ERR> please <ERR targ=note>note note</ERR> that grammar checkers aren't perfect."));

    assertTrue(iterator.hasNext());
    iterator.next();
    assertFalse(iterator.hasNext());
  } 

}
