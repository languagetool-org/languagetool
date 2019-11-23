/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FilePermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class PatternRuleLoaderPermissionTest {

  private static final SecurityManager secManager = System.getSecurityManager();

  @BeforeClass
  public static void startup() throws Exception {
    Policy.setPolicy(new MyPolicy());
    System.setSecurityManager(new SecurityManager());
  }

  @Ignore("doesn't work with Gradle, see http://stackoverflow.com/questions/32584997/;" +
    " also caused seemingly random exceptions after PR #1443 was merged")
  @Test
  public void testPermissionManager() throws Exception {
    try {
      PatternRuleLoader loader = new PatternRuleLoader();
      // do not crash if Authenticator.setDefault() is forbidden,
      // see https://github.com/languagetool-org/languagetool/issues/255
      loader.getRules(new ByteArrayInputStream("<rules lang='xx'></rules>".getBytes("utf-8")), "fakeName");
    } finally {
      System.setSecurityManager(null);
    }
  }

  @AfterClass
  public static void shutdown(){
    System.setSecurityManager(secManager);
  }
  
  static class MyPolicy extends Policy {
    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
      PermissionCollection perms = new MyPermissionCollection();
      perms.add(new RuntimePermission("setIO"));
      perms.add(new RuntimePermission("setSecurityManager"));
      perms.add(new FilePermission("<<ALL FILES>>", "read"));
      return perms;
    }
  }

  static class MyPermissionCollection extends PermissionCollection {
    private final List<Permission> perms = new ArrayList<>();
    @Override
    public void add(Permission p) {
      perms.add(p);
    }
    @Override
    public boolean implies(Permission p) {
      for (Permission perm : perms) {
        if (perm.implies(p)) {
          return true;
        }
      }
      return false;
    }
    @Override
    public Enumeration<Permission> elements() {
      return Collections.enumeration(perms);
    }
    @Override
    public boolean isReadOnly() {
      return false;
    }
  }

}
