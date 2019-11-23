#!/bin/bash
# Dégraissage du Fichier d'entrée

#Input=lexique-dicollecte-fr-v6.4
Input=$1

# Simplification
sed -e '1,16 d; s/\([^\t]*\)[\t]\([^\t]*\)[\t]\([^\t]*\)[\t]\([^\t]*\)[\t]\([^\t]*\)[\t]\([^\t]*\)[\t]\([^\t]*\)[\t]\(.*\)\([^\t]*\)[\t]\(.*\)/\3\t\4\t\5\t/g; s/[\t]mg /\t/g; s/prep prepv/prep/g; s/ preverb//g; s/adv negadv/adv/g; s/negadv/adv/g; s/ detex//g; s/ proadv//g; s/\(.*\)\(prep\)\(.*\)\( loc\.prep\)\(.*\)/\1\2\3\5/g; s/loc\.nom/nom/g;' $Input.txt  > $Input.01.txt

#corrections de bugs du lexique dicollecte
sed -e 's/adj adj/adj epi/g; s/sg pl/inv/g; s/\tmieux\tadv nom/\tmieux\tadv nom mas inv/; s/au\tau\tprep det mas sg/au\tau\tdet mas sg/; s/aux\taux\tprep det mas pl/aux\taux\tdet epi pl/;' $Input.01.txt  > $Input.02.txt

#suppression des entrées actuellement incompatibes
#cat  | grep -v $'\t'l’ > $Input.maigre.txt

#compatibilisation des apostrophes
sed -e 's/^\([^\t]*\)’[\t]/\1\t/g;' $Input.02.txt > $Input.maigre.txt

rm $Input.01.txt
rm $Input.02.txt

