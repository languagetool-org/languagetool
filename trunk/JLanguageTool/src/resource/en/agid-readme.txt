Automatically Generated Inflection Database (AGID)

January 3, 2003
Revision 4

Copyright 2000-2003 by Kevin Atkinson <kevina@gnu.org>

The file "infl.txt" is an automatically created database of the
inflected forms of words from a rather large word list.

The latest version can be found at http://aspell.sourceforge.net/wl/.

Entries are in the following form.

<word><sp><pos>[?]:<sp><inflected forms>
<word>             := [[A-Za-z']]+
<sp>               := <literal space>
<pos>              := [[VNA]]
<inflected forms>  := <inflected form><sp>|<sp>...<sp>|<sp><inflected form>
<inflected form>   := <individual entry>,<sp>...,<sp><individual entry>
<individual entry> := <word><word tags>[<sp><variant level>][<sp>{<explanation>}]
<word tags>        := [~][<][!][?]
<explanation>      := [<explanation text>][:<distinguishing number>]
<explanation text> := [[A-Za-z'_/]]+

where stuff between [ ] is optional, stuff between [[ ]] indicate a
range of possible characters for that entry.  If a [[ ]] is followed by
a + it means the entry can consist of one or more characters in
that range. { } are literal.

A typical entry will look like

WORD V: WORDed, WORed 2, WORD {EXPL} | WORDing, WORing 2 | WORDs

<pos> is V for verb, N for noun, or A or adjective or adverb.
If <pos> is followed by a ? that means that the part-of-speech was not
in the part-of-speech database however the inflected forms of the word
where found in the word list.

The inflected forms are in the following order for verbs (except for
a few special verbs):
  <past tense> [<past participle>] <-ing form> <-s form> 
and for adjective or adverbs:
  <-er form> <-est form>
Each form is seperated by a ' | '.  

An absence of a variant level implies a variant level of 0.  Two words
with the same whole number variant level are considered almost equal
with a slight preference given to the entry with a lower number.  A
whole number variant level of 1 indicates a less preferred form of the
word.  A whole number variant level of 2 indicates any number of
things.  It could mean that it is from an archaic use of the word, or
a variant that is hardly ever used or for an extremely obscure meaning
of the word, or finally it could mean that the word looked like it
could possibly be a inflected form of the base word but I could not
find any evidence for them.  If two words have the same variant level
and explanation it means that both inflections were found and the
script was not sure which one to use.

Sometimes the inflected form to use depends on the meaning of the
word.  If this is the case the two entries will have different
explanations.  If the distinction can be made in a few words it is
given with underscores (_) replacing spaces.  Otherwise the two
entries will have different distinguishing numbers.

A < after a word means that there is a good change that this is an
inflected form of the word, a ~ after a word means that there is a
slight chance.  A ! after a word indicates that the word is likely an
inflections of a similar word (generally one ending in e) and not the
current word.  A ? after a word means that the word was not in the
word list but if it was it would be considered an inflected form of
the base word.

This verson is now almost as accurate as Alan Beale's 2of12id file
distributed with the "Unofficial Alternate 12 Dicts Package" for the
base words which have an entry in 2of12id.txt with a few notable
exceptions.  The most obvious one is the "person" entry.  Alan Beale
considers, based on what his sources have told him, that "persons" is
the proper plural for "person" and "people" is considered a variant.
I however disagree and decided to consider "people" the primary form
and "persons" as the sligtly less perfered variant based on my own
experence and http://www.quinion.com/words/usagenotes/un-person.htm
which says:

  The normal plural of person was persons ... However, there is
  evidence from Chaucer onwards that some writers chose to use people
  as a plural for person, not only in the generalised sense of 'an
  uncountable or indistinct mass of individuals' but also in specific
  countable cases. ... Though persons survives, it does so largely in
  formal or legal contexts ...From the evidence, it seems that the
  trend towards using people instead of persons is accelerating and
  that it may not be so long before persons vanishes from the language
  except in certain set phrases.

I considered making "persons" a variant (level 1), but I decided
against it as "persons" is for the most part perfectly acceptable and
probably considered the proper plural to use by some.

I also considered the -people ending the primary form for all words
ending in -person such as salesperson and the -persons entry the
slightly less preferred variant in spite of what 2of12id.txt said.

In some cases a variant of level 2 is listed in AGID where it is not
listed at all in 2of12id.  In general this means that the script came
up with the possibility and, in spite it not being listed in 2of12id,
it seams logical to me.

The final case occurs when a word has two or more -s inflections used
as both noun and verb forms, and these forms would have different
variant levels in 2of12id.  For example:
  ditto N: dittos, dittoes 1
  ditto V: dittoed | dittoing | dittos, dittoes 0.1
For purely technical reasons and because I do not feel that it matters
too much I have made the variant levels for the -s forms the same.  For
example the ditto entries became:
  ditto N: dittos, dittoes 0.1
  ditto V: dittoed | dittoing | dittos, dittoes 0.1
The choice of the variant levels I used is somewhat arbitrary but I in
general went with the lower level.

Fell free to send me corrections to correct any of these questionable
words.  I am mostly interested in the preferred form of the word when
the script was not able to decide or words marked with < or ~ that are
valid inflected forms of the words.

Also included in this version are the files "variant_0.lst",
"variant_1.lst", "variant_2.lst", and "variant.tab".  The files
"variant_#.lst" include all of the inflected forms at the given level
found in infl.txt which are not generally considered to be some other
common word.  The file variant.tab contains a cross reference of all
alternate forms of inflected form of words.  The file variant-wroot.tab
is like variant.tab except that it also included the root form of the 
word.

Words are in mixed case but all accents have been striped thus words
like café are instead cafe.

The file "variant" contains a list of alternate inflections.

The file "irregular" contains extra information where a noun or verb
has irregular inflected forms.

The file "dontuse" contains a list of words not to consider an
inflected form of a word if more than one inflected form of a word is
found.

The files "prefixes" and "suffixes" contains a list of common prefixes
and suffixes respectfully.  These files are used by the script to
produce inflected forms for words that end in a word in the
"irregular" file. If the beginning appears in the word list or the
prefixes file and the ending appears in the irregular file I also
consider <prefix>+<irregular inflections>.  If the prefix is 3 letters
or more OR appears in the prefixes file and the suffix is 4 letters or
more OR appears in the suffixes file I consider it the most likely
choice, otherwise I consider it as a possible candidate but not the
most likely choice.

The file "make-infl" is the actual Perl script used to create the
data base.

The file "find-var" is the Perl script used to create the variant
lists and cross reference file.

The file "make-all" was used to create the word list used by the script.

CHANGES:

From Revision 3a to 4 (January 2, 2003)

  Added variant-wroot.tab
  Update find-var script to also produce variant-wroot.tab.

From Revision 3 to 3a (April 04, 2001)

  Fixed a bug in the find-var script which caused some common
  words which are variants for one usage of a word but not 
  variants for any other common usage to improperly appear in
  the variant list.

From Revision 2 to 3 (January 28, 2001)

  Changed the format of infl.txt to something which is slightly harder
  to read but a lot less ambiguous and easier to parse.

  Update various files, including the actual script, so that the
  output that is almost as accurate of Alan Beale 2of12id.txt

  Eliminated Moby Words and ABLE from the word list used by the script
  to give more accurate results.

From Revision 1 to 2 (August 18, 2000)

  Classified variants as either almost equal, also used, or
  secondary.

  The / is now used to indicate equal variants.  "/?" is now used to
  mean what "/" used to be.

  Lots of additional rules added which greatly improved the results.

COPYRIGHT AND SOURCE:

The final product is under the following copyright, as well as any
copyrights mentioned below.

  Copyright 2000-2003 by Kevin Atkinson

  Permission to use, copy, modify, distribute and sell this database,
  the associated scripts, the output created form the scripts and its
  documentation for any purpose is hereby granted without fee,
  provided that the above copyright notice appears in all copies and
  that both that copyright notice and this permission notice appear in
  supporting documentation. Kevin Atkinson makes no representations
  about the suitability of this array for any purpose. It is provided
  "as is" without express or implied warranty.

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


The word list used is a combination of several word list:

1) The ENABLE2K word lists which is in the public domain:

     The ENABLE master word list, WORD.LST, is herewith formally
     released into the Public Domain. Anyone is free to use it or
     distribute it in any manner they see fit. No fee or registration
     is required for its use nor are "contributions" solicited (if you
     feel you absolutely must contribute something for your own peace
     of mind, the authors of the ENABLE list ask that you make a
     donation on their behalf to your favorite charity). This word
     list is our gift to the Scrabble community, as an alternate to
     "official" word lists. Game designers may feel free to
     incorporate the WORD.LST into their games. Please mention the
     source and credit us as originators of the list. Note that if
     you, as a game designer, use the WORD.LST in your product, you
     may still copyright and protect your product, but you may *not*
     legally copyright or in any way restrict redistribution of the
     WORD.LST portion of your product. This *may* under law restrict
     your rights to restrict your users' rights, but that is only
     fair.

2) All of the word lists except ABLE.LST in the ENABLE2K Supplemnt
   which consists of:

     2DICTS.LST  ALSO.LST   LETTERS.LST  OSPDADD.LST  UCACR.LST
     LCACR.LST  NOPOS.LST    PLURALS.LST  UPPER.LST

   All of these word lists are also in the public domain.

3) The list of signature words from the YAWL package which is in the
   public domain.

4) The UK Advanced Cryptics Dictionary which in under the following
   copyright:

     Copyright (c) J Ross Beresford 1993-1999. All Rights Reserved.

     The following restriction is placed on the use of this
     publication: if The UK Advanced Cryptics Dictionary is used
     in a software package or redistributed in any form, the
     copyright notice must be prominently displayed and the text
     of this document must be included verbatim.

     There are no other restrictions: I would like to see the
     list distributed as widely as possible.

5) Some extra words found in the Part-Of-Speech database that was not
   found in any of the above word lists.

6) Words found in the Jargon File Word List package, available at
   http://aspell.sourceforge.net/wl/, which is in the Public Domain.

7) Words in 2of12id.txt not in any of the word lists above.  2of12id is
   indirectly derived from all the above sources and most of the word
   lists from the Moby Words package:

     10196pla.ces 113809of.fic 21986na.mes 256772co.mpo 354984si.ngl
     3897male.nam 4160offi.cia 4946fema.len 6213acro.nym 74550com.mon
   
   The Moby Word package, like the Part-Of-Speech database is in the
   public domain.

8) And finally some extra words that I added myself.  These words can be
   found in the file "extra-words"

The "dontuse", "irregular", and "variant" file was created by me
(Kevin Atkinson) from numerous sources.

