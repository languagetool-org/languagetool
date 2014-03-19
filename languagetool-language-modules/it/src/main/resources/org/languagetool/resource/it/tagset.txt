===================================================================
                            Morph-it!

       A free morphological lexicon for the Italian Language
===================================================================

                         version 0.4.8
                         February 23 2009

*******************************************************************
                THIS README IS NOT REALLY UP TO DATE
                      A NEW VERSION OF THIS
                       README FILE WILL BE
                    RELEASED (HOPEFULLY) SOON
                (BUT I WOULDN'T COUNT ON THAT...)
*******************************************************************

                     Copyright (c) 2004-2009
              Marco Baroni  (marco.baroni@unitn.it)
              Eros Zanchetta (eros@sslmit.unibo.it)

                 http://sslmit.unibo.it/morphit


Morph-it! is a free (as in free speech and in free beer) morphological
resource for the Italian language.

Morph-it! is a lexicon of inflected forms with their lemma and
morphological features. For example:

gattini		gattino		NOUN-M:p
andarono	andare		VER:ind+past+3+p
fastidiosetto	fastidioso	ADJ:dim+m+s

As of version 0.4.7 the list contains 504,906 entries and 34,968
lemmas.

Morph-it! can be used as a data source for an Italian lemmatizer /
morphological analyzer / morphological generator.

As example applications, on the Morph-it! site you can download the
lexicon compiled for the SFST [1] and Finite State Utilities [2]
packages.

The data for Morph-it! were prepared by Marco Baroni and Eros
Zanchetta using a mixture of corpus-based methods,
regular-expression-based rules and manual checking. We are currently
writing a paper that describes the procedure we used to build the
resource.

Morph-it! is still under development and there may still be gaps,
unlikely forms, etc. We will be very grateful if you let us know
about missing forms, problems, and ideas/resources that can help
us expanding or cleaning the list (sslmitdevonline@sslmit.unibo.it).

Notice in particular that, since we extracted data from an Italian
newspaper corpus (the la Repubblica corpus, also accessible from our
site), we have many gaps in basic, every-day vocabulary.

Also, the current version does not distinguish between coordinative
and subordinative conjunctions. We plan to do this in the near
future. More in general, we are not fully satisfied with our current
features for function words, and we plan to revise them.

A more ambitious plan we would like to pursue is the identification
of derivational structure and derivationally related lemmas. Then, we
will add full semantic representations. Then, we will take over the
world and reign supreme for the next 100 years.

The remainder of this document contains a commented list of the
morphological features used in the lexicon, licensing information and
aknowledgments.


FEATURES
========

We distinguish between derivational features, that pertain to the
lemma, and inflectional features, that pertain to the wordform.

Derivational and inflectional features are separated by a colon.

The derivational features are in upper case and they are
dash-delimited. The inflectional features are in lower case and they
are plus-sign-delimited.

For example, we represent gender as a derivational feature of nouns
(we take "cameriere" and "cameriera" to belong to different lemmas),
whereas we treat number as an inflectional feature of nouns. Thus,
gender and number are represented as in the following examples:

cameriere       cameriera       NOUN-F:p
cameriera       cameriera       NOUN-F:s
camerieri       cameriere       NOUN-M:p
cameriere       cameriere       NOUN-M:s

For adjectives, gender is considered an inflectional feature. Thus,
gender is represented differently in adjectives and nouns:

azzurre azzurra NOUN-F:p
azzurra azzurra NOUN-F:s
azzurri azzurro NOUN-M:p
azzurro azzurro NOUN-M:s

azzurra azzurro ADJ:pos+f+s
azzurri azzurro ADJ:pos+m+p
azzurro azzurro ADJ:pos+m+s
azzurre azzurro ADJ:pos+f+p

Changes that are purely orthographical/phonological but do not affect
morphology/syntax/meaning are not reflected in the features. For
example, the following variants of "cento" share the same lemma and
the same features:

cent'   cento   DET-NUM-CARD
cento   cento   DET-NUM-CARD

We now present the full list of features we used, organized by major
syntactic categories.

ABL

Abbreviated locutions, such as "a.C.", "ecc." and "i.e."

ADJ

Adjectives, with the following inflectional features:

pos/comp/sup

Thas is: positive, comparative, superlative. Although these are not
true inflectional features, given their high productivity we decided
to represent them as properties of inflected forms.

f/m

That is: feminine, masculine.

s/p

Thas is: singular, plural.

ADV

Adverbs.

ART

Articles, with gender as a derivational feature (F/M) and number as an
inflectional feature (s/p).

ARTPRE

Preposition+article compounds ("col", "della", "nei"...), with gender
as a derivational feature (F/M) and number as an inflectional feature
(s/p).

ASP

Aspectuals ("stare" in "stare per"). Same inflectional features as VER
(see below).

AUX

Auxiliaries ("essere", "avere", "venire"). Same inflectional features
as VER (see below).

CAU

Causatives ("fare" in "far sapere"). Same inflectional features as VER
(see below).

CE

Clitic "ce" as in "ce l'ho fatta".

CI

Clitic "ci" as in "ci prova".

CON

Conjunctions.

DET-DEMO

Demonstrative determiners (such as "questa" in "questa sera"), with
inflectional gender (f/s) and number (s/p) features.

DET-INDEF

Indefinite determiners (such as "molti" in "molti amici") with
inflectional gender (f/s) and number (s/p) features.

DET-NUM-CARD

Cardinal number determiners (e.g., "cinque" in "cinque
amici"). Pure-digit numbers are not included (i.e., the list includes
"100mila" but not "100000" nor "100,000", "100.000", etc.)

DET-POSS

Possessive determiners (e.g., "mio", "suo"), with inflectional gender
(f/s) and number (s/p) features.

DET-WH

Wh determiners (e.g., quale in "quale amico"), with inflectional
gender (f/s) and number (s/p) features.

INT

Interjections.

MOD

Modal verbs (e.g. "dover" in "dover ricostruire"). Same inflectional
features as VER (see below).

NE

Clitic "ne" (as in: "ne hanno molte").

NOUN

Nouns, with gender as a derivational feature (F/M) and number as an
inflectional feature (s/p).

PON

Non-sentential punctuation marks (e.g. , " $).

PRE

Prepositions.

PRO-DEMO

Demonstrative pronouns (e.g. "questa" in "voglio questa"), with both
gender and number as derivational features (F/M, S/P).

PRO-INDEF

Indefinite pronouns (e.g., "molti" in "vengono molti"), with both
gender and number as derivational features (F/M, S/P).

PRO-NUM

Numeral pronouns (e.g., "cinque" in "cinque sono
sopravvissuti"). Pure-digit numbers are not included (e.g., the list
includes "100mila" but not 100000 nor 100,000, 100.000, etc.)

PRO-PERS

Personal pronouns, such as "lui" and "loro". Clitic possessive
pronouns (such as pronominal "lo" and "si") are marked by the
derivational feature CLI. Person, gender and number are also encoded
as derivational features (1/2/3, F/M, S/P).

PRO-POSS

Possessive pronouns, such as "loro" in "non era uno dei loro"), with
gender and number encoded as derivational features (F/M, S/P).

PRO-WH

Wh-pronouns, such as "quale" in "quale e' venuto?"

SENT

End of sentence marker (! . ... : ?).

SI

Clitic "si" as in "di cui si discute".

TALE

"Tale" in constructions such as "una fortuna tale che...", "la tal
cosa", "tali amici", ecc. Gender (f/m) and number (s/p) as
inflectional features.

VER

Verbs, with the following inflectional features:

cond/ger/impr/ind/inf/part/sub

Conditional, gerundive, imperative, indicative, infinitive,
participle, subjunctive.

pre/past/impf/fut

Present, past, imperfective, future.

1/2/3

Person.

s/p

Number.

f/m

Gender (only relevant for participles).

cela/cele/celi/celo/cene/ci/gli/gliela/gliele/glieli/glielo/gliene/la/
le/li/lo/mela/mele/meli/melo/mene/mi/ne/sela/sele/seli/selo/sene/si/
tela/tele/teli/telo/tene/ti/vela/vele/veli/velo/vene/vi

Clitics attached to the verb.

WH

Wh elements ("come", "qualora", "quando"...)

WH-CHE

"Che" as a wh element (e.g., "l'uomo che hai visto", "hai detto che").


LICENSING INFORMATION
======================

This program is dual-licensed free software; you can redistribute it
and/or modify it under the terms of the under the Creative Commons 
Attribution ShareAlike 2.0 License and the GNU Lesser General Public
License.

***********************************************
* Creative Commons Attribution ShareAlike 2.0 *
***********************************************

Morph-it! is licensed under the Creative Commons Attribution
ShareAlike 2.0 License.

You are free:

- to copy, distribute and display the resource;
- to make derivative works;
- to make commercial use of the resource;

under the following conditions:

- you must give the original authors credit;
- if you alter, transform, or build upon this work, you may distribute
  the resulting work only under a license identical to this one;
- for any reuse or distribution, you must make clear to others the
  license terms of this work;
- any of these conditions can be waived if you get permission from the
  copyright holders.

Your fair use and other rights are in no way affected by the above.

You can find a link to the full license from the Morph-it! website.

Copyright (C) 2004-2007 Marco Baroni and Eros Zanchetta.

*************************************
* GNU Lesser General Public License *
*************************************

Morph-it! A free morphological lexicon for the Italian Language
Copyright (C) 2004-2007 Marco Baroni and Eros Zanchetta

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

AKNOWLEDGMENTS
==============

The main data source for the Morph-it! lexicon was the "la Repubblica"
corpus. Thus, we would like to thank the colleagues who developed this
resource with us: Lorenzo Piccioni, Guy Aston, Silvia Bernardini,
Federica Comastri, Alessandra Volpi, Marco Mazzoleni.

We would like to thank the developers of the tools we used to tag,
lemmatize and index the Repubblica corpus: the (Italian) TreeTagger
(Helmut Schmid, Achim Stein), the ACOPOST taggers (Ingo Schroeder) and
the IMS Corpus WorkBench (Oli Christ, Arne Fitschen and Stefan Evert).

Thanks to Helmut Schmid also for converting the Morph-it! lexicon into
a SFST transducer.

We would like to thank Aldo Calpini, who developed the perl module
Lingua:IT:Conjugate.

We are also very grateful to Jan Daciuk for creating his finite-state
utilities and for helping us learn to use them.

Finally, a big thanks to the members of the FoLUG, SannioLUG and
Scuola (software libero nella scuola) mailing lists, for advice about
licensing and dissemination.

...and kudos to Lorenzo for creating and maintaining the SSLMITDev
site!


FOOTNOTES
=========

[1] http://www.ims.uni-stuttgart.de/projekte/gramotron/SOFTWARE/SFST.html
[2] http://juggernaut.eti.pg.gda.pl/~jandac/fsa.html
