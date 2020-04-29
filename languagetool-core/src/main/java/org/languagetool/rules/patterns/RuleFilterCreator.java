/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import java.lang.reflect.Constructor;

/**
 * Create a {@link RuleFilter}.
 * @since 2.7 (public since 3.2)
 */
public class RuleFilterCreator {

  /**
   * @param className fully qualified class Name of a class implementing {@link RuleFilter}
   */
  public RuleFilter getFilter(String className) {
    try {
      Class<?> aClass = Class.forName(className);
      Constructor<?>[] constructors = aClass.getConstructors();
      if (constructors.length != 1) {
        throw new RuntimeException("Constructor of filter class '"
                + className + "' must have exactly one constructor, but it has " + constructors.length);
      }
      Constructor<?> constructor = constructors[0];
      try {
        if (constructor.getParameterTypes().length != 0) {
          throw new RuntimeException("Constructor of filter class '" + className + "' must not have arguments: " + constructor);
        }
        Object filter = constructor.newInstance();
        if (filter instanceof RuleFilter) {
          return (RuleFilter) filter;
        } else {
          throw new RuntimeException("Filter class '" + className + "' must implement interface " + RuleFilter.class.getSimpleName());
        }
      } catch (Exception e) {
        throw new RuntimeException("Could not create filter class using constructor " + constructor, e);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not find filter class: '"
              + className + "' - make sure to use a fully qualified class name like 'org.languagetool.rules.MyFilter'");
    }
  }
 
}
