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
package org.languagetool.rules;

import io.micrometer.core.instrument.Tags;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FakeRule extends Rule {

  private String ruleId = "FAKE-RULE";

  public FakeRule() {}

  public FakeRule(String id) {
    ruleId = id;
  }

  public FakeRule(String id, Tag tag) {
    ruleId = id;
    setTags(Arrays.asList(tag));
  }

  @Override
  public String getId() {
    return ruleId;
  }

  @Override
  public String getDescription() {
    return "<none>";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    return new RuleMatch[0];
  }

}
