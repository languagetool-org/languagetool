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
package org.languagetool.tagging;

import java.io.IOException;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * The part-of-speech tagger interface, whose implementations are usually language-dependent.
 * 
 * @author Daniel Naber
 */
public interface Tagger {

  /**
   * Returns a list of {@link AnalyzedToken}s that assigns each term in the 
   * sentence some kind of part-of-speech information (not necessarily just one tag).
   * 
   * <p>Note that this method takes exactly one sentence. Its implementation
   * may implement special cases for the first word of a sentence, which is 
   * usually written with an uppercase letter.
   * 
   * @param sentenceTokens the text as returned by a WordTokenizer 
   */
  List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException;
  
  /** 
   * Create the AnalyzedToken used for whitespace and other non-words. Use <code>null</code>
   * as the POS tag for this token.
   */
  AnalyzedTokenReadings createNullToken(String token, int startPos);

  /**
   * Create a token specific to the language of the implementing class.
   */
  AnalyzedToken createToken(String token, String posTag);
    
}
