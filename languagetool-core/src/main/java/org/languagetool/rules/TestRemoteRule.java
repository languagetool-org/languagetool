/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
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
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class TestRemoteRule extends RemoteRule {

    private long waitTime;

    public TestRemoteRule(Language lang, RemoteRuleConfig config) {
      super(lang, JLanguageTool.getMessageBundle(), config, false);
      waitTime = Long.parseLong(config.getOptions().getOrDefault("waitTime", "-1"));
    }

    class TestRemoteRequest extends RemoteRequest {
      private final List<AnalyzedSentence> sentences;

      TestRemoteRequest(List<AnalyzedSentence> sentences) {
        this.sentences = sentences;
      }
    }

    @Override
    protected RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, Long textSessionId) {
      return new TestRemoteRequest(sentences);
    }

    private RuleMatch testMatch(AnalyzedSentence s) {
      return new RuleMatch(this, s, 0, 1, "Test match");
    }

    @Override
    protected Callable<RemoteRuleResult> executeRequest(RemoteRequest request, long timeoutMilliseconds) throws TimeoutException {
      return () -> {
        TestRemoteRequest req = (TestRemoteRequest) request;
        List<RuleMatch> matches = req.sentences.stream().map(this::testMatch).collect(Collectors.toList());
        long deadline = System.currentTimeMillis() + waitTime;
        //noinspection StatementWithEmptyBody
        Thread.sleep(waitTime);
        // cancelling only works if implementations respect interrupts or timeouts
        //while(true);
        return new RemoteRuleResult(true, true, matches, req.sentences);
      };
    }

    @Override
    protected RemoteRuleResult fallbackResults(RemoteRequest request) {
      TestRemoteRequest req = (TestRemoteRequest) request;
      return new RemoteRuleResult(false, false, Collections.emptyList(), req.sentences);
    }

    @Override
    public String getDescription() {
      return "TEST REMOTE RULE";
    }
}
