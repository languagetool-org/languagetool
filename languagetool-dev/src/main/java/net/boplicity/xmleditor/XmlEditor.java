 /*
  * Copyright 2006-2008 Kees de Kooter
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package net.boplicity.xmleditor;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Frame displaying the XML textpane.
 *
 * @author kees
 *
 */
public class XmlEditor extends JFrame {

    private static final long serialVersionUID = 2623631186455160679L;

    public static void main(String[] args) {
        XmlEditor xmlEditor = new XmlEditor();
        xmlEditor.setVisible(true);
    }

    public XmlEditor() {

        super("XML Text Editor Demo");
        setSize(800, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());

        XmlTextPane xmlTextPane = new XmlTextPane();
        panel.add(xmlTextPane);

        add(panel);
    }
}
