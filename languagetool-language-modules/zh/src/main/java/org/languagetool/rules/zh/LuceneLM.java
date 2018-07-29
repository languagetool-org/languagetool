/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.zh;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LuceneLM {

  private static final Map<File, LuceneSearcher> dirToSearcherMap = new HashMap<>();

  private final List<File> indexes = new ArrayList<>();
  private final Map<Integer, LuceneSearcher> luceneSearcherMap = new HashMap<>();
  private final File topIndexDir;
  private final long maxNgram;

  public static void validateDirectory(File topIndexDir) {
    if (!topIndexDir.exists() || !topIndexDir.isDirectory()) {
      throw new RuntimeException("Not found or is not a directory:\n" +
              topIndexDir + "\n" +
              "As ngram directory, please select the directory that has a subdirectory like 'en'\n" +
              "(or whatever language code you're using).");
    }
    List<String> dirs = new ArrayList<>();
    for (String name: topIndexDir.list()) {
      if (name.matches("[123]grams")) {
        dirs.add(name);
      }
    }
    if (dirs.size() == 0) {
      throw new RuntimeException("Directory must contain at least '1grams', '2grams', '3grams': " + topIndexDir.getAbsolutePath());
    }
    if (dirs.size() < 3) {
      throw new RuntimeException("Expected at least '1grams', '2grams', and '3grams' sub directories but only got " + dirs + " in " + topIndexDir.getAbsolutePath());
    }
  }

  public LuceneLM(File topIndexDir) {
    doValidateDirectory(topIndexDir);
    this.topIndexDir = topIndexDir;
    addIndex(topIndexDir, 1);
    addIndex(topIndexDir, 2);
    addIndex(topIndexDir, 3);
    if (luceneSearcherMap.size() == 0) {
      throw new RuntimeException("No directories '1grams' ... '3grams' found in " + topIndexDir);
    }
    maxNgram = 3;
  }

  protected void doValidateDirectory(File topIndexDir) {
    validateDirectory(topIndexDir);
  }

  private void addIndex(File topIndexDir, int ngramSize) {
    File indexDir = new File(topIndexDir, ngramSize + "grams");
    if (indexDir.exists() && indexDir.isDirectory()) {
      if (luceneSearcherMap.containsKey(ngramSize)) {
        throw new RuntimeException("Searcher for ngram size " + ngramSize + " already exists");
      }
      luceneSearcherMap.put(ngramSize, getCachedLuceneSearcher(indexDir));
      indexes.add(indexDir);
    }
  }

  public double getLogProb(List<String> tokens) {
    if (tokens.size() > maxNgram) {
      throw new RuntimeException("Requested " + tokens.size() + " gram but index has only up to " + maxNgram + "gram: " + tokens);
    }
    Objects.requireNonNull(tokens);
//        Term term = new Term("ngram", String.join(" ",tokens));
    List<String> tokenList = getTokensWithUnk(tokens);
    return getLogProb(tokenList, false);
  }

  public double scoreSentence(List<String> ngrams) {
    List<String> sentence = new ArrayList<>();
    sentence.addAll(ngrams);
    sentence.add(0, "<s>");
    sentence.add(0, "<s>");
    sentence.add("</s>");
    double sentenceScore = 0;
    for (int i = 0; i < sentence.size() - 3; i ++) {
      sentenceScore += getLogProb(sentence.subList(i, i+3));
    }
    return sentenceScore;
  }

  protected LuceneSearcher getLuceneSearcher(int ngramSize) {
    LuceneSearcher luceneSearcher = luceneSearcherMap.get(ngramSize);
    if (luceneSearcher == null) {
      throw new RuntimeException("No " + ngramSize + "grams directory found in " + topIndexDir);
    }
    return luceneSearcher;
  }

  private LuceneSearcher getCachedLuceneSearcher(File indexDir) {
    LuceneSearcher lucenenSearcher = dirToSearcherMap.get(indexDir);
    if (lucenenSearcher == null) {
      try {
        LuceneSearcher newSearcher = new LuceneSearcher(indexDir);
        dirToSearcherMap.put(indexDir, newSearcher);
        return newSearcher;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return lucenenSearcher;
    }
  }

  private double getLogProb(List<String> tokens, boolean backoff) {
    if (tokens.size() == 0) {
      return 0;
    }
    double result = 0;
    Term term = new Term("ngram", String.join(" ", tokens));
    LuceneSearcher luceneSearcher = getLuceneSearcher(tokens.size());
    try {
      TopDocs docs = luceneSearcher.searcher.search(new TermQuery(term), 5);
      if (!backoff) {
        if (docs.totalHits == 0) {
          int middle = (int) Math.ceil((double)tokens.size() / 2);
          result = getLogProb(tokens.subList(0, middle), true) + getLogProb(tokens.subList(1, tokens.size()), false);
        } else {
          for (ScoreDoc scoreDoc : docs.scoreDocs) {
            String prob = luceneSearcher.reader.document(scoreDoc.doc).get("prob");
            result += Double.parseDouble(prob);
          }
        }
      } else {
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
          String backProb = luceneSearcher.reader.document(scoreDoc.doc).get("backProb");
          result += Double.parseDouble(backProb);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  List<String> getTokensWithUnk(List<String> tokens){
    List<String> result = new ArrayList<>();
    LuceneSearcher luceneSearcher = getLuceneSearcher(1);
    for (String token : tokens) {
      Term term = new Term("ngram", token);
      try {
        TopDocs docs = luceneSearcher.searcher.search(new TermQuery(term), 5);
        if (docs.totalHits == 0) {
          result.add("<unk>");
        } else {
          result.add(token);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return indexes.toString();
  }

  protected static class LuceneSearcher {
    final FSDirectory directory;
    final IndexReader reader;
    final IndexSearcher searcher;
    private LuceneSearcher(File indexDir) throws IOException {
      Path path = indexDir.toPath();
      if (Files.isSymbolicLink(path)) {
        path = indexDir.getCanonicalFile().toPath();
      }
      this.directory = FSDirectory.open(path);
      this.reader = DirectoryReader.open(directory);
      this.searcher = new IndexSearcher(reader);
    }
    public IndexReader getReader() {
      return reader;
    }
  }

}
