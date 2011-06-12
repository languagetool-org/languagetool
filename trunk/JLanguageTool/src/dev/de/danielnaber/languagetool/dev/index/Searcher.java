package de.danielnaber.languagetool.dev.index;

import java.io.File;
import java.io.FileInputStream;
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

public class Searcher {

  private static final String FN = "field";

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    run(args[0], args[1], args[2]);
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

  private static void run(String ruleId, String ruleXML, String indexDir) throws Exception {
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    File xml = new File(ruleXML);
    if (!xml.exists() || !xml.canRead()) {
      System.out.println("Rule XML file '" + xml.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    InputStream is = new FileInputStream(xml);
    List<PatternRule> rules = ruleLoader.getRules(is, ruleXML);
    PatternRule theRule = null;
    for (PatternRule rule : rules) {
      if (rule.getId().equals(ruleId)) {
        theRule = rule;
        break;
      }
    }
    if (theRule == null) {
      System.out.println("Can not find rule '" + ruleId + "'");
      System.exit(1);
    }
    Query query = PatternRuleQueryBuilder.bulidQuery(theRule);
    IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File(indexDir)));

    TopDocs docs = searcher.search(query, 100);
    ScoreDoc[] hits = docs.scoreDocs;
    System.out.println("Search results: " + docs.totalHits);

    for (int i = 0; i < docs.totalHits;) {
      Document d = searcher.doc(hits[i].doc);
      i++;
      System.out.println(i + ": " + d.get(FN));
    }
    searcher.close();
  }
}
