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

package org.languagetool.server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.languagetool.GlobalConfig;
import org.languagetool.Language;
import org.languagetool.UserConfig;

public class PipelineSettings {
  final Language lang;
  final Language motherTongue;
  final TextChecker.QueryParams query;
  final UserConfig userConfig;
  final GlobalConfig globalConfig;

  PipelineSettings(Language lang, UserConfig userConfig) {
    this(lang, null, new TextChecker.QueryParams(), new GlobalConfig(), userConfig);
  }

  PipelineSettings(Language lang, Language motherTongue, TextChecker.QueryParams params, GlobalConfig globalConfig, UserConfig userConfig) {
    this.lang = lang;
    this.motherTongue = motherTongue;
    this.query = params;
    this.userConfig = userConfig;
    this.globalConfig = globalConfig;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31)
      .append(lang)
      .append(motherTongue)
      .append(query)
      .append(globalConfig)
      .append(userConfig)
      .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    PipelineSettings other = (PipelineSettings) obj;
    return new EqualsBuilder()
      .append(lang, other.lang)
      .append(motherTongue, other.motherTongue)
      .append(query, other.query)
      .append(globalConfig, other.globalConfig)
      .append(userConfig, other.userConfig)
      .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("lang", lang)
      .append("motherTongue", motherTongue)
      .append("query", query)
      .append("globalConfig", globalConfig)
      .append("user", userConfig)
      .build();
  }
}
