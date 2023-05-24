/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Sohaib Afifi, Taha Zerrouki
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

import java.util.HashMap;
import java.util.Map;

public class ArabicConstantsMaps {
  protected static final Map<String, String> isolatedToAttachedPronoun = new HashMap<>();

  protected ArabicConstantsMaps() {
    // restrict instantiation
    // Isolated pronoun to attached
    isolatedToAttachedPronoun.put("أنا", "ني");
    isolatedToAttachedPronoun.put("نحن", "نا");
    isolatedToAttachedPronoun.put("هو", "ه");
    isolatedToAttachedPronoun.put("هي", "ها");
    isolatedToAttachedPronoun.put("هم", "هم");
    isolatedToAttachedPronoun.put("هن", "هن");
    isolatedToAttachedPronoun.put("أنتما", "كما");
    isolatedToAttachedPronoun.put("أنتم", "كم");
    isolatedToAttachedPronoun.put("أنتن", "كن");
  }
}