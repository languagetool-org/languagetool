package org.languagetool.dev.index;

/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.ArrayUtil;

/**
 * Taken from Lucene and adapted.
 */
public class RigidNearSpansOrdered extends Spans {
  
  private final int allowedSlop;
  private boolean firstTime = true;
  private boolean more = false;

  /** The spans in the same order as the SpanNearQuery */
  private final Spans[] subSpans;

  /** Indicates that all subSpans have same doc() */
  private boolean inSameDoc = false;

  private int matchDoc = -1;
  private int matchStart = -1;
  private int matchEnd = -1;

  private List<byte[]> matchPayload;

  private final Spans[] subSpansByDoc;

  private final Comparator<Spans> spanDocComparator = new Comparator<Spans>() {
    public int compare(Spans o1, Spans o2) {
      return o1.doc() - o2.doc();
    }
  };

  private RigidSpanNearQuery query;

  private boolean collectPayloads = true;

  public RigidNearSpansOrdered(RigidSpanNearQuery spanNearQuery, IndexReader reader)
      throws IOException {
    this(spanNearQuery, reader, true);
  }

  public RigidNearSpansOrdered(RigidSpanNearQuery spanNearQuery, IndexReader reader,
      boolean collectPayloads) throws IOException {
    if (spanNearQuery.getClauses().length < 2) {
      throw new IllegalArgumentException("Less than 2 clauses: " + spanNearQuery);
    }
    this.collectPayloads = collectPayloads;
    allowedSlop = spanNearQuery.getSlop();
    final SpanQuery[] clauses = spanNearQuery.getClauses();
    subSpans = new Spans[clauses.length];
    matchPayload = new LinkedList<byte[]>();
    subSpansByDoc = new Spans[clauses.length];
    for (int i = 0; i < clauses.length; i++) {
      subSpans[i] = clauses[i].getSpans(reader);
      subSpansByDoc[i] = subSpans[i]; // used in toSameDoc()
    }
    query = spanNearQuery; // kept for toString() only.
  }

  // inherit javadocs
  @Override
  public int doc() {
    return matchDoc;
  }

  // inherit javadocs
  @Override
  public int start() {
    return matchStart;
  }

  // inherit javadocs
  @Override
  public int end() {
    return matchEnd;
  }

  public Spans[] getSubSpans() {
    return subSpans;
  }

  // TODO: Remove warning after API has been finalized
  // TODO: Would be nice to be able to lazy load payloads
  @Override
  public Collection<byte[]> getPayload() throws IOException {
    return matchPayload;
  }

  // TODO: Remove warning after API has been finalized
  @Override
  public boolean isPayloadAvailable() {
    return !matchPayload.isEmpty();
  }

  // inherit javadocs
  @Override
  public boolean next() throws IOException {
    if (firstTime) {
      firstTime = false;
      for (Spans subSpan : subSpans) {
        if (!subSpan.next()) {
          more = false;
          return false;
        }
      }
      more = true;
    }
    if (collectPayloads) {
      matchPayload.clear();
    }
    return advanceAfterOrdered();
  }

  // inherit javadocs
  @Override
  public boolean skipTo(int target) throws IOException {
    if (firstTime) {
      firstTime = false;
      for (Spans subSpan : subSpans) {
        if (!subSpan.skipTo(target)) {
          more = false;
          return false;
        }
      }
      more = true;
    } else if (more && (subSpans[0].doc() < target)) {
      if (subSpans[0].skipTo(target)) {
        inSameDoc = false;
      } else {
        more = false;
        return false;
      }
    }
    if (collectPayloads) {
      matchPayload.clear();
    }
    return advanceAfterOrdered();
  }

  /**
   * Advances the subSpans to just after an ordered match with a minimum slop that is smaller than
   * the slop allowed by the SpanNearQuery.
   * 
   * @return true iff there is such a match.
   */
  private boolean advanceAfterOrdered() throws IOException {
    while (more && (inSameDoc || toSameDoc())) {
      if (stretchToOrder() && shrinkToAfterShortestMatch()) {
        return true;
      }
    }
    return false; // no more matches
  }

  /** Advance the subSpans to the same document */
  private boolean toSameDoc() throws IOException {
    ArrayUtil.quickSort(subSpansByDoc, spanDocComparator);
    int firstIndex = 0;
    int maxDoc = subSpansByDoc[subSpansByDoc.length - 1].doc();
    while (subSpansByDoc[firstIndex].doc() != maxDoc) {
      if (!subSpansByDoc[firstIndex].skipTo(maxDoc)) {
        more = false;
        inSameDoc = false;
        return false;
      }
      maxDoc = subSpansByDoc[firstIndex].doc();
      if (++firstIndex == subSpansByDoc.length) {
        firstIndex = 0;
      }
    }
    for (Spans aSubSpansByDoc : subSpansByDoc) {
      assert (aSubSpansByDoc.doc() == maxDoc) : " NearSpansOrdered.toSameDoc() spans "
              + subSpansByDoc[0] + "\n at doc " + aSubSpansByDoc.doc() + ", but should be at "
              + maxDoc;
    }
    inSameDoc = true;
    return true;
  }

  /**
   * Check whether two Spans in the same document are ordered.
   * 
   * @param spans1
   * @param spans2
   * @return true iff spans1 starts before spans2 or the spans start at the same position, and
   *         spans1 ends before spans2.
   */
  static final boolean docSpansOrdered(Spans spans1, Spans spans2, int allowedSlop) {
    assert spans1.doc() == spans2.doc() : "doc1 " + spans1.doc() + " != doc2 " + spans2.doc();
    final int start1 = spans1.start();
    final int start2 = spans2.start();
    /* Do not call docSpansOrdered(int,int,int,int) to avoid invoking .end() : */
    // return (start1 == start2) ? (spans1.end() <= spans2.end()) : (start1 < start2);
    if (allowedSlop == 0) {
      return (start1 == start2) ? (spans1.end() <= spans2.end()) : (start1 < start2);
    } else {
      return (start1 == start2) ? (spans1.end() < spans2.end()) : (start1 < start2);
    }
  }

  /**
   * Like {@link #docSpansOrdered(Spans,Spans)}, but use the spans starts and ends as parameters.
   */
  private static final boolean docSpansOrdered(int start1, int end1, int start2, int end2,
      int allowedSlop) {
    if (allowedSlop == 0) {
      return (start1 == start2) ? (end1 <= end2) : (start1 < start2);
    } else {
      return (start1 == start2) ? (end1 < end2) : (start1 < start2);
    }
  }

  /**
   * Order the subSpans within the same document by advancing all later spans after the previous
   * one.
   */
  private boolean stretchToOrder() throws IOException {
    matchDoc = subSpans[0].doc();
    for (int i = 1; inSameDoc && (i < subSpans.length); i++) {
      while (!docSpansOrdered(subSpans[i - 1], subSpans[i], allowedSlop)) {
        if (!subSpans[i].next()) {
          inSameDoc = false;
          more = false;
          break;
        } else if (matchDoc != subSpans[i].doc()) {
          inSameDoc = false;
          break;
        }
      }
    }
    return inSameDoc;
  }

  /**
   * The subSpans are ordered in the same doc, so there is a possible match. Compute the slop while
   * making the match as short as possible by advancing all subSpans except the last one in reverse
   * order.
   */
  private boolean shrinkToAfterShortestMatch() throws IOException {

    matchStart = subSpans[subSpans.length - 1].start();
    matchEnd = subSpans[subSpans.length - 1].end();

    final Set<byte[]> possibleMatchPayloads = new HashSet<byte[]>();
    if (subSpans[subSpans.length - 1].isPayloadAvailable()) {
      possibleMatchPayloads.addAll(subSpans[subSpans.length - 1].getPayload());
    }

    Collection<byte[]> possiblePayload = null;

    int matchSlop = 0;
    int lastStart = matchStart;
    int lastEnd = matchEnd;
    for (int i = subSpans.length - 2; i >= 0; i--) {
      // System.out.println(subSpans[i].toString());
      final Spans prevSpans = subSpans[i];
      if (collectPayloads && prevSpans.isPayloadAvailable()) {
        final Collection<byte[]> payload = prevSpans.getPayload();
        possiblePayload = new ArrayList<byte[]>(payload.size());
        possiblePayload.addAll(payload);
      }

      int prevStart = prevSpans.start();
      int prevEnd = prevSpans.end();

      while (true) { // Advance prevSpans until after (lastStart, lastEnd)
        if (!prevSpans.next()) {
          inSameDoc = false;
          more = false;
          break; // Check remaining subSpans for final match.
        } else if (matchDoc != prevSpans.doc()) {
          inSameDoc = false; // The last subSpans is not advanced here.
          break; // Check remaining subSpans for last match in this document.
        } else {
          final int ppStart = prevSpans.start();
          final int ppEnd = prevSpans.end(); // Cannot avoid invoking .end()

          if (allowedSlop > 0 && ppStart == lastStart) {
            continue;
          }
          // System.out.println(ppStart + ":" + ppEnd + ":" + lastStart + ":" + lastEnd);

          if (!docSpansOrdered(ppStart, ppEnd, lastStart, lastEnd, allowedSlop)) {

            break; // Check remaining subSpans.
          } else { // prevSpans still before (lastStart, lastEnd)

            prevStart = ppStart;
            prevEnd = ppEnd;

            if (collectPayloads && prevSpans.isPayloadAvailable()) {
              final Collection<byte[]> payload = prevSpans.getPayload();
              possiblePayload = new ArrayList<byte[]>(payload.size());
              possiblePayload.addAll(payload);
            }
          }
        }
      }

      if (collectPayloads && possiblePayload != null) {
        possibleMatchPayloads.addAll(possiblePayload);
      }

      assert prevStart <= matchStart;

      if (matchStart >= prevEnd) { // Only non overlapping spans add to slop.
        matchSlop += (matchStart - prevEnd);
        // if (allowedSlop > 0) {
        matchSlop++;
        // }
      }

      /*
       * Do not break on (matchSlop > allowedSlop) here to make sure that subSpans[0] is advanced
       * after the match, if any.
       */
      matchStart = prevStart;
      lastStart = prevStart;
      lastEnd = prevEnd;
    }

    // System.out.println("allowedSlop: " + allowedSlop);
    // System.out.println("matchSlop: " + matchSlop);

    boolean match = false;
    if (allowedSlop == 0) {
      match = matchSlop == 0;
    } else {
      match = matchSlop <= allowedSlop && matchSlop > 0;
      // match = (matchSlop <= allowedSlop && matchSlop > 0)
      // || (subSpans[0].start() == 0 && subSpans[0].end() == 0);
    }

    if (collectPayloads && match && possibleMatchPayloads.size() > 0) {
      matchPayload.addAll(possibleMatchPayloads);
    }

    return match; // ordered and allowed slop
  }

  @Override
  public String toString() {
    return getClass().getName() + "(" + query.toString() + ")@"
        + (firstTime ? "START" : (more ? (doc() + ":" + start() + "-" + end()) : "END"));
  }
}
