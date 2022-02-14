/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.pl;

import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordCoherencyDataLoader;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Polish version of {@link AbstractWordCoherencyRule}.
 * 
 * @author Marcin Miłkowski
 */
public class WordCoherencyRule extends AbstractWordCoherencyRule {

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/pl/coherency.txt");

  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    addExamplePair(Example.wrong("Grapefruity są zdrowe. <marker>Grejpfrut</marker> smakuje najlepiej smażony."),
                   Example.fixed("Grapefruity są zdrowe. <marker>Grapefruit</marker> smakuje najlepiej smażony."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "'" + word1 + "' i '" + word2 + "' nie powinny być używane jednocześnie.";
  }
  
  @Override
  public String getId() {
    return "PL_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Jednolita pisownia wyrazów o obocznej dopuszczalnej pisowni";
  }

}
