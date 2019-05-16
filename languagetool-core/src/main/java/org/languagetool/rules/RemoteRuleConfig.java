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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RemoteRuleConfig {
  private static final int DEFAULT_PORT = 443;
  private static final int DEFAULT_RETRIES = 0;
  private static final long DEFAULT_TIMEOUT = 1000;
  private static final int DEFAULT_FALL = 1;
  private static final int DEFAULT_DOWN = 5000;

  private final String ruleId;

  private final String url;
  private final Integer port;

  private final Integer maxRetries;
  private final Long timeoutMilliseconds;

  private final Integer fall;
  private final Long downMilliseconds;

  private final Map<String, String> options;

  // TODO configure error handling, health checks, timeouts, ...

  @JsonCreator
  public RemoteRuleConfig(@JsonProperty("ruleId") String ruleId,
                          @JsonProperty("url") String url,
                          @JsonProperty("port") Integer port,
                          @JsonProperty("maxRetries") Integer maxRetries,
                          @JsonProperty("timeoutMilliseconds") Long timeoutMilliseconds,
                          @JsonProperty("fall") Integer fall,
                          @JsonProperty("downMilliseconds") Long downMilliseconds,
                          @JsonProperty("options") Map<String, String> options) {
    this.ruleId = ruleId;
    this.url = url;
    this.port = port;
    this.maxRetries = maxRetries;
    this.timeoutMilliseconds = timeoutMilliseconds;
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
    return port  != null ? port : DEFAULT_PORT;
  }
  public int getMaxRetries() {
    return maxRetries != null ? maxRetries : DEFAULT_RETRIES;
  }
  public long getTimeoutMilliseconds() {
    return timeoutMilliseconds != null ? timeoutMilliseconds : DEFAULT_TIMEOUT;
  }
  public int getFall() {
    return fall != null ? fall : DEFAULT_FALL;
  }
  public long getDownMilliseconds() {
    return downMilliseconds != null ? downMilliseconds : DEFAULT_DOWN;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("ruleId", ruleId)
      .append("url", url)
      .append("port", port)
      .append("maxRetries", maxRetries)
      .append("timeout", timeoutMilliseconds)
      .append("fall", fall)
      .append("down", downMilliseconds)
      .append("options", options)
      .build();
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public static RemoteRuleConfig getRelevantConfig(Rule rule, List<RemoteRuleConfig> configs) {
    return configs.stream().filter(config -> config.getRuleId().equals(rule.getId())).findFirst().orElse(null);
  }

  public static List<RemoteRuleConfig> parse(InputStream json) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(json, new TypeReference<List<RemoteRuleConfig>>() {});
  }

}
