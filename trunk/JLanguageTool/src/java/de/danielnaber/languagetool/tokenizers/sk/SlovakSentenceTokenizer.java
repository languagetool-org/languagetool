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
package de.danielnaber.languagetool.tokenizers.sk;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own
 * token. Based on GermanSentenceWordTokenizer 
 * 
 * @author Zdenko Podobný
 * @since 22.05.2009 19:53:50
 */

public class SlovakSentenceTokenizer extends SentenceTokenizer {

  private static final String[] ABBREV_LIST = {
    "č", "čl", "napr"};

  // einige deutsche Monate, vor denen eine Zahl erscheinen kann,
  // ohne dass eine Satzgrenze erkannt wird (z.B. "am 13. Dezember" -> keine Satzgrenze)
  private static final String[] MONTH_NAMES = { "január", "február", "marec", "apríl", "máj",
      "jún", "júl", "august", "september", "október", "november", "december" };

  public SlovakSentenceTokenizer() {
    super(ABBREV_LIST);
    super.monthNames = MONTH_NAMES;
  }
 
}
