/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2019 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
 */

package org.languagetool.server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

// request body for POST /groups
public class APINewGroup {
  public APINewGroup() {}

  public APINewGroup(String name) {
    this.name = name;
  }

  public String name;

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    APINewGroup newGroup = (APINewGroup) o;
    return new EqualsBuilder().append(name, newGroup.name).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(13, 41).append(name).toHashCode();
  }
}
