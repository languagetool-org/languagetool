package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.EmptyLineRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.LongParagraphRule;
import org.languagetool.rules.LongSentenceRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.ParagraphRepeatBeginningRule;
import org.languagetool.rules.PunctuationMarkAtParagraphEnd;
import org.languagetool.rules.PunctuationMarkAtParagraphEnd2;
import org.languagetool.rules.Rule;
import org.languagetool.rules.SentenceWhitespaceRule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhiteSpaceAtBeginOfParagraph;
import org.languagetool.rules.WhiteSpaceBeforeParagraphEnd;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.no.NorwegianNBTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Norwegian extends Language {

  private String shortCode = "no";

  @Override
  public String getName() {
    return "Norwegian";
  }

  @Override
  public String getShortCode() {
    return shortCode;
  }

  @Override
  public String[] getCountries() {
    return new String[]{"NO"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new NorwegianNBTagger();
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return new NorwegianNB();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return super.createDefaultWordTokenizer();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Adthena")};
  }

  @Override
  public List<String> getRuleFileNames() {
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    return Arrays.asList(dataBroker.getRulesDir() + "/" + shortCode + "/" + JLanguageTool.PATTERN_FILE);
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Arrays.asList(
      new CommaWhitespaceRule(messages,
        Example.wrong("We had coffee<marker> ,</marker> cheese and crackers and grapes."),
        Example.fixed("We had coffee<marker>,</marker> cheese and crackers and grapes.")),
      new DoublePunctuationRule(messages),
      new UppercaseSentenceStartRule(messages, this,
        Example.wrong("This house is old. <marker>it</marker> was built in 1950."),
        Example.fixed("This house is old. <marker>It</marker> was built in 1950.")),
      new MultipleWhitespaceRule(messages, this),
      new SentenceWhitespaceRule(messages),
      new WhiteSpaceBeforeParagraphEnd(messages, this),
      new WhiteSpaceAtBeginOfParagraph(messages),
      new EmptyLineRule(messages, this),
      new LongSentenceRule(messages, userConfig, 33, true, true),
      new LongParagraphRule(messages, this, userConfig),
      new ParagraphRepeatBeginningRule(messages, this),
      new PunctuationMarkAtParagraphEnd(messages, this),
      new PunctuationMarkAtParagraphEnd2(messages, this)
    );
  }
}
