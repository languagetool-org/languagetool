package de.danielnaber.languagetool.dev.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.regex.*;
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
    RegexNotQuery query = new RegexNotQuery(newTerm(regex));

    if (capability != null) {
      query.setRegexImplementation(capability);
    }

    return searcher.search(query, null, 1000).totalHits;
  }

  private int spanRegexQueryNrHits(String regex1, String regex2, int slop, boolean ordered)
      throws Exception {
    SpanRegexNotQuery srq1 = new SpanRegexNotQuery(newTerm(regex1));
    SpanRegexQuery srq2 = new SpanRegexQuery(newTerm(regex2));
    SpanNearQuery query = new SpanNearQuery(new SpanQuery[] { srq1, srq2 }, slop, ordered);

    return searcher.search(query, null, 1000).totalHits;
  }

  public void testMatchAll() throws Exception {
    TermEnum terms = new RegexNotQuery(new Term(FN, "^q.[aeiou]c.*$")).getEnum(searcher
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
    RegexNotQuery query1 = new RegexNotQuery(newTerm("foo.*"));
    query1.setRegexImplementation(new JakartaRegexpCapabilities());

    RegexNotQuery query2 = new RegexNotQuery(newTerm("foo.*"));
    assertFalse(query1.equals(query2));

    RegexQuery query3 = new RegexQuery(newTerm("foo.*"));
    assertFalse(query2.equals(query3));
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
