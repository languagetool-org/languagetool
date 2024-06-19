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
package org.languagetool.rules;

/**
 * Class to configure rule option by option panel
 * @author Fred Kruse
 * @since 6.5
 */
public class RuleOption {
  public final static String DEFAULT_VALUE = "defaultValue";
  public final static String DEFAULT_TYPE = "defaultType";
  public final static String MIN_CONF_VALUE = "minConfigurableValue";
  public final static String MAX_CONF_VALUE = "maxConfigurableValue";
  public final static String CONF_TEXT = "configureText";
  private final Object defaultValue;
  private final String configureText;
  private Object minConfigurableValue;
  private Object maxConfigurableValue;
  
  public RuleOption(Object defaultValue, String configureText, Object minConfigurableValue, Object maxConfigurableValue) {
    this.defaultValue = defaultValue;
    this.configureText = configureText;
    if (minConfigurableValue != null && maxConfigurableValue != null && 
        (defaultValue instanceof Integer || defaultValue instanceof Character || 
            defaultValue instanceof Float || defaultValue instanceof Double)) {
      this.minConfigurableValue = minConfigurableValue;
      this.maxConfigurableValue = maxConfigurableValue;
    } else {
      this.minConfigurableValue = 0;
      this.maxConfigurableValue = 100;
    }
  }
  
  public RuleOption(Object defaultValue, String configureText) {
    this(defaultValue, configureText, 0 , 100);
  }

  /**
   * Get a default Value by option panel
   */
  public Object getDefaultValue() {
    return defaultValue;
  }

  /**
   * Get the Configuration Text by option panel
   */
  public String getConfigureText() {
    return configureText;
  }
  /**
   * Get the minimum of a configurable value
   */
  public Object getMinConfigurableValue() {
    return minConfigurableValue;
  }

  /**
   * Get the maximum of a configurable value
   */
  public Object getMaxConfigurableValue() {
    return maxConfigurableValue;
  }

  /**
   * Gives back a special String representation of an Object for save or communication
   * add a character to characterize the type of object before its String value
   * (i for Integer, b for Boolean, etc.
   * Can be decoded by StringToObject
   */
  public static String objectToString(Object o) {
    char c;
    if (o instanceof Integer) {
      c = 'i';
    } else if (o instanceof Character) {
      c = 'c';
    } else if (o instanceof Boolean) {
      c = 'b';
    } else if (o instanceof Float) {
      c = 'f';
    } else if (o instanceof Double) {
      c = 'd';
    } else {
      c = 's';
    }
    return c + o.toString();
  }

  /**
   * Gives back a special String representation of an array of Objects for save or communication
   * use ObjectToString for encoding the single objects
   * separate the objects by ';'
   * Can be decoded by StringToObjects
   */
  public static String objectsToString(Object[] o) {
    String s = "";
    for (int i = 0; i < o.length; i++) {
      if (i > 0) {
        s += ";";
      }
      s += objectToString(o[i]);
    }
    return s;
  }

  /**
   * decodes an String to an object encoded by ObjectToString
   * Note: if there is no special encoding character at the beginning of the string
   *       an integer is assumed for compatibility with older versions of LT
   */
  public static Object stringToObject(String s) {
    Object o;
    char c = s.charAt(0);
    String str = s.substring(1);
    if (c == 's') {
      o = str;
    } else if (c == 'b') {
      o = Boolean.parseBoolean(str);
    } else if (c == 'f') {
      o = Float.parseFloat(str);
    } else if (c == 'd') {
      o = Double.parseDouble(str);
    } else if (c == 'c') {
      o = str.charAt(0);
    } else if (c == 'i') {
      o = Integer.parseInt(str);
    } else {  //  compatible to old version 
      o = Integer.parseInt(s);
    }
    return o;
  }

  /**
   * decodes an String to an array of object encoded by ObjectsToString
   */
  public static Object[] stringToObjects(String s) {
    String[] strs = s.split(";");
    Object[] o = new Object[strs.length];
    for (int i = 0; i < strs.length; i++) {
      String str = strs[i].trim();
      o[i] = stringToObject(str);
    }
    return o;
  }



  
  
}
