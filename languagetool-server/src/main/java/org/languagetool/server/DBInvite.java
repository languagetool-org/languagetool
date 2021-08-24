/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Fabian Richter
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

import java.util.UUID;

public class DBInvite {
  public Long id;
  public Long user_id;
  public Long group_id;
  public String token;

  public DBInvite() {}

  public DBInvite(UserInfoEntry newUser, DBGroup group) {
    this(newUser.getUserId(), group);
  }

  @SuppressWarnings("DuplicateExpressions")
  public DBInvite(long userId, DBGroup group) {
    user_id = userId;
    group_id = group.id;
    String randomToken1 = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    String randomToken2 = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    token = randomToken1 + randomToken2;
  }
}
