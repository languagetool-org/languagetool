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

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.Language;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternToken;
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
  private final IndexSearcher indexSearcher;

  public PatternRuleQueryBuilder(Language language, IndexSearcher indexSearcher) {
    this.language = language;
    this.indexSearcher = indexSearcher;
  }

  /**
   * Iterate over all elements, ignore those not supported, add the other ones to a BooleanQuery.
   * @throws UnsupportedPatternRuleException if no query could be created for the rule
   */
  public Query buildRelaxedQuery(AbstractPatternRule rule) throws UnsupportedPatternRuleException {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (PatternToken patternToken : rule.getPatternTokens()) {
      try {
        BooleanClause clause = makeQuery(patternToken);
        builder.add(clause);
      } catch (UnsupportedPatternRuleException e) {
        //System.out.println("Ignoring because it's not supported: " + element + ": " + e);
        // cannot handle - okay to ignore, as we may return too broad matches
      } catch (Exception e) {
        throw new RuntimeException("Could not create query for rule " + rule.getId(), e);
      }
    }
    BooleanQuery query = builder.build();
    if (query.clauses().size() == 0) {
      throw new UnsupportedPatternRuleException("No items found in rule that can be used to build a search query: " + rule);
    }
    return query;
  }

  private BooleanClause makeQuery(PatternToken patternToken) throws UnsupportedPatternRuleException {
    checkUnsupportedElement(patternToken);

    String termStr = patternToken.getString();
    String pos = patternToken.getPOStag();

    BooleanClause termQuery = getTermQueryOrNull(patternToken, termStr);
    BooleanClause posQuery = getPosQueryOrNull(patternToken, pos);

    if (termQuery != null && posQuery != null) {
      // if both term and POS are set, we create a query where both are at the same position
      if (mustOccur(termQuery) && mustOccur(posQuery)) {
        SpanQuery spanQueryForTerm = asSpanQuery(termQuery);
        SpanQuery spanQueryForPos = asSpanQuery(posQuery);
        SpanQuery[] spanClauses = {spanQueryForTerm, spanQueryForPos};
        return new BooleanClause(new SpanNearQuery(spanClauses, 0, false), BooleanClause.Occur.MUST);
      } else {
        // should not happen, we always use Occur.MUST:
        throw new UnsupportedPatternRuleException("Term/POS combination not supported yet: " + patternToken);
      }
    
    } else if (termQuery != null) {
      return termQuery;

    } else if (posQuery != null) {
      return posQuery;
    }
    throw new UnsupportedPatternRuleException("Neither POS tag nor term set for element: " + patternToken);
  }

  private SpanQuery asSpanQuery(BooleanClause query) {
    if (query.getQuery() instanceof MultiTermQuery) {
      return new SpanMultiTermQueryWrapper<>((MultiTermQuery) query.getQuery());
    } else {
      Set<Term> terms = new HashSet<>();
      try {
        indexSearcher.createWeight(query.getQuery(), false).extractTerms(terms);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (terms.size() != 1) {
        throw new RuntimeException("Expected term set of size 1: " + terms);
      }
      return new SpanTermQuery(terms.iterator().next());
    }
  }

  private boolean mustOccur(BooleanClause query) {
    return query != null && query.getOccur() == BooleanClause.Occur.MUST;
  }

  @Nullable
  private BooleanClause getTermQueryOrNull(PatternToken patternToken, String termStr) throws UnsupportedPatternRuleException {
    if (termStr == null || termStr.isEmpty()) {
      return null;
    }
    Query termQuery;
    Term termQueryTerm = getTermQueryTerm(patternToken, termStr);
    if (patternToken.getNegation() || patternToken.getMinOccurrence() == 0) {
      // we need to ignore this - negation, if any, must happen at the same position
      return null;
    } else if (patternToken.isInflected() && patternToken.isRegularExpression()) {
      Term lemmaQueryTerm = getQueryTerm(patternToken, LEMMA_PREFIX + "(", simplifyRegex(termStr), ")");
      Query regexpQuery = getRegexQuery(lemmaQueryTerm, termStr, patternToken);
      return new BooleanClause(regexpQuery, BooleanClause.Occur.MUST);
    } else if (patternToken.isInflected() && !patternToken.isRegularExpression()) {
      /*
      This is simpler, but leads to problem with e.g. German rules ZEITLICH_SYNCHRON and GEWISSEN_SUBST:
      Term lemmaQueryTerm = getQueryTerm(element, LEMMA_PREFIX, termStr, "");
      Query query = new TermQuery(lemmaQueryTerm);
      return new BooleanClause(query, BooleanClause.Occur.MUST);
      */
      Synthesizer synthesizer = language.getSynthesizer();
      if (synthesizer != null) {
        try {
          String[] synthesized = synthesizer.synthesize(new AnalyzedToken(termStr, null, termStr), ".*", true);
          Query query;
          if (synthesized.length == 0) {
            query = new TermQuery(termQueryTerm);
          } else {
            query = new RegexpQuery(getTermQueryTerm(patternToken, StringUtils.join(synthesized, "|")));
          }
          return new BooleanClause(query, BooleanClause.Occur.MUST);
        } catch (IOException e) {
          throw new RuntimeException("Could not build Lucene query for '" + patternToken + "' and '" + termStr + "'", e);
        }
      }
      return null;
    } else if (patternToken.isRegularExpression()) {
      termQuery = getRegexQuery(termQueryTerm, termStr, patternToken);
    } else {
      termQuery = new TermQuery(termQueryTerm);
    }
    return new BooleanClause(termQuery, BooleanClause.Occur.MUST);
  }

  // regex syntax not supported, but doesn't matter - remove or simplify it:
  private String simplifyRegex(String regex) {
    return regex.replace("(?:", "(").replace("\\d", "[0-9]").replace("\\w", "[a-zA-Z_0-9]");
  }

  // the new and fast Regex query of Lucene doesn't support full Java regex syntax:
  private boolean needsSimplification(String regex) {
    return regex.contains("(?:") || regex.contains("\\d") || regex.contains("\\w");
  }

  @Nullable
  private BooleanClause getPosQueryOrNull(PatternToken patternToken, String pos) throws UnsupportedPatternRuleException {
    if (pos == null || pos.isEmpty()) {
      return null;
    }
    Query posQuery;
    Term posQueryTerm;
    if (patternToken.getPOSNegation() || patternToken.getMinOccurrence() == 0) {
      // we need to ignore this - negation, if any, must happen at the same position
      return null;
    } else if (patternToken.isPOStagRegularExpression()) {
      posQueryTerm = getQueryTerm(patternToken, POS_PREFIX + "(", pos, ")");
      posQuery = getRegexQuery(posQueryTerm, pos, patternToken);
    } else {
      posQueryTerm = getQueryTerm(patternToken, POS_PREFIX, pos, "");
      posQuery = new TermQuery(posQueryTerm);
    }
    return new BooleanClause(posQuery, BooleanClause.Occur.MUST);
  }

  private Term getTermQueryTerm(PatternToken patternToken, String str) {
    if (patternToken.isCaseSensitive()) {
      return new Term(FIELD_NAME, str);
    } else {
      return new Term(FIELD_NAME_LOWERCASE, str.toLowerCase());
    }
  }

  private Term getQueryTerm(PatternToken patternToken, String prefix, String str, String suffix) {
    if (patternToken.isCaseSensitive()) {
      return new Term(FIELD_NAME, prefix + str + suffix);
    } else {
      return new Term(FIELD_NAME_LOWERCASE, prefix.toLowerCase() + str.toLowerCase() + suffix.toLowerCase());
    }
  }

  private Query getRegexQuery(Term term, String str, PatternToken patternToken) throws UnsupportedPatternRuleException {
    try {
      if (needsSimplification(str)) {
        Term newTerm = new Term(term.field(), simplifyRegex(term.text()));
        return new RegexpQuery(newTerm);
      }
      if (str.contains("?iu") || str.contains("?-i")) {
        // Lucene's RegexpQuery doesn't seem to support this
        throw new UnsupportedPatternRuleException("Regex constructs like '?iu' and '?-i' are not supported: " + patternToken);
      }
      return new RegexpQuery(term);
    } catch (IllegalArgumentException e) {
      // constructs like "\p{Punct}" not supported by Lucene RegExp:
      throw new UnsupportedPatternRuleException("Advanced regex like '\\p{Punct}' are not supported: " + patternToken);
    }
  }

  private void checkUnsupportedElement(PatternToken patternPatternToken)
      throws UnsupportedPatternRuleException {
    if (patternPatternToken.hasOrGroup()) {
      // TODO: this is not enough: the first of the tokens in the <or> group will not get into this branch
      throw new UnsupportedPatternRuleException("<or> not yet supported.");
    }
    if (patternPatternToken.isUnified()) {
      throw new UnsupportedPatternRuleException("Elements with unified tokens are not supported.");
    }
    if (patternPatternToken.getString().matches("\\\\\\d+")) { // e.g. "\1"
      throw new UnsupportedPatternRuleException("Elements with only match references (e.g. \\1) are not supported.");
    }
  }

}
