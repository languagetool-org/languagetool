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
package org.languagetool.tools;

import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Create a Morfologik binary synthesizer dictionary from plain text data.
 */
final class SynthDictionaryBuilder extends DictionaryBuilder {

  /**
   * It makes sense to remove all forms from the synthesizer dict where POS tags indicate "unknown form",
   * "foreign word" etc., as they only take space. Probably nobody will ever use them:
   */
  private static final String POLISH_IGNORE_REGEX = ":neg|qub|depr";

  SynthDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }

  public static void main(String[] args) throws Exception {
    BuilderOptions builderOptions = new BuilderOptions();
    builderOptions.addOption(BuilderOptions.INPUT_OPTION, true, 
        BuilderOptions.TAB_INPUT_HELP, true);
    builderOptions.addOption(BuilderOptions.INFO_OPTION, true, 
        BuilderOptions.INFO_HELP, true);
    CommandLine cmdLine = builderOptions.parseArguments(args, SynthDictionaryBuilder.class);
    
    File plainTextDictFile = new File(cmdLine.getOptionValue(BuilderOptions.INPUT_OPTION));
    File infoFile = new File(cmdLine.getOptionValue(BuilderOptions.INFO_OPTION));
    
    SynthDictionaryBuilder builder = new SynthDictionaryBuilder(infoFile);
    builder.setOutputFilename(cmdLine.getOptionValue(BuilderOptions.OUTPUT_OPTION));

    builder.build(plainTextDictFile, infoFile);
  }
  
  File build(File plainTextDictFile, File infoFile) throws Exception {
    String outputFilename = this.getOutputFilename();
    File outputDirectory = new File(outputFilename).getParentFile();
    File tempFile = File.createTempFile(SynthDictionaryBuilder.class.getSimpleName(), ".txt", outputDirectory);
    File reversedFile = null;
    try {
      Set<String> itemsToBeIgnored = getIgnoreItems(new File(infoFile.getParent(), "filter-archaic.txt"));
      Pattern ignorePosRegex = getPosTagIgnoreRegex(infoFile);
      reversedFile = reverseLineContent(plainTextDictFile, itemsToBeIgnored, ignorePosRegex);
      writePosTagsToFile(plainTextDictFile, getTagFile(tempFile));
      return buildDict(reversedFile);
    } finally {
      tempFile.delete();
      if (reversedFile != null) {
        reversedFile.delete();
      }
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

  @Nullable
  private Pattern getPosTagIgnoreRegex(File infoFile) {
    String fileName = infoFile.getName();
    int underscorePos = fileName.indexOf('_');
    if (underscorePos == -1) {
      throw new IllegalArgumentException("Please specify an .info file for a synthesizer as the second parameter, named '<xyz>_synth.info', with <xyz> being a language'");
    }
    String baseName = fileName.substring(0, underscorePos);
    if (baseName.equals("polish")) {
      return Pattern.compile(POLISH_IGNORE_REGEX);
    }
    return null;
  }

  private File reverseLineContent(File plainTextDictFile, Set<String> itemsToBeIgnored, Pattern ignorePosRegex) throws IOException {
    File reversedFile = File.createTempFile(SynthDictionaryBuilder.class.getSimpleName() + "_reversed", ".txt");
    String separator = getOption("fsa.dict.separator");
    if (separator == null || separator.trim().isEmpty()) {
      throw new IOException("A separator character (fsa.dict.separator) must be defined in the dictionary info file.");
    }
    String encoding = getOption("fsa.dict.encoding");
    int posIgnoreCount = 0;
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
          String posTag = parts[2];
          if (ignorePosRegex != null && ignorePosRegex.matcher(posTag).find()) {
            posIgnoreCount++;
            continue;
          }
          out.write(parts[0] + separator + parts[1] + "|" + posTag );
          out.write("\n");
        } else {
          System.err.println("Invalid input, expected three tab-separated columns in " + plainTextDictFile + ": " + line + " => ignoring");
        }
      }
      scanner.close();
    }
    System.out.println("Number of lines ignored due to POS tag filter ('" + ignorePosRegex + "'): " + posIgnoreCount);
    return reversedFile;
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
