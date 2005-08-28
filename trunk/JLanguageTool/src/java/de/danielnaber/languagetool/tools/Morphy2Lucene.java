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
package de.danielnaber.languagetool.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import de.danielnaber.languagetool.tagging.de.GermanTagger;

/**
 * Tool that takes a Morphy export (with charset cp1252) like this and turns it into
 * a Lucene index that can be searched for words very fast:
 * 
 * <pre>
 * &lt;form>riesigem&lt;/form>
 * &lt;lemma wkl=ADJ kas=DAT num=SIN gen=MAS komp=GRU art=SOL>
 * &lt;lemma wkl=ADJ kas=DAT num=SIN gen=NEU komp=GRU art=SOL>
 * </pre>
 *
 * @author Daniel Naber
 */
public class Morphy2Lucene {
  
  // if no category is known for a word's reading, use this one: 
  private static final String DEFAULT_CATEGORY = "0";

  private static final String INDEX_DIR = "rules/de/categories";
  
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: de.danielnaber.languagetool.tools.Morphy2Lucene <morphyfile>");
      System.exit(1);
    }
    System.out.println("Writing category index to " + INDEX_DIR);
    Morphy2Lucene prg = new Morphy2Lucene();
    prg.run(args[0]);
    System.out.println("Done.");
  }

  private void run(String inputFile) throws IOException {
    IndexWriter iw = new IndexWriter(INDEX_DIR, new WhitespaceAnalyzer(), true);
    iw.setMaxBufferedDocs(500);
    iw.setMergeFactor(500);
    fillIndex(iw, inputFile);
    iw.optimize();
    iw.close();
  }

  private void fillIndex(IndexWriter iw, String inputFile) throws IOException {
    InputStreamReader isr = new InputStreamReader(new FileInputStream(inputFile), "cp1252");
    BufferedReader br = new BufferedReader(isr);
    String line;
    String fullform = null;
    Document doc = null;
    int addCount = 0;
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.equals(""))
        continue;
      if (line.indexOf("wkl=VER") != -1 || line.indexOf("wkl=PA2") != -1)      // not yet used
        continue;
      if (line.startsWith("<form>")) {
        if (doc != null) {
          iw.addDocument(doc);
          addCount++;
          if (addCount % 1000 == 0) {
            System.out.println(addCount + "...");
          }
        }
        fullform = line.substring("<form>".length(), line.indexOf("</form>"));
        doc = new Document();
        doc.add(new Field(GermanTagger.FULLFORM_FIELD, fullform, Field.Store.NO, Field.Index.UN_TOKENIZED));
      } else {
        if (line.startsWith("<lemma")) {
          String postype = DEFAULT_CATEGORY;
          String kasus = DEFAULT_CATEGORY;
          String numerus = DEFAULT_CATEGORY;
          String genus = DEFAULT_CATEGORY; 
          if (line.indexOf("wkl=") != -1)
            postype = line.replaceAll(".*wkl=([A-Z]+).*", "$1");
          if (line.indexOf("kas=") != -1)
            kasus = line.replaceAll(".*kas=([A-Z]+).*", "$1");
          if (line.indexOf("num=") != -1)
            numerus = line.replaceAll(".*num=([A-Z]+).*", "$1");
          if (line.indexOf("gen=") != -1)
            genus = line.replaceAll(".*gen=([A-Z]+).*", "$1");
          String cat = postype + " " + kasus + " " + numerus + " " + genus;
          if (cat.length() < 8)
            throw new IllegalStateException("category too short: " + cat);
          if (cat.length() > 15)
            throw new IllegalStateException("category too long: " + cat);
          doc.add(new Field(GermanTagger.CATEGORIES_FIELD, cat, Field.Store.YES, Field.Index.NO));
        } else {
          System.err.println("unknown format: " + line);
        }
      }
    }
    if (doc != null) {
      iw.addDocument(doc);
      addCount++;
    }
    isr.close();
    br.close();
    System.out.println("Added " + addCount + " terms.");
  }
  
}
