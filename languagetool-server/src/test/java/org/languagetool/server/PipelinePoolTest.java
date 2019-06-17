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

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class PipelinePoolTest {

  private final GlobalConfig gConfig = new GlobalConfig();

  @Test
  public void testPipelineCreatedAndUsed() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("text", "not used");
    params.put("language", "en-US");
    HTTPServerConfig config1 = new HTTPServerConfig(HTTPTools.getDefaultPort());
    config1.setPipelineCaching(true);
    config1.setPipelineExpireTime(10);
    config1.setMaxPipelinePoolSize(10);
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());
    PipelinePool pool = spy(checker.pipelinePool);
    checker.pipelinePool = pool;
    checker.checkText(new AnnotatedTextBuilder().addText("Hello World.").build(), new FakeHttpExchange(), params, null, null);
    Language lang1 = Languages.getLanguageForShortCode("en-US");
    TextChecker.QueryParams queryParams1 = new TextChecker.QueryParams(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(),
      new LinkedList<>(), new LinkedList<>(), false, false, false, false, JLanguageTool.Mode.ALL, null);
    UserConfig user1 = new UserConfig();
    
    PipelinePool.PipelineSettings settings1 = new PipelinePool.PipelineSettings(lang1,
      null, queryParams1, gConfig, user1);
    verify(pool).getPipeline(settings1);
    verify(pool).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool).returnPipeline(eq(settings1), notNull());
    checker.checkText(new AnnotatedTextBuilder().addText("Hello World, second time around.").build(), new FakeHttpExchange(), params, null, null);
    verify(pool, times(2)).getPipeline(settings1);
    verify(pool, times(1)).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool, times(2)).returnPipeline(eq(settings1), notNull());
  }

  @Test
  public void testDifferentPipelineSettings() throws Exception {
    Map<String, String> params1 = new HashMap<>();
    params1.put("text", "not used");
    params1.put("language", "en-US");
    Map<String, String> params2 = new HashMap<>();
    params2.put("text", "not used");
    params2.put("language", "de-DE");
    HTTPServerConfig config1 = new HTTPServerConfig(HTTPTools.getDefaultPort());
    config1.setPipelineCaching(true);
    config1.setPipelineExpireTime(10);
    config1.setMaxPipelinePoolSize(10);
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());
    PipelinePool pool = spy(checker.pipelinePool);
    checker.pipelinePool = pool;
    checker.checkText(new AnnotatedTextBuilder().addText("Hello World.").build(), new FakeHttpExchange(), params1, null, null);
    Language lang1 = Languages.getLanguageForShortCode("en-US");
    Language lang2 = Languages.getLanguageForShortCode("de-DE");
    TextChecker.QueryParams queryParams1 = new TextChecker.QueryParams(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(),
      new LinkedList<>(), new LinkedList<>(), false, false, false, false, JLanguageTool.Mode.ALL, null);
    UserConfig user1 = new UserConfig();

    PipelinePool.PipelineSettings settings1 = new PipelinePool.PipelineSettings(lang1, null, queryParams1, gConfig, user1);
    verify(pool).getPipeline(settings1);
    verify(pool).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool).returnPipeline(eq(settings1), notNull());

    PipelinePool.PipelineSettings settings2 = new PipelinePool.PipelineSettings(lang2, null, queryParams1, gConfig, user1);
    checker.checkText(new AnnotatedTextBuilder().addText("Hallo Welt!").build(), new FakeHttpExchange(), params2, null, null);

    verify(pool, times(1)).getPipeline(settings1);
    verify(pool, times(1)).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool, times(1)).returnPipeline(eq(settings1), notNull());

    verify(pool).getPipeline(settings2);
    verify(pool).createPipeline(lang2, null, queryParams1, gConfig, user1);
    verify(pool).returnPipeline(eq(settings2), notNull());

    TextChecker.QueryParams queryParams2 = new TextChecker.QueryParams(new LinkedList<>(), new LinkedList<>(), Collections.singletonList("DE_CASE"),
      new LinkedList<>(), new LinkedList<>(), false, true, false, false, JLanguageTool.Mode.ALL, null);
    Map<String, String> params3 = new HashMap<>();
    params3.put("language", "de-DE");
    params3.put("text", "not used");
    params3.put("disabledRules", "DE_CASE");
    PipelinePool.PipelineSettings settings3 = new PipelinePool.PipelineSettings(lang2, null, queryParams2, gConfig, user1);
    checker.checkText(new AnnotatedTextBuilder().addText("Hallo Welt!").build(), new FakeHttpExchange(), params3, null, null);

    verify(pool).getPipeline(settings3);
    verify(pool).createPipeline(lang2, null, queryParams2, gConfig, user1);
    verify(pool).returnPipeline(eq(settings3), notNull());
  }

  @Test
  public void testMaxPipelinePoolSize() throws Exception {
    Map<String, String> params1 = new HashMap<>();
    params1.put("text", "not used");
    params1.put("language", "en-US");
    Map<String, String> params2 = new HashMap<>();
    params2.put("text", "not used");
    params2.put("language", "de-DE");
    HTTPServerConfig config1 = new HTTPServerConfig(HTTPTools.getDefaultPort());
    config1.setPipelineCaching(true);
    config1.setPipelineExpireTime(10);
    config1.setMaxPipelinePoolSize(1);
    Language lang1 = Languages.getLanguageForShortCode("en-US");
    Language lang2 = Languages.getLanguageForShortCode("de-DE");
    TextChecker.QueryParams queryParams1 = new TextChecker.QueryParams(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(),
      new LinkedList<>(), new LinkedList<>(), false, false, false, false, JLanguageTool.Mode.ALL, null);
    UserConfig user1 = new UserConfig();
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());

    PipelinePool pool = spy(checker.pipelinePool);
    checker.pipelinePool = pool;

    // size = 1 -> needs to create new pipeline when language changes

    checker.checkText(new AnnotatedTextBuilder().addText("Hello World.").build(), new FakeHttpExchange(), params1, null, null);
    PipelinePool.PipelineSettings settings1 = new PipelinePool.PipelineSettings(lang1, null, queryParams1, gConfig, user1);
    verify(pool).getPipeline(settings1);
    verify(pool).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool).returnPipeline(eq(settings1), notNull());

    checker.checkText(new AnnotatedTextBuilder().addText("Hallo Welt!").build(), new FakeHttpExchange(), params2, null, null);
    PipelinePool.PipelineSettings settings2 = new PipelinePool.PipelineSettings(lang2, null, queryParams1, gConfig, user1);
    verify(pool, times(1)).getPipeline(settings1);
    verify(pool, times(1)).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool, times(1)).returnPipeline(eq(settings1), notNull());

    verify(pool).getPipeline(settings2);
    verify(pool).createPipeline(lang2, null, queryParams1, gConfig, user1);
    verify(pool).returnPipeline(eq(settings2), notNull());

    checker.checkText(new AnnotatedTextBuilder().addText("Hello World.").build(), new FakeHttpExchange(), params1, null, null);
    verify(pool, times(2)).getPipeline(settings1);
    verify(pool, times(2)).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool, times(2)).returnPipeline(eq(settings1), notNull());

    checker.checkText(new AnnotatedTextBuilder().addText("Hallo Welt!").build(), new FakeHttpExchange(), params2, null, null);
    verify(pool, times(2)).getPipeline(settings2);
    verify(pool, times(2)).createPipeline(lang2, null, queryParams1, gConfig, user1);
    verify(pool, times(2)).returnPipeline(eq(settings2), notNull());
  }

  @Test
  public void testPipelinePoolExpireTime() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("text", "not used");
    params.put("language", "en-US");
    int expireTime = 1;
    HTTPServerConfig config1 = new HTTPServerConfig(HTTPTools.getDefaultPort());
    config1.setPipelineCaching(true);
    config1.setPipelineExpireTime(expireTime);
    config1.setMaxPipelinePoolSize(10);
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());
    PipelinePool pool = spy(checker.pipelinePool);
    checker.pipelinePool = pool;
    checker.checkText(new AnnotatedTextBuilder().addText("Hello World.").build(), new FakeHttpExchange(), params, null, null);
    Language lang1 = Languages.getLanguageForShortCode("en-US");
    TextChecker.QueryParams queryParams1 = new TextChecker.QueryParams(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(),
      new LinkedList<>(), new LinkedList<>(), false, false, false, false, JLanguageTool.Mode.ALL, null);
    UserConfig user1 = new UserConfig();

    PipelinePool.PipelineSettings settings1 = new PipelinePool.PipelineSettings(lang1,
      null, queryParams1, gConfig, user1);
    verify(pool).getPipeline(settings1);
    verify(pool).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool).returnPipeline(eq(settings1), notNull());

    Thread.sleep(expireTime * 1000L * 2);

    checker.checkText(new AnnotatedTextBuilder().addText("Hello World, second time around.").build(), new FakeHttpExchange(), params, null, null);
    verify(pool, times(2)).getPipeline(settings1);
    verify(pool, times(2)).createPipeline(lang1, null, queryParams1, gConfig, user1);
    verify(pool, times(2)).returnPipeline(eq(settings1), notNull());
  }

  @Test
  public void testPipelineMutation() {
    Pipeline pipeline = new Pipeline(Languages.getLanguageForShortCode("en-US"),
      new ArrayList<>(), null, null, gConfig, null);
    pipeline.addRule(null);
    pipeline.setupFinished();
    boolean thrown = false;
    try {
      pipeline.addRule(null);
      fail("Expected IllegalPipelineMutationException to be thrown but nothing was thrown.");
    } catch(Pipeline.IllegalPipelineMutationException ignored) {
      thrown = true;
    } catch(Exception e) {
      fail("Expected IllegalPipelineMutationException to be thrown; got " + e);
    } finally {
      assertTrue("IllegalPipelineMutationException was thrown.", thrown);
    }
  }

}
