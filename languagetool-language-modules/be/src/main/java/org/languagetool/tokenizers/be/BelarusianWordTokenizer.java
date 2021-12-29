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
package org.languagetool.tokenizers.be;

import org.languagetool.tokenizers.WordTokenizer;

/**
 * Specific to Belarusian: apostrophes (\u0027, \u2019, \u02BC) are part of the word.
 * 
 * @author Aleś Bułojčyk (alex73mail@gmail.com)
 */
public class BelarusianWordTokenizer extends WordTokenizer {
    private final String tokenizingCharacters;

    public BelarusianWordTokenizer() {
        tokenizingCharacters = super.getTokenizingCharacters().replace("\u0027", "").replace("\u2019", "").replace("\u02BC", "");
    }

    @Override
    public String getTokenizingCharacters() {
        return tokenizingCharacters;
    }
}
