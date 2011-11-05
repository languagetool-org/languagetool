package org.languagetool.dev.index;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.regex.RegexCapabilities;

public class POSAwaredRegexTermEnum extends FilteredTermEnum {
  
  private final RegexCapabilities regexImpl;
  private final boolean isPOS;

  private String field = "";
  private String pre = "";
  private boolean endEnum = false;

  public POSAwaredRegexTermEnum(IndexReader reader, Term term, RegexCapabilities regexImpl, boolean isPOS)
      throws IOException {
    super();
    field = term.field();
    this.regexImpl = regexImpl;
    this.isPOS = isPOS;
    regexImpl.compile(term.text());
    pre = regexImpl.prefix();
    if (pre == null) {
      pre = "";
    }
    setEnum(reader.terms(new Term(term.field(), pre)));
  }

  @Override
  protected final boolean termCompare(Term term) {
    if (field == term.field()) {
      String searchText = term.text();

      if ((isPOS && !searchText.startsWith(LanguageToolFilter.POS_PREFIX))
          || (!isPOS && searchText.startsWith(LanguageToolFilter.POS_PREFIX))) {
        return false;
      }

      if (isPOS) {
        searchText = searchText.replaceFirst(LanguageToolFilter.POS_PREFIX, "");
        // System.out.println(searchText);
      }
      if (searchText.startsWith(pre)) {
        // System.out.println("1:" + searchText);
        // System.out.println("2:" + text);
        // System.out.println("3:" + regexImpl.match(searchText));
        return regexImpl.match(searchText);
      }
    }
    endEnum = true;
    return false;
  }

  @Override
  public final float difference() {
    // TODO: adjust difference based on distance of searchTerm.text() and term().text()
    return 1.0f;
  }

  @Override
  public final boolean endEnum() {
    return endEnum;
  }

  @Override
  public void close() throws IOException {
    super.close();
    field = null;
  }
}
