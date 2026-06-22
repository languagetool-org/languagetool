package org.languagetool.rules.en;

import java.io.IOException;
import java.util.Objects;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordCoherencyDataLoader;

/** English version of {@link AbstractWordCoherencyRule}. */
public class WordCoherencyRule extends AbstractWordCoherencyRule {
  private static final boolean DEBUG = Boolean.getBoolean("lt.debug.coherency");

  private static void dbg(String fmt, Object... args) {
    if (DEBUG)
      System.err.println(String.format(fmt, args));
  }

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/en/coherency.txt");

  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    addExamplePair(
        Example.wrong("He likes archaeology. Really? She likes <marker>archeology</marker>, too."),
        Example.fixed("He likes archaeology. Really? She likes <marker>archaeology</marker>, too."));
    if (DEBUG)
      dbg("Loaded wordMap with %d entries", wordMap.size());
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    Map<String, String> shouldNotAppearWord = new HashMap<>();
    int pos = 0;

    for (AnalyzedSentence sentence : sentences) {
      dbg("=== NEW SENTENCE === %s", sentence.getText());
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

      for (AnalyzedTokenReadings atr : tokens) {
        String surface = atr.getToken();
        String surfaceLc = surface.toLowerCase(Locale.ROOT);
        dbg("Token='%s'", surface);

        // collect lemmas
        Set<String> lemmasLc = atr.getReadings().stream()
            .map(AnalyzedToken::getLemma)
            .filter(Objects::nonNull)
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(java.util.stream.Collectors.toSet());
        dbg("  Lemmas=%s", lemmasLc);

        List<String> keysToCheck = candidateKeys(surfaceLc, lemmasLc);
        dbg("  keysToCheck=%s", keysToCheck);

        for (String key : keysToCheck) {
          Set<String> variants = wordMap.get(key);
          if (variants == null || variants.isEmpty())
            continue;
          dbg("  key='%s' → variants=%s", key, variants);

          if (shouldSkipInflection(surfaceLc, key, lemmasLc)) {
            dbg("  skipped inflection for key='%s'", key);
            continue;
          }

          int fromPos = pos + atr.getStartPos();
          int toPos = pos + atr.getEndPos();

          // try match
          String matchKey = null;
          if (shouldNotAppearWord.containsKey(key)) {
            matchKey = key;
          } else {
            for (String v : variants) {
              if (shouldNotAppearWord.containsKey(v)) {
                matchKey = v;
                break;
              }
            }
          }

          if (matchKey != null) {
            String other = shouldNotAppearWord.get(matchKey);
            dbg("  MATCH: '%s' vs '%s' (span %d-%d)", surface, other, fromPos, toPos);
            String msg = getMessage(surface, other);
            RuleMatch rm = new RuleMatch(this, sentence, fromPos, toPos, msg);

            String marked = sentence.getText().substring(atr.getStartPos(), atr.getEndPos());
            String replacement = createReplacement(marked, key, other, atr);
            if (org.languagetool.tools.StringTools.startsWithUppercase(surface)) {
              replacement = org.languagetool.tools.StringTools.uppercaseFirstChar(replacement);
            }
            if (!marked.equalsIgnoreCase(replacement)) {
              rm.setSuggestedReplacement(replacement);
              ruleMatches.add(rm);
            }
            rm.setShortMessage(getShortMessage());
            break;
          } else {
            // register new opposites
            for (String v : variants) {
              shouldNotAppearWord.put(v, key);
            }
            dbg("  register opposites: %s → %s", variants, key);
          }
        }
      }
      pos += sentence.getCorrectedTextLength();
    }
    dbg("Total matches: %d", ruleMatches.size());
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "Do not mix variants of the same word ('" + word1 + "' and '" + word2 + "') within a single text.";
  }

  @Override
  public String getId() {
    return "EN_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Coherent spelling of words with two admitted variants.";
  }

  private static List<String> candidateKeys(String surfaceLc, Set<String> lemmasLc) {
    LinkedHashSet<String> keys = new LinkedHashSet<>();
    if (!lemmasLc.isEmpty())
      keys.addAll(lemmasLc);
    else
      keys.add(surfaceLc);

    boolean anyHit = keys.stream().anyMatch(k -> {
      Set<String> vs = wordMap.get(k);
      return vs != null && !vs.isEmpty();
    });
    if (!anyHit) {
      for (String cand : computePastTenseFallbacks(surfaceLc)) {
        Set<String> vs = wordMap.get(cand);
        if (vs != null && !vs.isEmpty())
          keys.add(cand);
      }
    }
    return new ArrayList<>(keys);
  }

  /** Very small, safe fallback: trim trailing "ed"; if that leaves a trailing hyphen, trim it too. */
  private static List<String> computePastTenseFallbacks(String surfaceLc) {
    List<String> extra = new ArrayList<>();
    if (surfaceLc == null) return extra;

    int len = surfaceLc.length();
    // plain "...ed"
    if (len >= 3 && surfaceLc.endsWith("ed")) {
      String base = surfaceLc.substring(0, len - 2);   // safe: len-2 >= 1
      if (!base.isEmpty()) {
        extra.add(base);                                // e.g., "reelected" -> "reelect"
        // hyphenated end: "...-ed" -> "...-"
        if (base.charAt(base.length() - 1) == '-') {
          String baseNoHyphen = base.substring(0, base.length() - 1);  // safe
          if (!baseNoHyphen.isEmpty()) {
            extra.add(baseNoHyphen);                    // e.g., "re-elected" -> "re-elect"
          }
        }
      }
    }
    return extra;
  }


  private static boolean shouldSkipInflection(String surfaceLc, String key, Set<String> lemmasLc) {
    return !lemmasLc.isEmpty() && lemmasLc.contains(key) && isNounOrAdjInflectionOf(surfaceLc, key);
  }

  /** Whitelist only noun/adjective inflections that won't mask verb forms. */
  private static boolean isNounOrAdjInflectionOf(String surfaceLc, String lemmaLc) {
    if (surfaceLc.equals(lemmaLc))
      return false;

    // DO NOT: generic +s/+es — this hides verb 3sg (e.g., oxidises)
    // Keep only cases that are unlikely to be verb forms:

    // y-ending: y -> ies / ier / iest (e.g., doggy/doggie ->
    // doggies/doggier/doggiest)
    if (lemmaLc.endsWith("y")) {
      String stem = lemmaLc.substring(0, lemmaLc.length() - 1);
      if (surfaceLc.equals(stem + "ies"))
        return true; // plural of -y nouns/adjs
      if (surfaceLc.equals(stem + "ier"))
        return true; // comparative
      if (surfaceLc.equals(stem + "iest"))
        return true; // superlative
    }

    // Generic comparative/superlative (non-y cases): safer/safest etc.
    if (surfaceLc.equals(lemmaLc + "er") || surfaceLc.equals(lemmaLc + "est"))
      return true;

    // No whitelist for -ed/-ing/-s to avoid hiding real inconsistencies.
    return false;
  }
}
