/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

/**
 * JNA helper tools.
 * @since 2.2
 */
public final class JnaTools {

  private JnaTools() {
  }

  /**
   * Set a the <tt>jna.nosys</tt> to <tt>true</tt> to avoid the "There is an incompatible
   * JNA native library installed on this system" error from JNA. NOTE: only call this
   * from main() methods, as setting system properties in other contexts (web apps etc.)
   * can lead to strange effects.
   */
  public static void setBugWorkaroundProperty() {
    System.setProperty("jna.nosys", "true");
  }

}
