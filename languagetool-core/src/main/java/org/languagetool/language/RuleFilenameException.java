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

import java.io.File;

/**
 * Thrown if external rule filename doesn't match the required format.
 */
public class RuleFilenameException extends RuntimeException {

  private static final long serialVersionUID = 6642163394764392897L;

  public RuleFilenameException(File file) {
    super("Rule file must be named rules-<xx>-<lang>.xml (<xx> = language code, " +
        "<lang> = language name),\n" +
        "for example: rules-en-English.xml\n" +
        "Current name: " + file.getName());
  }
  
}
