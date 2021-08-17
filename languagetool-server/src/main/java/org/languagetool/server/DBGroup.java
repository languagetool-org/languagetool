/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2019 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
 */

package org.languagetool.server;

public class DBGroup {
  public long id;
  public String name;
  public long owner;

  public DBGroup(){}

  public DBGroup(APINewGroup requested, UserInfoEntry user) {
    owner = user.getUserId();
    name = requested.name;
  }
}
