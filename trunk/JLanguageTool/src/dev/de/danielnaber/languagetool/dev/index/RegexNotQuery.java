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
import org.apache.lucene.search.regex.RegexQuery;
import org.apache.lucene.search.regex.RegexTermEnum;
import org.apache.lucene.util.ToStringUtils;

/**
 * Implements the *NOT* regular expression term search query. The expressions supported depend on
 * the regular expression implementation used by way of the {@link RegexCapabilities} interface.
 * 
 * @see RegexTermEnum
 */

// TODO Tao: is it OK to make RegexNotQuery extends RegexQuery?
public class RegexNotQuery extends RegexQuery {

  private static final long serialVersionUID = -4990041844164168930L;

  /** Constructs a query for terms "NOT" matching <code>term</code>. */
  public RegexNotQuery(Term term) {
    super(term);
  }

  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new RegexNotTermEnum(reader, this.getTerm(), this.getRegexImplementation());
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!this.getTerm().field().equals(field)) {
      buffer.append(this.getTerm().field());
      buffer.append(":*NOT*");
    }
    buffer.append(this.getTerm().text());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

}
