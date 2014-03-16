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

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.sandbox.queries.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.languagetool.AnalyzedToken;
import org.languagetool.Language;
import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.languagetool.dev.index.LanguageToolFilter.LEMMA_PREFIX;
import static org.languagetool.dev.index.LanguageToolFilter.POS_PREFIX;

/**
 * A factory class for building a Lucene Query from a PatternRule. The query
 * requires an index where each document contains only one sentence. It returns
 * potential matches, i.e. LanguageTool still needs to run over the matches
 * to make sure there is indeed an error.
 * 
 * @author Tao Lin
 * @author Daniel Naber
 */
public class PatternRuleQueryBuilder {

  public static final String FIELD_NAME = "field";
  public static final String SOURCE_FIELD_NAME = "source";
  public static final String FIELD_NAME_LOWERCASE = "fieldLowercase";
  
  private final Language language;

  public PatternRuleQueryBuilder(Language language) {
    this.language = language;
  }

  /**
   * Iterate over all elements, ignore those not supported, add the other ones to a BooleanQuery.
   * @throws UnsupportedPatternRuleException if no query could be created for the rule
   */
  public Query buildRelaxedQuery(PatternRule rule) throws UnsupportedPatternRuleException {
    final BooleanQuery booleanQuery = new BooleanQuery();
    for (Element element : rule.getElements()) {
      try {
        final BooleanClause clause = makeQuery(element);
        booleanQuery.add(clause);
      } catch (UnsupportedPatternRuleException e) {
        //System.out.println("Ignoring because it's not supported: " + element + ": " + e);
        // cannot handle - okay to ignore, as we may return too broad matches
      } catch (Exception e) {
        throw new RuntimeException("Could not create query for rule " + rule.getId(), e);
      }
    }
    if (booleanQuery.clauses().size() == 0) {
      throw new UnsupportedPatternRuleException("No items found in rule that can be used to build a search query: " + rule);
    }
    return booleanQuery;
  }

  private BooleanClause makeQuery(Element element) throws UnsupportedPatternRuleException {
    checkUnsupportedElement(element);

    final String termStr = element.getString();
    final String pos = element.getPOStag();

    final BooleanClause termQuery = getTermQueryOrNull(element, termStr);
    final BooleanClause posQuery = getPosQueryOrNull(element, pos);

    if (termQuery != null && posQuery != null) {
      // if both term and POS are set, we create a query where both are at the same position
      if (mustOccur(termQuery) && mustOccur(posQuery)) {
        final SpanQuery spanQueryForTerm = asSpanQuery(termQuery);
        final SpanQuery spanQueryForPos = asSpanQuery(posQuery);
        final SpanQuery[] spanClauses = {spanQueryForTerm, spanQueryForPos};
        return new BooleanClause(new SpanNearQuery(spanClauses, 0, false), BooleanClause.Occur.MUST);
      } else {
        // should not happen, we always use Occur.MUST:
        throw new UnsupportedPatternRuleException("Term/POS combination not supported yet: " + element);
      }
    
    } else if (termQuery != null) {
      return termQuery;

    } else if (posQuery != null) {
      return posQuery;
    }
    throw new UnsupportedPatternRuleException("Neither POS tag nor term set for element: " + element);
  }

  private SpanQuery asSpanQuery(BooleanClause query) {
    if (query.getQuery() instanceof MultiTermQuery) {
      return new SpanMultiTermQueryWrapper<>((MultiTermQuery) query.getQuery());
    } else {
      final Set<Term> terms = new HashSet<>();
      query.getQuery().extractTerms(terms);
      if (terms.size() != 1) {
        throw new RuntimeException("Expected term set of size 1: " + terms);
      }
      return new SpanTermQuery(terms.iterator().next());
    }
  }

  private boolean mustOccur(BooleanClause query) {
    return query != null && query.getOccur() == BooleanClause.Occur.MUST;
  }

  private BooleanClause getTermQueryOrNull(Element element, String termStr) {
    if (termStr == null || termStr.isEmpty()) {
      return null;
    }
    final Query termQuery;
    final Term termQueryTerm = getTermQueryTerm(element, termStr);
    if (element.isInflected() && element.isRegularExpression()) {
      Term lemmaQueryTerm = getQueryTerm(element, LEMMA_PREFIX + "(", termStr, ")");
      final RegexpQuery regexpQuery = new RegexpQuery(lemmaQueryTerm);
      return new BooleanClause(regexpQuery, BooleanClause.Occur.MUST);
    } else if (element.isInflected() && !element.isRegularExpression()) {
      final Synthesizer synthesizer = language.getSynthesizer();
      if (synthesizer != null) {
        try {
          final String[] synthesized = synthesizer.synthesize(new AnalyzedToken(termStr, null, termStr), ".*", true);
          final RegexpQuery regexpQuery = new RegexpQuery(getTermQueryTerm(element, StringUtils.join(synthesized, "|")));
          return new BooleanClause(regexpQuery, BooleanClause.Occur.MUST);
        } catch (IOException e) {
          throw new RuntimeException("Could not build Lucene query for '" + element + "' and '" + termStr + "'", e);
        }
      }
      return null;
    } else if (element.isRegularExpression()) {
      termQuery = getRegexQuery(termQueryTerm, termStr);
    } else {
      termQuery = new TermQuery(termQueryTerm);
    }
    if (element.getNegation()) {
      // we need to ignore this - negation, if any, must happen at the same position
      return null;
    } else {
      return new BooleanClause(termQuery, BooleanClause.Occur.MUST);
    }
  }

  private BooleanClause getPosQueryOrNull(Element element, String pos) {
    if (pos == null || pos.isEmpty()) {
      return null;
    }
    final Query posQuery;
    final Term posQueryTerm;
    if (element.isPOStagRegularExpression()) {
      posQueryTerm = getQueryTerm(element, POS_PREFIX + "(", pos, ")");
      posQuery = getRegexQuery(posQueryTerm, pos);
    } else {
      posQueryTerm = getQueryTerm(element, POS_PREFIX, pos, "");
      posQuery = new TermQuery(posQueryTerm);
    }
    if (element.getPOSNegation()) {
      // we need to ignore this - negation, if any, must happen at the same position
      return null;
    } else {
      return new BooleanClause(posQuery, BooleanClause.Occur.MUST);
    }
  }

  private Term getTermQueryTerm(Element element, String str) {
    if (element.isCaseSensitive()) {
      return new Term(FIELD_NAME, str);
    } else {
      return new Term(FIELD_NAME_LOWERCASE, str.toLowerCase());
    }
  }

  private Term getQueryTerm(Element element, String prefix, String str, String suffix) {
    if (element.isCaseSensitive()) {
      return new Term(FIELD_NAME, prefix + str + suffix);
    } else {
      return new Term(FIELD_NAME_LOWERCASE, prefix.toLowerCase() + str.toLowerCase() + suffix.toLowerCase());
    }
  }

  private Query getRegexQuery(Term term, String str) {
    try {
      if (str.contains("?iu") || str.contains("?-i") || str.contains("\\d")) {
        // Lucene's RegexpQuery doesn't seem to handle this correctly
        return getFallbackRegexQuery(str);
      }
      return new RegexpQuery(term);
    } catch (IllegalArgumentException e) {
      // fallback for special constructs like "\p{Punct}" not supported by Lucene RegExp:
      return getFallbackRegexQuery(str);
    }
  }

  private RegexQuery getFallbackRegexQuery(String str) {
    // No lowercase of str, so '\p{Punct}' doesn't become '\p{punct}':
    final RegexQuery query = new RegexQuery(new Term(FIELD_NAME_LOWERCASE, str));
    query.setRegexImplementation(new JavaUtilRegexCapabilities(JavaUtilRegexCapabilities.FLAG_CASE_INSENSITIVE));
    return query;
  }

  private void checkUnsupportedElement(Element patternElement)
      throws UnsupportedPatternRuleException {
    if (patternElement.isUnified()) {
      throw new UnsupportedPatternRuleException("Elements with unified tokens are not supported.");
    }
    if (patternElement.getString().matches("\\\\\\d+")) { // e.g. "\1"
      throw new UnsupportedPatternRuleException("Elements with only match references (e.g. \\1) are not supported.");
    }
  }

}
