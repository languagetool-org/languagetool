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

package org.languagetool.tagging.uk;

public enum IPOSTag {
  noun("noun"),
  adj("adj"),
  verb("verb"),
  adv("adv"),
  part("part"),
  intj("intj"),
  numr("numr"),
  number("number"),
  date("date"),
  time("time"),
  advp("advp"),
  prep("prep"),
  predic("predic"),
  insert("insert"),
  abbr("abbr"),
  bad("bad"),
  unknown("unknown");

  private final String text;

  private IPOSTag(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public boolean match(String posTagPrefix) {
    return posTagPrefix != null && posTagPrefix.startsWith(this.name());
  }
  
  public static boolean isNum(String posTag) {
    return numr.match(posTag) || number.match(posTag);
  }

  public static boolean contains(String posTag, String postagMatch) {
    return posTag != null && posTag.contains(postagMatch);
  }

  /**
   * @since 2.9
   */
  public static boolean startsWith(String posTagPrefix, IPOSTag... posTags) {
    if( posTagPrefix == null )
      return false;
    
    for (IPOSTag posTag: posTags) {
      if( posTagPrefix.startsWith(posTag.getText()) )
        return true;
    }
    
    return false;
  }

}
