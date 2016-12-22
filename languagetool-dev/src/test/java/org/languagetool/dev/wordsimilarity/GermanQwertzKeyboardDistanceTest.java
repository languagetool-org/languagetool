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
package org.languagetool.dev.wordsimilarity;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class GermanQwertzKeyboardDistanceTest {

    @Test
    public void testDistance() {
        GermanQwertzKeyboardDistance distance = new GermanQwertzKeyboardDistance();
        assertThat(distance.getDistance('q', 'q'), is(0.0f));
        assertThat(distance.getDistance('q', 'w'), is(1.0f));
        assertThat(distance.getDistance('q', 'p'), is(9.0f));
        assertThat(distance.getDistance('q', 'a'), is(1.0f));
        assertThat(distance.getDistance('t', 'g'), is(1.0f));
        assertThat(distance.getDistance('a', 's'), is(1.0f));
        assertThat(distance.getDistance('a', 'g'), is(4.0f));
        assertThat(distance.getDistance('y', 'x'), is(1.0f));
        assertThat(distance.getDistance('c', 'n'), is(3.0f));
        assertThat(distance.getDistance('q', 'y'), is(2.0f));
        assertThat(distance.getDistance('q', 'm'), is(8.0f));
        assertThat(distance.getDistance('p', 'ß'), is(2.0f));
        assertThat(distance.getDistance('o', 'ß'), is(3.0f));
        // uppercase:
        assertThat(distance.getDistance('C', 'n'), is(3.0f));
        assertThat(distance.getDistance('c', 'N'), is(3.0f));
        assertThat(distance.getDistance('C', 'N'), is(3.0f));
    }
}