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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.regex.SpanRegexQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * A factory class for building a Query from a PatternRule.
 * 
 * @author Tao Lin
 * 
 */
public class PatternRuleQueryBuilder {

  public static final String FIELD_NAME = "field";

  public static Query buildQuery(PatternRule rule) throws UnsupportedPatternRuleException {
    return next(rule.getElements().iterator());
  }

  // create the next SpanQuery from the top Element in the Iterator.
  private static SpanQuery next(Iterator<Element> it) throws UnsupportedPatternRuleException {

    // no more Element
    if (!it.hasNext()) {
      return null;
    }

    final Element patternElement = it.next();

    // unsupported rule features
    if (patternElement.getExceptionList() != null) {
      throw new UnsupportedPatternRuleException(
          "Pattern rules with token exceptions are not supported.");
    }
    if (patternElement.isInflected()) {
      throw new UnsupportedPatternRuleException(
          "Pattern rules with inflated tokens are not supported.");
    }

    final ArrayList<SpanQuery> list = new ArrayList<SpanQuery>();

    int skip = 0;

    final SpanQuery termQuery = createSpanQuery(patternElement.getString(), "",
        patternElement.getNegation(), patternElement.isRegularExpression());
    final SpanQuery posQuery = createSpanQuery(patternElement.getPOStag(),
        LanguageToolFilter.POS_PREFIX, patternElement.getPOSNegation(),
        patternElement.isPOStagRegularExpression());

    if (termQuery != null && posQuery != null) {
      final SpanNearQuery q = new SpanNearQuery(new SpanQuery[] { termQuery, posQuery }, 0, false);
      list.add(q);
    } else if (termQuery != null) {
      list.add(termQuery);
    } else if (posQuery != null) {
      list.add(posQuery);
    } else {
      skip++;
    }
    if (patternElement.getSkipNext() >= 0) {
      skip += patternElement.getSkipNext();
    } else {
      // skip == -1
      skip = Integer.MAX_VALUE;
    }

    // recursion invoke
    final SpanQuery next = next(it);

    if (next != null) {
      list.add(next);
      return new SpanNearQuery(list.toArray(new SpanQuery[list.size()]), skip, true);
    } else {
      return list.get(0);
    }

  }

  private static SpanQuery createSpanQuery(String token, String prefix, boolean isNegation,
      boolean isRegularExpression) {
    SpanQuery q = null;
    if (token != null && !token.equals("")) {
      final Term term = new Term(FIELD_NAME, prefix + token);
      if (isNegation) {
        q = new SpanRegexNotQuery(term);
      } else {
        if (isRegularExpression) {
          q = new SpanRegexQuery(term);
        } else {
          q = new SpanTermQuery(term);
        }
      }
    }
    return q;
  }

}
