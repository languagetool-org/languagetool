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
package org.languagetool.dev;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.fail;

/**
 * Test for multiple languages in multiple threads.
 */
public class MultiThreadingTest1 {

  private static final int THREADS = 10;
  private static final int RUNS = 50;
  private static final Map<String,String> examples = new HashMap<>();
  static {
    // Examples copied from languagetool.org:
    examples.put("de-DE", "oder nutzen Sie diesen Text als Beispiel für ein Paar Fehler , die LanguageTool erkennen kann: Ihm wurde Angst und bange, als er davon hörte. ( Eine Rechtschreibprüfun findet findet übrigens auch statt.");
    examples.put("en-GB", "Paste your own text here and click the 'Check Text' button. Click the colored phrases for details on potential errors. or use this text too see an few of of the problems that LanguageTool can detecd. What do you thinks of grammar checkers? Please not that they are not perfect.");
    examples.put("en-US", "Paste your own text here and click the 'Check Text' button. Click the colored phrases for details on potential errors. or use this text too see an few of of the problems that LanguageTool can detecd. What do you thinks of grammar checkers? Please not that they are not perfect.");
    examples.put("ast-ES", "Apega testu equí. o revisa toes les pallabres de esti testu pa ver dalgún de los problemis que LanguageTool ye pa deteutar. ¿Afáyeste con los correutores gramaticales? Has date cuenta de que entá nun son perfeutos.");
    examples.put("be-BY", "Паспрабуйце напісаць нейкі тэкст з памылкамі, а LanguageTool паспрабуе паказаць нейкия найбольш распаусюджаныя памылки.");
    examples.put("br-FR", "Lakait amañ ho testenn vrezhonek da vezañ gwiriet. Pe implijit an frazenn-mañ gant meur a fazioù yezhadurel.");
    examples.put("ca-ES", "Introduïu açí el vostre text. o feu servir aquest texts com a a exemple per a alguns errades que LanguageTool hi pot detectat.");
    examples.put("zh-CN", "将文本粘贴在此，或者检测以下文本：我和她去看了二部电影。");
    examples.put("da-DK", "Indsæt din egen tekst her , eller brug denne tekst til at se nogle af de fejl LanguageTool fanger. vær opmærksom på at den langtfra er er perfect, men skal være en hjælp til at få standartfejl frem i lyset.");
    examples.put("nl", "Een ieder kan fouten maken, tikvouten bij voorbeeld.");
    examples.put("eo", "Alglui vian kontrolendan tekston ĉi tie... Aŭ nur kontrolu tiun ekzemplon. Ĉu vi vi rimarkis, ke estas gramatikaj eraro en tiu frazo? Rimarku, ke Lingvoilo ankaux atentigas pri literumaj erraroj kiel ĉi-tiu.");
    examples.put("fr", "Copiez votre texte ici ou vérifiez cet exemple contenant plusieurs erreur que LanguageTool doit doit pouvoir detecter.");
    examples.put("gl-ES", "Esta vai a ser unha mostra de de exemplo para amosar o funcionamento de LanguageTool.");
    examples.put("is-IS", "Þetta er dæmi um texta sem á að sína farm á hvernig LanguageTool virkar. Það er þó hérmeð gert ljóst að forritið framkvæmir ekki hefðbundna ritvilluleit.");
    examples.put("km-KH", "ឃ្លា​នេះ​បង្ហាញ​ពី​ពី​កំហុស​វេយ្យាករណ៍ ដើម្បី​បញ្ជាក់​ពី​ប្រសិទ្ធភាព​របស់​កម្មវិធី LanguageTool សំរាប់​ភាសាខ្មែរ។");
    examples.put("pl-PL", "Wpisz tekst lub użyj istniejącego przykładu. To jest przykładowy tekst który pokazuje, jak jak działa LanguageTool. LanguageTool ma jusz korektor psowni, który wyrurznia bledy na czewrono.");
    examples.put("it", "Inserite qui lo vostro testo... oppure controlate direttamente questo ed avrete un assaggio di quali errori possono essere identificati con LanguageTool.");
    examples.put("fa", "لطفا متن خود را اینجا قرار دهید . یا بررسی کنید که این متن را‌ برای دیدن بعضی بعضی از اشکال هایی که ابزار زبان توانسته تشخیس هدد. درباره ی نرم افزارهای بررسی کننده های گرامر چه فکر می کنید؟ لطفا در نظر داشته باشید که آن‌ها بی نقص نمی باشند.‎");
    examples.put("pt-PT", "Cola o teu próprio texto aqui... ou verifica este texto, afim de ver alguns dos dos problemas que o LanguageTool consegue detectar. Isto tal vez permita corrigir os teus erros à última da hora.");
    examples.put("ru-RU", "Вставьте ваш текст сюда .. или проверьте этот текстт.");
    examples.put("sk-SK", "Toto je ukážkový vstup, na predvedenie funkčnosti LanguageTool. Pamätajte si si, že neobsahuje \"kontrolu\" preklepo.");
    examples.put("es-ES", "Escriba un texto aquí. LanguageTool le ayudará a afrentar algunas dificultades propias de la escritura. Se a hecho un esfuerzo para detectar errores tipográficos, ortograficos y incluso gramaticales. También algunos errores de estilo, a grosso modo.");
    examples.put("ta-IN", "இந்த பெட்டியில் உங்கள் உரையை ஒட்டி சரிவர சோதிக்கிறதா என பாருங்கள். 'லேங்குவேஜ் டூல்' சில இலக்கணப் பிழைகளைச் சரியாக கண்டுபிடிக்கும். பல பிழைகளைப் பிடிக்க தடுமாறலாம்.");
    examples.put("tl-PH", "Ang LanguageTool ay maganda gamit sa araw-araw. Ang talatang ito ay nagpapakita ng ng kakayahan ng LanguageTool at hinahalimbawa kung paano ito gamitin. Litaw rin sa talatang ito na may mga bagaybagay na hindii pa kayang itama nng LanguageTool.");
    examples.put("uk-UA", "Будь-ласка, вставте тутт ваш текст, або перевірте цей текст на предмет помилок. Знайти всі помилки для LanguageTool є не по силах з багатьох причин але дещо він вам все таки підкаже. Порівняно з засобами перевірки орфографії LanguageTool також змайде граматичні та стильові проблеми. LanguageTool — ваш самий кращий помічник.");
  }

  private final Random rnd = new Random(1234);
  private final Map<String,String> expectedResults = new HashMap<>();

  @Test
  @Ignore("for interactive use only")
  public void test() throws Exception {
    List<Language> languages1 = new ArrayList<>(Languages.get());
    initExpectedResults(languages1);
    List<Language> languages2 = new ArrayList<>(Languages.get());
    ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    for (int i = 0; i < RUNS; i++) {
      System.out.println("Run #" + i);
      Collections.shuffle(languages1, rnd);
      Collections.shuffle(languages2, rnd);
      List<Future> futures = new ArrayList<>();
      for (int j = 0; j < languages1.size(); j++) {
        Language lang1 = languages1.get(j);
        Language lang2 = languages2.get(j);
        //System.out.println("Checking " + lang1 + " and " + lang2);
        futures.add(executor.submit(new Handler(lang1)));
        futures.add(executor.submit(new Handler(lang2)));
      }
      for (Future future : futures) {
        future.get();  // wait for all results or exception
      }
    }
  }

  private void initExpectedResults(List<Language> languages) throws IOException {
    for (Language lang : languages) {
      JLanguageTool lt = new JLanguageTool(lang);
      String input = examples.get(lang.getShortCodeWithCountryAndVariant());
      if (input != null) {
        List<RuleMatch> matches = lt.check(input);
        expectedResults.put(lang.getShortCodeWithCountryAndVariant(), toString(matches));
      }
    }
  }

  private static String toString(List<RuleMatch> matches) {
    List<String> result = new ArrayList<>();
    for (RuleMatch match : matches) {
      result.add(toString(match));
    }
    return StringUtils.join(result, ",");
  }

  private static String toString(RuleMatch match) {
    return match.getRule().getId() + "/"+ match.getFromPos() + "-" + match.getToPos();
  }

  class Handler implements Runnable {

    private final Language lang;

    Handler(Language lang) {
      this.lang = lang;
    }

    @Override
    public void run() {
      String input = examples.get(lang.getShortCodeWithCountryAndVariant());
      if (input != null) {
        try {
          JLanguageTool lt = new JLanguageTool(lang);
          //System.out.println("Running with " + lang.getShortNameWithCountryAndVariant());
          List<RuleMatch> matches = lt.check(input);
          //System.out.println("=>" + matches);
          String expected = expectedResults.get(lang.getShortCodeWithCountryAndVariant());
          String real = MultiThreadingTest1.toString(matches);
          if (!expectedResults.get(lang.getShortCodeWithCountryAndVariant()).equals(real)) {
            fail(lang + ": got '" + real + "', expected '" + expected + "'");
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        //System.out.println("Skipping " + lang.getShortNameWithCountryAndVariant());
      }
    }
  }

}
