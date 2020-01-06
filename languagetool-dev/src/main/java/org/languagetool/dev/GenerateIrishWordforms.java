package org.languagetool.dev;

import org.languagetool.tagging.ga.Utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateIrishWordforms {
  private static final Map<String, String[]> nounGuesses = new HashMap<>();
  static {
    nounGuesses.put("óir", new String[]{"m", "óir", "óra", "óirí", "óirí"});
    nounGuesses.put("eoir", new String[]{"m", "eoir", "eora", "eoirí", "eoirí"});
    nounGuesses.put("éir", new String[]{"m", "éir", "éara", "éirí", "éirí"});
    nounGuesses.put("úir", new String[]{"m", "úir", "úra", "úirí", "úirí"});
    nounGuesses.put("aeir", new String[]{"m", "aeir", "aera", "aeirí", "aeirí"});
    nounGuesses.put("álaí", new String[]{"m", "álaí", "álaí", "álaithe", "álaithe"});
    nounGuesses.put("eálaí", new String[]{"m", "eálaí", "eálaí", "eálaithe", "eálaithe"});
  }
  private static final String NOUN_ENDINGS_REGEX = getEndingsRegex(nounGuesses);
  private static final Pattern NOUN_PATTERN = Pattern.compile(NOUN_ENDINGS_REGEX);

  public static void expandNounForms(String stem, String[] parts) {
    String gender = parts[0];
    Map<String, String> forms = new HashMap<>();
    forms.put("sg.nom", stem + parts[1]);
    forms.put("sg.gen", stem + parts[2]);
    forms.put("pl.nom", stem + parts[3]);
    forms.put("pl.gen", stem + parts[4]);
    //Not doing separate vocative yet
    //if (parts.length == 6) {}
    addMutatedForms(forms);
    boolean strong = (forms.get("pl.nom").equals(forms.get("pl.gen")));
    String genderForDefArt = gender;
    if (Utils.isVowel(stem.charAt(0))) {
      genderForDefArt += "v";
    } else if (stem.toLowerCase().charAt(0) == 's' && stem.length() >= 2 && Utils.isSLenitable(stem.charAt(1))) {
      genderForDefArt += "s";
    }

  }
  private static void addMutatedForms(Map<String, String> map) {
    String[] baseforms = {"sg.nom", "sg.gen", "pl.nom", "pl.gen"};
    for (String s : baseforms) {
      String key = s + ".len";
      String len = Utils.lenite(map.get(s));
      map.put(key, len);
    }
    for (String s : baseforms) {
      String key = s + ".ecl";
      String ecl = Utils.eclipse(map.get(s));
      map.put(key, ecl);
    }
    for (String s : baseforms) {
      map.put(s + ".hpref", "h" + map.get(s));
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


  static String getEndingsRegex (Map<String, String[]> map) {
    List<String> endings = new ArrayList<>(map.size());
    endings.addAll(map.keySet());
    Collections.sort(endings, Comparator.comparingInt(String::length).reversed());
    return "(.+)(" + String.join("|", endings) + ")$";
  }
}
