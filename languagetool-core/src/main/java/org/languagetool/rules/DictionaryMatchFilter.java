/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import org.languagetool.UserConfig;
import org.languagetool.markup.AnnotatedText;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DictionaryMatchFilter implements RuleMatchFilter {
  private final UserConfig userConfig;

  public DictionaryMatchFilter(UserConfig userConfig) {
    this.userConfig = userConfig;
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches, AnnotatedText text) {
    Set<String> dictionary = new HashSet<>(userConfig.getAcceptedWords());

    return ruleMatches.stream().filter(match -> {
      // at this point, the offsets we get from match are already converted to be pointing to the text with markup
      // so we need to compute the substring based on that
      // using anything else leads to StringIndexOutOfBoundsErrors or getting the wrong text
      // if there's no markup, this is just equal to the original text
      String covered = text.getTextWithMarkup().substring(match.getFromPos(), match.getToPos());
      return !dictionary.contains(covered);
    }).collect(Collectors.toList());
  }
}
