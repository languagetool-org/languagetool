/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Experimental;

/**
 * Helper to build {@link PatternToken}s.
 * @since 3.1
 */
public class PatternTokenBuilder {

  private PatternToken token;

  /**
   * Add a case-insensitive token. 
   */
  public PatternTokenBuilder token(String token) {
    this.token = new PatternToken(token, false, false, false);
    return this;
  }

  /**
   * Add a case-sensitive token. 
   * @since 3.3 
   */
  public PatternTokenBuilder csToken(String token) {
    this.token = new PatternToken(token, true, false, false);
    return this;
  }

  public PatternTokenBuilder tokenRegex(String token) {
    this.token = new PatternToken(token, false, true, false);
    return this;
  }

  public PatternTokenBuilder pos(String posTag) {
    return pos(posTag, false);
  }

  public PatternTokenBuilder posRegex(String posTag) {
    return pos(posTag, true);
  }

  private PatternTokenBuilder pos(String posTag, boolean regex) {
    token = new PatternToken(null, false, false, false);
    token.setPosToken(new PatternToken.PosToken(posTag, regex, false));
    return this;
  }

  /** @since 3.3 */
  public PatternTokenBuilder negate() {
    token.setNegation(true);
    return this;
  }
  
  public PatternToken build() {
    return token;
  }
}
