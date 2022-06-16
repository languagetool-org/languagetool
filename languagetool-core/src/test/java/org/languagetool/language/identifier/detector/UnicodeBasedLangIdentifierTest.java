/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language.identifier.detector;

import org.junit.Test;
import org.languagetool.language.identifier.detector.UnicodeBasedDetector;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

public class UnicodeBasedLangIdentifierTest {

  private final UnicodeBasedDetector ident = new UnicodeBasedDetector(100);

  @Test
  public void testGetDominantLangCodes() {
    String arabic = "[ar, fa]";
    String cyrillic = "[ru, uk, be]";
    String cjk = "[zh, ja]";
    String devanagari = "[hi, mr]";
    String thai = "[th]";
    String hebrew = "[he]";
    String korean = "[ko]";

    assertThat(codes(""), is("[]"));
    assertThat(codes(" "), is("[]"));
    assertThat(codes("hallo"), is("[]"));
    assertThat(codes("hallo this is a text"), is("[]"));
    assertThat(codes("hallo this is a text стиль"), is("[]"));

    assertThat(codes("Грамматика, стиль и орфография LanguageTool проверяет ваше правописание на более чем 20 языках"), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках"), is(cyrillic));
    assertThat(codes("Програма перевірки граматики, стилю та орфографії. LanguageTool перевіряє ваші тексти більш ніж 20-ма мовами"), is(cyrillic));
    assertThat(codes("Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах."), is(cyrillic));  // Belarusian
    assertThat(codes("Програма перевірки граматики, стилю та орфографії."), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках - Програма перевірки граматики, стилю та орфографії."), is(cyrillic));
    assertThat(codes("Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах. - Програма перевірки граматики, стилю та орфографії."), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках - Сучасная беларуская мова існуе ў літаратурнай і дыялектнай формах."), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках" +
                     "Here's some short English text, but it's short"), is(cyrillic));
    assertThat(codes("Програма перевірки граматики, стилю та орфографії." +
                     "Here's some short English text, but it's short"), is(cyrillic));
    assertThat(codes("проверяет ваше правописание на более чем 20 языках" +
                     "Here's some English text"), is(cyrillic));

    // Arabic
    assertThat(codes("لِينُكس (بالإنجليزية: Linux)\u200F (عن هذا الملف استمع (؟·معلومات)) ويسمى أيضا"), is(arabic));
    assertThat(codes("طور لينكس في الأصل لكي يعمل على"), is(arabic));
    assertThat(codes("يعمل لينكس أيضا على"), is(arabic));
    assertThat(codes("في بادئ الأمر أراد"), is(arabic));

    // Chinese:
    // accept ambiguity here, we assume the actual language identifier will tell Chinese from Japanese
    assertThat(codes("您的意思是"), is(cjk));
    assertThat(codes("Linux嚴格來說是單指作業系統的内核"), is(cjk));
    assertThat(codes("通常情况下 but here's more text with Latin characters"), is("[]"));
    // Japanese:
    assertThat(codes("Linux（リナックス、他の読みは後述）とは、Unix系オペレーティングシステムカーネル"), is(cjk));
    assertThat(codes("1990年代はFreeBSDと比較して安定性に劣ると言われてきたが"), is(cjk));
    assertThat(codes("リーナス・トーバルズはカーネル開"), is(cjk));

    // Khmer:
    assertThat(codes("ហើយដោយ​ព្រោះ​"), is("[km]"));

    // Tamil:
    assertThat(codes("லேங்குவேஜ்"), is("[ta]"));

    // Greek:
    assertThat(codes("Το Linux μπορεί να εγκατασταθεί και"), is("[el]"));
    assertThat(codes("Δημιουργός του πυρήνα Linux είναι ο"), is("[el]"));
    assertThat(codes("Ο Τόρβαλντς ξεκίνησε"), is("[el]"));

    // Hindi:
    assertThat(codes("दरलैंड में कोरोनोवायरस के दर्ज मामलों की संख्या 38 से बढ़कर 82 हो गई है।\n" +
                     " मामलों में वृद्धि तब होती है जब देश उत्तरी इटली में स्कीइंग की छुट्टियों"), is(devanagari));  // Hindi
    assertThat(codes("आम्हाला उशीर होणार नाही"), is(devanagari));  // Marathi

    // Thai:
    assertThat(codes("ลินุกซ์ (อังกฤษ: Linux) และรู้จักในชื่อ กะนู/ลินุกซ์"), is(thai));
    assertThat(codes("ลินุกซ์มีสัญญาอนุญาตแบบ GPL ซึ่งเป็นสัญญาอนุญาตที่กำหนด"), is(thai));

    // Hebrew:
    assertThat(codes("לינוקס (באנגלית: Linux) היא משפחה של מערכות הפעלה המבוססות"), is(hebrew));
    assertThat(codes("לינוקס היא דוגמה"), is(hebrew));

    // Korean:
    assertThat(codes("리눅스(Linux)[4]는 1991년 9월 17일 리누스 토르발스가 처음 출시한"), is(korean));
    assertThat(codes("리눅스(Linux)[4]는 1991년 9월 17일 리누"), is(korean));
    assertThat(codes("배포판에는 리눅스 커널과 지원"), is(korean));
    assertThat(codes("저명한 리눅스 배포판"), is(korean));
    assertThat(codes("리눅스는 또한 일반적으로"), is(korean));
    assertThat(codes("리눅스는 자유-"), is(korean));
  }

  private String codes(String s) {
    return ident.getDominantLangCodes(s).toString();
  }
}
