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


package de.danielnaber.languagetool;

import junit.framework.TestCase;
import java.security.Permission;

/**
 * @author Charlie Collins (Maven Test Example from
 * http://www.screaming-penguin.com/node/7570)
 */

public class AbstractSecurityTestCase extends TestCase {

  public AbstractSecurityTestCase(String name) {
     super(name);
  }

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

  @Override
  protected void setUp() throws Exception {
     super.setUp();
     System.setSecurityManager(new NoExitSecurityManager());
  }

  @Override
  protected void tearDown() throws Exception {
     System.setSecurityManager(null); 
     super.tearDown();
  }
  
  //get rid of JUnit warning for this helper class
  public void testSomething() {
  }

}
