/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xtc.tree.Location;

import static org.hamcrest.CoreMatchers.is;

@Disabled
public class LocationHelperTest {

  @Test
  public void testAbsolutePositionFor() {
    MatcherAssert.assertThat(checkLocation(1, 1, "hallo"), is(0));
    MatcherAssert.assertThat(checkLocation(1, 2, "hallo"), is(1));
    MatcherAssert.assertThat(checkLocation(2, 1, "hallo\nx"), is(6));
    MatcherAssert.assertThat(checkLocation(3, 3, "\n\nxyz"), is(4));
  }

  @Test
  public void testInvalidPosition() {
    MatcherAssert.assertThat(checkLocation(1, 1, "hallo"), is(0));
    try {
      checkLocation(2, 2, "hallo");
      Assertions.fail();
    } catch (RuntimeException ignored) {}
  }

  private int checkLocation(int line, int col, String text) {
    return LocationHelper.absolutePositionFor(new Location("", line, col), text);
  }

}
