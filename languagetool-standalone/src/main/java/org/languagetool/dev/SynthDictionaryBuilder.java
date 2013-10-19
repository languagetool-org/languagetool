/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import java.io.*;
import java.util.*;

/**
 * Create a Morfologik binary dictionary from plain text data.
 */
final class SynthDictionaryBuilder extends DictionaryBuilder {

  public SynthDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }

  public static void main(String[] args) throws Exception {
    checkUsageOrExit(SynthDictionaryBuilder.class.getSimpleName(), args);
    File infoFile = new File(args[1]);
    SynthDictionaryBuilder builder = new SynthDictionaryBuilder(infoFile);
    builder.build(new File(args[0]), infoFile);
  }
  
  File build(File plainTextDictFile, File infoFile) throws Exception {
    File reversedFile = File.createTempFile(SynthDictionaryBuilder.class.getSimpleName() + "_reversed", ".txt");
    File tempFile = File.createTempFile(SynthDictionaryBuilder.class.getSimpleName(), ".txt");
    try {
      Set<String> itemsToBeIgnored = getIgnoreItems(new File(infoFile.getParent(), "filter-archaic.txt"));
      reverseLineContent(plainTextDictFile, reversedFile, itemsToBeIgnored);
      List<String> tab2morphOptions = getTab2MorphOptions(reversedFile, tempFile);
      tab2morphOptions.add(0, "tab2morph");
      tab2morphOptions.add(1, "-nw");  // no warnings, needed for synth dicts
      prepare(tab2morphOptions);
      writePosTagsToFile(plainTextDictFile, getTagFile(tempFile));
      return buildDict(tempFile);
    } finally {
      tempFile.delete();
      reversedFile.delete();
    }
  }

  private Set<String> getIgnoreItems(File file) throws FileNotFoundException {
    Set<String> result = new HashSet<>();
    if (file.exists()) {
      try (Scanner scanner = new Scanner(file, getOption("fsa.dict.encoding"))) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          if (!line.startsWith("#")) {
            result.add(line);
          }
        }
      }
      System.out.println("Loaded " + result.size() + " words to be ignored from " + file);
    } else {
      System.out.println("File " + file.getAbsolutePath() + " does not exist, no items will be ignored");
    }
    return result;
  }

  private void reverseLineContent(File plainTextDictFile, File reversedFile, Set<String> itemsToBeIgnored) throws IOException {
    String encoding = getOption("fsa.dict.encoding");
    Scanner scanner = new Scanner(plainTextDictFile, encoding);
    try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reversedFile), encoding))) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (itemsToBeIgnored.contains(line)) {
          System.out.println("Ignoring: " + line);
          continue;
        }
        String[] parts = line.split("\t");
        if (parts.length == 3) {
          out.write(parts[1] + "|" + parts[2] + "\t" + parts[0]);
          out.write("\n");
        } else {
          System.err.println("Invalid input, expected three tab-separated columns in " + plainTextDictFile + ": " + line + " => ignoring");
        }
      }
      scanner.close();
    }
  }

  private File getTagFile(File tempFile) {
    String name = tempFile.getAbsolutePath() + "_tags.txt";
    return new File(name);
  }

  private void writePosTagsToFile(File plainTextDictFile, File tagFile) throws IOException {
    Set<String> posTags = collectTags(plainTextDictFile);
    List<String> sortedTags = new ArrayList<>(posTags);
    Collections.sort(sortedTags);
    System.out.println("Writing tag file to " + tagFile);
    try (FileWriter out = new FileWriter(tagFile)) {
      for (String tag : sortedTags) {
        out.write(tag);
        out.write("\n");
      }
    }
  }

  private Set<String> collectTags(File plainTextDictFile) throws IOException {
    Set<String> posTags = new HashSet<>();
    try (Scanner scanner = new Scanner(plainTextDictFile, getOption("fsa.dict.encoding"))) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split("\t");
        if (parts.length == 3) {
          String posTag = parts[2];
          posTags.add(posTag);
        } else {
          System.err.println("Invalid input, expected three tab-separated columns in " + plainTextDictFile + ": " + line + " => ignoring");
        }
      }
    }
    return posTags;
  }

}
