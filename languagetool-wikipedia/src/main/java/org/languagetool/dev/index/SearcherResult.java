/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.lucene.search.Query;

import java.util.List;

/**
 * The result of a {@link Searcher}.
 */
public class SearcherResult {

  private final List<MatchingSentence> matchingSentences;
  private final int checkedSentences;
  private final Query relaxedQuery;

  private boolean resultIsTimeLimited;
  private int docCount;
  private boolean hasTooManyLuceneMatches;
  private int luceneMatchCount;

  public SearcherResult(List<MatchingSentence> matchingSentences, int checkedSentences, Query relaxedQuery) {
    this.matchingSentences = matchingSentences;
    this.checkedSentences = checkedSentences;
    this.relaxedQuery = relaxedQuery;
  }

  public List<MatchingSentence> getMatchingSentences() {
    return matchingSentences;
  }

  public int getCheckedSentences() {
    return checkedSentences;
  }

  public Query getRelaxedQuery() {
    return relaxedQuery;
  }

  public boolean isResultIsTimeLimited() {
    return resultIsTimeLimited;
  }

  public void setResultIsTimeLimited(boolean resultIsTimeLimited) {
    this.resultIsTimeLimited = resultIsTimeLimited;
  }

  public void setDocCount(int docCount) {
    this.docCount = docCount;
  }

  public int getDocCount() {
    return docCount;
  }

  public void setHasTooManyLuceneMatches(boolean hasTooManyLuceneMatches) {
    this.hasTooManyLuceneMatches = hasTooManyLuceneMatches;
  }

  public boolean hasTooManyLuceneMatches() {
    return hasTooManyLuceneMatches;
  }

  public void setLuceneMatchCount(int luceneMatchCount) {
    this.luceneMatchCount = luceneMatchCount;
  }

  public int getLuceneMatchCount() {
    return luceneMatchCount;
  }

}
