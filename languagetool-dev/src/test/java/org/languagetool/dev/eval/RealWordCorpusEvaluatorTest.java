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
package org.languagetool.dev.eval;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RealWordCorpusEvaluatorTest {

  @Ignore("requires local ngram index")
  @Test
  public void testCheck() throws IOException {
    RealWordCorpusEvaluator evaluator = new RealWordCorpusEvaluator(new File("/data/google-ngram-index/"));
    URL errors = RealWordCorpusEvaluatorTest.class.getResource("/org/languagetool/dev/eval");
    evaluator.run(new File(errors.getFile()));
    assertThat(evaluator.getSentencesChecked(), is(3));
    assertThat(evaluator.getErrorsChecked(), is(5));
    assertThat(evaluator.getRealErrorsFound(), is(3));
    assertThat(evaluator.getRealErrorsFoundWithGoodSuggestion(), is(2));
  }

}
