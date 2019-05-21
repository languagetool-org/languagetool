/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Fred Kruse
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
package org.languagetool.openoffice;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.UserConfig;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Class to switch between running LanguageTool in multi or single thread mode
 * @since 4.6
 * @author Fred Kruse
 */
public class SwJLanguageTool {
  
  boolean isMultiThread = false;
  JLanguageTool lt = null;
  MultiThreadedJLanguageTool mlt = null;

  public SwJLanguageTool(Language language, Language motherTongue, UserConfig userConfig, boolean isMulti) {
    isMultiThread = isMulti;
    if(isMultiThread) {
      mlt = new MultiThreadedJLanguageTool(language, motherTongue, userConfig); 
    } else {
      lt = new JLanguageTool(language, motherTongue, null, userConfig); 
    }
  }

  public List<Rule> getAllRules() {
    if(isMultiThread) {
      return mlt.getAllRules(); 
    } else {
      return lt.getAllRules(); 
    }
  }

  public List<Rule> getAllActiveOfficeRules() {
    if(isMultiThread) {
      return mlt.getAllActiveOfficeRules(); 
    } else {
      return lt.getAllActiveOfficeRules(); 
    }
  }

  public void enableRule(String ruleId) {
    if(isMultiThread) {
      mlt.enableRule(ruleId); 
    } else {
      lt.enableRule(ruleId); 
    }
  }

  public void disableRule(String ruleId) {
    if(isMultiThread) {
      mlt.disableRule(ruleId); 
    } else {
      lt.disableRule(ruleId); 
    }
  }

  public void disableCategory(CategoryId id) {
    if(isMultiThread) {
      mlt.disableCategory(id); 
    } else {
      lt.disableCategory(id); 
    }
  }

  public void activateLanguageModelRules(File indexDir) throws IOException {
    if(isMultiThread) {
      mlt.activateLanguageModelRules(indexDir); 
    } else {
      lt.activateLanguageModelRules(indexDir); 
    }
  }

  public List<RuleMatch> check(String text, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    if(isMultiThread) {
      return mlt.check(text, tokenizeText, paraMode); 
    } else {
      return lt.check(text, tokenizeText, paraMode); 
    }
  }

  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    if(isMultiThread) {
      synchronized(mlt) {
        return mlt.check(annotatedText, tokenizeText, paraMode);
      }
    } else {
      return lt.check(annotatedText, tokenizeText, paraMode); 
    }
  }

  public List<String> sentenceTokenize(String text) {
    if(isMultiThread) {
      return mlt.sentenceTokenize(text); 
    } else {
      return lt.sentenceTokenize(text); 
    }
  }

  public Language getLanguage() {
    if(isMultiThread) {
      return mlt.getLanguage(); 
    } else {
      return lt.getLanguage(); 
    }
  }

  

}
