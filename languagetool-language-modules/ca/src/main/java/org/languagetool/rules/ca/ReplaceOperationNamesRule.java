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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.*;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

/**
 * A rule that suggests better names for technical operation names
 * 
 * Loads the relevant words from <code>/ca/replace_operationnames.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class ReplaceOperationNamesRule extends Rule {

  private static final String FILE_NAME = "/ca/replace_operationnames.txt";
  // locale used on case-conversion
  private static final Locale CA_LOCALE = new Locale("CA");
  
  private static final String FILE_ENCODING = "utf-8";
  protected final Map<String, List<String>> possibleWrongWords;
  
  private final CatalanSynthesizer synth;
  
  private static final Pattern PrevToken_POS = Pattern.compile("D[^R].*|PX.*|SPS00|SENT_START");
  private static final Pattern PrevToken_POS_Excep = Pattern.compile("RG_anteposat|N.*|CC|_PUNCT.*|_loc_unavegada|RN");
  private static final Pattern NextToken_POS_Excep = Pattern.compile("N.*");
  
  private static final Pattern PUNTUACIO = Pattern.compile("PUNCT.*|SENT_START");
  private static final Pattern DETERMINANT = Pattern.compile("D[^R].M.*");

  public final String getFileName() {
    return FILE_NAME;
  }
  
  public String getEncoding() {
    return FILE_ENCODING;
  }
  
  public ReplaceOperationNamesRule(final ResourceBundle messages) throws IOException {
    super.setLocQualityIssueType(ITSIssueType.Style);
    super.setCategory(new Category(new CategoryId("FORMES_SECUNDARIES"), "C8) Formes secundàries"));
    possibleWrongWords = loadWords(JLanguageTool.getDataBroker()
        .getFromRulesDirAsStream(getFileName()));
    synth = new CatalanSynthesizer();
  }  

  @Override
  public final String getId() {
    return "NOMS_OPERACIONS";
  }

 @Override
  public String getDescription() {
    return "Noms d'operació tècnica: buidat/buidatge";
  }

  public String getShort() {
    return "Forma preferible";
  }
  
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Si és el nom d'una operació tècnica, val més usar una altra forma.";
  }

  public Locale getLocale() {
    return CA_LOCALE;
  }
  
  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    loop: for (int i=1; i<tokens.length; i++) {

      List<String> replacementLemmas = null; 

      String token = tokens[i].getToken().toLowerCase();
      
      if (token.length()>3 && token.endsWith("s")) {
        token = token.substring(0, token.length() - 1);
      }
      if (possibleWrongWords.containsKey(token)) {
        replacementLemmas = possibleWrongWords.get(token);
      } else {
        continue loop;
      }
      
      // exceptions
      if (token.equals("duplicat") && tokens[i-1].getToken().equalsIgnoreCase("per")) {
        continue loop;
      }
      // Assecat el braç del riu
      if (i + 1 < tokens.length &&
          matchPostagRegexp(tokens[i - 1], PUNTUACIO) &&
          matchPostagRegexp(tokens[i + 1], DETERMINANT)) {
        continue loop;
      }
      
      
      // relevant token
      if (tokens[i].hasPosTag("_GV_")) {
        continue loop;
      }
  
      // next token
      if (i + 1 < tokens.length
          && (tokens[i + 1].hasLemma("per") || tokens[i + 1].hasLemma("com")
              || tokens[i + 1].hasLemma("des") || tokens[i + 1].hasLemma("amb") 
              || matchPostagRegexp(tokens[i + 1], NextToken_POS_Excep))) {
        continue loop;
      }
      
      // prev token
      if (!matchPostagRegexp(tokens[i - 1], PrevToken_POS)
          || matchPostagRegexp(tokens[i - 1], PrevToken_POS_Excep)) {
        continue loop;
      }
      
      // The rule matches!
      //synthesize replacements
      
      if (replacementLemmas != null) {
        List<String> possibleReplacements = new ArrayList<>();
        String[] synthesized = null;

        if (!tokens[i].getToken().toLowerCase().endsWith("s")) {
          possibleReplacements.addAll(replacementLemmas);
        } else { 
          //synthesize plural
          for (String replacementLemma : replacementLemmas) {
            synthesized = synth.synthesize(new AnalyzedToken (replacementLemma,"NCMS000", replacementLemma), "NC.P.*");
            possibleReplacements.addAll(Arrays.asList(synthesized));
          }
        }
        if (possibleReplacements.size() > 0) {
            RuleMatch potentialRuleMatch = createRuleMatch(tokens[i],possibleReplacements);
            ruleMatches.add(potentialRuleMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }


  private RuleMatch createRuleMatch(AnalyzedTokenReadings tokenReadings,
      List<String> replacements) {
    String tokenString = tokenReadings.getToken();
    int pos = tokenReadings.getStartPos();

    RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos
        + tokenString.length(), getMessage(tokenString, replacements), getShort());

    if (StringTools.startsWithUppercase(tokenString)) {
      for (int i = 0; i < replacements.size(); i++) {
        replacements
            .set(i, StringTools.uppercaseFirstChar(replacements.get(i)));
      }
    }

    potentialRuleMatch.setSuggestedReplacements(replacements);

    return potentialRuleMatch;
  }


  private Map<String, List<String>> loadWords(final InputStream stream)
      throws IOException {
    Map<String, List<String>> map = new HashMap<>();

    try (Scanner scanner = new Scanner(stream, getEncoding())) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.isEmpty() || line.charAt(0) == '#') { // # = comment
          continue;
        }
        String[] parts = line.split("=");
        if (parts.length != 2) {
          throw new IOException("Format error in file "
                  + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(
                  getFileName()) + ", line: " + line);
        }

        String[] replacements = parts[1].split("\\|");

        // multiple incorrect forms
        final String[] wrongForms = parts[0].split("\\|");
        for (String wrongForm : wrongForms) {
          map.put(wrongForm, Arrays.asList(replacements));
        }
      }
    }
    return map;
  }

  @Override
  public void reset() {
  }
  
  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    boolean matches = false;
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        matches = true;
        break;
      }
    }
    return matches;
  }

}
