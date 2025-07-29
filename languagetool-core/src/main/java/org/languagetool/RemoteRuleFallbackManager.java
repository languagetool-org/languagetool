/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2025 Stefan Viol (https://stevio.de)
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

package org.languagetool;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.languagetool.rules.RemoteRule;
import org.languagetool.rules.RemoteRuleConfig;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public enum RemoteRuleFallbackManager {

  INSTANCE;

  private final AtomicBoolean initCalled = new AtomicBoolean(false);

  private final Map<String, RemoteRuleConfig> fallbackConfigs = new ConcurrentHashMap<>();

  /***
   * Initialize RemoteRuleFallbackManager from remoteRuleConfigFile, can only been called once
   * @param remoteRuleConfigFile
   */
  public void init(@NotNull File remoteRuleConfigFile) {
    if (initCalled.compareAndSet(false, true)) {
      return;
    }
    List<RemoteRuleConfig> config = null;
    try {
      config = RemoteRuleConfig.load(remoteRuleConfigFile);
    } catch (ExecutionException e) {
      log.error("Could not load remote rule configuration at {}", remoteRuleConfigFile.getAbsolutePath(), e);
    }
    if (config != null) {
      setup(config);
    } else {
      initCalled.set(false);
    }
  }

  /***
   * This method is only for internal tests and must not been called from production code
   * @param config
   */
  @TestOnly
  public void init_for_tests_only(@NotNull List<RemoteRuleConfig> config) {
    this.fallbackConfigs.clear();
    this.initCalled.set(true);
    setup(config);
  }

  private void setup(@NotNull List<RemoteRuleConfig> finalConfig) {
    finalConfig.forEach(remoteRuleConfig -> {
      String fallbackRuleId = remoteRuleConfig.getFallbackRuleId();
      log.info("Found remote rule {} with fallback rule {}", remoteRuleConfig.ruleId, fallbackRuleId);
      if (fallbackRuleId != null && !fallbackRuleId.isEmpty()) {
        var fallbackRulesFor = finalConfig.stream().filter(rc -> rc.getRuleId().equals(fallbackRuleId)).toList();
        if (fallbackRulesFor.size() != 1) {
          //TODO: decide if this is what we want!
          log.warn("Fallback rule {} for remote rule {} not found or not unique, skipping fallback configuration.", fallbackRuleId, remoteRuleConfig.ruleId);
        } else {
          fallbackConfigs.put(remoteRuleConfig.ruleId, fallbackRulesFor.get(0));
        }
      }
    });
    log.info("Loaded {} fallback rules: {}", fallbackConfigs.size(), fallbackConfigs.keySet());
  }

  /***
   *
   * @param ruleId a remote RuleId
   * @return the RemoteRuleConfig for the given RuleId
   */
  @Nullable
  public RemoteRuleConfig getInhouseFallback(@NotNull String ruleId) {
    if (!initCalled.get()) {
      log.warn("RemoteRuleFallbackManager not initialized cannot find fallback for rule {}", ruleId);
      return null;
    }
    if (fallbackConfigs.isEmpty()) {
      log.warn("No fallback rules configured, cannot find fallback for rule {}", ruleId);
      return null;
    }
    RemoteRuleConfig fallbackConfig = fallbackConfigs.get(ruleId);
    if (fallbackConfig == null || fallbackConfig.isUsingThirdPartyAI()) {
      log.warn("No fallback rule configured for rule {}, or it is a 3rd party rule, cannot find fallback.", ruleId);
      return null;
    }
    return fallbackConfig;
  }

  /***
   * Check the current status of the circuitBreaker of a remoteRule, and get the fallback option if not available
   * @param rule
   * @param remoteRules
   * @return A RuleId:
   *         1. Same RuleID as rule.getId() -> The rule is available
   *         2. Other RuleID as rule.getId() -> The rule is not available, and you get the fallbackID
   *         3. Null -> The rule is not available and has no available fallbacks
   */
  @Nullable
  public String isRuleOrFallbackAvailable(@NotNull RemoteRule rule, @NotNull Map<String, RemoteRule> remoteRules) {
    return isRuleOrFallbackAvailable(rule, remoteRules, new HashSet<>());
  }

  @Nullable
  private String isRuleOrFallbackAvailable(@NotNull RemoteRule rule, @NotNull Map<String, RemoteRule> remoteRules, Set<String> visited) {
    if (!visited.add(rule.getId())) {
      log.warn("Circular fallback chain detected for rule {}", rule.getId());
      return null;
    }
    var isOpen = rule.circuitBreaker().getState() == CircuitBreaker.State.OPEN;
    if (isOpen) {
      var fallbackRuleId = rule.getServiceConfiguration().getFallbackRuleId();
      if (fallbackRuleId != null && !fallbackRuleId.isEmpty()) {
        RemoteRule fallbackRule = remoteRules.get(fallbackRuleId);
        if (fallbackRule != null) {
          return isRuleOrFallbackAvailable(fallbackRule, remoteRules, visited);
        }
      }
      return null;
    } else {
      return rule.getId();
    }
  }
}
