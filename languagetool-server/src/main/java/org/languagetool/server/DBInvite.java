/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
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
