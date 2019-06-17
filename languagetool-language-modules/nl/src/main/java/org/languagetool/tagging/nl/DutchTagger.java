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
package org.languagetool.tagging.nl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

/**
 * Dutch tagger.
 * 
 * @author Marcin Milkowski
 */
public class DutchTagger extends BaseTagger {

  @Override
  public String getManualAdditionsFileName() {
    return "/nl/added.txt";
  }

  @Override
  public String getManualRemovalsFileName() {
    return "/nl/removed.txt";
  }

  public DutchTagger() {
    super("/nl/dutch.dict",  new Locale("nl"));
  }
  
  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens)
      throws IOException {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = getAnalyzedTokens(word);
      
      if (l.isEmpty()) {
        String word2 = word;
        word2 = word2.replace("á", "a").replace("é", "e").replace("í","i").replace("ó","o").replace("ú","u");
        if (!word2.equals(word)) {
          List<AnalyzedToken> l2  = getAnalyzedTokens(word2);
          if (l2 != null) {
            l.addAll(l2);
          }
        }
      }
      
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
      
    }
    
    return tokenReadings;
  }
  
}
