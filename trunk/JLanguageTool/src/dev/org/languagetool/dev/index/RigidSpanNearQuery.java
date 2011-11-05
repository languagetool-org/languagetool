package org.languagetool.dev.index;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.ToStringUtils;

public class RigidSpanNearQuery extends SpanNearQuery {
  /**
   * Construct a SpanNearQuery. Matches spans matching a span from each clause, with up to
   * <code>slop</code> total unmatched positions between them. * When <code>inOrder</code> is true,
   * the spans from each clause must be * ordered as in <code>clauses</code>.
   * 
   * @param clauses
   *          the clauses to find near each other
   * @param slop
   *          The slop value
   * @param inOrder
   *          true if order is important
   * */
  public RigidSpanNearQuery(SpanQuery[] clauses, int slop, boolean inOrder) {
    this(clauses, slop, inOrder, true);
  }

  public RigidSpanNearQuery(SpanQuery[] clauses, int slop, boolean inOrder, boolean collectPayloads) {
    super(clauses, slop, inOrder, collectPayloads);
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("rigidSpanNear([");
    Iterator<SpanQuery> i = clauses.iterator();
    while (i.hasNext()) {
      SpanQuery clause = i.next();
      buffer.append(clause.toString(field));
      if (i.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append("], ");
    buffer.append(slop);
    buffer.append(", ");
    buffer.append(inOrder);
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  @Override
  public Spans getSpans(final IndexReader reader) throws IOException {
    if (clauses.size() == 0) // optimize 0-clause case
      return new SpanOrQuery(getClauses()).getSpans(reader);

    if (clauses.size() == 1) // optimize 1-clause case
      return clauses.get(0).getSpans(reader);

    final Spans spans = new RigidNearSpansOrdered(this, reader);
    return spans;
    // return inOrder ? (Spans) new RigidNearSpansOrdered(this, reader)
    // : (Spans) new RigidNearSpansOrdered(this, reader);
  }

  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    RigidSpanNearQuery clone = null;
    for (int i = 0; i < clauses.size(); i++) {
      final SpanQuery c = clauses.get(i);
      final SpanQuery query = (SpanQuery) c.rewrite(reader);
      if (query != c) { // clause rewrote: must clone
        if (clone == null)
          clone = (RigidSpanNearQuery) this.clone();
        clone.clauses.set(i, query);
      }
    }
    if (clone != null) {
      return clone; // some clauses rewrote
    } else {
      return this; // no clauses rewrote
    }
  }

  @Override
  public Object clone() {
    final int sz = clauses.size();
    final SpanQuery[] newClauses = new SpanQuery[sz];
    for (int i = 0; i < sz; i++) {
      newClauses[i] = (SpanQuery) clauses.get(i).clone();
    }
    final RigidSpanNearQuery spanNearQuery = new RigidSpanNearQuery(newClauses, slop, inOrder);
    spanNearQuery.setBoost(getBoost());
    return spanNearQuery;
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RigidSpanNearQuery)) {
      return false;
    }

    final RigidSpanNearQuery spanNearQuery = (RigidSpanNearQuery) o;

    if (inOrder != spanNearQuery.inOrder) {
      return false;
    }
    if (slop != spanNearQuery.slop) {
      return false;
    }
    if (!clauses.equals(spanNearQuery.clauses)) {
      return false;
    }

    return getBoost() == spanNearQuery.getBoost();
  }

  @Override
  public int hashCode() {
    int result;
    result = clauses.hashCode();
    // Mix bits before folding in things like boost, since it could cancel the
    // last element of clauses. This particular mix also serves to
    // differentiate SpanNearQuery hashcodes from others.
    result ^= (result << 14) | (result >>> 19); // reversible
    result += Float.floatToRawIntBits(getBoost());
    result += slop;
    result ^= (inOrder ? 0x99AFD3BD : 0);
    return result;
  }
}
