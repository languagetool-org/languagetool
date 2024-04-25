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
 * @since 4.2
 */
public class RuleOption {
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



  
  
}
