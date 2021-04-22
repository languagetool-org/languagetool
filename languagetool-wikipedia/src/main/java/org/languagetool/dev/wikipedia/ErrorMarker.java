/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import java.util.Objects;

/**
 * String to mark the start and end of an error. Typically markup
 * like {@code <span class="error">} and {@code </span>}.
 */
public class ErrorMarker {

  private final String startMarker;
  private final String endMarker;

  public ErrorMarker(String startMarker, String endMarker) {
    this.startMarker = Objects.requireNonNull(startMarker);
    this.endMarker = Objects.requireNonNull(endMarker);
  }

  public String getStartMarker() {
    return startMarker;
  }

  public String getEndMarker() {
    return endMarker;
  }
}
