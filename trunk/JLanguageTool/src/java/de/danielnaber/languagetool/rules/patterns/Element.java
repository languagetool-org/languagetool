/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules.patterns;

import de.danielnaber.languagetool.AnalyzedToken;

/**
 * A part of a pattern.
 * 
 * @author Daniel Naber
 */
public abstract class Element {

  String[] tokens;
  boolean negation = false;
  int skip = 0;

  final boolean match(AnalyzedToken token) {
    if (negation)
      return !matchToken(token);
    else
      return matchToken(token);
  }

  abstract boolean matchToken(AnalyzedToken token);

  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < tokens.length; i++) {
      sb.append(tokens[i]);
      if (i < tokens.length-1)
        sb.append("|");
    }
    return sb.toString();
  }
  
  
  public int getSkipNext(){
	  return skip;
  }
  
  public void setSkipNext(int i) {
	  skip = i;
  }
  
  String[] getTokens() {
    return tokens;
  }
  
  /**
   * Negates the meaning of match(). 
   */
  void setNegation(boolean negation) {
    this.negation = negation;
  }
  
}
