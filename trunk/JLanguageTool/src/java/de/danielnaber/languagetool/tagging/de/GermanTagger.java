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
package de.danielnaber.languagetool.tagging.de;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * Experimental German tagger, requires data files in <code>resource/de/categories</code>.
 * 
 * @author Daniel Naber
 */
public class GermanTagger implements Tagger {

  public static final String FULLFORM_FIELD = "fullform";
  public static final String CATEGORIES_FIELD = "categories";
  
  private static final String INDEX_DIR = "resource" +File.separator+ "de" +File.separator+ "categories";
  private IndexSearcher searcher = null;

  public GermanTagger() {
  }

  public AnalyzedGermanToken lookup(String word, int startPos) throws IOException {
    return lookup(word, startPos, false);
  }
  
  private AnalyzedGermanToken lookup(String word, int startPos, boolean makeLowercase) throws IOException {
    initSearcher();
    Term term = null;
    if (makeLowercase)
      term = new Term(FULLFORM_FIELD, word.toLowerCase());
    else
      term = new Term(FULLFORM_FIELD, word);
    Query query = new TermQuery(term);
    Hits hits = searcher.search(query);
    if (hits.length() == 0) {
      return null;
    } else {
      List l = new ArrayList();
      for (int j = 0; j < hits.length(); j++) {
        Document doc = hits.doc(j);
        Field[] fields = doc.getFields(CATEGORIES_FIELD);
        if (fields != null) {
          for (int i = 0; i < fields.length; i++) {
            String val = fields[i].stringValue();
            if (!val.equals("")) {
              if (val.endsWith("O")) {        // originally from "NOG"
                // TODO: what exactly does "NOG" mean?! for now, assume
                // both MAS and FEM:
                String val1 = val.replaceFirst("O$", "M");
                GermanTokenReading tokenReading1 =
                  GermanTokenReading.createTokenReadingFromMorphyString(val1, word);
                l.add(tokenReading1);
                String val2 = val.replaceFirst("O$", "F");
                GermanTokenReading tokenReading2 =
                  GermanTokenReading.createTokenReadingFromMorphyString(val2, word);
                l.add(tokenReading2);
              } else {
                GermanTokenReading tokenReading =
                  GermanTokenReading.createTokenReadingFromMorphyString(val, word);
                l.add(tokenReading);
              }
            }
          }
        }
      }
      AnalyzedGermanToken aToken = new AnalyzedGermanToken(word, l, startPos);
      return aToken;
    }
  }
  
  public List tag(List tokens) throws IOException {
    initSearcher();
    List posTags = new ArrayList();
    int pos = 0;
    boolean firstWord = true;
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String word = (String) iter.next();
      AnalyzedGermanToken aToken = lookup(word, pos);
      if (firstWord && aToken == null) {        // e.g. "Das" -> "das" at start of sentence
        aToken = lookup(word, pos, true);
        firstWord = false;
      }
      pos += word.length();
      if (aToken != null && aToken.getReadings().size() > 0)
        posTags.add(aToken);
      else
        posTags.add(new AnalyzedGermanToken(word, (List)null, pos));
    }
    return posTags;
  }

  private void initSearcher() throws IOException {
    if (searcher == null) {
      // much faster, but needs much more RAM:
      //RAMDirectory ramDir = new RAMDirectory(JLanguageTool.getAbsoluteFile(INDEX_DIR).getAbsolutePath());
      //searcher = new IndexSearcher(ramDir);
      searcher = new IndexSearcher(JLanguageTool.getAbsoluteFile(INDEX_DIR).getAbsolutePath());
    }
  }

  /** For testing only. */
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage: GermanTagger <word1> [word2...]");
      System.exit(1);
    }
    GermanTagger tagger = new GermanTagger();
    List l = new ArrayList();
    for (int i = 0; i < args.length; i++) {
      l.add(args[i]);
    }
    List result = tagger.tag(l);
    System.out.println(result);
  }

}
