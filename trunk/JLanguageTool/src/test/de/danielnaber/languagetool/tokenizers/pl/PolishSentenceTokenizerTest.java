package de.danielnaber.languagetool.tokenizers.pl;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class PolishSentenceTokenizerTest extends TestCase {

  // accept \n as paragraph:
  private SentenceTokenizer stokenizer = new PolishSentenceTokenizer();
  // accept only \n\n as paragraph:
  private SentenceTokenizer stokenizer2 = new PolishSentenceTokenizer();
  
  
  public final void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);  
    stokenizer2.setSingleLineBreaksMarksParagraph(false);  
  }

  public final void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit(new String[] { "Dies ist ein Satz." });
    testSplit(new String[] { "Dies ist ein Satz. ", "Noch einer." });
    testSplit(new String[] { "Ein Satz! ", "Noch einer." });
    testSplit(new String[] { "Ein Satz... ", "Noch einer." });
    testSplit(new String[] { "Unter http://www.test.de gibt es eine Website." });
    testSplit(new String[] { "Das Schreiben ist auf den 3.10. datiert." });
    testSplit(new String[] { "Das Schreiben ist auf den 31.1. datiert." });
    testSplit(new String[] { "Das Schreiben ist auf den 3.10.2000 datiert." });

    testSplit(new String[] { "Heute ist der 13.12.2004." });
    testSplit(new String[] { "Dziś jest 13. rocznica powstania wąchockiego." });
    testSplit(new String[] { "To jest 1. wydanie." });
    testSplit(new String[] { "Es geht am 24.09. los." });
    testSplit(new String[] { "To jest np. ten debil spod jedynki." });
    testSplit(new String[] { "Das in Punkt 3.9.1 genannte Verhalten." });

    testSplit(new String[] { "To jest tzw. premier." });
    testSplit(new String[] { "Jarek kupił sobie kurteczkę, tj. strój Marka." });

    testSplit(new String[] { "Das ist,, also ob es bla." });
    testSplit(new String[] { "Das ist es.. ", "So geht es weiter." });

    testSplit(new String[] { "Das hier ist ein(!) Satz." });
    testSplit(new String[] { "Das hier ist ein(!!) Satz." });
    testSplit(new String[] { "Das hier ist ein(?) Satz." });
    testSplit(new String[] { "Das hier ist ein(???) Satz." });
    testSplit(new String[] { "Das hier ist ein(???) Satz." });

    testSplit(new String[] { "„Prezydent jest niemądry”. ",  "Tak wyszło." });
    testSplit(new String[] { "„Prezydent jest niemądry”, powiedział premier" });

    // TODO: derzeit unterscheiden wir nicht, ob nach dem Doppelpunkt ein
    // ganzer Satz kommt oder nicht:
    testSplit(new String[] { "Das war es: gar nichts." });
    testSplit(new String[] { "Das war es: Dies ist ein neuer Satz." });

    // incomplete sentences, need to work for on-thy-fly checking of texts:
    testSplit(new String[] { "Here's a" });
    testSplit(new String[] { "Here's a sentence. ", "And here's one that's not comp" });

    // Tests taken from LanguageTool's SentenceSplitterTest.py:
    testSplit(new String[] { "This is a sentence. " });
    testSplit(new String[] { "This is a sentence. ", "And this is another one." });
    testSplit(new String[] { "This is a sentence.", "Isn't it?", "Yes, it is." });
    
    testSplit(new String[] { "Don't split strings like U. S. A. either." });
    testSplit(new String[] { "Don't split... ", "Well you know. ", "Here comes more text." });
    testSplit(new String[] { "Don't split... well you know. ", "Here comes more text." });
    testSplit(new String[] { "The \".\" should not be a delimiter in quotes." });
    testSplit(new String[] { "\"Here he comes!\" she said." });
    testSplit(new String[] { "\"Here he comes!\", she said." });
    testSplit(new String[] { "\"Here he comes.\" ", "But this is another sentence." });
    testSplit(new String[] { "\"Here he comes!\". ", "That's what he said." });
    testSplit(new String[] { "The sentence ends here. ", "(Another sentence.)" });
    // known to fail:
    // testSplit(new String[]{"He won't. ", "Really."});
    testSplit(new String[] { "He won't go. ", "Really." });
    testSplit(new String[] { "He won't say no.", "Not really." });
    testSplit(new String[] { "He won't say No.", "Not really." });
    testSplit(new String[] { "This is it: a test." });
    // one/two returns = paragraph = new sentence:
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\n", "Really." }, stokenizer);
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\nReally." }, stokenizer2);
    // Missing space after sentence end:
    testSplit(new String[] { "James is from the Ireland!", "He lives in Spain now." });
    
    //from user bug reports:
    testSplit(new String[] { "Temperatura wody w systemie wynosi 30°C.", "W skład obiegu otwartego wchodzi zbiornik i armatura." });
    testSplit(new String[] { "Zabudowano kolumny o długości 45 m. ", "Woda z ujęcia jest dostarczana do zakładu." });     

    // From the abbreviation list:
    testSplit(new String[] { "Ks. Jankowski jest prof. teologii." });    
    testSplit(new String[] { "To wydarzyło się w 1939 r.", "To był burzliwy rok." });
    testSplit(new String[] { "Prezydent jest popierany przez 20 proc. społeczeństwa." });
    testSplit(new String[] { "Moje wystąpienie ma na celu zmobilizowanie zarządu partii do działań, które umożliwią uzyskanie 40 proc.", "Nie widzę dziś na scenie politycznej formacji, która lepiej by łączyła różne poglądy" });
    testSplit(new String[] {"To jest zmienna A.", "Zaś to jest zmienna B."});
    //SKROTY_BEZ_KROPKI in ENDABREVLIST
    testSplit(new String[] {"Mam już 20 mln.", "To powinno mi wystarczyć"});
    testSplit(new String[] {"Mam już 20 mln. buraków."});
    //ellipsis
    testSplit(new String[] { "Rytmem tej wiecznie przemijającej światowej egzystencji […] rytmem mesjańskiej natury jest szczęście." });
    //sic!
    testSplit(new String[] { "W gazecie napisali, że pasy (sic!) pogryzły człowieka." });
  }

  public final void testSplit(final String[] sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
