/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract homophones from CMUdict (or similar pronunciation dictionary)
 */
public class HomophonesFromCMUDict {
  
  private void run(String filename) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(new File(filename))) {
      String title = "";
      int lineCount = 0;
      Map<String, List<String>> rmap = new HashMap<String, List<String>>();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
	int firstSpace = line.indexOf(' ');
	String pron = line.substring(firstSpace + 1);
	String word = line.substring(0, firstSpace);
	if(word.contains('(') {
	  word = word.substring(0, word.indexOf('('));
	}
	List<String> cur = rmap.get(pron);
	if(cur==null) {
	  cur = new ArrayList<String>();
	  rmap.put(pron, cur);
	}
	cur.add(word);
      }
      for(Map.Entry<String, List<String>> me : rmap.entrySet()) {
        if(me.getValue().size() > 1) {
	  System.out.println(String.join("; ", me.getValue()));
        }
      }
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
	  /*
    if (args.length != 1) {
      System.out.println("Usage: " + HomophonesFromCMUDict.class.getSimpleName() + " <filename>");
      System.out.println("       <filename> is a pronunciation dictionary in CMUdict format");
      System.exit(1);
    }*/
    HomophonesFromCMUDict extractor = new HomophonesFromCMUDict();
    extractor.run(args[0]);
  }

}
