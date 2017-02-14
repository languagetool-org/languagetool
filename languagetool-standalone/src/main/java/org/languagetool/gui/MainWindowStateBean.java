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

import java.awt.Rectangle;
import java.io.Serializable;

/**
 * Helper class to store GUI state.
 * <p>
 * WARNING: This class is for internal use only, the only reason this class is
 * public is because it is used with {@link java.beans.XMLEncoder}.
 *
 * @author Panagiotis Minos
 * @since 3.4
 */
public class MainWindowStateBean implements Serializable {

  private static final long serialVersionUID = 1;
  private Integer state;
  private Rectangle bounds;
  private Integer dividerLocation;

  public MainWindowStateBean() {
  }

  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state;
  }

  public Rectangle getBounds() {
    return bounds;
  }

  public void setBounds(Rectangle bounds) {
    this.bounds = bounds;
  }

  public Integer getDividerLocation() {
    return dividerLocation;
  }

  public void setDividerLocation(Integer dividerLocation) {
    this.dividerLocation = dividerLocation;
  }

}
