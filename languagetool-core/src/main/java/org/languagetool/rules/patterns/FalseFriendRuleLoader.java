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
package org.languagetool.rules.patterns;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Loads {@link PatternRule}s from a false friends XML file.
 * 
 * @author Daniel Naber
 */
public class FalseFriendRuleLoader extends DefaultHandler {

  private static final LoadingCache<FalseFriendConfig, List<AbstractPatternRule>> cache = CacheBuilder.newBuilder()
          .expireAfterWrite(15, TimeUnit.MINUTES)
          .build(new CacheLoader<FalseFriendConfig, List<AbstractPatternRule>>() {
            @Override
            public List<AbstractPatternRule> load(@NotNull FalseFriendConfig ffConfig) throws Exception {
              try (InputStream is = this.getClass().getResourceAsStream(ffConfig.filename)) {
                if (is == null) {
                  try (InputStream is2 = new FileInputStream(ffConfig.filename)) {
                    return getRulesInternal(is2, ffConfig.language, ffConfig.motherTongue);
                  } catch (ParserConfigurationException | SAXException e) {
                    throw new IOException("Could not load false friend rules from " + ffConfig.filename, e);
                  }
                } else {
                  return getRulesInternal(is, ffConfig.language, ffConfig.motherTongue);
                }
              }
            }
          });

  private static List<AbstractPatternRule> getRulesInternal(InputStream stream,
      Language textLanguage, Language motherTongue)
      throws ParserConfigurationException, SAXException, IOException {
    FalseFriendRuleHandler handler = new FalseFriendRuleHandler(textLanguage, motherTongue);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    saxParser.parse(stream, handler);
    List<AbstractPatternRule> rules = handler.getRules();
    // Add suggestions to each rule:
    ResourceBundle messages = ResourceBundle.getBundle(JLanguageTool.MESSAGE_BUNDLE, motherTongue.getLocale());
    MessageFormat msgFormat = new MessageFormat(messages.getString("false_friend_suggestion"));
    for (AbstractPatternRule rule : rules) {
      List<String> suggestions = handler.getSuggestionMap().get(rule.getId());
      if (suggestions != null) {
        String[] msg = { formatSuggestions(suggestions) };
        rule.setMessage(rule.getMessage() + " " + msgFormat.format(msg));
      }
    }
    return rules;
  }

  private static String formatSuggestions(List<String> l) {
    return l.stream().map(o -> "<suggestion>" + o + "</suggestion>").collect(Collectors.joining(", "));
  }

  /**
   * @param filename XML file with false friend rules
   * @since 4.2
   */
  public final List<AbstractPatternRule> getRules(String filename, Language language, Language motherTongue) throws IOException, ParserConfigurationException, SAXException {
    return cache.getUnchecked(new FalseFriendConfig(filename, language, motherTongue));
  }
  
  private class FalseFriendConfig {
    
    private String filename;
    private Language language;
    private Language motherTongue;
    
    private FalseFriendConfig(String filename, Language language, Language motherTongue) {
      this.filename = Objects.requireNonNull(filename);
      this.language = Objects.requireNonNull(language);
      this.motherTongue = Objects.requireNonNull(motherTongue);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      FalseFriendConfig that = (FalseFriendConfig) o;
      if (!filename.equals(that.filename)) return false;
      if (!language.equals(that.language)) return false;
      return motherTongue.equals(that.motherTongue);
    }

    @Override
    public int hashCode() {
      int result = filename.hashCode();
      result = 31 * result + language.hashCode();
      result = 31 * result + motherTongue.hashCode();
      return result;
    }
  }
  
}
