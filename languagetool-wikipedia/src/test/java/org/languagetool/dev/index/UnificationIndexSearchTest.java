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
package org.languagetool.dev.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.English;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.languagetool.dev.index.Lucene.FIELD_NAME;
import static org.languagetool.dev.index.Lucene.FIELD_NAME_LOWERCASE;

/**
 * End-to-end proof that a rule using {@code <unify>} is handled correctly by the
 * {@link Indexer}/{@link Searcher} pipeline. The relaxed Lucene query built by
 * {@link PatternRuleQueryBuilder} silently drops unified tokens, so correctness of
 * agreement relies entirely on the full LanguageTool re-check that {@link Searcher}
 * runs over every candidate document. These tests verify that claim (and pin the
 * one case where it breaks down).
 *
 * <p>The unification here keys on surface case ({@code startupper} vs.
 * {@code lowercase}), exactly like the {@code case_sensitivity} feature in the
 * {@code xx} demo grammar, so it does not depend on the English tagger and is fully
 * deterministic. Note that Lucene stores the original-case field value (only indexed
 * <em>terms</em> are lowercased), so the re-check sees the true case.</p>
 */
public class UnificationIndexSearchTest extends LuceneTestCase {

  private static final String CASE_UNIFICATION =
      "  <unification feature=\"case_sensitivity\">\n" +
      "    <equivalence type=\"startupper\"><token regexp=\"yes\">\\p{Lu}\\p{Ll}+</token></equivalence>\n" +
      "    <equivalence type=\"lowercase\"><token regexp=\"yes\">\\p{Ll}+</token></equivalence>\n" +
      "  </unification>\n";

  // A rule with a plain anchor token ("visited") plus a two-token <unify> block. The anchor makes
  // the relaxed query non-empty; the unify block only survives via the LT re-check.
  private static final String ANCHORED_UNIFY_RULE =
      "  <category id='TEST' name='Test'>\n" +
      "    <rule id='UNI_CASE' name='uni case'>\n" +
      "      <pattern>\n" +
      "        <token>visited</token>\n" +
      "        <unify>\n" +
      "          <feature id=\"case_sensitivity\"/>\n" +
      "          <token/>\n" +
      "          <token>York</token>\n" +
      "        </unify>\n" +
      "      </pattern>\n" +
      "      <message>Case unification test match.</message>\n" +
      "    </rule>\n" +
      "  </category>\n";

  private Language language;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    language = new English();
  }

  /**
   * The agreeing sentence ("visited New York" — both start-uppercase) must be returned, while the
   * non-agreeing one ("visited new York" — lowercase vs. start-uppercase) must not, even though both
   * are candidates for the relaxed query (both contain the anchor "visited"). This isolates the
   * unification re-check as the sole deciding factor.
   */
  public void testUnificationMatchesThroughPipeline() throws Exception {
    PatternRule rule = (PatternRule) loadRules(CASE_UNIFICATION + ANCHORED_UNIFY_RULE).get(0);

    String agreeing = "I visited New York last year.";
    String notAgreeing = "I visited new York last year.";

    // Sanity: the rule itself matches the agreeing sentence and not the other, independent of Lucene.
    JLanguageTool lt = new JLanguageTool(language);
    lt.getAllActiveRules().forEach(r -> lt.disableRule(r.getId()));
    lt.addRule(rule);
    lt.enableRule(rule.getId());
    assertEquals("rule should fire on agreeing case", 1, lt.check(agreeing).size());
    assertEquals("rule should not fire on non-agreeing case", 0, lt.check(notAgreeing).size());

    try (Directory directory = new RAMDirectory()) {
      index(directory, agreeing, notAgreeing);
      Searcher searcher = new Searcher(directory);
      SearcherResult result = searcher.findRuleMatchesOnIndex(rule, language);

      List<MatchingSentence> matches = result.getMatchingSentences();
      assertEquals("exactly the agreeing sentence should match", 1, matches.size());
      MatchingSentence match = matches.get(0);
      assertEquals(agreeing, match.getSentence());
      List<RuleMatch> ruleMatches = match.getRuleMatches();
      assertEquals(1, ruleMatches.size());
      assertEquals(rule.getFullId(), ((PatternRule) ruleMatches.get(0).getRule()).getFullId());
    }
  }

  /**
   * Pins the known limitation: a rule whose only tokens are unified produces an empty relaxed query,
   * so {@link PatternRuleQueryBuilder} throws and the rule can never be searched (a recall hole, not
   * a slowdown). If this ever starts working, the unification support has been genuinely extended.
   */
  public void testRuleWithOnlyUnifiedTokensIsUnsearchable() throws Exception {
    String onlyUnifyRule =
        "  <category id='TEST' name='Test'>\n" +
        "    <rule id='UNI_ONLY' name='uni only'>\n" +
        "      <pattern>\n" +
        "        <unify>\n" +
        "          <feature id=\"case_sensitivity\"/>\n" +
        "          <token/>\n" +
        "          <token>York</token>\n" +
        "        </unify>\n" +
        "      </pattern>\n" +
        "      <message>Only unified tokens.</message>\n" +
        "    </rule>\n" +
        "  </category>\n";
    PatternRule rule = (PatternRule) loadRules(CASE_UNIFICATION + onlyUnifyRule).get(0);

    try (Directory directory = new RAMDirectory()) {
      index(directory, "I visited New York last year.");
      Searcher searcher = new Searcher(directory);
      try {
        searcher.findRuleMatchesOnIndex(rule, language);
        fail("expected UnsupportedPatternRuleException: an all-unified rule has no query anchor");
      } catch (UnsupportedPatternRuleException expected) {
        // documents the recall gap described in PatternRuleQueryBuilder.buildRelaxedQuery
      }
    }
  }

  private List<AbstractPatternRule> loadRules(String body) throws IOException {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>\n<rules lang='en'>\n" + body + "</rules>";
    InputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    return new PatternRuleLoader().getRules(input, "test.xml", language);
  }

  private void index(Directory directory, String... sentences) throws IOException {
    Analyzer analyzer = Indexer.getAnalyzer(language);
    IndexWriterConfig config = Indexer.getIndexWriterConfig(analyzer);
    try (IndexWriter writer = new IndexWriter(directory, config)) {
      for (String sentence : sentences) {
        Document doc = new Document();
        FieldType type = new FieldType();
        type.setStored(true);
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        type.setTokenized(true);
        // Store the original-case text in both fields; only indexed terms get lowercased.
        doc.add(new Field(FIELD_NAME, sentence, type));
        doc.add(new Field(FIELD_NAME_LOWERCASE, sentence, type));
        writer.addDocument(doc);
      }
    }
  }
}
