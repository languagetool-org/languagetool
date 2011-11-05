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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.search.spans.SpanQuery;

import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;

/**
 * A factory class for building a Query from a PatternRule.
 * 
 * @author Tao Lin
 */
public class PatternRuleQueryBuilder {

  public static final String FIELD_NAME = "field";

  private static final int MAX_SKIP = 1000;

  public Query buildQuery(PatternRule rule, boolean checkUnsupportedRule)
      throws UnsupportedPatternRuleException {
    return next(rule.getElements().iterator(), checkUnsupportedRule);
  }

  // create the next SpanQuery from the top Element in the Iterator.
  private SpanQuery next(Iterator<Element> it, boolean checkUnsupportedRule)
      throws UnsupportedPatternRuleException {

    // no more Element
    if (!it.hasNext()) {
      return null;
    }

    final Element patternElement = it.next();

    SpanQuery tokenQuery;
    SpanQuery posQuery = null;
    try {
      checkUnsupportedRule(patternElement);
      final boolean caseSensitive = patternElement.getCaseSensitive();

      tokenQuery = createTokenQuery(patternElement.getString(), patternElement.getNegation(),
          patternElement.isRegularExpression(), caseSensitive);

      posQuery = createPOSQuery(patternElement.getPOStag(), patternElement.getPOSNegation(),
          patternElement.isPOStagRegularExpression());

    } catch (UnsupportedPatternRuleException e) {
      if (checkUnsupportedRule) {
        throw e;
      } else {
        // create an empty token for the unsupported token, so that it can match any term with any
        // POS tag.
        tokenQuery = createTokenQuery("", false, false, false);
      }

    }
    final ArrayList<SpanQuery> list = new ArrayList<SpanQuery>();

    int skip = 0;

    if (tokenQuery != null && posQuery != null) {
      final RigidSpanNearQuery q = new RigidSpanNearQuery(new SpanQuery[] { tokenQuery, posQuery },
          0, true);
      list.add(q);
    } else if (tokenQuery != null) {
      list.add(tokenQuery);
    } else if (posQuery != null) {
      list.add(posQuery);
    } else {
      skip++;
    }
    if (patternElement.getSkipNext() >= 0) {
      skip += patternElement.getSkipNext();
    } else {
      skip = MAX_SKIP;
    }

    // recursion invoke
    final SpanQuery next = next(it, checkUnsupportedRule);

    if (next != null) {
      list.add(next);
      return new RigidSpanNearQuery(list.toArray(new SpanQuery[list.size()]), skip + 1, true);
    } else if (list.size() > 0) {
      return list.get(0);
    } else {
      return null;
    }

  }

  private void checkUnsupportedRule(Element patternElement)
      throws UnsupportedPatternRuleException {
    // we need Element to expose its features of exception and whitespace testing support.
    if (patternElement.hasExceptionList()) {
      throw new UnsupportedPatternRuleException(
          "Pattern rules with token exceptions are not supported.");
    }
    if (patternElement.testWhitespace()) {
      throw new UnsupportedPatternRuleException(
          "Pattern rules with tokens testing \"Whitespace before\" are not supported.");
    }
    if (patternElement.hasAndGroup()) {
      throw new UnsupportedPatternRuleException(
          "Pattern rules with tokens in \"And Group\" are not supported.");
    }
    if (patternElement.isPartOfPhrase()) {
      throw new UnsupportedPatternRuleException("Pattern rules with phrases are not supported.");
    }
    if (patternElement.isUnified()) {
      throw new UnsupportedPatternRuleException(
          "Pattern rules with unified tokens are not supported.");
    }
    if (patternElement.isInflected()) {
      throw new UnsupportedPatternRuleException(
          "Pattern rules with inflected tokens are not supported.");
    }
  }

  private SpanQuery createTokenQuery(String token, boolean isNegation,
      boolean isRegularExpression, boolean caseSensitive) {
    SpanQuery q = null;
    if (token != null && !token.equals("")) {
      if (!isNegation) {
        final String text = isRegularExpression ? token : Pattern.quote(token);
        final Term term = new Term(FIELD_NAME, text);
        q = new POSAwaredSpanRegexQuery(term, false);
        if (!caseSensitive) {
          ((POSAwaredSpanRegexQuery) q).setRegexImplementation(new JavaUtilRegexCapabilities(
              JavaUtilRegexCapabilities.FLAG_CASE_INSENSITIVE));
        }
      } else {
        final String text = isRegularExpression ? token : Pattern.quote(token);
        final Term term = new Term(FIELD_NAME, text);
        q = new POSAwaredSpanRegexNotQuery(term, false);
        if (!caseSensitive) {
          ((POSAwaredSpanRegexNotQuery) q).setRegexImplementation(new JavaUtilRegexCapabilities(
              JavaUtilRegexCapabilities.FLAG_CASE_INSENSITIVE));
        }
      }
    }
    return q;
  }

  private SpanQuery createPOSQuery(String token, boolean isNegation, boolean isRegularExpression) {
    SpanQuery q = null;
    if (token != null && !token.equals("")) {
      if (!isNegation) {
        final String text = isRegularExpression ? token : Pattern.quote(token);
        final Term term = new Term(FIELD_NAME, text);
        q = new POSAwaredSpanRegexQuery(term, true);
      } else {
        final String text = isRegularExpression ? token : Pattern.quote(token);
        final Term term = new Term(FIELD_NAME, text);
        q = new POSAwaredSpanRegexNotQuery(term, true);
      }
    }
    return q;
  }
  
}
