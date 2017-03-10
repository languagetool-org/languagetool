/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.util.Objects;

import static org.junit.Assert.fail;

public class LanguageIdentifierTest {

  private final LanguageIdentifier identifier = new LanguageIdentifier();

  @Test
  public void testDetection() {
    langAssert(null, "");
    langAssert(null, "X");
    langAssert("de", "Das ist ein deutscher Text");
    langAssert("en", "This is an English text");
    langAssert("fr", "Le mont Revard est un sommet du département français ...");
    // some test sentences from the "Linux" article of Wikipedia:
    langAssert("be", "Першапачаткова Linux распрацоўваўся і выкарыстоўваўся асобнымі аматарамі на сваіх персанальных камп'ютарах.");
    langAssert("ca", "Aquest sistema operatiu va créixer gràcies al treball col·laboratiu de programadors de tot el món ...");
    langAssert("zh", "Linux最初是作为支持英特尔x86架构的个人电脑的一个自由操作系统。目前Linux已经被移植到更多的计算机硬件平台");
    langAssert("da", "Linux-distributionerne har traditionelt deres største udbredelse på servere, men er hastigt på vej på almindelige pc'er.");
    langAssert("nl", "Linux is een verzameling van open source Unix-achtige besturingssystemen gebaseerd op de POSIX-standaard.");
    langAssert("el", "Το Linux μπορεί να εγκατασταθεί και να λειτουργήσει σε μεγάλη ποικιλία υπολογιστικών συστημάτων, από μικρές συσκευές όπως κινητά τηλέφωνα ...");
    //langAssert("is", "Linux er frjáls stýrikerfiskjarni sem Linus Torvalds byrjaði að skrifa árið 1991, ...");
    langAssert("it", "Grazie alla portabilità del kernel Linux sono stati sviluppati sistemi operativi Linux per un'ampia gamma di dispositivi:");
    langAssert("ja", "Linuxは、狭義ではLinuxカーネルを意味し、広義ではそれをカーネルとして用いたオペレーティングシステム (OS) を意味する。");
    langAssert("km", "បច្ចុប្បន្នគ្មានអត្ថបទក្នុងទំព័រនេះទេ។អ្នកអាច ស្វែងរក​ចំណងជើង​នៃទំព័រនេះក្នុងទំព័រដទៃទៀត​​ ឬ ស្វែង​រក​កំណត់​ហេតុ​ដែល​ពាក់ព័ន្ធ ឬ កែប្រែ​ទំព័រនេះ");
    //langAssert("lt", "Linux – laisvos (atviro kodo) operacinės sistemos branduolio (kernel) pavadinimas.");
    //langAssert("ml", "വളരെ പ്രശസ്തമായ ഒരു ഓപ്പറേറ്റിംഗ് സിസ്റ്റമാണ് ഗ്നു/ലിനക്സ് (ആംഗലേയം:GNU/Linux).");
    langAssert("fa", "این صفحه حذف شده\u200Cاست. در زیر سیاههٔ حذف و انتقال این صفحه نمایش داده شده\u200Cاست.");
    langAssert("pl", "Linux – rodzina uniksopodobnych systemów operacyjnych opartych na jądrze Linux.");
    langAssert("pt", "Linux é um termo utilizado para se referir a sistemas operativos ou sistemas operacionais que utilizem o núcleo Linux.");
    langAssert("ro", "Linux este o familie de sisteme de operare de tip Unix care folosesc Nucleul Linux (în engleză kernel).");
    langAssert("ru", "Linux, также Ли́нукс — общее название Unix-подобных операционных систем, основанных на одноимённом ядре.");
    langAssert("sk", "Linux je počítačový operačný systém a jeho jadro.");
    langAssert("sl", "Linux je prost operacijski sistem podoben Unixu s prosto dostopno izvorno kodo ...");
    langAssert("es", "GNU/Linux es uno de los términos empleados para referirse a la combinación del núcleo o kernel libre ...");
    langAssert("sv", "Linux eller GNU/Linux är ett Unix-liknande operativsystem som till största delen");
    langAssert("tl", "Ang Linux ay isang operating system kernel para sa mga operating system na humahalintulad sa Unix.");
    langAssert("ta", "Linux பற்றி பிற கட்டுரைகளில் தேடிப்பாருங்கள்.");
    langAssert("uk", "Лі́нукс — загальна назва UNIX-подібних операційних систем на основі однойменного ядра.");
    langAssert("km", "អ្នក\u200Bអាច\u200Bជួយ\u200Bលើក\u200Bស្ទួយ\u200Bវិគីភីឌាភាសាខ្មែរ\u200Bនេះ\u200Bឱ្យ\u200Bមាន\u200Bលក្ខណៈ");
    // not yet in language-detector 0.5:
    langAssert("eo", "Imperiestraj pingvenoj manĝas ĉefe krustacojn kaj malgrandajn ...");
  }

  @Test
  public void testKnownLimitations() {
    // not activated because it impairs detection of Spanish, so ast and gl may be mis-detected:
    langAssert("es", "L'Iberorrománicu o Iberromance ye un subgrupu de llingües romances que posiblemente ...");  // ast
    langAssert(null, "Dodro é un concello da provincia da Coruña pertencente á comarca do Sar ...");  // gl
    // Somali, known by language-detector, but not by LT, so we get back something else:
    langAssert("tl", "Dhammaan garoomada caalamka ayaa loo isticmaalaa. Ururku waxa uu qabtaa ama uu ku shaqaleeyahay " +
            "isusocodka diyaaradaha aduunka ee iskaga gooshaya xuduudaha iyo ka hortagga wixii qalad ah iyo baaritaanka " +
            "marka ay dhacdo dhibaato la xiriirta dulimaad.");
  }

  private void langAssert(String expectedLangCode, String text) {
    Language expectedLang = expectedLangCode != null ? Languages.getLanguageForShortCode(expectedLangCode) : null;
    Language detectedLang = identifier.detectLanguage(text);
    if (!Objects.equals(expectedLang, detectedLang)) {
      fail("Got '" + detectedLang + "', expected '" + expectedLangCode + "' for '" + text + "'");
    }
  }

}
