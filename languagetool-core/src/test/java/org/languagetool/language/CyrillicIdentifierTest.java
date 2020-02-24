/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.junit.Test;

import static org.junit.Assert.*;

public class CyrillicIdentifierTest {

  @Test
  public void testIsCyrillic() {
    CyrillicIdentifier ident = new CyrillicIdentifier(100);
    assertFalse(ident.isCyrillic(""));
    assertFalse(ident.isCyrillic(" "));
    assertFalse(ident.isCyrillic("hallo"));
    assertFalse(ident.isCyrillic("hallo this is a text"));
    assertFalse(ident.isCyrillic("hallo this is a text стиль"));

    assertTrue(ident.isCyrillic("Грамматика, стиль и орфография LanguageTool проверяет ваше правописание на более чем 20 языках"));
    assertTrue(ident.isCyrillic("проверяет ваше правописание на более чем 20 языках"));
    assertTrue(ident.isCyrillic("Програма перевірки граматики, стилю та орфографії. LanguageTool перевіряє ваші тексти більш ніж 20-ма мовами"));
    assertTrue(ident.isCyrillic("Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах."));  // Belarusian
    assertTrue(ident.isCyrillic("Програма перевірки граматики, стилю та орфографії."));
    assertTrue(ident.isCyrillic("проверяет ваше правописание на более чем 20 языках - Програма перевірки граматики, стилю та орфографії."));
    assertTrue(ident.isCyrillic("Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах. - Програма перевірки граматики, стилю та орфографії."));
    assertTrue(ident.isCyrillic("проверяет ваше правописание на более чем 20 языках - Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах."));
    assertTrue(ident.isCyrillic("проверяет ваше правописание на более чем 20 языках" +
                                "Here's some short English text, but it's short"));
    assertTrue(ident.isCyrillic("Програма перевірки граматики, стилю та орфографії." +
                                "Here's some short English text, but it's short"));
    assertTrue(ident.isCyrillic("проверяет ваше правописание на более чем 20 языках" +
                                "Here's some English text"));
  }

}
