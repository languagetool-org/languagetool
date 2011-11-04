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
package de.danielnaber.languagetool.language;

/**
 * A person that contributed rules or code to LanguageTool.
 * 
 * @author Daniel Naber
 */
public class Contributor {

  private final String name;
  private String remark;
  private String url;

  Contributor(String name) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  @Override
  public final String toString() {
    return getName();
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(final String remark) {
    this.remark = remark;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

}
