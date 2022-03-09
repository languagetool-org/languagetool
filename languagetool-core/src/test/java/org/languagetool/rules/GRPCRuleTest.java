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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;
import org.languagetool.rules.ml.MLServerProto;

import java.io.IOException;
import java.util.Arrays;

@Ignore("Requires local ML server")
public class GRPCRuleTest {

  public static final String RULE_ID = "REMOTE_ML_RULE";
  protected RemoteRuleConfig config;
  protected GRPCRule rule;

  @Before
  public void setUp() throws Exception {
    config = new RemoteRuleConfig();

    config.ruleId = RULE_ID;
    config.url = "localhost";
    config.port = 50000;


    rule = new GRPCRule(new Demo(), JLanguageTool.getMessageBundle(), config, true) {
      @Override
      protected String getMessage(MLServerProto.Match match, AnalyzedSentence sentence) {
        return "Matched: " + match.toString().replaceAll("\n", " | ");
      }

      @Override
      public String getId() {
        return RULE_ID;
      }

      @Override
      public String getDescription() {
        return "Test rule";
      }
    };
  }

  @Test
  public void testMatch() throws IOException {
    JLanguageTool lt = new JLanguageTool(TestTools.getDemoLanguage());
    AnalyzedSentence s  = lt.getAnalyzedSentence("This is a test.");
    Arrays.asList(rule.match(s)).forEach(System.out::println);
  }
}
