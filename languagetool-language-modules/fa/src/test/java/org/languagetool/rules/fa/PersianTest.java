/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fa;

import org.junit.jupiter.api.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Persian;

import java.io.IOException;
import java.util.Arrays;

public class PersianTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "لطفا متن خود را اینجا قرار دهید . یا بررسی کنید که این متن را\u200C برای دیدن بعضی بعضی از اشکال هایی که ابزار زبان توانسته تشخیس هدد. درباره ی نرم افزارهای بررسی کننده های گرامر چه فکر می کنید؟ لطفا در نظر داشته باشید که آن\u200Cها بی نقص نمی باشند.\u200E";
    Persian lang = new Persian();
    testDemoText(lang, s,
      Arrays.asList("PERSIAN_COMMA_PARENTHESIS_WHITESPACE", "Bad_ZWNJ", "PERSIAN_WORD_REPEAT_RULE", "PluralFix", "PluralFix", "Complex_Present_Verbs", "Complex_Present_Verbs")
    );
    runTests(lang, null, "ā");
  }
}
