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
package org.languagetool.dev.index;

import org.apache.lucene.index.Term;
import org.apache.lucene.sandbox.queries.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.spans.*;
import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;

import java.util.ArrayList;
import java.util.List;

import static org.languagetool.dev.index.LanguageToolFilter.POS_PREFIX;

/**
 * A factory class for building a Query from a PatternRule.
 * 
 * @author Tao Lin
 * @author Daniel Naber
 */
public class PatternRuleQueryBuilder {

  public static final String FIELD_NAME = "field";
  public static final String FIELD_NAME_LOWERCASE = "fieldLowercase";

  private static final int MAX_SKIP = 1000;

  public Query buildQuery(PatternRule rule) throws UnsupportedPatternRuleException {
    try {
      return makeQuery(rule, true);
    } catch (UnsupportedPatternRuleException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Could not convert rule " + rule, e);
    }
  }

  public Query buildPossiblyRelaxedQuery(PatternRule rule) {
    try {
      return makeQuery(rule, false);
    } catch (UnsupportedPatternRuleException e) {
      throw new RuntimeException("Internal error, exception thrown although it was disabled", e);
    } catch (Exception e) {
      throw new RuntimeException("Could not convert rule " + rule, e);
    }
  }

  private Query makeQuery(PatternRule patternRule, boolean throwExceptionOnUnsupportedElement) throws UnsupportedPatternRuleException {
    SpanQuery query = null;
    Element prevElement = null;
    int position = 0;
    int skipCount = 0;
    for (Element element : patternRule.getElements()) {

      SpanQuery spanQuery;
      try {
        spanQuery = makeQuery(element, position, true);
      } catch (UnsupportedPatternRuleException e) {
        if (throwExceptionOnUnsupportedElement) {
          throw e;
        } else {
          spanQuery = getMatchAllQuery(element);
        }
      }
      if (query == null) {
        if (element.getNegation()) {
          query = null;
        } else {
          query = spanQuery;
        }
      } else {
        final int skip = getSkip(prevElement);
        if (skip == 0) {
          // we need to increase the skip because counting start from the beginning of a span query match:
          skipCount++;
        } else {
          skipCount = 0;
        }
        // attach the new query to the existing query - only this way
        // we can have per-query skips:
        if (element.getNegation()) {
          // SpanNotQuery(A,B) means: match spans A that don't overlap with spans B, thus for negation
          // construct an "overlap" match of the previous and current span and keep only the previous matches:
          final SpanNearQuery excludeQuery = new SpanNearQuery(new SpanQuery[] { query, spanQuery }, getSkip(prevElement) + skipCount, true);
          query = new SpanNotQuery(query, excludeQuery);
        } else {
          query = new SpanNearQuery(new SpanQuery[] { query, spanQuery }, getSkip(prevElement) + skipCount, true);
        }
      }
      prevElement = element;
      position++;
    }
    return query;
  }

  private int getSkip(Element element) {
    int skip = 0;
    if (element.getSkipNext() >= 0) {
      skip += element.getSkipNext();
    } else {
      skip = MAX_SKIP;
    }
    return skip;
  }

  private SpanQuery makeQuery(Element element, int position, boolean throwExceptionOnUnsupportedElement) throws UnsupportedPatternRuleException {
    if (throwExceptionOnUnsupportedElement) {
      checkUnsupportedElement(element, position);
    }
    final List<SpanQuery> queries = new ArrayList<SpanQuery>();

    SpanQuery termQuery = null;
    final String str = element.getString();
    if (str != null && !str.isEmpty()) {
      if (element.isRegularExpression()) {
        //final RegexpQuery regexpQuery = getWrappedRegexQuery(element, str);
        //termQuery = new SpanMultiTermQueryWrapper<RegexpQuery>(regexpQuery);
        termQuery = getWrappedRegexQuery(element, str);
      } else {
        if (element.getCaseSensitive()) {
          termQuery = new SpanTermQuery(new Term(FIELD_NAME, str));
        } else {
          termQuery = new SpanTermQuery(new Term(FIELD_NAME_LOWERCASE, str.toLowerCase()));
        }
      }
      queries.add(termQuery);
    }

    SpanQuery posQuery = null;
    final String pos = element.getPOStag();
    if (pos != null) {
      if (element.isPOStagRegularExpression()) {
        // TODO: this is not correct, but POS tags are probably always uppercase so we ignore
        // this to avoid the exception that all fields need to have the same field
        // when constructing the SpanNearQuery:
        //final RegexpQuery regexpQuery = getCaseSensitiveRegexQuery(POS_PREFIX + pos);
        posQuery = getWrappedRegexQuery(element, POS_PREFIX + "(" + pos + ")");
        //posQuery = new SpanMultiTermQueryWrapper<RegexpQuery>(regexpQuery);
      } else {
        if (element.getCaseSensitive()) {
          posQuery = new SpanTermQuery(new Term(FIELD_NAME, POS_PREFIX + pos));
        } else {
          posQuery = new SpanTermQuery(new Term(FIELD_NAME_LOWERCASE, POS_PREFIX.toLowerCase() + pos.toLowerCase()));
        }
      }
      queries.add(posQuery);
    }

    if (termQuery == null && posQuery == null) {
      if (str != null && str.isEmpty() && pos == null) {
        // this is an actually empty token that can match any term:
        return getMatchAllQuery(element);
      } else if (throwExceptionOnUnsupportedElement) {
        throw new UnsupportedPatternRuleException("Internal error: both term and POS query are null for element '" + element + "'");
      }
      return getMatchAllQuery(element);
    }

    if (queries.size() > 1) {
      return new SpanNearQuery(queries.toArray(new SpanQuery[queries.size()]), 0, false);
    } else {
      return queries.get(0);
    }

  }

  private SpanMultiTermQueryWrapper<RegexpQuery> getMatchAllQuery(Element element) {
    final RegexpQuery query;
    if (element.getCaseSensitive()) {
      // the field name is relevant because all parts of a SpanQuery must refer to the same field:
      query = new RegexpQuery(new Term(FIELD_NAME, ".*"));
    } else {
      query = new RegexpQuery(new Term(FIELD_NAME_LOWERCASE, ".*"));
    }
    return new SpanMultiTermQueryWrapper<RegexpQuery>(query);
  }

  private SpanMultiTermQueryWrapper<? extends org.apache.lucene.search.MultiTermQuery> getWrappedRegexQuery(Element element, String str) {
    if (element.getCaseSensitive()) {
      final RegexpQuery query = getCaseSensitiveRegexQuery(str);
      return new SpanMultiTermQueryWrapper<RegexpQuery>(query);
    } else {
      try {
        final RegexpQuery query = new RegexpQuery(new Term(FIELD_NAME_LOWERCASE, str.toLowerCase()));
        return new SpanMultiTermQueryWrapper<RegexpQuery>(query);
      } catch (IllegalArgumentException e) {
        // fallback for special constructs like "\p{Punct}" not supported by Lucene RegExp:
        final RegexQuery query = new RegexQuery(new Term(FIELD_NAME_LOWERCASE, str));
        query.setRegexImplementation(new JavaUtilRegexCapabilities(JavaUtilRegexCapabilities.FLAG_CASE_INSENSITIVE));
        return new SpanMultiTermQueryWrapper<RegexQuery>(query);
      }
    }
  }

  private RegexpQuery getCaseSensitiveRegexQuery(String str) {
    return new RegexpQuery(new Term(FIELD_NAME, str));
  }

  private void checkUnsupportedElement(Element patternElement, int position)
      throws UnsupportedPatternRuleException {
    if (position == 0 && patternElement.getNegation()) {
      throw new UnsupportedPatternRuleException("Pattern rules with negation in first element are not supported.");
    }
    if (patternElement.hasExceptionList()) {
      throw new UnsupportedPatternRuleException("Pattern rules with token exceptions are not supported.");
    }
    if (patternElement.testWhitespace()) {
      throw new UnsupportedPatternRuleException("Pattern rules with tokens testing \"Whitespace before\" are not supported.");
    }
    if (patternElement.hasAndGroup()) {
      throw new UnsupportedPatternRuleException("Pattern rules with tokens in \"And Group\" are not supported.");
    }
    if (patternElement.isPartOfPhrase()) {
      throw new UnsupportedPatternRuleException("Pattern rules with phrases are not supported.");
    }
    if (patternElement.isUnified()) {
      throw new UnsupportedPatternRuleException("Pattern rules with unified tokens are not supported.");
    }
    if (patternElement.isInflected()) {
      throw new UnsupportedPatternRuleException("Pattern rules with inflected tokens are not supported.");
    }
    if (patternElement.getString().matches("\\\\\\d+")) { // e.g. "\1"
      throw new UnsupportedPatternRuleException("Pattern rules with only match references are not supported.");
    }
    // TODO: exception for <match no="0"/> etc. (patternElement.getMatch()?)
  }

}
