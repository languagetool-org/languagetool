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

public class PatternRuleQueryBuilder {

  public static final String FIELD_NAME = "field";

  public static Query buildQuery(PatternRule rule) {
    return next(rule.getElements().iterator());
  }

  // create the next SpanQuery from the top Element in the Iterator.
  private static SpanQuery next(Iterator<Element> it) {

    // no more Element
    if (!it.hasNext()) {
      return null;
    }

    final Element patternElement = it.next();
    patternElement.getExceptionList();

    final ArrayList<SpanQuery> list = new ArrayList<SpanQuery>();

    int skip = 0;

    final SpanQuery termQuery = createSpanQuery(patternElement.getString(), "",
        patternElement.getNegation(), patternElement.isRegularExpression());
    final SpanQuery posQuery = createSpanQuery(patternElement.getPOStag(), LanguageToolFilter.POS_PREFIX,
        patternElement.getPOSNegation(), patternElement.isPOStagRegularExpression());

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
