# LanguageTool Change Log

## 3.0 (release planned for 2015-06-29)

#### English
  * added a few new rules

#### German
  * fixed some false alarms

#### Slovak
  * dictionary update and several new rules

#### GUI (stand-alone version)
  * fixed auto-detection of text language, which didn't work after editing text

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
  * `Language.setName()` has been removed. If you need to set the name,
    overwrite the `getName()` method instead.
  * several deprecated methods and classes have been removed, e.g.
    * `Language.REAL_LANGUAGES` is now `Languages.get()`
    * `Language.LANGUAGES` is now `Languages.getWithDemoLanguage()` - but you will probably
       want to use `Languages.get()`
  * Other static methods from class Language have also been moved to Languages
  * `Rule.getCorrectExamples()/getIncorrectExamples()` and
    `PatternToken.getOrGroup()/getAndGroup()` now return an unmodifiable list
  * `AbstractSimpleReplaceRule.getFileName()` and `AbstractWordCoherencyRule.getFileName()`
    have been removed, the sub classes are now themselves responsible for loading their data
  * Sub classes of `AbstractCompoundRule` are now responsible for loading the
    compound data themselves using `CompoundRuleData`
  * `AbstractCompoundRule.setShort(String)` has been removed and added as
    a constructor parameter instead.

#### Internal
  * updated to language-detector 0.5


## Older versions

See [CHANGES.txt](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.txt) for changes before 3.0.
