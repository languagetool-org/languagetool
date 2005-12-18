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
package de.danielnaber.languagetool.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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
  //private static final String IS_BASEFORM = "is_baseform";

  private final static Map manualMapping = new HashMap();
  static {
    manualMapping.put("PRP", new Character('R'));       // wkl: ??? (z.B. bei "*laut")
    manualMapping.put("PRO", new Character('O'));       // wkl: Pronomen 
    manualMapping.put("ADJ", new Character('J'));       // wkl: Adjektiv
    manualMapping.put("ZAL", new Character('1'));       // wkl: Zahl
    manualMapping.put("ART", new Character('T'));       // wkl: Artikel
    manualMapping.put("NEG", new Character('X'));       // wkl: Negation (z.b. "garnicht")
    manualMapping.put("ABK", new Character('K'));       // wkl: Abkürzung
    manualMapping.put("PAR", new Character('2'));       // wortwkl: ??? (z.B. bei "dienstags")
    manualMapping.put("NOG", new Character('O'));       // gen: z.B. bei "MitarbeiterInnen", "Möbel"
  }
  
  private Map avoidAmbiguitiesCat = new HashMap();
  private Map avoidAmbiguitiesKasus = new HashMap();
  private Map avoidAmbiguitiesNumerus = new HashMap();
  private Map avoidAmbiguitiesGenus = new HashMap();

  private Morphy2Lucene() {
    // use main() method
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: de.danielnaber.languagetool.tools.Morphy2Lucene [--append] <morphyfile>");
      System.exit(1);
    }
    Morphy2Lucene prg = new Morphy2Lucene();
    if (args.length == 2 && args[0].equals("--append")) {
      System.out.println("Appending category index to " + INDEX_DIR);
      prg.run(args[1], false);
    } else {
      System.out.println("Creating new category index in " + INDEX_DIR);
      prg.run(args[0], true);
    }
    System.out.println("Done.");
  }

  private void run(String inputFile, boolean createFromScratch) throws IOException {
    IndexWriter iw = new IndexWriter(INDEX_DIR, new WhitespaceAnalyzer(), createFromScratch);
    iw.setMaxBufferedDocs(500);
    iw.setMergeFactor(500);
    fillIndex(iw, inputFile);
    System.out.println("Optimizing index...");
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
      if (line.startsWith("#"))
        continue;
      if (line.equals(""))
        continue;
      if (line.indexOf("wkl=VER") != -1 || line.indexOf("wkl=PA2") != -1)      // not yet used
        continue;
      if (line.startsWith("<form>")) {
        if (doc != null) {
          iw.addDocument(doc);
          addCount++;
          // test only: if (addCount > 10000) break;
          if (addCount % 1000 == 0) {
            System.out.println(addCount + "...");
          }
        }
        fullform = line.substring("<form>".length(), line.indexOf("</form>"));
        doc = new Document();
        doc.add(new Field(GermanTagger.FULLFORM_FIELD, fullform, Field.Store.NO, Field.Index.UN_TOKENIZED));
      } else {
        if (line.startsWith("<lemma")) {
          //String baseform = line.substring(line.indexOf(">")+1, line.indexOf("<", 1));
          String postype = DEFAULT_CATEGORY;
          String kasus = DEFAULT_CATEGORY;
          String numerus = DEFAULT_CATEGORY;
          String genus = DEFAULT_CATEGORY; 
          if (line.indexOf("wkl=") != -1)
            postype = line.replaceAll(".*wkl=([A-Z]+).*", "$1");
          //if (line.indexOf("wortwkl") != -1)
          //  System.err.println(postype + " : " + line);
          if (line.indexOf("kas=") != -1)
            kasus = line.replaceAll(".*kas=([A-Z]+).*", "$1");
          if (line.indexOf("num=") != -1)
            numerus = line.replaceAll(".*num=([A-Z]+).*", "$1");
          if (line.indexOf("gen=") != -1)
            genus = line.replaceAll(".*gen=([A-Z]+).*", "$1");
          String cat = "" + map(postype, avoidAmbiguitiesCat) +  map(kasus, avoidAmbiguitiesKasus) 
            + map(numerus, avoidAmbiguitiesNumerus) + map(genus, avoidAmbiguitiesGenus);
          //String cat = postype + " " + kasus + " " + numerus + " " + genus;
          if (cat.length() != 4)
            throw new IllegalStateException("category.length != 4: " + cat);
          doc.add(new Field(GermanTagger.CATEGORIES_FIELD, cat, Field.Store.YES, Field.Index.NO));
          /*if (fullform.equals(baseform)) {
            doc.removeField(IS_BASEFORM);   // avoid duplication
            doc.add(new Field(IS_BASEFORM, "1", Field.Store.YES, Field.Index.NO));
          }*/
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
    System.out.println("Added " + addCount + " terms from " + inputFile);
  }

  private char map(String str, Map avoidAmbiguities) {
    char shortForm;
    if (manualMapping.containsKey(str))
      shortForm = ((Character)manualMapping.get(str)).charValue();
    else
      shortForm = str.charAt(0);
    Character shortFormObj = new Character(shortForm);
    if (avoidAmbiguities.containsKey(shortFormObj)) {
      String oldValue = (String)avoidAmbiguities.get(shortFormObj);
      if (!oldValue.equals(str))
        System.err.println("Mapping ambiguous: " + shortForm + " <- " + str + "/" + oldValue);
        //throw new IllegalStateException("Mapping ambiguous: " + shortForm + " <- " + str + "/" + oldValue);
    } else {
      avoidAmbiguities.put(new Character(shortForm), str);
    }
    return shortForm;
  }
  
}
