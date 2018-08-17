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

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;

/**
 * A class that listens for window resize/move events and saves its bounds.
 *
 * @author Panagiotis Minos
 * @since 3.4
 */
class ResizeComponentListener extends ComponentAdapter {

  private static final String BOUNDS_PROPERTY_NAME = "frame.bounds";

  static void attachToWindow(JFrame frame) {
    frame.addComponentListener(new ResizeComponentListener(frame));
  }

  static void setBoundsProperty(JFrame frame, Rectangle bounds) {
    frame.getRootPane().putClientProperty(BOUNDS_PROPERTY_NAME, bounds);
  }

  static Rectangle getBoundsProperty(JFrame frame) {
    return (Rectangle) frame.getRootPane().getClientProperty(BOUNDS_PROPERTY_NAME);
  }

  private final JFrame frame;

  private ResizeComponentListener(JFrame frame) {
    this.frame = frame;
  }

  @Override
  public void componentResized(ComponentEvent e) {
    saveBounds();
  }

  @Override
  public void componentMoved(ComponentEvent e) {
    saveBounds();
  }

  private void saveBounds() {
    if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
      Rectangle bounds = frame.getBounds();
      frame.getRootPane().putClientProperty(BOUNDS_PROPERTY_NAME, bounds);
    }
  }

}
