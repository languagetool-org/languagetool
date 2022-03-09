/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.*;
import org.languagetool.broker.ResourceDataBroker;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Information about common words - use as a fallback if language identification yields low confidence.
 * @since 4.4
 */
public class CommonWords {

  private final static Map<String, List<Language>> word2langs = Collections.synchronizedMap(new HashMap<>());
  private final static Pattern numberPattern = Pattern.compile("[0-9.,%-]+");
  
  private final static Language esLang = Languages.getLanguageForShortCode("es");
  private final static Language caLang = Languages.getLanguageForShortCode("ca");
  private final static Language ptLang = Languages.getLanguageForShortCode("pt");
  // but -cion can be Esperanto; ía(n) can be Galician
  private final static Pattern spanishPattern = Pattern.compile("^[a-zñ]+(ón|cion|aban|ábamos|ábais|íamos|íais|[úí]a[sn]?|úe[ns]?)$");
  private final static Pattern notSpanishPattern = Pattern.compile("^[lmndts]['’].*$|^.*(ns|[áéó].i[oa]s?)$|^.*(ss|[çàèòïâêôãõìù]|l·l).*$");
  private final static Pattern notCatalanPattern = Pattern.compile("^.*([áéó].i[oa]s?|d[oa]s)$|^.*[áâêôãõìùñ].*$");
  private final static Pattern portuguesePattern = Pattern.compile("^.*([áó]ri[oa]|ério)s?$"); // éria can be French
  
  public CommonWords() throws IOException {
    synchronized (word2langs) {
      if (word2langs.isEmpty()) {
        for (Language lang : Languages.get()) {
          if (lang.isVariant() &&
              !lang.getShortCode().equals("no")) {  // ugly hack to quick fix https://github.com/languagetooler-gmbh/languagetool-premium/issues/822 
            continue;
          }
          ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
          String path = lang.getCommonWordsPath();
          InputStream stream = null;
          try {
            if (path != null) {
              if (dataBroker.resourceExists(path)) {
                stream = dataBroker.getFromResourceDirAsStream(path);
              } else if (new File(path).exists()) {
                stream = new FileInputStream(path);
              } else {
                throw new IOException("Common words file not found for " + lang + ": " + path);
              }
            } else {
              System.out.println("WARN: no common words file defined for " + lang + " - this language might not be correctly auto-detected");
              continue;
            }
            try (Scanner scanner = new Scanner(stream, "utf-8")) {
              while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty() || line.startsWith("#")) {
                  continue;
                }
                String key = line.toLowerCase();
                if (key.length() == 1 && Character.isSpaceChar(key.charAt(0))) {
                  continue;
                }
                List<Language> languages = word2langs.get(key);
                if (languages == null) {
                  // word2langs is static, so this can be accessed from multiple threads concurrently -> prevent exceptions
                  List<Language> l = Collections.synchronizedList(new LinkedList<>());
                  l.add(lang);
                  word2langs.put(key, l);
                } else {
                  if (!languages.contains(lang)) {
                    languages.add(lang);
                  }
                }
              }
            }
          } finally {
            if (stream != null) {
              stream.close();
            }
          }
        }
      }
    }
  }

  public Map<Language, Integer> getKnownWordsPerLanguage(String text) {
    Map<Language,Integer> result = new HashMap<>();
    String auxText = text.replaceAll("[(),.:;!?„“\"¡¿\\s\\[\\]{}-«»”]", " ");
    if (!auxText.endsWith(" ") && StringUtils.countMatches(auxText, " ") > 0) {
      // last word might not be finished yet, so ignore
      auxText = auxText.replaceFirst("\\p{L}+$", "");
    }
    // Proper per-language tokenizing might help, but then the common_words.txt
    // will also need to be tokenized the same way. Also, this is quite fast.
    String[] words = auxText.split("[ -]");
    for (String word : words) {
      if (numberPattern.matcher(word).matches()) {
        continue;
      }
      String lcWord = word.toLowerCase();
      List<Language> languages = word2langs.get(lcWord);
      if (languages != null) {
        for (Language lang : languages) {
          result.put(lang, result.getOrDefault(lang, 0) + 1);
        }
      }
      //Portuguese
      if ((languages == null || !languages.contains(ptLang)) && portuguesePattern.matcher(lcWord).matches()) {
        result.put(ptLang, result.getOrDefault(ptLang, 0) + 1);
      }
      //Spanish
      if ((languages == null || !languages.contains(esLang)) && spanishPattern.matcher(lcWord).matches()) {
        result.put(esLang, result.getOrDefault(esLang, 0) + 1);
      }
      if ((languages == null || !languages.contains(esLang)) && notSpanishPattern.matcher(lcWord).matches()) {
        result.put(esLang, result.getOrDefault(esLang, 0) - 1);
      }
      //Catalan
      if ((languages == null || !languages.contains(caLang)) && notCatalanPattern.matcher(lcWord).matches()) {
        result.put(caLang, result.getOrDefault(caLang, 0) - 1);
      }
    }
    return result;
  }
  
}
