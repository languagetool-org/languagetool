/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikCatalanSpellerRule extends MorfologikSpellerRule {

  //private static final String RESOURCE_FILENAME = "/ca/hunspell/ca_ES.dict";
  private static final String RESOURCE_FILENAME = "/ca/catalan.dict";
  //private static final String FILE_NAME = "/ca/frequentwords.txt";
  //private static final String FILE_ENCODING = "utf-8";
  //private final List<String> frequentWords;
  
  public MorfologikCatalanSpellerRule(ResourceBundle messages,
                                      Language language) throws IOException {
    super(messages, language);
    this.setIgnoreTaggedWords();
    //frequentWords=loadWords(FILE_NAME);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_CA_ES";
  }
  
 /* @Override
  protected List<String> orderSuggestions(List<String> suggestions, String word) {
    List<String> orderedSuggestions1 = new ArrayList<String>();
    List<String> orderedSuggestions2 = new ArrayList<String>();
    for (String suggestion : suggestions) {
        if (frequentWords.contains(suggestion)) {
          orderedSuggestions1.add(suggestion);
        } else {
          orderedSuggestions2.add(suggestion);
        }      
    }
    List<String> orderedSuggestions = new ArrayList<String>();
    orderedSuggestions.addAll(orderedSuggestions1);
    orderedSuggestions.addAll(orderedSuggestions2);
    return orderedSuggestions;
  }*/
  
  /**
   * Load words.
   */
  /*private List<String> loadWords(String fileName) throws IOException {
    final ArrayList<String> list = new ArrayList<String>();
    final InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(fileName);
    final Scanner scanner = new Scanner(inputStream, FILE_ENCODING);
    try {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().trim();
        if (line.length() < 1) {
          continue;
        }
        if (line.charAt(0) == '#') {      // ignore comments
          continue;
        }
        list.add(line);
      }
    } finally {
      scanner.close();
    }
    return list;
  }*/
  

}
