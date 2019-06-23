/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.de;

import java.io.IOException;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * @since 4.4
 */
public class SwissGermanTagger extends GermanTagger {

  /* (non-Javadoc)
   * @see org.languagetool.tagging.de.GermanTagger#tag(java.util.List, boolean)
   */
  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens, boolean ignoreCase) throws IOException { 
    List<AnalyzedTokenReadings> tokens = super.tag(sentenceTokens, ignoreCase);
    for (int i = 0; i < tokens.size(); i++) {
      AnalyzedTokenReadings reading = tokens.get(i);
      if (reading != null && 
          reading.getToken() != null &&
          reading.getToken().contains("ss") && 
          !reading.isTagged()) {
        AnalyzedTokenReadings replacementReading = lookup(reading.getToken().replace("ss", "ß"));
        if(replacementReading != null) {
          for(AnalyzedToken at : replacementReading.getReadings()) {
            reading.addReading(new AnalyzedToken(reading.getToken(), at.getPOSTag(), at.getLemma()));
          }
        }
      }
    }
    return tokens;
  }
}
