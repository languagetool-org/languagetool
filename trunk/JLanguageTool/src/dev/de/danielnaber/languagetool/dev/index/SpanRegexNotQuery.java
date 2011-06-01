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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.search.regex.RegexCapabilities;
import org.apache.lucene.search.regex.RegexQueryCapable;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;

/**
 * A SpanQuery version of {@link RegexNotQuery} allowing regular expression queries to be nested
 * within other SpanQuery subclasses.
 * 
 */

public class SpanRegexNotQuery extends SpanMultiTermQueryWrapper<RegexNotQuery> implements
    RegexQueryCapable {
  private final RegexCapabilities regexImpl = new JavaUtilRegexCapabilities();

  public SpanRegexNotQuery(Term term) {
    super(new RegexNotQuery(term));
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
