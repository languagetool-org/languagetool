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

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import org.languagetool.JLanguageTool;

/**
 * LanguageToolAnalyzer emits the entire input (i.e. a sentence) as a single token by
 * AnyCharTokenizer, and then use JLanguageTool to analyze and tag the tokens by LanguageToolFilter.
 * 
 * @author Tao Lin
 */
public class LanguageToolAnalyzer extends Analyzer {

  private final JLanguageTool languageTool;

  private final Version matchVersion;

  public LanguageToolAnalyzer(Version matchVersion, JLanguageTool languageTool) {
    super();
    this.matchVersion = matchVersion;
    this.languageTool = languageTool;
  }

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new AnyCharTokenizer(this.matchVersion, reader);
    result = new LanguageToolFilter(result, languageTool);
    return result;
  }

}
