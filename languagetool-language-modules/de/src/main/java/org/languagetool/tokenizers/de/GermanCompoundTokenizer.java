/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.de;

import com.google.common.base.Suppliers;
import de.danielnaber.jwordsplitter.EmbeddedGermanDictionary;
import de.danielnaber.jwordsplitter.GermanWordSplitter;
import de.danielnaber.jwordsplitter.InputTooLongException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.languagetool.tokenizers.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

/**
 * Split German nouns using the jWordSplitter library.
 *
 * @author Daniel Naber
 */
public class GermanCompoundTokenizer implements Tokenizer {
  private static final Supplier<GermanCompoundTokenizer> strictInstance = Suppliers.memoize(() -> {
    try {
      return new GermanCompoundTokenizer(true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  });
  private static final Supplier<GermanCompoundTokenizer> nonStrictInstance = Suppliers.memoize(() -> {
    try {
      return new GermanCompoundTokenizer(false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  });

  private final ExtendedGermanWordSplitter wordSplitter;

  public GermanCompoundTokenizer() throws IOException {
    this(true);
  }

  static class ExtendedGermanWordSplitter extends GermanWordSplitter {
    ExtendedGermanWordSplitter(boolean hideInterfixCharacters) throws IOException {
      super(hideInterfixCharacters, extendedList());
    }
    static Set<String> extendedList() {
      ObjectOpenHashSet<String> words = new ObjectOpenHashSet<>(EmbeddedGermanDictionary.getWords());
      // Add compound parts here so we don't need to update JWordSplitter for every missing word we find.
      // Note: adding words, especially short ones, can also cause incorrect splits. E.g. if "sport"
      // is in the list and you add "tran", without "transport" being in the list, it would split "transport".
      words.add("influencer");
      words.add("katheterisierung");
      words.add("rücklass");
      words.add("abdichtung");
      words.add("laptop");
      words.add("verschattung");
      words.add("paradeiser");
      words.add("einreichung");
      words.add("bestatter");
      words.add("divergenz");
      words.add("schrumpf");
      words.add("degustation");
      words.add("schaft");
      words.add("abstreifer");
      words.add("aufputz");
      words.add("glühwürmchen");
      words.add("aufwertung");
      words.add("einhausung");
      words.add("lackier");
      words.add("zarge");
      words.add("pluralisierung");
      words.add("schanzen");
      words.add("abscheide");
      words.add("rangier");
      words.add("temporal");
      words.add("kartonage");
      words.add("kartonagen");
      words.add("rebellion");
      words.add("binokular");
      words.add("umverlegung");
      words.add("umhausung");
      words.add("überholung");
      words.add("chloroplasten");
      words.add("nachrangigkeit");
      words.add("spital");
      words.add("turnus");
      words.add("teilnehmenden");
      words.add("pensionisten");
      words.add("graduierten");
      words.add("beladung");
      words.add("controller");
      words.add("resilienz");
      words.add("mitführ");
      words.add("trauma");
      words.add("abtau");
      words.add("normung");
      words.add("mikroskopie");
      words.add("bitumen");
      words.add("erfolglosigkeit");
      words.add("pneumatik");
      words.add("anlasser");
      words.add("allozierung");
      words.add("alphabetisierung");
      words.add("aktuator");
      words.add("akademisierung");
      words.add("allergiker");
      words.add("queer");
      words.add("filament");
      words.add("querung");
      words.add("curling");
      words.add("opioid");
      words.add("booster");
      words.add("schmuse");
      words.add("thrombozyten");
      words.add("dysfunktion");
      words.add("storchen");
      words.add("nasch");
      words.add("esperanto");
      words.add("passivierung");
      words.add("radikalisierung");
      words.add("erleuchtung");
      words.add("verwalter");
      words.add("verbiss");
      words.add("ausleih");
      words.add("rutsch");
      words.add("kufen");
      words.add("entferner");
      words.add("debitoren");
      words.add("terrakotta");
      words.add("graffiti");
      words.add("auffahr");
      words.add("anmutung");
      words.add("kritzel");
      words.add("salami");
      words.add("eukalyptus");
      words.add("kreativ");
      words.add("hochvolt");
      words.add("trading");
      words.add("extraktion");
      words.add("verstetigung");
      words.add("diagonal");
      words.add("margen");
      words.add("synonym");
      words.add("aufbringung");
      words.add("robustheit");
      words.add("nachuntersuchung");
      words.add("erstkommunion");
      words.add("hauptstadt");
      words.add("neustart");
      words.add("polarisierung");
      words.add("vollstreckbarkeit");
      words.add("vollziehung");
      words.add("kasko");
      words.add("blitzableiter");
      words.add("abschattungen");
      words.add("kuscheltier");
      words.add("gastro");
      words.add("hortensien");
      words.trim();
      return words;
    }
  }

  public GermanCompoundTokenizer(boolean strictMode) throws IOException {
    wordSplitter = new ExtendedGermanWordSplitter(false);
    wordSplitter.setStrictMode(strictMode);
    wordSplitter.setMinimumWordLength(3);
    // add exceptions here so we don't need to update JWordSplitter for every exception we find:
    //wordSplitter.addException("Maskerade", Collections.singletonList("Maskerade"));
    //wordSplitter.addException("Sportshorts", asList("Sport", "shorts"));
    wordSplitter.addException("Absolventen", asList("Absolventen"));
    wordSplitter.addException("Acetat", asList("Acetat"));
    wordSplitter.addException("Alkoholabstinenz", asList("Alkohol", "abstinenz"));
    wordSplitter.addException("Androgen", asList("Androgen"));
    wordSplitter.addException("Auberginen", asList("Auberginen"));
    wordSplitter.addException("Auckland", asList("Auckland"));
    wordSplitter.addException("Boston", asList("Boston"));
    wordSplitter.addException("Brandenburg", asList("Brandenburg"));
    wordSplitter.addException("Broadcast", asList("Broadcast"));
    wordSplitter.addException("Buchsbaum", asList("Buchsbaum"));
    wordSplitter.addException("Chiemsee", asList("Chiemsee"));
    wordSplitter.addException("Coffein", asList("Coffein"));
    wordSplitter.addException("Drohnen", asList("Drohnen"));
    wordSplitter.addException("Eiben", asList("Eiben"));
    wordSplitter.addException("Eingroschen", asList("Eingroschen"));
    wordSplitter.addException("Einkomponenten", asList("Einkomponenten"));
    wordSplitter.addException("Elster", asList("Elster"));
    wordSplitter.addException("Engineering", asList("Engineering"));
    wordSplitter.addException("Factoring", asList("Factoring"));
    wordSplitter.addException("Flexodruck", asList("Flexo", "druck"));
    wordSplitter.addException("Graviton", asList("Graviton"));
    wordSplitter.addException("Göttinnen", asList("Göttinnen"));
    wordSplitter.addException("Hallesche", asList("Hallesche"));
    wordSplitter.addException("Hinspiel", asList("Hinspiel"));
    wordSplitter.addException("Homogen", asList("Homogen"));
    wordSplitter.addException("Kolleggen", asList("Kolleggen"));
    wordSplitter.addException("Karstadt", asList("Karstadt"));
    wordSplitter.addException("Kartier", asList("Kartier"));
    wordSplitter.addException("Kaukasus", asList("Kaukasus"));
    wordSplitter.addException("Knoblauch", asList("Knoblauch"));
    wordSplitter.addException("Kollagen", asList("Kollagen"));
    wordSplitter.addException("Kommerz", asList("Kommerz"));
    wordSplitter.addException("Mentoring", asList("Mentoring"));
    wordSplitter.addException("Monarchen", asList("Monarchen"));
    wordSplitter.addException("Oligarchen", asList("Oligarchen"));
    wordSplitter.addException("Optimal", asList("Optimal"));
    wordSplitter.addException("Saunieren", asList("Saunieren"));
    wordSplitter.addException("Schiessen", asList("Schiessen"));
    wordSplitter.addException("Spielgeleier", asList("Spielgeleier"));
    wordSplitter.addException("Halleschen", asList("Halleschen"));
    wordSplitter.addException("Reinigungstab", asList("Reinigungs", "tab"));
    wordSplitter.addException("Reinigungstabs", asList("Reinigungs", "tabs"));
    wordSplitter.addException("Tauschwerte", asList("Tausch", "werte"));
    wordSplitter.addException("Tauschwertes", asList("Tausch", "wertes"));
    wordSplitter.addException("Kinderspielen", asList("Kinder", "spielen"));
    wordSplitter.addException("Buchhaltungstrick", asList("Buchhaltungs", "trick"));
    wordSplitter.addException("Buchhaltungstricks", asList("Buchhaltungs", "tricks"));
    wordSplitter.addException("Haushaltstrick", asList("Haushalts", "trick"));
    wordSplitter.addException("Haushaltstricks", asList("Haushalts", "tricks"));
    wordSplitter.addException("Verkaufstrick", asList("Verkaufs", "trick"));
    wordSplitter.addException("Verkaufstricks", asList("Verkaufs", "tricks"));
    wordSplitter.addException("Ablenkungstrick", asList("Ablenkungs", "trick"));
    wordSplitter.addException("Ablenkungstricks", asList("Ablenkungs", "tricks"));
    wordSplitter.addException("Manipulationstrick", asList("Manipulations", "trick"));
    wordSplitter.addException("Manipulationstricks", asList("Manipulations", "tricks"));
    wordSplitter.addException("Erziehungstrick", asList("Erziehungs", "trick"));
    wordSplitter.addException("Erziehungstricks", asList("Erziehungs", "tricks"));
    wordSplitter.addException("Messetage", asList("Messe", "tage"));
    wordSplitter.addException("Messetagen", asList("Messe", "tagen"));
    wordSplitter.addException("karamelligen", asList("karamelligen"));  // != Karamel+Ligen
    wordSplitter.addException("Häkelnadel", asList("Häkel", "nadel"));
    wordSplitter.addException("Häkelnadeln", asList("Häkel", "nadeln"));
    wordSplitter.addException("Freiberg", asList("Freiberg"));
    wordSplitter.addException("Abtestat", asList("Abtestat"));
    wordSplitter.addException("Abtestaten", asList("Abtestaten"));
    wordSplitter.addException("Freibergs", asList("Freibergs"));
    wordSplitter.addException("Kreuzberg", asList("Kreuzberg"));
    wordSplitter.addException("Kreuzbergs", asList("Kreuzbergs"));
    wordSplitter.addException("Digitalisierung", asList("Digitalisierung"));
    wordSplitter.addException("Abtrocknung", asList("Abtrocknung"));
    wordSplitter.addException("Erlösung", asList("Erlösung"));
    wordSplitter.addException("Feuerung", asList("Feuerung"));
    wordSplitter.addException("Aktivierung", asList("Aktivierung"));
    wordSplitter.addException("Protokollierung", asList("Protokollierung"));
    wordSplitter.addException("Budgetierung", asList("Budgetierung"));
    wordSplitter.addException("Faltung", asList("Faltung"));
    wordSplitter.addException("Anhäufung", asList("Anhäufung"));
    wordSplitter.addException("Aufkohlung", asList("Aufkohlung"));
    wordSplitter.addException("Festigung", asList("Festigung"));
    wordSplitter.addException("Allerheiligen", asList("Allerheiligen"));
    wordSplitter.addException("Druckerpressen", asList("Drucker", "pressen"));
    wordSplitter.addException("Habitat", asList("Habitat"));
    wordSplitter.addException("Augarten", asList("Augarten"));
    wordSplitter.addException("Auszeit", asList("Auszeit"));
    wordSplitter.addException("Bewegtbild", asList("Bewegt", "bild"));
    wordSplitter.addException("Bigband", asList("Bigband"));
    wordSplitter.addException("Bisexuelle", asList("Bisexuelle"));
    wordSplitter.addException("Bisexuellen", asList("Bisexuellen"));
    wordSplitter.addException("Bunsenbrenner", asList("Bunsenbrenner"));  // avoid "Bunsenweisheit"
    wordSplitter.addException("Carbon", asList("Carbon"));
    wordSplitter.addException("Carsharing", asList("Carsharing"));
    wordSplitter.addException("Castor", asList("Castor"));
    wordSplitter.addException("Catering", asList("Catering"));
    wordSplitter.addException("Cholesterin", asList("Cholesterin"));
    wordSplitter.addException("Damast", asList("Damast"));
    wordSplitter.addException("Dispositiv", asList("Dispositiv"));
    wordSplitter.addException("Emittent", asList("Emittent"));
    wordSplitter.addException("Emittenten", asList("Emittenten"));
    wordSplitter.addException("Express", asList("Express"));
    wordSplitter.addException("Fairness", asList("Fairness"));
    wordSplitter.addException("Fiberglas", asList("Fiberglas"));  // avoid "Fiberthermometer"
    wordSplitter.addException("Globus", asList("Globus"));
    wordSplitter.addException("Göttinnen", asList("Göttinnen"));
    wordSplitter.addException("Illustration", asList("Illustration"));
    wordSplitter.addException("Muttertag", asList("Muttertag"));  // "Muttertag" needs infix s
    wordSplitter.addException("Muttertags", asList("Muttertags"));  // "Muttertag" needs infix s
    wordSplitter.addException("Patriarchen", asList("Patriarchen"));
    wordSplitter.addException("Phosgen", asList("Phosgen"));
    wordSplitter.addException("Vatertag", asList("Vatertag"));  // "Vatertag" needs infix s
    wordSplitter.addException("Vatertags", asList("Vatertags"));  // "Vatertag" needs infix s
    wordSplitter.addException("Vaterland", asList("Vaterland"));
    wordSplitter.addException("Vaterlands", asList("Vaterlands"));
    wordSplitter.addException("Wehrmacht", asList("Wehrmacht"));
    wordSplitter.addException("Wehrmachts", asList("Wehrmachts"));
  }

  @Override
  public List<String> tokenize(String word) {
    try {
      return wordSplitter.splitWord(word);
    } catch (InputTooLongException e) {
      return Collections.singletonList(word);
    }
  }

  public static GermanCompoundTokenizer getStrictInstance() {
    return strictInstance.get();
  }

  public static GermanCompoundTokenizer getNonStrictInstance() {
    return nonStrictInstance.get();
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage: " + GermanCompoundTokenizer.class.getSimpleName() + " <wordsToSplit... or file>");
      System.exit(1);
    }
    GermanCompoundTokenizer tokenizer = new GermanCompoundTokenizer();
    if (new File(args[0]).exists()) {
      System.out.println("Working on lines from " + args[0] + ":");
      List<String> lines = Files.readAllLines(Paths.get(args[0]));
      for (String line : lines) {
        System.out.println(tokenizer.tokenize(line));
      }
    } else {
      for (String arg : args) {
        System.out.println(tokenizer.tokenize(arg));
      }
    }
  }

}

