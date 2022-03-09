package org.languagetool.tokenizers.fr;

/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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


import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FrenchSentenceTokenizerTest {

  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(new French());

  @Test
  public final void testTokenize() {
    testSplit("Je suis Chris.");
    testSplit("Je suis Chris.");
    testSplit("Je suis Chris ?!");
    testSplit("Je suis Chris ?");
    testSplit("Je suis      Chris ?");
    testSplit("Je suis Chris...");
    testSplit("Je suis Chris ...");
    testSplit("Je suis Chris …");
    testSplit("Votre nom: Chris !");
    testSplit("Je suis (...) Chris");
    testSplit("Je suis Chris (Christopher?).");
    testSplit("Je suis Chris (Christopher ?).");
    testSplit("Je suis Chris (Christopher ?!).");
    testSplit("Je suis E. Macron de France.");
    testSplit("J'ai beaucoup d'amis (Tom, Lisa, ...).");
    testSplit("J'ai beaucoup d'amis (Tom, Lisa, ... ).");
    testSplit("J'ai beaucoup d'amis (Tom, Lisa, …).");
    testSplit("La fréquence des P.A. et le nombre de fibres recrutées.");
    testSplit("Mrs. America est une mini-série américaine créée par Dahvi Waller, diffusée depuis le 15 avril 2020 sur le site de VOD Hulu et la chaîne FX.");
    testSplit("Il travaille pour Tiffany & Co. à Paris.");
    testSplit("J'ai beaucoup d'amis (Tom, Lisa, ...) et je suis populaire !");
    testSplit("J'ai beaucoup d'amis (Tom, Lisa !) et je suis populaire !");
    testSplit("Ph.D. est un groupe de musique britannique.");
    testSplit("Google Inc. est une entreprise américaine");
    testSplit("Le discours de E. Philippe devrait nous éclairer (un peu, beaucoup, ...?) sur ce qui nous attend.");
    testSplit("Le discours de E. Philippe devrait nous éclairer (un peu, beaucoup, ...?) sur ce qui nous attend.");
    testSplit("Op. cit., op. cit.");
    testSplit("IVe siècle av. J.C. en architecture");
    testSplit("IVe\u00a0siècle\u00a0av.\u00a0J.C.\u00a0en\u00a0architecture");
    testSplit("IVe siècle av. J.-C. en architecture");
    testSplit("sa mort le 19 août 14 apr. J.-C.");
    testSplit("Je suis Chris.[4] ", "Je suis Chris."); 
    testSplit("Je suis Chris.[4]\u00a0", "Je suis Chris.");
    testSplit("gaffa.org");
    
    testSplit("Le discours de E. Philippe devrait nous éclairer (un peu, beaucoup, …?) sur ce qui nous attend.");
    // TODO:
    //testSplit("Le discours de E. Philippe devrait nous éclairer (un peu, beaucoup, … ?) sur ce qui nous attend.");

    // without nbsp
    testSplit("« Le film était bien ? » ", "« Il était énorme ! ", "J'ai eu mal au ventre tellement je me suis marré ! »");
    testSplit("Si « cf. » désigne l’abréviation de « confer »");
    // with nbsp
    testSplit("« Le film était bien ? » ", "« Il était énorme ! ", "J'ai eu mal au ventre tellement je me suis marré ! »");
    testSplit("Si « cf. » désigne l’abréviation de « confer »,"); 
    testSplit("Ça ne sert à rien de me dire « Salut, comment ça va ? » si tu n'as rien d'autre à dire.");
    testSplit("« Madame est dans sa chambre. » dit le serviteur.");
    testSplit("« L'État, c'est moi ! » dit le roi.");
   
    
    assertThat(stokenizer.tokenize("Je suis Chris. Comment allez vous ?").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris?   Comment allez vous ???").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris ! Comment allez vous ???").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris ? Comment allez vous ???").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris. comment allez vous").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris (...). comment allez vous").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris (la la la …). comment allez vous").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris (CHRISTOPHER!). Comment allez vous").size(), is(2));
    assertThat(stokenizer.tokenize("Je suis Chris... Comment allez vous.").size(), is(2));
    
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
