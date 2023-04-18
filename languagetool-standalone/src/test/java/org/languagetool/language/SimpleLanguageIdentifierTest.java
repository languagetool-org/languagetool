package org.languagetool.language;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;

public class SimpleLanguageIdentifierTest extends LanguageIdentifierTest {

  private LanguageIdentifier identifier = LanguageIdentifierService.INSTANCE
          .clearLanguageIdentifier("simple")
          .getSimpleLanguageIdentifier(null);

  @Test
  public void testDetection() {
    langAssert("de", "Das ist ein deutscher Text", identifier);
    langAssert("en", "This is an English text", identifier);
    langAssert("fr", "Le mont Revard est un sommet du département français ...", identifier);
    // some test sentences from the "Linux" article of Wikipedia:
    langAssert("be", "Першапачаткова Linux распрацоўваўся і выкарыстоўваўся асобнымі аматарамі на сваіх персанальных камп'ютарах.", identifier);
    langAssert("ca", "Aquest sistema operatiu va créixer gràcies al treball col·laboratiu de programadors de tot el món ...", identifier);
    //Unable to detect
    //langAssert("zh", "Linux最初是作为支持英特尔x86架构的个人电脑的一个自由操作系统。目前Linux已经被移植到更多的计算机硬件平台", identifier);
    langAssert("da", "Linux-distributionerne har traditionelt deres største udbredelse på servere, men er hastigt på vej på almindelige pc'er.", identifier);
    langAssert("nl", "Linux is een verzameling van open source Unix-achtige besturingssystemen gebaseerd op de POSIX-standaard.", identifier);
    langAssert("el", "Το Linux μπορεί να εγκατασταθεί και να λειτουργήσει σε μεγάλη ποικιλία υπολογιστικών συστημάτων, από μικρές συσκευές όπως κινητά τηλέφωνα ...", identifier);
    //langAssert("is", "Linux er frjáls stýrikerfiskjarni sem Linus Torvalds byrjaði að skrifa árið 1991, ...");
    langAssert("it", "Grazie alla portabilità del kernel Linux sono stati sviluppati sistemi operativi Linux per un'ampia gamma di dispositivi:", identifier);
    //Unable to detect
    //langAssert("ja", "Linuxは、狭義ではLinuxカーネルを意味し、広義ではそれをカーネルとして用いたオペレーティングシステム (OS) を意味する。", identifier);
    langAssert("km", "បច្ចុប្បន្នគ្មានអត្ថបទក្នុងទំព័រនេះទេ។អ្នកអាច ស្វែងរក​ចំណងជើង​នៃទំព័រនេះក្នុងទំព័រដទៃទៀត​​ ឬ ស្វែង​រក​កំណត់​ហេតុ​ដែល​ពាក់ព័ន្ធ ឬ កែប្រែ​ទំព័រនេះ", identifier);
    //langAssert("lt", "Linux – laisvos (atviro kodo) operacinės sistemos branduolio (kernel) pavadinimas.");
    //langAssert("ml", "വളരെ പ്രശസ്തമായ ഒരു ഓപ്പറേറ്റിംഗ് സിസ്റ്റമാണ് ഗ്നു/ലിനക്സ് (ആംഗലേയം:GNU/Linux).");
    langAssert("fa", "این صفحه حذف شده\u200Cاست. در زیر سیاههٔ حذف و انتقال این صفحه نمایش داده شده\u200Cاست.", identifier);
    //Unable to detect
    //langAssert("pl", "Linux – rodzina uniksopodobnych systemów operacyjnych opartych na jądrze Linux.", identifier);
    langAssert("pt", "Linux é um termo utilizado para se referir a sistemas operativos ou sistemas operacionais que utilizem o núcleo Linux.", identifier);
    langAssert("ro", "Linux este o familie de sisteme de operare de tip Unix care folosesc Nucleul Linux (în engleză kernel).", identifier);
    langAssert("ru", "Linux, также Ли́нукс — общее название Unix-подобных операционных систем, основанных на одноимённом ядре.", identifier);
    langAssert("sk", "Linux je počítačový operačný systém a jeho jadro.", identifier);
    langAssert("sl", "Linux je prost operacijski sistem podoben Unixu s prosto dostopno izvorno kodo ...", identifier);
    langAssert("es", "GNU/Linux es uno de los términos empleados para referirse a la combinación del núcleo o kernel libre ...", identifier);
    langAssert("sv", "Linux eller GNU/Linux är ett Unix-liknande operativsystem som till största delen", identifier);
    langAssert("tl", "Ang Linux ay isang operating system kernel para sa mga operating system na humahalintulad sa Unix.", identifier);
    langAssert("ta", "Linux பற்றி பிற கட்டுரைகளில் தேடிப்பாருங்கள்.", identifier);
    //Unable to detect
    //langAssert("uk", "Лі́нукс — загальна назва UNIX-подібних операційних систем на основі однойменного ядра.", identifier);
    langAssert("km", "អ្នក\u200Bអាច\u200Bជួយ\u200Bលើក\u200Bស្ទួយ\u200Bវិគីភីឌាភាសាខ្មែរ\u200Bនេះ\u200Bឱ្យ\u200Bមាន\u200Bលក្ខណៈ", identifier);
    // not yet in language-detector 0.5:
    langAssert("eo", "Imperiestraj pingvenoj manĝas ĉefe krustacojn kaj malgrandajn ...", identifier);
    // detected as not supported by the unicode characters used:
    langAssert("zz", "ลินุกซ์ (อังกฤษ: Linux)", identifier);  // Thai
    langAssert("zz", "यूएसबी (अंग्रेज़ी: Live ...)", identifier);  // Hindi
    langAssert("zz", "लिनक्स (इंग्लिश: Linux)", identifier);  // Marathi
  }

  @Test
  public void testShortTexts() {
    langAssert("en", "If the", identifier);
    langAssert("en", "if the man", identifier);
    langAssert("en", "Paste text", identifier);
    langAssert("de", "Sehr leckere Sourcreme", identifier);
    langAssert("de", "Die Menschen in den öst", identifier);
    langAssert("de", "Den Vogel ", identifier);
    langAssert("de", "Den Eisenbahner-Esperanto-Kongress im", identifier);
    langAssert("uk", "Зараз десь когось нема", identifier);
    langAssert("da", "En to meter lang levende krokodille er blevet fundet i et drivhus i en have i Sveriges tredje største by", identifier);
    langAssert("da", "Elektriske lamper, gemt bag et loft af mælkehvidt, gennemskinneligt glas, kastede et mildt lys på museets skatt", identifier);
    langAssert("de", "Am 31. September", identifier);
    langAssert("es", "Colores", identifier);
    langAssert("fr", "Fermer", identifier);
    langAssert("es", "Cerrar", identifier);
    langAssert("nl", "Lettertypes", identifier);
    //langAssert("it", "Brand in arrivo!", identifier);
    langAssert("de", "Alles was Tom woll", identifier);
    langAssert("nl", "Bewerk lettertypen", identifier);
    langAssert("en", "Die in peace", identifier);
    //langAssert("ja", "一体日本人は生きるということを知っているだろうか。小学校の門を潜ってから", identifier);   // see https://github.com/languagetool-org/languagetool/issues/1278
  }
}
