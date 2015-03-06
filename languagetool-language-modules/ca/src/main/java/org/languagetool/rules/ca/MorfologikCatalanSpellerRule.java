/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.ca.CatalanTagger;

public final class MorfologikCatalanSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/ca/catalan.dict";
  private static final Pattern APOSTROF_INICI_VERBS = Pattern.compile("^([lnmts])(h?[aeiouàéèíòóú].+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_SING = Pattern.compile("^([ld])(h?[aeiouàéèíòóú].+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_PLURAL = Pattern.compile("^(d)(h?[aeiouàéèíòóú].+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_FINAL = Pattern.compile("^(.+[aei])(l|ls|m|n|ns|s|t)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern GUIONET_FINAL = Pattern.compile("^(.+)(hi|ho|la|les|li|lo|los|me|ne|nos|se|te|vos)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V.[SI].*");
  private static final Pattern NOM_SING = Pattern.compile("V.[NG].*|V.P..S..|N..[SN].*|A...[SN].");
  private static final Pattern NOM_PLURAL = Pattern.compile("V.P..P..|N..[PN].*|A...[PN].");
  private static final Pattern VERB_INFGERIMP = Pattern.compile("V.[NGM].*");

  public MorfologikCatalanSpellerRule(ResourceBundle messages, Language language)
      throws IOException {
    super(messages, language);
    this.setIgnoreTaggedWords();
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_CA_ES";
  }
  
  @Override
  // Use this rule in LO/OO extension despite being a spelling rule
  public boolean useInOffice() {
    return true;
  }
  
  @Override
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) throws IOException {
    //TODO try other combinations. Ex. daconseguirlos
    CatalanTagger tagger=new CatalanTagger();
    Matcher matcher=APOSTROF_INICI_VERBS.matcher(word);
    if (matcher.matches()) {
      String newSuggestion=matcher.group(2);
      if (matchPostagRegexp(tagger.tag(Arrays.asList(newSuggestion)).get(0),VERB_INDSUBJ)) {
        return Collections.singletonList(matcher.group(1)+"'"+matcher.group(2));
      }
    }
    matcher=APOSTROF_INICI_NOM_SING.matcher(word);
    if (matcher.matches()) {
      String newSuggestion=matcher.group(2);
      if (matchPostagRegexp(tagger.tag(Arrays.asList(newSuggestion)).get(0),NOM_SING)) {
        return Collections.singletonList(matcher.group(1)+"'"+matcher.group(2));
      }
    }
    matcher=APOSTROF_INICI_NOM_PLURAL.matcher(word);
    if (matcher.matches()) {
      String newSuggestion=matcher.group(2);
      if (matchPostagRegexp(tagger.tag(Arrays.asList(newSuggestion)).get(0),NOM_PLURAL)) {
        return Collections.singletonList(matcher.group(1)+"'"+matcher.group(2));
      }
    }
    matcher=APOSTROF_FINAL.matcher(word);
    if (matcher.matches()) {
      String newSuggestion=matcher.group(1);
      if (matchPostagRegexp(tagger.tag(Arrays.asList(newSuggestion)).get(0), VERB_INFGERIMP)) {
        return Collections.singletonList(matcher.group(1) + "'" + matcher.group(2));
      }
    }
    matcher=GUIONET_FINAL.matcher(word);
    if (matcher.matches()) {
      String newSuggestion=matcher.group(1);
      if (matchPostagRegexp(tagger.tag(Arrays.asList(newSuggestion)).get(0), VERB_INFGERIMP)) {
        return Collections.singletonList(matcher.group(1)+"-"+matcher.group(2));
      }
    }
    return Collections.emptyList();
  }
 

  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return true;
      }
    }
    return false;
  }

}
