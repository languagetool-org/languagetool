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

package org.languagetool;

import java.util.ArrayList;
import java.util.List;

/**
 * Store integer values to configuration of single rules in a List
 * and returns the values by rule-ID
 * @since 4.2
 * @author Fred Kruse
 */
public class ConfigValues {
  private class ValuePair {
    String ruleID;
    int value;
    
    ValuePair(String ID, int v) {
      ruleID = ID;
      value = v;
    }
  }
  private List<ValuePair> values;
  
  /**
   * create a List with 0 entries
   * @since 4.2
   */
  public ConfigValues() {
    values = new ArrayList<ValuePair>();
  }
  
  /**
   * add a pair of rule-ID and value
   * @since 4.2
   */
  public void addValue(String ruleID, int value) {
    for (ValuePair v : values) {
      if(ruleID.equals(v.ruleID)) {
        v.value = value;
        return;
      }
    }
    values.add( new ValuePair(ruleID, value) ); 
  }
  
  /**
   * Get all configuration values as String
   * @since 4.2
   */
  public String getAsString() {
    String txt = "";
    for (ValuePair v : values) {
      txt += v.ruleID + ": " + v.value +"\n";
    }
    return txt; 
  }
  
  /**
   * Get a list of ValuePairs
   * @since 4.2
   */
  public List<ValuePair> getValuePairs() {
    return values; 
  }
  
  /**
   * Inserts a list of ValuePairs
   * note: overrides the existing list
   * @since 4.2
   */
  public void insertList(List<ValuePair> v) {
    values = new ArrayList<ValuePair>();
    values.addAll( v ); 
  }
  
  /**
   * Inserts the List of ConfigValues
   * note: overrides the existing list
   * @since 4.2
   */
  public void insertList(ConfigValues v) {
    values = new ArrayList<ValuePair>();
    values.addAll( v.getValuePairs() ); 
  }
  
  /**
   * returns the value for a rule-ID
   * returns -1, if the rule-ID wasn't found
   * @since 4.2
   */
  public int getValueByID (String ID) {
    for (ValuePair v : values) {
      if(ID.startsWith(v.ruleID)) {
        return v.value;
      }
    }
    return -1;
  }

}