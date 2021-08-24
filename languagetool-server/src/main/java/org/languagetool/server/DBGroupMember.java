/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Fabian Richter
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
