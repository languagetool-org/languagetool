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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.ITSIssueType;

/**
 * A rule that matches words for which better alternatives exist and suggests them instead.
 * On top of generic replacement list supports allowed word contexts, e.g.
 * спасіння=ctx: релігія,поезія|рятування|...
 * Loads the relevant words from <code>rules/uk/replace_soft.txt</code>.
 * 
 * TODO: AbstractSimpleReplaceRule loads context as part of suggestion list
 * and to be able to merge contexts for different lemmas we need to extract context out
 * of suggestions list on every match. We may need to write our own replacement loader to make it right.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceSoftRule extends AbstractSimpleReplaceRule {

  private static final String CONTEXT_PREFIX = "ctx:";
  private static final Map<String, List<String>> WRONG_WORDS = loadFromPath("/uk/replace_soft.txt");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return WRONG_WORDS;
  }

  public SimpleReplaceSoftRule(ResourceBundle messages, final Language language) throws IOException {
    super(messages, language);
    setLocQualityIssueType(ITSIssueType.Style);
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE_SOFT";
  }

  @Override
  public String getDescription() {
    return "Пошук нерекомендованих слів";
  }

  @Override
  public String getShort() {
    return "Нерекомендоване слово";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    ContextRepl repl = findContext(replacements);
    String replaceText = StringUtils.join(repl.replacements, ", ");

    // this is a bit ugly as we're modifying original list
    replacements.retainAll(repl.replacements);
    
    if( repl.contexts.size() > 0 ) {
      return "«" + tokenStr + "» вживається лише в таких контекстах: " 
          + StringUtils.join(repl.contexts, ", ")
          + ", можливо, ви мали на увазі: " + replaceText + "?";
    }

    return "«" + tokenStr + "» — нерекомендоване слово, кращий варіант: " + replaceText + ".";
  }

  @Override
  protected boolean isTokenException(AnalyzedTokenReadings atr) {
    // завидна - could be normal adv
    return "завидна".equals(atr.getCleanToken()) || super.isTokenException(atr);
  }
  
  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  private static ContextRepl findContext(List<String> replacements) {
    ContextRepl contextRepl = new ContextRepl();
    
    for (String replacement: replacements) {
      if( replacement.startsWith(CONTEXT_PREFIX) ) {
        contextRepl.contexts.addAll(Arrays.asList(replacement.replace(CONTEXT_PREFIX, "").trim().split(", *")));
      } else {
        contextRepl.replacements.add(replacement);
      }
    }
    
    return contextRepl;
  }

  private static final class ContextRepl {
    final List<String> contexts = new ArrayList<>();
    final List<String> replacements = new ArrayList<>();
  }
  
}
