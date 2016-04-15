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
package org.languagetool.openoffice;

import com.sun.star.linguistic2.SingleProofreadingError;

import java.util.Comparator;

/**
 * A simple comparator for sorting errors by their position.
 */
class ErrorPositionComparator implements Comparator<SingleProofreadingError> {

  @Override
  public int compare(SingleProofreadingError match1, SingleProofreadingError match2) {
    if (match1.aSuggestions.length == 0 && match2.aSuggestions.length > 0) {
      return 1;
    }
    if (match2.aSuggestions.length == 0 && match1.aSuggestions.length > 0) {
      return -1;
    }
    int error1pos = match1.nErrorStart;
    int error2pos = match2.nErrorStart;
    if (error1pos > error2pos) {
      return 1;
    } else if (error1pos < error2pos) {
      return -1;
    } else {
      if (match1.aSuggestions.length != 0 && match2.aSuggestions.length != 0
          && match1.aSuggestions.length != match2.aSuggestions.length) {
        return Integer.compare(match1.aSuggestions.length, match2.aSuggestions.length);
      }
    }
    return match1.aRuleIdentifier.compareTo(match2.aRuleIdentifier);
  }

}
