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

package org.languagetool;

import org.junit.Test;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

import static org.mockito.Mockito.*;

public class SlowRuleLoggerTest {

  @Test
  public void log() throws IOException {

    PrintStream stream = spy(System.out);
    SlowRuleLogger logger = new SlowRuleLogger(stream);
    RuleLoggerManager manager = RuleLoggerManager.getInstance();

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
        long startTime = System.currentTimeMillis();

        if (sentence.getText().contains("slow")) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }

        manager.log(new SlowRuleMessage(this.getId(), "YY", startTime), Level.FINE);
        return new RuleMatch[0];
      }
    };

    JLanguageTool lt = new JLanguageTool(new FakeLanguage());
    lt.addRule(testRule);

    lt.check("This should go fast");
    verify(stream, never()).printf(any(), any());
    lt.check("This should be slow");
    verify(stream).printf(any(), any());
  }
}
