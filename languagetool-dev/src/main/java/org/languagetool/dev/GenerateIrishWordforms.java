/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Jim O'Regan
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

import org.languagetool.tagging.ga.Utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateIrishWordforms {
  private static final Map<String, String[]> nounGuesses = new HashMap<>();
  static {
    nounGuesses.put("óir", new String[]{"m3", "óir", "óra", "óirí", "óirí"});
    nounGuesses.put("eoir", new String[]{"m3", "eoir", "eora", "eoirí", "eoirí"});
    nounGuesses.put("éir", new String[]{"m3", "éir", "éara", "éirí", "éirí"});
    nounGuesses.put("úir", new String[]{"m3", "úir", "úra", "úirí", "úirí"});
    nounGuesses.put("aeir", new String[]{"m3", "aeir", "aera", "aeirí", "aeirí"});
    nounGuesses.put("álaí", new String[]{"m4", "álaí", "álaí", "álaithe", "álaithe"});
    nounGuesses.put("eálaí", new String[]{"m4", "eálaí", "eálaí", "eálaithe", "eálaithe"});
  }
  private static final String NOUN_ENDINGS_REGEX = getEndingsRegex(nounGuesses);
  private static final Pattern NOUN_PATTERN = Pattern.compile(NOUN_ENDINGS_REGEX);
  private static final String[] BASEFORMS = {"sg.nom", "sg.gen", "pl.nom", "pl.gen"};

  public static void writeFromGuess(String word) {
    Matcher m = NOUN_PATTERN.matcher(word);
    if(m.find()) {
      String stem = m.group(1);
      String ending = m.group(2);
      String[] endings = nounGuesses.get(ending);
      Map<String, String> forms = expandNounForms(stem, endings);
      Map<String, String> tags = getIrishFSTTags(forms);
      for (String s : tags.keySet()) {
        StringBuilder sb = new StringBuilder();
        sb.append(forms.get(s));
        sb.append('\t');
        sb.append(word);
        sb.append('\t');
        sb.append(tags.get(s));
        System.out.println(sb.toString());
      }
    }

  }

  public static Map<String, String> expandNounForms(String stem, String[] parts) {
    String gender = parts[0];
    String nounClass = "";
    if (parts[0].matches("[mf][0-9]")) {
      gender = parts[0].substring(0, 1);
      nounClass = parts[0].substring(1);
    }
    if (nounClass.equals("")) {
      String irishFSTOut = getIrishFSTNounClass(parts[1]);
      nounClass = irishFSTOut.substring(2, 1);
    }
    Map<String, String> forms = new HashMap<>();
    forms.put("stem", stem);
    forms.put("pos", "n");
    forms.put("class", gender + nounClass);
    forms.put("gender", gender);
    forms.put("sg.nom", stem + parts[1]);
    forms.put("sg.gen", stem + parts[2]);
    forms.put("pl.nom", stem + parts[3]);
    forms.put("pl.gen", stem + parts[4]);
    //Not doing separate vocative yet
    //if (parts.length == 6) {}
    addMutatedForms(forms);
    if (nounClass.equals("3") || nounClass.equals("4")) {
      forms.put("sg.voc.len", forms.get("sg.nom.len"));
      forms.put("pl.voc.len", forms.get("pl.nom.len"));
    }
    for (String bf : BASEFORMS) {
      forms.put(bf + ".defart", mutate(forms.get(bf), getDefArtMutation(gender, bf)));
    }
    String genderForDefArt = gender;
    if (Utils.isVowel(stem.charAt(0))) {
      genderForDefArt += "v";
    } else if (stem.toLowerCase().charAt(0) == 's' && stem.length() >= 2 && Utils.isSLenitable(stem.charAt(1))) {
      genderForDefArt += "s";
    }
    return forms;
  }

  private static Map<String, String> getIrishFSTTags(Map<String, String> forms) {
    Map<String, String> out = new HashMap<>();
    boolean strong = (forms.get("pl.nom").equals(forms.get("pl.gen")));
    StringBuilder builder = new StringBuilder();
    if(forms.get("pos").equals("n"))  {
      builder.append("Noun");
    }
    if (forms.get("gender").equals("m")) {
      builder.append(":Masc");
    } else if (forms.get("gender").equals("f")) {
      builder.append(":Fem");
    }
    String base = builder.toString();
    for (String k : forms.keySet()) {
      if (!k.startsWith("sg") && !k.startsWith("pl")) {
        continue;
      }
      String[] tagParts = k.split("\\.");
      StringBuilder sb = new StringBuilder();
      sb.append(base);
      if (k.startsWith("pl.gen")) {
        sb.append(":Gen");
        if (strong) {
          sb.append(":Strong");
        } else {
          sb.append(":Weak");
        }
        sb.append(":Pl");
        sb.append(morphTag(k));
      } else {
        sb.append(':');
        sb.append(tagParts[1].toUpperCase().charAt(0));
        sb.append(tagParts[1].substring(1));
        sb.append(':');
        sb.append(tagParts[0].toUpperCase().charAt(0));
        sb.append(tagParts[0].substring(1));
        sb.append(morphTag(k));
      }
      out.put(k, sb.toString());
    }
    return out;
  }

  private static String morphTag(String s) {
    if (s.endsWith(".len")) {
      return ":Len";
    } else if (s.endsWith(".ecl")) {
      return ":Ecl";
    } else if (s.endsWith(".hpref")) {
      return ":hPref";
    } else if (s.endsWith(".defart")) {
      return ":DefArt";
    } else {
      return "";
    }
  }

  private static void addMutatedForms(Map<String, String> map) {
    for (String s : BASEFORMS) {
      String key = s + ".len";
      String len = Utils.lenite(map.get(s));
      map.put(key, len);
    }
    for (String s : BASEFORMS) {
      String key = s + ".ecl";
      String ecl = Utils.eclipse(map.get(s));
      map.put(key, ecl);
    }
    for (String s : BASEFORMS) {
      if (Utils.isVowel(map.get(s).charAt(0))) {
        map.put(s + ".hpref", "h" + map.get(s));
      }
    }
  }

  private static String mutate(String word, String mutation) {
    if (mutation == null) {
      return word;
    }
    if (mutation.equals("len")) {
      return Utils.lenite(word);
    } else if (mutation.equals("ecl")) {
      return Utils.eclipse(word);
    } else if (mutation.equals("hpref")) {
      return "h" + word;
    } else {
      return word;
    }
  }

  public static String guessIrishFSTNounClassSimple(String ending) {
    Matcher m = NOUN_PATTERN.matcher(ending);
    if(m.find()) {
      return getIrishFSTNounClass(m.group(2));
    } else {
      return "";
    }
  }
  static String getIrishFSTNounClass(String ending) {
    switch (ending) {
      case "óir":
      case "eoir":
      case "éir":
      case "úir":
      case "aeir":
        return "Nm3-1";
      case "álaí":
      case "eálaí":
        return "Nm4-4";
    }
    return null;
  }
  static String getIrishFSTAdjClass(String ending) {
    switch (ending) {
      case "iúil":
      case "úil":
        return "Adj2-1";
      case "each":
      case "ach":
        return "Adj3-1";
      case "aíoch":
      case "íoch":
        return "Adj1-4";
      default:
        return "";
    }
  }

  private static String getDefArtMutation(String gender, String form) {
    if (gender.equals("f")) {
      switch (form) {
        case "nom.sg":
          return "len";
        case "nom.pl":
          return "";
        case "gen.sg":
          return "";
        case "gen.pl":
          return "ecl";
        case "voc.pl":
          return "";
      }
    } else if (gender.equals("fv")) {
      switch (form) {
        case "nom.sg":
          return "";
        case "nom.pl":
          return "hpref";
        case "gen.sg":
          return "hpref";
        case "gen.pl":
          return "ecl";
        case "voc.pl":
          return "";
      }
    } else if (gender.equals("fs")) {
      switch (form) {
        case "nom.pl":
        case "gen.sg":
        case "gen.pl":
        case "voc.pl":
          return "";
        case "nom.sg":
          return "tpref";
      }
    } else if (gender.equals("m")) {
      switch (form) {
        case "nom.sg":
          return "";
        case "nom.pl":
          return "";
        case "gen.sg":
          return "len";
        case "gen.pl":
          return "ecl";
        case "voc.pl":
          return "";
      }
    } else if (gender.equals("mv")) {
      switch (form) {
        case "nom.sg":
          return "tpref";
        case "nom.pl":
          return "hpref";
        case "gen.sg":
          return "hpref";
        case "gen.pl":
          return "ecl";
        case "voc.pl":
          return "";
      }
    } else if (gender.equals("ms")) {
      switch (form) {
        case "nom.sg":
        case "nom.pl":
        case "gen.pl":
        case "voc.pl":
          return "";
        case "gen.sg":
          return "tpref";
      }
    }
    return null;
  }

  public static Map<String, String> extractEnWiktionaryNounTemplate(String tpl) {
    Map<String, String> out = new HashMap<>();
    if (!tpl.contains("{{") && !tpl.contains("}}")) {
      return out;
    }
    int start = tpl.indexOf("{{") + 2;
    int end = tpl.indexOf("}}", start);
    String inner = tpl.substring(start, end);
    String[] parts = inner.split("\\|");
    if(parts[0].equals("ga-decl-m3") && parts.length >= 4) {
      out.put("class", "m3");
      out.put("stem", parts[1]);
      out.put("sg.nom", parts[2]);
      out.put("sg.gen", parts[3]);
      if (parts.length == 4) {
        out.put("pl.nom", parts[3]);
        out.put("pl.gen", parts[3]);
      } else if (parts.length == 5) {
        out.put("pl.nom", parts[3]);
        out.put("pl.gen", parts[4]);
      } else if (parts.length == 6) {
        out.put("pl.nom", parts[4]);
        out.put("pl.gen", parts[5]);
      }
    }
    return out;
  }


  static String getEndingsRegex (Map<String, String[]> map) {
    List<String> endings = new ArrayList<>(map.size());
    endings.addAll(map.keySet());
    Collections.sort(endings, Comparator.comparingInt(String::length).reversed());
    return "(.+)(" + String.join("|", endings) + ")$";
  }
}
