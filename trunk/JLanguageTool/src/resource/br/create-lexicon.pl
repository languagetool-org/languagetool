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
#    $ cd apertium-br-fr/
# 2) Install Apertium tools:
#    $ sudo apt-get install lttoolbox
# 3) Download morfologik-stemming-1.4.0.zip from
#    http://sourceforge.net/projects/morfologik/files/morfologik-stemming/1.4.0/
#    $ unzip morfologik-stemming-1.4.0.zip
#    This creates morfologik-stemming-nodict-1.4.0.jar
# 4) Run the script:
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
  "Abkhazianed",
  "Afrikaned",
  "Akadianed",
  "Aljerianed",
  "Alamaned",
  "Amerikaned",
  "Andoraned",
  "Angled",
  "Aostralianed",
  "Aostrianed",
  "Arabed",
  "Asturianed",
  "Barbared",           "Varbared",       "Parbared",
  "Bachkired",          "Vachkired",      "Pachkired",
  "Bahameaned",         "Vahameaned",     "Pahameaned",
  "Barbadianed",        "Varbadianed",    "Parbadianed",
  "Belarusianed",       "Velarusianed",   "Pelarusianed",
  "Belizeaned",         "Velizeaned",     "Pelizeaned",
  "Berbered",           "Verbered",       "Perbered",
  "Bermudaned",         "Vermudaned",     "Permudaned",
  "Bolivianed",         "Volivianed",     "Polivianed",
  "Brazilianed",        "Vrazilianed",    "Prazilianed",
  "Bretoned",           "Vretoned",       "Pretoned",
  "Brezhoned",          "Vrezhoned",      "Prezhoned",
  "Bruneianed",         "Vruneianed",     "Pruneianed",
  "Bulgared",           "Vulgared",       "Pulgared",
  "Chileaned",
  "Daned",              "Zaned",          "Taned",
  "Dominikaned",        "Zominikaned",    "Tominikaned",
  "Eskimoed",
  "Fidjianed",
  "Finned",
  "Flamanked",
  "Franked",
  "Frañsizien",
  "Frioulaned",
  "Frizianed",
  "Gallaoued",          "C’hallaoued",    "Kallaoued",
  "Gambianed",          "C’hambianed",    "Kambianed",
  "Germaned",           "C’hermaned",     "Kermaned",
  "Ghanaianed",         "C’hanaianed",    "Khanaianed",
  "Gineaned",           "C’hineaned",     "Kineaned",
  "Grenadianed",        "C’hrenadianed",  "Krenadianed",
  "Gwenedourien",       "Wenedourien",    "Kwenedourien",
  "Gwenedourion",       "Wenedourion",    "Kwenedourion",
  "Gresianed",          "C’hresianed",    "Kresianed",
  "Hindoued",
  "Honduraned",
  "Indianed",
  "Italianed",
  "Jamaikaned",
  "Jordanianed",
  "Kabiled",            "Gabiled",        "C’habiled",
  "Kanadianed",         "Ganadianed",     "C’hanadianed",
  "Kanaked",            "Ganaked",        "C’hanaked",
  "Karnuted",           "Garnuted",       "C’harnuted",
  "Kastilhaned",        "Gastilhaned",    "C’hastilhaned",
  "Katalaned",          "Gatalaned",      "C’hatalaned",
  "Kazaked",            "Gazaked",        "C’hazaked",
  "Kelted",             "Gelted",         "C’helted",
  "Kenyaned",           "Genyaned",       "C’henyaned",
  "Kolombianed",        "Golombianed",    "C’holombianed",
  "Komorianed",         "Gomorianed",     "C’homorianed",
  "Koreaned",           "Goreaned",       "C’horeaned",
  "Kostarikaned",       "Gostarikaned",   "C’hostarikaned",
  "Kreizafrikaned",     "Greizafrikaned", "C’hreizafrikaned",
  "Kroated",            "Groated",        "C’hroated",
  "Kubaned",            "Gubaned",        "C’hubaned",
  "Kurded",             "Gurded",         "C’hurded",
  "Kuriosolited",       "Guriosolited",   "C’huriosolited",
  "Laponed",
  "Makedonianed",       "Vakedonianed",
  "Malgached",          "Valgached",
  "Malianed",           "Valianed",
  "Maouritanianed",     "Vaouritanianed",
  "Marokaned",          "Varokaned",
  "Mec’hikaned",        "Vec’hikaned",
  "Mongoled",           "Vongoled",
  "Muzulmaned",         "Vuzulmaned",
  "Namibianed",
  "Navajoed",
  "Nevezkaledonianed",
  "Nigerianed",
  "Okitaned",
  "Ouzbeked",
  "Palestinianed",      "Balestinianed",                   "Falestinianed",
  "Panameaned",         "Banameaned",                      "Fanameaned",
  "Papoued",            "Bapoued",                         "Fapoued",
  "Parizianed",         "Barizianed",                      "Farizianed",
  "Polinezianed",       "Bolinezianed",                    "Folinezianed",
  "Romaned",
  "Rusianed",
  "Salvadorianed",
  "Samoaned",
  "Sarded",
  "Savoazianed",
  "Serbed",
  "Sikilianed",
  "Sirianed",
  "Slovaked",
  "Slovened",
  "Spagnoled",
  "Suafrikaned",
  "Suised",
  "Tadjiked",           "Dadjiked",                        "Zadjiked",
  "Tanzanianed",        "Danzanianed",                     "Zanzanianed",
  "Tatared",            "Datared",                         "Zatared",
  "Tibetaned",          "Dibetaned",                       "Zibetaned",
  "Tonganed",           "Donganed",                        "Zonganed",
  "Tunizianed",         "Dunizianed",                      "Zunizianed",
  "Turked",             "Durked",                          "Zurked",
  "Turkmened",          "Durkmened",                       "Zurkmened",
  "Tuvaluaned",         "Duvaluaned",                      "Zuvaluaned",
  "Vikinged",
  "Yakouted",
  "Zambianed",
  "Zouloued",
  "adeiladourien",
  "adeiladourion",
  "advibien",
  "advibion",
  "aktorien",
  "aktorion",
  "aktourien",
  "aktourion",
  "alamanegerien",
  "alamanegerion",
  "alierien",
  "alierion",
  "alkimiourien",
  "alkimiourion",
  "alouberien",
  "alouberion",
  "alpaerien",
  "alpaerion",
  "aluzenerien",
  "aluzenerion",
  "amaezhierien",
  "amaezhierion",
  "amatourien",
  "amatourion",
  "ambrougerien",
  "ambrougerion",
  "ambulañsourien",
  "ambulañsourion",
  "amezeien",
  "amezeion",
  "amprevanoniourien",
  "amprevanoniourion",
  "animatourien",
  "animatourion",
  "annezerien",
  "annezerion",
  "antropologourien",
  "antropologourion",
  "aozerien",
  "aozerion",
  "apotikerien",
  "apotikerion",
  "arbennigourien",
  "arbennigourion",
  "arboellerien",
  "arboellerion",
  "archerien",
  "archerion",
  "arc’hwilierien",
  "arc’hwilierion",
  "ardivikerien",
  "ardivikerion",
  "ardivinkerien",
  "ardivinkerion",
  "arerien",
  "arerion",
  "argaderien",
  "argaderion",
  "arkeologourien",
  "arkeologourion",
  "armdiourien",
  "armdiourion",
  "armeourien",
  "armeourion",
  "armerzhourien",
  "armerzhourion",
  "arsellerien",
  "arsellerion",
  "arvesterien",
  "arvesterion",
  "arvestourien",
  "arvestourion",
  "arzourien",
  "arzourion",
  "askellerien",
  "askellerion",
  "astraerien",
  "astraerion",
  "aterserien",
  "aterserion",
  "atletourien",
  "atletourion",
  "avielerien",
  "avielerion",
  "avielourien",
  "avielourion",
  "bachelourien",       "vachelourien",   "pachelourien",
  "bachelourion",       "vachelourion",   "pachelourion",
  "bac’herien",         "vac’herien",     "pac’herien",
  "bac’herion",         "vac’herion",     "pac’herion",
  "bagsavourien",       "vagsavourien",   "pagsavourien",
  "bagsavourion",       "vagsavourion",   "pagsavourion",
  "bagsturierien",      "vagsturierien",  "pagsturierien",
  "bagsturierion",      "vagsturierion",  "pagsturierion",
  "baleerien",          "valeerien",      "paleerien",
  "baleerion",          "valeerion",      "paleerion",
  "bamerien",           "vamerien",       "pamerien",
  "bamerion",           "vamerion",       "pamerion",
  "bangounellerien",    "vangounellerien", "pangounellerien",
  "bangounellerion",    "vangounellerion", "pangounellerion",
  "bannerien",          "vannerien",      "pannerien",
  "bannerion",          "vannerion",      "pannerion",
  "bannikerien",        "vannikerien",    "pannikerien",
  "bannikerion",        "vannikerion",    "pannikerion",
  "banvezerien",        "vanvezerien",    "panvezerien",
  "banvezerion",        "vanvezerion",    "panvezerion",
  "baraerien",          "varaerien",      "paraerien",
  "baraerion",          "varaerion",      "paraerion",
  "bargederien",        "vargederien",    "pargederien",
  "bargederion",        "vargederion",    "pargederion",
  "barnerien",          "varnerien",      "parnerien",
  "barnerion",          "varnerion",      "parnerion",
  "baroned",            "varoned",        "paroned",
  "barverien",          "varverien",      "parverien",
  "barverion",          "varverion",      "parverion",
  "barzhed",            "varzhed",        "parzhed",
  "barzhonegourien",    "varzhonegourien", "parzhonegourien",
  "barzhonegourion",    "varzhonegourion", "parzhonegourion",
  "barzhoniezhourien",  "varzhoniezhourien", "parzhoniezhourien",
  "barzhoniezhourion",  "varzhoniezhourion", "parzhoniezhourion",
  "bastrouilherien",    "vastrouilherien", "pastrouilherien",
  "bastrouilherion",    "vastrouilherion", "pastrouilherion",
  "beajourien",         "veajourien",     "peajourien",
  "beajourion",         "veajourion",     "peajourion",
  "bedoniourien",       "vedoniourien",   "pedoniourien",
  "bedoniourion",       "vedoniourion",   "pedoniourion",
  "begennelourien",     "vegennelourien", "pegennelourien",
  "begennelourion",     "vegennelourion", "pegennelourion",
  "beleien",            "veleien",        "peleien",
  "beliourien",         "veliourien",     "peliourien",
  "beliourion",         "veliourion",     "peliourion",
  "bellerien",          "vellerien",      "pellerien",
  "bellerion",          "vellerion",      "pellerion",
  "benerien",           "venerien",       "penerien",
  "benerion",           "venerion",       "penerion",
  "berranaleien",       "verranaleien",   "perranaleien",
  "berrskriverien",     "verrskriverien", "perrskriverien",
  "berrskriverion",     "verrskriverion", "perrskriverion",
  "bes-sekretourien",   "ves-sekretourien", "pes-sekretourien",
  "bes-sekretourion",   "ves-sekretourion", "pes-sekretourion",
  "bes-teñzorerien",    "ves-teñzorerien", "pes-teñzorerien",
  "bes-teñzorerion",    "ves-teñzorerion", "pes-teñzorerion",
  "besrektorien",       "vesrektorien",   "pesrektorien",
  "besrektorion",       "vesrektorion",   "pesrektorion",
  "besrenerien",        "vesrenerien",    "pesrenerien",
  "besrenerion",        "vesrenerion",    "pesrenerion",
  "bessekretourien",    "vessekretourien", "pessekretourien",
  "bessekretourion",    "vessekretourion", "pessekretourion",
  "besteñzorerien",     "vesteñzorerien", "pesteñzorerien",
  "besteñzorerion",     "vesteñzorerion", "pesteñzorerion",
  "bevezerien",         "vevezerien",     "pevezerien",
  "bevezerion",         "vevezerion",     "pevezerion",
  "bevoniourien",       "vevoniourien",   "pevoniourien",
  "bevoniourion",       "vevoniourion",    "pevoniourion",
  "bezhinerien",        "vezhinerien",    "pezhinerien",
  "bezhinerion",        "vezhinerion",    "pezhinerion",
  "bezierien",          "vezierien",      "pezierien",
  "bezierion",          "vezierion",      "pezierion",
  "bidanellerien",      "vidanellerien",  "pidanellerien",
  "bidanellerion",      "vidanellerion",  "pidanellerion",
  "bigrierien",         "vigrierien",     "pigrierien",
  "bigrierion",         "vigrierion",     "pigrierion",
  "biniaouerien",       "viniaouerien",   "piniaouerien",
  "biniaouerion",       "viniaouerion",   "piniaouerion",
  "biolinourien",       "violinourien",   "piolinourien",
  "biolinourion",       "violinourion",   "piolinourion",
  "bizaouierien",       "vizaouierien",   "pizaouierien",
  "bizaouierion",       "vizaouierion",   "pizaouierion",
  "bleinerien",         "vleinerien",     "pleinerien",
  "bleinerion",         "vleinerion",     "pleinerion",
  "blenierien",         "vlenierien",     "plenierien",
  "blenierion",         "vlenierion",     "plenierion",
  "boloñjerien",        "voloñjerien",    "poloñjerien",
  "boloñjerion",        "voloñjerion",    "poloñjerion",
  "bombarderien",       "vombarderien",   "pombarderien",
  "bombarderion",       "vombarderion",   "pombarderion",
  "bonelourien",        "vonelourien",    "ponelourien",
  "bonelourion",        "vonelourion",    "ponelourion",
  "boseien",            "voseien",        "poseien",
  "boserien",           "voserien",       "poserien",
  "boserion",           "voserion",       "poserion",
  "botaouerien",        "votaouerien",    "potaouerien",
  "botaouerion",        "votaouerion",    "potaouerion",
  "bouloñjerien",       "vouloñjerien",   "pouloñjerien",
  "bouloñjerion",       "vouloñjerion",   "pouloñjerion",
  "bourc’hizien",       "vourc’hizien",   "pourc’hizien",
  "bourevien",          "vourevien",      "pourevien",
  "boutellerien",       "voutellerien",   "poutellerien",
  "boutellerion",       "voutellerion",   "poutellerion",
  "boutinelerien",      "voutinelerien",  "poutinelerien",
  "boutinelerion",      "voutinelerion",  "poutinelerion",
  "brabañserien",       "vrabañserien",   "prabañserien",
  "brabañserion",       "vrabañserion",   "prabañserion",
  "braventiourien",     "vraventiourien", "praventiourien",
  "braventiourion",     "vraventiourion", "praventiourion",
  "bravigourien",       "vravigourien",   "pravigourien",
  "bravigourion",       "vravigourion",   "pravigourion",
  "bredelfennerien",    "vredelfennerien", "predelfennerien",
  "bredelfennerion",    "vredelfennerion", "predelfennerion",
  "bredklañvourien",    "vredklañvourien", "predklañvourien",
  "bredklañvourion",    "vredklañvourion", "predklañvourion",
  "bredoniourien",      "vredoniourien",  "predoniourien",
  "bredoniourion",      "vredoniourion",  "predoniourion",
  "bredourien",         "vredourien",     "predourien",
  "bredourion",         "vredourion",     "predourion",
  "bredvezeien",        "vredvezeien",    "predvezeien",
  "bredvezeion",        "vredvezeion",    "predvezeion",
  "brellien",           "vrellien",       "prellien",
  "brellion",           "vrellion",       "prellion",
  "breolimerien",       "vreolimerien",   "preolimerien",
  "breolimerion",       "vreolimerion",   "preolimerion",
  "breserien",          "vreserien",      "preserien",
  "breserion",          "vreserion",      "preserion",
  "bresourien",         "vresourien",     "presourien",
  "bresourion",         "vresourion",     "presourion",
  "bretorien",          "vretorien",      "pretorien",
  "bretorion",          "vretorion",      "pretorion",
  "breudeur",           "vreudeur",       "preudeur",
  "breutaerien",        "vreutaerien",    "preutaerien",
  "breutaerion",        "vreutaerion",    "preutaerion",
  "brezelourien",       "vrezelourien",   "prezelourien",
  "brezelourion",       "vrezelourion",   "prezelourion",
  "brezhonegerien",     "vrezhonegerien", "prezhonegerien",
  "brezhonegerion",     "vrezhonegerion", "prezhonegerion",
  "brientinien",        "vrientinien",    "prientinien",
  "brigadennourien",    "vrigadennourien", "prigadennourien",
  "brigadennourion",    "vrigadennourion", "prigadennourion",
  "brigadierien",       "vrigadierien",   "prigadierien",
  "brigadierion",       "vrigadierion",   "prigadierion",
  "brikerien",          "vrikerien",      "prikerien",
  "brikerion",          "vrikerion",      "prikerion",
  "brizhkeltiegourien", "vrizhkeltiegourien", "prizhkeltiegourien",
  "brizhkeltiegourion", "vrizhkeltiegourion", "prizhkeltiegourion",
  "brizhkredennourien", "vrizhkredennourien", "prizhkredennourien",
  "brizhkredennourion", "vrizhkredennourion", "prizhkredennourion",
  "brizhouizieien",     "vrizhouizieien", "prizhouizieien",
  "broadelourien",      "vroadelourien",  "proadelourien",
  "broadelourion",      "vroadelourion",  "proadelourion",
  "brogarourien",       "vrogarourien",   "progarourien",
  "brogarourion",       "vrogarourion",   "progarourion",
  "brorenerien",        "vrorenerien",    "prorenerien",
  "brorenerion",        "vrorenerion",    "prorenerion",
  "brozennourien",      "vrozennourien",  "prozennourien",
  "brozennourion",      "vrozennourion",  "prozennourion",
  "brudourien",         "vrudourien",     "prudourien",
  "brudourion",         "vrudourion",     "prudourion",
  "bugale",             "vugale",         "pugale",
  "bugaleigoù",         "vugaleigoù",     "pugaleigoù",
  "bugaligoù",          "vugaligoù",      "pugaligoù",
  "bugulien",           "vugulien",       "pugulien",
  "buhezegezhourien",   "vuhezegezhourien", "puhezegezhourien",
  "buhezegezhourion",   "vuhezegezhourion", "puhezegezhourion",
  "buhezoniourien",     "vuhezoniourien", "puhezoniourien",
  "buhezoniourion",     "vuhezoniourion", "puhezoniourion",
  "buhezourien",        "vuhezourien",    "puhezourien",
  "buhezourion",        "vuhezourion",    "puhezourion",
  "buhezskridourien",   "vuhezskridourien", "puhezskridourien",
  "buhezskridourion",   "vuhezskridourion", "puhezskridourion",
  "buhezskriverien",    "vuhezskriverien", "puhezskriverien",
  "buhezskriverion",    "vuhezskriverion", "puhezskriverion",
  "burutellerien",      "vurutellerien",  "purutellerien",
  "burutellerion",      "vurutellerion",  "purutellerion",
  "butunerien",         "vutunerien",     "putunerien",
  "butunerion",         "vutunerion",     "putunerion",
  "chakerien",
  "chakerion",
  "chalboterien",
  "chalboterion",
  "chaokerien",
  "chaokerion",
  "charreerien",
  "charreerion",
  "charretourien",
  "charretourion",
  "chaseourien",
  "chaseourion",
  "cherifed",
  "chikanerien",
  "chikanerion",
  "cow-boyed",
  "c’hoarierien",
  "c’hoarierion",
  "c’hoarzherien",
  "c’hoarzherion",
  "c’hwennerien",
  "c’hwennerion",
  "c’hwiblaerien",
  "c’hwiblaerion",
  "c’hwiletaerien",
  "c’hwiletaerion",
  "c’hwilierien",
  "c’hwilierion",
  "c’hwisterien",
  "c’hwisterion",
  "dalc’hourien",       "zalc’hourien",   "talc’hourien",
  "dalc’hourion",       "zalc’hourion",   "talc’hourion",
  "damesaerien",        "zamesaerien",    "tamesaerien",
  "damesaerion",        "zamesaerion",    "tamesaerion",
  "damkanourien",       "zamkanourien",   "tamkanourien",
  "damkanourion",       "zamkanourion",   "tamkanourion",
  "danevellerien",      "zanevellerien",  "tanevellerien",
  "danevellerion",      "zanevellerion",  "tanevellerion",
  "danevellourien",     "zanevellourien", "tanevellourien",
  "danevellourion",     "zanevellourion", "tanevellourion",
  "dantourien",         "zantourien",     "tantourien",
  "dantourion",         "zantourion",     "tantourion",
  "daranverien",        "zaranverien",    "taranverien",
  "daranverion",        "zaranverion",    "taranverion",
  "darbarerien",        "zarbarerien",    "tarbarerien",
  "darbarerion",        "zarbarerion",    "tarbarerion",
  "daremprederien",     "zaremprederien", "taremprederien",
  "daremprederion",     "zaremprederion", "taremprederion",
  "dastumerien",        "zastumerien",    "tastumerien",
  "dastumerion",        "zastumerion",    "tastumerion",
  "dañserien",          "zañserien",      "tañserien",
  "debarzhadourien",    "zebarzhadourien", "tebarzhadourien",
  "debrerien",          "zebrerien",      "tebrerien",
  "debrerion",          "zebrerion",      "tebrerion",
  "dengarourien",       "zengarourien",   "tengarourien",
  "dengarourion",       "zengarourion",   "tengarourion",
  "denoniourien",       "zenoniourien",   "tenoniourien",
  "denoniourion",       "zenoniourion",   "tenoniourion",
  "dentourien",         "zentourien",     "tentourien",
  "dentourion",         "zentourion",     "tentourion",
  "deroerien",          "zeroerien",      "teroerien",
  "deroerion",          "zeroerion",      "teroerion",
  "deskadourien",       "zeskadourien",   "teskadourien",
  "deskadourion",       "zeskadourion",   "teskadourion",
  "deskerien",          "zeskerien",      "teskerien",
  "deskerion",          "zeskerion",      "teskerion",
  "deuñvien",           "zeuñvien",       "teuñvien",
  "dezvarnourien",      "zezvarnourien",  "tezvarnourien",
  "dezvarnourion",      "zezvarnourion",  "tezvarnourion",
  "diaraogerien",       "ziaraogerien",   "tiaraogerien",
  "diaraogerion",       "ziaraogerion",   "tiaraogerion",
  "diazezerien",        "ziazezerien",    "tiazezerien",
  "diazezerion",        "ziazezerion",    "tiazezerion",
  "diazezourien",       "ziazezourien",   "tiazezourien",
  "diazezourion",       "ziazezourion",   "tiazezourion",
  "dibaberien",         "zibaberien",     "tibaberien",
  "dibaberion",         "zibaberion",     "tibaberion",
  "dibennerien",        "zibennerien",    "tibennerien",
  "dibennerion",        "zibennerion",    "tibennerion",
  "dibunerien",         "zibunerien",     "tibunerien",
  "dibunerion",         "zibunerion",     "tibunerion",
  "diellourien",        "ziellourien",    "tiellourien",
  "diellourion",        "ziellourion",    "tiellourion",
  "difennerien",        "zifennerien",    "tifennerien", 
  "difennerion",        "zifennerion",    "tifennerion", 
  "difraosterien",      "zifraosterien",  "tifraosterien",
  "difraosterion",      "zifraosterion",  "tifraosterion",
  "diktatourien",       "ziktatourien",   "tiktatourien",
  "diktatourion",       "ziktatourion",   "tiktatourion",
  "diorroerien",        "ziorroerien",    "tiorroerien",
  "diorroerion",        "ziorroerion",    "tiorroerion",
  "diouganerien",       "ziouganerien",   "tiouganerien",
  "diouganerion",       "ziouganerion",   "tiouganerion",
  "disivouderien",      "zisivouderien",  "tisivouderien",
  "disivouderion",      "zisivouderion",  "tisivouderion",
  "diskibien",          "ziskibien",      "tiskibien",
  "diskibion",          "ziskibion",      "tiskibion",
  "diskouezerien",      "ziskouezerien",  "tiskouezerien",
  "diskouezerion",      "ziskouezerion",  "tiskouezerion",
  "dispac’herien",      "zispac’herien",  "tispac’herien",
  "dispac’herion",      "zispac’herion",  "tispac’herion",
  "displegadegerien",   "zisplegadegerien", "tisplegadegerien",
  "displegadegerion",   "zisplegadegerion", "tisplegadegerion",
  "disrannerien",       "zisrannerien",   "tisrannerien",
  "disrannerion",       "zisrannerion",   "tisrannerion",
  "diveliourien",       "ziveliourien",   "tiveliourien",
  "diveliourion",       "ziveliourion",   "tiveliourion",
  "divizourien",        "zivizourien",    "tivizourien",
  "divizourion",        "zivizourion",    "tivizourion",
  "dizalc’hourien",     "zizalc’hourien", "tizalc’hourien",
  "dizalc’hourion",     "zizalc’hourion", "tizalc’hourion",
  "dizertourien",       "zizertourien",   "tizertourien",
  "dizertourion",       "zizertourion",   "tizertourion",
  "dorloerien",         "zorloerien",     "torloerien",
  "dorloerion",         "zorloerion",     "torloerion",
  "dornvicherourien",   "zornvicherourien", "tornvicherourien",
  "dornvicherourion",   "zornvicherourion", "tornvicherourion",
  "dornwezhourien",     "zornwezhourien", "tornwezhourien",
  "dornwezhourion",     "zornwezhourion", "tornwezhourion",
  "douarourien",        "zouarourien",    "touarourien",
  "douarourion",        "zouarourion",    "touarourion",
  "doueoniourien",      "zoueoniourien",  "toueoniourien",
  "doueoniourion",      "zoueoniourion",  "toueoniourion",
  "dougerien",          "zougerien",      "tougerien",
  "dougerion",          "zougerion",      "tougerion",
  "dramaourien",        "zramaourien",    "tramaourien",
  "dramaourion",        "zramaourion",    "tramaourion",
  "dreistwelourien",    "zreistwelourien", "treistwelourien",
  "dreistwelourion",    "zreistwelourion", "treistwelourion",
  "drouklazherien",     "zrouklazherien", "trouklazherien",
  "drouklazherion",     "zrouklazherion", "trouklazherion",
  "eil-sekretourien",
  "eil-sekretourion",
  "eil-teñzorerien",
  "eil-teñzorerion",
  "eilc’hoarierien",
  "eilc’hoarierion",
  "eilerien",
  "eilerion",
  "eilrenerien",
  "eilrenerion",
  "eilsekretourien",
  "eilsekretourion",
  "eilteñzorerien",
  "eilteñzorerion",
  "ekologourien",
  "ekologourion",
  "eksibien",
  "embannerien",
  "embannerion",
  "embregourien",
  "embregourion",
  "emgannerien",
  "emgannerion",
  "emrenerien",
  "emrenerion",
  "emsaverien",
  "emsaverion",
  "emstriverien",
  "emstriverion",
  "emzivaded",
  "enaouerien",
  "enaouerion",
  "enbroerien",
  "enbroerion",
  "enbroidi",
  "eneberien",
  "eneberion",
  "enebourien",
  "enebourion",
  "eneoniourien",
  "eneoniourion",
  "engraverien",
  "engraverion",
  "enklaskerien",
  "enklaskerion",
  "enporzhierien",
  "enporzhierion",
  "ensellerien",
  "ensellerion",
  "eontred-kozh",
  "eosterien",
  "eosterion",
  "erbederien",
  "erbederion",
  "ergerzherien",
  "ergerzherion",
  "estlammerien",
  "estlammerion",
  "estrañjourien",
  "estrañjourion",
  "etrebroadelourien",
  "etrebroadelourion",
  "etrevroadelourien",
  "etrevroadelourion",
  "eveshaerien",
  "eveshaerion",
  "evezhierien",
  "evezhierion",
  "evnoniourien",
  "evnoniourion",
  "ezporzhierien",
  "ezporzhierion",
  "faezherien",
  "faezherion",
  "faktorien",
  "faktorion",
  "falc’herien",
  "falc’herion",
  "falserien",
  "falserion",
  "farderien",
  "farderion",
  "farserien",
  "farserion",
  "faskourien",
  "faskourion",
  "feizidi",
  "fentourien",
  "fentourion",
  "feurmerien",
  "feurmerion",
  "filmaozerien",
  "filmaozerion",
  "filozofed",
  "filozoferien",
  "filozoferion",
  "fistoulerien",
  "fistoulerion",
  "fizikourien",
  "fizikourion",
  "flatrerien",
  "flatrerion",
  "fleüterien",
  "fleüterion",
  "foeterien-bro",
  "foeterion-bro",
  "fougaserien",
  "fougaserion",
  "frankizourien",
  "frankizourion",
  "fungorollerien",
  "fungorollerion",
  "furcherien",
  "furcherion",
  "gallaouegerien",     "c’hallaouegerien", "kallaouegerien",
  "gallaouegerion",     "c’hallaouegerion", "kallaouegerion",
  "gallegerien",        "c’hallegerien",  "kallegerien",
  "gallegerion",        "c’hallegerion",  "kallegerion",
  "gaouidi",            "c’haouidi",      "kaouidi",
  "gastaouerien",       "c’hastaouerien", "kastaouerien",
  "gastaouerion",       "c’hastaouerion", "kastaouerion",
  "genaoueien",         "c’henaoueien",   "kenaoueien",
  "geriadurourien",     "c’heriadurourien", "keriadurourien",
  "geriadurourion",     "c’heriadurourion", "keriadurourion",
  "gitarourien",        "c’hitarourien",  "kitarourien",
  "gitarourion",        "c’hitarourion",  "kitarourion",
  "gouarnourien",       "c’houranourien", "kouarnourien",
  "gouarnourion",       "c’houranourion", "kouarnourion",
  "gouerien",           "ouerien",        "kouerien",
  "gouerion",           "ouerion",        "kouerion",
  "gouizieien",         "ouizieien",      "kouizieien",
  "goulennerien",       "c’houlennerien", "koulennerien",
  "goulennerion",       "c’houlennerion", "koulennerion",
  "goulevierien",       "c’houlevierien", "koulevierien",
  "goulevierion",       "c’houlevierion", "koulevierion",
  "gounezerien",        "c’hounezerien",   "kounezerien",
  "gounezerion",        "c’hounezerion",   "kounezerion",
  "gourdonerien",       "c’hourdonerien", "kourdonerien",
  "gourdonerion",       "c’hourdonerion", "kourdonerion",
  "gourenerien",        "c’hourenerien",  "kourenerien",
  "gourenerion",        "c’hourenerion",  "kourenerion",
  "gouzañverien",       "c’houzañverien", "kouzañverien",
  "gouzañverion",       "c’houzañverion", "kouzañverion",
  "goved",              "c’hoved",        "koved",
  "groserien",          "c’hroserien",    "kroserien",
  "groserion",          "c’hroserion",    "kroserion",
  "gwallerien",         "wallerien",      "kwallerien",
  "gwallerion",         "wallerion",      "kwallerion",
  "gwarded",            "warded",         "kwarded",
  "gwaregerien",        "waregerien",     "kwaregerien",
  "gwaregerion",        "waregerion",     "kwaregerion",
  "gwastadourien",      "wastadourien",   "kwastadourien",
  "gwastadourion",      "wastadourion",   "kwastadourion",
  "gwazed",             "wazed",          "kwazed",
  "gwazhwelerien",      "wazhwelerien",   "kwazhwelerien",
  "gwazhwelerion",      "wazhwelerion",   "kwazhwelerion",
  "gwazourien",         "wazourien",      "kwazourien",
  "gwazourion",         "wazourion",      "kwazourion",
  "gweladennerien",     "weladennerien",  "kweladennerien",
  "gweladennerion",     "weladennerion",  "kweladennerion",
  "gwellwelerien",      "wellwelerien",   "kwellwelerien",
  "gwellwelerion",      "wellwelerion",   "kwellwelerion",
  "gwenanerien",        "wenanerien",     "kwenanerien",
  "gwenanerion",        "wenanerion",     "kwenanerion",
  "gwerzherien",        "werzherien",     "kwerzherien",
  "gwerzherion",        "werzherion",     "kwerzherion",
  "gwiaderien",         "wiaderien",      "kwiaderien",
  "gwiaderion",         "wiaderion",      "kwiaderion",
  "gwiniegourien",      "winiegourien",   "kwiniegourien",
  "gwiniegourion",      "winiegourion",   "kwiniegourion",
  "gwiraourien",        "wiraourien",     "kwiraourien",
  "gwiraourion",        "wiraourion",     "kwiraourion",
  "haderien",
  "haderion",
  "hailhoned",
  "hanterourien",
  "hanterourion",
  "harperien",
  "harperion",
  "hañvourien",
  "hañvourion",
  "heforzhourien",
  "heforzhourion",
  "hegazerien",
  "hegazerion",
  "helavarourien",
  "helavarourion",
  "hellenegourien",
  "hellenegourion",
  "hemolc’herien",
  "hemolc’herion",
  "henaourien",
  "henaourion",
  "hendraourien",
  "hendraourion",
  "henoniourien",
  "henoniourion",
  "herperien",
  "herperion",
  "heñcherien",
  "heñcherion",
  "hollveliourien",
  "hollveliourion",
  "hollwashaourien",
  "hollwashaourion",
  "horolajerien",
  "horolajerion",
  "houlierien",
  "houlierion",
  "hudourien",
  "hudourion",
  "hudsteredourien",
  "hudsteredourion",
  "hudstrilhourien",
  "hudstrilhourion",
  "hunerien",
  "hunerion",
  "ijinadennourien",
  "ijinadennourion",
  "ijinourien",
  "ijinourion",
  "imbrouderien",
  "imbrouderion",
  "impalaerien",
  "impalaerion",
  "implijerien",
  "implijerion",
  "implijidi",
  "irrinnerien",
  "irrinnerion",
  "iskrimerien",
  "iskrimerion",
  "ispiserien",
  "ispiserion",
  "isrenerien",
  "isrenerion",
  "istorourien",
  "istorourion",
  "jahinerien",
  "jahinerion",
  "jakerien",
  "jakerion",
  "jedoniourien",
  "jedoniourion",
  "jiboesaourien",
  "jiboesaourion",
  "jubennourien",
  "jubennourion",
  "junterien",
  "junterion",
  "kabitened",          "gabitened",      "c’habitened",
  "kadoniourien",       "gadoniourien",   "c’hadoniourien",
  "kadoniourion",       "gadoniourion",   "c’hadoniourion",
  "kadourien",          "gadourien",      "c’hadourien",
  "kadourion",          "gadourion",      "c’hadourion",
  "kalvezourien",       "galvezourien",   "c’halvezourien",
  "kalvezourion",       "galvezourion",   "c’halvezourion",
  "kamaladed",          "gamaladed",      "c’hamaladed",
  "kamaraded",          "gamaraded",      "c’hamaraded",
  "kanerien",           "ganerien",       "c’hanerien",
  "kanerion",           "ganerion",       "c’hanerion",
  "kannaderien",        "gannaderien",    "c’hannaderien",
  "kannaderion",        "gannaderion",    "c’hannaderion",
  "kannadourien",       "gannadourien",   "c’hannadourien",
  "kannadourion",       "gannadourion",   "c’hannadourion",
  "kannerien",          "gannerien",      "c’hannerien",
  "kannerion",          "gannerion",      "c’hannerion",
  "kanolierien",        "ganolierien",    "c’hanolierien",
  "kanolierion",        "ganolierion",    "c’hanolierion",
  "kantennerien",       "gantennerien",   "c’hantennerien",
  "kantennerion",       "gantennerion",   "c’hantennerion",
  "kantonierien",       "gantonierien",   "c’hantonierien",
  "kantonierion",       "gantonierion",   "c’hantonierion",
  "kantreerien",        "gantreerien",    "c’hantreerien",
  "kantreerion",        "gantreerion",    "c’hantreerion",
  "kariaded",           "gariaded",       "c’hariaded",
  "karngerzherien",     "garngerzherien", "c’harngerzherien",
  "karngerzherion",     "garngerzherion", "c’harngerzherion",
  "karourien",          "garourien",      "c’harourien",
  "karourion",          "garourion",      "c’harourion",
  "karrdiourien",       "garrdiourien",   "c’harrdiourien",
  "karrdiourion",       "garrdiourion",   "c’harrdiourion",
  "karrellerien",       "garrellerien",   "c’harrellerien",
  "karrellerion",       "garrellerion",   "c’harrellerion",
  "karrerien",          "garrerien",      "c’harrerien",
  "karrerion",          "garrerion",      "c’harrerion",
  "kasedourien",        "gasedourien",    "c’hasedourien",
  "kasedourion",        "gasedourion",    "c’hasedourion",
  "kaserien",           "gaserien",       "c’haserien",
  "kaserion",           "gaserion",       "c’haserion",
  "kasourien",          "gasourien",      "c’hasourien",
  "kasourion",          "gasourion",      "c’hasourion",
  "kavadennerien",      "gavadennerien",  "c’havadennerien",
  "kavadennerion",      "gavadennerion",  "c’havadennerion",
  "kavalierien",        "gavalierien",    "c’havalierien",
  "kavalierion",        "gavalierion",    "c’havalierion",
  "kazetennerien",      "gazetennerien",  "c’hazetennerien",
  "kazetennerion",      "gazetennerion",  "c’hazetennerion",
  "kañfarded",          "gañfarded",      "c’hañfarded",
  "kañsellerien",       "gañsellerien",   "c’hañsellerien",
  "kañsellerion",       "gañsellerion",   "c’hañsellerion",
  "kefierien",          "gefierien",      "c’hefierien",
  "kefierion",          "gefierion",      "c’hefierion",
  "kefredourien",       "gefredourien",   "c’hefredourien",
  "kefredourion",       "gefredourion",   "c’hefredourion",
  "kefridierien",       "gefridierien",   "c’hefridierien",
  "keginerien",         "geginerien",     "c’heginerien",
  "keginerion",         "geginerion",     "c’heginerion",
  "kelaouennerien",     "gelaouennerien", "c’helaouennerien",
  "kelaouennerion",     "gelaouennerion", "c’helaouennerion",
  "kelaouerien",        "gelaouerien",    "c’helaouerien",
  "kelaouerion",        "gelaouerion",    "c’helaouerion",
  "kelennerien",        "gelennerien",    "c’helennerien",
  "kelennerien-enklasker", "gelennerien-enklasker", "c’helennerien-enklasker",
  "kelennerion-enklasker", "gelennerion-enklasker", "c’helennerion-enklasker",
  "kelennerien-enklaskerien", "gelennerien-enklaskerien", "c’helennerien-enklaskerien",
  "kelennerien-enklaskerion", "gelennerien-enklaskerion", "c’helennerien-enklaskerion",
  "kelennerion",        "gelennerion",    "c’helennerion",
  "kembraegerien",      "gembraegerien",  "c’hembraegerien",
  "kembraegerion",      "gembraegerion",  "c’hembraegerion",
  "kemenerien",         "gemenerien",     "c’hemenerien",
  "kemenerion",         "gemenerion",     "c’hemenerion",
  "kempouezerien",      "gempouezerien",  "c’hempouezerien",
  "kempouezerion",      "gempouezerion",  "c’hempouezerion",
  "kenaozerien",        "genaozerien",    "c’henaozerien",
  "kenaozerion",        "genaozerion",    "c’henaozerion",
  "kenbrezegerien",     "genbrezegerien", "c’henbrezegerien",
  "kenbrezegerion",     "genbrezegerion", "c’henbrezegerion",
  "kenderc’herien",     "genderc’herien", "c’henderc’herien",
  "kenderc’herion",     "genderc’herion", "c’henderc’herion",
  "kendirvi",           "gendirvi",       "c’hendirvi",
  "kendiskulierien",    "gendiskulierien", "c’hendiskulierien",
  "kendivizerien",      "gendivizerien",  "c’hendivizerien",
  "kendivizerion",      "gendivizerion",  "c’hendivizerion",
  "kengourenerien",     "gengourenerien", "c’hengourenerien",
  "kengourenerion",     "gengourenerion", "c’hengourenerion",
  "kenlabourerien",     "genlabourerien", "c’henlabourerien",
  "kenlabourerion",     "genlabourerion", "c’henlabourerion",
  "kenoberourien",      "genoberourien",  "c’henoberourien",
  "kenoberourion",      "genoberourion",  "c’henoberourion",
  "kensanterien",       "gensanterien",   "c’hensanterien",
  "kensanterion",       "gensanterion",   "c’hensanterion",
  "kenseurted",         "genseurted",     "c’henseurted",
  "kenskriverien",      "genskriverien",  "c’henskriverien",
  "kenskriverion",      "genskriverion",  "c’henskriverion",
  "kenstriverien",      "genstriverien",  "c’henstriverien",
  "kenstriverion",      "genstriverion",  "c’henstriverion",
  "kenurzhierien",      "genurzhierien",  "c’henurzhierien",
  "kenurzhierion",      "genurzhierion",  "c’henurzhierion",
  "kenwallerien",       "genwallerien",   "c’henwallerien",
  "kenwallerion",       "genwallerion",   "c’henwallerion",
  "kenwerzherien",      "genwerzherien",  "c’henwerzherien",
  "kenwerzherion",      "genwerzherion",  "c’henwerzherion",
  "keodedourien",       "geodedourien",   "c’heodedourien",
  "kereon",             "gereon",         "c’hereon",
  "kereourien",         "gereourien",     "c’hereourien",
  "kereourion",         "gereourion",     "c’hereourion",
  "kerzherien",         "gerzherien",     "c’herzherien",
  "kerzherion",         "gerzherion",     "c’herzherion",
  "kevalaourien",       "gevalaourien",   "c’hevalaourien",
  "kevalaourion",       "gevalaourion",   "c’hevalaourion",
  "kevelerien",         "gevelerien",     "c’hevelerien",
  "kevelerion",         "gevelerion",     "c’hevelerion",
  "kevezerien",         "gevezerien",     "c’hevezerien",
  "kevezerion",         "gevezerion",     "c’hevezerion",
  "kevrinourien",       "gevrinourien",   "c’hevrinourien",
  "kevrinourion",       "gevrinourion",   "c’hevrinourion",
  "kigerien",           "gigerien",       "c’higerien",
  "kigerion",           "gigerion",       "c’higerion",
  "kilstourmerien",     "gilstourmerien", "c’hilstourmerien",
  "kilstourmerion",     "gilstourmerion", "c’hilstourmerion",
  "kilvizien",          "gilvizien",      "c’hilvizien",
  "kilvizion",          "gilvizion",      "c’hilvizion",
  "kinnigerien",        "ginnigerien",    "c’hinnigerien",
  "kivijerien",         "givijerien",     "c’hivijerien",
  "kivijerion",         "givijerion",     "c’hivijerion",
  "kizellerien",        "gizellerien",    "c’hizellerien",
  "kizellerion",        "gizellerion",    "c’hizellerion",
  "klaskerien",         "glaskerien",     "c’hlaskerien",
  "klaskerion",         "glaskerion",     "c’hlaskerion",
  "klaskerien-bara",    "glaskerien-bara", "c’hlaskerien-bara",
  "klaskerion-bara",    "glaskerion-bara", "c’hlaskerion-bara",
  "klañvdiourien",      "glañvdiourien",  "c’hlañvdiourien",
  "klañvdiourion",      "glañvdiourion",  "c’hlañvdiourion",
  "klañvourien",        "glañvourien",    "c’hlañvourien",
  "klañvourion",        "glañvourion",    "c’hlañvourion",
  "klerinellourien",    "glerinellourien", "c’hlerinellourien",
  "klerinellourion",    "glerinellourion", "c’hlerinellourion",
  "kleuzierien",        "gleuzierien",    "c’hleuzierien",
  "kleuzierion",        "gleuzierion",    "c’hleuzierion",
  "klezeourien",        "glezeourien",    "c’hlezeourien",
  "klezeourion",        "glezeourion",    "c’hlezeourion",
  "koataerien",         "goataerien",     "c’hoataerien",
  "koataerion",         "goataerion",     "c’hoataerion",
  "kollerien",          "gollerien",      "c’hollerien",
  "kollerion",          "gollerion",      "c’hollerion",
  "komiserien",         "gomiserien",     "c’homiserien",
  "komiserion",         "gomiserion",     "c’homiserion",
  "komisien",           "gomisien",       "c’homisien",
  "kompezourien",       "gompezourien",   "c’hompezourien",
  "kompezourion",       "gompezourion",   "c’hompezourion",
  "komunourien",        "gomunourien",    "c’homunourien",
  "komunourion",        "gomunourion",    "c’homunourion",
  "komzerien",          "gomzerien",      "c’homzerien",
  "komzerion",          "gomzerion",      "c’homzerion",
  "konterien",          "gonterien",      "c’honterien",
  "konterion",          "gonterion",      "c’honterion",
  "kontourien",         "gontourien",     "c’hontourien",
  "kontourion",         "gontourion",     "c’hontourion",
  "korollerien",        "gorollerien",    "c’horollerien",
  "korollerion",        "gorollerion",    "c’horollerion",
  "kouerien",           "gouerien",       "c’houerien",
  "kouerion",           "gouerion",       "c’houerion",
  "koumananterien",     "goumananterien", "c’houmananterien",
  "koumananterion",     "goumananterion", "c’houmananterion",
  "kouraterien",        "gouraterien",    "c’houraterien",
  "kouraterion",        "gouraterion",    "c’houraterion",
  "kouronkerien",       "gouronkerien",   "c’houronkerien",
  "kouronkerion",       "gouronkerion",   "c’houronkerion",
  "kourserien",         "gourserien",     "c’hourserien",
  "kourserion",         "gourserion",     "c’hourserion",
  "kourvibien",         "gourvibien",     "c’hourvibien",
  "koñversanted",       "goñversanted",   "c’hoñversanted",
  "krakaotrouien",      "grakaotrouien",  "c’hrakaotrouien",
  "krampouezherien",    "grampouezherien", "c’hrampouezherien",
  "krampouezherion",    "grampouezherion", "c’hrampouezherion",
  "kravazherien",       "gravazherien",   "c’hravazherien",
  "kravazherion",       "gravazherion",   "c’hravazherion",
  "kredennourien",      "gredennourien",  "c’hredennourien",
  "kredennourion",      "gredennourion",  "c’hredennourion",
  "kreizourien",        "greizourien",    "c’hreizourien",
  "krennarded",         "grennarded",     "c’hrennarded",
  "kretadennerien",     "gretadennerien", "c’hretadennerien",
  "kretadennerion",     "gretadennerion", "c’hretadennerion",
  "kristenien",         "gristenien",     "c’hristenien",
  "kristenion",         "gristenion",     "c’hristenion",
  "krouadurien",        "grouadurien",    "c’hrouadurien",
  "krouadurion",        "grouadurion",    "c’hrouadurion",
  "krouerien",          "grouerien",      "c’hrouerien",
  "krouerion",          "grouerion",      "c’hrouerion",
  "krougerien",         "grougerien",     "c’hrougerien",
  "krougerion",         "grougerion",     "c’hrougerion",
  "kulatorien",         "gulatorien",     "c’hulatorien",
  "kulatorion",         "gulatorion",     "c’hulatorion",
  "kunduerien",         "gunduerien",     "c’hunduerien",
  "kunduerion",         "gunduerion",     "c’hunduerion",
  "kuzulierien",        "guzulierien",    "c’huzulierien",
  "kuzulierion",        "guzulierion",    "c’huzulierion",
  "kuzulierien-departamant", "guzulierien-departamant", "c’huzulierien-departamant",
  "kuzulierion-departamant", "guzulierion-departamant", "c’huzulierion-departamant",
  "kuzulierien-kêr",    "guzulierien-kêr", "c’huzulierien-kêr",
  "kuzulierien-rannvro",    "guzulierien-rannvro", "c’huzulierien-rannvro",
  "kuzulierion-rannvro",    "guzulierion-rannvro", "c’huzulierion-rannvro",
  "kêraozourien",       "gêraozourien",   "c’hêraozourien",
  "kêraozourion",       "gêraozourion",   "c’hêraozourion",
  "labourerien",
  "labourerien-douar",
  "labourerion",
  "labourerion-douar",
  "laeron",
  "laezherien",
  "laezherion",
  "lagadourien",
  "lagadourion",
  "lakizien",
  "lakizion",
  "lammerien",
  "lammerion",
  "lamponed",
  "lavarerien",
  "lavarerion",
  "lazherien",
  "lazherion",
  "leaned",
  "lemmerien",
  "lemmerion",
  "lennerien",
  "lennerion",
  "lennourien",
  "lennourion",
  "leurennerien",
  "leurennerion",
  "levraouaerien",
  "levraouaerion",
  "levraouegerien",
  "levraouegerion",
  "levraouerien",
  "levraouerion",
  "levrierien",
  "levrierion",
  "lezvibien",
  "liorzherien",
  "liorzherion",
  "liorzhourien",
  "liorzhourion",
  "liperien",
  "liperion",
  "lipouzerien",
  "lipouzerion",
  "liverien",
  "liverion",
  "livourien",
  "livourion",
  "lizheregourien",
  "lizheregourion",
  "lizherennerien",
  "lizherennerion",
  "loenoniourien",
  "loenoniourion",
  "lonkerien",
  "lonkerion",
  "louzaouerien",
  "louzaouerion",
  "louzawourien",
  "louzawourion",
  "lubanerien",
  "lubanerion",
  "luc’hskeudennerien",
  "luc’hskeudennerion",
  "luc’hvannerien",
  "luc’hvannerion",
  "lunederien",
  "lunederion",
  "lunedourien",
  "lunedourion",
  "luskerien",
  "luskerion",
  "mab-kaer",           "vab-kaer",
  "madoberourien",      "vadoberourien",
  "madoberourion",      "vadoberourion",
  "maendreserien",      "vaendreserien",
  "maendreserion",      "vaendreserion",
  "maengizellerien",    "vaengizellerien",
  "maengizellerion",    "vaengizellerion",
  "maered",             "vaered",
  "maesaerien",         "vaesaerien",
  "maesaerion",         "vaesaerion",
  "magerien",           "vagerien",
  "magerion",           "vagerion",
  "mailhed",            "vailhed",
  "maltouterien",       "valtouterien",
  "maltouterion",       "valtouterion",
  "manifesterien",      "vanifesterien",
  "manifesterion",      "vanifesterion",
  "maodierned",         "vaodierned",
  "marc’hadourien",     "varc’hadourien",
  "marc’hadourion",     "varc’hadourion",
  "marc’hegerien",      "varc’hegerien",
  "marc’hegerion",      "varc’hegerion",
  "marc’heien",         "varc’heien",
  "marc’hekaerien",     "varc’hekaerien",
  "marc’hekaerion",     "varc’hekaerion",
  "marc’hergerien",     "varc’hergerien",
  "marc’hergerion",     "varc’hergerion",
  "marc’hhouarnerien",  "varc’hhouarnerien",
  "marc’hhouarnerion",  "varc’hhouarnerion",
  "marc’homerien",      "varc’homerien",
  "marc’homerion",      "varc’homerion",
  "margodennerien",     "vargodennerien",
  "margodennerion",     "vargodennerion",
  "markizien",          "varkizien",
  "martoloded",         "vartoloded",
  "marvailherien",      "varvailherien",
  "marvailherion",      "varvailherion",
  "mañsonerien",        "vañsonerien",
  "mañsonerion",        "vañsonerion",
  "mbourc’herien",
  "mbourc’herion",
  "mederien",           "vederien",
  "medisined",          "vedisined",
  "medisinourien",      "vedisinourien",
  "medisinourion",      "vedisinourion",
  "mekanikerien",       "vekanikerien",
  "mekanikerion",       "vekanikerion",
  "melestrourien",      "velestrourien",
  "melestrourion",      "velestrourion",
  "melldroaderien",     "velldroaderien",
  "melldroaderion",     "velldroaderion",
  "mendemerien",        "vendemerien",
  "mendemerion",        "vendemerion",
  "menec’h",            "venec’h",
  "mengleuzierien",     "vengleuzierien",
  "mengleuzierion",     "vengleuzierion",
  "merc’hetaerien",     "verc’hetaerien",
  "merc’hetaerion",     "verc’hetaerion",
  "merdeerien",         "merdeerien",
  "merdeerion",         "merdeerion",
  "mererien",           "vererien",
  "mererion",           "vererion",
  "merourien",          "verourien",
  "merourion",          "verourion",
  "merserien",          "verserien",
  "merserion",          "verserion",
  "merzherien",         "verzherien",
  "merzherierien",      "verzherierien",
  "merzherierion",      "verzherierion",
  "merzherion",         "verzherion",
  "mesaerien",          "mesaerien",
  "mesaerion",          "mesaerion",
  "metalourien",        "vetalourien",
  "metalourion",        "vetalourion",
  "meveled",            "veveled",
  "mevelien",           "vevelien",
  "mevelion",           "vevelion",
  "mezeien",            "vezeien",
  "mezvierien",         "vezvierien",
  "mezvierion",         "vezvierion",
  "mibien",             "vibien",
  "mibien-gaer",        "vibien-gaer",
  "mibien-vihan",       "vibien-vihan",
  "micherelourien",     "vicherelourien",
  "micherelourion",     "vicherelourion",
  "micherourien",       "vicherourien",
  "micherourion",       "vicherourion",
  "mic’hieien",         "vic’hieien",
  "mignoned",           "vignoned",
  "milinerien",         "vilinerien",
  "milinerion",         "vilinerion",
  "milourien",          "vilourien",
  "milourion",          "vilourion",
  "milvezeien",         "vilvezeien",
  "milvezeion",         "vilvezeion",
  "minerien",           "vinerien",
  "minerion",           "vinerion",
  "ministred",          "vinistred",
  "mirerien",           "virerien",
  "mirerion",           "virerion",
  "mirourien",          "virourien",
  "mirourion",          "virourion",
  "misionerien",        "visionerien",
  "misionerion",        "visionerion",
  "mistri",             "vistri",
  "mistri-prezegennerien", "vistri-prezegennerien",
  "mistri-prezegennerion", "vistri-prezegennerion",
  "mistri-skol",        "vistri-skol",
  "mistri-vicherour",   "vistri-vicherour",
  "monitourien",        "vonitourien",
  "monitourion",        "vonitourion",
  "moraerien",          "voraerien",
  "moraerion",          "voraerion",
  "morianetaerien",     "vorianetaerien",
  "morianetaerion",     "vorianetaerion",
  "morlaeron",          "vorlaeron",
  "moruteaerien",       "voruteaerien",
  "moruteaerion",       "voruteaerion",
  "mouezhierien",       "vouezhierien",
  "mouezhierion",       "vouezhierion",
  "moullerien",         "voullerien",
  "moullerion",         "voullerion",
  "mouskederien",       "vouskederien",
  "mouskederion",       "vouskederion",
  "mouzherien",         "vouzherien",
  "mouzherion",         "vouzherion",
  "mudien",             "vudien",
  "muntrerien",         "vutrerien",
  "muntrerion",         "vutrerion",
  "munuzerien",         "vunuzerien",
  "munuzerion",         "vunuzerion",
  "muzikerien",         "vuzikerien",
  "muzikerion",         "vuzikerion",
  "naetaerien",
  "naetaerion",
  "naturoniourien",
  "naturoniourion",
  "naturourien",
  "naturourion",
  "neuñverien",
  "neuñverion",
  "nijerien",
  "nijerion",
  "nized",
  "nizien",
  "notered",
  "noterien",
  "noterion",
  "nozourien",
  "nozourion",
  "oadourien",
  "oadourion",
  "oberataourien",
  "oberataourion",
  "obererien",
  "obererion",
  "oberiataerien",
  "oberiataerion",
  "oberierien",
  "oberierion",
  "oberourien",
  "oberourion",
  "ofiserien",
  "ofiserion",
  "ograouerien",
  "ograouerion",
  "orfebourien",
  "orfebourion",
  "oueskerien",
  "oueskerion",
  "paeerien",           "baeerien",                        "faeerien",
  "paeerion",           "baeerion",                        "faeerion",
  "paeroned",           "baeroned",                        "faeroned",
  "palerien",           "balerien",                        "falerien",
  "palerion",           "balerion",                        "falerion",
  "palforserien",       "balforserien",                    "falforserien",
  "palforserion",       "balforserion",                    "falforserion",
  "paluderien",         "baluderien",                      "faluderien",
  "paluderion",         "baluderion",                      "faluderion",
  "pantierien",         "bantierien",                      "fantierien",
  "pantierion",         "bantierion",                      "fantierion",
  "paotred",            "baotred",                         "faotred",
  "paotredoù",          "baotredoù",                       "faotredoù",
  "paotredigoù",        "baotredigoù",                     "faotredigoù",
  "paramantourien",     "baramantourien",                  "faramantourien",
  "paramantourion",     "baramantourion",                  "faramantourion",
  "pardonerien",        "bardonerien",                     "fardonerien",
  "pardonerion",        "bardonerion",                     "fardonerion",
  "pareourien",         "bareourien",                      "fareourien",
  "pareourion",         "bareourion",                      "fareourion",
  "pec’herien",         "bec’herien",                      "fec’herien",
  "pec’herion",         "bec’herion",                      "fec’herion",
  "pellenndroaderien",  "bellenndroaderien",               "fellenndroaderien",
  "pellenndroaderion",  "bellenndroaderion",               "fellenndroaderion",
  "pellskriverien",     "bellskriverien",                  "fellskriverien",
  "pellskriverion",     "bellskriverion",                  "fellskriverion",
  "pennsekretourien",   "bennsekretourien",                "fennsekretourien",
  "pennsekretourion",   "bennsekretourion",                "fennsekretourion",
  "pennskridaozerien",  "bennskridaozerien",               "fennskridaozerien",
  "pennskridaozerion",  "bennskridaozerion",               "fennskridaozerion",
  "pennskrivagnerien",  "bennskrivagnerien",               "fennskrivagnerien",
  "pennskrivagnerion",  "bennskrivagnerion",               "fennskrivagnerion",
  "pennsonerien",       "bennsonerien",                    "fennsonerien",
  "pennsonerion",       "bennsonerion",                    "fennsonerion",
  "penterien",          "benterien",                       "fenterien",
  "penterion",          "benterion",                       "fenterion",
  "peoc’hgarourien",    "beoc’hgarourien",                 "feoc’hgarourien",
  "peoc’hgarourion",    "beoc’hgarourion",                 "feoc’hgarourion",
  "peorien",            "beorien",                         "feorien",
  "peorion",            "beorion",                         "feorion",
  "perc’henned",        "berc’henned",                     "ferc’henned",
  "perc’herined",       "berc’herined",                    "ferc’herined",
  "personed",           "bersoned",                        "fersoned",
  "perukennerien",      "berukennerien",                   "ferukennerien",
  "perukennerion",      "berukennerion",                   "ferukennerion",
  "peskedoniourien",    "beskedoniourien",                 "feskedoniourien",
  "peskedoniourion",    "beskedoniourion",                 "feskedoniourion",
  "peskerien",          "beskerien",                       "feskerien",
  "peskerion",          "beskerion",                       "feskerion",
  "pesketaerien",       "besketaerien",                    "fesketaerien",
  "pesketaerion",       "besketaerion",                    "fesketaerion",
  "pianoourien",        "bianoourien",                     "fianoourien",
  "pianoourion",        "bianoourion",                     "fianoourion",
  "piaouerien",         "biaouerien",                      "fiaouerien",
  "piaouerion",         "biaouerion",                      "fiaouerion",
  "pibien",             "bibien",                          "fibien",
  "pikerien",           "bikerien",                        "fikerien",
  "pikerion",           "bikerion",                        "fikerion",
  "pilhaouaerien",      "bilhaouaerien",                   "filhaouaerien",
  "pilhaouaerion",      "bilhaouaerion",                   "filhaouaerion",
  "pleustrerien",       "bleustrerien",                    "fleustrerien",
  "pleustrerion",       "bleustrerion",                    "fleustrerion",
  "plomerien",          "blomerien",                       "flomerien",
  "plomerion",          "blomerion",                       "flomerion",
  "poberien",           "boberien",                        "foberien",
  "poberion",           "boberion",                        "foberion",
  "poderien",           "boderien",                        "foberien",
  "poderion",           "boderion",                        "foberion",
  "poliserien",         "boliserien",                      "foliserien",
  "poliserion",         "boliserion",                      "foliserion",
  "politikacherien",    "bolitikacherien",                 "folitikacherien",
  "politikacherion",    "bolitikacherion",                 "folitikacherion",
  "politikerien",       "bolitikerien",                    "folitikerien",
  "politikerion",       "bolitikerion",                    "folitikerion",
  "poltredourien",      "boltredourien",                   "foltredourien",
  "poltredourion",      "boltredourion",                   "foltredourion",
  "pomperien",          "bomperien",                       "fomperien",
  "pomperion",          "bomperion",                       "fomperion",
  "porzhierien",        "borzhierien",                     "forzhierien",
  "porzhierion",        "borzhierion",                     "forzhierion",
  "posterien",          "bosterien",                       "fosterien",
  "posterion",          "bosterion",                       "fosterion",
  "pourchaserien",      "bourchaserien",                   "fourchaserien",
  "pourchaserion",      "bourchaserion",                   "fourchaserion",
  "pourvezerien",       "bourvezerien",                    "fourvezerien",
  "pourvezerion",       "bourvezerion",                    "fourvezerion",
  "prederourien",       "brederourien",                    "frederourien",
  "prederourion",       "brederourion",                    "frederourion",
  "prefeded",           "brefeded",                        "frefeded",
  "preizherien",        "breizherien",                     "freizherien",
  "preizherion",        "breizherion",                     "freizherion",
  "prenerien",          "brenerien",                       "frenerien",
  "prenerion",          "brenerion",                       "frenerion",
  "prezegennerien",     "brezegennerien",                  "frezegennerien",
  "prezegennerion",     "brezegennerion",                  "frezegennerion",
  "prezidanted",        "brezidanted",                     "frezidanted",
  "priourien",          "briourien",                       "friourien",
  "priourion",          "briourion",                       "friourion",
  "prizachourien",      "brizachourien",                   "frizachourien",
  "prizachourion",      "brizachourion",                   "frizachourion",
  "prizonidi",          "brizonidi",                       "frizonidi",
  "prizonierien",       "brizonierien",                    "frizonierien",
  "prizonierion",       "brizonierion",                    "frizonierion",
  "priñsed",            "briñsed",                         "friñsed",
  "produerien",         "broduerien",                      "froduerien",
  "produerion",         "broduerion",                      "froduerion",
  "psikiatrourien",     "bsikiatrourien",                  "fsikiatrourien",
  "psikiatrourion",     "bsikiatrourion",                  "fsikiatrourion",
  "psikologourien",     "bsikologourien",                  "fsikologourien",
  "psikologourion",     "bsikologourion",                  "fsikologourion",
  "rakprenerien",
  "rakprenerion",
  "randonerien",
  "randonerion",
  "ratouzed",
  "rebecherien",
  "rebecherion",
  "rederien",
  "rederien-vor",
  "rederien-vro",
  "rederion",
  "rederion-vor",
  "rederion-vro",
  "reizhaouerien",
  "reizhaouerion",
  "reizherien",
  "reizherion",
  "rektorien",
  "rektorion",
  "renerien",
  "renerion",
  "repuidi",
  "reveulzierien",
  "reveulzierion",
  "riblerien",
  "riblerion",
  "riboderien",
  "riboderion",
  "riboulerien",
  "riboulerion",
  "roberien",
  "roberion",
  "roerien",
  "roerion",
  "romanterien",
  "romanterion",
  "rugbierien",
  "rugbierion",
  "ruzarded",
  "ruzerien",
  "ruzerion",
  "ruzikerien",
  "ruzikerion",
  "salverien",
  "salverion",
  "saoznegerien",
  "saoznegerion",
  "savadennerien",
  "savadennerion",
  "saverien",
  "saverion",
  "saveteerien",
  "saveteerion",
  "savourien",
  "savourion",
  "sekretourien",
  "sekretourien-kontour",
  "sekretourion-kontour",
  "sekretourien-kontourien",
  "sekretourion",
  "sekretourion-kontourion",
  "selaouerien",
  "selaouerion",
  "sellerien",
  "sellerion",
  "sellourien",
  "sellourion",
  "senedourien",
  "senedourion",
  "servijerien",
  "servijerion",
  "sevenerien",
  "sevenerion",
  "sikourerien",
  "sikourerion",
  "sinerien",
  "sinerion",
  "skarzherien",
  "skarzherion",
  "skeudennaouerien",
  "skeudennaouerion",
  "skeudennourien",
  "skeudennourion",
  "skiantourien",
  "skiantourion",
  "skignerien",
  "skignerion",
  "sklavourien",
  "sklavourion",
  "skoazellerien",
  "skoazellerion",
  "skolaerien",
  "skolaerion",
  "skorerien",
  "skorerion",
  "skraperien",
  "skraperion",
  "skridaozerien",
  "skridaozerion",
  "skridvarnourien",
  "skridvarnourion",
  "skrivagnerien",
  "skrivagnerion",
  "skuberien",
  "skuberion",
  "skultourien",
  "skultourion",
  "sodien",
  "sokialourien",
  "sokialourion",
  "sokiologourien",
  "sokiologourion",
  "sonaozourien",
  "sonaozourion",
  "sonerien",
  "sonerion",
  "sonourien",
  "sonourion",
  "soroc’horien",
  "soroc’horion",
  "sorserien",
  "sorserion",
  "soudarded",
  "soñjerien",
  "soñjerion",
  "sperederien",
  "sperederion",
  "spierien",
  "spierion",
  "splujerien",
  "splujerion",
  "sponsorien",
  "sponsorion",
  "sponterien",
  "sponterion",
  "sporterien",
  "sporterion",
  "sportourien",
  "sportourion",
  "stadrenerien",
  "stadrenerion",
  "stalierien",
  "stalierion",
  "steredoniourien",
  "steredoniourion",
  "steredourien",
  "steredourion",
  "stlejerien",
  "stlejerion",
  "stolierien",
  "stolierion",
  "stourmerien",
  "stourmerion",
  "stranerien",
  "stranerion",
  "strinkerien",
  "strinkerion",
  "strobinellerien",
  "strobinellerion",
  "studierien",
  "studierion",
  "stummerien",
  "stummerion",
  "sturierien",
  "sturierion",
  "taboulinerien",      "daboulinerien",                   "zaboulinerien",
  "taboulinerion",      "daboulinerion",                   "zaboulinerion",
  "tagerien",           "dagerien",                        "zagerien",
  "tagerion",           "dagerion",                        "zagerion",
  "tailhanterien",      "dailhanterien",                   "zailhanterien",
  "tailhanterion",      "dailhanterion",                   "zailhanterion",
  "talabarderien",      "dalabarderien",                   "zalabarderien",
  "talabarderion",      "dalabarderion",                   "zalabarderion",
  "tanerien",           "danerien",                        "zanerien",
  "tanerion",           "danerion",                        "zanerion",
  "taolerien",          "daolerien",                       "zaolerien",
  "taolerion",          "daolerion",                       "zaolerion",
  "tavarnourien",       "davarnourien",                    "zavarnourien",
  "tavarnourion",       "davarnourion",                    "zavarnourion",
  "teknikourien",       "deknikourien",                    "zeknikourien",
  "teknikourion",       "deknikourion",                    "zeknikourion",
  "telennourien",       "delennourien",                    "zelennourien",
  "telennourion",       "delennourion",                    "zelennourion",
  "tennerien",          "dennerien",                       "zennerien",
  "tennerion",          "dennerion",                       "zennerion",
  "teozofourien",       "deozofourien",                    "zeozofourien",
  "teozofourion",       "deozofourion",                    "zeozofourion",
  "teñzorerien",        "deñzorerien",                     "zeñzorerien",
  "teñzorerion",        "deñzorerion",                     "zeñzorerion",
  "tinellerien",        "dinellerien",                     "zinellerien",
  "tinellerion",        "dinellerion",                     "zinellerion",
  "tisavourien",        "disavourien",                     "zisavourien",
  "tisavourion",        "disavourion",                     "zisavourion",
  "titourerien",        "ditourerien",                     "zitourerien",
  "titourerion",        "ditourerion",                     "zitourerion",
  "toerien",            "doerien",                         "zoerien",
  "toerion",            "doerion",                         "zoerion",
  "togerien",           "dogerien",                        "zogerien",
  "togerion",           "dogerion",                        "zogerion",
  "tommerien",          "dommerien",                       "zommerien",
  "tommerion",          "dommerion",                       "zommerion",
  "tontoned",           "dontoned",                        "zontoned",
  "torfedourien",       "dorfedourien",                    "zorfedourien",
  "torfedourion",       "dorfedourion",                    "zorfedourion",
  "toucherien",         "doucherien",                      "zoucherien",
  "toucherion",         "doucherion",                      "zoucherion",
  "touellerien",        "douellerien",                     "zouellerien",
  "touellerion",        "douellerion",                     "zouellerion",
  "toullerien",         "doullerien",                      "zoullerien",
  "toullerien-buñsoù",  "doullerien-buñsoù",               "zoullerien-buñsoù",
  "toullerien-vezioù",  "doullerien-vezioù",               "zoullerien-vezioù",
  "toullerion",         "doullerion",                      "zoullerion",
  "toullerion-buñsoù",  "doullerion-buñsoù",               "zoullerion-buñsoù",
  "toullerion-vezioù",  "doullerion-vezioù",               "zoullerion-vezioù",
  "touristed",          "douristed",                       "zouristed",
  "trafikerien",        "drafikerien",                     "zrafikerien",
  "trafikerion",        "drafikerion",                     "zrafikerion",
  "trapezerien",        "drapezerien",                     "zrapezerien",
  "trapezerion",        "drapezerion",                     "zrapezerion",
  "trec’hourien",       "drec’hourien",                    "zrec’hourien",
  "trec’hourion",       "drec’hourion",                    "zrec’hourion",
  "tredanerien",        "dredanerien",                     "zredanerien",
  "tredanerion",        "dredanerion",                     "zredanerion",
  "tredeeged",          "dredeeged",                       "zredeeged",
  "tredeoged",          "dredeoged",                       "zredeoged",
  "treitourien",        "dreitourien",                     "zreitourien",
  "treitourion",        "dreitourion",                     "zreitourion",
  "treizherien",        "dreizherien",                     "zreizherien",
  "treizherion",        "dreizherion",                     "zreizherion",
  "tremenerien",        "dremenerien",                     "zremenerien",
  "tremenerion",        "dremenerion",                     "zremenerion",
  "tresourien",         "dresourien",                      "zresourien",
  "tresourion",         "dresourion",                      "zresourion",
  "trevadennerien",     "drevadennerien",                  "zrevadennerien",
  "trevadennerion",     "drevadennerion",                  "zrevadennerion",
  "trevourien",         "drevourien",                      "zrevourien",
  "trevourion",         "drevourion",                      "zrevourion",
  "troadeien",          "droadeien",                       "zroadeien",
  "troergerzherien",    "droergerzherien",                 "zroergerzherien",
  "troergerzherion",    "droergerzherion",                 "zroergerzherion",
  "troerien",           "droerien",                        "zroerien",
  "troerien-douar",     "droerien-douar",                  "zroerien-douar",
  "troerion",           "droerion",                        "zroerion",
  "troerion-douar",     "droerion-douar",                  "zroerion-douar",
  "troiadourien",       "droiadourien",                    "zroiadourien",
  "troiadourion",       "droiadourion",                    "zroiadourion",
  "trompilherien",      "drompilherien",                   "zrompilherien",
  "trompilherion",      "drompilherion",                   "zrompilherion",
  "trubarded",          "drubarded",                       "zrubarded",
  "trucherien",         "drucherien",                      "zrucherien",
  "trucherion",         "drucherion",                      "zrucherion",
  "truilhenned",        "druilhenned",                     "zruilhenned",
  "tud",                "dud",                             "zud",
  "tudoniourien",       "dudoniourien",                    "zudoniourien",
  "tudonourien",        "dudonourien",                     "zudonourien",
  "tudonourion",        "dudonourion",                     "zudonourion",
  "turgnerien",         "durgnerien",                      "zurgnerien",
  "turgnerion",         "durgnerion",                      "zurgnerion",
  "unyezherien",
  "urcherien",
  "urcherion",
  "uzurerien",
  "uzurerion",
  "voterien",
  "voterion",
  "wikipedourien",
  "wikipedourion",
  "yalc’hadourien",
  "yalc’hadourion",
  "yezhadurourien",
  "yezhadurourion",
  "yezherien",
  "yezherion",
  "yezhoniourien",
  "yezhoniourion",
  "yezhourien",
  "yezhourion",
  "yunerien",
  "yunerion",
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
    $first_letter_lemma = "" unless (defined $first_letter_lemma);
    my ($first_letter_word) = $word  =~ /^([kg]w|c’h|[gdbzfktvpw]).*/i;
    $first_letter_word = "" unless (defined $first_letter_word);
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
        if    ($first_letter_word  eq 'c’h')      { $tag .= " M:0a:2:" }
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

# Adding missing words in dictionary.
# kiz exists only in expressions in Apertium (which is OK) but
# for LanguageTool, it's easier to make it a normal word so we
# don't give false positive on "war ho c'hiz", etc.
print OUT "kiz\tkiz\tN f s\n";
print OUT "c’hiz\tkiz\tN f s M:0a:2:\n";
print OUT "giz\tkiz\tN f s M:1:1a:\n";

print "Lemma words missing from dictionary:\n";
foreach (sort keys %all_lemmas) { print "$_\n" unless (exists $all_words{$_}); }

# Check whether some words in anv_lies_tud have are missing in dictionary.
foreach (sort keys %anv_lies_tud) {
  print STDERR "*** plural noun [$_] is missing in Apertium dictionary.\n" unless ($anv_lies_tud{$_});
}

`java -jar morfologik-stemming-nodict-1.4.0.jar tab2morph -i apertium-br-fr.br.dix-LT.txt -o output.txt`;
`java -jar morfologik-stemming-nodict-1.4.0.jar fsa_build -i output.txt -o breton.dict`;

print "Created [$out_count] words, unhandled [$err_count] words\n";
