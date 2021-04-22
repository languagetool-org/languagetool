Part Of Speech Database
July 23, 2000

Compiled by Kevin Atkinson <kevina@users.sourceforge.net>

The part-of-speech.txt file contains is a combination of 
"Moby (tm) Part-of-Speech II" and the WordNet database.

The latest version can be found at http://aspell.sourceforge.net/wl/.

The format of each entry is
<word><tab><POS tag(s)><unix newline>

Where the POS tag is one or more of the following:

N	Noun
P	Plural
h	Noun Phrase
V	Verb (usu participle)
t	Verb (transitive)
i	Verb (intransitive)
A	Adjective
v	Adverb
C	Conjunction
P	Preposition
!	Interjection
r	Pronoun
D	Definite Article
I	Indefinite Article
o	Nominative

The parts of speech before any '|' (if at all) come from the original
Moby database.  Anything after the '|' comes from the WordNet
database.  The part of speech tags from the original Moby database are
in priority order where the principle usage is listed first.  The ones
from the WordNet database are not.  Entries from the moby database
have had any accents removed, the "ae" character expanded, the
pound, yen, and peseta sign converted to a '$' and the CP437
character 0xBE replaced with a '~'.

                                CREATION

The C++ program used to create the database is also included for those
of you are interested.  The WordNet database will require a bit of
preprocessing with the following command:
  cat POS.lst | sed -n 's/\([^ ][^ ]*\) /\1/p' | tr '_' ' ' > noun.POS 
The paths will also need to be changed in order to get it to run.


                               COPYRIGHT

The Moby database was explicitly pleased in the public domain:

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


The WordNet database is under the following Copyright:

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

I assign no additional copyright to the combined database and the
software is explicitly being pleased in the public domain. However, I
would appreciate credit for my work.









