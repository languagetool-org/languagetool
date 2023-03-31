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
import org.languagetool.language.identifier.DefaultLanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.languagetool.language.identifier.detector.FastTextDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultLanguageIdentifierTest extends LanguageIdentifierTest{

  private final static String fastTextBinary = "/home/languagetool/fasttext/fasttext";
  private final static String fastTextModel = "/home/languagetool/fasttext/lid.176.bin";
  private final static String ngramData = "/home/languagetool/model_ml50_new.zip";
  private final static String czech = "V současné době je označením Linux míněno nejen jádro operačního systému, " +
          "ale zahrnuje do něj též veškeré programové vybavení";
  
  @Test
  @Ignore("requires ngram data from https://languagetool.org/download/ngram-lang-detect/")
  public void cleanAndShortenText() {
    LanguageIdentifier ident = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(20, null, null, null);
    assertThat(ident.cleanAndShortenText("foo"), is("foo"));
    assertThat(ident.cleanAndShortenText("foo this is so long it will be cut off"), is("foo this is so long "));
    assertThat(ident.cleanAndShortenText("clean\uFEFF\uFEFFme"), is("clean me"));
    assertThat(ident.cleanAndShortenText("a https://x.com blah"), is("a https://x.com blah"));
    LanguageIdentifier ident2 = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("")
            .getDefaultLanguageIdentifier(100, new File(ngramData), null, null);
    assertThat(ident2.cleanAndShortenText("foo https://www.example.com blah"), is("foo   blah"));
    assertThat(ident2.cleanAndShortenText("foo https://example.com?foo-bar blah"), is("foo   blah"));
    assertThat(ident2.cleanAndShortenText("foo foo.bla@example.com blah"), is("foo   blah"));
    assertThat(ident2.cleanAndShortenText("But @handle said so on twitter!"), is("But  said so on twitter!"));
    assertThat(ident2.cleanAndShortenText("A non\u00A0breaking space."), is("A non breaking space."));
  }
  
  @Test
  @Ignore("Requires local files to run")
  public void fasttextReinitTest() throws IOException, InterruptedException {
    File ngramDataFile = new File("/home/stefan/Dokumente/languagetool/data/model_ml50_new.zip");
    File fastTextBinaryFile = new File("/home/stefan/Dokumente/languagetool/data/fasttext/fasttext");
    File fastTextModelFile = new File("/home/stefan/Dokumente/languagetool/data/fasttext/lid.176.bin");
    LanguageIdentifier ident = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(50, ngramDataFile, fastTextBinaryFile, fastTextModelFile);
    FastTextDetector failingFastTextDetector = spy(new FastTextDetector(fastTextModelFile, fastTextBinaryFile));
    doThrow(new FastTextDetector.FastTextException("Fasttext failure", true)).when(failingFastTextDetector).runFasttext(anyString(), any());
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked1 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked1);
    assertEquals("+fallback", langWithMocked1.getDetectionSource());
    assertEquals("de", langWithMocked1.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    
    DetectedLanguage langWithoutMocked1 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithoutMocked1);
    assertEquals("fasttext", langWithoutMocked1.getDetectionSource());
    assertEquals("de", langWithoutMocked1.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked2 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked2);
    assertEquals("+fallback", langWithMocked2.getDetectionSource());
    assertEquals("de", langWithMocked2.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked3 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked3);
    assertEquals("+fallback", langWithMocked3.getDetectionSource());
    assertEquals("de", langWithMocked3.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked4 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked4);
    assertEquals("+fallback", langWithMocked4.getDetectionSource());
    assertEquals("de", langWithMocked4.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked5 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked5);
    assertEquals("+fallback", langWithMocked5.getDetectionSource());
    assertEquals("de", langWithMocked5.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked6 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked6);
    assertEquals("+fallback", langWithMocked6.getDetectionSource());
    assertEquals("de", langWithMocked6.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked7 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked7);
    assertEquals("+fallback", langWithMocked7.getDetectionSource());
    assertEquals("de", langWithMocked7.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked8 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked8);
    assertEquals("+fallback", langWithMocked8.getDetectionSource());
    assertEquals("de", langWithMocked8.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked9 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked9);
    assertEquals("+fallback", langWithMocked9.getDetectionSource());
    assertEquals("de", langWithMocked9.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked10 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked10);
    assertEquals("+fallback", langWithMocked10.getDetectionSource());
    assertEquals("de", langWithMocked10.getDetectedLanguage().getShortCode());
    assertTrue(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);
    DetectedLanguage langWithMocked11 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithMocked11);
    assertEquals("+fallback", langWithMocked11.getDetectionSource());
    assertEquals("de", langWithMocked11.getDetectedLanguage().getShortCode());
    assertFalse(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
    Thread.sleep(50);
    
    DetectedLanguage langWithoutMocked2 = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
    assertNotNull(langWithoutMocked2);
    assertEquals("ngram", langWithoutMocked2.getDetectionSource());
    assertEquals("de", langWithoutMocked2.getDetectedLanguage().getShortCode());
    assertFalse(((DefaultLanguageIdentifier) ident).isFastTextEnabled());
  }
  
  @Test
  @Ignore("Requires local files to run")
  public void fasttextReinitMultiThreadedTest() throws IOException, InterruptedException {
    File ngramDataFile = new File("/home/stefan/Dokumente/languagetool/data/model_ml50_new.zip");
    File fastTextBinaryFile = new File("/home/stefan/Dokumente/languagetool/data/fasttext/fasttext");
    File fastTextModelFile = new File("/home/stefan/Dokumente/languagetool/data/fasttext/lid.176.bin");
    LanguageIdentifier ident = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(50, ngramDataFile, fastTextBinaryFile, fastTextModelFile);
    FastTextDetector failingFastTextDetector = spy(new FastTextDetector(fastTextModelFile, fastTextBinaryFile));
    doThrow(new FastTextDetector.FastTextException("Fasttext failure", true)).when(failingFastTextDetector).runFasttext(anyString(), any());
    ((DefaultLanguageIdentifier) ident).setFastTextDetector(failingFastTextDetector);

    ExecutorService executorService = Executors.newFixedThreadPool(32);
    List<Callable<DetectedLanguage>> tasks = new ArrayList<>();
    for (int i = 0; i < 32; i++) {
      Callable<DetectedLanguage> callable = () -> {
        DetectedLanguage dl = ident.detectLanguage("LanguageTool prüft einen Satz nicht auf grammatikalische Korrektheit, sondern, ob er typische Fehler enthält. Daher ist es einfach, ungrammatikalische Sätze zu erfinden, die LanguageTool trotzdem akzeptiert. Die Fehlererkennung gelingt mit einer Vielzahl von Regeln, die auf XML basieren oder in Java geschrieben sind.", Collections.emptyList(), Collections.emptyList());
        return dl;
      };
      tasks.add(callable);
    }
    List<Future<DetectedLanguage>> futures = executorService.invokeAll(tasks);
    executorService.awaitTermination(10, TimeUnit.SECONDS);
    assertEquals(1, ((DefaultLanguageIdentifier) ident).getFasttextInitCounter().get());
  }
  
  @Test
  public void testDetection() {
    LanguageIdentifier identifier = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, null, null);
    langAssert("de", "Das ist ein deutscher Text", identifier);
    langAssert("en", "This is an English text", identifier);
    langAssert("fr", "Le mont Revard est un sommet du département français ...", identifier);
    // some test sentences from the "Linux" article of Wikipedia:
    langAssert("be", "Першапачаткова Linux распрацоўваўся і выкарыстоўваўся асобнымі аматарамі на сваіх персанальных камп'ютарах.", identifier);
    langAssert("ca", "Aquest sistema operatiu va créixer gràcies al treball col·laboratiu de programadors de tot el món ...", identifier);
    langAssert("zh", "Linux最初是作为支持英特尔x86架构的个人电脑的一个自由操作系统。目前Linux已经被移植到更多的计算机硬件平台", identifier);
    langAssert("da", "Linux-distributionerne har traditionelt deres største udbredelse på servere, men er hastigt på vej på almindelige pc'er.", identifier);
    langAssert("nl", "Linux is een verzameling van open source Unix-achtige besturingssystemen gebaseerd op de POSIX-standaard.", identifier);
    langAssert("el", "Το Linux μπορεί να εγκατασταθεί και να λειτουργήσει σε μεγάλη ποικιλία υπολογιστικών συστημάτων, από μικρές συσκευές όπως κινητά τηλέφωνα ...", identifier);
    //langAssert("is", "Linux er frjáls stýrikerfiskjarni sem Linus Torvalds byrjaði að skrifa árið 1991, ...");
    langAssert("it", "Grazie alla portabilità del kernel Linux sono stati sviluppati sistemi operativi Linux per un'ampia gamma di dispositivi:", identifier);
    langAssert("ja", "Linuxは、狭義ではLinuxカーネルを意味し、広義ではそれをカーネルとして用いたオペレーティングシステム (OS) を意味する。", identifier);
    langAssert("km", "បច្ចុប្បន្នគ្មានអត្ថបទក្នុងទំព័រនេះទេ។អ្នកអាច ស្វែងរក​ចំណងជើង​នៃទំព័រនេះក្នុងទំព័រដទៃទៀត​​ ឬ ស្វែង​រក​កំណត់​ហេតុ​ដែល​ពាក់ព័ន្ធ ឬ កែប្រែ​ទំព័រនេះ", identifier);
    //langAssert("lt", "Linux – laisvos (atviro kodo) operacinės sistemos branduolio (kernel) pavadinimas.");
    //langAssert("ml", "വളരെ പ്രശസ്തമായ ഒരു ഓപ്പറേറ്റിംഗ് സിസ്റ്റമാണ് ഗ്നു/ലിനക്സ് (ആംഗലേയം:GNU/Linux).");
    langAssert("fa", "این صفحه حذف شده\u200Cاست. در زیر سیاههٔ حذف و انتقال این صفحه نمایش داده شده\u200Cاست.", identifier);
    langAssert("pl", "Linux – rodzina uniksopodobnych systemów operacyjnych opartych na jądrze Linux.", identifier);
    langAssert("pt", "Linux é um termo utilizado para se referir a sistemas operativos ou sistemas operacionais que utilizem o núcleo Linux.", identifier);
    langAssert("ro", "Linux este o familie de sisteme de operare de tip Unix care folosesc Nucleul Linux (în engleză kernel).", identifier);
    langAssert("ru", "Linux, также Ли́нукс — общее название Unix-подобных операционных систем, основанных на одноимённом ядре.", identifier);
    langAssert("sk", "Linux je počítačový operačný systém a jeho jadro.", identifier);
    langAssert("sl", "Linux je prost operacijski sistem podoben Unixu s prosto dostopno izvorno kodo ...", identifier);
    langAssert("es", "GNU/Linux es uno de los términos empleados para referirse a la combinación del núcleo o kernel libre ...", identifier);
    langAssert("sv", "Linux eller GNU/Linux är ett Unix-liknande operativsystem som till största delen", identifier);
    langAssert("tl", "Ang Linux ay isang operating system kernel para sa mga operating system na humahalintulad sa Unix.", identifier);
    langAssert("ta", "Linux பற்றி பிற கட்டுரைகளில் தேடிப்பாருங்கள்.", identifier);
    langAssert("uk", "Лі́нукс — загальна назва UNIX-подібних операційних систем на основі однойменного ядра.", identifier);
    langAssert("km", "អ្នក\u200Bអាច\u200Bជួយ\u200Bលើក\u200Bស្ទួយ\u200Bវិគីភីឌាភាសាខ្មែរ\u200Bនេះ\u200Bឱ្យ\u200Bមាន\u200Bលក្ខណៈ", identifier);
    // not yet in language-detector 0.5:
    langAssert("eo", "Imperiestraj pingvenoj manĝas ĉefe krustacojn kaj malgrandajn ...", identifier);
    // detected as not supported by the unicode characters used:
    langAssert("zz", "ลินุกซ์ (อังกฤษ: Linux)", identifier);  // Thai
    langAssert("zz", "यूएसबी (अंग्रेज़ी: Live ...)", identifier);  // Hindi
    langAssert("zz", "लिनक्स (इंग्लिश: Linux)", identifier);  // Marathi
  }

  @Test
  @Ignore("disabled minimum length, instead now providing confidence score")
  public void testShortAndLongText() {
    LanguageIdentifier id10 = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(10, null, null, null);
    langAssert(null, "Das ist so ein Text, mit dem man testen kann", id10);  // too short when max length is applied
    langAssert(null, "012345678", id10);
    langAssert(null, "0123456789", id10);
    langAssert(null, "0123456789A", id10);
    langAssert(null, "0123456789AB", id10);
    langAssert(null, "0123456789ABC", id10);
    LanguageIdentifier id20 = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(20, null, null, null);
    langAssert("de", "Das ist so ein Text, mit dem man testen kann", id20);
  }
  
  @Test
  public void testKnownLimitations() {
    LanguageIdentifier identifier = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, null, null);
    // wrong, but low probabilities
    //identifier.enableFasttext(new File(fastTextBinary), new File(fastTextModel));
    // fasttext just assumes english, ignore / comment out
    langAssert(null, "", identifier);
    langAssert("ca", "X", identifier);

    // not activated because it impairs detection of Spanish, so ast and gl may be mis-detected:
    langAssert("es", "L'Iberorrománicu o Iberromance ye un subgrupu de llingües romances que posiblemente ...", identifier);  // ast
    langAssert("es", "Dodro é un concello da provincia da Coruña pertencente á comarca do Sar ...", identifier);  // gl
    // Somali, known by language-detector, but not by LT, so we get back something else:
    langAssert("tl", "Dhammaan garoomada caalamka ayaa loo isticmaalaa. Ururku waxa uu qabtaa ama uu ku shaqaleeyahay " +
            "isusocodka diyaaradaha aduunka ee iskaga gooshaya xuduudaha iyo ka hortagga wixii qalad ah iyo baaritaanka " +
            "marka ay dhacdo dhibaato la xiriirta dulimaad.", identifier);
  }

  @Test
  public void testIgnoreSignature() {
    LanguageIdentifier identifier = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, null, null);
    langAssert("de", "Das ist ein deutscher Text\n-- \nBut this is an\nEnglish text in the signature, and it's much longer than the original text.", identifier);
    langAssert("en", "This is an English text.\n-- \nDas ist ein\ndeutscher Text in der Signatur, der länger ist als der Haupttext.", identifier);
  }

  @Test
  @Ignore("Only works with locally installed fastText")
  public void testAdditionalLanguagesFasttext() {
    LanguageIdentifier defaultIdent = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, null, null);
    langAssert("sk", czech, defaultIdent);  // misdetected, as cz isn't supported by LT

    LanguageIdentifier csIdent = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, new File(fastTextBinary), new File(fastTextModel));
    langAssert("zz", czech, csIdent, Arrays.asList("cs"), Collections.emptyList());   // the no-op language
  }

  @Test
  @Ignore("Only works with locally installed fastText, no test - for interactive use")
  public void testInteractively() {
    LanguageIdentifier ident = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(
                    0,
                    null,
                    new File(fastTextBinary),
                    new File(fastTextModel));
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
      //DetectedLanguage lang = ident.detectLanguageWithDetails(input);
      DetectedLanguage lang = ident.detectLanguage(input, Collections.emptyList(), Collections.emptyList());
      System.out.println("Input     : " + input);
      System.out.println("Language  : " + lang.getDetectedLanguage());
      System.out.println("confidence: " + lang.getDetectionConfidence());
      System.out.println();
    }
  }

  @Test
  @Ignore("Only works with locally installed fastText")
  public void testShortTexts() {
    LanguageIdentifier defaultIdent = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, new File(fastTextBinary), new File(fastTextModel));
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
    LanguageIdentifier ident = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, new File(fastTextBinary), new File(fastTextModel));
    List<String> enDePreferred = Arrays.asList("de", "en");
    List<String> noop = Arrays.asList();

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
    LanguageIdentifier defaultIdent = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, null, null);
    langAssert("sk", czech, defaultIdent);  // misdetected, as cz isn't supported by LT
    LanguageIdentifier csIdent = LanguageIdentifierService.INSTANCE
            .clearLanguageIdentifier("default")
            .getDefaultLanguageIdentifier(0, null, null, null);
    langAssert("sk", czech, csIdent, Arrays.asList("cs"), Collections.emptyList());   // no-op language only supported by fastText 
  }
 
  /*
  private void langAssert(String expectedLangCode, String text) {
    langAssert(expectedLangCode, text, identifier, Collections.emptyList(), Collections.emptyList());
  }
   */
   

}
