/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.pl;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PolishTaggerTest {

  /**
   * Joined negations the dictionary build accepts but the tagger cannot reach,
   * because the form is also an inflected form of an unrelated lexeme and so is
   * never handed to {@link PolishTagger#additionalTags}, which only runs for
   * words nothing else matched.
   * <p>
   * Across the whole of polish.dict there are 39 such forms. The comparative
   * reading is restored for them in disambiguation.xml, by the rulegroup
   * NIE_STOPIEN_ZASLONIETY and the rule NIEMNIEJ_STOPIEN, which is also where
   * the frequent particle "niemniej" can be handled by context. So the forms
   * below are expected to stay incomplete at this level, and complete after
   * disambiguation.
   */
  private static final Set<String> KNOWN_MASKED = new HashSet<>(Arrays.asList(
      "niegodniej", "nieintratniej", "nieprzydatniej", "nieskładniej", "nieżywotniej"));

  private PolishTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new PolishTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Polish());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("To jest duży dom.",
        "To/[ten]adj:sg:acc:n1.n2:pos|To/[ten]adj:sg:nom.voc:n1.n2:pos|To/[to]conj|To/[to]qub|To/[to]subst:sg:acc:n2|To/[to]subst:sg:nom:n2 -- jest/[być]verb:fin:sg:ter:imperf:nonrefl -- duży/[duży]adj:sg:acc:m3:pos|duży/[duży]adj:sg:nom.voc:m1.m2.m3:pos -- dom/[dom]subst:sg:acc:m3|dom/[dom]subst:sg:nom:m3", tokenizer, tagger);
    TestTools.myAssert("Krowa pasie się na pastwisku.",
        "Krowa/[krowa]subst:sg:nom:f -- pasie/[pas]subst:sg:loc:m3|pasie/[pas]subst:sg:voc:m3|pasie/[paść]verb:fin:sg:ter:imperf:refl.nonrefl -- się/[się]qub|się/[się]siebie:acc:nakc|się/[się]siebie:gen:nakc -- na/[na]interj|na/[na]prep:acc|na/[na]prep:loc -- pastwisku/[pastwisko]subst:sg:dat:n2|pastwisku/[pastwisko]subst:sg:loc:n2", tokenizer, tagger);
    TestTools.myAssert("blablabla", "blablabla/[null]null", tokenizer, tagger);
  }

  /**
   * "nie" joined to a comparative or superlative, allowed since the 2026
   * spelling reform. These forms are not in the binary dictionary, so they are
   * analyzed by stripping the prefix; see {@link PolishTagger#additionalTags}.
   */
  @Test
  public void testJoinedNegation() throws IOException {
    // adverbs; the lemma is the negated positive form, as for "niepewien" -> "niepewny"
    TestTools.myAssert("nielepiej", "nielepiej/[niedobrze]adv:com", tokenizer, tagger);
    TestTools.myAssert("nienajlepiej", "nienajlepiej/[niedobrze]adv:sup", tokenizer, tagger);
    TestTools.myAssert("nieszybciej", "nieszybciej/[nieszybko]adv:com", tokenizer, tagger);
    // adjectives, in all their case/gender forms
    TestTools.myAssert("nietańszy",
        "nietańszy/[nietani]adj:sg:acc:m3:com|nietańszy/[nietani]adj:sg:nom.voc:m1.m2.m3:com",
        tokenizer, tagger);
    TestTools.myAssert("nielepsza", "nielepsza/[niedobry]adj:sg:nom.voc:f:com", tokenizer, tagger);
    TestTools.myAssert("nienajlepszy",
        "nienajlepszy/[niedobry]adj:sg:acc:m3:sup|nienajlepszy/[niedobry]adj:sg:nom.voc:m1.m2.m3:sup",
        tokenizer, tagger);
    // sentence-initial and all-uppercase
    TestTools.myAssert("Nielepiej", "Nielepiej/[niedobrze]adv:com", tokenizer, tagger);
    TestTools.myAssert("NIELEPIEJ", "NIELEPIEJ/[niedobrze]adv:com", tokenizer, tagger);

    // an imperative homograph the dictionary build rejected: "niemdlej" is a
    // misspelled "nie mdlej", not the comparative adverb of "mdło"
    TestTools.myAssert("niemdlej", "niemdlej/[null]null", tokenizer, tagger);
    TestTools.myAssert("niełysiej", "niełysiej/[null]null", tokenizer, tagger);
    // ... but a homograph whose adverb reading is the strong one stays admissible
    TestTools.myAssert("niegorzej", "niegorzej/[nieźle]adv:com", tokenizer, tagger);
    // banned by the spelling dictionary
    TestTools.myAssert("niebardziej", "niebardziej/[null]null", tokenizer, tagger);
    TestTools.myAssert("nienajbardziej", "nienajbardziej/[null]null", tokenizer, tagger);

    // the prefix is stripped once only, so this does not cascade
    TestTools.myAssert("nienielepiej", "nienielepiej/[null]null", tokenizer, tagger);

    // words that merely start with "nie" must keep their own reading, which
    // they do because the prefix is only tried when nothing else matched
    TestTools.myAssert("niemniej", "niemniej/[niemniej]qub", tokenizer, tagger);
    TestTools.myAssert("niedaleko", "niedaleko/[niedaleko]adv:pos", tokenizer, tagger);
  }

  /**
   * Checks the joined-negation analysis against the verdicts of the dictionary
   * build that produced the spelling dictionary, so that the tagger and the
   * speller cannot drift apart when either is regenerated. See the fixture's
   * header for its provenance.
   */
  @Test
  public void testJoinedNegationAgainstDictionaryVerdicts() throws IOException {
    Pattern comparSuperl = Pattern.compile("(adv|adj:.+):(com|sup)");
    List<String> missing = new ArrayList<>();
    List<String> masked = new ArrayList<>();
    List<String> unexpected = new ArrayList<>();
    int checked = 0;
    for (String[] row : readVerdicts()) {
      String joinedForm = row[0];
      String stem = row[1];
      boolean banned = "Y".equals(row[4]);
      String verdict = row[5];
      if (stem.startsWith("nie")) {
        // Double negations such as "nieniepozorniej". The prefix is stripped
        // once only, so the stem would itself have to be derived the same way.
        continue;
      }
      // The tagger can only produce a reading if the stem has one, so ask it
      // rather than trusting the tags recorded when the fixture was built.
      if (!hasReading(stem, comparSuperl, false)) {
        continue;
      }
      checked++;
      boolean tagged = hasReading(joinedForm, comparSuperl, true);
      if ("admissible".equals(verdict)) {
        if (tagged) {
          continue;
        }
        // The prefix analysis is a fallback for words nothing else matched, so
        // a joined form that happens to be an inflected form of some other
        // lexeme keeps only that reading: "niegodniej" is the feminine oblique
        // of "niegodni" in the dictionary, which masks the comparative adverb
        // of "niegodnie". Known and bounded; anything beyond this is a bug.
        (isTagged(joinedForm) ? masked : missing).add(joinedForm);
      } else if (banned || "exclude-typo".equals(verdict)) {
        // imperative homographs and spelling-dictionary bans: must stay untagged
        if (tagged) {
          unexpected.add(joinedForm);
        }
      }
      // The remaining "exclude" rows are merely corpus-unattested. They are
      // well formed under the reform and the speller rejects them anyway, so
      // the tagger deliberately does not exclude them; nothing to assert.
    }
    assertTrue("fixture did not yield enough testable rows: " + checked, checked > 2000);
    assertTrue("admissible joined forms left without any analysis: " + missing, missing.isEmpty());
    assertTrue("joined forms tagged although the dictionary build rejected them: " + unexpected,
        unexpected.isEmpty());
    assertTrue("more joined forms are masked by an unrelated dictionary reading than expected: "
        + masked, KNOWN_MASKED.containsAll(masked));
  }

  private boolean isTagged(String word) throws IOException {
    for (AnalyzedTokenReadings reading : tagger.tag(Collections.singletonList(word))) {
      if (reading.isTagged()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param requireNegatedLemma also require the reading's lemma to be a negated
   * one, i.e. to start with "nie"
   */
  private boolean hasReading(String word, Pattern posTag, boolean requireNegatedLemma) throws IOException {
    List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList(word));
    for (AnalyzedTokenReadings reading : readings) {
      for (AnalyzedToken token : reading.getReadings()) {
        if (token.getPOSTag() == null || !posTag.matcher(token.getPOSTag()).matches()) {
          continue;
        }
        if (!requireNegatedLemma
            || (token.getLemma() != null && token.getLemma().startsWith("nie"))) {
          return true;
        }
      }
    }
    return false;
  }

  private List<String[]> readVerdicts() throws IOException {
    List<String[]> rows = new ArrayList<>();
    try (InputStream stream = PolishTaggerTest.class.getResourceAsStream("nie_verdicts.tsv")) {
      if (stream == null) {
        fail("could not find nie_verdicts.tsv next to " + PolishTaggerTest.class.getName());
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      String line;
      boolean headerSeen = false;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        if (!headerSeen) {
          headerSeen = true;
          continue;
        }
        rows.add(line.split("\t", -1));
      }
    }
    assertFalse("nie_verdicts.tsv is empty", rows.isEmpty());
    return rows;
  }

}
