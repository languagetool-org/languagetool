/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Luboš Lehotský lubo.lehotsky (at) gmail (dot) com
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

package de.danielnaber.languagetool.rules.sk;


import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @deprecated this is currently buggy, do not use
 */
@Deprecated
public class SlovakVesRule extends SlovakRule {

  public SlovakVesRule(final ResourceBundle messages) {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    setDefaultOff();
  }

  @Override
  public final String getId() {
    return "SK_VES";
  }

  @Override
  public final String getDescription() {
    return "Názvy obcí, v ktorých je \"Ves\"";
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    // never read        boolean prve_uvodzovky;
    boolean tag, tag2, tag3;
    final String pomoc;
    final char znak;

// never read         prve_uvodzovky = false;
    tag = false;
    tag2 = false;
    tag3 = false;

    pomoc = tokens[1].getToken();
    if (pomoc.length() >= 1) {
      znak = pomoc.charAt(0);
    } else {
      znak = '.';
    }

    if (znak == '?') {
// never read  prve_uvodzovky = true;
    }
    for (int i = 1; i < tokens.length; i++) {
      final String token = tokens[i].getToken();
// never read           String premenna = token.toString();
      final char pomocnik;
// never read           final int help; 
      boolean bodka;
      boolean pady;

      pady = false;
      pomocnik = token.charAt(0);
      bodka = false;
      if (token.charAt(0) == '.' || token.charAt(0) == '?'
              || token.charAt(0) == '!') {
        bodka = true;
      }

      if (tokens[i].hasPosTag("AAfs1x") || tokens[i].hasPosTag("AAfs2x")
              || tokens[i].hasPosTag("AAfs3x")
              || tokens[i].hasPosTag("AAfs4x")
              || tokens[i].hasPosTag("AAfs6x")
              || tokens[i].hasPosTag("AAfs7x")) {
        pady = true;
      }
      if (pady && Character.isUpperCase(pomocnik)) {
        tag = true;
      }

      if (tag && !tag2) {
        if (pady && Character.isLowerCase(pomocnik)) {
          tag2 = true;
          //                   premenna = tokens[i].getToken();
        }

      }

      if (tag2) {
        if (token.equals("Ves") || token.equals("Vsi")
                || token.equals("Vsou")) {
          tag3 = true;
        }
      }
      if (tag3 && !bodka) {
        String spravne;
        char prve;

        prve = tokens[i - 1].getToken().charAt(0);
        prve = Character.toUpperCase(prve);
        spravne = tokens[i - 1].getToken().substring(1,
                tokens[i - 1].getToken().length());

        final String msg = "Zmeňte začiatočné písmeno na veľké: <suggestion> "
                + prve + spravne + " </suggestion>";
        final int pos = tokens[i - 1].getStartPos();
        final int pos2 = tokens[i - 1].getToken().length();
        final RuleMatch ruleMatch = new RuleMatch(this, pos, pos + pos2,
                msg, "Zmeňte začiatočné písmeno na veľké: ");

        ruleMatches.add(ruleMatch);

      }

    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public void reset() {// nothing
  }

}

