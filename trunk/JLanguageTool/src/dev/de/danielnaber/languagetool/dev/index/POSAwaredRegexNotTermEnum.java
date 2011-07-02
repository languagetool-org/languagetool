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
package de.danielnaber.languagetool.dev.index;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.regex.RegexCapabilities;

/**
 * Subclass of FilteredTermEnum for enumerating all terms that *DOES NOT* match the specified
 * regular expression term using the specified regular expression implementation.
 * <p>
 * Term enumerations are always ordered by Term.compareTo(). Each term in the enumeration is greater
 * than all that precede it.
 */

public class POSAwaredRegexNotTermEnum extends FilteredTermEnum {
  private String field = "";

  private String pre = "";

  private boolean endEnum = false;

  private RegexCapabilities regexImpl;

  private String text = "";

  private boolean isPOS;

  public POSAwaredRegexNotTermEnum(IndexReader reader, Term term, RegexCapabilities regexImpl, boolean isPOS)
      throws IOException {
    super();
    field = term.field();
    text = term.text();
    this.regexImpl = regexImpl;
    this.isPOS = isPOS;

    regexImpl.compile(text);

    setEnum(reader.terms(new Term(term.field(), pre)));
  }

  @Override
  protected final boolean termCompare(Term term) {
    if (field == term.field()) {
      String searchText = term.text();

      if ((isPOS && !searchText.startsWith(LanguageToolFilter.POS_PREFIX))
          || (!isPOS && searchText.startsWith(LanguageToolFilter.POS_PREFIX))) {
        return false;
      }

      if (isPOS) {
        searchText = searchText.replaceFirst(LanguageToolFilter.POS_PREFIX, "");

      }

      // System.out.println("a:" + searchText);
      // System.out.println("b:" + text);
      // System.out.println("c:" + !regexImpl.match(searchText));
      return !regexImpl.match(searchText);

    }
    endEnum = true;
    return false;
  }

  @Override
  public final float difference() {
    // TODO: adjust difference based on distance of searchTerm.text() and
    // term().text()
    return 1.0f;
  }

  @Override
  public final boolean endEnum() {
    return endEnum;
  }

  @Override
  public void close() throws IOException {
    super.close();
    field = null;
  }
}
