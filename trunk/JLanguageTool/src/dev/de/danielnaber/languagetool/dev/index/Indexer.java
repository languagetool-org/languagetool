package de.danielnaber.languagetool.dev.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class Indexer {

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    run(args[0], args[1]);
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: Indexer <textFile> <indexDir>");
      System.err.println("\ttextFile path to a text file to be indexed");
      System.err.println("\tindexDir path to a directory storing the index");
      System.exit(1);
    }
  }

  private static void run(String textFile, String indexDir) throws Exception {
    final File file = new File(textFile);
    if (!file.exists() || !file.canRead()) {
      System.out.println("Text file '" + file.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }

    System.out.println("Indexing to directory '" + indexDir + "'...");
    final Directory dir = FSDirectory.open(new File(indexDir));

    final Language language = Language.ENGLISH;
    final Analyzer analyzer = new LanguageToolAnalyzer(Version.LUCENE_31, new JLanguageTool(language));

    final IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
    iwc.setOpenMode(OpenMode.CREATE);
    final IndexWriter writer = new IndexWriter(dir, iwc);

    final BufferedReader reader = new BufferedReader(new FileReader(file));
    final StringBuffer buffer = new StringBuffer();
    String line = "";
    while ((line = reader.readLine()) != null) {
      buffer.append(line);
    }
    final String content = buffer.toString();
    final SentenceTokenizer sentenceTokenizer = language.getSentenceTokenizer();
    final List<String> sentences = sentenceTokenizer.tokenize(content);
    for (String sentence : sentences) {
      final Document doc = new Document();
      doc.add(new Field(PatternRuleQueryBuilder.FIELD_NAME, sentence, Store.YES, Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
    System.out.println("Index complete!");
  }
}
