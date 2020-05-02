package org.languagetool.synthesis;

/* Soros interpreter (see numbertext.org)
 * 2009-2010 (c) László Németh
 * License: LGPL/BSD dual license */

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public class Soros {
  private ArrayList<Pattern> patterns = new ArrayList<Pattern>();
  private ArrayList<String> values = new ArrayList<String>();
  private ArrayList<Boolean> begins = new ArrayList<Boolean>();
  private ArrayList<Boolean> ends = new ArrayList<Boolean>();

  private static String m = "\\\";#";
  private static String m2 = "$()|[]";
  private static String c = "\uE000\uE001\uE002\uE003";
  private static String c2 = "\uE004\uE005\uE006\uE007\uE008\uE009";
  private static String slash = "\uE000";
  private static String pipe = "\uE003";

  // pattern to recognize function calls in the replacement string

  private static Pattern func = Pattern.compile(translate(
        "(?:\\|?(?:\\$\\()+)?" +                // optional nested calls
        "(\\|?\\$\\(([^\\(\\)]*)\\)\\|?)" +     // inner call (2 subgroups)
        "(?:\\)+\\|?)?",                        // optional nested calls
        m2.substring(0, c.length()), c, "\\")); // \$, \(, \), \| -> \uE000..\uE003

  private boolean numbertext = false;

  public Soros(String source, String lang) {
    source = translate(source, m, c, "\\");      // \\, \", \;, \# -> \uE000..\uE003
    // switch off all country-dependent lines, and switch on the requested ones
    source = source.replaceAll("(^|[\n;])([^\n;#]*#[^\n]*\\[:[^\n:\\]]*:][^\n]*)", "$1#$2")
        .replaceAll("(^|[\n;])#([^\n;#]*#[^\n]*\\[:" + lang.replace('_', '-') + ":][^\n]*)", "$1$2")
        .replaceAll("(#[^\n]*)?(\n|$)", ";");   // remove comments
    if (source.indexOf("__numbertext__") == -1)
        source = "__numbertext__;" + source;
    source = source.replace("__numbertext__",
        // default left zero deletion
        "\"([a-z][-a-z]* )?0+(0|[1-9]\\d*)\" $(\\1\\2);" +
        // separator function
        "\"\uE00A(.*)\uE00A(.+)\uE00A(.*)\" \\1\\2\\3;" +
        // no separation, if subcall returns with empty string
        "\"\uE00A.*\uE00A\uE00A.*\"");

    Pattern p = Pattern.compile("^\\s*(\"[^\"]*\"|[^\\s]*)\\s*(.*[^\\s])?\\s*$");
    Pattern macro = Pattern.compile("== *(.*[^ ]?) ==");
    String prefix = "";
    for (String s : source.split(";")) {
        Matcher matchmacro = macro.matcher(s);
        if (matchmacro.matches()) {
            prefix = matchmacro.group(1);
            continue;
        }
        Matcher sp = p.matcher(s);
        if (!prefix.equals("") && !s.equals("") && sp.matches()) {
            s = sp.group(1).replaceFirst("^\"", "").replaceFirst("\"$","");
            s = "\"" + (s.startsWith("^") ? "^" : "") + prefix + (s.equals("") ? "" : " ") +
                 s.replaceFirst("^\\^", "") + "\" " + sp.group(2);
            sp = p.matcher(s);
        }
        if (!s.equals("") && sp.matches()) {
            s = translate(sp.group(1).replaceFirst("^\"", "").replaceFirst("\"$",""),
                c.substring(1), m.substring(1), "");
            s = s.replace(slash, "\\\\"); // -> \\, ", ;, #
            String s2 = "";
            if (sp.group(2) != null) s2 = sp.group(2).replaceFirst("^\"", "").replaceFirst("\"$","");
            s2 = translate(s2, m2, c2, "\\");   // \$, \(, \), \|, \[, \] -> \uE004..\uE009
            // call inner separator: [ ... $1 ... ] -> $(\uE00A ... \uE00A$1\uE00A ... )
            s2 = s2.replaceAll("^\\[[$](\\d\\d?|\\([^\\)]+\\))", "\\$(\uE00A\uE00A|\\$$1\uE00A") // add "|"
                .replaceAll("\\[([^$\\[\\\\]*)[$](\\d\\d?|\\([^\\)]+\\))", "\\$(\uE00A$1\uE00A\\$$2\uE00A")
                .replaceAll("\uE00A\\]$","|\uE00A)") // add "|" in terminating position
                .replaceAll("\\]", ")")
                .replaceAll("(\\$\\d|\\))\\|\\$", "$1||\\$"); // $()|$() -> $()||$()
            s2 = translate(s2, c, m, "");       // \uE000..\uE003-> \, ", ;, #
            s2 = translate(s2, m2.substring(0, c.length()), c, "");      // $, (, ), | -> \uE000..\uE003
            s2 = translate(s2, c2, m2, "");     // \uE004..\uE009 -> $, (, ), |, [, ]
            s2 = s2.replaceAll("[$]", "\\$")    // $ -> \$
                .replaceAll("\uE000(\\d)", "\uE000\uE001\\$$1\uE002") // $n -> $(\n)
                .replaceAll("\\\\(\\d)", "\\$$1") // \[n] -> $[n]
                .replace("\\n", "\n");            // \n -> [new line]
            patterns.add(Pattern.compile("^" + s.replaceFirst("^\\^", "")
                .replaceFirst("\\$$", "") + "$"));
            begins.add(s.startsWith("^"));
            ends.add(s.endsWith("$"));
            values.add(s2);
        }
    }
  }

  public String run(String input) {
    return run(input, true, true);
  }

  private String run(String input, boolean begin, boolean end) {
    for (int i = 0; i < patterns.size(); i++) {
        if ((!begin && begins.get(i)) || (!end && ends.get(i))) continue;
        Matcher m = patterns.get(i).matcher(input);
        if (!m.matches()) continue;
        String s = m.replaceAll(values.get(i));
        Matcher n = func.matcher(s);
        while (n.find()) {
            boolean b = false;
            boolean e = false;
            if (n.group(1).startsWith(pipe) || n.group().startsWith(pipe)) b = true;
            else if (n.start() == 0) b = begin;
            if (n.group(1).endsWith(pipe) || n.group().endsWith(pipe)) e = true;
            else if (n.end() == s.length()) e = end;
            s = s.substring(0, n.start(1)) + run(n.group(2), b, e) + s.substring(n.end(1));
            n = func.matcher(s);
        }
        return s;
    }
    return "";
  }

  private static String translate(String s, String chars, String chars2, String delim) {
    for (int i = 0; i < chars.length(); i++) {
        s = s.replace(delim + chars.charAt(i), "" + chars2.charAt(i));
    }
    return s;
  }
}