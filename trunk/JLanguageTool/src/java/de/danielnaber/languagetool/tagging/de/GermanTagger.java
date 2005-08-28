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

  public List lookup(String word) throws IOException {
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
          if (!val.equals(""))
            l.add(val);
        }
      }
      return l;
    }
  }
  
  public List tag(List tokens) throws IOException {
    if (searcher == null)
      searcher = new IndexSearcher(INDEX_DIR);

    List posTags = new ArrayList();
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String word = (String) iter.next();
      List l = lookup(word);
      if (l == null)
        posTags.add(null);
      else if (l.size() > 0)
        posTags.add(l.toString());
      else
        posTags.add(null);
    }
    return posTags;
  }

}
