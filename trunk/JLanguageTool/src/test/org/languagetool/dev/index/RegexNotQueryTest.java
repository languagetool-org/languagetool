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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.regex.JakartaRegexpCapabilities;
import org.apache.lucene.search.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.search.regex.RegexCapabilities;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;

public class RegexNotQueryTest extends LuceneTestCase {
  private IndexSearcher searcher;

  private IndexReader reader;

  private Directory directory;

  private final String FN = "field";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    directory = newDirectory();
    RandomIndexWriter writer = new RandomIndexWriter(random, directory);
    Document doc = new Document();
    doc.add(newField(FN, "the quick brown fox jumps over the lazy dog", Field.Store.NO,
        Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(newField(FN, "quick", Field.Store.NO, Field.Index.ANALYZED));
    writer.addDocument(doc);
    reader = writer.getReader();
    writer.close();
    searcher = newSearcher(reader);
  }

  @Override
  public void tearDown() throws Exception {
    searcher.close();
    reader.close();
    directory.close();
    super.tearDown();
  }

  private Term newTerm(String value) {
    return new Term(FN, value);
  }

  private int regexQueryNrHits(String regex, RegexCapabilities capability) throws Exception {
    POSAwaredRegexNotQuery query = new POSAwaredRegexNotQuery(newTerm(regex), false);

    if (capability != null) {
      query.setRegexImplementation(capability);
    }

    return searcher.search(query, null, 1000).totalHits;
  }

  private int spanRegexQueryNrHits(String regex1, String regex2, int slop, boolean ordered)
      throws Exception {
    POSAwaredSpanRegexNotQuery srq1 = new POSAwaredSpanRegexNotQuery(newTerm(regex1), false);
    POSAwaredSpanRegexQuery srq2 = new POSAwaredSpanRegexQuery(newTerm(regex2), false);
    SpanNearQuery query = new SpanNearQuery(new SpanQuery[] { srq1, srq2 }, slop, ordered);

    return searcher.search(query, null, 1000).totalHits;
  }

  public void testMatchAll() throws Exception {
    TermEnum terms = new POSAwaredRegexNotQuery(new Term(FN, "^q.[aeiou]c.*$"), false).getEnum(searcher
        .getIndexReader());
    do {
      Term term = terms.term();
      assertNotNull(term);
    } while (terms.next());
  }

  public void testRegex1() throws Exception {
    assertEquals(1, regexQueryNrHits("^q.[aeiou]c.*$", null));
  }

  public void testRegex2() throws Exception {
    assertEquals(2, regexQueryNrHits("^.[aeiou]c.*$", null));
  }

  public void testRegex3() throws Exception {
    assertEquals(2, regexQueryNrHits("^q.[aeiou]c$", null));
  }

  public void testSpanRegex1() throws Exception {
    assertEquals(0, spanRegexQueryNrHits("^lazy$", "dog", 0, true));
  }

  public void testSpanRegex2() throws Exception {
    assertEquals(1, spanRegexQueryNrHits("^lazy$", "dog", 1, true));
  }

  public void testEquals() throws Exception {
    POSAwaredRegexNotQuery query1 = new POSAwaredRegexNotQuery(newTerm("foo.*"), false);
    query1.setRegexImplementation(new JakartaRegexpCapabilities());

    POSAwaredRegexNotQuery query2 = new POSAwaredRegexNotQuery(newTerm("foo.*"), false);
    assertFalse(query1.equals(query2));

    POSAwaredRegexQuery query3 = new POSAwaredRegexQuery(newTerm("foo.*"), false);
    assertFalse(query2.equals(query3));

    assertFalse(query3.equals(query2));
  }

  // Both of the two documents contain terms that does not match "^.*QUICK.*$" in case-sensitive
  // way.
  // Document No.1 : the, quick, brown
  // Document No.2 : quick
  // Hits count is 2.
  public void testJakartaCaseSensitive() throws Exception {
    assertEquals(2, regexQueryNrHits("^.*QUICK.*$", null));
  }

  public void testJavaUtilCaseSensitive() throws Exception {
    assertEquals(2, regexQueryNrHits("^.*QUICK.*$", null));
  }

  // Only document No.1 contain terms that does not match "^.*QUICK.*$" in case-insensitive
  // way.
  // Document No.1 : the, brown, fox ...
  // Document No.2 :
  // Hits count is 1.
  public void testJakartaCaseInsensitive() throws Exception {
    assertEquals(
        1,
        regexQueryNrHits("^.*QUICK.*$", new JakartaRegexpCapabilities(
            JakartaRegexpCapabilities.FLAG_MATCH_CASEINDEPENDENT)));
  }

  public void testJavaUtilCaseInsensitive() throws Exception {
    assertEquals(
        1,
        regexQueryNrHits("^.*QUICK.*$", new JavaUtilRegexCapabilities(
            JavaUtilRegexCapabilities.FLAG_CASE_INSENSITIVE)));
  }

}
