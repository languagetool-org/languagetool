/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.markup;

import org.junit.Test;
import org.languagetool.tools.ContextTools;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class AnnotatedTextTest {
  
  @Test
  public void test() {
    AnnotatedText text = new AnnotatedTextBuilder().
            addGlobalMetaData("foo", "val").
            addGlobalMetaData(AnnotatedText.MetaDataKey.EmailToAddress, "Foo Bar <foo@foobar.org>").
            // text:
            // hello <b>user!</b>
            addText("hello ").
            addMarkup("<b>").
            addText("user!").
            addMarkup("</b>").
            build();
    assertThat(text.getGlobalMetaData("foo", ""), is("val"));
    assertThat(text.getGlobalMetaData("non-existing-key", "xxx"), is("xxx"));
    assertThat(text.getGlobalMetaData(AnnotatedText.MetaDataKey.EmailToAddress, "xxx"), is("Foo Bar <foo@foobar.org>"));
    assertThat(text.getGlobalMetaData(AnnotatedText.MetaDataKey.DocumentTitle, "default-title"), is("default-title"));
    assertThat(text.getPlainText(), is("hello user!"));
    assertThat(text.getOriginalTextPositionFor(0), is(0));
    assertThat(text.getOriginalTextPositionFor(5), is(5));
    assertThat(text.getOriginalTextPositionFor(6), is(9));
    assertThat(text.getOriginalTextPositionFor(7), is(10));

    // Example:
    // hello user!
    //        ^ = position 8
    // hello <b>user!</b>
    //           ^ = position 11
    assertThat(text.getOriginalTextPositionFor(8), is(11));
  }

  @Test
  public void testIgnoreInterpretAs() {   // https://github.com/languagetool-org/languagetool/issues/1393
    AnnotatedText text = new AnnotatedTextBuilder().
            addText("hello ").
            addMarkup("<p>","\n\n").
            addText("more xxxx text!").
            build();
    assertThat(text.getPlainText(), is("hello \n\nmore xxxx text!"));
    assertThat(text.getTextWithMarkup(), is("hello <p>more xxxx text!"));
    ContextTools contextTools = new ContextTools();
    contextTools.setErrorMarkerStart("#");
    contextTools.setErrorMarkerEnd("#");
    contextTools.setEscapeHtml(false);
    assertThat(contextTools.getContext(14, 18, text.getTextWithMarkup()), is("hello <p>more #xxxx# text!"));
  }

}