/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tokenizers.pt;

import org.junit.Test;

import java.sql.Struct;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Tiago F. Santos
 * @since 3.6
 */

public class PortugueseWordTokenizerTest {
  final PortugueseWordTokenizer wordTokenizer = new PortugueseWordTokenizer();

  private void testTokenise(String sentence, String ...tokens) {
    assertArrayEquals(tokens, wordTokenizer.tokenize(sentence).toArray());
  }

  @Test
  public void testTokeniseBreakChars() {
    testTokenise("Isto é\u00A0um teste", new String[]{"Isto", " ", "é", " ", "um", " ", "teste"});
    testTokenise("Isto\rquebra", new String[]{"Isto", "\r", "quebra"});
  }

  @Test
  public void testTokeniseHyphenNoWhiteSpace() {
    testTokenise("Agora isto sim é-mesmo!-um teste.",
      new String[]{"Agora", " ", "isto", " ", "sim", " ", "é", "-", "mesmo", "!", "-", "um", " ", "teste", "."});
  }

  @Test
  public void testTokeniseWordFinalHyphen() {
    testTokenise("Agora isto é- realmente!- um teste.",
      new String[]{"Agora", " ", "isto", " ", "é", "-", " ", "realmente", "!", "-", " ", "um", " ", "teste", "."});
  }

  @Test
  public void testTokeniseMDash() {
    testTokenise("Agora isto é—realmente!—um teste.",
      new String[]{"Agora", " ", "isto", " ", "é", "—", "realmente", "!", "—", "um", " ", "teste", "."});
  }

  @Test
  public void testTokeniseHyphenatedSingleToken() {
    testTokenise("sex-appeal", new String[]{"sex-appeal"});
    testTokenise("Aix-en-Provence", new String[]{"Aix-en-Provence"});
    testTokenise("Montemor-o-Novo", new String[]{"Montemor-o-Novo"});
    testTokenise("Andorra-a-Velha", new String[]{"Andorra-a-Velha"});
    testTokenise("Tsé-Tung", new String[]{"Tsé-Tung"});
  }

  @Test
  public void testTokeniseHyphenatedSplitRegardlessOfLetterCase() {
    testTokenise("jiu-jitsu", "jiu-jitsu");
    testTokenise("Jiu-jitsu", "Jiu-jitsu");
    testTokenise("JIU-JITSU", "JIU-JITSU");
    testTokenise("Jiu-Jitsu", "Jiu-Jitsu");
    testTokenise("franco-prussiano", "franco-prussiano");
    testTokenise("Franco-prussiano", "Franco-prussiano");
    testTokenise("Franco-Prussiano", "Franco-Prussiano");
  }

  @Test
  public void testTokeniseHyphenatedSplit() {
    testTokenise("Paris-São Paulo", new String[]{"Paris", "-", "São", " ", "Paulo"});
    // this word exists in the speller but not the tagger dict; this may become a problem
    testTokenise("Sem-Peixe", new String[]{"Sem", "-", "Peixe"});
    testTokenise("húngaro-americano", new String[]{"húngaro", "-", "americano"});
  }

  @Test
  public void testTokeniseHyphenatedClitics() {
    testTokenise("diz-se", new String[]{"diz", "-", "se"});
  }

  @Test
  public void testTokeniseMesoclisis() {
    testTokenise("fá-lo-á", new String[]{"fá", "-", "lo", "-", "á"});
    testTokenise("dir-lhe-ia", new String[]{"dir", "-", "lhe", "-", "ia"});
    testTokenise("banhar-nos-emos", new String[]{"banhar", "-", "nos", "-", "emos"});
  }

  @Test
  public void testTokeniseApostrophe() {
    // is
    testTokenise("d'água", new String[]{"d", "'", "água"});
    testTokenise("d’água", new String[]{"d", "’", "água"});
    // should be
    // testTokenise("d’água", new String[]{"d’", "água"});
    // testTokenise("d'água", new String[]{"d'", "água"});
  }

  @Test
    public void testTokeniseHashtags() {
    // Twitter and whatnot; same as English
    testTokenise("#CantadasDoBem", new String[]{"#", "CantadasDoBem"});
  }

  @Test
  public void testDoNotTokeniseUserMentions() {
    // Twitter and whatnot; same as English
    testTokenise("@user", new String[]{"@user"});
  }

  @Test
  public void testTokeniseCurrency() {
    testTokenise("R$45,00", new String[]{"R$", "45,00"});
    testTokenise("5£", new String[]{"5", "£"});
    testTokenise("US$249,99", new String[]{"US$", "249,99"});
    testTokenise("€2.000,00", new String[]{"€", "2.000,00"});
  }

  @Test
  public void testTokeniseSplitsPercent() {
    testTokenise("50%OFF", new String[]{"50%", "OFF"});
    testTokenise("%50", new String[]{"%", "50"});
    testTokenise("%", new String[]{"%"});
  }

  @Test
  public void testTokeniseNumberAbbreviation() {
    testTokenise("Nº666", new String[]{"Nº666"});  // superscript 'o'
    testTokenise("N°666", new String[]{"N°666"});  // degree symbol
    testTokenise("Nº 420", new String[]{"Nº", " ", "420"});
    testTokenise("N.º69", new String[]{"N", ".", "º69"});  // the '.' char splits it
    testTokenise("N.º 80085", new String[]{"N", ".", "º", " ", "80085"});  // the '.' char splits it
  }

  @Test
  public void testDoNotTokeniseOrdinalSuperscript() {
    // ordinal indicators
    testTokenise("1º", new String[]{"1º"});
    testTokenise("2.º", new String[]{"2.º"});
    testTokenise("3ºˢ", new String[]{"3ºˢ"});
    testTokenise("4.ºˢ", new String[]{"4.ºˢ"});
    testTokenise("5ª", new String[]{"5ª"});
    testTokenise("6ª", new String[]{"6ª"});
    testTokenise("7ªˢ", new String[]{"7ªˢ"});
    testTokenise("8ªˢ", new String[]{"8ªˢ"});
    // superscripts
    testTokenise("9ᵒ", new String[]{"9ᵒ"});
    testTokenise("10.ᵒ", new String[]{"10.ᵒ"});
    testTokenise("11ᵒˢ", new String[]{"11ᵒˢ"});
    testTokenise("12.ᵒˢ", new String[]{"12.ᵒˢ"});
    testTokenise("13ᵃ", new String[]{"13ᵃ"});
    testTokenise("14.ᵃ", new String[]{"14.ᵃ"});
    testTokenise("15ᵃˢ", new String[]{"15ᵃˢ"});
    testTokenise("16.ᵃˢ", new String[]{"16.ᵃˢ"});
    // regular lowercase
    testTokenise("17o", new String[]{"17o"});
    testTokenise("18.o", new String[]{"18.o"});
    testTokenise("19os", new String[]{"19os"});
    testTokenise("20.os", new String[]{"20.os"});
    testTokenise("21a", new String[]{"21a"});
    testTokenise("22.a", new String[]{"22.a"});
    testTokenise("23as", new String[]{"23as"});
    testTokenise("24.as", new String[]{"24.as"});
  }

  @Test
  public void testDoNotTokeniseDegreeExpressions() {
    testTokenise("25°", new String[]{"25°"});
    testTokenise("26,0°", new String[]{"26,0°"});
    testTokenise("27.0°", new String[]{"27.0°"});
    testTokenise("28,0°C", new String[]{"28,0°C"});
    testTokenise("29.0°C", new String[]{"29.0°C"});
    testTokenise("30,0°c", new String[]{"30,0°c"});
    testTokenise("31.0°c", new String[]{"31.0°c"});
    testTokenise("32°Ra", new String[]{"32°Ra"});
    testTokenise("33,1°Rø", new String[]{"33,1°Rø"});
    testTokenise("34°N", new String[]{"34°N"});
  }

  @Test
  public void testDoNotTokeniseSpaceSeparatedThousands() {
    testTokenise("35 000", new String[]{"35 000"});
    testTokenise("36 000 000", new String[]{"36 000 000"});
    testTokenise("37 000,00", new String[]{"37 000,00"});
    testTokenise("38 000 000,00", new String[]{"38 000 000,00"});
    testTokenise("39 000°", new String[]{"39 000°"});
    testTokenise("40 000%", new String[]{"40 000%"});
    testTokenise("41 000º", new String[]{"41 000º"});
    testTokenise("42 000o", new String[]{"42 000o"});
    testTokenise("43 00", new String[]{"43", " ", "00"});
  }

  @Test
  public void testTokeniseExponent() {
    testTokenise("km²", new String[]{"km", "²"});
  }

  @Test public void testTokeniseCopyrightAndSimilarSymbols() {
    testTokenise("Copyright©", new String[]{"Copyright", "©"});
    testTokenise("Bacana®", new String[]{"Bacana", "®"});
    testTokenise("Legal™", new String[]{"Legal", "™"});
  }

  @Test
  public void testDoNotTokeniseEmoji() {
    testTokenise("☺☺☺Só", new String[]{"☺☺☺Só"});
  }

  @Test
  public void testDoNotTokeniseModifierDiacritics() {
    // the tilde here is a unicode modifier char; normally, the unicode a-tilde (ã) is used
    testTokenise("Não", new String[]{"Não"});
  }

  @Test
  public void testTokeniseRarePunctuation() {
    testTokenise("⌈Herói⌋", new String[]{"⌈", "Herói", "⌋"});
    testTokenise("″Santo Antônio do Manga″", new String[]{"″", "Santo", " ", "Antônio", " ", "do", " ", "Manga", "″"});
  }
}
