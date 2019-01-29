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

package org.languagetool.server;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.mockito.Mockito.*;

@Ignore("For version of SlowRuleLogger that logs to database; can't mock System.out in maven tests anyway")
public class SlowRuleLoggerTest {

  @Test
  public void log() throws IOException {

    //SlowRuleLogger logger = new SlowRuleLogger(0L);
    SlowRuleLogger logger = new SlowRuleLogger(System.out);
    RuleLoggerManager manager = RuleLoggerManager.getInstance();
    DatabaseLogger.instance = spy(DatabaseLogger.getInstance());

    manager.addLogger(logger);
    Rule testRule = new Rule() {
      @Override
      public String getId() {
        return "TEST_RULE";
      }

      @Override
      public String getDescription() {
        return "";
      }

      @Override
      public RuleMatch[] match(AnalyzedSentence sentence) {
        if (sentence.getText().contains("slow")) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
        return new RuleMatch[0];
      }
    };

    JLanguageTool lt = new JLanguageTool(new FakeLanguage());
    lt.addRule(testRule);

    lt.check("This should go fast");
    verify(DatabaseLogger.getInstance(), never()).log(any());
    lt.check("This should be slow");
    verify(DatabaseLogger.getInstance()).log(isA(DatabaseMiscLogEntry.class));
  }
}
