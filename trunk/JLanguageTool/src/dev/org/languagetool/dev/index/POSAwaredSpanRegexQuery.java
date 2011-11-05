package de.danielnaber.languagetool.dev.index;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.regex.RegexCapabilities;
import org.apache.lucene.search.regex.RegexQuery;
import org.apache.lucene.search.regex.RegexQueryCapable;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;

public class POSAwaredSpanRegexQuery extends SpanMultiTermQueryWrapper<RegexQuery> implements
    RegexQueryCapable {

  public POSAwaredSpanRegexQuery(Term term, boolean isPOS) {
    super(new POSAwaredRegexQuery(term, isPOS));
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
