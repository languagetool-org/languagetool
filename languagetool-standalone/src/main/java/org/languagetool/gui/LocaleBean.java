/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.gui;

import java.io.Serializable;
import java.util.Locale;

/**
 * Helper class to store GUI Language.
 * <p>
 * WARNING: This class is for internal use only, the only reason this class is
 * public is because it is used with {@link java.beans.XMLEncoder}.
 *
 * @author Panagiotis Minos
 * @since 3.4
 */
public class LocaleBean implements Serializable {

  private static final long serialVersionUID = 1;
  private String country;
  private String language;
  private String variant;

  public LocaleBean() {
  }

  public LocaleBean(Locale l) {
    country = l.getCountry();
    language = l.getLanguage();
    variant = l.getVariant();
  }

  public String getCountry() {
    return country;
  }

  public String getLanguage() {
    return language;
  }

  public String getVariant() {
    return variant;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setVariant(String variant) {
    this.variant = variant;
  }

  Locale asLocale() {
    if (language != null) {
      if (country != null) {
        if (variant != null) {
          return new Locale(language, country, variant);
        }
        return new Locale(language, country);
      }
      return new Locale(language);
    }
    return Locale.getDefault();
  }

}
