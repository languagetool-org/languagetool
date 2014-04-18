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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

import net.boplicity.xmleditor.XmlEditorKit;


/**
 * JTextPane implementation that can handle xml text. The IndentKeyListener
 * implements smart indenting.
 *  
 * @author kees
 * @date 27-jan-2006
 *
 */
public class XmlTextPane extends JTextPane {

    private static final long serialVersionUID = 6270183148379328084L;
    private Logger logger = Logger.getLogger(getClass().getName());

    public XmlTextPane() {
        
        // Set editor kit
        this.setEditorKitForContentType("text/xml", new XmlEditorKit());
        this.setContentType("text/xml");
        
        addKeyListener(new IndentKeyListener());
    }
    
	private class IndentKeyListener implements KeyListener {

		private boolean enterFlag;
		private final Character NEW_LINE = '\n';

		public void keyPressed(KeyEvent event) {
			enterFlag = false;
			if ((event.getKeyCode() == KeyEvent.VK_ENTER)
					&& (event.getModifiers() == 0)) {
				if (getSelectionStart() == getSelectionEnd()) {
					enterFlag = true;
					event.consume();
				}
			}
		}

		public void keyReleased(KeyEvent event) {
			if ((event.getKeyCode() == KeyEvent.VK_ENTER)
					&& (event.getModifiers() == 0)) {
				if (enterFlag) {
					event.consume();

					int start, end;
					String text = getText();

					int caretPosition = getCaretPosition();
					try {
						if (text.charAt(caretPosition) == NEW_LINE) {
							caretPosition--;
						}
					} catch (IndexOutOfBoundsException e) {
					}

					start = text.lastIndexOf(NEW_LINE, caretPosition) + 1;
					end = start;
					try {
						if (text.charAt(start) != NEW_LINE) {
							while ((end < text.length())
									&& (Character
											.isWhitespace(text.charAt(end)))
									&& (text.charAt(end) != NEW_LINE)) {
								end++;
							}
							if (end > start) {
								getDocument()
										.insertString(
												getCaretPosition(),
												NEW_LINE
														+ text.substring(start,
																end), null);
							} else {
								getDocument().insertString(getCaretPosition(),
										NEW_LINE.toString(), null);
							}
						} else {
							getDocument().insertString(getCaretPosition(),
									NEW_LINE.toString(), null);
						}
					} catch (IndexOutOfBoundsException e) {
						try {
							getDocument().insertString(getCaretPosition(),
									NEW_LINE.toString(), null);
						} catch (BadLocationException e1) {
							logger.log(Level.WARNING, e1.toString());
						}
					} catch (BadLocationException e) {
						logger.log(Level.WARNING, e.toString());
					}
				}
			}
		}

		public void keyTyped(KeyEvent e) {
		}
	}
    
}
