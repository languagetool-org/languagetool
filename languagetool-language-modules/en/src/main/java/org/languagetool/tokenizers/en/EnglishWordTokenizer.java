/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Marcin Milkowski (http://www.languagetool.org)
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
package org.languagetool.tokenizers.en;

import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tokenizers.WordTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 * @author Marcin Milkowski
 * @since 2.5
 */
public class EnglishWordTokenizer extends WordTokenizer {

  private static final Pattern SINGLE_QUOTE = compile("'");
  private static final Pattern CURLY_QUOTE = compile("’");
  private static final Pattern APOSTYPEW = compile("\u0001\u0001APOSTYPEW\u0001\u0001");
  private static final Pattern APOSTYPOG = compile("\u0001\u0001APOSTYPOG\u0001\u0001");
  private static final Pattern SOFT_HYPHEN = compile("\u00AD");
  private static final List<Pattern> patternList = Arrays.asList(
      compile("^(fo['’]c['’]sle|rec['’][ds]|OK['’]d|cc['’][ds]|DJ['’][d]|[pd]m['’]d|rsvp['’]d)$", CASE_INSENSITIVE | UNICODE_CASE),
      compile(
          "^(['’]?)(are|is|were|was|do|does|did|have|has|had|wo|would|ca|could|sha|should|must|ai|ought|might|need|may|am|dare|das|dass|hai|used|use)(n['’]t)$",
          CASE_INSENSITIVE | UNICODE_CASE),
      compile("^(.+)(['’]m|['’]re|['’]ll|['’]ve|['’]d|['’]s)(['’-]?)$",
          CASE_INSENSITIVE | UNICODE_CASE),
      compile("^(['’]t)(was|were|is)$", CASE_INSENSITIVE | UNICODE_CASE));

  //the string used to tokenize characters
  private final String enTokenizingChars = super.getTokenizingCharacters() + "_"; // underscore;

//  public EnglishWordTokenizer() {
//
//  }

  /**
   * Tokenizes text. The English tokenizer differs from the standard one in two
   * respects:
   * <ol>
   * <li>it does not treat the hyphen as part of the word if the hyphen is at the
   * end of the word;</li>
   * <li>it includes n-dash as a tokenizing character, as it is used without a
   * whitespace in English.
   * </ol>
   * 
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(String text) {
    List<String> l = new ArrayList<>();
    String auxText = text;
    auxText = SINGLE_QUOTE.matcher(auxText).replaceAll("\u0001\u0001APOSTYPEW\u0001\u0001");
    auxText = CURLY_QUOTE.matcher(auxText).replaceAll("\u0001\u0001APOSTYPOG\u0001\u0001");
    //auxText = auxText.replaceAll("-", "\u0001\u0001HYPHEN\u0001\u0001");
    String s;
    String groupStr;

    final StringTokenizer st = new StringTokenizer(auxText, enTokenizingChars, true);

    while (st.hasMoreElements()) {
      s = APOSTYPEW.matcher(st.nextToken()).replaceAll("'");
      s = APOSTYPOG.matcher(s).replaceAll("’");
          //.replaceAll("\u0001\u0001HYPHEN\u0001\u0001", "-");
      boolean matchFound = false;
      Matcher matcher = null;
      if (s.contains("'") || s.contains("’")) {
        for (Pattern pattern : patternList) {
          matcher = pattern.matcher(s);
          matchFound = matcher.find();
          if (matchFound) {
            break;
          }
        }
      }
      if (matchFound) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
          groupStr = matcher.group(i);
          l.addAll(wordsToAdd(groupStr));
        }
      } else {
        l.addAll(wordsToAdd(s));
      }
    }
    return joinEMailsAndUrls(l);
  }

  /* Splits a word containing hyphen(-’') if it doesn't exist in the dictionary. */
  private List<String> wordsToAdd(String s) {
    final List<String> l = new ArrayList<>();
    int hyphensAtEnd = 0;
    synchronized (this) { // speller is not thread-safe
      if (!s.isEmpty()) {
        while (s.startsWith("-")) {
          l.add("-");
          s = s.substring(1);
        }
        while (s.endsWith("-")) {
          s = s.substring(0,s.length()-1);
          hyphensAtEnd++;
        }
        if (!s.isEmpty()) {
          if (!s.contains("-") && !s.contains("'") && !s.contains("’")) {
            l.add(s);
          } else {
            String normalized = SOFT_HYPHEN.matcher(s).replaceAll("");
            normalized = CURLY_QUOTE.matcher(normalized).replaceAll("'");
            if (EnglishTagger.INSTANCE.tag(Arrays.asList(normalized)).get(0)
                .isTagged()) {
              l.add(s);
            }
            // some camel-case words containing hyphen (is there any better fix?)
            else if (s.equalsIgnoreCase("mers-cov") || s.equalsIgnoreCase("mcgraw-hill")
                || s.equalsIgnoreCase("sars-cov-2") || s.equalsIgnoreCase("sars-cov") || s.equalsIgnoreCase("ph-metre")
                || s.equalsIgnoreCase("ph-metres") || s.equalsIgnoreCase("anti-ivg") || s.equalsIgnoreCase("anti-uv")
                || s.equalsIgnoreCase("anti-vih") || s.equalsIgnoreCase("al-qaida")) {
              l.add(s);
            } else {
              // if not found, the word is split
              // final StringTokenizer st2 = new StringTokenizer(s, "-’'", true);
              final StringTokenizer st2 = new StringTokenizer(s, "’'", true);
              while (st2.hasMoreElements()) {
                l.add(st2.nextToken());
              }
            }
          }
        }
      }
      while (hyphensAtEnd > 0) {
        l.add("-");
        hyphensAtEnd--;
      }
      return l;
    }
  }
}
