package org.languagetool.dev.index;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.regex.RegexQuery;
import org.apache.lucene.util.ToStringUtils;

public class POSAwaredRegexQuery extends RegexQuery {

  private static final long serialVersionUID = -7478945008142212501L;

  private final boolean isPOS;

  /** Constructs a query for terms matching <code>term</code>. */
  public POSAwaredRegexQuery(Term term, boolean isPOS) {
    super(term);
    this.isPOS = isPOS;
  }

  public boolean isPOS() {
    return isPOS;
  }

  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new POSAwaredRegexTermEnum(reader, this.getTerm(), this.getRegexImplementation(),
        this.isPOS());
  }

  @Override
  public String toString(String field) {
    final StringBuilder buffer = new StringBuilder();
    if (!this.getTerm().field().equals(field)) {
      buffer.append(this.getTerm().field());
      buffer.append(":");
    }
    buffer.append(this.isPOS() ? "$POS$" : "");
    buffer.append(this.getTerm().text());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

}