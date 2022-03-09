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
package org.languagetool.tokenizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.tools.StringTools;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets their own tokens.
 * The tokenizer is a quite simple character-based one, though it knows
 * about urls and will put them in one token, if fully specified including
 * a protocol (like {@code http://foobar.org}).
 * 
 * @author Daniel Naber
 */
public class WordTokenizer implements Tokenizer {

  private static final List<String> PROTOCOLS = Collections.unmodifiableList(Arrays.asList("http", "https", "ftp"));
  private static final Pattern URL_CHARS = Pattern.compile("[a-zA-Z0-9/%$-_.+!*'(),?#~]+");
  private static final Pattern DOMAIN_CHARS = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9-]+");
  private static final Pattern NO_PROTOCOL_URL = Pattern.compile("([a-zA-Z0-9][a-zA-Z0-9-]+\\.)?([a-zA-Z0-9][a-zA-Z0-9-]+)\\.([a-zA-Z0-9][a-zA-Z0-9-]+)/.*");
  private static final Pattern E_MAIL = Pattern.compile("(?<!:)@?\\b[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))\\b");

  /*
   * Possibly problematic characters for tokenization:
   * \u00ad soft hyphen (not included) 
   * \u002d hyphen (-) (not included): needs special processing in different languages
   * \u2011 non-breaking hyphen (not included): similar to hyphen 
   * \u2013 en dash (included): it can be used sometimes as hyphen (not included) and rules need changes in some languages 
   * \u00b7 middle dot (·) (included): excluded in Catalan because it is a word character
   * \u005f underscore, low line (_) (not included): included in English, Dutch
   */
  private static final String TOKENIZING_CHARACTERS = 
      "\u0020\u00A0\u115f\u1160\u1680"
      + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
      + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
      + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
      + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
      + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
      + "¦‖∣|,.;()[]{}=*#∗+×·÷<>!?:~/\\\"'«»„”“‘’`´‛′›‹…¿¡‼⁇⁈⁉™®\u203d"
      + "\u2012\u2013\u2014\u2015" // dashes
      + "\u2500\u3161\u2713" // other dashes
      + "\u25CF\u25CB\u25C6\u27A2\u25A0\u25A1\u2605\u274F\u2794\u21B5\u2756\u25AA\u2751\u2022" // bullet points
      + "\u2B9A\u2265\u2192\u21FE\u21C9\u21D2\u21E8\u21DB" // arrows
      + "\u00b9\u00b2\u00b3\u2070\u2071\u2074\u2075\u2076\u2077\u2078\u2079" // superscripts
      + "\t\n\r";

  /**
   * Get the protocols that the tokenizer knows about.
   * @return currently {@code http}, {@code https}, and {@code ftp}
   * @since 2.1
   */
  public static List<String> getProtocols() {
    return PROTOCOLS;
  }

  /**
   * @since 3.0
   */
  public static boolean isUrl(String token) {
    for (String protocol : WordTokenizer.getProtocols()) {
      if (token.startsWith(protocol + "://") || token.startsWith("www.")) {
        return true;
      }
    }
    return NO_PROTOCOL_URL.matcher(token).matches();
  }

  /**
   * @since 3.5
   */
  public static boolean isEMail(String token) {
    return E_MAIL.matcher(token).matches();
  }

  @Override
  public List<String> tokenize(String text) {
    List<String> l = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, getTokenizingCharacters(), true);
    while (st.hasMoreElements()) {
      l.add(st.nextToken());
    }
    return joinEMailsAndUrls(l);
  }

  /**
   * @return The string containing the characters used by the
   * tokenizer to tokenize words.
   * @since 2.5
   */
  public String getTokenizingCharacters() {
    return TOKENIZING_CHARACTERS;
  }

  protected List<String> joinEMailsAndUrls(List<String> list) {
    return joinUrls(joinEMails(list));
  }

  /**
   * @since 3.5
   */
  protected List<String> joinEMails(List<String> list) {
    StringBuilder sb = new StringBuilder();
    for (String item : list) {
      sb.append(item);
    }
    String text = sb.toString();
    if (text.contains("@") && E_MAIL.matcher(text).find()) {  // explicit check for "@" speeds up method by factor of ~10
      Matcher matcher = E_MAIL.matcher(text);
      List<String> l = new ArrayList<>();
      int currentPosition = 0, start, end, idx = 0;
      while (matcher.find()) {
        start = matcher.start();
        end = matcher.end();
        while (currentPosition < end) {
          if (currentPosition < start) {
            l.add(list.get(idx));
          } else if (currentPosition == start) {
            l.add(matcher.group());
          }
          currentPosition += list.get(idx).length();
          idx++;
        }
      }
      if (currentPosition < text.length()) {
        l.addAll(list.subList(idx, list.size()));
      }
      return l;
    }
    return list;
  }

  // see rfc1738 and http://stackoverflow.com/questions/1856785/characters-allowed-in-a-url
  protected List<String> joinUrls(List<String> l) {
    List<String> newList = new ArrayList<>();
    boolean inUrl = false;
    StringBuilder url = new StringBuilder();
    String urlQuote = null;
    for (int i = 0; i < l.size(); i++) {
      if (urlStartsAt(i, l) && !inUrl) {
        inUrl = true;
        if (i-1 >= 0) {
          urlQuote = l.get(i-1);
        }
        url.append(l.get(i));
      } else if (inUrl && urlEndsAt(i, l, urlQuote)) {
        inUrl = false;
        urlQuote = null;
        newList.add(url.toString());
        url.setLength(0);
        newList.add(l.get(i));
      } else if (inUrl) {
        url.append(l.get(i));
      } else {
        newList.add(l.get(i));
      }
    }
    if (url.length() > 0) {
      newList.add(url.toString());
    }
    return newList;
  }

  private boolean urlStartsAt(int i, List<String> l) {
    String token = l.get(i);
    if (isProtocol(token) && l.size() > i + 3) {
      String nToken = l.get(i + 1);
      String nnToken = l.get(i + 2);
      String nnnToken = l.get(i + 3);
      if (nToken.equals(":") && nnToken.equals("/") && nnnToken.equals("/")) {
        return true;
      }
    }
    if (l.size() > i + 1) {
      // e.g. www.mydomain.org
      String nToken = l.get(i);
      String nnToken = l.get(i + 1);
      if (nToken.equals("www") && nnToken.equals(".")) {
        return true;
      }
    }
    if (l.size() > i + 3 && // e.g. mydomain.org/ (require slash to avoid missing errors that can be interpreted as domains)
        l.get(i + 1).equals(".") &&   // use this order so the regex only gets matched if needed
        l.get(i + 3).equals("/") &&
        DOMAIN_CHARS.matcher(token).matches() &&
        DOMAIN_CHARS.matcher(l.get(i + 2)).matches()) {
      return true;
    }
    return (l.size() > i + 5 &&          // e.g. sub.mydomain.org/ (require slash to avoid missing errors that can be interpreted as domains)
        l.get(i + 1).equals(".") &&  // use this order so the regex only gets matched if needed
        l.get(i + 3).equals(".") &&
        l.get(i + 5).equals("/") &&
        DOMAIN_CHARS.matcher(token).matches() &&
        DOMAIN_CHARS.matcher(l.get(i + 2)).matches() &&
        DOMAIN_CHARS.matcher(l.get(i + 4)).matches()
       );
  }

  private boolean isProtocol(String token) {
    return PROTOCOLS.contains(token);
  }

  private boolean urlEndsAt(int i, List<String> l, String urlQuote) {
    String token = l.get(i);
    if (StringTools.isWhitespace(token) || token.equals(")") || token.equals("]")) {   // this is guesswork
      return true;
    } else if (l.size() > i + 1) {
      String nextToken = l.get(i + 1);
      if (((StringTools.isWhitespace(nextToken) || StringUtils.equalsAny(nextToken, "\"", "»", "«", "‘", "’", "“", "”", "'", ".")) &&
          (StringUtils.equalsAny(token, ".", ",", ";", ":", "!", "?") || token.equals(urlQuote))) ||
          !URL_CHARS.matcher(token).matches()) {
        return true;
      }
    } else {
      if (!URL_CHARS.matcher(token).matches() || token.equals(".") || token.equals(urlQuote)) {
        return true;
      }
    }
    return false;
  }

}
