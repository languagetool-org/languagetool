/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Marcin Miłkowski (www.languagetool.org)
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
package org.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.Language;

import static org.junit.Assert.fail;

/**
 * Common pattern test routines (usable for Disambiguation rules as well).
 *
 * @author Marcin Miłkowski
 */
public final class PatternTestTools {

  // These characters should not be present in token values as they split tokens in all languages.
  private static final Pattern TOKEN_SEPARATOR_PATTERN = Pattern.compile("[ 	.,:;…!?(){}<>«»\"]");
  private static final Pattern TOKEN_SEPARATOR_PATTERN_NO_DOT = Pattern.compile("[ 	,:;…!?(){}<>«»\"]");

  private static final Pattern PROBABLE_PATTERN = Pattern.compile("(\\\\[dDsSwW])|.*([^*]\\*|[.+?{}()|\\[\\]].*|\\\\d).*");

  // Polish POS tags use dots, so do not consider the presence of a dot
  // as indicating a probable regular expression.
  private static final Pattern PROBABLE_PATTERN_PL_POS = Pattern.compile("(\\\\[dDsSwW])|.*([^*]\\*|[+?{}()|\\[\\]].*|\\\\d).*");

  private static final Pattern CHAR_SET_PATTERN = Pattern.compile("\\[^?([^\\]]+)\\]");
  private static final Pattern STRICT_CHAR_SET_PATTERN = Pattern.compile("(\\(\\?-i\\))?.*(?<!\\\\)\\[^?([^\\]]+)\\]");
  private static final Pattern UNBOUND_REPEAT = Pattern.compile(".*\\{\\d+,\\}.*");

  /*
   * These strings are not be recognized as a regular expression
   */
  private static final Set<String> NO_REGEXP = new HashSet<>(Arrays.asList(
    "PRP:LOK+TMP+MOD:DAT+AKK", "AUX:ind+pres+3+p", "PRP:TMP+MOD+CAU:DAT", "PRP:LOK+TMP:DAT", "PRP:LOK+TMP+CAU:DAT+AKK",
    "PRP:MOD:GEN+DAT", "PRP:LOK+TMP+CAU:DAT"
    ));


  private PatternTestTools() {
  }

  public static void failIfWhitespaceInToken(List<PatternToken> patternTokens, AbstractPatternRule rule, Language lang) {
    if (patternTokens != null) {
      for (PatternToken token : patternTokens) {
        if (token.getString() != null && token.getString().matches(".*\\s.*")) {
          fail("Whitespace found in token '" + token.getString() + "' of rule " + rule.getFullId() +
               " (language " + lang.getShortCodeWithCountryAndVariant() + "): " +
               "Using whitespace in a token will not work, as text gets split at whitespace. " +
               "Use a new <token> element instead.");
        }
      }
    }
  }
  
  // TODO: probably this would be more useful for exceptions
  // instead of adding next methods to PatternRule
  // we can probably validate using XSD and specify regexes straight there
  public static void warnIfRegexpSyntaxNotKosher(List<PatternToken> patternTokens,
          String ruleId, String ruleSubId, Language lang) {
    if (patternTokens == null) {   // for <regexp>
      return;
    }
    int i = 0;
    for (PatternToken pToken : patternTokens) {
      i++;

      if (pToken.isReferenceElement()) {
        continue;
      }

      // Check whether token value is consistent with regexp="..."
      warnIfElementNotKosher(
              pToken.getString(),
              pToken.isRegularExpression(),
              pToken.isCaseSensitive(),
              pToken.getNegation(),
              pToken.isInflected(),
              false,  // not a POS
              lang, ruleId + "[" + ruleSubId + "]",
              i);

      // Check postag="..." is consistent with postag_regexp="..."
      warnIfElementNotKosher(
              pToken.getPOStag() == null ? "" : pToken.getPOStag(),
              pToken.isPOStagRegularExpression(),
              pToken.isCaseSensitive(),
              pToken.getPOSNegation(),
              false,
              true,   // a POS.
              lang, ruleId + "[" + ruleSubId + "] (POS tag)",
              i);

      List<PatternToken> exceptionPatternTokens = new ArrayList<>();
      if (pToken.getExceptionList() != null) {
        for (PatternToken exception: pToken.getExceptionList()) {
          // Detect useless exception or missing skip="...". I.e. things like this:
          // <token postag="..."><exception scope="next">foo</exception</token>
          
          // We now allow scope="next" without skip="..."
          if (exception.hasNextException())
            continue;

//          if (exception.hasNextException() && pToken.getSkipNext() == 0) {
//            warn("The " + lang + " rule: "
//                    + ruleId + "[" + ruleSubId + "]"
//                    + " (exception in token [" + i + "])"
//                    + " has no skip=\"...\" and yet contains scope=\"next\""
//                    + " so the exception never applies."
//                    + " Did you forget skip=\"...\"?");
//          }

          // Detect exception that can't possibly be matched.
          if ( !pToken.getString().isEmpty()
                  && !exception.getString().isEmpty()
                  && !pToken.getNegation()
                  && !pToken.isInflected()
                  && !exception.getNegation()
                  && !exception.isInflected()
                  && pToken.getSkipNext() == 0
                  && pToken.isCaseSensitive() == exception.isCaseSensitive()) {

            if (exception.isRegularExpression()) {
              if (pToken.isRegularExpression()) {
                // Both exception and token are regexp.  In that case, we only
                // check sanity when exception regexp is a simple disjunction as
                // in this example:
                // <token regexp="yes">...some arbitrary regexp...
                //      <exception regexp="yes">foo|bar|xxx</exception>
                // </token>
                // All the words foo, bar, xxx should match the token regexp, or else they
                // are useless.
                if (exception.getString().indexOf('|') >= 0) {
                  String[] alt = exception.getString().split("\\|");
                  for (String part : alt) {
                    if (exception.getString().indexOf('(') >= 0) {
                      break;
                    }
                    if (part.matches("[^.*?{}\\[\\]]+")) {
                      if (!part.matches("(?i)" + pToken.getString())) {
                        warn("The " + lang + " rule: "
                                + ruleId + "[" + ruleSubId + "]"
                                + " has exception regexp [" + exception.getString()
                                + "] which contains disjunction part [" + part
                                + "] which seems useless since it does not match "
                                + "the regexp of token word [" + i + "] "
                                + "[" + pToken.getString()
                                + "], or did you forget skip=\"...\" or scope=\"previous\"?");
                      }
                    }
                  }
                }
              } else {
                // It does not make sense to to have a regexp exception
                // with a token which is not a regexp!?
                // Example <token>foo<exception regexp="xxx|yyy"/></token>
                warn("The " + lang + " rule: "
                        + ruleId + "[" + ruleSubId + "]"
                        + " has exception regexp [" + exception.getString()
                        + "] in token word [" + i +"] [" + pToken.getString()
                        + "] which seems useless, or "
                        + "did you forget skip=\"...\" or scope=\"previous\"?");
              }
            } else {
              if (pToken.isRegularExpression()) {
                // An exception that cannot match a token regexp is useless.
                // Example: <token regexp="yes">foo|bar<exception>xxx</exception></token>
                // Here exception word xxx cannot possibly match the regexp "foo|bar".
                if (!exception.getString().matches(
                        (exception.isCaseSensitive() ? "" : "(?i)") +  pToken.getString())) {
                  warn("The " + lang + " rule: "
                          + ruleId + "[" + ruleSubId + "] has exception word ["
                          +  exception.getString() + "] which cannot match the "
                          + "regexp token [" + i + "] [" + pToken.getString()
                          + "] so exception seems useless, "
                          + "or did you forget skip=\"...\" or scope=\"previous\"?");
                }
              } else {
                // An exception that cannot match a token string is useless,
                // Example: <token>foo<exception>bar</exception></token>
                warn("The " + lang + " rule: "
                        + ruleId + "[" + ruleSubId + "] has exception word ["
                        + exception.getString() + "] in token word [" + i
                        + "] [" + pToken.getString() + "] which seems useless, "
                        + "or did you forget skip=\"...\" or scope=\"previous\"?");
              }
            }
          }

          // Check whether exception value is consistent with regexp="..."
          // Don't check string "." since it is sometimes used as a regexp
          // and sometimes used as non regexp.
          if (!exception.getString().equals(".")) {
            warnIfElementNotKosher(
                    exception.getString(),
                    exception.isRegularExpression(),
                    exception.isCaseSensitive(),
                    exception.getNegation(),
                    exception.isInflected(),
                    false,  // not a POS
                    lang,
                    ruleId + "[" + ruleSubId+ "] (exception in token [" + i + "])",
                    i);
          }
          // Check postag="..." of exception is consistent with postag_regexp="..."
          warnIfElementNotKosher(
                  exception.getPOStag() == null ? "" : exception.getPOStag(),
                  exception.isPOStagRegularExpression(),
                  exception.isCaseSensitive(),
                  exception.getPOSNegation(),
                  false,
                  true,  // a POS
                  lang,
                  ruleId + "[" + ruleSubId + "] (exception in POS tag of token [" + i + "])",
                  i);

          // Search for duplicate exceptions (which are useless).
          // Since there are 2 nested loops on the list of exceptions,
          // this has thus a O(n^2) complexity, where n is the number
          // of exceptions in a token. But n is small and it is also
          // for testing only so that's OK.
          for (PatternToken otherException: exceptionPatternTokens) {
            if (equalException(exception, otherException)) {
              warn("The " + lang + " rule: "
                      + ruleId + "[" + ruleSubId + "]"
                      + " in token [" + i + "]"
                      + " contains duplicate exceptions with"
                      + " string=[" + exception.getString() + "]"
                      + " POS tag=[" + exception.getPOStag() + "]"
                      + " negate=[" + exception.getNegation() + "]"
                      + " POS negate=[" + exception.getPOSNegation() + "]");
              break;
            }
          }
          exceptionPatternTokens.add(exception);
        }
      }
    }

  }

  /**
   * Predicate to check whether two exceptions are identical or whether
   * one exception always implies the other.
   *
   * Example #1, useless identical exceptions:
   * <exception>xx</exception><exception>xx</exception>
   *
   * Example #2, first exception implies the second exception:
   * <exception>xx</exception><exception postag="A">xx</exception>
   */
  private static boolean equalException(PatternToken exception1,
                                        PatternToken exception2)
  {
    String string1 = exception1.getString() == null ? "" : exception1.getString();
    String string2 = exception2.getString() == null ? "" : exception2.getString();
    if (!exception1.isCaseSensitive() || !exception2.isCaseSensitive()) {
      // String comparison is done case insensitive if one or both strings
      // are case insensitive, because the case insensitive one would imply
      // the case sensitive one.
      string1 = string1.toLowerCase();
      string2 = string2.toLowerCase();
    }
    if (!string1.isEmpty() && !string2.isEmpty()) {
      if (!string1.equals(string2)) {
        return false;
      }
    }

    String posTag1 = exception1.getPOStag() == null ? "" : exception1.getPOStag();
    String posTag2 = exception2.getPOStag() == null ? "" : exception2.getPOStag();
    if (!posTag1.isEmpty() && !posTag2.isEmpty()) {
      if (!posTag1.equals(posTag2)) {
        return false;
      }
    }

    if ( string1.isEmpty() != string2.isEmpty() &&
         posTag1.isEmpty() != posTag2.isEmpty()) {
      return false;
    }

    // We should not need to check for:
    // - isCaseSensitive() since an exception without isCaseSensitive
    //   imply the one with isCaseSensitive.
    // - isInflected() since an exception with inflected="yes"
    //   implies the one without inflected="yes" if they have
    //   identical strings.
    //   without inflected="yes".
    // - isRegularExpression() since a given string is either
    //   a regexp or not.
    return exception1.getNegation() == exception2.getNegation()
            && exception1.getPOSNegation() == exception2.getPOSNegation()
            && exception1.hasNextException() == exception2.hasNextException()
            && exception1.hasPreviousException() == exception2.hasPreviousException();
  }

  private static void warnIfElementNotKosher(
          String stringValue,
          boolean isRegularExpression,
          boolean isCaseSensitive,
          boolean isNegated,
          boolean isInflected,
          boolean isPos,
          Language lang,
          String ruleId,
          int tokenIndex) {

    // Check that the string value does not contain token separator.
    if (!isPos && !isRegularExpression && stringValue.length() > 1) {
      // Example: <token>foo bar</token> can't be valid because
      // token value contains a space which is a token separator.

      // Ukrainian dictionary contains some abbreviations with dot
      Pattern tokenSeparatorPattern = lang.getShortCode().equals("uk")
    		  ? TOKEN_SEPARATOR_PATTERN_NO_DOT
    		  : TOKEN_SEPARATOR_PATTERN;

      if (tokenSeparatorPattern.matcher(stringValue).find()) {
        warn("The " + lang + " rule: "
                + ruleId + ", token [" + tokenIndex + "], contains " + "\"" + stringValue
                + "\" that contains token separators, so can't possibly be matched.");
      }
    }

    // Use a different regexp to check for probable regexp in Polish POS tags
    // since Polish uses dot '.' in POS tags. So a dot does not indicate that
    // it's a probable regexp for Polish POS tags.
    Pattern regexPattern = (isPos && lang.getShortCode().equals("pl"))
        || (!isPos && lang.getShortCode().equals("uk"))
            ? PROBABLE_PATTERN_PL_POS // Polish POS tag or Ukrainian token
            : PROBABLE_PATTERN;       // other usual cases

    if (!isRegularExpression && stringValue.length() > 1
            && regexPattern.matcher(stringValue).find() && !NO_REGEXP.contains(stringValue)) {
      warn("The " + lang + " rule: "
              + ruleId + ", token [" + tokenIndex + "], contains " + "\"" + stringValue
              + "\" that is not marked as regular expression but probably is one.");
    }

    if (isRegularExpression && stringValue.isEmpty()) {
      warn("The " + lang + " rule: "
              + ruleId + ", token [" + tokenIndex + "], contains an empty string " + "\""
              + stringValue + "\" that is marked as regular expression.");
    } else if (isRegularExpression && stringValue.length() > 1
            && !regexPattern.matcher(stringValue).find()) {
      warn("The " + lang + " rule: "
              + ruleId + ", token [" + tokenIndex + "], contains " + "\"" + stringValue
              + "\" that is marked as regular expression but probably is not one.");
    }

    if (isNegated && stringValue.isEmpty()) {
      warn("The " + lang + " rule: "
              + ruleId + ", token [" + tokenIndex + "], marked as negated but is "
              + "empty so the negation is useless. Did you mix up "
              + "negate=\"yes\" and negate_pos=\"yes\"?");
    }
    if (isInflected && stringValue.isEmpty()) {
      warn("The " + lang + " rule: "
              + ruleId + ", token [" + tokenIndex + "], contains " + "\"" + stringValue
              + "\" that is marked as inflected but is empty, so the attribute is redundant.");
    }
    if (isRegularExpression && ".*".equals(stringValue)) {
      warn("The " + lang + " rule: "
              + ruleId + ", token [" + tokenIndex + "], marked as regular expression contains "
              + "regular expression \".*\" which is useless: "
              + "(use an empty string without regexp=\"yes\" such as <token/>)");
    }

    if (isRegularExpression) {
      if (UNBOUND_REPEAT.matcher(stringValue).matches()) {
        warn(lang + ": Please limit repetition in regex, e.g. use '{2,30}' instead of '{2,}': " + stringValue +  " (" + ruleId + ")");
      }
      Matcher matcher = CHAR_SET_PATTERN.matcher(stringValue);
      if (matcher.find()) {
        Matcher strictMatcher = STRICT_CHAR_SET_PATTERN.matcher(stringValue);  // for performance reasons, only now use the strict pattern
        if (strictMatcher.find()) {
          // Remove things like \p{Punct} which are irrelevant here.
          String s = strictMatcher.group(2).replaceAll("\\\\p\\{[^}]*\\}", "");
          // case sensitive if pattern contains (?-i).
          if (s.indexOf('|') >= 0) {
            if (!(s.indexOf('|') >= 1 && s.charAt(s.indexOf('|') -1) == '\\')){ //don't warn if it's escaped
              warn("The " + lang + " rule: "
                  + ruleId + ", token [" + tokenIndex + "], contains | (pipe) in "
                  + "regexp bracket expression [" + strictMatcher.group(2)
                  + "] which is unlikely to be correct.");
            }
          }

        /* Disabled case insensitive check for now: it gives several errors
         * in German which are minor and debatable whether it adds value.
        boolean caseSensitive = matcher.group(1) != null || isCaseSensitive;
        if (!caseSensitive) {
          s = s.toLowerCase();
        }
        */
          char[] sorted = s.toCharArray();
          // Sort characters in string, so finding duplicate characters can be done by
          // looking for identical adjacent characters.
          Arrays.sort(sorted);
          for (int i = 1; i < sorted.length; ++i) {
            char c = sorted[i];
            if ("&\\-|".indexOf(c) < 0 && sorted[i - 1] == c) {
              warn("The " + lang + " rule: "
                      + ruleId + ", token [" + tokenIndex + "], contains "
                      + "regexp part [" + strictMatcher.group(2)
                      + "] which contains duplicated char [" + c + "].");
              break;
            }
          }
        }
      }
      if (stringValue.contains("|")) {
        if (stringValue.contains("||")
                || stringValue.charAt(0) == '|'
                || stringValue.charAt(stringValue.length() - 1) == '|') {
          // Empty disjunctions in regular expression are most likely not intended.
          warn("The " + lang + " rule: "
                  + ruleId + ", token [" + tokenIndex + "], contains empty "
                  + "disjunction | within " + "\"" + stringValue + "\".");
        }
        String[] groups = stringValue.split("[)(]");
        for (String group : groups) {
          String[] alt = group.split("\\|");
          Set<String> partSet = new HashSet<>();
          Set<String> partSetNoCase = new HashSet<>();
          boolean hasSingleChar = false;
          boolean hasSingleDot = false;

          for (String part : alt) {
            if (part.length() == 1) {
              // If all alternatives in disjunction have one char, then
              // a dot . (any char) does not make sense since it would match
              // other characters.
              if (part.equals(".")) {
                hasSingleDot = true;
              } else {
                hasSingleChar = true;
              }
            }
            String partNoCase = isCaseSensitive ? part : part.toLowerCase();
            if (partSetNoCase.contains(partNoCase)) {
              if (partSet.contains(part)) {
                // Duplicate disjunction parts "foo|foo".
                warn("The " + lang + " rule: "
                        + ruleId + ", token [" + tokenIndex + "], contains "
                        + "duplicated disjunction part ("
                        + part + ") within " + "\"" + stringValue + "\".");
              } else {
                // Duplicate disjunction parts "Foo|foo" since element ignores case.
                warn("The " + lang + " rule: "
                        + ruleId + ", token [" + tokenIndex + "], contains duplicated "
                        + "non case sensitive disjunction part ("
                        + part + ") within " + "\"" + stringValue + "\". Did you "
                        + "forget case_sensitive=\"yes\"?");
              }
            }
            partSetNoCase.add(partNoCase);
            partSet.add(part);
          }
          if (hasSingleDot && hasSingleChar) {
            // This finds errors like this <token regexp="yes">.|;|:</token>
            // which should be <token regexp="yes">\.|;|:</token> or
            // even better <token regexp="yes">[.;:]</token>
            warn("The " + lang + " rule: "
                    + ruleId + ", token [" + tokenIndex + "], contains a single dot (matching any char) "
                    + "so other single char disjunctions are useless within " + "\"" + stringValue
                    + "\". Did you forget forget a backslash before the dot?");
          }
        }
      }
    }
  }
  
  private static void warn(String s) {
    System.err.println("*** WARNING: " + s);
  }

}
