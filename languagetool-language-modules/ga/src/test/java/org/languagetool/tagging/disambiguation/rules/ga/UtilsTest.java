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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.tagging.ga.Retaggable;
import org.languagetool.tagging.ga.Utils;

public class UtilsTest {
  
  @Test
  public void testToLowerCaseIrish() {
    Assertions.assertEquals("test", Utils.toLowerCaseIrish("TEST"));
    Assertions.assertEquals("test", Utils.toLowerCaseIrish("Test"));
    Assertions.assertEquals("t-aon", Utils.toLowerCaseIrish("tAON"));
    Assertions.assertEquals("n-aon", Utils.toLowerCaseIrish("nAON"));
  }

  @Test
  public void testUnLenited() {
    Assertions.assertEquals("Kate", Utils.unLenite("Khate"));
    Assertions.assertEquals("can", Utils.unLenite("chan"));
    Assertions.assertEquals("ba", Utils.unLenite("bha"));
    Assertions.assertEquals("b", Utils.unLenite("bh"));
    Assertions.assertNull(Utils.unLenite("can"));
    Assertions.assertNull(Utils.unLenite("a"));
  }

  @Test
  public void testUnEclipseChar() {
    // properly eclipsed
    Assertions.assertEquals("carr", Utils.unEclipseChar("gcarr", 'g', 'c'));
    Assertions.assertEquals("Carr", Utils.unEclipseChar("gCarr", 'g', 'c'));
    // improperly eclipsed
    Assertions.assertEquals("Carr", Utils.unEclipseChar("G-carr", 'g', 'c'));
    Assertions.assertEquals("Carr", Utils.unEclipseChar("Gcarr", 'g', 'c'));
    Assertions.assertEquals("CARR", Utils.unEclipseChar("GCARR", 'g', 'c'));
    // not eclipsed
    Assertions.assertNull(Utils.unEclipseChar("carr", 'g', 'c'));
  }

  @Test
  public void testUnEclipse() {
    Assertions.assertEquals("carr", Utils.unEclipse("g-carr"));
    Assertions.assertEquals("doras", Utils.unEclipse("n-doras"));
    Assertions.assertEquals("Geata", Utils.unEclipse("N-geata"));
    Assertions.assertEquals("peann", Utils.unEclipse("bpeann"));
    Assertions.assertEquals("bean", Utils.unEclipse("mbean"));
    Assertions.assertEquals("Éin", Utils.unEclipse("N-éin"));
    Assertions.assertEquals("focal", Utils.unEclipse("bhfocal"));
    Assertions.assertEquals("Focal", Utils.unEclipse("Bhfocal"));
    Assertions.assertEquals("Focal", Utils.unEclipse("Bfocal"));
    Assertions.assertNull(Utils.unEclipse("carr"));
  }

  @Test
  public void testUnLeniteDefiniteS() {
    Assertions.assertEquals("seomra1", Utils.unLeniteDefiniteS("t-seomra1"));
    Assertions.assertEquals("seomra2", Utils.unLeniteDefiniteS("tseomra2"));
    Assertions.assertEquals("Seomra3", Utils.unLeniteDefiniteS("tSeomra3"));
    Assertions.assertEquals("Seomra4", Utils.unLeniteDefiniteS("TSeomra4"));
    Assertions.assertEquals("Seomra5", Utils.unLeniteDefiniteS("Tseomra5"));
    Assertions.assertEquals("Seomra6", Utils.unLeniteDefiniteS("t-Seomra6"));
    Assertions.assertEquals("Seomra7", Utils.unLeniteDefiniteS("T-Seomra7"));
    Assertions.assertEquals("Seomra8", Utils.unLeniteDefiniteS("T-seomra8"));
    Assertions.assertNull(Utils.unLeniteDefiniteS("seomra9"));
  }

  @Test
  public void testDemutate() {
    Retaggable tmp = Utils.demutate("gcharr");
    Assertions.assertEquals(tmp.getWord(), "carr");
    Assertions.assertEquals(tmp.getAppendTag(), ":EclLen");
    tmp = Utils.demutate("t-seomra");
    Assertions.assertEquals(tmp.getWord(), "seomra");
    Assertions.assertEquals(tmp.getRestrictToPos(), "(?:C[UMC]:)?Noun:.*:DefArt");
  }

  @Test
  public void testFixSuffix() {
    Retaggable tmp = Utils.fixSuffix("caimiléaracht");
    Assertions.assertEquals(tmp.getWord(), "caimiléireacht");
  }

  @Test
  public void testUnPonc() {
    Assertions.assertEquals("chuir", Utils.unPonc("ċuir"));
    Assertions.assertEquals("CHUIR", Utils.unPonc("ĊUIR"));
    Assertions.assertEquals("Chuir", Utils.unPonc("Ċuir"));
    Assertions.assertEquals("FÉACH", Utils.unPonc("FÉAĊ"));
  }

  @Test
  public void testSimplify() {
    // perl -e 'for my $i (qw/18 4 0 13 0 19 7 0 8 17/) { printf "\\uD835\\uDC%X", (hex("1A") + $i);}'
    String boldUpper = "\uD835\uDC12\uD835\uDC04\uD835\uDC00\uD835\uDC0D\uD835\uDC00\uD835\uDC13\uD835\uDC07\uD835\uDC00\uD835\uDC08\uD835\uDC11";
    Assertions.assertEquals("SEANATHAIR", Utils.simplifyMathematical(boldUpper));
    String boldLower = "\uD835\uDC2C\uD835\uDC1E\uD835\uDC1A\uD835\uDC27\uD835\uDC1A\uD835\uDC2D\uD835\uDC21\uD835\uDC1A\uD835\uDC22\uD835\uDC2B";
    Assertions.assertEquals("seanathair", Utils.simplifyMathematical(boldLower));

    Assertions.assertEquals("999", Utils.simplifyMathematical("\uD835\uDFFF\uD835\uDFFF\uD835\uDFFF", false, true));
  }

  @Test
  public void testGreekToLatin() {
    Assertions.assertEquals("BOTTOM", Utils.greekToLatin("ΒΟΤΤΟΜ"));
  }

  @Test
  public void testHasMixedGreekAndLatin() {
    Assertions.assertEquals(true, Utils.hasMixedGreekAndLatin("Nοt"));
  }

  @Test
  public void testIsAllMathsChars() {
    String boldUpper = "\uD835\uDC12\uD835\uDC04\uD835\uDC00\uD835\uDC0D\uD835\uDC00\uD835\uDC13\uD835\uDC07\uD835\uDC00\uD835\uDC08\uD835\uDC11";
    Assertions.assertEquals(false, Utils.isAllMathsChars("foo"));
    Assertions.assertEquals(false, Utils.isAllMathsChars("f\uD835\uDC12"));
    Assertions.assertEquals(true, Utils.isAllMathsChars(boldUpper));
  }

  String torrach = "ｔｏｒｒａｃｈ";
  @Test
  public void testIsAllHalfWidthChars() {
    Assertions.assertEquals(true, Utils.isAllHalfWidthChars(torrach));
    Assertions.assertEquals(false, Utils.isAllHalfWidthChars(torrach + "a"));
  }
  @Test
  public void testHalfwidthLatinToLatin() {
    Assertions.assertEquals("torrach", Utils.halfwidthLatinToLatin(torrach));
  }

}
