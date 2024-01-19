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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompoundAcceptorTest {

  @Test
  public void testAcceptCompound() {
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
    assertTrue(acceptor.acceptCompound("adresvervloeking"));
    assertTrue(acceptor.acceptCompound("sportagente"));
    assertTrue(acceptor.acceptCompound("transferpersjes"));
    assertTrue(acceptor.acceptCompound("kunstomlijning"));
    assertTrue(acceptor.acceptCompound("webomlijning"));
    assertFalse(acceptor.acceptCompound("lingsboek"));

    assertTrue(acceptor.acceptCompound("webschoolboek"));
    assertFalse(acceptor.acceptCompound("gezondheidsomlijningssvervangingsinfluencers"));

    // test areas
    assertTrue(acceptor.acceptCompound("Zuidoost-Turkije"));
    assertTrue(acceptor.acceptCompound("Noord-Afghanistan"));
    assertFalse(acceptor.acceptCompound("Zuidwest-Frank"));
    assertTrue(acceptor.acceptCompound("Zuidwest-Gouda"));

    assertFalse(acceptor.acceptCompound("Papiersversnipperaar"));

    // prevent duplicate part1 & part2 from being accepted
    assertFalse(acceptor.acceptCompound("vriendenvrienden"));

    assertFalse(acceptor.acceptCompound("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    assertFalse(acceptor.acceptCompound("bedrijfskijkt"));

    // test for acronyms in compounds
    assertTrue(acceptor.acceptCompound("IRA-akkoord"));
    assertTrue(acceptor.acceptCompound("WK-finale"));
    assertTrue(acceptor.acceptCompound("VRF-regels"));
    assertFalse(acceptor.acceptCompound("WIFI-verbinding"));

    // test for words that always need a hyphen
    assertTrue(acceptor.acceptCompound("collega-burgemeester"));
    assertTrue(acceptor.acceptCompound("aspirant-buschauffeur"));
    assertFalse(acceptor.acceptCompound("aspirantbuschauffeur"));

    // test for acronym exceptions
    assertFalse(acceptor.acceptCompound("AIDS-afdeling"));
    assertTrue(acceptor.acceptCompound("aidsafdeling"));
    assertFalse(acceptor.acceptCompound("VIP-behandeling"));
    assertTrue(acceptor.acceptCompound("vipbehandeling"));
    assertTrue(acceptor.acceptCompound("vipcriticus"));
    assertTrue(acceptor.acceptCompound("vip-criticus"));
    assertFalse(acceptor.acceptCompound("ZZP-ondernemertje"));
    assertFalse(acceptor.acceptCompound("Zzp-ondernemertje"));
    assertTrue(acceptor.acceptCompound("zzp-ondernemertje"));
    assertFalse(acceptor.acceptCompound("CD-spelertje"));
    assertTrue(acceptor.acceptCompound("cd-spelertje"));
    assertFalse(acceptor.acceptCompound("BTW-toevoegingen"));
    assertTrue(acceptor.acceptCompound("btw-toevoegingen"));
    assertFalse(acceptor.acceptCompound("TV-poppetjes"));
    assertTrue(acceptor.acceptCompound("tv-poppetjes"));

    // test part1 exceptions
    assertFalse(acceptor.acceptCompound("honingsbijtje"));
    assertFalse(acceptor.acceptCompound("datingswebsite"));
    assertTrue(acceptor.acceptCompound("belastingvrij"));
    assertFalse(acceptor.acceptCompound("belastingsvrij"));

    // test part2 exceptions
    assertFalse(acceptor.acceptCompound("mannenlijk"));
    assertFalse(acceptor.acceptCompound("dienstvoor"));

    assertTrue(acceptor.acceptCompound("tombeplunderaar"));
    assertFalse(acceptor.acceptCompound("wetenschapbelasting"));
    assertTrue(acceptor.acceptCompound("Zwangerschapsblijheid"));

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
  public void testAcceptCompoundInternal() {
    CompoundAcceptor acceptor = new CompoundAcceptor();
    assertTrue(acceptor.acceptCompound("passagiers", "schip"));
    assertTrue(acceptor.acceptCompound("papier", "versnipperaar"));
    assertFalse(acceptor.acceptCompound("politie", "eenheid"));
    assertTrue(acceptor.acceptCompound("politie", "-eenheid"));
  }

}