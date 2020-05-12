/*
 * Copyright 2019 Jim O'Regan <jaoregan@tcd.ie>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.languagetool.tagging.disambiguation.rules.ga;

import org.junit.jupiter.api.Test;
import org.languagetool.tagging.ga.Retaggable;
import org.languagetool.tagging.ga.Utils;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {
  
  @Test
  public void testToLowerCaseIrish() {
    assertEquals("test", Utils.toLowerCaseIrish("TEST"));
    assertEquals("test", Utils.toLowerCaseIrish("Test"));
    assertEquals("t-aon", Utils.toLowerCaseIrish("tAON"));
    assertEquals("n-aon", Utils.toLowerCaseIrish("nAON"));
  }

  @Test
  public void testUnLenited() {
    assertEquals("Kate", Utils.unLenite("Khate"));
    assertEquals("can", Utils.unLenite("chan"));
    assertEquals("ba", Utils.unLenite("bha"));
    assertEquals("b", Utils.unLenite("bh"));
    assertEquals(null, Utils.unLenite("can"));
    assertEquals(null, Utils.unLenite("a"));
  }

  @Test
  public void testUnEclipseChar() {
    // properly eclipsed
    assertEquals("carr", Utils.unEclipseChar("gcarr", 'g', 'c'));
    assertEquals("Carr", Utils.unEclipseChar("gCarr", 'g', 'c'));
    // improperly eclipsed
    assertEquals("Carr", Utils.unEclipseChar("G-carr", 'g', 'c'));
    assertEquals("Carr", Utils.unEclipseChar("Gcarr", 'g', 'c'));
    assertEquals("CARR", Utils.unEclipseChar("GCARR", 'g', 'c'));
    // not eclipsed
    assertEquals(null, Utils.unEclipseChar("carr", 'g', 'c'));
  }

  @Test
  public void testUnEclipse() {
    assertEquals("carr", Utils.unEclipse("g-carr"));
    assertEquals("doras", Utils.unEclipse("n-doras"));
    assertEquals("Geata", Utils.unEclipse("N-geata"));
    assertEquals("peann", Utils.unEclipse("bpeann"));
    assertEquals("bean", Utils.unEclipse("mbean"));
    assertEquals("Éin", Utils.unEclipse("N-éin"));
    assertEquals("focal", Utils.unEclipse("bhfocal"));
    assertEquals("Focal", Utils.unEclipse("Bhfocal"));
    assertEquals("Focal", Utils.unEclipse("Bfocal"));
    assertEquals(null, Utils.unEclipse("carr"));
  }

  @Test
  public void testUnLeniteDefiniteS() {
    assertEquals("seomra1", Utils.unLeniteDefiniteS("t-seomra1"));
    assertEquals("seomra2", Utils.unLeniteDefiniteS("tseomra2"));
    assertEquals("Seomra3", Utils.unLeniteDefiniteS("tSeomra3"));
    assertEquals("Seomra4", Utils.unLeniteDefiniteS("TSeomra4"));
    assertEquals("Seomra5", Utils.unLeniteDefiniteS("Tseomra5"));
    assertEquals("Seomra6", Utils.unLeniteDefiniteS("t-Seomra6"));
    assertEquals("Seomra7", Utils.unLeniteDefiniteS("T-Seomra7"));
    assertEquals("Seomra8", Utils.unLeniteDefiniteS("T-seomra8"));
    assertEquals(null, Utils.unLeniteDefiniteS("seomra9"));
  }

  @Test
  public void testDemutate() {
    Retaggable tmp = Utils.demutate("gcharr");
    assertEquals(tmp.getWord(), "carr");
    assertEquals(tmp.getAppendTag(), ":EclLen");
    tmp = Utils.demutate("t-seomra");
    assertEquals(tmp.getWord(), "seomra");
    assertEquals(tmp.getRestrictToPos(), "(?:C[UMC]:)?Noun:.*:DefArt");
  }

  @Test
  public void testFixSuffix() {
    Retaggable tmp = Utils.fixSuffix("caimiléaracht");
    assertEquals(tmp.getWord(), "caimiléireacht");
  }

  @Test
  public void testUnPonc() {
    assertEquals("chuir", Utils.unPonc("ċuir"));
    assertEquals("CHUIR", Utils.unPonc("ĊUIR"));
    assertEquals("Chuir", Utils.unPonc("Ċuir"));
    assertEquals("FÉACH", Utils.unPonc("FÉAĊ"));
  }

  @Test
  public void testSimplify() {
    // perl -e 'for my $i (qw/18 4 0 13 0 19 7 0 8 17/) { printf "\\uD835\\uDC%X", (hex("1A") + $i);}'
    String boldUpper = "\uD835\uDC12\uD835\uDC04\uD835\uDC00\uD835\uDC0D\uD835\uDC00\uD835\uDC13\uD835\uDC07\uD835\uDC00\uD835\uDC08\uD835\uDC11";
    assertEquals("SEANATHAIR", Utils.simplifyMathematical(boldUpper));
    String boldLower = "\uD835\uDC2C\uD835\uDC1E\uD835\uDC1A\uD835\uDC27\uD835\uDC1A\uD835\uDC2D\uD835\uDC21\uD835\uDC1A\uD835\uDC22\uD835\uDC2B";
    assertEquals("seanathair", Utils.simplifyMathematical(boldLower));

    assertEquals("999", Utils.simplifyMathematical("\uD835\uDFFF\uD835\uDFFF\uD835\uDFFF", false, true));
  }

  @Test
  public void testGreekToLatin() {
    assertEquals("BOTTOM", Utils.greekToLatin("ΒΟΤΤΟΜ"));
  }

  @Test
  public void testHasMixedGreekAndLatin() {
    assertEquals(true, Utils.hasMixedGreekAndLatin("Nοt"));
  }

  @Test
  public void testIsAllMathsChars() {
    String boldUpper = "\uD835\uDC12\uD835\uDC04\uD835\uDC00\uD835\uDC0D\uD835\uDC00\uD835\uDC13\uD835\uDC07\uD835\uDC00\uD835\uDC08\uD835\uDC11";
    assertEquals(false, Utils.isAllMathsChars("foo"));
    assertEquals(false, Utils.isAllMathsChars("f\uD835\uDC12"));
    assertEquals(true, Utils.isAllMathsChars(boldUpper));
  }

  String torrach = "ｔｏｒｒａｃｈ";
  @Test
  public void testIsAllHalfWidthChars() {
    assertEquals(true, Utils.isAllHalfWidthChars(torrach));
    assertEquals(false, Utils.isAllHalfWidthChars(torrach + "a"));
  }
  @Test
  public void testHalfwidthLatinToLatin() {
    assertEquals("torrach", Utils.halfwidthLatinToLatin(torrach));
  }

}
