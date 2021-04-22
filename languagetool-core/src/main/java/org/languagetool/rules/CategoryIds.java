/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

/**
 * Category ids.
 * @since 3.3
 */
public final class CategoryIds {

  public static final CategoryId TYPOGRAPHY = new CategoryId("TYPOGRAPHY");
  /** Rules about detecting uppercase words where lowercase is required and vice versa. */
  public static final CategoryId CASING = new CategoryId("CASING");
  public static final CategoryId GRAMMAR = new CategoryId("GRAMMAR");
  public static final CategoryId TYPOS = new CategoryId("TYPOS");
  public static final CategoryId PUNCTUATION = new CategoryId("PUNCTUATION");
  /** Words that are easily confused, like 'there' and 'their' in English. */
  public static final CategoryId CONFUSED_WORDS = new CategoryId("CONFUSED_WORDS");
  public static final CategoryId REDUNDANCY = new CategoryId("REDUNDANCY");
  public static final CategoryId STYLE = new CategoryId("STYLE");
  public static final CategoryId GENDER_NEUTRALITY = new CategoryId("GENDER_NEUTRALITY");
  public static final CategoryId SEMANTICS = new CategoryId("SEMANTICS");
  /** Colloquial style. */
  public static final CategoryId COLLOQUIALISMS = new CategoryId("COLLOQUIALISMS");
  /** Rules that only make sense when editing Wikipedia (typically turned off by default in LanguageTool). */
  public static final CategoryId WIKIPEDIA = new CategoryId("WIKIPEDIA");
  /** A words or expressions that are badly formed according to traditional philological rules, 
   * for example a word formed from elements of different languages */
  public static final CategoryId BARBARISM = new CategoryId("BARBARISM");
  /** Miscellaneous rules that don't fit elsewhere. */
  public static final CategoryId MISC = new CategoryId("MISC");

  private CategoryIds() {
  }
}
