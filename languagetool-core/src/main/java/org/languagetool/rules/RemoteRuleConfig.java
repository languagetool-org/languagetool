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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("PublicField")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteRuleConfig {
  private static final int DEFAULT_PORT = 443;
  private static final long DEFAULT_BASE_TIMEOUT = 1000;
  private static final float DEFAULT_TIMEOUT_PER_CHAR = 0;
  private static final long DEFAULT_DOWN = 5000L;
  private static final float DEFAULT_FAILURE_RATE_THRESHOLD = 50f;
  private static final String DEFAULT_SLIDING_WINDOW_TYPE = CircuitBreakerConfig.SlidingWindowType.TIME_BASED.name();
  private static final int DEFAULT_SLIDING_WINDOW_SIZE = 60;
  private static final int DEFAULT_MINIMUM_NUMBER_OF_CALLS = 10;


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
  public String ruleId;
  public String url;
  public Integer port = DEFAULT_PORT;
  public long baseTimeoutMilliseconds = DEFAULT_BASE_TIMEOUT;
  public float timeoutPerCharacterMilliseconds = DEFAULT_TIMEOUT_PER_CHAR;
  public long downMilliseconds = DEFAULT_DOWN;
  public float failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;
  public String slidingWindowType = DEFAULT_SLIDING_WINDOW_TYPE;
  public int slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;
  public int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_OF_CALLS;
  public Map<String, String> options = new HashMap<>();

  public RemoteRuleConfig() {
  }

  public RemoteRuleConfig(RemoteRuleConfig copy) {
    this.ruleId = copy.ruleId;
    this.url = copy.url;
    this.port = copy.port;
    this.baseTimeoutMilliseconds = copy.baseTimeoutMilliseconds;
    this.timeoutPerCharacterMilliseconds = copy.timeoutPerCharacterMilliseconds;
    this.downMilliseconds = copy.downMilliseconds;
    this.failureRateThreshold = copy.failureRateThreshold;
    this.slidingWindowType = copy.slidingWindowType;
    this.slidingWindowSize = copy.slidingWindowSize;
    this.minimumNumberOfCalls = copy.minimumNumberOfCalls;
    this.options = new HashMap<>(copy.options);
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

  public float getFailureRateThreshold() {
    return failureRateThreshold;
  }

  public String getSlidingWindowType() {
    return slidingWindowType;
  }

  public int getSlidingWindowSize() {
    return slidingWindowSize;
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

  public long getDownMilliseconds() {
    return downMilliseconds;
  }

  public long getBaseTimeoutMilliseconds() {
    return baseTimeoutMilliseconds;
  }

  public float getTimeoutPerCharacterMilliseconds() {
    return timeoutPerCharacterMilliseconds;
  }

  public int getMinimumNumberOfCalls() {
    return minimumNumberOfCalls;
  }

  /**
   *  miscellaneous options for remote rules
   *  allows implementing additional behavior in subclasses
   *  some options defined in {@link RemoteRule}:
   *  fixOffsets: boolean - adjust offsets of matches because of discrepancies in string length for some unicode characters between Java and Python
   *  filterMatches: boolean - enable anti-patterns from remote-rule-filters.xml
   *  suppressMisspelledMatch: regex - filter out matches with matching rule IDs that have misspelled suggestions
   *  suppressMisspelledSuggestions: regex - filter out misspelled suggestions from matches with matching rule IDs
   *  */
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

    return new EqualsBuilder().append(baseTimeoutMilliseconds, that.baseTimeoutMilliseconds).append(timeoutPerCharacterMilliseconds, that.timeoutPerCharacterMilliseconds).append(downMilliseconds, that.downMilliseconds).append(failureRateThreshold, that.failureRateThreshold).append(slidingWindowSize, that.slidingWindowSize).append(minimumNumberOfCalls, that.minimumNumberOfCalls).append(ruleId, that.ruleId).append(url, that.url).append(port, that.port).append(slidingWindowType, that.slidingWindowType).append(options, that.options).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(ruleId).append(url).append(port).append(baseTimeoutMilliseconds).append(timeoutPerCharacterMilliseconds).append(downMilliseconds).append(failureRateThreshold).append(slidingWindowType).append(slidingWindowSize).append(minimumNumberOfCalls).append(options).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("ruleId", ruleId)
      .append("url", url)
      .append("port", port)
      .append("baseTimeoutMilliseconds", baseTimeoutMilliseconds)
      .append("timeoutPerCharacterMilliseconds", timeoutPerCharacterMilliseconds)
      .append("downMilliseconds", downMilliseconds)
      .append("failureRateThreshold", failureRateThreshold)
      .append("slidingWindowType", slidingWindowType)
      .append("slidingWindowSize", slidingWindowSize)
      .append("minimumNumberOfCalls", minimumNumberOfCalls)
      .append("options", options)
      .toString();
  }

}
