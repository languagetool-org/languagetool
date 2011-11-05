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
import org.apache.lucene.search.regex.RegexTermEnum;
import org.apache.lucene.util.ToStringUtils;

/**
 * Implements the *NOT* regular expression term search query. The expressions supported depend on
 * the regular expression implementation used by way of the {@link RegexCapabilities} interface.
 * 
 * @see RegexTermEnum
 */

// TODO Tao: is it OK to make RegexNotQuery extends RegexQuery?
public class POSAwaredRegexNotQuery extends POSAwaredRegexQuery {

  private static final long serialVersionUID = -4990041844164168930L;

  /** Constructs a query for terms "NOT" matching <code>term</code>. */
  public POSAwaredRegexNotQuery(Term term, boolean isPOS) {
    super(term, isPOS);
  }

  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new POSAwaredRegexNotTermEnum(reader, this.getTerm(), this.getRegexImplementation(), this.isPOS());
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!this.getTerm().field().equals(field)) {
      buffer.append(this.getTerm().field());
      buffer.append(":");
    }
    buffer.append("*NOT*" + (this.isPOS() ? "$POS$" : ""));
    buffer.append(this.getTerm().text());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
}
