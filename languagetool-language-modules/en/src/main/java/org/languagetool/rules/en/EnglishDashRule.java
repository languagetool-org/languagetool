/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.en;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.languagetool.rules.AbstractDashRule;
import org.languagetool.rules.Example;
import org.languagetool.tools.Tools;

import java.util.ResourceBundle;

/**
 * Check for compounds written with dashes instead of hyphens.
 * @since 3.8
 */
public class EnglishDashRule extends AbstractDashRule {

  private static volatile AhoCorasickDoubleArrayTrie<String> trie;

  public EnglishDashRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("I'll buy a new <marker>T—shirt</marker>."),
                   Example.fixed("I'll buy a new <marker>T-shirt</marker>."));
    setUrl(Tools.getUrl("https://languagetool.org/insights/post/hyphen/"));
  }

  @Override
  public String getId() {
    return "EN_DASH_RULE";
  }

  @Override
  public String getDescription() {
    return "Checks if hyphenated words were spelled with dashes (e.g., 'T — shirt' instead 'T-shirt').";
  }

  @Override
  public String getMessage() {
    return "A dash was used instead of a hyphen.";
  }

  @Override
  protected AhoCorasickDoubleArrayTrie<String> getCompoundsData() {
    AhoCorasickDoubleArrayTrie<String> data = trie;
    if (data == null) {
      synchronized (EnglishDashRule.class) {
        data = trie;
        if (data == null) {
          trie = data = loadCompoundFile("/en/compounds.txt");
        }
      }
    }

    return data;
  }
}
