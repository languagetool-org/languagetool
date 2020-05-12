/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.commandline;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Permission;

/**
 * @author Charlie Collins (Maven Test Example from
 * http://www.screaming-penguin.com/node/7570)
 */
public class AbstractSecurityTestCase {

  protected static class ExitException extends SecurityException {
    private static final long serialVersionUID = 1L;
    public final int status;
    public ExitException(int status) {
      super("There is no escape!");
      this.status = status;
    }
  }

  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(@SuppressWarnings("unused") Permission perm) {
      // allow anything.
    }

    @Override
    @SuppressWarnings("unused")
    public void checkPermission(Permission perm, Object context) {
      // allow anything.
    }

    @Override
    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @AfterEach
  public void tearDown() throws Exception {
    System.setSecurityManager(null);
  }

  //get rid of JUnit warning for this helper class
  @Test
  public void testSomething() {
  }

}
