# LanguageTool

**A proof-reading tool for English, Spanish, German,
Polish, Chinese, French, Russian, Italian, Dutch
and [more languages](https://www.languagetool.org/languages/)**

Version 4.2, 2018-06-26  
Copyright (C) 2005-2018 the LanguageTool community and Daniel Naber (www.danielnaber.de)  
https://www.languagetool.org


## Requirements

* Java 8 or later
* For LibreOffice/OpenOffice.org integration:
    * LibreOffice 3.5.4 (or later) or
    * Apache OpenOffice 3.4.1 (or later)


## Usage

### LibreOffice/OpenOffice

To integrate LanguageTool into LibreOffice or OpenOffice.org, you can use two methods:

* Double-click `LanguageTool-4.2.oxt`. The extension should
   start installing. Follow the on-screen instructions.

* If the above method doesn't work, call `Tools > Extension
   Manager > Add...` in LibreOffice/OpenOffice.org and browse for the
   `LanguageTool-4.2.oxt` file.

Close and restart LibreOffice/OpenOffice.org Writer. Remember to close
the QuickStarter as well if you use it. Type text with
an error, e.g. "Feel tree to do so." - make sure the text language
is set to English for this example.

You should see a blue underline under the word "tree" after about a second.
Opening the context menu with the right mouse button on that word
offers you a short description of the error and a correction ("free").

If you are using LibreOffice and you want to check English or Russian texts:
Use `Options -> Language Settings -> Writing Aids -> Edit...` in the
`Tools` menu to disable LightProof and enable LanguageTool for English.

Note that there will also be a new menu item "LanguageTool"
under the `Tools` menu.
If the native spelling and grammar dialog doesn't check grammar,
make sure that the check box `Check Grammar` is checked in it
(if the window closes because of no mistakes in the document,
simply make any spelling mistake to make it open for a longer
time, and check the box). Check also if LanguageTool is visible
under `Grammar` in `Tools > Options > Language Settings > Spelling`
for your language. Note: you can disable the grammar check without
uninstalling LanguageTool simply by clearing the check box next to
LanguageTool in the same dialog.
  
Please see https://www.languagetool.org/issues/ if you experience problems.

### Stand-alone version

To use the stand-alone version, double click on the `languagetool.jar` file
or call `java -jar languagetool.jar` from the command line.

### Command-line version

To check plain text files from the command line, use

    java -jar languagetool-commandline.jar -l xx <filename>

with `xx` being the code for your language, e.g. `en-US` for American English
or just `en` for English without spell checking activated.


## Source code history

On 2013-08-08 we moved our source code from Subversion at Sourceforge to
git at github (https://github.com/languagetool-org/languagetool). Most
history has been preserved. History in git is lost for:

* binary files
* branches and tags

If you need this history, the old subversion repository is still available
at http://svn.code.sf.net/p/languagetool/code/trunk/languagetool/.


## License
 
Unless otherwise noted, this software is distributed under
the LGPL, see file [COPYING.txt](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/COPYING.txt)

See [third-party-licenses/README.txt](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/src/main/resources/third-party-licenses/README.txt) for the copyright of the external libraries.

#### Frequency data

Some language's spelling dictionaries contain frequency data. This is taken
from the Mozilla-B2G Gaia project (https://github.com/mozilla-b2g/gaia/) which
again takes it from Spell On It (http://www.spellonit.com/downloads/frequencies/).
The frequency data is released under Creative Commons Attribution 4.0
International License (http://creativecommons.org/licenses/by/4.0/).

#### Asturian

See org/languagetool/resource/ast/README.txt and org/languagetool/resource/ast/hunspell/LICENSES*.txt

#### Belarusian

See org/languagetool/resource/be/hunspell/README.txt

#### Breton

See org/languagetool/resource/br/README.txt and See org/languagetool/resource/br/hunspell/README.txt

#### Catalan

See org/languagetool/resource/ca/README.txt

#### Chinese

See org/languagetool/resource/zh/README.txt

#### Danish

See org/languagetool/resource/da/README.txt and org/languagetool/resource/da/spelling/README_da_DK.txt

#### Dutch

See org/languagetool/resource/nl/README.txt and org/languagetool/resource/nl/spelling/README.txt
 
#### English

The English data for part-of-speech tagging are based on:

##### Automatically Generated Inflection Database (AGID) version 4

Automatically Generated Inflection Database (AGID) version 4,
Copyright 2000-2003 by Kevin Atkinson <kevina@gnu.org>
The part-of-speech database is taken from Alan Beale 2of12id
and the WordNet database which is under the following copyright:

    This software and database is being provided to you, the LICENSEE, by
    Princeton University under the following license.  By obtaining, using
    and/or copying this software and database, you agree that you have
    read, understood, and will comply with these terms and conditions.:

    Permission to use, copy, modify and distribute this software and
    database and its documentation for any purpose and without fee or
    royalty is hereby granted, provided that you agree to comply with
    the following copyright notice and statements, including the disclaimer,
    and that the same appear on ALL copies of the software, database and
    documentation, including modifications that you make for internal
    use or for distribution.

    WordNet 1.6 Copyright 1997 by Princeton University.  All rights reserved.

    THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" AND PRINCETON
    UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR
    IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION, PRINCETON
    UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES OF MERCHANT-
    ABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE
    OF THE LICENSED SOFTWARE, DATABASE OR DOCUMENTATION WILL NOT
    INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR
    OTHER RIGHTS.

    The name of Princeton University or Princeton may not be used in
    advertising or publicity pertaining to distribution of the software
    and/or database.  Title to copyright in this software, database and
    any associated documentation shall at all times remain with
    Princeton University and LICENSEE agrees to preserve same.

Alan Beale 2of12id.txt is indirectly derived from the Moby part-of-speech
database and the WordNet database.  The Moby part-of-speech is in the
public domain:

    The Moby lexicon project is complete and has
    been place into the public domain. Use, sell,
    rework, excerpt and use in any way on any platform.

    Placing this material on internal or public servers is
    also encouraged. The compiler is not aware of any
    export restrictions so freely distribute world-wide.

    You can verify the public domain status by contacting

    Grady Ward
    3449 Martha Ct.
    Arcata, CA  95521-4884

    grady@netcom.com
    grady@northcoast.com

    For more information on wordlists used, see agid-readme.txt.

##### Part Of Speech Database, compiled by Kevin Atkinson <kevina@users.sourceforge.net>

The part-of-speech.txt file contains is a combination of
"Moby (tm) Part-of-Speech II" and the WordNet database (see above and
[pos-readme.txt](https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/en/src/main/resources/org/languagetool/resource/en/pos-readme.txt)).

##### 2of12inf wordlist

Released to public domain, see `resource/en/12dicts-readme.html`.

##### Public domain Moby wordlists

Public domain Moby wordlists were used also for generating
POS tag information for common proper names.

For more information, see the scripts in the source directory
`languagetool-language-modules/en/src/main/resources/org/languagetool/resource/en/`.

#### French

See org/languagetool/resource/fr/README_lexique.txt and org/languagetool/resource/fr/hunspell/fr_FR.README

#### Galician

See org/languagetool/resource/gl/README.txt and org/languagetool/resource/gl/hunspell/README-gl-ES.txt
and LICENSES-en.txt

#### German

See org/languagetool/resource/de/README.txt and org/languagetool/resource/de/hunspell/*README.txt

#### Greek

See org/languagetool/resource/el/README.txt and org/languagetool/resource/el/hunspell/README_el_GR.txt

#### Italian

See org/languagetool/resource/it/README.txt and org/languagetool/resource/it/hunspell/README_it_IT.txt
 
#### Khmer

See org/languagetool/resource/km/README.txt

#### Malayalam (inactive)

See org/languagetool/resource/ml/README.txt
 
#### Polish

See org/languagetool/resource/pl/README.txt and org/languagetool/resource/pl/hunspell/README_en.txt

#### Romanian

See org/languagetool/resource/ro/README.txt and org/languagetool/resource/ro/hunspell/README_*.txt

#### Russian

See org/languagetool/resource/ru/README.txt and org/languagetool/resource/ru/hunspell/README.txt

#### Slovak

See org/languagetool/resource/sk/README.txt

#### Spanish

See  org/languagetool/resource/es/hunspell/README_es_ES.txt and org/languagetool/resource/es/README.cvs

#### Swedish

See org/languagetool/resource/sv/hunspell/LICENSE*.txt and org/languagetool/resource/sv/README.txt
 
#### Tagalog

See org/languagetool/resource/tl/README.txt

#### Tamil

See org/languagetool/resource/ta/README.txt

#### Ukrainian

See org/languagetool/resource/uk/README.txt
