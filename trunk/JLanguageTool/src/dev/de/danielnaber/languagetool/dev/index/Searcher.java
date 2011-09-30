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
package de.danielnaber.languagetool.dev.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;

/**
 * A class with a main() method that takes a rule id (of a simple rule) and the location of the
 * index that runs the query on that index and prints all matches
 * 
 * @author Tao Lin
 */
public class Searcher {

  private static final int MAX_HITS = 1000;

  public static TopDocs run(PatternRule rule, IndexSearcher searcher, boolean checkUnsupportedRule)
      throws IOException {
    final Query query = PatternRuleQueryBuilder.buildQuery(rule, checkUnsupportedRule);
    //System.out.println("QUERY: " + query);
    return searcher.search(query, MAX_HITS);
  }

  public TopDocs run(String ruleId, InputStream ruleXMLStream, String ruleXmlFile, IndexSearcher searcher,
                            boolean checkUnsupportedRule) throws IOException {
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();
    final List<PatternRule> rules = ruleLoader.getRules(ruleXMLStream, ruleXmlFile);
    ruleXMLStream.close();
    PatternRule theRule = null;
    for (PatternRule rule : rules) {
      if (rule.getId().equals(ruleId)) {
        theRule = rule;
        // TODO: don't stop here, it means we only use the first rule of a rulegroup
        break;
      }
    }
    if (theRule == null) {
      throw new PatternRuleNotFoundException(ruleId);
    }
    return run(theRule, searcher, checkUnsupportedRule);
  }
  
  private void run(String ruleId, String ruleXmlFile, String indexDir)
      throws IOException {
    final File xml = new File(ruleXmlFile);
    if (!xml.exists() || !xml.canRead()) {
      System.out.println("Rule XML file '" + xml.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    InputStream is = new FileInputStream(xml);
    final IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File(indexDir)));
    TopDocs docs;
    try {
      docs = run(ruleId, is, ruleXmlFile, searcher, true);
    } catch (UnsupportedPatternRuleException e) {
      System.out.println(e.getMessage() + " Try to search potential matches:");
      is = new FileInputStream(xml);
      docs = run(ruleId, is, ruleXmlFile, searcher, false);
    }
    printResult(docs, searcher);
    searcher.close();
  }

  private void printResult(TopDocs docs, IndexSearcher searcher)
      throws IOException {

    final ScoreDoc[] hits = docs.scoreDocs;
    System.out.println("Search results: " + docs.totalHits);

    for (int i = 0; i < docs.totalHits;) {
      final Document d = searcher.doc(hits[i].doc);
      i++;
      System.out.println(i + ": " + d.get(PatternRuleQueryBuilder.FIELD_NAME));
    }
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage: Searcher <ruleId> <ruleXML> <indexDir>");
      System.err.println("\truleId Id of the rule to search");
      System.err.println("\truleXML path to a rule file, e.g. en/grammar.xml");
      System.err.println("\tindexDir path to a directory storing the index");
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    final Searcher searcher = new Searcher();
    searcher.run(args[0], args[1], args[2]);
  }

}
