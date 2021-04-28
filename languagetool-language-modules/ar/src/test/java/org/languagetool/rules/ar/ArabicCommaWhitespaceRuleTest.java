/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Sohaib Afifi, Taha Zerrouki
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

package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ArabicCommaWhitespaceRuleTest {
  
  private ArabicCommaWhitespaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new ArabicCommaWhitespaceRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));
  }

  @Test
  public void testRule() throws IOException {
    // correct
    assertMatches("هذه جملة تجريبية.", 0);
    assertMatches("هذه, هي, جملة التجربة.", 0);
    assertMatches("قل (كيت وكيت) تجربة!.", 0);
    assertMatches("تكلف €2,45.", 0);
    assertMatches("ثمنها 50,- يورو", 0);
    assertMatches("جملة مع علامات الحذف ...", 0);
    assertMatches("هذه صورة: .5 وهي صحيحة.", 0);
    assertMatches("هذه $1,000,000.", 0);
    assertMatches("هذه 1,5.", 0);
    assertMatches("هذا ,,فحص''.", 0);
    assertMatches("نفّذ ./validate.sh لفحص الملف.", 0);
    assertMatches("هذه,\u00A0حقا,\u00A0فراغ غير فاصل.", 0);

    // errors:
    // arabic comma
    assertMatches("هذه،جملة للتجربة.", 1);
    assertMatches("هذه ، جملة للتجربة.", 1);
    assertMatches("هذه ،تجربة جملة.", 2);
    assertMatches("،هذه جملة للتجربة.", 2);
  }

  private void assertMatches(String text, int expectedMatches) throws IOException {
    assertEquals(expectedMatches, rule.match(lt.getAnalyzedSentence(text)).length);
  }
}
