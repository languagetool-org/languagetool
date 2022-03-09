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

import com.optimaize.langdetect.text.TextObjectFactory;
import com.optimaize.langdetect.text.TextObjectFactoryBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.DetectedLanguage;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LanguageIdentifierTest {

  private final static String fastTextBinary = "/home/languagetool/fasttext/fasttext";
  private final static String fastTextModel = "/home/languagetool/fasttext/lid.176.bin";
  private final static String ngramData = "/home/languagetool/model_ml50_new.zip";
  private final static String czech = "V současné době je označením Linux míněno nejen jádro operačního systému, " +
          "ale zahrnuje do něj též veškeré programové vybavení";

  private final LanguageIdentifier identifier = new LanguageIdentifier();

  @Test
  @Ignore("requires ngram data from https://languagetool.org/download/ngram-lang-detect/")
  public void cleanAndShortenText() {
    LanguageIdentifier ident = new LanguageIdentifier(20);
    assertThat(ident.cleanAndShortenText("foo"), is("foo"));
    assertThat(ident.cleanAndShortenText("foo this is so long it will be cut off"), is("foo this is so long "));
    assertThat(ident.cleanAndShortenText("clean\uFEFF\uFEFFme"), is("clean me"));
    assertThat(ident.cleanAndShortenText("a https://x.com blah"), is("a https://x.com blah"));
    LanguageIdentifier ident2 = new LanguageIdentifier(100);
    ident2.enableNgrams(new File(ngramData));
    assertThat(ident2.cleanAndShortenText("foo https://www.example.com blah"), is("foo   blah"));
    assertThat(ident2.cleanAndShortenText("foo https://example.com?foo-bar blah"), is("foo   blah"));
    assertThat(ident2.cleanAndShortenText("foo foo.bla@example.com blah"), is("foo   blah"));
    assertThat(ident2.cleanAndShortenText("But @handle said so on twitter!"), is("But  said so on twitter!"));
    assertThat(ident2.cleanAndShortenText("A non\u00A0breaking space."), is("A non breaking space."));
  }
  
  @Test
  public void testDetection() {
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
    // detected as not supported by the unicode characters used:
    langAssert("zz", "ลินุกซ์ (อังกฤษ: Linux)");  // Thai
    langAssert("zz", "यूएसबी (अंग्रेज़ी: Live ...)");  // Hindi
    langAssert("zz", "लिनक्स (इंग्लिश: Linux)");  // Marathi
  }

  @Test
  @Ignore("disabled minimum length, instead now providing confidence score")
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
    // wrong, but low probabilities
    //identifier.enableFasttext(new File(fastTextBinary), new File(fastTextModel));
    // fasttext just assumes english, ignore / comment out
    langAssert(null, "");
    langAssert("ca", "X");

    // not activated because it impairs detection of Spanish, so ast and gl may be mis-detected:
    langAssert("es", "L'Iberorrománicu o Iberromance ye un subgrupu de llingües romances que posiblemente ...");  // ast
    langAssert("es", "Dodro é un concello da provincia da Coruña pertencente á comarca do Sar ...");  // gl
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
    langAssert("zz", czech, csIdent, Arrays.asList("cs"), Collections.emptyList());   // the no-op language
  }

  @Test
  @Ignore("Only works with locally installed fastText, no test - for interactive use")
  public void testInteractively() {
    LanguageIdentifier ident = new LanguageIdentifier();
    ident.enableFasttext(new File(fastTextBinary), new File(fastTextModel));
    List<String> inputs = Arrays.asList(
            "was meinst du?",          // en, should be de - fasttext confidence used (>0.9) 
            "was meinst?",             // es, should be de - fasttext confidence used (>0.9)
            "Am 31. September",        // en, should be de - fasttext confidence used (>0.9)
            "Anbei.",                  // sv, should be de - not in German common words
            "what do you think",       // ok
            "If the",                  // ok
            "Lettertypes",             // en, should be nl
            "Зараз десь когось нема",  // ok
            "if the man",              // ok
            "Die in peace",            // de, should be en - "die" not in English common words list
            "Paste text",              // ok
            "Sehr leckere Sourcreme",  // ok
            "Bewerk lettertypen",      // de, should be nl - both words not in common words
            "Colores",                 // en, should be es - not in Spanish common words
            "Cerrar",                  // sv, should be es - not in Spanish common words
            "Brand in arrivo"          // en, should be it
    );
    //List<String> inputs = Arrays.asList("Brand in arrivo!");
    for (String input : inputs) {
      DetectedLanguage lang = ident.detectLanguageWithDetails(input);
      System.out.println("Input     : " + input);
      System.out.println("Language  : " + lang.getDetectedLanguage());
      System.out.println("confidence: " + lang.getDetectionConfidence());
      System.out.println();
    }
  }

  @Test
  @Ignore("Only works with locally installed fastText")
  public void testShortTexts() {
    LanguageIdentifier defaultIdent = new LanguageIdentifier();
    defaultIdent.enableFasttext(new File(fastTextBinary), new File(fastTextModel));
    langAssert("en", "If the", defaultIdent);
    langAssert("en", "if the man", defaultIdent);
    langAssert("en", "Paste text", defaultIdent);
    langAssert("de", "Sehr leckere Sourcreme", defaultIdent);
    langAssert("de", "Die Menschen in den öst", defaultIdent);
    langAssert("de", "Den Vogel ", defaultIdent);
    langAssert("de", "Den Eisenbahner-Esperanto-Kongress im", defaultIdent);
    langAssert("uk", "Зараз десь когось нема", defaultIdent);
    langAssert("da", "En to meter lang levende krokodille er blevet fundet i et drivhus i en have i Sveriges tredje største by", defaultIdent);
    langAssert("da", "Elektriske lamper, gemt bag et loft af mælkehvidt, gennemskinneligt glas, kastede et mildt lys på museets skatt", defaultIdent);
    //langAssert("de", "Am 31. September", defaultIdent);
    //langAssert("es", "Colores", defaultIdent);
    //langAssert("fr", "Fermer", defaultIdent);
    //langAssert("es", "Cerrar", defaultIdent);
    //langAssert("nl", "Lettertypes", defaultIdent);
    //langAssert("it", "Brand in arrivo!", defaultIdent);
    //langAssert("de", "Alles was Tom woll", defaultIdent);
    //langAssert("nl", "Bewerk lettertypen", defaultIdent);
    //langAssert("en", "Die in peace", defaultIdent);
    //langAssert("ja", "一体日本人は生きるということを知っているだろうか。小学校の門を潜ってから", defaultIdent);   // see https://github.com/languagetool-org/languagetool/issues/1278
  }

  @Test
  @Ignore("Only works with locally installed fastText")
  public void testShortTextsWithPreferredLanguage() {
    LanguageIdentifier ident = new LanguageIdentifier();
    List<String> enDePreferred = Arrays.asList("de", "en");
    List<String> noop = Arrays.asList();
    ident.enableFasttext(new File(fastTextBinary), new File(fastTextModel));

    // short, but has a specific character set that helps detection:
    langAssert("uk", "Зараз десь когось нема", ident, noop, enDePreferred);
    // long enough to ignore the preferred languages:
    langAssert("da", "En to meter lang levende krokodille er blevet fundet i et drivhus i en have i Sveriges tredje største by", ident, noop, enDePreferred);
    langAssert("da", "Elektriske lamper, gemt bag et loft af mælkehvidt, gennemskinneligt glas, kastede et mildt lys på museets skatt", ident, noop, enDePreferred);
    
    // English:
    //langAssert("en", "Die in peace", ident, noop, enDePreferred);
    langAssert("en", "Paste text", ident, noop, enDePreferred);
    langAssert("en", "If the man", ident, noop, enDePreferred);
    langAssert("en", "If the", ident, noop, enDePreferred);
    
    // German:
    //langAssert("de", "Am 31. September", ident, noop, enDePreferred);
    //langAssert("de", "Anbei.", ident, noop, enDePreferred);
    //langAssert("de", "was meinst", ident, noop, enDePreferred);
    //langAssert("de", "was meinst du?", ident, noop, enDePreferred);
    //langAssert("de", "Hi Traumfrau", ident, noop, enDePreferred);
    langAssert("de", "Sehr leckere Sourcreme", ident, noop, enDePreferred);
    langAssert("de", "Die Menschen in den öst", ident, noop, enDePreferred);
    langAssert("de", "Den Vogel ", ident, noop, enDePreferred);
    langAssert("de", "Den Eisenbahner-Esperanto-Kongress im", ident, noop, enDePreferred);
    
    try {
      ident.detectLanguage("fake", noop, Arrays.asList("de", "en-US"));
      fail("Expected exception");
    } catch (IllegalArgumentException ignore) { }
  }

  @Test
  @Ignore("Known to fail due to bug")
  public void textObjectBugForJapanese() {
    // see https://github.com/languagetool-org/languagetool/issues/1278
    TextObjectFactory textObjectFactory  = new TextObjectFactoryBuilder().maxTextLength(1000).build();
    String inp = "一体日本人は生きるということを知っているだろうか。";
    String shortText = textObjectFactory.forText(inp).toString();
    System.out.println(shortText);
    assertEquals(inp, shortText);
  }

  @Test
  public void testAdditionalLanguagesBuiltIn() {
    LanguageIdentifier defaultIdent = new LanguageIdentifier();
    langAssert("sk", czech, defaultIdent);  // misdetected, as cz isn't supported by LT
    LanguageIdentifier csIdent = new LanguageIdentifier();
    langAssert("sk", czech, csIdent, Arrays.asList("cs"), Collections.emptyList());   // no-op language only supported by fastText 
  }

  private void langAssert(String expectedLangCode, String text) {
    langAssert(expectedLangCode, text, identifier, Collections.emptyList(), Collections.emptyList());
  }
  
  private void langAssert(String expectedLangCode, String text, LanguageIdentifier id) {
    langAssert(expectedLangCode, text, id, Collections.emptyList(), Collections.emptyList());
  }
  
  private void langAssert(String expectedLangCode, String text, LanguageIdentifier id, List<String> noopLangCodes,
                          List<String> preferredLangCodes) {
    DetectedLanguage detectedLang = id.detectLanguage(text, noopLangCodes, preferredLangCodes);
    String detectedLangCode = detectedLang != null ?
      detectedLang.getDetectedLanguage() != null ? detectedLang.getDetectedLanguage().getShortCode() : null
      : null;
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
