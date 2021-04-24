/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import java.util.Objects;

/**
 * A person that contributed rules or code to LanguageTool.
 */
public final class Contributor {

  private final String name;
  private final String url;

  /**
   * @param name full name
   * @param url URL to homepage or similar (optional)
   */
  Contributor(String name, String url) {
    this.name = Objects.requireNonNull(name, "name cannot be null");
    this.url = url;
  }

  Contributor(String name) {
    this(name, null);
  }
  
  public String getName() {
    return name;
  }
  
  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return name;
  }

}
