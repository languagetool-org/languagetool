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
package de.danielnaber.languagetool.dev;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import de.danielnaber.languagetool.tagging.Tagger;

/**
 * Analyzer that stores text and its POS analysis.
 * 
 * @author Daniel Naber
 */
class POSTagAnalyzer extends Analyzer {

  private Tagger tagger = null;
  
  public POSTagAnalyzer(Tagger tagger) {
    this.tagger = tagger;
  }

  public TokenStream tokenStream(@SuppressWarnings("unused")String fieldName, Reader reader) {
    TokenStream result = new StandardTokenizer(reader);
    //result = new LowerCaseFilter(result);
    result = new POSTagFilter(result, tagger);
    return result;
  }

}
