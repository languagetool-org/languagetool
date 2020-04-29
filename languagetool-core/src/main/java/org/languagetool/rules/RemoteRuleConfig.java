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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RemoteRuleConfig {
  private static final int DEFAULT_PORT = 443;
  private static final int DEFAULT_RETRIES = 0;
  private static final long DEFAULT_BASE_TIMEOUT = 1000;
  private static final float DEFAULT_TIMEOUT_PER_CHAR = 0;
  private static final int DEFAULT_FALL = 1;
  private static final int DEFAULT_DOWN = 5000;

  private static final LoadingCache<File, List<RemoteRuleConfig>> configCache = CacheBuilder.newBuilder()
    .expireAfterWrite(15, TimeUnit.MINUTES)
    .build(new CacheLoader<File, List<RemoteRuleConfig>>() {
      @Override
      public List<RemoteRuleConfig> load(File path) throws Exception {
        try (FileInputStream in = new FileInputStream(path)) {
          return parse(in);
        }
      }
    });

  private final String ruleId;

  private final String url;
  private final Integer port;

  private final Integer maxRetries;
  private final Long baseTimeoutMilliseconds;
  private final Float timeoutPerCharacterMilliseconds;

  private final Integer fall;
  private final Long downMilliseconds;

  private final Map<String, String> options;

  // TODO configure health checks, load balancing, ...?

  @JsonCreator
  public RemoteRuleConfig(@JsonProperty("ruleId") String ruleId,
                          @JsonProperty("url") String url,
                          @JsonProperty("port") Integer port,
                          @JsonProperty("maxRetries") Integer maxRetries,
                          @JsonProperty("baseTimeoutMilliseconds") Long baseTimeoutMilliseconds,
                          @JsonProperty("timeoutPerCharacterMilliseconds") Float timeoutPerCharacterMilliseconds,
                          @JsonProperty("fall") Integer fall,
                          @JsonProperty("downMilliseconds") Long downMilliseconds,
                          @JsonProperty("options") Map<String, String> options) {
    this.ruleId = ruleId;
    this.url = url;
    this.port = port;
    this.maxRetries = maxRetries;
    this.baseTimeoutMilliseconds = baseTimeoutMilliseconds;
    this.timeoutPerCharacterMilliseconds = timeoutPerCharacterMilliseconds;
    this.fall = fall;
    this.downMilliseconds = downMilliseconds;
    this.options = Collections.unmodifiableMap(options != null ? options : Collections.emptyMap());
  }

  public String getRuleId() {
    return ruleId;
  }
  public String getUrl() {
    return url;
  }
  public int getPort() {
    return port != null ? port : DEFAULT_PORT;
  }
  public int getMaxRetries() {
    return maxRetries != null ? maxRetries : DEFAULT_RETRIES;
  }
  public int getFall() {
    return fall != null ? fall : DEFAULT_FALL;
  }
  public long getDownMilliseconds() {
    return downMilliseconds != null ? downMilliseconds : DEFAULT_DOWN;
  }
  public long getBaseTimeoutMilliseconds() {
    return baseTimeoutMilliseconds != null ? baseTimeoutMilliseconds : DEFAULT_BASE_TIMEOUT;
  }

  public float getTimeoutPerCharacterMilliseconds() {
    return timeoutPerCharacterMilliseconds != null ? timeoutPerCharacterMilliseconds : DEFAULT_TIMEOUT_PER_CHAR;
  }
  public Map<String, String> getOptions() {
    return options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RemoteRuleConfig that = (RemoteRuleConfig) o;

    return new EqualsBuilder()
      .append(ruleId, that.ruleId)
      .append(url, that.url)
      .append(port, that.port)
      .append(maxRetries, that.maxRetries)
      .append(baseTimeoutMilliseconds, that.baseTimeoutMilliseconds)
      .append(timeoutPerCharacterMilliseconds, that.timeoutPerCharacterMilliseconds)
      .append(fall, that.fall)
      .append(downMilliseconds, that.downMilliseconds)
      .append(options, that.options)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(ruleId)
      .append(url)
      .append(port)
      .append(maxRetries)
      .append(baseTimeoutMilliseconds)
      .append(timeoutPerCharacterMilliseconds)
      .append(fall)
      .append(downMilliseconds)
      .append(options)
      .toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("ruleId", ruleId)
      .append("url", url)
      .append("port", port)
      .append("maxRetries", maxRetries)
      .append("baseTimeout", baseTimeoutMilliseconds)
      .append("timeoutPerCharacter", timeoutPerCharacterMilliseconds)
      .append("fall", fall)
      .append("down", downMilliseconds)
      .append("options", options)
      .build();
  }

  public static RemoteRuleConfig getRelevantConfig(String rule, List<RemoteRuleConfig> configs) {
    return configs.stream().filter(config -> config.getRuleId().equals(rule)).findFirst().orElse(null);
  }

  public static List<RemoteRuleConfig> parse(InputStream json) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new JsonFactory().enable(JsonParser.Feature.ALLOW_COMMENTS));
    return mapper.readValue(json, new TypeReference<List<RemoteRuleConfig>>() {});
  }

  public static List<RemoteRuleConfig> load(File configFile) throws ExecutionException {
    return configCache.get(configFile);
  }

}
