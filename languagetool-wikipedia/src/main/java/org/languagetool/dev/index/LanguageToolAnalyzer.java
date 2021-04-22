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
package org.languagetool.dev.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.languagetool.JLanguageTool;

/**
 * LanguageToolAnalyzer emits the entire input (i.e. a sentence) as a single token by
 * AnyCharTokenizer, and then use JLanguageTool to analyze and tag the tokens by LanguageToolFilter.
 * 
 * @author Tao Lin
 */
public final class LanguageToolAnalyzer extends Analyzer {

  private final JLanguageTool languageTool;
  private final boolean toLowerCase;

  public LanguageToolAnalyzer(JLanguageTool languageTool, boolean toLowerCase) {
    super();
    this.languageTool = languageTool;
    this.toLowerCase = toLowerCase;
  }

  @Override
  protected TokenStreamComponents createComponents(String s) {
    Tokenizer tokenizer = new AnyCharTokenizer();
    TokenStream result = new LanguageToolFilter(tokenizer, languageTool, toLowerCase);
    return new TokenStreamComponents(tokenizer, result);
  }

}
