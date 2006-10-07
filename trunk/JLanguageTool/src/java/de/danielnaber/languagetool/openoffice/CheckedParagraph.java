/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.openoffice;

import java.util.List;

import de.danielnaber.languagetool.rules.RuleMatch;

class CheckedParagraph {

  private int paraNumber;
  private List<RuleMatch> ruleMatches;
  
  CheckedParagraph(int paraNumber, List<RuleMatch> ruleMatches) {
    this.paraNumber = paraNumber;
    this.ruleMatches = ruleMatches;
  }
  
  List<RuleMatch> getRuleMatches() {
    return ruleMatches;
  }
  
  int getParagraphNumber() {
    return paraNumber;
  }
  
}
