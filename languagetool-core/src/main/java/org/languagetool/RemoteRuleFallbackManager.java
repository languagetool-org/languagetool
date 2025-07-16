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

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.rules.RemoteRuleConfig;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public enum RemoteRuleFallbackManager {

  INSTANCE;

  private final AtomicBoolean initCalled = new AtomicBoolean(false);

  private final Map<String, RemoteRuleConfig> fallbackConfigs = new ConcurrentHashMap<>();

  public void init(@NotNull File remoteRuleConfigFile) throws ExecutionException {
    initCalled.set(true);
    if (initCalled.get()) {
      return;
    }
    var config = RemoteRuleConfig.load(remoteRuleConfigFile);

    config.forEach(remoteRuleConfig -> {
      String fallbackRuleId = remoteRuleConfig.getFallbackRuleId();
      if (fallbackRuleId != null && !fallbackRuleId.isEmpty()) {
        var fallbackRulesFor = config.stream().filter(rc -> rc.getRuleId().equals(fallbackRuleId)).toList();
        if (fallbackRulesFor.size() != 1) {
          log.warn("Fallback rule {} for remote rule {} not found or not unique, skipping fallback configuration.", fallbackRuleId, remoteRuleConfig.ruleId);
          return;
        }
        fallbackConfigs.put(remoteRuleConfig.ruleId, fallbackRulesFor.get(0));
      }
    });
  }

  @Nullable
  public RemoteRuleConfig getInhouseFallback(@NotNull String ruleId) {
    if (!initCalled.get()) {
      log.warn("RemoteRuleFallbackManager not initialized, cannot find fallback for rule {}", ruleId);
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
}
