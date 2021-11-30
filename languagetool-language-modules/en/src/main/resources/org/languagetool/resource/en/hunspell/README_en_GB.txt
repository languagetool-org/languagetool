This dictionary was initially based on a subset of the
original English wordlist created by Kevin Atkinson for
Pspell and Aspell and thus is covered by his original
LGPL licence.

It has been extensively updated by David Bartlett, Brian Kelk,
Andrew Brown and Marco A.G.Pinto:
 — Numerous Americanisms/spellings have been removed;
 — Missing words have been added;
 — Many errors have been corrected;
 — Compound hyphenated words have been added where appropriate;
 — Thousands of proper/places names have been added;
 — Thousands of possessives have been added;
 — Thousands of plurals have been added;
 — Thousands of duplicates have been removed.

Valuable inputs to this process were received from many other
people — far too numerous to name. Serious thanks to all for
your greatly appreciated help.

This wordlist is intended to be a good representation of
current modern British English and thus it should be a good
basis for Commonwealth English in most countries of the world
outside North America.

The affix file has been created completely from scratch
by David Bartlett and Andrew Brown, based on the published
rules for MySpell and is also provided under the LGPL.

In creating the affix rules an attempt has been made to
reproduce the most general rules for English word
formation, rather than merely use it to compress the
size of the dictionary. It is hoped that this will
facilitate future localisation to other variants of English.

---

This is a locally hosted copy of the English dictionaries with fixed
dash handling and new ligature and phonetic suggestion support extension:
https://extensions.openoffice.org/en/node/3785

Original version of the en_GB dictionary:
https://bz.apache.org/ooo/show_bug.cgi?id=72145

OpenOffice.org patch and morphological extension.

The morphological extension based on Wordlist POS and AGID data
created by Kevin Atkinson and released on http://wordlist.sourceforge.net.

Other fixes:

OOo Issue 48060 — add numbers with affixes by COMPOUNDRULE (1st, 111th, 1990s etc.)
OOo Issue 29112, 55498 — add NOSUGGEST flags to taboo words
New REP items (better suggestions for accented words and a few mistakes)
OOo Issue 63541 — remove *dessicated

2008-12-18 — NOSUGGEST, NUMBER/COMPOUNDRULE patches (nemeth AT OOo)
2010-03-09 (nemeth AT OOo)
		   — UTF-8 encoded dictionary:
			 — Fix em-dash problem of OOo 3.2 by BREAK
			 — Suggesting words with typographical apostrophes
			 — Recognising words with Unicode f ligatures
			 — Add phonetic suggestion (© 2000 Björn Jacke)
2013-08-25 — GB forked by Marco A.G.Pinto
2016-06-10 — NOSUGGEST added to this clean version of the GB .AFF (Marco A.G.Pinto)
2016-06-21 — COMPOUNDING added to this clean version of the GB .AFF (Áron Budea)
2016-08-01 — GB changelog is no longer included in the README file
2016-09-11 — .AFF + .DIC now use UNIX line endings
2017-10-08 — Mozilla: used <em:maxVersion>*</em:maxVersion> to work with all future
			 versions, except Thunderbird
2017-12-16 — Added to the .AFF:
			 ICONV 1
			 ICONV ’ '
			 Thanks to Jeroen Ooms
2018-05-01 — Andrew Ziem suggested a list of 328 names of famous people on Kevin's GitHub:
			 "These 328 name tokens were derived from the top 100 lists in Google Trends via
			 this repository (https://github.com/az0/google-trend-names). The geography was
			 set to US, and it spanned dates from 2004 to 2018."
2018-08-01 — Slightly higher quality icon
		   — Added tons of drugs names supplied by the user Andrew Ziem on Kevin's GitHub
		   — Fixed/improved flag "5": "women's" was missing
2018-06-01
to
2018-09-01 — Added places from New Zealand/UK (England, Scotland, Wales & Northern Ireland):
			 On V2.61–2.64 I included tons of place names.
			 My scientist friend, Peter McGavin, told me that in NZ they use British, so I decided
			 to do something about it. I did the same for UK. I searched on Wikipedia for "towns",
			 "counties", "villages", "boroughs", "suburbs", etc. and based me on:
			  — https://en.wikipedia.org/wiki/List_of_towns_in_England;
			  — https://en.wikipedia.org/wiki/List_of_towns_in_New_Zealand;
			  — https://en.wikipedia.org/wiki/List_of_civil_parishes_in_England;
			  — https://en.wikipedia.org/wiki/List_of_civil_parishes_in_Scotland;
			  — https://en.wikipedia.org/wiki/List_of_places_in_Scotland;
			  — https://en.wikipedia.org/wiki/List_of_communities_in_Wales;
			  — https://en.wikipedia.org/wiki/Local_government_in_Wales;
			  — https://en.wikipedia.org/wiki/List_of_towns_and_villages_in_Northern_Ireland;
			  — https://en.wikipedia.org/wiki/Counties_of_Northern_Ireland;
			  — https://en.wikipedia.org/wiki/Category:Suburbs_in_New_Zealand;
			  — https://en.wikipedia.org/wiki/List_of_Church_of_Scotland_parishes.
			 Also, added places sent to me by Peter C.:
			 © OpenStreetMap contributors: www.openstreetmap.org/copyright.
			 © The Clergy of the Church of England Database Project, 2005.
2018-10-01 — Added the cities from Australia by population:
			  — https://en.wikipedia.org/wiki/List_of_cities_in_Australia_by_population
		   — Added tons of cities from the US with a 10 000+ population.
			 This list was supplied by Michael Holroyd on Kevin Atkinson's GitHub.
		   — Added tons of possessives to nouns, thanks to Jörg Knobloch.
2018-12-01 — Added the cities from Canada:
			  — https://en.wikipedia.org/wiki/List_of_cities_in_Canada
2019-02-01 — Improved flag "5" thanks to the GitHub user Ding-adong:
			 Some "swomen's" and "women's" entries were missing.
		   — Fixed flag "3": -ists, -ists, -ist's → -ist, -ists, -ist's.
		   — Improved flag "N".
2019-03-01 — Added the LGPL_V3 License .txt into the Extension.
		   — Ding-adong added a flag "=" for suffixes: -lessness, -lessnesses, -lessness's.
		   — Ding-adong changed the prefix flag "O" to "^" since "O" was both prefix and suffix.
		   — Small fixes and enhancements on flags "z" and "O" by Ding-adong.
2019-04-01 — Improved flag "P" thanks to Ding-adong, giving also -nesses which
			 increased the wordlist in ~1800 valid words.
2019-07-01
to
2019-10-01 — Major cleanup of the .dic by removing thousands of duplicates, merging flags, adding
			 possessives and plurals.
		   — Improved flags: "i", "n", "N", "O", "W", "Z", "2" and "3":
			  — Flag "2" increased the wordlist in ~400 valid words;
			  — Flag "i" increased the wordlist in ~200 valid words;
			  — Flag "n" increased the wordlist in ~1000 valid words.
2020-11-01 — Added the State and union territory capitals in India:
			  — https://en.wikipedia.org/wiki/List_of_state_and_union_territory_capitals_in_India		  
2019-11-01
to
2021-12-01 — Added thousands of possessives and plurals.
		   — Improved flags: "3", "N", "O", "W".
-------

MARCO A.G.PINTO:
Since the dictionary wasn't updated for many years, I forked it in 2013 to add new words and fixes.

I grabbed Mozilla's version since it wasn't obfuscated. Alexandro Colorado and I
tried to unmunch the OpenOffice version but all we got was rubbish.

The dictionary icon in the Extension Manager was designed by Pedro Marques.

The sources used to verify the spelling of the words I included in the dictionary:
 1) Oxford Dictionaries;
 2) Collins Dictionary;
 3) Macmillan Dictionary;
 4) Cambridge Dictionary;
 5) Merriam-Webster Dictionary (used with caution ⚠);
 6) Wiktionary (used with caution ⚠);
 7) Wikipedia (used with caution ⚠);
 8) Physical dictionaries.

Main difficulties developing this dictionary:
 1) Proper names;
 2) Possessive forms;
 3) Plurals.

Please let Marco A.G.Pinto know of any errors that you find:
E-mail:
marcoagpinto@sapo.pt

Site:
https://proofingtoolgui.org

FAQ:
https://proofingtoolgui.org/faq.html

FAQ ("movie", "automobile", "airplane", "hardcover" and "bookstore"):
https://proofingtoolgui.org/faq.html#7
Notice: Due to complaints, "movie" was added on V2.57 since it is a widely used word.

Changelog:
https://proofingtoolgui.org/en_GB_CHANGES.txt

Nightly changes (GitHub):
https://github.com/marcoagpinto/aoo-mozilla-en-dict
