/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.crh;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.CrimeanTatar;
import org.languagetool.tokenizers.crh.CrimeanTatarWordTokenizer;

public class CrimeanTatarTaggerTest {

  private CrimeanTatarTagger tagger;
  private CrimeanTatarWordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new CrimeanTatarTagger();
    tokenizer = new CrimeanTatarWordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new CrimeanTatar());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("meraba", "meraba/[meraba]INTJ", tokenizer, tagger);
  }

}
