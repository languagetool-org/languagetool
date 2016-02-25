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

import morfologik.tools.FSABuild;
import morfologik.tools.Launcher;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create a Morfologik binary dictionary from plain text data.
 */
class DictionaryBuilder {

  private final Properties props = new Properties();

  private static final int FREQ_RANGES_IN = 256;
  private static final int FREQ_RANGES_OUT = 26; // (A-Z)
  private static final int FIRST_RANGE_CODE = 65; // character 'A', less frequent words

  private final Map<String, Integer> freqList = new HashMap<>();
  private final Pattern pFreqEntry = Pattern.compile(".*<w f=\"(\\d+)\" flags=\"(.*)\">(.+)</w>.*");
  // Valid for tagger dictionaries (wordform_TAB_lemma_TAB_postag) or spelling dictionaries (wordform)
  private final Pattern pTaggerEntry = Pattern.compile("^([^\t]+).*$");
  private String outputFilename;

  protected DictionaryBuilder(File infoFile) throws IOException {
    props.load(new FileInputStream(infoFile));
  }
  
  protected void setOutputFilename(String outputFilename) {
    this.outputFilename = outputFilename;
  }

  protected List<String> getTab2MorphOptions(File dictFile, File outputFile) throws IOException {
    List<String> tab2morphOptions = new ArrayList<>();
    String separator = getOption("fsa.dict.separator");
    if (separator != null && !separator.trim().isEmpty()) {
      tab2morphOptions.add("--annotation");
      tab2morphOptions.add(separator);
    }
    
    if ((isOptionTrue("fsa.dict.uses-prefixes") || isOptionTrue("fsa.dict.uses-infixes")) &&
         hasOption("fsa.dict.encoder")) {
      throw new IOException(".info file must specify either fsa.dict.encoder (preferred) or fsa.dict.uses-* properties.");
    }

    if (hasOption("fsa.dict.encoder")) {
      tab2morphOptions.add("--encoder");
      tab2morphOptions.add(getOption("fsa.dict.encoder"));
    } else {
      if (isOptionTrue("fsa.dict.uses-prefixes")) {
        tab2morphOptions.add("--encoder");
        tab2morphOptions.add("prefix");
      } else if (isOptionTrue("fsa.dict.uses-infixes")) {
        tab2morphOptions.add("--encoder");
        tab2morphOptions.add("infix");
      }
    }

    tab2morphOptions.add("-i");
    tab2morphOptions.add(dictFile.getAbsolutePath());
    tab2morphOptions.add("-o");
    tab2morphOptions.add(outputFile.getAbsolutePath());
    return tab2morphOptions;
  }

  protected void prepare(List<String> tab2morphOptions) throws Exception {
    System.out.println("Running Morfologik Launcher.main with these options: " + tab2morphOptions);
    Launcher.main(tab2morphOptions.toArray(new String[tab2morphOptions.size()]));
  }

  protected File buildDict(File tempFile) throws Exception {
    File resultFile = new File(outputFilename);
        
    String[] buildToolOptions = {"-f", "cfsa2", "-i", tempFile.getAbsolutePath(), "-o", resultFile.getAbsolutePath()};
    System.out.println("Running Morfologik FSABuildTool.main with these options: " + Arrays.toString(buildToolOptions));
    FSABuild.main(buildToolOptions);
    System.out.println("Done. The binary dictionary has been written to " + resultFile.getAbsolutePath());
    return resultFile;
  }

  @Nullable
  protected String getOption(String option) {
    String property = props.getProperty(option);
    if (property == null) {
      return null;
    }
    return property.trim();
  }

  protected boolean hasOption(String option) {
    return props.getProperty(option) != null; 
  }
  
  private boolean isOptionTrue(String option) {
    return hasOption(option) && "true".equals(getOption(option));
  }
  
  protected void readFreqList(File freqListFile) {
    try (
      FileInputStream fis = new FileInputStream(freqListFile.getAbsoluteFile());
      InputStreamReader reader = new InputStreamReader(fis, "utf-8");
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        Matcher m = pFreqEntry.matcher(line);
        if (m.matches()) {
          freqList.put(m.group(3), Integer.parseInt(m.group(1)));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot read file: " + freqListFile.getAbsolutePath());
    }
  }
  
  protected File addFreqData(File dictFile) throws IOException {
    if (!isOptionTrue("fsa.dict.frequency-included")) {
      throw new IOException("In order to use frequency data add the line 'fsa.dict.frequency-included=true' to the dictionary info file.");
    }
    String separator = getOption("fsa.dict.separator");
    if (separator == null || separator.trim().isEmpty()) {
      throw new IOException("A separator character (fsa.dict.separator) must be defined in the dictionary info file.");
    }
    File tempFile = File.createTempFile(DictionaryBuilder.class.getSimpleName(), "WithFrequencies.txt");
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream(tempFile.getAbsoluteFile()),
        getOption("fsa.dict.encoding")));
    int freqValuesApplied = 0;
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(
          new FileInputStream(dictFile.getAbsoluteFile()),
          getOption("fsa.dict.encoding")));
      String line;
      int maxFreq = Collections.max(freqList.values());
      double maxFreqLog = Math.log(maxFreq);
      while ((line = br.readLine()) != null) {
        Matcher m = pTaggerEntry.matcher(line);
        if (m.matches()) {
          int freq = 0;
          String key = m.group(1);
          if (freqList.containsKey(key)) {
            freq = freqList.get(key);
            freqValuesApplied++;
          }
          int normalizedFreq = freq;
          if (freq > 0 && maxFreq > 255) {
            double freqZeroToOne = Math.log(freq) / maxFreqLog;  // spread number better over the range
            normalizedFreq = (int) (freqZeroToOne * (FREQ_RANGES_IN-1));  // 0 to 255
          }
          if (normalizedFreq < 0 || normalizedFreq > 255) {
            throw new RuntimeException("Frequency out of range (0-255): " + normalizedFreq + " in word " + key);
          }
          // Convert integers 0-255 to ranges A-Z, and write output 
          String freqChar = Character.toString((char) (FIRST_RANGE_CODE + normalizedFreq*FREQ_RANGES_OUT/FREQ_RANGES_IN));
          bw.write(line + separator + freqChar + "\n");
        }
      }
      br.close();
      bw.close();
      System.out.println(freqList.size() + " frequency values applied in " + freqValuesApplied + " word forms.");
    } catch (IOException e) {
      throw new RuntimeException("Cannot read file: " + dictFile.getAbsolutePath());
    }
    tempFile.deleteOnExit();
    return tempFile;
  }
  
}
