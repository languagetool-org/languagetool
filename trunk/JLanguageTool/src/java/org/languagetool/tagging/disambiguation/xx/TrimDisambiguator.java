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

package de.danielnaber.languagetool.tagging.disambiguation.xx;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;

/**
 * Trivial disambiguator. Just cuts out tags from the token. It leaves only the
 * first tag.
 * 
 * @author Jozef Licko
 */
public class TrimDisambiguator implements Disambiguator {

  @Override
  public final AnalyzedSentence disambiguate(final AnalyzedSentence input) {

    final AnalyzedTokenReadings[] anTokens = input.getTokens();
    final AnalyzedTokenReadings[] output = new AnalyzedTokenReadings[anTokens.length];

    for (int i = 0; i < anTokens.length; i++) {

      if (anTokens[i].getReadingsLength() > 1) {
        final AnalyzedToken[] firstToken = new AnalyzedToken[1];
        firstToken[0] = anTokens[i].getAnalyzedToken(0);
        output[i] = new AnalyzedTokenReadings(firstToken, anTokens[i].getStartPos());
      } else {
        output[i] = anTokens[i];
      }
    }
    return new AnalyzedSentence(output);
  }

}
