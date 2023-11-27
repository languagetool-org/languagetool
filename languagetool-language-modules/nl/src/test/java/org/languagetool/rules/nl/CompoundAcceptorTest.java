/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Mark Baas
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
package org.languagetool.rules.nl;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompoundAcceptorTest {

  @Test
  public void testAcceptCompound() throws IOException {
    CompoundAcceptor acceptor = new CompoundAcceptor();

    assertTrue(acceptor.acceptCompound("bedrijfsregels"));
    assertFalse(acceptor.acceptCompound("bedrijfregels"));

    assertTrue(acceptor.acceptCompound("Bedrijfsbrommer"));
    assertFalse(acceptor.acceptCompound("Bedrijfbrommer"));

    assertTrue(acceptor.acceptCompound("straatpuzzel"));
    assertFalse(acceptor.acceptCompound("straatspuzzel"));

    assertTrue(acceptor.acceptCompound("zwangerschap"));
    assertFalse(acceptor.acceptCompound("zwangersschap"));

    assertTrue(acceptor.acceptCompound("Papierversnipperaar"));
    assertFalse(acceptor.acceptCompound("Papiersversnipperaar"));

    assertFalse(acceptor.acceptCompound("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    assertFalse(acceptor.acceptCompound("bedrijfskijkt"));

    //assertTrue(acceptor.acceptCompound("belastingvrij"));
    //assertFalse(acceptor.acceptCompound("belastingsvrij"));

    // test for acronyms in compounds
    assertTrue(acceptor.acceptCompound("IRA-akkoord"));
    assertFalse(acceptor.acceptCompound("iraakkoord"));

    assertTrue(acceptor.acceptCompound("tombeplunderaar"));
    assertFalse(acceptor.acceptCompound("wetenschapbelasting"));
    assertTrue(acceptor.acceptCompound("Zwangerschapsblijheid"));

    assertTrue(acceptor.acceptCompound("VRF-regels"));
    assertFalse(acceptor.acceptCompound("VRFregels"));

    // test for colliding vowels
    assertTrue(acceptor.acceptCompound("privé-eigenaar"));
    assertFalse(acceptor.acceptCompound("privéeigenaar"));

    assertTrue(acceptor.acceptCompound("politie-eenheid"));
    assertFalse(acceptor.acceptCompound("politieeenheid"));

    assertTrue(acceptor.acceptCompound("auto-uitlaat"));
    assertFalse(acceptor.acceptCompound("autouitlaat"));

  }

  @Ignore("Use for interactive debugging")
  @Test
  public void testAcceptCompoundInternal() throws IOException {
    CompoundAcceptor acceptor = new CompoundAcceptor();
    assertTrue(acceptor.acceptCompound("passagiers", "schip"));
    assertTrue(acceptor.acceptCompound("papier", "versnipperaar"));
    assertFalse(acceptor.acceptCompound("politie", "eenheid"));
    assertTrue(acceptor.acceptCompound("politie", "-eenheid"));
  }

}