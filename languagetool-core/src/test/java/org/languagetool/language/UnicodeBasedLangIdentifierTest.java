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
import static org.hamcrest.CoreMatchers.is;

public class UnicodeBasedLangIdentifierTest {

  private final UnicodeBasedLangIdentifier ident = new UnicodeBasedLangIdentifier(100);

  @Test
  public void testIsCyrillic() {
    String cyrillic = "[ru, uk, be]";
    String chinese = "[zh]";

    assertThat(codes(""), is("[]"));
    assertThat(codes(" "), is("[]"));
    assertThat(codes("hallo"), is("[]"));
    assertThat(codes("hallo this is a text"), is("[]"));
    assertThat(codes("hallo this is a text стиль"), is("[]"));

    assertThat(codes("Грамматика, стиль и орфография LanguageTool проверяет ваше правописание на более чем 20 языках"), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках"), is(cyrillic));
    assertThat(codes("Програма перевірки граматики, стилю та орфографії. LanguageTool перевіряє ваші тексти більш ніж 20-ма мовами"), is(cyrillic));
    assertThat(codes("Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах."), is(cyrillic));  // Belarusian
    assertThat(codes("Програма перевірки граматики, стилю та орфографії."), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках - Програма перевірки граматики, стилю та орфографії."), is(cyrillic));
    assertThat(codes("Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах. - Програма перевірки граматики, стилю та орфографії."), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках - Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах."), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках" +
                     "Here's some short English text, but it's short"), is(cyrillic));
    assertThat(codes("Програма перевірки граматики, стилю та орфографії." +
                     "Here's some short English text, but it's short"), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках" +
                     "Here's some English text"), is(cyrillic));

    assertThat(codes("您的意思是"), is(chinese));
    assertThat(codes("Linux嚴格來說是單指作業系統的内核"), is(chinese));
    assertThat(codes("通常情况下 but here's more text with Latin characters"), is("[]"));
  }

  private String codes(String s) {
    return ident.getAdditionalLangCodes(s).toString();
  }
}
