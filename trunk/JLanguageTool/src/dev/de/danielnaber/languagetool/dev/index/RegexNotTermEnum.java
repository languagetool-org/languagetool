package de.danielnaber.languagetool.dev.index;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.regex.RegexCapabilities;

/**
 * Subclass of FilteredTermEnum for enumerating all terms that *DOES NOT* match the specified
 * regular expression term using the specified regular expression implementation.
 * <p>
 * Term enumerations are always ordered by Term.compareTo(). Each term in the enumeration is greater
 * than all that precede it.
 */

public class RegexNotTermEnum extends FilteredTermEnum {
  private String field = "";

  private String pre = "";

  private boolean endEnum = false;

  private RegexCapabilities regexImpl;

  public RegexNotTermEnum(IndexReader reader, Term term, RegexCapabilities regexImpl)
      throws IOException {
    super();
    field = term.field();
    String text = term.text();
    this.regexImpl = regexImpl;

    regexImpl.compile(text);

    // pre = regexImpl.prefix();
    // if (pre == null)
    // pre = "";

    setEnum(reader.terms(new Term(term.field(), pre)));
  }

  @Override
  protected final boolean termCompare(Term term) {
    if (field == term.field()) {
      String searchText = term.text();
      return !regexImpl.match(searchText);
      // if (searchText.startsWith(pre)) {
      // return regexImpl.match(searchText);
      // }
    }
    endEnum = true;
    return false;
  }

  @Override
  public final float difference() {
    // TODO: adjust difference based on distance of searchTerm.text() and
    // term().text()
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
