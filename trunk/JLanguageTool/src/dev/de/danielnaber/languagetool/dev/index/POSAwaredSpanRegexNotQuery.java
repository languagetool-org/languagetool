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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.regex.RegexCapabilities;
import org.apache.lucene.search.regex.RegexQueryCapable;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;

/**
 * A SpanQuery version of {@link POSAwaredRegexNotQuery} allowing regular expression queries to be nested
 * within other SpanQuery subclasses.
 * 
 */

public class POSAwaredSpanRegexNotQuery extends SpanMultiTermQueryWrapper<POSAwaredRegexNotQuery> implements
    RegexQueryCapable {

  private static final long serialVersionUID = -2877900815692852272L;

  public POSAwaredSpanRegexNotQuery(Term term, boolean isPOS) {
    super(new POSAwaredRegexNotQuery(term, isPOS));
  }

  public Term getTerm() {
    return query.getTerm();
  }

  public void setRegexImplementation(RegexCapabilities impl) {
    query.setRegexImplementation(impl);
  }

  public RegexCapabilities getRegexImplementation() {
    return query.getRegexImplementation();
  }
}
