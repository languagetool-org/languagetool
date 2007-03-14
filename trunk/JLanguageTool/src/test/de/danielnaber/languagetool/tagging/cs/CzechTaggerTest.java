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
package de.danielnaber.languagetool.tagging.cs;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class CzechTaggerTest extends TestCase {
	
  private CzechTagger tagger;
	private WordTokenizer tokenizer;
	  
	public void setUp() {
	  tagger = new CzechTagger();
	  tokenizer = new WordTokenizer();
	}

	public void testTagger() throws IOException {
	  TestTools.myAssert("Ukončuje větu rozkazovací či zvolací.", "Ukončuje/[ukončovat]k5eAaImIp3nS větu/[věta]k1gFnSc4 rozkazovací/[rozkazovací]k2eAgFnPc1d1 či/[či]k8 zvolací/[zvolací]k2eAgFnPc1d1", tokenizer, tagger);
    TestTools.myAssert("Nahrazuje vynechané písmeno, používá se pro zkracování letopočtů.", "Nahrazuje/[nahrazovat]k5eAaImIp3nS vynechané/[vynechaný]k2eAgFnPc1d1 písmeno/[písmeno]k1gNnSc1|písmeno/[písmena]k1gFnSc5 používá/[používat]k5eAaImIp3nS se/[se]k3c4 pro/[pro]k7 zkracování/[zkracování]k1gNnPc1 letopočtů/[letopočet]k1gInPc2", tokenizer, tagger);
    TestTools.myAssert("blablabla","blablabla/[null]null", tokenizer, tagger);
	}

}
