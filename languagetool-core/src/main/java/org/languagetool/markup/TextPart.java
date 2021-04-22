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
package org.languagetool.markup;

import java.util.Objects;

/**
 * A part of a text with markup, either plain text (to be checked by LanguageTool),
 * or markup (to be ignored by LanguageTool).
 * @since 2.3
 */
public class TextPart {

  public enum Type {TEXT, MARKUP, FAKE_CONTENT}

  private final String part;
  private final Type typ;

  TextPart(String part, Type typ) {
    this.part = Objects.requireNonNull(part);
    this.typ = Objects.requireNonNull(typ);
  }

  public String getPart() {
    return part;
  }

  public Type getType() {
    return typ;
  }

  @Override
  public String toString() {
    return part;
  }
}
