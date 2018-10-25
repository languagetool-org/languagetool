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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

public class LanguageIdentifierTest {

  private final LanguageIdentifier identifier = new LanguageIdentifier();
  private final static String fastTextBinary = "/prg/fastText-0.1.0/fasttext";
  private final static String fastTextModel = "/prg/fastText-0.1.0/data/lid.176.bin";
  private final static String czech = "V současné době je označením Linux míněno nejen jádro operačního systému, " +
          "ale zahrnuje do něj též veškeré programové vybavení";

  @Test
  public void testDetection() {
    //identifier.enableFasttext(new File(fastTextBinary), new File(fastTextModel));
    // fasttext just assumes english, ignore / comment out
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
  public void testShortAndLongText() {
    LanguageIdentifier id10 = new LanguageIdentifier(10);
    langAssert(null, "Das ist so ein Text, mit dem man testen kann", id10);  // too short when max length is applied
    langAssert(null, "012345678", id10);
    langAssert(null, "0123456789", id10);
    langAssert(null, "0123456789A", id10);
    langAssert(null, "0123456789AB", id10);
    langAssert(null, "0123456789ABC", id10);

    LanguageIdentifier id20 = new LanguageIdentifier(20);
    langAssert("de", "Das ist so ein Text, mit dem man testen kann", id20);
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

  @Test
  public void testIgnoreSignature() {
    langAssert("de", "Das ist ein deutscher Text\n-- \nBut this is an\nEnglish text in the signature, and it's much longer than the original text.");
    langAssert("en", "This is an English text.\n-- \nDas ist ein\ndeutscher Text in der Signatur, der länger ist als der Haupttext.");
  }

  @Test
  @Ignore("Only works with locally installed fastText")
  public void testAdditionalLanguagesFasttext() {
    LanguageIdentifier defaultIdent = new LanguageIdentifier();
    langAssert("sk", czech, defaultIdent);  // misdetected, as cz isn't supported by LT

    LanguageIdentifier csIdent = new LanguageIdentifier();
    csIdent.enableFasttext(new File(fastTextBinary), new File(fastTextModel));
    langAssert("zz", czech, csIdent, Arrays.asList("cs"));   // the no-op language
  }

  @Test
  public void testAdditionalLanguagesBuiltIn() {
    LanguageIdentifier defaultIdent = new LanguageIdentifier();
    langAssert("sk", czech, defaultIdent);  // misdetected, as cz isn't supported by LT
    LanguageIdentifier csIdent = new LanguageIdentifier();
    langAssert("sk", czech, csIdent, Arrays.asList("cs"));   // no-op language only supported by fastText 
  }

  private void langAssert(String expectedLangCode, String text) {
    langAssert(expectedLangCode, text, identifier, Collections.emptyList());
  }
  
  private void langAssert(String expectedLangCode, String text, LanguageIdentifier id) {
    langAssert(expectedLangCode, text, id, Collections.emptyList());
  }
  
  private void langAssert(String expectedLangCode, String text, LanguageIdentifier id, List<String> noopLangCodes) {
    Language detectedLang = id.detectLanguage(text, noopLangCodes);
    String detectedLangCode = detectedLang != null ? detectedLang.getShortCode() : null;
    if (expectedLangCode == null) {
      if (detectedLangCode != null) {
        fail("Got '" + detectedLangCode + "', expected null for '" + text + "'");
      }
    } else {
      if (!expectedLangCode.equals(detectedLangCode)) {
        if (detectedLang != null) {
          fail("Got '" + detectedLangCode + "', expected '" + expectedLangCode + "' for '" + text + "'");
        } else {
          fail("Got null, expected '" + expectedLangCode + "' for '" + text + "'");
        }
      }
    }
  }

}
