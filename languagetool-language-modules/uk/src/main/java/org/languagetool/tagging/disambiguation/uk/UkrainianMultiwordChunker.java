/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.uk;

import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.disambiguation.MultiWordChunker2;
import org.languagetool.tagging.uk.PosTagHelper;

class UkrainianMultiwordChunker extends MultiWordChunker2 {
  UkrainianMultiwordChunker(String filename, boolean allowFirstCapitalized) {
    super(filename, allowFirstCapitalized);
  }

  protected boolean matches(String matchText, AnalyzedTokenReadings inputTokens) {
    if( ! matchText.startsWith("/") )
      return super.matches(matchText, inputTokens);
    
    // TODO: this is a bit slow - we recompile regex every time, need better solution
    // possible more flexibility in MultiWordChunker2
    return PosTagHelper.hasPosTag(inputTokens, Pattern.compile(matchText.substring(1)));
  }
}