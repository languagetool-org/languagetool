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

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.ml.MLServerProto;
import org.languagetool.tools.Tools;

import java.util.List;
import java.util.ResourceBundle;

public class GRPCConfusionRule extends GRPCRule {

  public GRPCConfusionRule(ResourceBundle messages, RemoteRuleConfig config) {
    super(messages, config);
  }

  @Override
  protected String getMessage(MLServerProto.Match match, AnalyzedSentence sentence) {
    String matched = sentence.getText().substring(match.getOffset(), match.getOffset() + match.getLength());
    List<String> suggestions = match.getSuggestionsList();
    if (suggestions == null || suggestions.isEmpty()) {
      return Tools.i18n(messages, "confpairs_rule_message_no_suggestion", matched);
    } else if (suggestions.size() == 1) {
      return Tools.i18n(messages, "confpairs_rule_message_one_suggestion", matched, suggestions.get(0));
    } else {
      return Tools.i18n(messages, "confpairs_rule_message_more_suggestions", matched);
    }
  }

  @Override
  public String getDescription() {
    return messages.getString("confpairs_rule_description");
  }
}
