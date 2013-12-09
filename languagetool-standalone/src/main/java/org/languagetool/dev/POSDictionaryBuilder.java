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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create a Morfologik binary dictionary from plain text data.
 */
final class POSDictionaryBuilder extends DictionaryBuilder {

  Hashtable<String, Integer> freqList = new Hashtable<String, Integer>();
  Pattern pFreqEntry = Pattern.compile(".*<w f=\"(\\d+)\" flags=\"(.*)\">(.+)</w>.*");
  Pattern pTaggerEntry = Pattern.compile("^(.+)\\t(.+)\\t(.+)$");
  final static int FREQ_RANGES_IN = 256;
  final static int FREQ_RANGES_OUT = 10;
  final static int FIRST_RANGE_CODE = 65; // character 'A', less frequent words
  
  POSDictionaryBuilder(File infoFile) throws IOException {
    super(infoFile);
  }

  public static void main(String[] args) throws Exception {
    checkUsageOrExit(POSDictionaryBuilder.class.getSimpleName(), args);
    POSDictionaryBuilder builder = new POSDictionaryBuilder(new File(args[1]));
    if (args.length==3) {
      builder.readFreqList(new File(args[2]));
      builder.build(builder.addFreqData(new File(args[0])));
    }
    else {
      builder.build(new File(args[0]));
    }
    
  }

  File build(File dictFile) throws Exception {
    File tempFile = File.createTempFile(POSDictionaryBuilder.class.getSimpleName(), ".txt");
    try {
      List<String> tab2morphOptions = getTab2MorphOptions(dictFile, tempFile);
      tab2morphOptions.add(0, "tab2morph");
      prepare(tab2morphOptions);
      return buildDict(tempFile);
    } finally {
      tempFile.delete();
    }
  }
  
  void readFreqList(File freqListFile) {
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(freqListFile));
      String line;
      while ((line = br.readLine()) != null) {
        Matcher m = pFreqEntry.matcher(line);
        if (m.matches()) {
          freqList.put(m.group(3), Integer.parseInt(m.group(1)));
        }
      }
      br.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  File addFreqData(File dictFile) throws Exception {
    if (!isOptionTrue("fsa.dict.frequency-included")) {
      throw new IOException("In order to use frequency data add the line 'dict.fsa.frequency-included=true' to the dictionary info file.");
    }
    File tempFile = File.createTempFile(POSDictionaryBuilder.class.getSimpleName(), "WithFrequencies.txt");
    BufferedReader br;
    FileWriter fw = new FileWriter(tempFile.getAbsoluteFile());
    BufferedWriter bw = new BufferedWriter(fw);
    int freqValuesApplied = 0;
    try {
      br = new BufferedReader(new FileReader(dictFile));
      String line;
      while ((line = br.readLine()) != null) {
        Matcher m = pTaggerEntry.matcher(line);
        if (m.matches()) {
          int freq = 0;
          String key=m.group(1);
          if (freqList.containsKey(key)) {
            freq = freqList.get(key);
            freqValuesApplied++;
          }
          // Convert integers 0-255 to ranges A-J, and write output 
          String freqChar = Character.toString((char) (FIRST_RANGE_CODE + freq*FREQ_RANGES_OUT/FREQ_RANGES_IN));
          bw.write(line + freqChar + "\n");
        }
      }
      br.close();
      bw.close();
      System.out.println(freqList.size()+" frequency values applied in "+freqValuesApplied+" word forms.");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return tempFile;
  }

}
