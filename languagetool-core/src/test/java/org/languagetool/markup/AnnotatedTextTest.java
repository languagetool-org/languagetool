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

import org.junit.jupiter.api.Test;
import org.hamcrest.MatcherAssert;
import org.languagetool.tools.ContextTools;

import static org.hamcrest.core.Is.is;

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
    MatcherAssert.assertThat(text.getGlobalMetaData("foo", ""), is("val"));
    MatcherAssert.assertThat(text.getGlobalMetaData("non-existing-key", "xxx"), is("xxx"));
    MatcherAssert.assertThat(text.getGlobalMetaData(AnnotatedText.MetaDataKey.EmailToAddress, "xxx"), is("Foo Bar <foo@foobar.org>"));
    MatcherAssert.assertThat(text.getGlobalMetaData(AnnotatedText.MetaDataKey.DocumentTitle, "default-title"), is("default-title"));
    MatcherAssert.assertThat(text.getPlainText(), is("hello user!"));
    MatcherAssert.assertThat(text.getOriginalTextPositionFor(0, false), is(0));
    MatcherAssert.assertThat(text.getOriginalTextPositionFor(5, false), is(5));
    MatcherAssert.assertThat(text.getOriginalTextPositionFor(6, false), is(9));
    MatcherAssert.assertThat(text.getOriginalTextPositionFor(7, false), is(10));

    // Example:
    // hello user!
    //        ^ = position 8
    // hello <b>user!</b>
    //           ^ = position 11
    MatcherAssert.assertThat(text.getOriginalTextPositionFor(8, false), is(11));
  }

  @Test
  public void testIgnoreInterpretAs() {   // https://github.com/languagetool-org/languagetool/issues/1393
    AnnotatedText text = new AnnotatedTextBuilder().
            addText("hello ").
            addMarkup("<p>","\n\n").
            addText("more xxxx text!").
            build();
    MatcherAssert.assertThat(text.getPlainText(), is("hello \n\nmore xxxx text!"));
    MatcherAssert.assertThat(text.getTextWithMarkup(), is("hello <p>more xxxx text!"));
    ContextTools contextTools = new ContextTools();
    contextTools.setErrorMarker("#", "#");
    contextTools.setEscapeHtml(false);
    MatcherAssert.assertThat(contextTools.getContext(14, 18, text.getTextWithMarkup()), is("hello <p>more #xxxx# text!"));
  }

}
