/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * A resource bundle that uses its fallback resource bundle if the
 * value from the original bundle is null or empty.
 */
public class ResourceBundleWithFallback extends ResourceBundle {

  private final ResourceBundle bundle;
  private final ResourceBundle fallbackBundle;

  public ResourceBundleWithFallback(ResourceBundle bundle, ResourceBundle fallbackBundle) {
    this.bundle = bundle;
    this.fallbackBundle = fallbackBundle;
  }

  @Override
  public Object handleGetObject(String key) {
    String s = bundle.getString(key);
    if (s.trim().isEmpty()) {
      return fallbackBundle.getString(key);
    }
    return s;
  }

  @Override
  public Enumeration<String> getKeys() {
    return bundle.getKeys();
  }

}
