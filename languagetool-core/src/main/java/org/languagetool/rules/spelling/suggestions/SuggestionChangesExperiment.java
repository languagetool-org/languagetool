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

package org.languagetool.rules.spelling.suggestions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
class SuggestionChangesTestConfig {

  public String ngramLocation;
  public String rule;
  public String language;
  public String logDir;

  public List<SuggestionChangesExperimentRuns> experiments;
  public List<SuggestionChangesDataset> datasets;


  @Override
  public String toString() {
    return "SuggestionChangesTestConfig{" +
      "ngramLocation='" + ngramLocation + '\'' +
      ", rule='" + rule + '\'' +
      ", language='" + language + '\'' +
      ", logDir='" + logDir + '\'' +
      ", experiments=" + experiments +
      ", datasets=" + datasets +
      '}';
  }
}

@SuppressWarnings("ALL")
class SuggestionChangesDataset {
  public String name;
  public String path;
  public String type; // dump | artificial; different columns in CSV file
  public float sampleRate;
  public boolean enforceCorrect; // no spelling errors in correction, otherwise entries are skipped
  public boolean enforceAcceptLanguage; // only for type==dump: first field in acceptLanguage must be equal to the text language

  @Override
  public String toString() {
    return "SuggestionChangesDataset{" +
      "name='" + name + '\'' +
      ", path='" + path + '\'' +
      ", type='" + type + '\'' +
      ", sampleRate=" + sampleRate +
      ", enforceCorrect=" + enforceCorrect +
      ", enforceAcceptLanguage=" + enforceAcceptLanguage +
      '}';
  }
}

@SuppressWarnings("ALL")
class SuggestionChangesExperimentRuns {
  public String name;
  public Map<String, List<Object>> parameters;

  @Override
  public String toString() {
    return "SuggestionChangesExperimentRuns{" +
      "name='" + name + '\'' +
      ", parameters=" + parameters +
      '}';
  }
}

@SuppressWarnings("ALL")
public class SuggestionChangesExperiment {
  public String name;
  public Map<String, Object> parameters;

  public SuggestionChangesExperiment(String name, Map<String, Object> parameters) {
    this.name = name;
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("name", name)
      .append("parameters", parameters)
      .build();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(37, 53)
      .append(name)
      .append(parameters)
      .build();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    SuggestionChangesExperiment other = (SuggestionChangesExperiment) obj;
    return new EqualsBuilder()
      .append(name, other.name)
      .append(parameters, other.parameters)
      .build();
  }
}
