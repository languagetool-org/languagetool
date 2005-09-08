/* JLanguageTool, a natural language style checker 
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

import de.danielnaber.languagetool.tagging.Tagger;

/**
 * Experimental German tagger, requires data files in <code>rules/de/categories</code>.
 * 
 * @author Daniel Naber
 */
public class GermanTagger implements Tagger {

  public static final String FULLFORM_FIELD = "fullform";
  public static final String CATEGORIES_FIELD = "categories";
  
  private static final String INDEX_DIR = "rules/de/categories";
  private IndexSearcher searcher = null;

  public GermanTagger() {
  }

  public AnalyzedGermanToken lookup(String word, int startPos) throws IOException {
    if (searcher == null)
      searcher = new IndexSearcher(INDEX_DIR);
    Query query = new TermQuery(new Term(FULLFORM_FIELD, word));
    Hits hits = searcher.search(query);
    if (hits.length() == 0) {
      return null;
    } else if (hits.length() > 1){
      throw new IllegalStateException("More than one hit for " + word);
    } else {
      Document doc = hits.doc(0);
      Field[] fields = doc.getFields(CATEGORIES_FIELD);
      List l = new ArrayList();
      if (fields != null) {
        for (int i = 0; i < fields.length; i++) {
          String val = fields[i].stringValue();
          if (!val.equals("")) {
            if (val.indexOf(" NOG") != -1) {
              // TODO: what exactly does "NOG" mean?!
              String val1 = val.replaceFirst(" NOG", " MAS");
              String val2 = val.replaceFirst(" NOG", " FEM");
              GermanTokenReading tokenReading1 =
                GermanTokenReading.createTokenReadingFromMorphyString(val1, word);
              l.add(tokenReading1);
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
      AnalyzedGermanToken aToken = new AnalyzedGermanToken(word, l, startPos);
      return aToken;
    }
  }
  
  public List tag(List tokens) throws IOException {
    if (searcher == null)
      searcher = new IndexSearcher(INDEX_DIR);

    List posTags = new ArrayList();
    int pos = 0;
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String word = (String) iter.next();
      AnalyzedGermanToken aToken = lookup(word, pos);
      pos += word.length();
      if (aToken != null && aToken.getReadings().size() > 0)
        posTags.add(aToken);
      else
        posTags.add(new AnalyzedGermanToken(word, (List)null, pos));
    }
    return posTags;
  }
  
  // test only:
  public static void main(String[] args) throws IOException {
    GermanTagger tagger = new GermanTagger();
    //AnalyzedGermanToken aToken = tagger.lookup("Eltern", 0);
    List l = new ArrayList();
    l.add("das");
    l.add("Haus");
    List result = tagger.tag(l);
    System.out.println(result);
  }

}
