#! /bin/bash
# le fichier doit être dans le directory de travail et passé en argument
###################################################### 
# fonction
######################################################
FontionSuppressionDoublons() {
# Fichier à trier passé en argument
FileName=$1


MyLine1=""
MyLine2=""

#rm "Doublons.""$FileName"

sed -e 's/[\t]/\\\\t/g;' "$FileName" > "$FileName"".tmp"
rm $FileName
while read MyLine

do

MyLine2=$MyLine1
MyLine1=$MyLine
if [ "$MyLine1" != "$MyLine2" ] ;
then
    echo -e $MyLine1 >> $FileName
#else
#    echo $MyLine1 >> "Doublons.""$FileName"
fi

done < $FileName.tmp
rm $FileName.tmp
}

