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
package de.danielnaber.languagetool.openoffice;

import java.util.Arrays;
import java.util.ResourceBundle;

import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.uno.UnoRuntime;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tools.StringTools;

public class OOoAboutDialog {

  private ResourceBundle messages;
  private XWindowPeer winPeer;

  public OOoAboutDialog(final ResourceBundle messages,
      final XWindowPeer parentWindowPeer) {
    this.messages = messages;
    winPeer = parentWindowPeer;
  }

  public void show() {
    final StringBuilder maintainersInfo = new StringBuilder();
    for (final Language lang : Language.LANGUAGES) {
      if (lang != Language.DEMO) {
        if (lang.getMaintainers() != null) {
          final String m = Arrays.toString(lang.getMaintainers());
          maintainersInfo.append(messages.getString(lang.getShortName()));
          maintainersInfo.append(" – ");
          maintainersInfo.append(m);
          maintainersInfo.append("\n");
        }
      }
    }
    final String aboutText = StringTools.getLabel(messages
        .getString("guiMenuAbout"));

    final XMessageBoxFactory messageBoxFactory = (XMessageBoxFactory) UnoRuntime
        .queryInterface(XMessageBoxFactory.class, winPeer.getToolkit());

    final Rectangle messageBoxRectangle = new Rectangle();

    final XMessageBox box = messageBoxFactory
        .createMessageBox(
            winPeer,
            messageBoxRectangle,
            "infobox",
            0,
            aboutText,
            "LanguageTool "
                + JLanguageTool.VERSION
                + "\n"
                + "Copyright (C) 2005-2009 Daniel Naber\n"
                + "This software is licensed under the GNU Lesser General Public License.\n"
                + "LanguageTool Homepage: http://www.danielnaber.de/languagetool\n\n"
                + "Maintainers of the language modules:\n"
                + maintainersInfo.toString());
    box.execute();
  }
}