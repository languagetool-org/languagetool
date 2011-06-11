package de.danielnaber.languagetool.dev.index;

import java.util.ArrayList;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.regex.SpanRegexQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

public class PatternRuleQueryBuilder {

  public static final String FN = "field";

  public static Query bulidQuery(PatternRule rule) {
    ArrayList<SpanQuery> list = new ArrayList<SpanQuery>();
    int i = 0;
    for (Element patternElement : rule.getElements()) {
      SpanQuery termQuery = createSpanQuery(patternElement.getString(), "",
          patternElement.getNegation(), patternElement.isRegularExpression());
      SpanQuery posQuery = createSpanQuery(patternElement.getPOStag(),
          LanguageToolFilter.POS_PREFIX, patternElement.getPOSNegation(),
          patternElement.isPOStagRegularExpression());

      if (termQuery != null && posQuery != null) {
        SpanNearQuery q = new SpanNearQuery(new SpanQuery[] { termQuery, posQuery }, 0, false);
        list.add(q);
      } else if (termQuery != null) {
        list.add(termQuery);
      } else if (posQuery != null) {
        list.add(posQuery);
      } else {
        i++;
      }
      i += patternElement.getSkipNext();

    }

    SpanNearQuery snq = new SpanNearQuery(list.toArray(new SpanQuery[list.size()]), i, true);
    return snq;
  }

  private static SpanQuery createSpanQuery(String token, String prefix, boolean isNegation,
      boolean isRegularExpression) {
    SpanQuery q = null;
    if (token != null && !token.equals("")) {
      Term term = new Term(FN, prefix + token);
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
