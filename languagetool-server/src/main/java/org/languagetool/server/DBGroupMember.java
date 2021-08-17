/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2019 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
 */

package org.languagetool.server;

import java.util.List;
import java.util.stream.Collectors;

public class DBGroupMember {

  public DBGroupMember(Long id, Long groupId, Long userId, String role) {
    this.id = id;
    this.groupId = groupId;
    this.userId = userId;
    this.role = role;
  }
  public DBGroupMember(Long id, Long groupId, Long userId, GroupRoles role) {
    this(id, groupId, userId, role.name());
  }

  public DBGroupMember(Long id, Long groupId, Long userId, List<GroupRoles> roles) {
    this(id, groupId, userId, roles.stream().map(GroupRoles::name).collect(Collectors.joining(GroupRoles.SEPARATOR)));
  }

  public Long id;
  public Long groupId;
  public Long userId;
  public String role;
}
