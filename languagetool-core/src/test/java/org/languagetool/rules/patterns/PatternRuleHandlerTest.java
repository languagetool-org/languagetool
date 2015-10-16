/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PatternRuleHandlerTest {
  
  @Test
  public void testReplaceSpacesInRegex() {
    PatternRuleHandler handler = new PatternRuleHandler();
    String s = "(?:[\\s\u00A0\u202F]+)";
    assertThat(handler.replaceSpacesInRegex("foo bar"), is("foo" + s + "bar"));
    assertThat(handler.replaceSpacesInRegex("foo bar x"), is("foo" + s + "bar" + s + "x"));
    assertThat(handler.replaceSpacesInRegex("foo  bar"), is("foo" + s + s + "bar"));  // well, does not really make sense
    assertThat(handler.replaceSpacesInRegex("fo[xy ] bar"), is("fo[xy ]" + s + "bar"));
  }

}