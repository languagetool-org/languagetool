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
package de.danielnaber.languagetool.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.tools.StringTools.XmlPrintMode;

public final class Tools {

  private static final int DEFAULT_CONTEXT_SIZE = 45;

  private Tools() {
    // cannot construct, static methods only
  }

  /**
   * Tags text using the LanguageTool tagger.
   * 
   * @param contents
   *          Text to tag.
   * @param lt
   *          LanguageTool instance
   * @throws IOException
   */
  public static void tagText(final String contents, final JLanguageTool lt)
  throws IOException {
    AnalyzedSentence analyzedText;
    final List<String> sentences = lt.sentenceTokenize(contents);
    for (final String sentence : sentences) {
      analyzedText = lt.getAnalyzedSentence(sentence);
      System.out.println(analyzedText.toString());
    }
  }

  public static int checkText(final String contents, final JLanguageTool lt)
  throws IOException {
    return checkText(contents, lt, false, -1, 0, 0, StringTools.XmlPrintMode.NORMAL_XML);
  }

  public static int checkText(final String contents, final JLanguageTool lt, final int lineOffset)
  throws IOException {
    return checkText(contents, lt, false, -1, lineOffset, 0, StringTools.XmlPrintMode.NORMAL_XML);
  }

  public static int checkText(final String contents, final JLanguageTool lt,
      final boolean apiFormat, final int lineOffset) throws IOException {
    return checkText(contents, lt, apiFormat, -1, lineOffset, 0, StringTools.XmlPrintMode.NORMAL_XML);
  }

  /**
   * Check the given text and print results to System.out.
   * 
   * @param contents
   *          a text to check (may be more than one sentence)
   * @param lt
   *        Initialized LanguageTool
   * @param apiFormat
   *          whether to print the result in a simple XML format
   * @param contextSize
   *          error text context size: -1 for default
   * @param lineOffset
   *          line number offset to be added to line numbers in matches
   * @param prevMatches
   *          number of previously matched rules
   * @param xmlMode
   *          mode of xml printout for simple xml output
   * @return
   *      Number of rule matches to the input text.
   * @throws IOException
   */
  public static int checkText(final String contents, final JLanguageTool lt,
      final boolean apiFormat, int contextSize, final int lineOffset, 
      final int prevMatches, final XmlPrintMode xmlMode) throws IOException {
    if (contextSize == -1) {
      contextSize = DEFAULT_CONTEXT_SIZE;
    }
    final long startTime = System.currentTimeMillis();
    final List<RuleMatch> ruleMatches = lt.check(contents);
    // adjust line numbers
    for (RuleMatch r : ruleMatches) {
      r.setLine(r.getLine() + lineOffset);
      r.setEndLine(r.getEndLine() + lineOffset);
    }
    if (apiFormat) {
      final String xml = StringTools.ruleMatchesToXML(ruleMatches, contents,
          contextSize, xmlMode);
      System.out.print(xml);
    } else {
      int i = 1;
      for (final RuleMatch match : ruleMatches) {
        String output = i + prevMatches + ".) Line " + (match.getLine() + 1) + ", column "
        + match.getColumn() + ", Rule ID: " + match.getRule().getId();
        if (match.getRule() instanceof PatternRule) {
          final PatternRule pRule = (PatternRule) match.getRule();
          output += "[" + pRule.getSubId() + "]";
        }
        System.out.println(output);
        String msg = match.getMessage();
        msg = msg.replaceAll("<suggestion>", "'");
        msg = msg.replaceAll("</suggestion>", "'");
        System.out.println("Message: " + msg);
        final List<String> repl = match.getSuggestedReplacements();
        if (!repl.isEmpty()) {
          System.out.println("Suggestion: "
              + StringTools.listToString(repl, "; "));
        }
        System.out.println(StringTools.getContext(match.getFromPos(), match
            .getToPos(), contents, contextSize));
        if (i < ruleMatches.size()) {
          System.out.println();
        }
        i++;
      }
    }

    //display stats if it's not in a buffered mode
    if (xmlMode == StringTools.XmlPrintMode.NORMAL_XML) {
      final long endTime = System.currentTimeMillis();
      final long time = endTime - startTime;
      final float timeInSeconds = time / 1000.0f;
      final float sentencesPerSecond = lt.getSentenceCount() / timeInSeconds;
      if (apiFormat) {
        System.out.println("<!--");
      }
      System.out.printf(Locale.ENGLISH,
          "Time: %dms for %d sentences (%.1f sentences/sec)", time, lt
          .getSentenceCount(), sentencesPerSecond);
      System.out.println();
      if (apiFormat) {
        System.out.println("-->");
      }
    }
    return ruleMatches.size();
  }

  /**
   *  Automatically applies suggestions to the text.
   *  Note: if there is more than one suggestion, always the first
   *  one is applied, and others ignored silently.
   *
   *  @param
   *    contents - String to be corrected
   *  @param
   *    lt - Initialized LanguageTool object
   *  @return
   *    Corrected text as String.
   */
  public static String correctText(final String contents, final JLanguageTool lt) throws IOException {
    final List<RuleMatch> ruleMatches = lt.check(contents);
    if (!ruleMatches.isEmpty()) {
      final StringBuilder sb = new StringBuilder(contents);
      //build error list:
      List<String> errors = new ArrayList<String>();
      for (RuleMatch rm : ruleMatches) {
        final List<String> replacements = rm.getSuggestedReplacements();
        if (!replacements.isEmpty()) {
          errors.add(sb.substring(rm.getFromPos(), rm.getToPos()));
        }
      }
      int offset = 0;
      int counter = 0;
      for (RuleMatch rm : ruleMatches) {
        final List<String> replacements = rm.getSuggestedReplacements();
        if (!replacements.isEmpty()) {
          //make sure the error hasn't been already corrected:
          if (errors.get(counter).equals(sb.substring(rm.getFromPos() - offset, rm.getToPos() - offset))) {
            sb.replace(rm.getFromPos() - offset,
                rm.getToPos() - offset, replacements.get(0));
            offset += (rm.getToPos() - rm.getFromPos())
            - replacements.get(0).length();
          }
          counter++;
        }
      }
      return sb.toString();
    }
    return contents;
  }

  public static InputStream getInputStream(final String resourcePath)
  throws IOException {
    try {
      // try the URL first:
      final URL url = new URL(resourcePath);
      // success, load the resource.
      return url.openStream();
    } catch (final MalformedURLException e) {
      // no luck. Fallback to class loader paths.
    }
    // try file path:
    final File f = new File(resourcePath);
    if (f.exists() && f.isFile() && f.canRead()) {
      return new BufferedInputStream(new FileInputStream(f));
    }
    throw new IOException(
        "Could not open input stream from URL/resource/file: "
        + f.getAbsolutePath());
  }

  /**
   * Get a stacktrace as a string.
   */
  public static String getFullStackTrace(final Throwable e) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  /**
   * Load a file form the classpath using getResourceAsStream().
   * 
   * @param filename
   * @return the stream of the file
   * @throws IOException
   *           if the file cannot be loaded
   */
  public static InputStream getStream(final String filename) throws IOException {
    // the other ways to load the stream like
    // "Tools.class.getClass().getResourceAsStream(filename)"
    // don't work in a web context (using Grails):
    final InputStream is = Tools.class.getResourceAsStream(filename);
    if (is == null) {
      throw new IOException("Could not load file from classpath : " + filename);
    }
    return is;
  }

}
