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
package org.languagetool.gui;

import javax.swing.text.*;
import java.awt.*;

/**
 * Wavy underline painter.
 * @since 3.3
 */
class HighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

  private static final BasicStroke OO_STROKE1 = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{3.0f, 5.0f}, 2);
  private static final BasicStroke OO_STROKE2 = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{1.0f, 3.0f}, 3);
  private static final BasicStroke OO_STROKE3 = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{3.0f, 5.0f}, 6);
  private static final BasicStroke ZIGZAG_STROKE1 = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{1.0f, 1.0f}, 0);

  private final Color underlineColor;
  private final Color backgroundColor;

  HighlightPainter(Color backgroundColor, Color underlineColor) {
    super(backgroundColor);
    this.backgroundColor = backgroundColor;
    this.underlineColor = underlineColor;
  }

  @Override
  public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
    if (backgroundColor != null) {
      super.paintLayer(g, offs0, offs1, bounds, c, view);
    }
    Rectangle rect;

    if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
      if (bounds instanceof Rectangle) {
        rect = (Rectangle) bounds;
      } else {
        rect = bounds.getBounds();
      }
    } else {
      try {
        Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
        rect = shape instanceof Rectangle ? (Rectangle) shape : shape.getBounds();
      } catch (BadLocationException e) {
        rect = null;
      }
    }

    if (rect != null) {
      Color color = underlineColor;

      if (color == null) {
        g.setColor(c.getSelectionColor());
      } else {
        g.setColor(color);
      }

      rect.width = Math.max(rect.width, 1);

      int descent = c.getFontMetrics(c.getFont()).getDescent();

      if (descent > 3) {
        drawCurvedLine(g, rect);
      } else if (descent > 2) {
        drawCurvedLine(g, rect);
      } else {
        drawLine(g, rect);
      }
    }

    return rect;
  }

  private void drawCurvedLine(Graphics g, Rectangle rect) {
    int x1 = rect.x;
    int x2 = rect.x + rect.width;
    int y = rect.y + rect.height;
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setStroke(OO_STROKE1);
    g2.drawLine(x1, y - 1, x2, y - 1);
    g2.setStroke(OO_STROKE2);
    g2.drawLine(x1, y - 2, x2, y - 2);
    g2.setStroke(OO_STROKE3);
    g2.drawLine(x1, y - 3, x2, y - 3);
  }

  private void drawLine(Graphics g, Rectangle rect) {
    int x1 = rect.x;
    int x2 = rect.x + rect.width;
    int y = rect.y + rect.height;
    Graphics2D g2 = (Graphics2D) g;
    g2.setStroke(ZIGZAG_STROKE1);
    g2.drawLine(x1, y - 1, x2, y - 1);
  }
  
}
