#!/bin/bash
# Simplification du jeu de tags

#Input=lexique-dicollecte-fr-v6.4
Input=$1
mv $Input.txt $Input.01.txt


# Simplification
sed -e 's/R pers suj \([12]\) [^ ]*/R pers suj \1/g; s/R pers obj \([12]\) [^ ]*/R pers obj \1/g;' $Input.01.txt  > $Input.02.txt
# Correction de bugs
sed -e 's/\tlaquelle\t/\tlequel\t/g; s/\tlesquels\t/\tlequel\t/g; s/\tauxquelles\t/\tauquel\t/g; s/\tauxquels\t/\tauquel\t/g; ' $Input.02.txt  > $Input.txt

rm $Input.01.txt

