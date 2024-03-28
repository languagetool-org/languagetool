# LanguageTool Change Log

## 6.4 (2024-03-28)

#### Asturian
  * tagger and spelling dictionaries have been moved to an external dependency (asturian-pos-dict v 0.1)

#### Catalan
  * added and improved rules
  * updated dictionary (spanish-pos-dict-2.25)

#### Dutch
  * added and improved rules

#### English
  * tagger and spelling dictionaries have been moved to an external dependency (english-pos-dict v 0.3)

#### French
  * added and improved rules

#### German
  * added and improved rules
  * extended dictionary

#### Polish
  * small rule improvements

#### Portuguese
  * added and improved rules

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-2.2)

#### Ukrainian
  * new words in the POS dictionary
  * new rules
  * tagging and disambiguation improvements


## 6.3 (2023-10-06)

#### Catalan
  * added and improved rules

#### Dutch
  * added and improved rules
  * tagger and spelling dictionaries have been moved to an external dependency (dutch-pos-dict v 0.1)

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2023.06.01, v.3.2.1)

#### French
  * added and improved rules

#### German
  * added and improved rules
  * extended dictionary

#### Portuguese
  * added and improved rules

#### Spanish
  * added and improved rules

#### Ukrainian
  * new words in the POS dictionary
  * new rules
  * tagging and disambiguation improvements



## 6.2 (2023-07-02)

#### Catalan
  * added and improved rules

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2023.06.01, v.3.2.1)

#### French
  * added and improved rules

#### German
  * added and improved rules
  * extended dictionary

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Spanish
  * added and improved rules

#### Ukrainian
  * new words in the POS dictionary
  * new rules

There were also minor rule improvements for Galician, Belarusian, Esperanto, Arabic,
and Russian.



## 6.1 (2023-03-28)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.22)

#### Dutch
  * improved and cleaned up rules

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2023.03.01, v.3.1.8)

#### French
  * added and improved rules

#### German
  * added and improved rules
  * extended dictionary

#### Polish
  * small rule updates

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved xml and java rules 
  * improved disambiguation
  * fix a lot of false positives
  * added words and POS data
  * added chunker rules
  * some xml grammar rules uses a chunker now

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-2.0)

#### Ukrainian
  * new words in the POS dictionary
  * improved tokenization, tagging, and disambiguation
  * new rules



## 6.0 (released 2022-12-29)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.21)

#### Dutch
  * improved rules

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2022.12.01, v.3.1.5)

#### French
  * added and improved rules

#### German
  * added and improved rules
  * extended dictionary

#### Polish
  * improved rules

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-1.9)

#### Ukrainian
  * new words in the POS dictionary
  * improved tokenization, tagging, and disambiguation
  * new rules

#### HTTP API / LT server
  * The `/languages` endpoint now lists language codes like `fr-FR` and `es-ES` for languages
    that actually don't have a variant (e.g. there is no `fr-CA`). These codes can also be used
    for the `language` parameter when sending a request. `fr-FR` will internally be mapped
    to `fr` etc. (https://github.com/languagetool-org/languagetool/issues/7421)

### General
  * The `--api` parameter for the command-line version has been removed. It had
    long been deprecated and replaced by `--json`.
  * The `warmup` setting for the config file, which had no effect anymore, has been removed.
  * The deprecated `--word2vecmodel` and `--neuralnetworkmodel` options have been removed,
    as these features were not maintained and had never been used on languagetool.org.
  * You can put a file `grammar_custom.xml` into the same directory that contains the
    `grammar.xml` file for your language. This file will be loaded in addition to
    `grammar.xml`. It can contain custom rules that you want to use now and with future
    versions of LanguageTool, without modifying existing files. The `grammar_custom.xml`
    needs to use the same XML syntax as `grammar.xml` and it must not introduce rule IDs
    that are in use by other rules in other files already.


## 5.9 (2022-09-28)

#### Catalan
  * added and improved rules

#### Dutch
  * improved rules, removed many false alarms

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2022.09.01, v.3.1.2)

#### German
  * added and improved rules
  * extended dictionary

#### French
  * added and improved rules

#### Polish
  * added and improved rules

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * updated dictionary
  * some rules improvements

#### Spanish
  * added and improved rules

#### Ukrainian
  * new words in the POS dictionary
  * added verb and adj/noun agreement rule
  * added and improved several rules
  * improved tagging and disambiguation

### General
  * The `--word2vecModel` option has been deprecated


## 5.8 (2022-07-01)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.19)

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2022.06.01, v.3.0.9)

#### German
  * added and improved rules
  * extended dictionary

#### French
  * added and improved rules

#### Polish
  * updated the spelling dictionary to match changes in language (the current version is in sync with sjp.pl as of April 1, 2022)
  * added and improved rules

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives
  * the tagger dictionary has been moved to an external dependency (portuguese-pos-dict)

#### Russian
  * improved some rules

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-1.7)

#### Ukrainian
  * new words in the POS dictionary
  * added and improved several rules
  * added numeric and adj/noun agreement
  * improved tagging and disambiguation



## 5.7 (2022-03-28)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.18)

#### French
  * added and improved rules
  * updated dictionary (french-pos-dict-0.5)

#### German
  * added and improved rules
  * extended dictionary

#### Polish
  * added and improved rules, especially for frequent mistakes
  * added a rule for spelling coherency (*menedżer* or *menadżer* but not both in the same document)
  * updated the user interface translation

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved rules
  * improved disambiguation

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-1.6)

#### Ukrainian
  * new words in the POS dictionary
  * added and improved several rules
  * added pronoun checking for adj/noun agreement
  * improved tagging and disambiguation


## 5.6 (2021-12-29)

#### Catalan
  * added and improved rules

#### Dutch
  * more words in dictionary
  * added many special word groups
  * added rules for accidentally split words

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2021.12.01, v. 3.0.3)

#### French
  * added and improved rules

#### German
  * added and improved rules

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved rules
  * improved disambiguation
  * added words and POS data
  * fix POS data
  * rebuilt POS dictionary
  * improved spell checking

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-1.5)

#### Ukrainian
  * many new words in the POS dictionary
  * added and improved rules
  * improved tagging and disambiguation

#### General
  * The `--allow-origin` option doesn't require a parameter anymore
    in order to avoid confusion about whether `*` needs to be quoted
    on Windows. Using `--allow-origin` without a parameter now implies `*`.
  * Added new value `firstupper` for `case_conversion` attribute in `grammar.xml` (see issue #3241).


## 5.5 (2021-10-02)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.15)

#### Dutch
  * added and improved rules
  * extended spelling dictionary

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2021.09.01)

#### French
  * added and improved rules
  * updated dictionary (french-pos-dict-0.4) with words from added.txt and removed.txt, 
    and fixed lemmas of many adjectives (infinitive->masc. sing.)

#### German
  * added and improved rules

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved rules
  * updated POS and spellchecker dictionary
  * activate some picky rules in picky mode

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-1.3)

#### Ukrainian
  * over 5000 new words in the POS dictionary
  * added and improved rules
  * improved tagging and disambiguation


## 5.4 (2021-06-25)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.14)

#### Dutch
  * added and improved rules
  * extended spelling dictionary

#### English
  * additional tags for personal pronouns, e.g. `us[we/PRP,we/PRP_O1P]`; `mine[mine/PRP$,I/PRP$_P1S]`

#### French
  * added and improved rules

#### Galician
  * small rule improvements

#### German
  * added and improved rules

#### Portuguese
  * the sentence length rule is now active in 'picky' mode
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * small rule improvements

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-1.2)

#### Ukrainian
  * new words in the POS dictionary
  * added and improved rules
  * improved tagging and disambiguation

#### General
  * The sentence length rule is now a text-level rule
    and it underlines the whole sentence, not just the position where the threshold
    is reached.



## 5.3 (2021-03-29)

#### Arabic
  * added and improved rules
  * improve tagger and synthesizer to better tag pronouns
  * add ArabicTransVerbRule and Arabic Punctuations Whitespace Rules

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.14)

#### Dutch
  * added and improved rules

#### English
  * updated en_US spellchecker dictionary from http://wordlist.aspell.net (Version 2020.12.07)
  * updated en_CA spellchecker dictionary from http://wordlist.aspell.net (Version 2020.12.07)
  * updated en_AU spellchecker dictionary from http://wordlist.aspell.net (Version 2020.12.07)
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2021.03.01)
  * updated en_ZA spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2021.02.15)
  * changes in the word tokenizer for contractions and possessives, e.g. `does[do/VBZ]n't[not/RB]`; `Harper[Harper/NNP,harper/NN]'s['s/POS]`

#### French
  * added and improved rules

#### German
  * added and improved rules

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved rules

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-1.1)

#### Ukrainian
  * over 6000 new words in the POS dictionary
  * added and improved rules
  * improved tagging and disambiguation



## 5.2 (released 2020-12-29)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.13)

#### Dutch
  * added and improved rules
  * There's now support for Belgian Dutch (`nl-BE`). "Dutch" (`nl`) is
    still the default. nl-BE-specific rules can be added to `nl-BE/grammar.xml`

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.91 - 2020-12-01)

#### German
  * added and improved rules

#### French
  * added and improved rules
  * updated spell checker and POS dictionary (unified in one dictionary) to lexique-grammalecte 7-0
    (source: https://grammalecte.net/download.php?prj=fr),
    as an external dependency (source: https://github.com/languagetool-org/french-pos-dict)

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved rules
  * added new words and POS data for it
  * improved suggestions algorithms for spellchecking

#### Spanish
  * added and improved rules
  * updated dictionary (spanish-pos-dict-0.9)

#### Ukrainian
  * over 7000 new words in the POS dictionary
  * added and improved rules
  * improved tagging and disambiguation

#### General
  * There's now `RegexAntiPatternFilter` which can be used to have antipatterns
    for `<regexp>` rules. Use like this:
    ```
    <regexp>my regex</regexp>
    <filter class="org.languagetool.rules.patterns.RegexAntiPatternFilter" args="antipatterns:regex1|regex2"/>
    ```
    Note that the regex after `antipatterns:` cannot contain spaces.
  * German, French, Dutch, and Spanish have ngram-based false friends for
    some time already, meaning that a German/Dutch/... native speaker will
    get an error if (probably) using and English word incorrectly in an English
    text. The change in this version is that for all other language pairs that
    also have false friends, these rules are now active only in picky mode
    (`--level PICKY` on the command line, `level=picky` with the HTTP API.)


## 5.1.3 (released 2020-10-15)

#### LibreOffice / Apache OpenOffice Integration
  * fixed https://github.com/languagetool-org/languagetool/issues/3666
    ("... not a language code known to LanguageTool")



## 5.1.2 (released 2020-10-05)

#### LibreOffice / Apache OpenOffice Integration
  * fixed https://github.com/languagetool-org/languagetool/issues/3638,
    https://github.com/languagetool-org/languagetool/issues/3652, and
    https://github.com/languagetool-org/languagetool/issues/3575



## 5.1.1 (released 2020-09-29)

#### LibreOffice / Apache OpenOffice Integration
  * fixed a NullPointerException crash in the LibreOffice/OpenOffice add-on



## 5.1 (released 2020-09-25)

#### Catalan
  * added and improved rules

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.88 - 2020-09-01)

#### French
  * added and improved rules

#### German
  * Updated the German part-of-speech dictionary (https://github.com/languagetool-org/german-pos-dict)
    to version 1.2.2.
  * each pair of `ProhibitedCompoundRule` has its own ID now, so it can be separately turned on/off
  * added and improved rules

#### Italian
  * small rule improvements

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved rules

#### Spanish
  * added and improved rules

#### Ukrainian
  * dictionary update
  * many new punctuation rules
  * many new styling rules
  * tokenization and tagging improvements
  * disambiguation improvements

#### General
  * each pair of `ConfusionProbabilityRule` has its own ID now, so it can be separately turned on/off
  * new XML attribute `chunk_re` for `<token>`, which specifies a chunk as a regular expression



## 5.0.2 (2020-08-28)

  * (languagetool-core only) merged https://github.com/languagetool-org/languagetool/pull/3491 and
    https://github.com/languagetool-org/languagetool/pull/3487



## 5.0 (2020-06-27)

#### Arabic
  * added and improved rules
  * updated POS dictionary (Arramooz [#e33794e](https://github.com/linuxscout/arramooz/commit/e33794e787d56e7c185c0e281fd8e6d6274f3fdc))
  * remove the Algerian variant (ar-DZ)
  * add support of ngram data ([languagetool-tools-ar](https://github.com/sohaibafifi/languagetool-tools-ar))
  * add Darja, Diacritics, Redundancy, WrongWordInContext, Wordiness, Homophones and WordCoherency rules.

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.10)

#### Dutch
  * added and improved rules

#### English
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.85 - 2020-06-01)

#### Esperanto
  * added and improved rules

#### French
  * added and improved rules

#### German
  * added and improved rules
  * rules that apply to de-DE and de-AT (but not de-CH) can now be placed in `de/de-DE-AT/grammar.xml`
  * Updated the German part-of-speech dictionary (https://github.com/languagetool-org/german-pos-dict)
    to version 1.2.1.
  * Special chars `_` and `/` can now be escaped in `spelling.txt` and `spelling_custom.txt` using
    the backslash. For example, `foo\/s` will add `foo/s` to the dictionary.

#### Persian
  * commented out rules that caused many false alarms

#### Portuguese
  * added and improved rules
  * added words and POS data
  * fixed tons of false positives

#### Russian
  * added and improved rules
  * added new Java rules
  * rebuilt and improved main spellchecker dictionary, added many new words
  * new variant (only yo "ё") spellchecker dictionary and new java rule for it (set off by default)
  * new `filter` arguments: `prefix` and `suffix` to be used for matching the part-of-speech of parts of words
    with prefix and suffix added to original token, e.g.:
```xml
       <filter class="org.languagetool.rules.ru.RussianPartialPosTagFilter"
                args="no:2 regexp:(.*) postag_regexp:(ADV) prefix:не suffix:  "/>
```

#### Slovak
  * commented out rules that caused many false alarms

#### Spanish
  * added and improved rules
  * new tagger dictionary by Jaume Ortolà, LGPL, source: https://github.com/jaumeortola/spanish-dict-tools
  * the spelling rule is enabled in LibreOffice using the tagger dictionary (no other spelling dictionary is needed)

#### Ukrainian
  * dictionary update, including many rare and slang words
  * new rules
  * tokenization and tagging improvements
  * disambiguation improvements

#### General
  * added `replace_custom.txt` for several languages so users can have their own very simple replace
    rules without worrying about updates (they still need to copy the file to the new LT version, though).
  * Updated dependency `com.gitlab.dumonts:hunspell` to 1.1.1 to make spell checking work on older Linux
    distributions like RHEL 7.



## 4.9 (2020-03-24)

#### Arabic
  * Added initial support for Arabic, contributed by Sohaib Afifi
    (https://github.com/languagetool-org/languagetool/pull/2219)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.7)

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * added new part-of-speech tag `ORD` for ordinal numbers (e.g., first, second, twenty-third etc.)
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.82 - 2020-03-01)

#### French
  * improved rules

#### German
  * added and improved rules
  * `compounds.txt` now automatically expands `ß` to `ss` when using German (Switzerland)
  * German `spelling.txt` now supports `prefix_verb` syntax like `vorüber_eilen` so
    the speller will accept all forms of "eilen" prefixed by "vorüber"

#### Irish
  * Added initial support for Irish, contributed by Jim Regan
    (https://github.com/languagetool-org/languagetool/pull/2260)

#### Portuguese
  * added and improved rules
  * added words and POS data

#### Russian
  * small improvements

#### Ukrainian
  * dictionary update
  * new rules
  * tokenization and tagging improvements


## 4.8 (released 2019-12-27)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.6)

#### Chinese
  * Now using https://github.com/hankcs/HanLP for tokenization (PR 1981)

#### Danish
  * corrections are now offered for spell check errors
  * updated spell checker to version 2.4 (2018-04-15)
    (source: https://extensions.libreoffice.org/extensions/stavekontrolden-danish-dictionary)

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.79 - 2019-12-01)
  * updated en_US spellchecker dictionary from http://wordlist.aspell.net (Version 2019.10.06)
  * updated en_CA spellchecker dictionary from http://wordlist.aspell.net (Version 2019.10.06)
  * updated en_AU spellchecker dictionary from http://wordlist.aspell.net (Version 2019.10.06)

#### Esperanto
  * corrections are now offered for spell check errors

#### French
  * improved rules
  * updated spell checker (Grammalecte·dic/Dicollecte) to version 6.4.1 (2019-04-05)
    (source: https://grammalecte.net/download.php?prj=fr)
  * updated part-of-speech dictionaries to dicollecte-6.4.1
    (https://github.com/languagetool-org/languagetool/pull/1963)

#### German
  * added and improved rules

#### Greek
  * updated spelling dictionary to el_GR 0.9 (14/03/2019), by George Zougianos

#### Khmer
  * updated spell checker to version 1.82 (2015-10-23)
    (source: https://extensions.libreoffice.org/extensions/khmer-spelling-checker-sbbic-version)

#### Portuguese
  * added and improved rules
  * added words and POS data

#### Russian
  * added new words
  * improve java rule

#### Swedish
  * updated spelling dictionary to version 2.42 (Released Feb 03, 2019)
    (source: https://extensions.libreoffice.org/extensions/swedish-spelling-dictionary-den-stora-svenska-ordlistan)

#### Ukrainian
  * dictionary update
  * new rules
  * tokenization improvements

#### General
  * The unmaintained code from package `org.languagetool.dev.wikipedia.atom`
    has been removed. It hadn't been maintained for years and didn't work properly
    anymore.
  * `spelling_global.txt` has been added. Words or phrases added here will
    be accepted for all languages.
  * `prohibit_custom.txt` and `spelling_custom.txt` can be used to make your
    own additions to `spelling.txt` and `prohibit.txt` without having to edit those
    files after a LanguageTool update (you will still need to manually copy those
    files).
    Paths to these files (`xx` = language code):
    `./org/languagetool/resource/xx/hunspell/prohibit_custom.txt`
    `./org/languagetool/resource/xx/hunspell/spelling_custom.txt`
    Note that you can simply create these files if they don't exist for your language yet.

#### HTTP API / LT server
  * The dynamic languages feature (`lang-xx=...` and `lang-xx-dictPath=...`) now
    also supports hunspell dictionaries. Just let `lang-xx-dictPath` point to the
    absolute path of the `.dic` file. Note that hunspell is quite slow when it
    comes to offering suggestions for misspelled words.

#### Java API
  * `AbstractSimpleReplaceRule2` has been fixed so that it's now case-insensitive.
    If you implement a sub class of it and you want the old behavior, please implement
    `isCaseSensitive()` and have it return `true`. (Issue #2051)

#### Internal
  * The internal hunspell has been updated from 1.3 to 1.7, now using
    https://gitlab.com/dumonts/hunspell-java as the project providing the bindings.
    For Portuguese, this speeds up generating suggestions for misspellings by
    a factor of about 3 (but it's still slow compared to Morfologik).
    32-bit systems are not supported anymore (only affects languages like German
    and French).
  * Experimental: the new `default="temp_off"` attribute in `grammar.xml` files will
    turn off a rule/rulegroup, but keep it activated for our nightly regression tests.
  * Many external dependencies have been updated to new versions.



## 4.7 (2019-09-28)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.5)

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.76 - 2019-09-01)

#### French
  * improved rules

#### Galician
  * improved rules

#### German
  * added and improved rules

#### Italian
  * small rule improvements

#### Portuguese
  * added rules and significantly improved accuracy
  * disambiguation improvements
  * POS and spelling improvements

#### Russian
  * improved rules
  * added new words to spellchecker dictionary

#### Spanish
  * added and improved rules

#### Ukrainian
  * 2k of new words in the dictionary
  * improved tokenization
  * improved dynamic tagging
  * added and improved rules

#### General
  * Spell suggestion improvements: for many cases of a misplaced space,
    the suggestions are now better. For example, "thef eedback" can now
    be corrected to "the feedback" in one step. (#1729)
  * The synthesizer now considers entries in `added.txt` and `removed.txt`
    (except for Catalan and Polish; for German removing compounds
    in `removed.txt` might not work) (#884)



## 4.6 (2019-06-26)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.4) with more health terminology

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * introduced new part-of-speech tag `PCT` for punctuation marks (`.,;:…!?`)
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.73 - 2019-06-01)

#### Esperanto
  * added and improved rules

#### French
  * added and improved rules
  * Rule `FRENCH_WHITESPACE` has been split into `FRENCH_WHITESPACE` (on
    by default) and `FRENCH_WHITESPACE_STRICT` (off by default).
    `FRENCH_WHITESPACE` only complains if there's no space at all before
    `?`, `!`, `;`, `:`, or `»`. `FRENCH_WHITESPACE_STRICT` complains
    if there's no space or a common space instead of a non-breaking space
    before these characters.
  * added some popular names to dictionary

#### Galician
  * added verbal agreement rules

#### German
  * added and improved rules
  * The false friend rule has been modified to use ngrams: Now false friends
    cause error messages if they are used in a wrong context, according to ngram statistics.
    Note that some pairs from `false-friends.xml` are not supported anymore because
    their precision isn't good enough. See `confusion_sets_l2_de.txt` for active DE/EN pairs.
    Use `My handy is broken.` to test the rule. As before, this will only create
    an error if `motherTongue` is set to a German language code.
  * `prohibit.txt`: lines starting with `.*` will prohibit all words ending with
    the subsequent string (e.g., `.*artigel` will prohibit `Versandartigel`)

#### Greek
  * added rules

#### Italian
  * added popular names to dictionary

#### Portuguese
  * POS and spelling improvements

#### Russian
  * added and improved rules
  * added new words to spell dictionary

#### Spanish
  * updated spell dictionary from 2.1 to 2.4

#### Ukrainian
  * support for new spelling rules from 2019
  * thousands of new words in the dictionary
  * many rule improvements
  * tokenization and tagging improvements

#### HTTP API / LT server
  * `altLanguages` will only be considered for words with >= 3 characters
  * Cleaned up error handling: invalid parameters will now return an HTTP error 400
    instead of 500.


## 4.5.1 (2019-03-28)

#### LibreOffice / Apache OpenOffice Integration

 * Fixed a bug that caused the rules in the options dialog to not appear in the text language



## 4.5 (2019-03-26)

#### Catalan
  * added and improved rules
  * updated dictionary (catalan-pos-dict-2.3) with health terminology

#### English
  * `resource/en/en-US-GB.txt` contains a mapping from US to British
    English and vice versa. It's not used to detect correct or incorrect spellings,
    but only to improve error messages so that they explicitly explain that
    the incorrect word is actually a different variant (like 'colour' in an en-US
    text).
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.70 - 2019-03-01)
  * spell check ignores single characters (e.g., 'α')

#### Galician
  * added and improved rules
  * disambiguation improvements
  * foreign names recognition

#### German
  * added and improved rules
  * Simple German: added and improved rules
  * improved suggestions for typos that end with a dot (typically at the end of
    the sentence) - the dot is not included anymore
  * spell check ignores single characters (e.g., 'α') and hyphenated compounds (e.g., 'α-Strahler')

#### Portuguese
  * added and significantly improved rules accuracy
  * disambiguation improvements
     - Chinese common names are now detected
  * POS and spelling improvements
  * updated Hunspell dictionaries to:
    - [pt-PT pos-AO] Dicionários Portugueses Complementares 3.1

#### Russian
  * added and improved rules
  * disambiguation improvements
  * added many words without "yo" letter to POS dictionary
  * added new words to spell dictionary

#### Ukrainian
  * dictionary update
  * added and improved rules
  * improvements to tokenization, tagging, and disambiguation

#### General
  * URLs written like `mydomain.org/` are now detected as domains and not
    considered spelling errors anymore. Note that the slash is still needed
    to avoid missing real errors.
  * JSON output: The `replacements` list now has an optional new item `shortDescription`
    for each `value`. It can contain a short definition/hint about the word. Currently,
    the only words that have a short description are ones that have a description
    in `confusion_sets.txt` (i.e. a text after the `|` symbol).

#### General
  * bug fix: don't make `interpretAs` part of getTextWithMarkup() (#1393)
  * Experimental new attribute `raw_pos` for the `<pattern>` element in `grammar.xml`.
    If set to `yes`,  the `postag` will refer to the part-of-speech tags *before*
    disambiguation.
  * Experimental support for `<antipattern>` in `disambiguation.xml`

#### HTTP API / LT server
  * Experimental new parameter `preferredLanguages`: up to a certain limit (currently
    50 characters), only these languages will be considered for language detection.
    This has to be a comma-delimited list of language codes without variants (e.g.
    use 'en', not 'en-US').
    This only works with fasttext configured as the language detector.
  * Spellcheck-only languages can now be added dynamically from the configuration
    using `lang-xx=languagename` and `lang-xx-dictPath=/path/to/morfologik.dict`.
    `xx` needs to be the language code. The JSON result will contain `spellCheckOnly: true`
    for these languages.



## 4.4.1 (2019-01-14)

  * Fixed a bug that prevented opening the Options dialog in LibreOffice/OpenOffice



## 4.4 (2018-12-27)

#### Catalan
  * added and improved rules
  * updated dictionary

#### Dutch
  * added and improved rules, including more confusion rules for dyslectic people
  * added large amount of family names to reduce false alarms in spelling

#### English
  * added and improved rules
  * segmentation improvements
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict (Version 2.67 - 2018-12-01)
  * added rules for 'Oxford spelling' (applicable to British English only)

##### French
  * small rule improvements

#### German
  * added and improved rules
  * Swiss German: improved POS tagging of words that contain 'ß' in de-DE German (e.g.,
    'gross' is tagged as 'gross[groß/ADJ:PRD:GRU]'); (#1147)
  * Simple German: added and improved rules; restructured grammar.xml

#### Portuguese
  * added and improved rules
  * disambiguation improvements
  * POS and spelling improvements

#### Russian
  * added and improved rules
  * disambiguation improvements
  * POS and spelling dictionary improvements

#### Serbian
  * Serbian never moved beyond its "initial support" state with a tiny number of rules,
    and it has no active maintainer, so we have deactivated it for now. If you'd like to
    maintain support for Serbian, let us know in the forum (https://forum.languagetool.org).
    Once it's clear that a new active long-term maintainer has been found, we'll activate
    support for Serbian again.

#### Ukrainian
  * dictionary update (about 7k of new words)
  * added and improved rules
  * improvements to tokenization, tagging, and disambiguation

#### HTTP API / LT server
  * Experimental support for `altLanguages` parameter: takes a list of language
    codes. Unknown words of the main languages (as specified by the `language` parameter)
    will cause errors of type "Hint" if accepted by one of these languages.
    We expect clients to interpret this like style issues, e.g. these words should
    be underlined with a light blue instead of red.
    Support for this is experimental, i.e. it might be removed again or implemented
    in a different way.
  * Experimental support for `noopLanguages` parameter: takes a list of language
    codes of languages that are not supported by LT but that will be detected and
    mapped to a no-op language without rules. Useful for clients that rely on
    language auto-detection and whose users might use languages not supported by LT.
    NOTE 1: only works with fastText configured
    NOTE 2: setting languages here will worsen language detection quality on average
  * Change to language detection behavior: Removed fallback to English when confidence of
    detection algorithm is low, instead now always returning highest scoring detected language.
    Added a field `confidence` to `detectedLanguage` object in the JSON response that contains
    the probability score for the detected language as computed by the detection algorithm.



## 4.3 (2018-09-26)

#### Catalan
  * added and improved rules

#### Dutch
  * added and improved rules

#### English
  * added and improved rules

##### Esperanto
  * added and improved rules

##### French
  * small rule improvements

#### Galician
  * added and improved rules

#### German
  * added and improved rules

#### German (simple)
  * added and improved rules

#### Portuguese
  * added and improved rules
  * improvements to disambiguation, and segmentation
  * updated Hunspell dictionaries to:
    - [pt-PT pos-AO] Dicionários Portugueses Complementares 3.0

#### Russian
  * added and improved rules

#### Ukrainian
  * added and improved rules

#### General
  * Prepared support for AIX. See https://github.com/MartinKallinger/hunspell-aix
    for the required libraries
  * Email signatures are now ignored for language detection as long as they are
    separated from the main text with `\n-- \n`

#### HTTP API / LT server
  * The server can now accept JSON as the `data` parameter that describes
    markup. For example:
    ```
    {"annotation":[
      {"text": "A "},
      {"markup": "<b>"},
      {"text": "test"},
      {"markup": "</b>"}
    ]}
    ```
    With this input, LT will ignore the `markup` parts and run the check only
    on the `text` parts. The error offset positions will still refer to the
    original input including the markup, so that suggestions can easily be applied.
    You can optionally use `interpretAs` to have markup interpreted as whitespace, like this:
    ```
    {"markup": "<p>", "interpretAs": "\n\n"}
    ```
    Note that HTML entities (including `&nbsp;`) still need to be converted to Unicode characters
    before feeding them into LT.
    (Issue: https://github.com/languagetool-org/languagetool/issues/757)
  * The `blockedReferrers` setting now also considers the `Origin` header
  * A `blockedReferrers` setting of `foobar.org` will now automatically match `http://foobar.org`,
   `http://www.foobar.org`, `https://foobar.org`, and `https://www.foobar.org`
  * New setting `fasttextModel` (see https://fasttext.cc/docs/en/language-identification.html)
    and `fasttextBinary` (see https://fasttext.cc/docs/en/support.html). With these
    options set, the automatic language detection is much better than the built-in one.
  * Experimental new `mode` parameter with `all`, `textLevelOnly`, or `allButTextLevelOnly` as value:
    Will check only text-level rules or all other rules. As there are fewer text-level rules,
    this is usually much faster and the access limit for characters per minute that can be
    checked is more generous for this mode.
  * Improved spellchecker suggestions (not yet enabled by default).
    See https://forum.languagetool.org/t/gsoc-reports-spellchecker-server-side-framework-and-build-tool-tasks/2926/43
  * Experimental new `type` in JSON. This is supposed to help clients choose the color
    with which they underline/mark errors. Please do not rely on this yet, it might change
    or even be removed.


## 4.2 (2018-06-26)

#### Breton
  * made many messages shorter
  * updated FSA spelling dictionary from An Drouizig Breton Spellchecker 0.15

#### Catalan
  * added and improved rules
  * rules and updated dictionary for new diacritics rules (IEC 2017)

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict  (Version 2018-06-01)
  * updated en_US spellchecker dictionary from http://wordlist.aspell.net (Version 2018.04.16)
  * updated en_CA spellchecker dictionary from http://wordlist.aspell.net (Version 2018.04.16)

#### Esperanto
  * added and improved rules

#### German
  * added and improved rules
  * updated jwordsplitter to 4.4 to prevent excessively long processing times for
    artificially long compounds
  * `prohibit.txt`: lines ending with ".*" will prohibit all words starting with
    the previous string

#### German (simple)
  * added and improved rules

#### Greek
  * added rules

#### Portuguese
  * added and improved rules

#### Russian
  * added and improved grammar and punctuation rules
  * upgraded the tagging and synthesizer dictionaries from AOT.ru rev.269 (extend tags, add missing tags)
  * spelling dictionary update

#### Spanish
  * added and improved a few rules

#### Ukrainian
  * dictionary update (more than 15k of new words)
  * added and improved rules
  * some improvements to tokenization, tagging and disambiguation

#### HTTP API / LT server
  * The JSON contains a new section `detectedLanguage` (under `language`) that
    contains information about the automatically detected language. This way
    clients can suggest switching to that language, e.g. in cases where the
    user had selected the wrong language.
  * New optional configuration setting `blockedReferrers`: a comma-separated list
    of HTTP referrers that are blocked and will not be served
  * BETA: New optional configuration settings `dbDriver`, `dbUrl`, `dbUsername`,
    `dbPassword` to allow user-specific dictionaries

#### Java API
  * The parameters of the `*SpellerRule` classes (e.g. `MorfologikRussianSpellerRule`)
    have changed
  * `LanguageIdentifier` will now only consider the first 1000 characters when
    identifying the language of a text. This improves performance for long texts.



## 4.1 (2018-03-27)

#### Catalan
  * added and improved rules

#### Chinese
  * added some rules

#### Dutch
  * added and improved rules
  * added new Java rule `NL_PREFERRED_WORD_RULE` that suggests preferred words (e.g., 'fiets' for 'rijwiel')

#### English
  * all-uppercase words are now also spellchecked
  * added and improved rules
  * added remaining collocation rules (~130) contributed by Nicholas Walker (Bokomaru)

#### Esperanto
  * words written with x-sistemo now get proper POS tag so grammar mistakes can now
    be found in: ambaux virino (->ambaux virinoj), mi farigxis maljunan (-> mi
    farigxis maljuna), etc.
  * added and improved rules
  * added many `<url>` to rules

#### French
  * improved suggestion for spelling mistakes (#912)

#### Galician
  * added a couple of rules

#### German
  * added and improved rules
  * New rule that checks coherent use of Du/du, Dich/dich etc. Assumes that the first
    use has 'correct' capitalization and suggests the same capitalization for subsequent uses.
  * New line extension `-*` for `ignore.txt`: entries ending with `-*` are ignored only if
    they are part of a hyphenated compound (e.g, `Fair-Trade-*` allows `Fair-Trade-Kakao`)
  * Added a new rule that tries to find compounds that are probably not correct, like
    `Lehrzeile` instead of `Leerzeile`, requires ngram data (rule id `DE_PROHIBITED_COMPOUNDS`)

#### German (simple)
  * added and improved rules

#### Portuguese
  * added and improved rules

#### Russian
  * sentence segmentation improvements
  * added and improved rules
  * upgraded the tagging and synthesizer dictionaries with extended POS tags from AOT.ru rev.269

#### Spanish
  * update to the part-of-speech dictionary

#### Ukrainian
  * dictionary update (~5K new lemmas)
  * compound word tagging improvements
  * many new disambiguation rules
  * several new barbarism and grammar rules

#### HTTP API / LT server
  * The server now returns HTTP error code 500 in case of a timeout (it used to return 503)

#### Java API
  * Constructors that take a `ResultCache` have been removed from `MultiThreadedJLanguageTool`
    as using them caused incorrect results. (https://github.com/languagetool-org/languagetool/issues/897)



## 4.0 (2017-12-29)

#### Catalan
  * added and improved rules
  * updated and renamed dictionary: ca-ES.dict (external dependency: catalan-pos-dict 1.6)
  * added new dictionary for Valencian including most words from Diccionari Normatiu Valencià (AVL):
    ca-ES-valencia.dict (external dependency: catalan-pos-dict 1.6)

#### Dutch
  * added and improved rules

#### English
  * added and improved rules
  * removed the category `MISC` and moved the rules to more specific categories
  * added WordCoherencyRule, to detect cases where two different variants of a word
    are used in the same text (e.g. archaeology and archeology)
  * added approximately 70 collocation rules contributed by Nicholas Walker (Bokomaru)
  * added support for locale-specific spelling suggestions (locale-specific spelling_en-XY.txt files)
  * updated en_GB spellchecker dictionary from https://github.com/marcoagpinto/aoo-mozilla-en-dict
  * updated en_US spellchecker dictionary from http://wordlist.aspell.net (Version 2017.08.24)
  * updated en_CA spellchecker dictionary from http://wordlist.aspell.net (Version 2017.08.24)

#### French
  * LT now offers suggestions for spelling errors

#### Galician
  * added and improved rules, including:
    - grammar: agreement rules added (only number and gender agreement)
    - common normative errors: includes Castilianisms, Lusitanianisms, Hipergalicisms, archaisms and
      Anglicisms correction
    - style: barbarism, redundant expressions, and wordy expressions detection added
    - typography: spacing and number formatting improvements; chemical formulas; degree
      signs; dashes; punctuation; international system standards; and mathematical symbol formatting
  * development, punctuation and repetition rules categories added
  * multiword disambiguation added
  * disambiguation improvements
  * new word tokenizer
  * significant POS tagging and synthesizing improvements
  * spellchecking exceptions for:
    - abbreviations;
    - variables in formulas, units, and related statistical vocabulary;
    - common Latin, English and French expressions;
    - species scientific names;
    - famous personalities
  * updated Hunspell dictionaries to:
    - [gl-ES] Version 12.10 "Xoán Manuel Pintos"

#### German
  * added and improved rules
  * New rule that checks coherency of hyphen usage in compounds, e.g. it complains
    when "Ärzteverband" and "Ärzte-Verband" are both used in the same text. While both
    spellings are correct, it's probably a good idea to stick to one spelling.
  * improved POS tagging of hyphenated compounds (e.g., "CO2-arm" is recognized as a variant of "arm")

#### Polish
  * added rules
  * disambiguation improvements

#### Portuguese
  * added and improved rules
  * LibreOffice category rules moved to other categories
  * disambiguation improvements
  * updated Hunspell dictionaries to:
    - [pt-PT pos-AO] Dicionários Portugueses Complementares 2.2
    - [pt-AO pre-AO] Dicionários Portugueses Complementares 2.2
    - [pt-MZ pre-AO] Dicionários Natura 14.08.2017

#### Russian
  * added and improved grammar and punctuation rules
  * spelling dictionary update
  * new Russian-English false friends added (thanks to ZakShaker)

#### Serbian
  * initial support for Serbian by Zoltán Csala

#### Ukrainian
  * big dictionary update (~10K new lemmas)
  * improvements in tokenization
  * compound word tagging improvements
  * more than 350 new disambiguation rules
  * several new barbarism and grammar rules

#### General
  * Now runs with Java 9 (compilation with Maven still has issues with Java9)
  * The spell checker tries harder to find suggestion for misspellings that have
    a Levenshtein distance of larger than 2. The maximum Levenshtein distance is now 3.
    This way you now get a suggestion for e.g. `algortherm` (algorithm) or `theromator` (thermometer).
    In the worst case (every single word of a text misspelled), this has a performance
    penalty of about 30%.
  * Better support for Unicode codepoints greater than `0xFFFF`

### word2vec
  * word2vec word embeddings (cf. http://colah.github.io/posts/2014-07-NLP-RNNs-Representations/#word-embeddings)
    are now supported as additional language models and currently available for
    English, German, and Portuguese.
  * Neural network based rules for confusion pair disambiguation using the
    word2vec model are available for English, German, and Portuguese. The necessary
    data must be downloaded separately from https://languagetool.org/download/word2vec/.
    For details, please see:
    * Code: https://github.com/gulp21/languagetool-neural-network
    * Forum discussion: https://forum.languagetool.org/t/neural-network-rules/2225
    * Paper: "Development of neural network based rules for confusion set disambiguation in LanguageTool"
      by Markus Brenneis and Sebastian Krings: https://fscs.hhu.de/languagetool/summary.pdf

#### GUI (stand-alone version)
  * show line numbers in the text area
  * a directory with word2vec language model for neural network rules can now be
    specified in the configuration dialog, see https://forum.languagetool.org/t/neural-network-rules/2225
  * Stop disposition of vertical scroll when expanding the checkbox.

#### Java API
  * A `RuleMatch` can now have a URL, too. The URL usually points to a page that
    describes the error or grammar rule in more detail. Before, only the `Rule`
    could have a URL. A `RuleMatch` URL will overwrite the `Rule` URL in the
    JSON output.
  * A `RuleMatch` now also has information about the sentence the error occurred in
    (it used to have only position information and the caller was expected to find
    the error context and/or sentence position in the original text).

#### HTTP API / LT server
  * change in configuration: `requestLimit` and `requestLimitPeriodInSeconds` now both
    need to be set for the limit to work
  * new property key `timeoutRequestLimit`: similar to `requestLimit`, but this one limits
    not all requests but blocks once this many timeouts have been caused by the IP in the
    time span set by `requestLimitPeriodInSeconds`
  * new property key `requestLimitInBytes`: similar to `requestLimit`, but this one limits
    the aggregated size of requests caused by an IP in the time span set
    by `requestLimitPeriodInSeconds`
  * New property key `maxErrorsPerWordRate`: set the maximum allowed errors per word, e.g.
    `0.3` if the maximum is about one error per three words. More errors will stop the
    check with an exception. This is useful so no processing time gets wasted for texts
    with a huge amount of errors that are only caused by the wrong language being
    selected (leading to most words being detected as spelling errors).
  * The JSON output now contains a `sentence` property with the text of the sentence
    the error occurred in.


## 3.9 (2017-09-26)

#### Breton
  * small rule improvements

#### Catalan
  * added and improved rules

#### Dutch
  * added and improved rules

#### English
  * added and improved rules

#### Esperanto
  * added and improved rules

#### French
  * added and improved rules
  * upgraded dictionaries to Dicollecte-6.1

#### German
  * added and improved rules
  * spell checker suggestions have been improved a lot by considering
    more words, especially compounds (de-DE only so far, not yet active for
    de-AT and de-CH)
    (https://github.com/languagetool-org/languagetool/issues/725)
  * added special dictionary extension files `spelling-de-AT.txt` and
    `spelling-de-CH.txt` for de-AT and de-CH that will be considered in addition
    to `spelling.txt`
  * updates according to "Amtliches Regelwerk der deutschen Rechtschreibung aktualisiert",
    6/2017 (http://www.rechtschreibrat.com/DOX/rfdr_PM_2017-06-29_Aktualisierung_Regelwerk.pdf)
  * added POS tagging of alternative imperative forms such as "Geh" or "küss" (in
    addition to "Gehe"/"küsse")
  * introduced two new line endings ('?' and '$') for the data-file `compounds.txt`; these
    endings indicate that the mid-word parts of the compound need to be lower-cased
    (e.g., 'Geräte Wahl' -> 'Gerätewahl')

#### Portuguese
  * added and improved grammar and style rules, including:
    - grammar: general agreement rules, pronominal collocations, paronyms and homophones
      detection improvements; time agreement rules added
    - punctuation: greetings and farewell punctuation
    - style: puffery, weasel words, weak expressions, and biased opinion words detection added
      (disabled by default)
    - syntax: new category; fragment detection improvements
    - typography: spacing, number, and mathematical symbol formatting improvements
  * disambiguation improvements
  * false friends added
    - Portuguese to Galician (16 new pairs)
  * significant POS tagging and synthesizing improvements
  * spellchecking exceptions for abbreviations, variables in formulas, units, and related
    statistical vocabulary
  * updated Hunspell dictionaries to:
    - [pt-PT pos-AO] Dicionários Portugueses Complementares 2.0
    - [pt-AO pre-AO] Dicionários Portugueses Complementares 2.0
    - [pt-MZ pre-AO] Dicionários Natura 15.06.2017

#### Russian
  * spelling dictionary update
  * added and improved some rules

#### Spanish
  * added and improved some rules

#### Ukrainian
  * significant dictionary update:
    - more than 60K of new words
    - some inflection adjustments
  * improved dynamic tagging for compound words
  * many new rules (barbarism, grammar, and spelling)
  * inflection agreement rule updates

#### Java API
  * `AnnotatedText` (built via `AnnotatedTextBuilder`) can now contain
    document-level meta data. This might be used by rules in the future.


## 3.8 (2017-06-27)

#### Catalan
  * added and improved rules
  * updated dictionary and rules for official names of Valencian municipalities

#### Chinese
  * added one rule

#### Dutch
  * added many rules (by Ruud Baars)
  * spelling dictionary update

#### English
  * added and improved rules

#### German
  * added and improved rules
  * improved messages for old spelling variants, e.g. `Kuß` now suggests only `Kuss` and
    also has a message explaining the user that `Kuß` is an old spelling

#### Polish
   * added rules
   * added some common typos

#### Portuguese
  * added and improved grammar and style rules, including:
    - grammar: general agreement rules, contractions, pronominal collocations, compounding, and paronyms detection
    - style: wordy expressions detection added and significant redundant expressions detection improvements
    - punctuation: significant improvements
    - formal speech: archaims, cacophonies, childish language and slang detection added
    - typography: international system standards, number and mathematical symbol formatting
    - misspellings: foreign famous personalities common misspelings
    - AO90: identify words with changed spelling
  * disambiguation improvements
  * false friends support added
    - Portuguese to Catalan (26 new pairs)
    - Portuguese to Spanish (7 new pairs)
  * spell checking exceptions for common Latin, English, and French expressions, species scientific names,
    and famous personalities
  * updated Hunspell dictionaries to:
    - [pt-PT pos-AO] Dicionários Portugueses Complementares 1.4
    - [pt-BR]        VERO version 2.1.4

#### Russian
  * added and improved rules

#### Slovak
  * major rule updates by Matúš Matula

#### Ukrainian
  * Significant dictionary update:
    - thousands of new words
    - some inflection adjustments
  * Improved dynamic tagging for compound words
  * Many new rules (barbarism, grammar, and spelling)
  * New noun-verb agreement rule

#### HTTP API
  * The deprecated AfterTheDeadline mode has been removed
  * The `apiVersion` property of the JSON output is now a number
    instead of a string (issue #712)

#### Java API
  * Some deprecated methods and classes have been removed.

#### Internal
  * `spelling.txt` allows multi-word entries: the words/tokens (separated by " ") of one
    line are converted to a `DisambiguationPatternRule` in which each word is a case-sensitive
    and non-inflected `PatternToken` (result: the entire multi-word entry is ignored by
    the spell checker)

#### LT server
   * When running a LT server, the enabled/disabled rules loaded from a configuration file
     at the startup time will be the new default rules. Previously these rules were "forgotten"
     when a server query used the parameters for enabling and disabling rules. Now the rules
     from the query will be added to the rules from the configuration file.


## 3.7 (2017-03-27)

#### Breton
  * small rule improvements

#### Catalan
  * added and improved rules
  * updated dictionary

#### English
  * added and improved rules

#### French
  * improved rules
  * upgraded dictionaries to Dicollecte-6.0.2

#### German
  * added and improved rules
  * added some common Latin, French, and English phrases that will be ignored by the spell checker
  * updated Hunspell dictionary to version 2017.01.12:
    * https://extensions.libreoffice.org/extensions/german-de-de-frami-dictionaries
    * https://extensions.libreoffice.org/extensions/german-de-at-frami-dictionaries
    * https://extensions.libreoffice.org/extensions/german-de-ch-frami-dictionaries

#### Greek
  * added and improved rules

#### Italian
  * added one rule

#### Lithuanian, Malayalam, and Icelandic
  * Lithuanian, Malayalam, and Icelandic are not part of this release anymore. They still
    exist in the git repository and can be re-activated as soon as a new maintainer takes
    care of them.

#### Portuguese
  * added and improved grammar and style rules, including:
    - grammar: general agreement rules, 'crase', pronomial colocations, impersonal verbs, fragment, and paronyms detection improvements
    - capitalization: AO90 and AO45 rules
    - style: repetitions and barbarism detection
    - typography: number formatting, chemical formulas, degrees signs, dash signs, and punctuation
    - semantics: wrong words in the context (22 confusion pairs), url validator and date checker improvements
    - registered brands category added
    - translation errors category added
  * false friends support added:
    - Portuguese to Spanish (186 new pairs)
    - Portuguese to English (156 new pairs)
    - Portuguese to French (78 new pairs)
    - Portuguese to German (16 new pairs)
    - Portuguese to Galician (9 new pairs)
  * spellchecking suggestions activated
  * updated Hunspell dictionary to:
    - [pt-PT pos-AO] Dicionários Portugueses Complementares 1.2
    - [pt-AO pre-AO] Dicionários Portugueses Complementares 1.2
    - [pt-MZ pre-AO] Dicionários Natura 18.02.2017

#### Russian
  * added and improved rules
  * updated tagger dictionary from AOT.ru rev.269 with extended POS tags

#### Ukrainian
  * Significant dictionary update:
    - many new words
    - some inflection adjustments
  * Many new rules (barbarism, punctuations, and grammar)
  * Improved dynamic tagging for compound words

#### LibreOffice / Apache OpenOffice Integration
  * Options dialog now uses system theme instead of Nimbus.

#### Command-line
  * Added a `--languageModel` option to the embedded server, thanks to
    Michał Janik (issue #404)

#### HTTP API
  * The 'AfterTheDeadline' mode has been deprecated and will be removed in
    the next version, unless users complain and present a valid use case.
  * The old XML-based API has been removed. The migration to the new JSON-based
    API is documented at https://languagetool.org/http-api/migration.php
  * Speed up with a cache for cases where the same sentences get checked
    again (e.g. due to a correction in a text that doesn't affect all sentences
    but causes the whole text to be re-checked)

#### Java API
  * Some deprecated methods have been removed.
  * A new class `ResultCache` has been added to speed up the LT server
  * `EnglishRule`, `GermanRule`, `CatalanRule`, and `FrenchRule`are now
    deprecated. These are empty abstract classes that never had any real
    use. Rules that extend these classes will directly extend `Rule` or
    `TextLevelRule` in a future release.
  * All rules that work on the text level instead of the sentence level
    (e.g. word coherency) now extend `TextLevelRule` instead of `Rule`

#### Internal
  * OpenNLP has been updated from 1.6.0 to 1.7.2 (only used for English)


## 3.6 (2016-12-28)

#### Breton
  * small rule improvements

#### Catalan
  * added and improved rules

#### English
  * added and improved rules
  * added about 131 confusion pairs like woman/women (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data)
  * The American and Canadian English (en-US, en-CA) spelling dictionaries have
    been updated to the latest version from http://wordlist.aspell.net (2016.06.26)
  * The Australian English (en-AU) spelling dictionary has been updated to the
    latest version from http://extensions.libreoffice.org/extension-center/english-dictionaries
    (2016-03-14 according to that page)

#### French
  * added and improved rules
  * upgraded dictionaries to Dicollecte-5.7

#### German
  * added and improved rules
  * added about 34 confusion pairs like ihm/im (works only with ngram data,
    see http://wiki.languagetool.org/finding-errors-using-n-gram-data)
  * bugfix regarding errors in the last word of a sentence (#273)
  * The internal part-of-speech dictionary has been updated with the help of
    Julian von Heyl of http://korrekturen.de - many entries have been fixed and
    added. The new data has its own Maven and git project now
    (https://github.com/languagetool-org/german-pos-dict)

#### Lithuanian
  * The `Lithuanian` class has been deprecated. Lithuanian in LT hasn't been maintained
    for years and there's no new maintainer in sight. It has also very low usage
    on languagetool.org and very few error detection rules anyway, so we'll remove its
    support from LT in the next release.

#### Malayalam
  * The `Malayalam` class has been deprecated. Malayalam in LT hasn't been maintained
    for years and there's no new maintainer in sight. It has also very low usage
    on languagetool.org and very few error detection rules anyway, so we'll remove its
    support from LT in the next release.

#### Portuguese
  * general agreement rules added
    * number and gender words agreement
    * general subject-verb agreement
    * accentuated form confusion, 'dequeísmos' and many more
  * new compound form detection (pt-PT recognizes all compound verbal derivations)
  * duplications, redundancies, typography and semantics categories added
  * style category rules added
    * new word repetitions rules, fragment detection, verbosity checks, passive voice and many other
  * new sentence disambiguator and new word tokenizer
  * sentence segmentation improvements
  * former rules and messages revision, improvement and classification
  * post-reform agreement support added and pre-reform components updated
    * European Portuguese specific rule group added
      * post-reform agreement by default
      * compound verbs, possessive pronouns, reflexive forms placement, gerund and more
    * pre-reform agreement locales support added
      * Angola, Cape Verde, East Timor, Guinea Bissau, Macau, Mozambique and São Tomé e Principe
    * base spelling dictionary and tagger update
    * variants dictionaries added and many part-of-speech fixes
  * Portuguese has been prepared to use ngram data, that means it has a
    `confusion_sets.txt` file where word pairs could be added.
    See http://wiki.languagetool.org/finding-errors-using-n-gram-data
    for more information but note that we cannot offer the required
    ngram data yet for Portuguese, as we rely on the Google ngram
    data and Portuguese isn't part of that.

#### Russian
  * added and improved many rules
  * added new rules with java filter
  * added new Java rule `RussianWordCoherencyRule`
  * added words suggested by users
  * improved disambiguation rules
  * updated tagger dictionary from AOT rev.268 with extended POS tags
  * improved SRX sentences segmentation
  * added `removed.txt` for words that need to be removed from the dictionary

#### Spanish
  * added and improved rules

#### Ukrainian
  * significant dictionary update
  * new adj/noun inflection rule
  * dynamic tagging improvements
  * disambiguation improvements
  * some improvements to existing rules
  * experimental noun/verb agreement rule

#### HTTP API
  * The old API has been deactivated, as documented at
    https://languagetool.org/http-api/migration.php - it
    now returns a pseudo error pointing to the migration page

#### Java API
  * A new method for removing overlapping errors has been implemented. By default,
    it is enabled for the HTTP API and LibreOffice outputs, and disabled for the
    command-line output. If necessary, priorities for rules and categories can bet set
    in `Language.getPriorityForId(String id)`. Default value is `0`, positive integers have
    higher priority and negative integers have lower priority.
  * `Language.getShortName()` has been deprecated, use `Language.getShortCode()`
    instead
  * `Language.getShortNameWithCountryAndVariant()` has been deprecated, use
    `Language.getShortCodeWithCountryAndVariant()` instead
  * `Languages.getLanguageForShortName()` has been deprecated, use
    `Languages.getLanguageForShortCode()` instead
  * The following languages have been unmaintained for a long time. A warning has been
    shown for some time on languagetool.org and in the stand-alone GUI for these
    languages. This warning has now been extended to Java in the form of a deprecation,
    i.e. the constructors of the following languages have been deprecated. That does
    *not* mean they are going to be removed in the next version, but it's a warning
    that we cannot offer support for them or guarantee they will be included in the
    future:
    * Belarusian
    * Swedish
    * Icelandic
    * Tagalog
    * Asturian
    * Danish
    * Slovenian

    If you're interested in contributing to one of these languages, please post to
    our forum at http://forum.languagetool.org.
  * The uppercase sentence start rule (id `UPPERCASE_SENTENCE_START`) now ignores
    immunized tokens - this way users can add lowercase words to `disambiguation.xml`
    so the rule won't complain about these lowercase words at the beginning of a sentence.

#### Command-line
  * Added a `--json` option as an alternative to `--api` (deprecated XML output)
    See https://languagetool.org/http-api/swagger-ui/#/default
    for a documentation of the new API.

#### Internal
  * Apache commons-lang has been updated from 2.6 to commons-lang3 3.5
  * Updated lucene-gosen-ipadic to 6.2.1 (#376)


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
