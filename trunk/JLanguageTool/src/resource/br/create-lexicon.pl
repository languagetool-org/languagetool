#!/usr/bin/perl -w
#
# Convert the Breton lexicon from apertium into a lexicon suitable for
# the LanguageTool grammar checker.
#
# LanguageTool POS tags for Breton are more or less similar to French tags to
# keep it simple.  This makes it easier to maintain grammar for # both French
# and Breton without too much to remember.
#
# POS tags for LanguageTool simplify POS tags present in apertium.
# Simpler POS tags make it easier to write regular expression in
# LanguageTool, but information can be lost in the conversion.
#
# How to use this script:
#
# 1) Download apertium Breton dictionary:
#    $ svn co https://apertium.svn.sourceforge.net/svnroot/apertium/trunk/apertium-br-fr
# 2) Install apertium tools:
#    $ sudo apt-get install lttoolbox
# 3) Run the script:
#    $ cd apertium-br-fr/
#    $ ./create-lexicon.pl
# 4) Use morfologik-stemming to create the LanguageTool dictionary:
#    See http://languagetool.wikidot.com/developing-a-tagger-dictionary
#    $ java -jar morfologik-stemming-nodict-1.4.0.jar tab2morph -i apertium-br-fr.br.dix-LT.txt -o output.txt
#    $ java -jar morfologik-stemming-nodict-1.4.0.jar fsa_build -i output.txt -o breton.dict
#
# Author: Dominique Pelle <dominique.pelle@gmail.com>

use strict;

my $dic_in  = 'apertium-br-fr.br.dix';
my $dic_out = "$dic_in-LT.txt";
my $dic_err = "$dic_in-LT.err";

open(LT_EXPAND, "lt-expand $dic_in |") or die "can't fork lt-expand: $!\n";
open(OUT, "> $dic_out") or die "can't open $dic_out: $!\n";
open(ERR, "> $dic_err") or die "can't open $dic_err: $!\n";

# Count how many words handled and unhandled.
my ($out_count, $err_count) = (0, 0);

while (<LT_EXPAND>) {
  chomp;
  if (/^([^: _~]+):(>:)?([^:<]+)([^#]*)(#.*)?/) {
    my ($word, $lemma, $tags) = ($1, $3, $4);

    $tags =~ s/(<adj><mf><sp>)\+.*/$1/;
    $tags =~ s/(<vblex><pri><p.><..>)\+.*/$1/;
    $lemma = $word if ($lemma eq 'direct');

    my $tag = '';

    if    ($tags eq '<det><def><sp>')     { $tag = "D e sp" }    # an, ar, al
    elsif ($tags eq '<det><ind><sp>')     { $tag = "D e sp" }    # un, ur, ul
    elsif ($tags eq '<det><ind><mf><sg>') { $tag = "D e s" }     # bep
    elsif ($tags eq '<det><pos><mf><sp>') { $tag = "D e sp" }    # hon
    elsif ($tags eq '<det><pos><m><sp>')  { $tag = "D m sp" }    # e
    elsif ($tags eq '<det><pos><f><sp>')  { $tag = "D f sp" }    # he

    # Verbal particles.
    elsif ($tags eq '<vpart>')            { $tag = "L a" }       # a
    elsif ($tags eq '<vpart><obj>')       { $tag = "L e" }       # e, ec’h, ez
    elsif ($tags eq '<vpart><ger>')       { $tag = "L o" }       # e, ec’h, ez
    elsif ($tags eq '<vpart><neg>')       { $tag = "L n" }       # na
    elsif ($tags eq '<vpart><opt>')       { $tag = "L r" }       # ra

    elsif ($tags eq '<ij>')               { $tag = "I" }         # ac’hanta

    # Adverbs.
    elsif ($tags eq '<cnjcoo>')           { $tag = "C coor" }    # ha, met
    elsif ($tags eq '<cnjadv>')           { $tag = "C adv" }     # eta, michañs
    elsif ($tags =~ /<cnjsub>.*/)         { $tag = "C sub" }     # mar, pa

    # Adverbs.
    elsif ($tags eq '<adv>')              { $tag = "A" }         # alies, alese, amañ
    elsif ($tags eq '<adv><neg>')         { $tag = "A neg" }     # ne, ned, n’
    elsif ($tags eq '<adv><itg>')         { $tag = "A itg" }     # perak, penaos
    elsif ($tags eq '<preadv>')           { $tag = "A pre" }     # gwall, ken, pegen

    # Adjectives.
    elsif ($tags eq '<adj><mf><sp>')      { $tag = "J" }     # brav, fur
    elsif ($tags eq '<adj><sint><comp>')  { $tag = "J cmp" } # bravoc’h
    elsif ($tags eq '<adj><sint><sup>')   { $tag = "J sup" } # bravvañ
    elsif ($tags eq '<adj><sint><excl>')  { $tag = "J exc" } # bravat
    elsif ($tags eq '<adj><itg><mf><sp>') { $tag = "J itg" } # peseurt, petore
    elsif ($tags eq '<adj><ind><mf><sp>') { $tag = "J ind" } # all, memes

    # Pronouns subject.
    elsif ($tags eq '<prn><subj><p1><mf><sg>') { $tag = "R suj e s 1"; } # me
    elsif ($tags eq '<prn><subj><p2><mf><sg>') { $tag = "R suj e s 2"; } # te
    elsif ($tags eq '<prn><subj><p3><m><sg>')  { $tag = "R suj m s 3"; } # eñ
    elsif ($tags eq '<prn><subj><p3><f><sg>')  { $tag = "R suj f s 3"; } # hi
    elsif ($tags eq '<prn><subj><p1><mf><pl>') { $tag = "R suj e p 1"; } # ni
    elsif ($tags eq '<prn><subj><p2><mf><pl>') { $tag = "R suj e p 2"; } # c’hwi
    elsif ($tags eq '<prn><subj><p3><mf><pl>') { $tag = "R suj e p 3"; } # int
    elsif ($tags eq '<prn><subj><p3><mf><pl>') { $tag = "R suj e p 3"; } # int
    elsif ($tags eq '<prn><ref><p1><mf><sg>')  { $tag = "R ref e s 1"; } # ma-unan
    elsif ($tags eq '<prn><ref><p2><mf><sg>')  { $tag = "R ref e s 2"; } # da-unan
    elsif ($tags eq '<prn><ref><p3><f><sg>')   { $tag = "R ref f s 3"; } # e-unan
    elsif ($tags eq '<prn><ref><p3><m><sg>')   { $tag = "R ref m s 3"; } # he-unan
    elsif ($tags eq '<prn><ref><p1><mf><pl>')  { $tag = "R ref e p 1"; } # hon-unan
    elsif ($tags eq '<prn><ref><p2><mf><pl>')  { $tag = "R ref e p 2"; } # hoc’h-unan
    elsif ($tags eq '<prn><ref><p3><mf><pl>')  { $tag = "R ref e p 3"; } # o-unan
    elsif ($tags eq '<prn><itg><mf><sp>')      { $tag = "R itg e sp"; }  # petra, piv
    elsif ($tags eq '<prn><itg><mf><pl>')      { $tag = "R itg e p"; }   # pere
    elsif ($tags eq '<prn><dem><m><sg>')       { $tag = "R dem m s"; }   # hemañ
    elsif ($tags eq '<prn><dem><f><sg>')       { $tag = "R dem f s"; }   # hennezh
    elsif ($tags eq '<prn><dem><mf><sg>')      { $tag = "R dem e s"; }   # se
    elsif ($tags eq '<prn><ind><mf><sg>')      { $tag = "R ind mf s"; }  # hini
    elsif ($tags eq '<prn><ind><mf><pl>')      { $tag = "R ind mf p"; }  # re
    elsif ($tags eq '<prn><def><mf><sg>')      { $tag = "R def e s"; }   # henn
    elsif ($tags eq '<prn><def><m><sg>')       { $tag = "R def m s"; }   # egile
    elsif ($tags eq '<prn><def><f><sg>')       { $tag = "R def f s"; }   # eben

    # Pronouns object.
    elsif ($tags eq '<prn><obj><p1><mf><sg>') { $tag = "R e s 1 obj"; } # va
    elsif ($tags eq '<prn><obj><p1><mf><pl>') { $tag = "R e p 1 obj"; } # hon, hor, hol
    elsif ($tags eq '<prn><obj><p2><mf><sg>') { $tag = "R e s 2 obj"; } # az
    elsif ($tags eq '<prn><obj><p2><mf><pl>') { $tag = "R e p 2 obj"; } # ho
    elsif ($tags eq '<prn><obj><p3><m><sg>')  { $tag = "R m s 1 obj"; } # e
    elsif ($tags eq '<prn><obj><p3><f><sg>')  { $tag = "R f s 1 obj"; } # he
    elsif ($tags eq '<prn><obj><p3><mf><pl>') { $tag = "R e p 3 obj"; } # o

    # Numbers.
    elsif ($tags eq '<num><mf><sg>') { $tag = "K e s"; }
    elsif ($tags eq '<num><m><pl>')  { $tag = "K m p"; }
    elsif ($tags eq '<num><f><pl>')  { $tag = "K f p"; }
    elsif ($tags eq '<num><mf><pl>') { $tag = "K e p"; }

    # Ordinal numbers.
    elsif ($tags eq '<num><ord><mf><sp>')   { $tag = "K e sp o"; } # eil
    elsif ($tags eq '<num><ord><mf><sg>')   { $tag = "K e s o"; }  # trede
    elsif ($tags eq '<num><ord><mf><pl>')   { $tag = "K e p o"; }
    elsif ($tags eq '<num><ord><m><pl>')    { $tag = "K m p o"; }
    elsif ($tags eq '<num><ord><m><sp>')    { $tag = "K m sp o"; } # kentañ
    elsif ($tags eq '<num><ord><f><pl>')    { $tag = "K f p o"; }

    # Indirect preposition.
    elsif ($tags eq '<pr>')                                { $tag = "P" }        # da
    elsif ($tags eq '<pr>+indirect<prn><obj><p1><mf><sg>') { $tag = "P e 1 s" }  # din
    elsif ($tags eq '<pr>+indirect<prn><obj><p2><mf><sg>') { $tag = "P e 2 s" }  # dit
    elsif ($tags eq '<pr>+indirect<prn><obj><p3><m><sg>')  { $tag = "P m 3 s" }  # dezhañ
    elsif ($tags eq '<pr>+indirect<prn><obj><p3><f><sg>')  { $tag = "P f 3 s" }  # dezhi
    elsif ($tags eq '<pr>+indirect<prn><obj><p1><mf><pl>') { $tag = "P e 1 p" }  # dimp
    elsif ($tags eq '<pr>+indirect<prn><obj><p2><mf><pl>') { $tag = "P e 2 p" }  # deoc’h
    elsif ($tags eq '<pr>+indirect<prn><obj><p3><mf><pl>') { $tag = "P e 3 p" }  # dezho
    elsif ($tags =~ /<pr>.*/)                              { $tag = "P" }        # er, ez

    # Nouns.
    elsif ($tags eq '<n><m><sg>')  { $tag = "N m s" }
    elsif ($tags eq '<n><m><pl>')  { $tag = "N m p" }
    elsif ($tags eq '<n><f><sg>')  { $tag = "N f s" }
    elsif ($tags eq '<n><f><pl>')  { $tag = "N f p" }
    elsif ($tags eq '<n><mf><sg>') { $tag = "N e s" }
    elsif ($tags eq '<n><mf><pl>') { $tag = "N e p" }
    elsif ($tags eq '<n><m><sp>')  { $tag = "N m sp" }

    # Proper nouns.
    elsif ($tags eq '<np><top><sg>')     { $tag = "Z e s" }  # Aostria
    elsif ($tags eq '<np><top><pl>')     { $tag = "Z e p" }  # Azorez
    elsif ($tags eq '<np><top><m><sg>')  { $tag = "Z m s" }  # Kreiz-Breizh
    elsif ($tags eq '<np><cog><mf><sg>') { $tag = "Z e s" }
    elsif ($tags eq '<np><ant><m><sg>')  { $tag = "Z m s" }  # Alan
    elsif ($tags eq '<np><ant><f><sg>')  { $tag = "Z f s" }  # Youna
    elsif ($tags eq '<np><al><mf><sg>')  { $tag = "Z e s" }  # Leclerc
    elsif ($tags eq '<np><al><m><sg>')   { $tag = "Z m s" }  # Ofis
    elsif ($tags eq '<np><al><f><sg>')   { $tag = "Z f s" }  # Bibl

    elsif ($tags eq '<n><acr><m><sg>')   { $tag = "S m s" }  # TER

    # Verbs.
    elsif ($tags eq '<vblex><inf>')             { $tag = "V inf" } # komz
    elsif ($tags eq '<vblex><pp>')              { $tag = "V ppa" } # komzet

    # Present
    elsif ($tags eq '<vblex><pri><p1><sg>')     { $tag = "V pres 1 s" }       # komzan
    elsif ($tags eq '<vblex><pri><p2><sg>')     { $tag = "V pres 2 s" }       # komzez
    elsif ($tags eq '<vblex><pri><p3><sg>')     { $tag = "V pres 3 s" }       # komz
    elsif ($tags eq '<vblex><pri><p1><pl>')     { $tag = "V pres 1 p" }       # komzomp
    elsif ($tags eq '<vblex><pri><p2><pl>')     { $tag = "V pres 2 p" }       # komzit
    elsif ($tags eq '<vblex><pri><p3><pl>')     { $tag = "V pres 3 p" }       # komzont
    elsif ($tags eq '<vblex><pri><impers><sp>') { $tag = "V pres impers sp" } # komzer
    elsif ($tags eq '<vblex><pri><impers><pl>') { $tag = "V pres impers p" }  # oad

    # Imperfect.
    elsif ($tags eq '<vblex><pii><p1><sg>')     { $tag = "V impa 1 s" }        # komzen
    elsif ($tags eq '<vblex><pii><p2><sg>')     { $tag = "V impa 2 s" }        # komzes
    elsif ($tags eq '<vblex><pii><p3><sg>')     { $tag = "V impa 3 s" }        # komze
    elsif ($tags eq '<vblex><pii><p1><pl>')     { $tag = "V impa 1 p" }        # komzemp
    elsif ($tags eq '<vblex><pii><p2><pl>')     { $tag = "V impa 2 p" }        # komzec’h
    elsif ($tags eq '<vblex><pii><p3><pl>')     { $tag = "V impa 3 p" }        # komzent
    elsif ($tags eq '<vblex><pii><impers><sp>') { $tag = "V impa impers sp" }  # komzed
    elsif ($tags eq '<vblex><pii><impers><pl>') { $tag = "V impa impers p" }   # oad
  
    # Past definite.
    elsif ($tags eq '<vblex><past><p1><sg>')    { $tag = "V pass 1 s" }        # komzis
    elsif ($tags eq '<vblex><past><p2><sg>')    { $tag = "V pass 2 s" }        # komzjout
    elsif ($tags eq '<vblex><past><p3><sg>')    { $tag = "V pass 3 s" }        # komzas
    elsif ($tags eq '<vblex><past><p1><pl>')    { $tag = "V pass 1 p" }        # komzjomp
    elsif ($tags eq '<vblex><past><p2><pl>')    { $tag = "V pass 2 p" }        # komzjoc’h
    elsif ($tags eq '<vblex><past><p3><pl>')    { $tag = "V pass 3 p" }        # komzjont
    elsif ($tags eq '<vblex><past><impers><sp>'){ $tag = "V pass impers sp" }  # komzod
    elsif ($tags eq '<vblex><past><impers><pl>'){ $tag = "V pass impers pl" }  # poed

    # Future.
    elsif ($tags eq '<vblex><fti><p1><sg>')     { $tag = "V futu 1 s" }        # komzin
    elsif ($tags eq '<vblex><fti><p2><sg>')     { $tag = "V futu 2 s" }        # komzi
    elsif ($tags eq '<vblex><fti><p3><sg>')     { $tag = "V futu 3 s" }        # komzo
    elsif ($tags eq '<vblex><fti><p1><pl>')     { $tag = "V futu 1 p" }        # komzimp
    elsif ($tags eq '<vblex><fti><p2><pl>')     { $tag = "V futu 2 p" }        # komzot
    elsif ($tags eq '<vblex><fti><p3><pl>')     { $tag = "V futu 3 p" }        # komzint
    elsif ($tags eq '<vblex><fti><impers><sp>') { $tag = "V futu impers sp" }  # komzor
    elsif ($tags eq '<vblex><fti><impers><pl>') { $tag = "V futu impers p" }   # pior

    # Conditional.
    elsif ($tags eq '<vblex><cni><p1><sg>')     { $tag = "V conf 1 s" }        # komzfen
    elsif ($tags eq '<vblex><cni><p2><sg>')     { $tag = "V conf 2 s" }        # komzfes
    elsif ($tags eq '<vblex><cni><p3><sg>')     { $tag = "V conf 3 s" }        # komzfe
    elsif ($tags eq '<vblex><cni><p1><pl>')     { $tag = "V conf 1 p" }        # komzfemp
    elsif ($tags eq '<vblex><cni><p2><pl>')     { $tag = "V conf 2 p" }        # komzfec’h
    elsif ($tags eq '<vblex><cni><p3><pl>')     { $tag = "V conf 3 p" }        # komzfent
    elsif ($tags eq '<vblex><cni><impers><sp>') { $tag = "V conf impers sp" }  # komzfed
    elsif ($tags eq '<vblex><cni><impers><pl>') { $tag = "V conf impers p" }

    # Conditional.
    elsif ($tags eq '<vblex><cip><p1><sg>')     { $tag = "V conj 1 s" }       # komzjen
    elsif ($tags eq '<vblex><cip><p2><sg>')     { $tag = "V conj 2 s" }       # komzjes
    elsif ($tags eq '<vblex><cip><p3><sg>')     { $tag = "V conj 3 s" }       # komzje
    elsif ($tags eq '<vblex><cip><p1><pl>')     { $tag = "V conj 1 p" }       # komzjemp
    elsif ($tags eq '<vblex><cip><p2><pl>')     { $tag = "V conj 2 p" }       # komzjec’h
    elsif ($tags eq '<vblex><cip><p3><pl>')     { $tag = "V conj 3 p" }       # komzjent
    elsif ($tags eq '<vblex><cip><impers><sp>') { $tag = "V conj impers sp" } # komzjed
    elsif ($tags eq '<vblex><cip><impers><pl>') { $tag = "V conj impers p" }  # komzjed

    # Imperative.
    elsif ($tags eq '<vblex><imp><p2><sg>')     { $tag = "V impe 2 s" }     # komz
    elsif ($tags eq '<vblex><imp><p3><sg>')     { $tag = "V impe 3 s" }     # komzet
    elsif ($tags eq '<vblex><imp><p1><pl>')     { $tag = "V impe 1 p" }     # komzomp
    elsif ($tags eq '<vblex><imp><p2><pl>')     { $tag = "V impe 2 p" }     # komzit
    elsif ($tags eq '<vblex><imp><p3><pl>')     { $tag = "V impe 3 p" }     # komzent

    # Present, habitual.
    elsif ($tags eq '<vblex><prh><p1><sg>')     { $tag = "V preh 1 s" }        # pezan
    elsif ($tags eq '<vblex><prh><p2><sg>')     { $tag = "V preh 2 s" }        # pezez
    elsif ($tags eq '<vblex><prh><p3><sg>')     { $tag = "V preh 3 s" }        # pez
    elsif ($tags eq '<vblex><prh><p1><pl>')     { $tag = "V preh 1 p" }        # pezomp
    elsif ($tags eq '<vblex><prh><p2><pl>')     { $tag = "V preh 2 p" }        # pezit
    elsif ($tags eq '<vblex><prh><p3><pl>')     { $tag = "V preh 3 p" }        # pezont
    elsif ($tags eq '<vblex><prh><impers><pl>') { $tag = "V preh impers p" }   # pezer

    # Imperfect, habitual.
    elsif ($tags eq '<vblex><pih><p1><sg>')     { $tag = "V imph 1 s" }     # bezen
    elsif ($tags eq '<vblex><pih><p2><sg>')     { $tag = "V imph 2 s" }     # pezen
    elsif ($tags eq '<vblex><pih><p3><sg>')     { $tag = "V imph 3 s" }     # peze
    elsif ($tags eq '<vblex><pih><p1><pl>')     { $tag = "V imph 1 p" }     # pezemp
    elsif ($tags eq '<vblex><pih><p2><pl>')     { $tag = "V imph 2 p" }     # pezec’h
    elsif ($tags eq '<vblex><pih><p3><pl>')     { $tag = "V imph 3 p" }     # pezent
    elsif ($tags eq '<vblex><pih><impers><pl>') { $tag = "V imph impers" }  # pezed

    # present, locative.
    elsif ($tags eq '<vbloc><pri><p1><sg>')     { $tag = "V prel 1 s" }     # emaoñ
    elsif ($tags eq '<vbloc><pri><p2><sg>')     { $tag = "V prel 2 s" }     # emaout
    elsif ($tags eq '<vbloc><pri><p3><sg>')     { $tag = "V prel 3 s" }     # emañ
    elsif ($tags eq '<vbloc><pri><p1><pl>')     { $tag = "V prel 1 p" }     # emaomp
    elsif ($tags eq '<vbloc><pri><p2><pl>')     { $tag = "V prel 2 p" }     # emaoc’h
    elsif ($tags eq '<vbloc><pri><p3><pl>')     { $tag = "V prel 3 p" }     # emaint
    elsif ($tags eq '<vbloc><pri><impers><sp>') { $tag = "V prel impers" }  # emeur

    # Imperfect, locative.
    elsif ($tags eq '<vbloc><pii><p1><sg>')     { $tag = "V impl 1 s" }     # edon
    elsif ($tags eq '<vbloc><pii><p2><sg>')     { $tag = "V impl 2 s" }     # edos
    elsif ($tags eq '<vbloc><pii><p3><sg>')     { $tag = "V impl 3 s" }     # edo
    elsif ($tags eq '<vbloc><pii><p1><pl>')     { $tag = "V impl 1 p" }     # edomp
    elsif ($tags eq '<vbloc><pii><p2><pl>')     { $tag = "V impl 2 p" }     # edoc’h
    elsif ($tags eq '<vbloc><pii><p3><pl>')     { $tag = "V impl 3 p" }     # edont
    elsif ($tags eq '<vbloc><pii><impers><sp>') { $tag = "V impl impers" }  # emod


    if ($tag) {
      print OUT "$word\t$lemma\t$tag\n";
      ++$out_count;
    } else {
      print ERR "$_ -> word=$word lemma=$lemma tags=$tags\n";
      ++$err_count;
    }
  }
}
print "handled [$out_count] words, unhandled [$err_count] words\n";
