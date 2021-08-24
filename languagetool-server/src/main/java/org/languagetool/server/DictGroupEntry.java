/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;

/**
 * An item from a user's dictionary, fetched from a database.
 * @since 4.3
 */
public class DictGroupEntry {

  private final String name;
  private final long id;
  @Nullable
  private final Long userGroupId;

  public DictGroupEntry(long id, String name, @Nullable Long userGroupId) {
    this.name = name;
    this.id = id;
    this.userGroupId = userGroupId;
  }

  public String getName() {
    return name;
  }

  public long getId() {
    return id;
  }

  @Nullable
  public Long getUserGroupId() {
    return userGroupId;
  }
}
