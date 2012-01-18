#!/usr/bin/perl -w
#
# Convert the Breton lexicon from Apertium into a lexicon suitable for
# the LanguageTool grammar checker.
#
# LanguageTool POS tags for Breton are more or less similar to French tags to
# keep it simple.  This makes it easier to maintain grammar for both French
# and Breton without too much to remember.
#
# POS tags for LanguageTool simplify POS tags present in Apertium.
# Simpler POS tags make it easier to write regular expression in
# LanguageTool, but information can be lost in the conversion.
#
# How to use this script:
#
# 1) Download the Apertium Breton dictionary:
#    $ svn co https://apertium.svn.sourceforge.net/svnroot/apertium/trunk/apertium-br-fr
# 2) Install Apertium tools:
#    $ sudo apt-get install lttoolbox
# 3) Run the script:
#    $ cd apertium-br-fr/
#    $ ./create-lexicon.pl
#
# Author: Dominique Pelle <dominique.pelle@gmail.com>

use strict;

my $dic_in  = 'apertium-br-fr.br.dix';
my $dic_out = "$dic_in-LT.txt";
my $dic_err = "$dic_in-LT.err";

# List of plural masculine nouns of persons for which it matters to know
# whether they are persons or not for the mutation after article "ar".
# Those are unfortunately not tagged in the Apertium dictionary.
# So we enhance tagging here to be able to detect some incorrect mutations
# after the article ar/an/al.
#
# The tag "N m p t" (N masculine plural tud) is used not only for mutation
# after ar/an/al (such as *Ar Kelted* -> "Ar Gelted") but also for
# mutations of adjective after noun such as:
# *Ar studierien pinvidik* -> "Ar studierien binvidik"
#
# This list is far from being complete. The more words the more
# mutation errors can be detected. But missing words should not
# cause false positives.
# Case matters!
my @anv_lies_tud = (
  # plural              softening         reinforcing     spirant
  "Afrikaned",
  "Alamaned",
  "alouberien",
  "ambrougerien",
  "Amerikaned",
  "amezeien",
  "amprevanoniourien",
  "annezerien",
  "aozerien",
  "apotikerien",
  "arboellerien",
  "archerien",
  "ardivikerien",
  "arvesterien",
  "arzourien",
  "aterserien",
  "atletourien",
  "Bretoned",           "Vretoned",       "Pretoned",
  "Brezhoned",          "Vrezhoned",      "Prezhoned",
  "Gallaoued",          "C’hallaoued",    "Kallaoued",
  "Kabiled",            "Gabiled",        "c’habiled",
  "Karnuted",           "Garnuted",       "C’harnuted",
  "Kelted",             "Gelted",         "C’helted",
  "Kuriosolited",       "Guriosolited",   "C’huriosolited",
  "Muzulmaned",         "Vuzulmaned",
  "Palestinianed",      "Balestinianed",                   "Falestinianed",
  "Parizianed",         "Barizianed",                      "Farizianed",
  "bachelourien",       "vachelourien",   "pachelourien",
  "bac’herien",         "vac’herien",     "pac’herien",
  "bac’herion",         "vac’herion",     "pac’herion",
  "bagsavourien",       "vagsavourien",   "pagsavourien",
  "baleerien",          "valeerien",      "paleerien",
  "bamerien",           "vamerien",       "pamerien",
  "bamerion",           "vamerion",       "pamerion",
  "baraerien",          "varaerien",      "paraerien",
  "baraerion",          "varaerion",      "paraerion",
  "barnerien",          "varnerien",      "parnerien",
  "barnerion",          "varnerion",      "parnerion",
  "barzhed",            "varzhed",        "parzhed",
  "beajourien",         "veajourien",     "peajourien",
  "bedoniourien",       "vedoniourien",   "pedoniourien",
  "begennelourien",     "vegennelourien", "pegennelourien",
  "beleien",            "veleien",        "peleien",
  "benerien",           "venerien",       "penerien",
  "bevezerien",         "vevezerien",     "pevezerien",
  "bevoniourien",       "vevonourien",    "pevonourien",
  "bigrierien",         "vigrierien",     "pigrierien",
  "biniaouerien",       "viniaouerien",   "piniaouerien",
  "biolinourien",       "violinourien",   "piolinourien",
  "bleinerien",         "vleinerien",     "pleinerien",
  "bleinerion",         "vleinerion",     "pleinerion",
  "bombarderien",       "vombarderien",   "pombarderien",
  "bonelourien",        "vonelourien",    "ponelourien",
  "bouloñjerien",       "vouloñjerien",   "pouloñjerien",
  "braventiourien",     "vraventiourien", "praventiourien",
  "bredklañvourien",    "vredklañvourien", "predklañvourien",
  "bredoniourien",      "vredoniourien",  "predoniourien",
  "bresourien",         "vresourien",     "presourien",
  "breudeur",           "vreudeur",       "preudeur",
  "breutaerien",        "vreutaerien",    "preutaerien",
  "brezelourien",       "vrezelourien",   "prezelourien",
  "brezhonegerien",     "vrezhonegerien", "prezhonegerien",
  "brezhonegerien",     "vrezhonegerien", "prezhonegerien",
  "brigadennourien",    "vrigadennourien", "prigadennourien",
  "brizhkeltiegourien", "vrizhkeltiegourien", "prizhkeltiegourien",
  "brizhkredennourien", "vrizhkredennourien", "prizhkredennourien",
  "broadelourien",      "vroadelourien",  "proadelourien",
  "brogarourien",       "vrogarourien",   "progarourien",
  "brozennourien",      "vrozennourien",  "prozennourien",
  "brudourien",         "vrudourien",     "prudourien",
  "bugale",             "vugale",         "pugale",
  "bugulien",           "vugulien",       "pugulien",
  "buhezegezhourien",   "vuhezegezhourien", "puhezegezhourien",
  "butunerien",         "vutunerien",     "putunerien",
  "butunerion",         "vutunerion",     "putunerion",
  "dañser`ien",                            "tañserien",
  "eilrenerien",
  "embannerien",
  "emgannerien",
  "emrenerien",
  "emsaverien",
  "emstriverien",
  "emzivaded",
  "enbroidi",
  "eneberien",
  "enebourien",
  "engraverien",
  "enklaskerien",
  "ensellerien",
  "eontred-kozh",
  "eosterien",
  "erbederien",
  "ergerzherien",
  "eveshaerien",
  "evezhierien",
  "evnoniourien",
  "falc’herien",
  "falserien",
  "farderien",
  "farserien",
  "feizidi",
  "fistoulerien",
  "fizikourien",
  "flatrerien",
  "fougaserien",
  "Frañsizien",
  "furcherien",
  "gallegerien",        "c’hallegerien",  "kallegerien",
  "gallegerion",        "c’hallegerion",  "kallegerion",
  "gaouidi",            "c’haouidi",      "kaouidi",
  "genaoueien",         "c’henaoueien",   "kenaoueien",
  "gouerien",           "c’houerien",     "kouerien",
  "gouizieien",         "c’houizieien",   "kouizieien",
  "gourdonerien",       "c’hourdonerien", "kourdonerien",
  "goved",              "c’hoved",        "koved",
  "gwazed",             "wazed",          "kwazed",
  "gwenanerien",        "wenanerien",     "kwenanerien",
  "gwerzherien",        "werzherien",     "kwerzherien",
  "gwiaderien",         "wiaderien",      "kwiaderien",
  "gwiaderion",         "wiaderion",      "kwiaderion",
  "gwiniegourien",      "winiegourien",   "kwiniegourien",
  "haderien",
  "hailhoned",
  "hanterourien",
  "hañvourien",
  "hegazerien",
  "hemolc’herien",
  "hendraourien",
  "henoniourien",
  "ijinourien",
  "imbrouderien",
  "impalaerien",
  "implijidi",
  "implijerien",
  "irrinnerien",
  "ispiserien",
  "isrenerien",
  "istorourien",
  "Italianed",
  "jedoniourien",
  "jiboesaourien",
  "jubennourien",
  "kabitened",          "gabitened",      "c’habitened",
  "kamaladed",          "gamaladed",      "c’hamaladed",
  "kamaraded",          "gamaraded",      "c’hamaraded",
  "kanerien",           "ganerien",       "c’hanerien",
  "kannerien",          "gannerien",      "c’hannerien",
  "kantennerien",       "gantennerien",   "c’hantennerien",
  "kantreerien",        "gantreerien",    "c’hantreerien",
  "kariaded",           "gariaded",       "c’hariaded",
  "karngerzherien",     "garngerzherien", "c’harngerzherien",
  "karrellerien",       "garrellerien",   "c’harrellerien",
  "karrerien",          "garrerien",      "c’harrerien",
  "kazetennerien",      "gazetennerien",  "c’hazetennerien",
  "kañfarded",          "gañfarded",      "c’hañfarded",
  "keginerien",         "geginerien",     "c’heginerien",
  "kelaouennerien",     "gelaouennerien", "c’helaouennerien",
  "kelaouerien",        "gelaouerien",    "c’helaouerien",
  "kelennerien",        "gelennerien",    "c’helennerien",
  "kemenerien",         "gemenerien",     "c’hemenerien",
  "kenaozerien",        "genaozerien",    "c’henaozerien",
  "kenderc’herien",     "genderc’herien", "c’henderc’herien",
  "kendirvi",           "gendirvi",       "c’hendirvi",
  "kenlabourerien",     "genlabourerien", "c’henlabourerien",
  "kenoberourien",      "genoberourien",  "c’henoberourien",
  "kenseurted",         "genseurted",     "c’henseurted",
  "kenskriverien",      "genskriverien",  "c’henskriverien",
  "kenstriverien",      "genstriverien",  "c’henstriverien",
  "kenwerzherien",      "genwerzherien",  "c’henwerzherien",
  "kereon",             "gereon",         "c’hereon",
  "kerzherien",         "gerzherien",     "c’herzherien",
  "kevellerien",        "gevellerien",    "c’hevellerien",
  "kevezerien",         "gevezerien",     "c’hevezerien",
  "kigerien",           "gigerien",       "c’higerien",
  "kinnigerien",        "ginnigerien",    "c’hinnigerien",
  "kizellerien",        "gizellerien",    "c’hizellerien",
  "klaskerien",         "glaskerien",     "c’hlaskerien",
  "klañvdiourien",      "glañvdiourien",  "c’hlañvdiourien",
  "kouronkerien",       "gouronkerien",   "c’houronkerien",
  "kourserien",         "gourserien",     "c’hourserien",
  "kouvierien",         "gouverien",      "c’houverien",
  "koñversanted",       "goñversanted",   "c’hoñversanted",
  "krakaotrouien",      "grakaotrouien",  "c’hrakaotrouien",
  "krampouezherien",    "grampouezherien", "c’hampouezherien",
  "krennarded",         "grennarded",     "c’hrennarded",
  "kristenien",         "gristenien",     "c’hristenien",
  "kristenion",         "gristenion",     "c’hristenion",
  "krouadurien",        "grouadurien",    "c’hrouadurien",
  "labourerien",
  "labourerien-douar",
  "laeron",
  "lagadourien",
  "lamponed",
  "lavarerien",
  "lazherien",
  "leaned",
  "lemmerien",
  "lennerien",
  "levraouaerien",
  "levraouegerien",
  "levrierien",
  "liorzherien",
  "liorzhourien",
  "liperien",
  "lipouzerien",
  "loenoniourien",
  "lonkerien",
  "louzaouerien",
  "louzawourien",
  "lubanerien",
  "luc’hskeudennerien",
  "luc’hvannerien",
  "lunederien",
  "luskerien",
  "madoberourien",      "vadoberourien",
  "maered",             "vaered",
  "maesaerien",         "vaesarien",
  "magerien",           "vagerien",
  "mailhed",            "vailhed",
  "maltouterien",       "valtouterien",
  "maodierned",         "vaodierned",
  "marc’hadourien",     "varc’hadourien",
  "marc’heien",         "varc’heien",
  "marc’hergerien",     "varc’hergerien",
  "marc’homerien",      "varc’homerien",
  "margodennerien",     "vargodennerien",
  "markizien",          "varkizien",
  "martoloded",         "vartoloded",
  "marvailherien",      "varvailherien",
  "mañsonerien",        "vañsonerien",
  "mederien",           "vederien",
  "medisined",          "vedisined",
  "mekanikerien",       "vekanikerien",
  "mendemerien",        "vendemerien",
  "menec’h",            "venec’h",
  "merc’hetaerien",     "verc’hetaerien",
  "mererien",           "vererien",
  "merourien",          "verourien",
  "merzherien",         "verzherien",
  "meveled",            "veveled",
  "mevelien",           "vevelien",
  "mezeien",            "vezeien",
  "mezvierien",         "vezvierien",
  "mibien",             "vibien",
  "mibien-gaer",        "vibien-gaer",
  "mibien-vihan",       "vibien-vihan",
  "micherourien",       "vicherourien",
  "mic’hieien",         "vic’hieien",
  "mignoned",           "vignoned",
  "milinerien",         "vilinerien",
  "milvezeien",         "vilvezeien",
  "ministred",          "vinistred",
  "misionerien",        "visionerien",
  "mistri",             "vistri",
  "mistri-skol",        "vistri-skol",
  "mistri-vicherour",   "vistri-vicherour",
  "monitorien",         "vonitourien",
  "morlaeron",          "vorlaeron",
  "moruteaerien",       "voruteaerien",
  "mouezhierien",       "vouezhierien",
  "moullerien",         "voullerien",
  "mudien",             "vudien",
  "muntrerien",         "vutrerien",
  "munuzerien",         "vunuzerien",
  "neuñverien",
  "nized",
  "nizien",
  "notered",
  "noterien",
  "oadourien",
  "obererien",
  "oberourien",
  "orfebourien",
  "paeerien",           "baeerien",                        "faeerien",
  "paeroned",           "baeroned",                        "faeroned",
  "palerien",           "balerien",                        "falerien",
  "paluderien",         "baluderien",                      "faluderien",
  "paotred",            "baotred",                         "faotred",
  "paramantourien",     "baramantourien",                  "faramantourien",
  "pardonerien",        "bardonerien",                     "fardonerien",
  "pec’herien",         "bec’herien",                      "fec’herien",
  "pellskriverien",     "bellskriverien",                  "fellskriverien",
  "peorien",            "beorien",                         "feorien",
  "perc’henned",        "berc’henned",                     "ferc’henned",
  "perc’herined",       "berc’herined",                    "ferc’herined",
  "personed",           "bersoned",                        "fersoned",
  "perukennerien",      "berukennerien",                   "ferukennerien",
  "perukennerion",      "berukennerion",                   "ferukennerion",
  "peskedoniourien",    "beskedoniourien",                 "feskedoniourien",
  "pesketaerien",       "besketaerien",                    "fesketaerien",
  "pianoourien",        "bianoourien",                     "fianoourien",
  "piaouerien",         "biaouerien",                      "fiaouerien",
  "pibien",             "bibien",                          "fibien",
  "pilhaouaerien",      "bilhaouaerien",                   "filhaouaerien",
  "poliserien",         "boliserien",                      "foliserien",
  "politikerien",       "bolitikerien",                    "folitikerien",
  "prederourien",       "brederourien",                    "frederourien",
  "prefeded",           "brefeded",                        "frefeded",
  "prezidanted",        "presidanted",                     "frezidanted",
  "prizonidi",          "brizonidi",                       "frizonidi",
  "priñsed",            "briñsed",                         "friñsed",
  "rakprenerien",
  "randonerien",
  "ratouzed",
  "rederien",
  "rederien-vro",
  "rederien-vor",
  "renerien",
  "riblerien",
  "riboderien",
  "riboderien",
  "Romaned",
  "Rusianed",
  "ruzarded",
  "ruzerien",
  "salverien",
  "saverien",
  "saveteerien",
  "savourien",
  "selaouerien",
  "sellerien",
  "sonerien",
  "sonaozourien",
  "soroc’horien",
  "soudarded",
  "splujerien",
  "sponterien",
  "sportourien",
  "steredourien",
  "steredoniourien",
  "stranerien",
  "strobinellerien",
  "studierien",
  "sturierien",
  "tagerien",           "dagerien",                        "zagerien",
  "tailhanterien",      "dailhanterien",                   "zailhanterien",
  "talabarderien",      "dalabarderien",                   "zalabarderien",
  "teknikourien",       "deknikourien",                    "zeknikourien",
  "telennourien",       "delennourien",                    "zelennourien",
  "tennerien",          "dennerien",                       "zennerien",
  "teñzorierien",       "deñzorierien",                    "zeñzorierien",
  "tinellerien",        "dinellerien",                     "zinellerien",
  "titourerien",        "ditourerien",                     "zitourerien",
  "toerien",            "doerien",                         "zoerien",
  "togerien",           "dogerien",                        "zogerien",
  "tommerien",          "dommerien",                       "zommerien",
  "tontoned",           "dontoned",                        "zontoned",
  "torfedourien",       "dorfedourien",                    "zorfedourien",
  "touellerien",        "douellerien",                     "zouellerien",
  "toullerien",         "doullerien",                      "zoullerien",
  "toullerien-buñsoù",  "doullerien-buñsoù",               "zoullerien-buñsoù",
  "toullerien-vezioù",  "doullerien-vezioù",               "zoullerien-vezioù",
  "touristed",          "douristed",                       "zouristed",
  "tredanerien",        "dredanerien",                     "zredanerien",
  "tredanerion",        "dredanerion",                     "zredanerion",
  "tredeeged",          "dredeeged",                       "zredeeged",
  "tredeoged",          "dredeoged",                       "zredeoged",
  "treitourien",        "dreitourien",                     "zreitourien",
  "treizherien",        "dreizherien",                     "zreizherien",
  "tresourien",         "dresourien",                      "zresourien",
  "trevadennerien",     "drevadennerien",                  "zrevadennerien",
  "troadeien",          "droadeien",                       "zroadeien",
  "troerien",           "droerien",                        "zroerien",
  "troerien-douar",     "droerien-douar",                  "zroerien-douar",
  "trubarded",          "drubarded",                       "zrubarded",
  "truilhenned",        "druilhenned",                     "zruilhenned",
  "tud",                "dud",                             "zud",
  "tudonourien",        "dudonourien",                     "zudonourien",
  "uzurerien",
  "Vikinged",
  "yezherien",
  "yunerien",
);
my %anv_lies_tud = map { $_ => 0 } @anv_lies_tud;

open(LT_EXPAND, "lt-expand $dic_in |") or die "can't fork lt-expand: $!\n";
open(OUT, "> $dic_out") or die "can't open $dic_out: $!\n";
open(ERR, "> $dic_err") or die "can't open $dic_err: $!\n";

# Count how many words handled and unhandled.
my ($out_count, $err_count) = (0, 0);
my %all_words;
my %all_lemmas;

while (<LT_EXPAND>) {
  chomp;
  if (/^([^: _~]+):(>:)?([^:<]+)([^#]*)(#.*)?/) {
    my ($word, $lemma, $tags) = ($1, $3, $4);

    $tags =~ s/(<adj><mf><sp>)\+.*/$1/;
    $tags =~ s/(<vblex><pri><p.><..>)\+.*/$1/;
    $lemma = $word if ($lemma eq 'direct' or $lemma eq 'prpers');

    $all_lemmas{$lemma} = 1;
    $all_words{$word} = 1;

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
    elsif ($tags eq '<cnjadv>')           { $tag = "C adv" }     # eta, emichañs
    elsif ($tags =~ /<cnjsub>.*/)         { $tag = "C sub" }     # mar, pa

    # Adverbs.
    elsif ($tags eq '<adv>')              { $tag = "A" }         # alies, alese, amañ
    elsif ($tags eq '<adv><neg>')         { $tag = "A neg" }     # ne, ned, n’
    elsif ($tags eq '<adv><itg>')         { $tag = "A itg" }     # perak, penaos
    elsif ($tags eq '<preadv>')           { $tag = "A pre" }     # gwall, ken, pegen

    # Adjectives.
    elsif ($tags eq '<adj><mf><sp>')      { $tag = "J" }     # brav, fur
    elsif ($tags eq '<adj><sint><comp>')  { $tag = "J cmp" } # bravoc’h
    elsif ($tags eq '<adj><sint><sup>')   { $tag = "J sup" } # bravañ
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
    elsif ($tags eq '<prn><dem><f><sg>')       { $tag = "R dem f s"; }   # homañ
    elsif ($tags eq '<prn><dem><mf><sg>')      { $tag = "R dem e s"; }   # se
    elsif ($tags eq '<prn><ind><mf><sg>')      { $tag = "R ind mf s"; }  # hini
    elsif ($tags eq '<prn><ind><mf><pl>')      { $tag = "R ind mf p"; }  # re
    elsif ($tags eq '<prn><def><mf><sg>')      { $tag = "R def e s"; }   # henn
    elsif ($tags eq '<prn><def><m><sg>')       { $tag = "R def m s"; }   # egile
    elsif ($tags eq '<prn><def><f><sg>')       { $tag = "R def f s"; }   # eben

    # Pronouns object.
    elsif ($tags eq '<prn><obj><p1><mf><sg>') { $tag = "R e s 1 obj"; } # ma, va
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
    elsif ($tags eq '<num><ord><mf><sp>')   { $tag = "K e sp o"; }
    elsif ($tags eq '<num><ord><mf><sg>')   { $tag = "K e s o"; }
    elsif ($tags eq '<num><ord><mf><pl>')   { $tag = "K e p o"; }
    elsif ($tags eq '<num><ord><m><pl>')    { $tag = "K m p o"; }
    elsif ($tags eq '<num><ord><m><sp>')    { $tag = "K m sp o"; }
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
    elsif ($tags eq '<np><top><sg>')     { $tag = "Z e s top" }  # Aostria
    elsif ($tags eq '<np><top><pl>')     { $tag = "Z e p top" }  # Azorez
    elsif ($tags eq '<np><top><m><sg>')  { $tag = "Z m s top" }  # Kreiz-Breizh
    elsif ($tags eq '<np><cog><mf><sg>') { $tag = "Z e s cog" }
    elsif ($tags eq '<np><ant><m><sg>')  { $tag = "Z m s ant" }  # Alan
    elsif ($tags eq '<np><ant><f><sg>')  { $tag = "Z f s ant" }  # Youna
    elsif ($tags eq '<np><al><mf><sg>')  { $tag = "Z e s al" }   # Leclerc
    elsif ($tags eq '<np><al><m><sg>')   { $tag = "Z m s al" }   # Ofis
    elsif ($tags eq '<np><al><f><sg>')   { $tag = "Z f s al" }   # Bibl

    elsif ($tags eq '<n><acr><m><sg>')   { $tag = "S m s" }      # TER

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

    # Words that we tag as both masculine, feminine
    # even though Apertium does not tag them with both gender.
    if ($lemma eq 'trubuilh' and $word =~ /^[tdz]rubuilh(où)?$/) {
      $tag =~ s/^N m/N e/;
    }

    if ($tag =~ /N m p/) {
      if (exists $anv_lies_tud{$word} or $word =~ /[A-Z].*iz$/) {
        $tag .= ' t';
        ++$anv_lies_tud{$word};
      }
    }

    my ($first_letter_lemma) = $lemma =~ /^(gw|[ktpgdbm]).*/i;
    my ($first_letter_word)  = $word  =~ /^([kg]w|c’h|[gdbzfktvpw]).*/i;
    $first_letter_lemma = lc $first_letter_lemma;
    $first_letter_word  = lc $first_letter_word;

    if    ($lemma eq 'kaout' and !($word =~ '.*aout')) { }
    elsif ($word  eq 'tud')    { }
    elsif ($word  eq 'dud')    { $tag .= " M:1:1a" }
    elsif ($word  eq 'zud')    { $tag .= " M:2:" }
    elsif ($word  eq 'diweuz') { }
    elsif ($word  eq 'tiweuz') { $tag .= " M:3:" }
    elsif ($word  eq 'ziweuz') { $tag .= " M:1:1b:" }
    elsif ($word =~ '^kezeg-?(koad|mor|blein)?$')   { }
    elsif ($word =~ '^gezeg-?(koad|mor|blein)?$')   { $tag .= " M:1:1a:" }
    elsif ($word =~ '^c’hezeg-?(koad|mor|blein)?$') { $tag .= " M:2:" }
    elsif ($word =~ '^daou(ividig|lin|lagad|ufern)$') { }
    elsif ($word =~ '^taou(ividig|lin|lagad|ufern)$') { $tag .= " M:3:" }
    elsif ($word =~ '^zaou(ividig|lin|lagad|ufern)$') { $tag .= " M:1:1b:" }
    elsif ($word =~ '^div(abrant|c’har|esker|lez|rec’h|ronn|orzhed|jod|skoaz|skouarn)$') { }
    elsif ($word =~ '^tiv(abrant|c’har|esker|lez|rec’h|ronn|orzhed|jod|skoaz|skouarn)$') { $tag .= " M:3:" }
    elsif ($word =~ '^ziv(abrant|c’har|esker|lez|rec’h|ronn|orzhed|jod|skoaz|skouarn)$') { $tag .= " M:1:1b:" }
    elsif ($lemma =~ /^gou[ei]/i){
      if  ($word  =~ /^ou[ei]/i) { $tag .= " M:1:1a:1b:4:" }
      elsif ($first_letter_word  eq 'k')   { $tag .= " M:3:" }
      elsif ($first_letter_word  eq 'c’h') { $tag .= " M:4:" }
    } elsif ($first_letter_lemma and 
             $first_letter_word  and 
             $first_letter_lemma ne $first_letter_word and
             !($first_letter_lemma eq 'k' and $first_letter_word eq 'kw')) {
      # Add mutation tag.
      if      ($first_letter_lemma eq 'k') {
        if    ($first_letter_word  eq 'c’h')      { $tag .= " M:a0:2:" }
        elsif ($first_letter_word  eq 'g')        { $tag .= " M:1:1a:" }
        elsif ($first_letter_word  eq 'gw')       { $tag .= " M:1:1a:" }
      } elsif ($first_letter_lemma eq 't')        {
        if    ($first_letter_word  eq 'd')        { $tag .= " M:1:1a:" }
        elsif ($first_letter_word  eq 'z')        { $tag .= " M:2:" }
      } elsif ($first_letter_lemma eq 'p')        {
        if    ($first_letter_word  eq 'b')        { $tag .= " M:1:1a:" }
        elsif ($first_letter_word  eq 'f')        { $tag .= " M:2:" }
      } elsif ($first_letter_lemma eq 'gw')       {
        if    ($first_letter_word  eq 'w')        { $tag .= " M:1:1a:1b:4:" }
        elsif ($first_letter_word  eq 'kw')       { $tag .= " M:3:" }
        elsif ($first_letter_word  eq 'c’h')      { $tag .= " M:4:" }
      } elsif ($first_letter_lemma eq 'g')        {
        if    ($first_letter_word  eq 'c’h')      { $tag .= " M:1:1a:1b:4:" }
        elsif ($first_letter_word  eq 'k')        { $tag .= " M:3:" }
      } elsif ($first_letter_lemma eq 'd')        {
        if    ($first_letter_word  eq 'z')        { $tag .= " M:1:1b:4:" }
        elsif ($first_letter_word  eq 't')        { $tag .= " M:3:4:" }
      } elsif ($first_letter_lemma eq 'b')        {
        if    ($first_letter_word  eq 'v')        { $tag .= " M:1:1a:1b:4:" }
        elsif ($first_letter_word  eq 'p')        { $tag .= " M:3:" }
      } elsif ($first_letter_lemma eq 'm')        {
        if    ($first_letter_word  eq 'v')        { $tag .= " M:1:1a:1b:4:" }
      }
      unless ($tag =~ /:$/) {
        print STDERR "*** unexpected mutation [$first_letter_lemma] -> "
                   . "[$first_letter_word] lemma=[$lemma][$first_letter_lemma] "
                   . "-> word=[$word][$first_letter_word] tag=[$tag]\n";
      }
    }
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

print "Lemma words missing from dictionary:\n";
foreach (sort keys %all_lemmas) { print "$_\n" unless (exists $all_words{$_}); }

# Check whether some words in anv_lies_tud have are missing in dictionary.
foreach (sort keys %anv_lies_tud) {
  print STDERR "*** plural noun [$_] is missing in Apertium dictionary.\n" unless ($anv_lies_tud{$_});
}

`java -jar morfologik-stemming-nodict-1.4.0.jar tab2morph -i apertium-br-fr.br.dix-LT.txt -o output.txt`;
`java -jar morfologik-stemming-nodict-1.4.0.jar fsa_build -i output.txt -o breton.dict`;

print "Created [$out_count] words, unhandled [$err_count] words\n";
