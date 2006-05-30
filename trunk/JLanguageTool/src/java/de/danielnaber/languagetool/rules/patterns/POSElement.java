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
import java.util.regex.*;

/**
 * A part of a pattern that matches a part-of-speech tag.
 * Typically built from patterns like <code>JJ</code> or <code>NNS</code>.
 * It also supports exceptions built form patterns like <code>CD^foo|bar</code>:
 * this will match any word tagged as <code>CD</code>, except the word
 * <code>foo</code> or <code>bar</code>.
 *
 * @author Daniel Naber
 */
public class POSElement extends Element {

  private String[] exceptions = null;
  private boolean caseSensitive = false;
  
  POSElement(String[] tokens, String[] exceptions) {
    this(tokens, false, exceptions);
  }

  POSElement(String[] tokens, boolean caseSensitive, String[] exceptions) {
    this.tokens = tokens;
    this.exceptions = exceptions;
    this.caseSensitive = caseSensitive;
  }

  boolean matchToken(AnalyzedToken token) {
    boolean match = false;
    for (int i = 0; i < tokens.length; i++) {
      // if (tokens[i].equals(token.getPOSTag())) {
      // changed to match regexps
      if (token.getPOSTag() != null)
        if (Pattern.matches(tokens[i], token.getPOSTag())) {
          match = true;
          break;
        }
    }
    if (exceptions != null) {
      for (int i = 0; i < exceptions.length; i++) {
        boolean tmpMatch;
        if (caseSensitive)
          tmpMatch = exceptions[i].equals(token.getToken());
        else
          tmpMatch = exceptions[i].equalsIgnoreCase(token.getToken());
        if (tmpMatch) {
          return false;
        }
      }
    }
    return match;
  }

}
