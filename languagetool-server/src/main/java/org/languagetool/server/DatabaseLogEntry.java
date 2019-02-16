/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.server;

import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Map;

/**
 * @since 4.3
 */
public abstract class DatabaseLogEntry {

  /**
   * @return parameters for mybatis SQL statement
   */
  public abstract Map<Object, Object> getMapping();

  /**
   * @return identifier for mybatis SQL statement
   */
  public abstract String getMappingIdentifier();

  /**
   * for interdependent log entries, i.e. with foreign keys referencing other entries
   * @return null if no followup needed, else log entry that needs to be inserted directly afterwards
   * e.g. to use LAST_INSERT_ID from mysql
   */
  @SuppressWarnings("UnusedReturnValue")
  @Nullable
  public DatabaseLogEntry followup() {
    // default: nothing to be done
    return null;
  }

  public void print(PrintStream out) {
    out.println(getMappingIdentifier());
    out.print(getMapping());
    out.println();
  }

  public void print() {
    this.print(System.out);
  }
}
