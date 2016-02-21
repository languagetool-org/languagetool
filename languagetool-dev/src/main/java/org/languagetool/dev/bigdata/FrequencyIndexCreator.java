/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.languagemodel.LanguageModel;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

/**
 * Index *.gz files from Google's ngram corpus into a Lucene index ('text' mode)
 * or aggregate them to plain text files ('lucene' mode).
 * Index time (1 doc = 1 ngram and its count, years are aggregated into one number):
 * 130µs/doc (both on an external USB hard disk or on an internal SSD) = about 7700 docs/sec
 * 
 * <p>The reason this isn't faster is not Lucene but the aggregation work we do or simply
 * the large amount of data. Indexing every line takes 3µs/doc, i.e. Lucene can 
 * index about 333,000 docs/s.
 * 
 * <p>Also see http://wiki.languagetool.org/finding-errors-using-big-data.
 * @since 2.7
 */
public class FrequencyIndexCreator {

  private static final int MIN_YEAR = 1910;
  private static final String NAME_REGEX1 = "googlebooks-[a-z]{3}-all-[1-5]gram-20120701-(.*?).gz";
  private static final String NAME_REGEX2 = "[a-z0-9]+-[a-z0-9]+-[a-z0-9]+-[a-z0-9]+-[a-z0-9]+[_-](.*?).gz";  // Hive result
  private static final String NAME_REGEX3 = "([_a-z0-9]{1,2}|other|pos|punctuation|_(ADJ|ADP|ADV|CONJ|DET|NOUN|NUM|PRON|PRT|VERB)_)";  // result of FrequencyIndexCreator with text mode
  private static final int BUFFER_SIZE = 16384;
  private static final String LT_COMPLETE_MARKER = "languagetool_index_complete";
  private static final boolean IGNORE_POS = true;

  private enum Mode { PlainText, Lucene }

  private final AtomicLong bytesProcessed = new AtomicLong(0);
  private final Mode mode;
  
  private long totalTokenCount;
  private long inputFileCount;

  public FrequencyIndexCreator(Mode mode) {
    this.mode = mode;
  }
  
  private void run(File inputDir, File indexBaseDir) throws Exception {
    if (!inputDir.exists()) {
      throw new RuntimeException("Not found: " + inputDir);
    }
    List<File> files = Arrays.asList(inputDir.listFiles());
    long totalBytes = files.stream().mapToLong(File::length).sum();
    System.out.println("Total input bytes: " + totalBytes);
    //Collections.sort(files);  use for non-parallel streams
    // use this to get one index per input file:
    //files.parallelStream().forEach(dir -> index(dir, indexBaseDir, totalBytes, files.size(), null));
    // use this to get one large index for all input files:
    DataWriter dw;
    if (mode == Mode.PlainText) {
      dw = new TextDataWriter(indexBaseDir);
    } else {
      dw = new LuceneDataWriter(indexBaseDir);
    }
    try {
      files.parallelStream().forEach(dir -> index(dir, indexBaseDir, totalBytes, files.size(), dw));
      markIndexAsComplete(indexBaseDir);
    } finally {
      dw.close();
    }
  }

  private void index(File file, File indexBaseDir, long totalBytes, int inputFiles, DataWriter globalDataWriter) {
    System.out.println(file);
    String name = file.getName();
    //if (file.list().length == 1) {
    //  System.out.println("Ignoring empty dir " + file);
    //  return;
    //}
    if (IGNORE_POS && name.matches(".*_[A-Z]+_.*")) {
      System.out.println("Skipping POS tag file " + name);
      return;
    }
    File indexDir;
    boolean hiveMode;
    if (name.matches(NAME_REGEX1)) {
      indexDir = new File(indexBaseDir, name.replaceAll(NAME_REGEX1, "$1"));
      hiveMode = false;
      System.out.println("Running in corpus mode (i.e. aggregation of years)");
    } else if (name.matches(NAME_REGEX2)) {
      indexDir = new File(indexBaseDir, name.replaceAll(NAME_REGEX2, "$1"));
      hiveMode = true;
      System.out.println("Running in Hive mode (i.e. no aggregation of years)");
    } else if (name.matches(NAME_REGEX3) && file.isDirectory()) {
      file = new File(file, file.getName() + "-output.csv.gz");
      indexDir = new File(indexBaseDir, name.replaceAll(NAME_REGEX1, "$1"));
      hiveMode = true;
      System.out.println("Running in Hive/Text mode (i.e. no aggregation of years)");
    } else {
      System.out.println("Skipping " + name + " - doesn't match regex " + NAME_REGEX1 + ", " + NAME_REGEX2 + ", or " + NAME_REGEX3);
      return;
    }
    if (indexDir.exists() && indexDir.isDirectory()) {
      if (isIndexComplete(indexDir)) {
        System.out.println("Skipping " + name + " - index dir '" + indexDir + "' already exists and is complete");
        bytesProcessed.addAndGet(file.length());
        return;
      } else {
        System.out.println("Not skipping " + name + " - index dir '" + indexDir + "' already exists but is not complete");
      }
    }
    System.out.println("Index dir: " + indexDir + " - " + (++inputFileCount) + " of " + inputFiles);
    try {
      if (mode == Mode.PlainText) {
        if (globalDataWriter != null) {
          indexLinesFromGoogleFile(globalDataWriter, file, totalBytes, hiveMode);
        } else {
          try (DataWriter dw = new TextDataWriter(indexDir)) {
            indexLinesFromGoogleFile(dw, file, totalBytes, hiveMode);
          }
          markIndexAsComplete(indexDir);
        }
      } else {
        if (globalDataWriter != null) {
          indexLinesFromGoogleFile(globalDataWriter, file, totalBytes, hiveMode);
        } else {
          try (DataWriter dw = new LuceneDataWriter(indexDir)) {
            indexLinesFromGoogleFile(dw, file, totalBytes, hiveMode);
          }
          markIndexAsComplete(indexDir);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not index " + file, e);
    }
    bytesProcessed.addAndGet(file.length());
  }

  private void markIndexAsComplete(File directory) throws IOException {
    try (FileWriter fw = new FileWriter(new File(directory, LT_COMPLETE_MARKER))) {
      fw.write(new Date().toString());
    }
  }

  private boolean isIndexComplete(File directory) {
    return new File(directory, LT_COMPLETE_MARKER).exists();
  }

  private void indexLinesFromGoogleFile(DataWriter writer, File inputFile, long totalBytes, boolean hiveMode) throws IOException {
    float progress = (float)bytesProcessed.get() / totalBytes * 100;
    System.out.printf("==== Working on " + inputFile + " (%.2f%%) ====\n", progress);
    try (
      InputStream fileStream = new FileInputStream(inputFile);
      InputStream gzipStream = new GZIPInputStream(fileStream, BUFFER_SIZE);
      Reader decoder = new InputStreamReader(gzipStream, "utf-8");
      BufferedReader buffered = new BufferedReader(decoder, BUFFER_SIZE)
    ) {
      int i = 0;
      long docCount = 0;
      long lineCount = 0;
      String prevText = null;
      long startTime = System.nanoTime()/1000;
      String line;
      //noinspection NestedAssignment
      while ((line = buffered.readLine()) != null) {
        lineCount++;
        // To create a smaller index just for testing, comment in this. For there/their
        // with the v1 Google ngram data, the index will be 110MB (instead of 3.1GB with all words):
        //if (!line.matches(".*\\b([Tt]here|[Tt]heir)\\b.*")) {
        //  continue;
        //}
        String[] parts = line.split("\t");
        String text = parts[0];
        if (IGNORE_POS && isRealPosTag(text)) {  // filtering '_VERB_', 'Italian_ADJ', etc.
          continue;
        }
        if (hiveMode) {
          if (parts.length <= 1) {
            System.err.println("Could not index: " + line);
            continue;
          }
          String docCountStr = parts[1];
          writer.addDoc(text, Long.parseLong(docCountStr));
          if (++i % 500_000 == 0) {
            printStats(i, inputFile, Long.parseLong(docCountStr), lineCount, text, startTime, totalBytes);
          }
        } else {
          int year = Integer.parseInt(parts[1]);
          if (year < MIN_YEAR) {
            continue;
          }
          if (prevText == null || prevText.equals(text)) {
            // aggregate years
            docCount += Long.parseLong(parts[2]);
          } else {
            //System.out.println(">"+ prevText + ": " + count);
            writer.addDoc(prevText, docCount);
            if (++i % 5_000 == 0) {
              printStats(i, inputFile, docCount, lineCount, prevText, startTime, totalBytes);
            }
            docCount = Long.parseLong(parts[2]);
          }
        }
        prevText = text;
      }
      printStats(i, inputFile, docCount, lineCount, prevText, startTime, totalBytes);
    }
    writer.addTotalTokenCountDoc(totalTokenCount);
  }

  private boolean isRealPosTag(String text) {
    int idx = text.indexOf('_');
    if (idx == -1) {
      return false;
    } else {
      String tag = idx + 7 <= text.length() ? text.substring(idx, idx + 7) : ""; // _START_
      if (tag.equals(LanguageModel.GOOGLE_SENTENCE_START)) {
        return false;
      }
      String tag2 = idx + 5 <= text.length() ? text.substring(idx, idx + 5) : ""; // _END_
      if (tag2.equals(LanguageModel.GOOGLE_SENTENCE_END)) {
        return false;
      }
      return true;
    }
  }

  private void printStats(int i, File inputFile, long docCount, long lineCount, String prevText, long startTimeMicros, long totalBytes) {
    long microsNow = System.nanoTime()/1000;
    float millisPerDoc = (microsNow-startTimeMicros)/Math.max(1, i);
    NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
    float progress = (float)bytesProcessed.get() / totalBytes * 100;
    System.out.printf("%.2f%% input:%s doc:%s line:%s ngram:%s occ:%s (%.0fµs/doc)\n",
            progress, inputFile.getName(), format.format(i), format.format(lineCount),
            prevText, format.format(docCount), millisPerDoc);
  }
  
  abstract static class DataWriter implements AutoCloseable {
    abstract void addDoc(String text, long count) throws IOException;
    abstract void addTotalTokenCountDoc(long totalTokenCount) throws IOException;
  }

  
  class LuceneDataWriter extends DataWriter {

    IndexWriter writer;
    
    LuceneDataWriter(File indexDir) throws IOException {
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      config.setUseCompoundFile(false);  // ~10% speedup
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
      //config.setRAMBufferSizeMB(1000);
      Directory directory = FSDirectory.open(indexDir.toPath());
      writer = new IndexWriter(directory, config);
    }

    @Override
    void addDoc(String text, long count) throws IOException {
      if (text.length() > 1000) {
        System.err.println("Ignoring doc, ngram is > 1000 chars: " + text.substring(0, 50) + "...");
      } else {
        Document doc = new Document();
        doc.add(new Field("ngram", text, StringField.TYPE_NOT_STORED));
        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        Field countField = new Field("count", String.valueOf(count), fieldType);
        doc.add(countField);
        totalTokenCount += count;
        writer.addDocument(doc);
      }
    }

    @Override
    void addTotalTokenCountDoc(long totalTokenCount) throws IOException {
      FieldType fieldType = new FieldType();
      fieldType.setIndexOptions(IndexOptions.DOCS);
      fieldType.setStored(true);
      Field countField = new Field("totalTokenCount", String.valueOf(totalTokenCount), fieldType);
      Document doc = new Document();
      doc.add(countField);
      writer.addDocument(doc);
    }

    @Override
    public void close() throws Exception {
      if (writer != null) {
        writer.close();
      }
    }
  }

  static class TextDataWriter extends DataWriter {

    private final FileWriter fw;
    private final BufferedWriter writer;
    
    TextDataWriter(File indexDir) throws IOException {
      if (indexDir.exists()) {
        System.out.println("Using existing dir: " + indexDir.getAbsolutePath());
      } else {
        boolean mkdir = indexDir.mkdir();
        if (!mkdir) {
          throw new RuntimeException("Could not create: " + indexDir.getAbsolutePath());
        }
      }
      fw = new FileWriter(new File(indexDir, indexDir.getName() + "-output.csv"));
      writer = new BufferedWriter(fw);
    }

    @Override
    void addDoc(String text, long count) throws IOException {
      fw.write(text + "\t" + count + "\n");
    }

    @Override
    void addTotalTokenCountDoc(long totalTokenCount) throws IOException {
      System.err.println("Note: not writing totalTokenCount (" + totalTokenCount + ") in file mode");
    }

    @Override
    public void close() throws Exception {
      if (fw != null) {
        fw.close();
      }
      writer.close();
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out.println("Usage: " + FrequencyIndexCreator.class.getSimpleName() + " <text|lucene> <inputDir> <outputDir>");
      System.out.println("    <text|lucene> 'text' will write plain text files, 'lucene' will write Lucene indexes");
      System.out.println("    <inputDir> is the Google ngram data, optionally already aggregated by Hive (lucene mode),");
      System.out.println("               please see http://wiki.languagetool.org/finding-errors-using-big-data");
      System.exit(1);
    }
    Mode mode;
    if (args[0].equals("text")) {
      mode = Mode.PlainText;
    } else if (args[0].equals("lucene")) {
      mode = Mode.Lucene;
    } else {
      throw new RuntimeException("Unknown mode: " + args[0]);
    }
    FrequencyIndexCreator creator = new FrequencyIndexCreator(mode);
    System.out.println("Mode: " + mode);
    System.out.println("Minimum year: " + MIN_YEAR);
    System.out.println("Ignore POS tags: " + IGNORE_POS);
    creator.run(new File(args[1]), new File(args[2]));
  }
}
