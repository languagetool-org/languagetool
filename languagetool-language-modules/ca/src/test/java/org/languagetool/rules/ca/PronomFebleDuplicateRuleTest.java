package org.languagetool.rules.ca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

public class PronomFebleDuplicateRuleTest {
  private PronomFebleDuplicateRule rule;
  private JLanguageTool lt;

  @BeforeEach
  public void setUp() throws IOException {
    rule = new PronomFebleDuplicateRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException { 
    
    assertCorrect("N'hi ha d'haver.");
    assertCorrect("Hi podria haver un error.");
    assertCorrect("Es divertien llançant-se pedres.");
    assertCorrect("Es recomana tapar-se la boca.");
    assertCorrect("S'ordena dutxar-se cada dia.");
    assertCorrect("Es va quedar barallant-se amb el seu amic.");
    assertCorrect("Es va quedar se");
    
    assertCorrect("M’encantava enfilar-me");
    assertCorrect("t'obliguen a penjar-te");
    assertCorrect("ens permeten endinsar-nos");
    assertCorrect("els llepaven fins a donar-los");
    assertCorrect("em fa doblegar fins a tocar-me");
    assertCorrect("la batalla per defensar-la");
    assertCorrect("ens convida a treure'ns-la");
    assertCorrect("ens ve a buscar per ajudar-nos");
    assertCorrect("et fan adonar-te");
    assertCorrect("m'agrada enfonsar-me");
    assertCorrect("em dedico a fer-me");
    assertCorrect("la mira sense veure-la");
    assertCorrect("l'havia podat fins a a deixar-lo");
    assertCorrect("em costava deixar-me anar");
    assertCorrect("m'obliga a allunyar-me");
    assertCorrect("el papà havia de canviar-lo");
    assertCorrect("ens congregava per assabentar-nos");
    assertCorrect("es podia morir de taponar-se-li");
    assertCorrect("l’hagin preservada sense tocar-la");
    assertCorrect("li impedeixi aconseguir-la");
    assertCorrect("us he fet venir per llevar-vos");
    assertCorrect("ajuda'm a alçar-me");
    assertCorrect("l'esperava per agrair-li");
    assertCorrect("els va empènyer a adreçar-li");
    assertCorrect("em vaig oblidar de rentar-me");
    assertCorrect("ens ajudà a animar-nos");
    assertCorrect("l'encalçava sense poder atrapar-la");
    assertCorrect("em manava barrejar-me");
    assertCorrect("el convidà a obrir-los");
    assertCorrect("es disposava a despullar-se");
    assertCorrect("es mudà per dirigir-se");
    assertCorrect("li va costar d'aconseguir tenir-lo");
    assertCorrect("es va poder estar d'atansar-s'hi");
    assertCorrect("el dissuadeixi de matar-lo");
    assertCorrect("la va festejar per engalipar-la");
    assertCorrect("s'havia negat a casar-s'hi");
    assertCorrect("es disposaven a envolar-se");
    assertCorrect("li sabia d'haver-la repudiada");
    assertCorrect("li sabia greu d'haver-la repudiada");
    assertCorrect("el féu acostar per besar-li");
    assertCorrect("En acostar-se va fer-se això.");

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("S'ha de fer-se"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("Ha de fer-se", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("S'ha de fer", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("N'ha d'haver-hi"));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("Hi ha d'haver-ne"));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("Es va continuar barallant-se amb el seu amic."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("Va continuar barallant-se", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("Es va continuar barallant", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("Hi podria haver-hi"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("Podria haver-hi", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("Hi podria haver", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("ho puc arreglar-ho"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("puc arreglar-ho", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("ho puc arreglar", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("La volia veure-la."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("Volia veure-la", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("La volia veure", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("En vaig portar-ne quatre."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("Vaig portar-ne", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("En vaig portar", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("Ho hem hagut de fer-ho."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("Hem hagut de fer-ho", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("Ho hem hagut de fer", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("Hi hem hagut de continuar anant-hi."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("Hem hagut de continuar anant-hi", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("Hi hem hagut de continuar anant", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("M'he de rentar-me les dents."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("He de rentar-me", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("M'he de rentar", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("Et deu enganyar-te."));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("Et deu voler enganyar-te."));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("Et deu haver de dir-te."));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("Ho deu continuar dient-ho."));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("S'està rebel·lant-se."));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("Li va començar a dur-li problemes."));
    Assertions.assertEquals(1, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("S'acabarà carregant-se."));
    Assertions.assertEquals(1, matches.length);
    
  }
    
    private void assertCorrect(String sentence) throws IOException {
      final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
      Assertions.assertEquals(0, matches.length);
    }

}
