/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

/**
 * @since 4.8
 */
public class PatternRuleBuilderHelper {

  public static PatternToken tokenRegex(String s) {
    return new PatternTokenBuilder().tokenRegex(s).build();
  }

  public static PatternToken posRegex(String s) {
    return new PatternTokenBuilder().posRegex(s).build();
  }

  public static PatternToken csToken(String s) {
    return new PatternTokenBuilder().csToken(s).build();
  }

  public static PatternToken pos(String s) {
    return new PatternTokenBuilder().pos(s).build();
  }

  public static PatternToken token(String s) {
    return new PatternTokenBuilder().token(s).build();
  }

  public static PatternToken regex(String regex) {
    return new PatternTokenBuilder().tokenRegex(regex).build();
  }

  public static PatternToken csRegex(String regex) {
    return new PatternTokenBuilder().csTokenRegex(regex).build();
  }
}
