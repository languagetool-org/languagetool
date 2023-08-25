/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Tag;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SpecificIdRule extends Rule {

  private final String id;
  private final String desc;

  public SpecificIdRule(String id, String desc, boolean isPremium, Category category, List<Tag> tags) {
    this.id = Objects.requireNonNull(id);
    this.desc = Objects.requireNonNull(desc);
    this.setPremium(isPremium);
    setCategory(category);
    setTags(tags);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDescription() {
    return desc;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    return RuleMatch.EMPTY_ARRAY;
  }

}
