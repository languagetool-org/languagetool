# LanguageTool Change Log

## 3.5 (2016-09-30)

#### Catalan
  * added and improved rules
  * added words suggested by users

#### English
  * added and improved rules
  * added about 50 confusion pairs like talking/taking (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data) 
  * added category `MISUSED_TERMS_EU_PUBLICATIONS`
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict

#### Esperanto
  * added and improved rules

#### French
  * added and improved rules

#### German
  * added rules
  * fixed several false alarms

#### Polish
  * added and improved rules

#### Portuguese (European)
  * added and improved rules
 
#### Portuguese (Brazilian)
  * added rules

#### Russian
  * now possible checking the texts with the signs of stress
  * added and improved many new grammar and style rules
  * added words suggested by users
  * improved disambiguation rules
  * for review, test and improve rules, feedback in bugtracker thanks to Konstantin Ladutenko

#### Spanish
  * added and improved rules

#### Ukrainian
  * added ~6k new words
  * added many new grammar and styling rules
  * added many new barbarism replacement suggestions
  * improved dynamic word tagging

#### General
  * Bugfix: avoid repeating the same suggestion
  * Enhancement: ignore e-mail addresses

#### Java API
  * `Rule.getCorrectExamples()` now returns a list of `CorrectExample`s
    instead of a list of `String`s.

#### GUI (stand-alone version)
  * speed up for long texts with many errors (#530)
  * add new menu item for showing/hiding the result area

#### Command-line
  * Deprecated the `--api` option - we recommend using LanguageTool
    in server mode (JSON API), which is faster as it has no start up
    overhead for each call. See https://languagetool.org/http-api/swagger-ui/#/default
    for a documentation of the new API.


## 3.4 (2016-06-27)

#### Catalan
  * added and improved rules
  * added words suggested by users
 
#### English
  * added about 33 confusion pairs such as throe/throw, raps/wraps (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data) 

#### French
  * upgraded dictionaries to Dicollecte-5.6
  * added 32 confusion pairs like pris/prix, quand/quant (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data) 

#### German
  * added some rules
  * improved handling of hyphenated compound words
  
#### Greek
  * added some rules

#### Polish
  * added and improved rules
  * removed some false alarms
  
#### Portuguese
  * added and improved rules

#### Spanish
  * added 14 confusion pairs like tubo/tuvo, ciento/siento (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data)
  * upgraded Hunspell dictionary to 2.1

#### Russian
  * rebuilt spellchecker dictionary
  * added words suggested by users
  * added and improved rules

#### Ukrainian
  * big dictionary update (thousands of new words and many fixes)
  * compound tagger improvements
  * several new rules and many improvements to existing ones
  * new token inflection agreement rule (still work-in-progress so turned off by default)
  * new replacement suggestions for barbarisms

#### Java API
  * some formerly deprecated code has been removed
  * all rules now have a category ("Misc" if the rule doesn't specify a category)
  * a new module `languagetool-http-client` has been added with a class
    `RemoteLanguageTool` that you can use to query a remote LanguageTool server
    via HTTP or HTTPS
  * removed the public modifier from `LanguageComboBox`

#### Embedded HTTPS server
  * The existing HTTP/HTTPS API will be replaced by a new one
    that returns JSON. This version of LanguageTool supports
    both APIs. The new API is prefixed with `/v2/`.
    It is documented at https://languagetool.org/http-api/swagger-ui/#/default.
    Please do not use the old XML-based HTTP API anymore. 
    Information about migrating from the old to the new API
    can be found at https://languagetool.org/http-api/migration.php
  * Changed behaviour for OutOfMemory situations: the server
    process now stops instead of being in an unstable state
  * Missing parameters (like `text`) now cause a `400 Bad Request`
    response (it used to produce `500 Internal Server Error`)
  * New parameter `preferredVariants` to specify which variant is preferred
    when the language is auto-detected: Example:
    `language=auto&preferredVariants=en-GB,de-AT` - if English text is detected,
    British English will be used, if German text is detected, German (Austria)
    will be used.
  * Code refactorings: methods have been removed without being deprecated first,
    e.g. in `LanguageToolHttpHandler`

#### Rule Syntax
  * groups of rules and categories are now required to have non-empty names
    to avoid user confusion

#### GUI (stand-alone version)
  * detect encoding of files with BOM header
  * add new menu to open recent files
  * add new configuration option to allow user to select the GUI language
  * preserve GUI state between program restarts

#### Command-line
  * detect encoding of files with BOM header when there is no `encoding` parameter


## 3.3 (2016-03-28)

#### Breton
  * small rule improvements

#### Catalan
  * added and improved rules
  * added words suggested by users
  * minor change in the format of the binary dictionary: POS tag and frequency data are no 
    longer separated by a separator character.

#### Dutch
  * small rule improvements and URL updates, thanks to Koen Vervloesem

#### English
  * added and improved rules, improved categorization of rules
  * added checks on date ranges
  * added about 215 confusion pairs like best/bets, wand/want (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data) 

#### Esperanto
  * improved several rules

#### French
  * added and improved rules

#### German
  * added and improved rules, improved categorization of rules
  * updated Hunspell dictionary to version 2015.12.28
    (http://extensions.libreoffice.org/extension-center/german-de-de-frami-dictionaries etc.)
  * added Spanish false friends
  * better suggestions for some errors that involve compounds

#### Greek
  * new rule for checking correct spell of ordinal numerals
  * added new XML rules

#### Polish
  * added and improved a large number of rules, largely improved disambiguation
  * upgraded the tagging and synthesis dictionaries to Morfologik Polimorf 2.1
  * improved tokenization of number ranges (such as 1-1234 or 1--10)
  * added checks on date ranges
    
#### Portuguese
  * added and improved rules, improved categorization of rules 
    
#### Russian
  * added and improved rules, improved categorization of rules
  * added words suggested by users
  
#### Spanish
  * added German false friends

#### Ukrainian
  * big dictionary update:
    * more than 202K lemmas
    * homonyms have been properly split
    * vocative case for inanimates has be added
    * list of barbarism has been updated
  * improved some rules
  * improved sentence tokenization
  * improved dynamic tagging for compounds
  * some improvements for disambiguation

#### Java API
  * some formerly deprecated code has been removed
  * added `acceptPhrases(List<String> phrases)` to `SpellingCheckRule`
    so you can avoid false alarms on names and technical terms
    that consist of more than one word.

#### Embedded HTTPS server
  * Speed up for input with short sentences
  * Added new parameters `enabledCategories` and `disabledCategories`
    that take a comma-separated list of categories to enable/disable.
    Fixes https://github.com/languagetool-org/languagetool/pull/326.
  * The output now contains a `shortmsg` attribute if available, which
    is a short version of the `msg` attribute.
  * The output now contains a `categoryid` attribute if available. It's
    supposed not to change in future versions (while `category` might
    change).

#### Command-line
  * new parameters `--enablecategories` and `--disablecategories`
    to activate/deactivate all rules in a category
    (https://github.com/languagetool-org/languagetool/issues/66)
  * Bugfix: for files >= 64,000 bytes, the position information
    (`fromx` and `tox`) could be wrong. Also, rules that work
    across paragraphs like the German word coherency rule wouldn't
    work. Both bugs have been fixed but with the side-effect that
    large files will now be loaded into memory completely. If
    you're using LanguageTool on large files (several MB) you might
    need to split these files now before you check them.
    If you need the old behavior, use the `--line-by-line` switch.
    https://github.com/languagetool-org/languagetool/issues/254
  
#### Wikipedia
  * Indexing: fixed an `IllegalArgumentException` for long sentences
    (https://github.com/languagetool-org/languagetool/issues/364)

#### Core code
  * Fixed a bug while sentence and paragraph end tags were removed during 
    disambiguation.
  * Fixed a bug with a possible `NullPointerException` for tokens containing
    soft hyphens that might be disambiguated.

#### Morfologik binary dictionaries
  * Updated Morfologik library to version 2.1.0. The tools for building 
    dictionaries (languagetool-tools) have been adapted to the new version. 
    The format of the dictionaries has not changed, except for a minor 
    change only in Catalan.



## 3.2 (2015-12-29)

* LanguageTool requires Java 8 now

#### Belarusian
  * new spellchecker dictionary. This dictionary is based on dict-be-official-2008-20140108.oxt
    from http://bnkorpus.info/download.html

#### Catalan
  * fixed false alarms
  * added new rules
  * added words suggested by users

#### Danish
  * updated hunspell dictionary to Version 2.3 (2015-11-15):
    * Corrections made regarding new spelling of 2012
    * General cleanup
    * A lot of  compound flags added
  * fixed bug where Hunspell flags wrongly was in the tagger-dictionary. For example:  
    `vintrenes+F+sub:bes:plu:utr:gen/115,70,85,976,941,947`  
    `vinåndstermometrenes+F+sub:bes:plu:neu:gen/70,118,85,976`
  * added new tags
  * updated and made adjustment for the new things introduced by the new spelling of 2012 and Hunspell-da 2.3
  
#### English
  * added/improved several rules
  * added more than 150 confusion pairs like shall/shell, sheer/shear (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data) 
  * added `en/removed.txt` so incorrect readings of the POS tagger can be avoided without
    rebuilding the binary dictionary (https://github.com/languagetool-org/languagetool/issues/306)

#### Esperanto
  * added/improved several rules

#### French
  * upgraded dictionaries to Dicollecte-5.5
  * added/improved several rules

#### German
  * added/improved a few rules
  * improved agreement rule to detect errors like `Ich gebe dir ein kleine Kaninchen.`
    where the determiner is indefinite but the adjective fits only for a definite determiner
  * added `de/removed.txt` so incorrect readings of the POS tagger can be avoided without
    rebuilding the binary dictionary

#### Italian
  * added an agreement rule

#### Portuguese
  * added/improved several rules
  
#### Russian
  * added/improved several rules
  * added words suggested by users to spellchecker dictionary

#### Ukrainian
  * big dictionary update: more than 10k new words, many fixes (the dictionary source is now
    available at https://github.com/arysin/dict_uk)
  * many new rules
  * improvements for euphony rules
  * improvements in dynamic compound tagger
  * new disambiguation rules

#### Rule Syntax
  * New rule syntax `<regexp>...<regexp>` as a simple alternative
    to `<pattern><token>...</token></pattern>`. Note that this is limited:
    E.g. it's not possible to address POS tags and the `<suggestion>` cannot
    change the case of the match.
    Available attributes: `type` with value `smart` (treats space in the regular
    expression as `\s+` or a non-breaking space) or `exact` (`smart` is the default),
    `mark` to specify which part of the match gets underlined (everything by default,
    use `1` to only underline the first group etc.)
  * Non-breaking spaces (`\u00A0`) are now treated like regular spaces. Before,
    using a non-breaking space could cause a rule not to match.
  * `<filter>` can now also be used in `disambiguation.xml`

#### Embedded HTTPS server
  * Speed up for testing short sentences for de-DE, de-AT, and de-CH

#### Java API
  * `GeneralCatalan` has been removed, use `Catalan` instead
  * `SuggestionExtractorTool` and `SuggestionExtractor` have been removed
  * `ConfusionProbabilityRule` has been moved to package `org.languagetool.rules.ngrams`
  * `ConfusionProbabilityRule.getWordTokenizer()` is now called
    `ConfusionProbabilityRule.getGoogleStyleWordTokenizer()`
  * `RuleAsXmlSerializer` has been renamed to `RuleMatchAsXmlSerializer`
  * some formerly deprecated code has been removed
  * some code has been deprecated
  * `StringTools.isWhitespace()` now returns `true` for a token that is
    a non-breaking space or a narrow non-breaking space
  * `RuleFilter` is not an interface anymore but an abstract class
  * the `LanguageModel` interface has been redesigned, see `BaseLanguageModel`
    for a class similar to the previous implementation
  * Class `BerkeleyLanguageModel` was added to support BerkeleyLM language models.
    See https://github.com/adampauls/berkeleylm for the software and e.g.
    http://tomato.banatao.berkeley.edu:8080/berkeleylm_binaries/ for pre-built models.
    To use the new models your language class needs to overwrite the `getLanguageModel(File)`
    method. For now, we recommend you continue using the Lucene-based models at
    http://languagetool.org/download/ngram-data/.

#### LibreOffice / Apache OpenOffice Integration
  * fix: disabling rules that are disabled by default and had been 
    enabled didn't work

#### Internal
  * updated segment library to 2.0.0 (https://github.com/loomchild/segment)



## 3.1 (2015-09-28)

#### Catalan
  * added new rules
  * fixed false alarms
  * added words suggested by users
 
#### English
  * added and improved a few rules
  * added several pairs of easily confused words - active only with
    ngram data (see http://wiki.languagetool.org/finding-errors-using-n-gram-data)

#### French
  * upgraded Hunspell dictionary to Dicollecte-5.4.1
  * upgraded POS tag and Synthesizer dictionaries to Dicollecte-5.4
  * added/improved several rules
  * new filter to be used for matching the part-of-speech of parts of words, e.g.:
```xml
       <filter class="org.languagetool.rules.fr.FrenchPartialPosTagFilter"
               args="no:1 regexp:(.*)-tu postag_regexp:V.*(ind|con|sub).*2\ss negate_pos:yes"/>
```

#### German
  * added and improved several rules
  * added a rule to detect word confusion by using ngram data, so far it has
    only a few word pairs
    (see http://wiki.languagetool.org/finding-errors-using-n-gram-data),

#### Japanese
  * major rule update with 700+ new rules, thanks to Shugyousha

#### Polish
  * added some compound prepositions to avoid false alarms (thanks to
    Sławek Borewicz)

#### Portuguese
  * added/improved several rules

#### Russian
  * added and improved a few rules
  * added a few false friends rules (Russian/English)

#### Ukrainian
  * significant dictionary update (fixes, lot of new adjectives and last names)

#### LibreOffice / Apache OpenOffice Integration
  * fix: the ngram directory that turns on the confusion rule (see
    http://wiki.languagetool.org/finding-errors-using-big-data) was
    ignored in LibreOffice and OpenOffice

#### ngrams
  * Chinese, French, Italian, Russian, and Spanish have been prepared to
    use ngram data, that means they have a `confusion_sets.txt` file
    where word pairs can be added.
    See http://wiki.languagetool.org/finding-errors-using-n-gram-data
    for information on where to download the ngram data.
  * if a directory with ngram data for the confusion rule is specified,
    this directory is now expected to have at least one sub directory `en`
    or `de` with the `1grams`, `2grams`, and `3grams` directories
    (also see http://wiki.languagetool.org/finding-errors-using-n-gram-data)

#### Embedded server
  * new property file key `rulesFile` to use a `.languagetool.cfg` file
    to configure which options should be enabled/disabled in a server
    (https://github.com/languagetool-org/languagetool/pull/281)

#### API
  * several deprecated methods and classes have been removed
  * Rules can now overwrite `getAntiPatterns()` with patterns to
    be ignored. See the javadoc for details of what needs to
    be considered to make this work. See `org.languagetool.rules.de.CaseRule`
    for an example.

#### Internal
  * updated to Lucene 5.2.1
  * updated to Apache OpenNLP 1.6.0



## 3.0 (2015-06-29)

#### Breton
  * updated FSA spelling dictionary from An Drouizig Breton Spellchecker 0.13
  * updated POS dictionary from Apertium (svn r61079)

#### Catalan
  * added new rules
  * fixed false alarms
  * added words suggested by users

#### English
  * added a few new rules
  * ConfusionProbabilityRule (only enabled with the `--languagemodel` option)
    has been rewritten and `homophones.txt` has been renamed to `confusion_sets.txt`
    and now only has few items enabled by default, the rest is commented out
    to improve quality (less false alarms).
    Also see http://wiki.languagetool.org/finding-errors-using-big-data

#### German
  * fixed some false alarms
  * updated to jwordsplitter 4.1 for better compound splitting
  * the spell checker offers correct suggestions now for
    incorrect past tense forms like "gehte" -> "ging" (useful
    mostly for non-native speakers)
  * added word frequency information to improve spelling suggestions (but this
    won't help for compounds which are not in the dictionary)

#### Polish
  * added new rules
  * fixed dozens of false alarms

#### Portuguese
  * added/improved several rules (started adding morphologic rules)

#### Russian
  * improved rules
  * updated spellchecker

#### Slovak
  * dictionary update and several new rules

#### Ukrainian
  * big dictionary update (thousands of new words, new tagging for pronouns)
  * improved sentence and word tokenization
  * improved tokenization and tagging of lowercase abbreviations
  * new grammar and styling rules
  * new spelling rules, especially for lowercase abbreviations with dots
  * improved compound word tagging
  * improved some rules coverage
  * many new barbarism replacement suggestions

#### Bug Fixes
  * `UppercaseSentenceStartRule` didn't properly reset its state so that
    different errors could be found when e.g. `JLanguageTool.check()` got
    called twice with the same text.
  * `Authenticator.setDefault()` is now only called if it's allowed by
    the Java security manager. In rare cases, this might affect using
    external XML rule files as documented at
    http://wiki.languagetool.org/tips-and-tricks#toc9 (Github issue #255)

#### GUI (stand-alone version)
  * fixed auto-detection of text language, which didn't work after editing text
  * a directory with ngram data for the confusion rule can now be specified
    in the configuration dialog (English only for now), see
    http://wiki.languagetool.org/finding-errors-using-big-data

#### Embedded server
  * performance improvements for checking small texts
    for the use case that creates a new `JLanguageTool` object
    for every check, as done by the embedded server (or multithreaded
    LT users in general)

#### Command-line
  * Fixed an error with the `--api` option that printed invalid XML
    for large documents or when the input was STDIN (Github issue #251)
  * Print some information to STDERR instead of STDOUT so the `--api`
    option makes more sense

#### API
  * added `MultiThreadedJLanguageTool.shutdown()` to clean up the thread pool
  * several deprecated methods and classes have been removed, e.g.
    * `Language.REAL_LANGUAGES` is now `Languages.get()`
    * `Language.LANGUAGES` is now `Languages.getWithDemoLanguage()` - but you will probably
       want to use `Languages.get()`
  * Other static methods from class `Language` have also been moved to `Languages`
  * `Language.addExternalRuleFile()` and `Language.getExternalRuleFiles()`
    have been removed. To add rules, load them with `PatternRuleLoader`
    and call `JLanguageTool.addRule()`.
  * `getAllRules()`, `getAllActiveRules()`, and `getPatternRulesByIdAndSubId()`
    in class `JLanguageTool` used to call `reset()` for all rules. This is
    not the case anymore. `reset()` is now called when one of the `check()`
    methods is called. This shouldn't make a difference for all common use-cases.
  * `Language.setName()` has been removed. If you need to set the name,
    overwrite the `getName()` method instead.
  * `Rule.getCorrectExamples()/getIncorrectExamples()`, `PatternToken.getOrGroup()/getAndGroup()`
    and `RuleMatch.getSuggestedReplacements()` now return an unmodifiable list
  * `AbstractSimpleReplaceRule.getFileName()` and `AbstractWordCoherencyRule.getFileName()`
    have been removed, the sub classes are now themselves responsible for loading their data
  * Sub classes of `AbstractCompoundRule` are now responsible for loading the
    compound data themselves using `CompoundRuleData`
  * `AbstractCompoundRule.setShort(String)` has been removed and added as
    a constructor parameter instead.

#### Internal
  * updated to language-detector 0.5


## 2.9.1 (2015-05-14)

#### LibreOffice / Apache OpenOffice Integration
  * fix `osl::Thread::Create failed` error message, see https://bugs.documentfoundation.org/show_bug.cgi?id=90740

## Older versions

See [CHANGES.txt](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.txt) for changes before 2.9.1.
