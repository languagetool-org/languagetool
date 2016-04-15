/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.languagetool.rules.Rule;
import org.languagetool.rules.pt.PostReformPortugueseCompoundRule;
import org.languagetool.rules.pt.PreReformPortugueseCompoundRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Post-spelling-reform Portuguese.
 * @since 2.6
 * @deprecated see https://github.com/languagetool-org/languagetool/issues/96
 */
public class PostReformPortugalPortuguese extends PortugalPortuguese {

  @Override
  public String getName() {
    return "Portuguese (Portugal, post-reform)";
  }

  @Override
  public String getVariant() {
    return "post-reform";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    List<Rule> filteredRules = new ArrayList<>();
    for (Rule rule : super.getRelevantRules(messages)) {
      if (rule.getClass() != PreReformPortugueseCompoundRule.class) {
        filteredRules.add(rule);
      }
    }
    filteredRules.add(new PostReformPortugueseCompoundRule(messages));
    return filteredRules;
  }

}
