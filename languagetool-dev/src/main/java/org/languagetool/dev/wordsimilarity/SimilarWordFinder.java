/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wordsimilarity;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.*;

class SimilarWordFinder {

    private void createIndex(List<String> words, File path) throws IOException {
        FSDirectory dir = FSDirectory.open(path.toPath());
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
        System.out.println("Creating index...");
        int docs = 0;
        try (IndexWriter writer = new IndexWriter(dir, indexWriterConfig)) {
            for (String word : words) {
                Document doc = new Document();
                doc.add(new TextField("word", word, Field.Store.YES));
                writer.addDocument(doc);
                docs++;
            }
        }
        System.out.println("Index created: " + docs + " docs");
    }

    private void findSimilarWords(File path, List<String> words) throws IOException {
        FSDirectory dir = FSDirectory.open(path.toPath());
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            for (String word : words) {
                //System.out.println("------------------");
                FuzzyQuery query = new FuzzyQuery(new Term("word", word), 1);
                TopDocs topDocs = searcher.search(query, 10);
                //System.out.println(topDocs.totalHits + " hits for " + word);
                findSimilarWordsFor(reader, word, topDocs);
            }
        }
    }

    private void findSimilarWordsFor(DirectoryReader reader, String word, TopDocs topDocs) throws IOException {
        List<String> result = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            String simWord = reader.document(scoreDoc.doc).get("word");
            if (!simWord.equalsIgnoreCase(word)) {
                int firstDiffPos = getDiffPos(simWord.toLowerCase(), word.toLowerCase());
                int limit = Math.min(word.length(), simWord.length())-1;
                if (firstDiffPos > limit) {
                    //System.out.println("FILTERED: " + word + " -> " + simWord + " [" + firstDiffPos + " <= " + limit + "]");
                } else {
                    System.out.println(word + " -> " + simWord + " [" + firstDiffPos + "]");
                    result.add(simWord);
                }
            }
        }
        // TODO: sort by keyboard distance
    }

    private int getDiffPos(String s1, String s2) {
        int i;
        for (i = 0; i < s1.length(); i++) {
            if (i >= s2.length()) {
                return i;
            }
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        return i;
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: " + SimilarWordFinder.class.getSimpleName() + " <file>");
            System.exit(1);
        }
        //List<String> words = Arrays.asList("Mediation", "Meditation", "Medallion", "Messe", "Muschel", "Medizin");
        List<String> words = FileUtils.readLines(new File(args[0]));
        SimilarWordFinder simWordFinder = new SimilarWordFinder();
        File path = new File("/tmp/test");
        //Files.deleteIfExists(path.toPath());
        //simWordFinder.createIndex(words, path);
        simWordFinder.findSimilarWords(path, words);
    }
}
