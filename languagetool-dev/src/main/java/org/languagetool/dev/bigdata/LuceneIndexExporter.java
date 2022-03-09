/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Export the sentences of a Lucene index.
 */
public class LuceneIndexExporter {

  private static final String FIELD_NAME = "fieldLowercase";

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + LuceneIndexExporter.class.getSimpleName() + " <luceneIndexDir>");
      System.exit(1);
    }
    System.out.println("Using field: " + FIELD_NAME);
    Directory directory = SimpleFSDirectory.open(Paths.get(args[0]));
    try (DirectoryReader indexReader = DirectoryReader.open(directory)) {
      for (int i = 0; i < indexReader.maxDoc(); i++) {
        Document doc = indexReader.document(i);
        System.out.println(doc.get(FIELD_NAME));
      }
    }
  }
}
