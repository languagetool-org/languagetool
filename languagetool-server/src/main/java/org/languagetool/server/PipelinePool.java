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

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.languagetool.*;
import org.languagetool.gui.Configuration;
import org.languagetool.rules.DictionaryMatchFilter;
import org.languagetool.rules.DictionarySpellMatchFilter;
import org.languagetool.rules.RemoteRuleConfig;
import org.languagetool.rules.Rule;
import org.languagetool.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Caches pre-configured JLanguageTool instances to avoid costly setup time of rules, etc.
 */
class PipelinePool implements KeyedPooledObjectFactory<PipelineSettings, Pipeline> {

  private static final Logger logger = LoggerFactory.getLogger(PipelinePool.class);

  private final KeyedObjectPool<PipelineSettings, Pipeline> pool;

  private final HTTPServerConfig config;
  private final ResultCache cache;
  private final boolean internalServer;

  PipelinePool(HTTPServerConfig config, ResultCache cache, boolean internalServer) {
    this.internalServer = internalServer;
    this.config = config;
    this.cache = cache;
    int maxPoolSize = config.getMaxPipelinePoolSize();
    if (config.isPipelineCachingEnabled()) {
      GenericKeyedObjectPoolConfig<Pipeline> poolConfig = new GenericKeyedObjectPoolConfig<>();
      poolConfig.setMaxTotal(maxPoolSize);
      poolConfig.setMaxIdlePerKey(maxPoolSize);
      poolConfig.setMaxTotalPerKey(maxPoolSize);
      poolConfig.setMinIdlePerKey(0);
      poolConfig.setBlockWhenExhausted(false);
      // could try setting wait time, idle time (from expireTime), use another eviction policy, ...
      this.pool = new GenericKeyedObjectPool<>(this, poolConfig);
    } else {
      this.pool = null;
    }
  }

  Pipeline getPipeline(PipelineSettings settings) throws Exception {
    if (pool == null) {
      return createPipeline(settings.lang, settings.motherTongue, settings.query, settings.globalConfig, settings.userConfig, config.getDisabledRuleIds());
    } else {
      try {
        long time = System.currentTimeMillis();
        logger.debug("Requesting pipeline; pool has {} active objects, {} idle; pipeline settings: {}",
          pool.getNumActive(), pool.getNumIdle(), settings);
        Pipeline p = pool.borrowObject(settings);
        logger.debug("Fetching pipeline took {}ms; pool has {} active objects, {} idle; pipeline settings: {}",
          System.currentTimeMillis() - time, pool.getNumActive(), pool.getNumIdle(), settings);
        return p;
      } catch(NoSuchElementException ignored) {
        logger.info("Pipeline pool capacity reached: {} active objects, {} idle",
          pool.getNumActive(), pool.getNumIdle());
        return createPipeline(settings.lang, settings.motherTongue, settings.query, settings.globalConfig, settings.userConfig, config.getDisabledRuleIds());
      }
    }
  }


  void returnPipeline(PipelineSettings settings, Pipeline pipeline) throws Exception {
    if (pool == null) return;
    try {
      pool.returnObject(settings, pipeline);
    } catch(IllegalStateException e) {
      // this might happen when pool capacity is reached and we return newly created objects that were never borrowed
      logger.info("Exception while trying to return pipeline to pool;" +
        " this is expected when pipeline capacity is reached", e);
    }
  }

  /**
   * Create a JLanguageTool instance for a specific language, mother tongue, and rule configuration.
   * Uses Pipeline wrapper to safely share objects
   *  @param lang the language to be used
   * @param motherTongue the user's mother tongue or {@code null}
   */
  Pipeline createPipeline(Language lang, Language motherTongue, TextChecker.QueryParams params, GlobalConfig globalConfig,
                          UserConfig userConfig, List<String> disabledRuleIds)
    throws Exception { // package-private for mocking
    Pipeline lt = new Pipeline(lang, params.altLanguages, motherTongue, cache, globalConfig, userConfig, params.inputLogging);
    lt.setMaxErrorsPerWordRate(config.getMaxErrorsPerWordRate());
    lt.disableRules(disabledRuleIds);
    if (config.getLanguageModelDir() != null) {
      lt.activateLanguageModelRules(config.getLanguageModelDir());
    }
    if (config.getWord2VecModelDir () != null) {
      lt.activateWord2VecModelRules(config.getWord2VecModelDir());
    }
    if (config.getRulesConfigFile() != null) {
      configureFromRulesFile(lt, lang);
    } else {
      configureFromGUI(lt, lang);
    }
    if (params.regressionTestMode) {
      List<RemoteRuleConfig> rules = Collections.emptyList();
      try {
        if (config.getRemoteRulesConfigFile() != null) {
          rules = RemoteRuleConfig.load(config.getRemoteRulesConfigFile());
        }
      } catch (Exception e) {
        logger.error("Could not load remote rule configuration", e);
      }
      // modify remote rule configuration to avoid timeouts

      // temporary workaround: don't run into check timeout, causes limit enforcement;
      // extend timeout as long as possible instead
      long timeout = Math.max(config.getMaxCheckTimeMillisAnonymous() - 1, 0);
      rules = rules.stream().map(c -> {
        RemoteRuleConfig config = new RemoteRuleConfig(c);
        config.baseTimeoutMilliseconds = timeout;
        config.timeoutPerCharacterMilliseconds = 0f;
        return config;
      }).collect(Collectors.toList());
      lt.activateRemoteRules(rules);
    } else {
      lt.activateRemoteRules(config.getRemoteRulesConfigFile());
    }
    if (params.useQuerySettings) {
      Tools.selectRules(lt, new HashSet<>(params.disabledCategories), new HashSet<>(params.enabledCategories),
        new HashSet<>(params.disabledRules), new HashSet<>(params.enabledRules), params.useEnabledOnly, params.enableTempOffRules);
    }
    if (userConfig.filterDictionaryMatches()) {
      lt.addMatchFilter(new DictionaryMatchFilter(userConfig));
    }
    lt.addMatchFilter(new DictionarySpellMatchFilter(userConfig));

    Premium premium = Premium.get();
    if (config.isPremiumOnly()) {
      //System.out.println("Enabling ONLY premium rules.");
      int premiumEnabled = 0;
      int otherDisabled = 0;
      for (Rule rule : lt.getAllActiveRules()) {
        if (premium.isPremiumRule(rule)) {
          lt.enableRule(rule.getFullId());
          premiumEnabled++;
        } else {
          lt.disableRule(rule.getFullId());
          otherDisabled++;
        }
      }
      //System.out.println("Enabled " + premiumEnabled + " premium rules, disabled " + otherDisabled + " non-premium rules.");
    } else if (!params.premium && !params.enableHiddenRules) { // compute premium matches locally to use as hidden matches
      if (!(premium instanceof PremiumOff)) {
        for (Rule rule : lt.getAllActiveRules()) {
          if (premium.isPremiumRule(rule)) {
            lt.disableRule(rule.getFullId());
          }
        }
      }
    }

    if (pool != null) {
      lt.setupFinished();
    }
    return lt;
  }

  private void configureFromRulesFile(JLanguageTool lt, Language lang) throws IOException {
    ServerTools.print("Using options configured in " + config.getRulesConfigFile());
    // If we are explicitly configuring from rules, ignore the useGUIConfig flag
    if (config.getRulesConfigFile() != null) {
      org.languagetool.gui.Tools.configureFromRules(lt, new Configuration(config.getRulesConfigFile()
        .getCanonicalFile().getParentFile(), config.getRulesConfigFile().getName(), lang));
    } else {
      throw new RuntimeException("config.getRulesConfigFile() is null");
    }
  }

  private void configureFromGUI(JLanguageTool lt, Language lang) throws IOException {
    Configuration config = new Configuration(lang);
    if (internalServer && config.getUseGUIConfig()) {
      ServerTools.print("Using options configured in the GUI");
      org.languagetool.gui.Tools.configureFromRules(lt, config);
    }
  }

  @Override
  public PooledObject<Pipeline> makeObject(PipelineSettings pipelineSettings) throws Exception {
    return new DefaultPooledObject<>(createPipeline(pipelineSettings.lang, pipelineSettings.motherTongue, pipelineSettings.query,
      pipelineSettings.globalConfig, pipelineSettings.userConfig, config.getDisabledRuleIds()));
  }

  @Override
  public void destroyObject(PipelineSettings pipelineSettings, PooledObject<Pipeline> pooledObject) throws Exception {
  }

  @Override
  public boolean validateObject(PipelineSettings pipelineSettings, PooledObject<Pipeline> pooledObject) {
    return true;
  }

  // can make equal on pipeline settings more liberal (e.g. equal language, but some rule IDs disabled)
  // and make required changes to the pipeline before activating/passivating
  // to improve reuse and reduce number of stored pipelines
  @Override
  public void activateObject(PipelineSettings pipelineSettings, PooledObject<Pipeline> pooledObject) throws Exception {
  }

  @Override
  public void passivateObject(PipelineSettings pipelineSettings, PooledObject<Pipeline> pooledObject) throws Exception {
  }

}
