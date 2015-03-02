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
package org.languagetool.rules;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.FakeLanguage;
import org.languagetool.TestTools;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfusionProbabilityRuleTest {
  
  @Test
  public void testRule() throws IOException, ClassNotFoundException {
    // directly using FrequencyIndexCreator would be better, but we're in core here...
    File topTempDir = new File(System.getProperty("java.io.tmpdir"),
            ConfusionProbabilityRuleTest.class.getName() + "-" + System.currentTimeMillis());
    File tempDir = new File(topTempDir, "3grams");
    try {
      create3gramIndex(tempDir);
      testIndex(topTempDir);
    } finally {
      System.out.println("Deleting " + topTempDir);
      for (File file : tempDir.listFiles()) {
        String name = file.getName();
        if (!name.matches("^(write\\.lock|segments\\.gen|segments_.*|_\\d+\\..{2,3}?)$")) {
          throw new RuntimeException("Sanity check failed: unexpected file name '" + file.getAbsoluteFile() + "'");
        }
        file.delete();
      }
      tempDir.delete();
      topTempDir.delete();
    }
  }

  private void create3gramIndex(File tempDir) throws IOException {
    Directory directory = FSDirectory.open(tempDir);
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, new FakeAnalyzer());
    try (IndexWriter writer = new IndexWriter(directory, config)) {
      // wrong:
      addDoc(writer, "is an café", "1");
      addDoc(writer, "an café in", "2");
      addDoc(writer, "café in Berlin", "1");
      // correct:
      addDoc(writer, "is a café", "5000");
      addDoc(writer, "a café in", "1000");
      addDoc(writer, "café in Berlin", "20");
    }
  }

  private void addDoc(IndexWriter writer, String text, String count) throws IOException {
    Document doc = new Document();
    doc.add(new Field("ngram", text, StringField.TYPE_NOT_STORED));
    FieldType fieldType = new FieldType();
    fieldType.setStored(true);
    Field countField = new Field("count", count, fieldType);
    doc.add(countField);
    writer.addDocument(doc);
  }

  private void testIndex(File languageModelIndex) throws IOException {
    LanguageModel languageModel = new LuceneLanguageModel(languageModelIndex);
    ConfusionProbabilityRule rule = new ConfusionProbabilityRule(TestTools.getEnglishMessages(), languageModel, new FakeLanguage()) {
      @Override public String getDescription() { return null; }
      @Override public String getMessage(String suggestion) { return null; }
    };
    ConfusionProbabilityRule.ConfusionSet confusionSet = new ConfusionProbabilityRule.ConfusionSet("a", "an");
    AnalyzedTokenReadings[] tokens = {
            reading("is"),
            reading("an"),
            reading("café"),
            reading("in"),
            reading("Berlin")
    };
    String alternative = rule.getBetterAlternativeOrNull(tokens, 1, confusionSet);
    assertThat(alternative, is("a"));
  }

  private AnalyzedTokenReadings reading(String token) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), 0);
  }
  
  private class FakeAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {
      return null;
    }
  }
}
