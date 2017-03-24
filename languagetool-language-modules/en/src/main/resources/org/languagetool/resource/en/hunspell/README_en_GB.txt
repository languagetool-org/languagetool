This dictionary was initially based on a subset of the
original English wordlist created by Kevin Atkinson for
Pspell and  Aspell and thus is covered by his original
LGPL licence. 

It has been extensively updated by David Bartlett, Brian Kelk
and Andrew Brown:
- Numerous Americanism have been removed;
- Numerous American spellings have been corrected;
- Missing words have been added;
- Many errors have been corrected;
- Compound hyphenated words have been added where appropriate.

Valuable inputs to this process were received from many other
people - far too numerous to name. Serious thanks to you all
for your greatly appreciated help.

This wordlist is intended to be a good representation of
current modern British English and thus it should be a good
basis for Commonwealth English in most countries of the world
outside North America.

The affix file has been created completely from scratch
by David Bartlett and Andrew Brown, based on the published
rules for MySpell and is also provided under the LGPL.

In creating the affix rules an attempt has been made to
reproduce the most general rules for English word
formation, rather than merely use it as a means to
compress the size of the dictionary. It is hoped that this
will facilitate future localisation to other variants of
English.

---

This is a locally hosted copy of the English dictionaries with fixed dash handling and new ligature and phonetic suggestion support extension:
http://extensions.openoffice.org/en/node/3785

Original version of the en_GB dictionary:
http://www.openoffice.org/issues/show_bug.cgi/id=72145

OpenOffice.org patch and morphological extension.

The morphological extension based on Wordlist POS and AGID data
created by Kevin Atkinson and released on http://wordlist.sourceforge.net.

Other fixes:

OOo Issue 48060 - add numbers with affixes by COMPOUNDRULE (1st, 111th, 1990s etc.)
OOo Issue 29112, 55498 - add NOSUGGEST flags to taboo words
New REP items (better suggestions for accented words and a few mistakes)
OOo Issue 63541 - remove *dessicated

2008-12-18 - NOSUGGEST, NUMBER/COMPOUNDRULE patches (nemeth AT OOo)
2010-03-09 (nemeth AT OOo)
 - UTF-8 encoded dictionary:
      - fix em-dash problem of OOo 3.2 by BREAK
      - suggesting words with typographical apostrophes
      - recognizing words with Unicode f ligatures
 - add phonetic suggestion (Copyright (C) 2000 Björn Jacke, see the end of the file)

 2013-08-25 - GB Forked by Marco A.G.Pinto
 2016-06-10 - NOSUGGEST added to this clean version of the GB .AFF (Marco A.G.Pinto)
 2016-06-21 - COMPOUNDING added to this clean version of the GB .AFF (Áron Budea)
 2016-08-01 - GB changelog is no longer included in the README file.
 
-------

MARCO A.G.PINTO:
Since the dictionary hasn't been updated for many years,
I decided to fork it in order to add new words and fixes.

I even added words such as common names of software and hardware.

I grabbed Mozilla's version since it wasn't obfuscated. Alexandro Colorado and I
tried to unmunch the OpenOffice version but all we got was garbage.

The dictionary icon in the Extension Manager was designed by Pedro Marques.

The sources used to verify the spelling of the words I included in the dictionary:
1) Oxford Dictionaries;
2) Collins Dictionary;
3) Macmillan Dictionary;
4) Wiktionary (used with caution);
5) Wikipedia (used with caution);
6) Physical dictionaries

Main difficulties developing this dictionary:
1) Proper names;
2) Possessive forms;
3) Plurals.

Please let Marco A.G.Pinto know of any errors that you find:
E-mail:
marcoagpinto@mail.telepac.pt

Site:
http://marcoagpinto.cidadevirtual.pt/proofingtoolgui.html

FAQ:
http://marcoagpinto.cidadevirtual.pt/faq.html

Changelog:
http://marcoagpinto.cidadevirtual.pt/en_GB_CHANGES.txt

Nightly changes (GitHub):
https://github.com/marcoagpinto/aoo-mozilla-en-dict
