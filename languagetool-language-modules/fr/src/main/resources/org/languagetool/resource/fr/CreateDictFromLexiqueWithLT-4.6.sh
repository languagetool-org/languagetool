#!/bin/sh
#
#   Run the script:
#    $ ./CreateDictFromLexiqueWithLT-4.6
#    This creates the POS tag dictionary 'french.dict'
#    and the synthesizer dictionary 'french_synth.dict'
#    from the dicollecte lexique
#
#   Compile Input format :
#   en trois colonnes : flexion lemme   étiquette
#   séparées par des tabulations
#
#   abandonnique abandonnique J e s
#   abandonniques abandonnique J e p
#   abandonnisme abandonnisme N m s
#   abandonnismes abandonnisme N m p
#   abandonnons abandonner V imp pres 1 p
#   abandonnons abandonner V ind pres 1 p
#   abandonnions abandonner V sub pres 1 p
#   abaque abaque N m s
#   abaques abaque N m p
#
#
ToolPath="../../../../../../../../../languagetool-tools/target/"
ToolName="languagetool-tools-4.6-SNAPSHOT-jar-with-dependencies"
Input="lexique-dicollecte-fr-v6.4"
Output="french"
. ./SuppressionDoublons.sh

echo "Create dict from lexique is running ..."
# rm $Input*
rm $Output*
# Téléchargement du Lexique Dicollecte
echo "Donwloading lexique-dicollecte ..."
if [ ! -f $Input.zip ]; then
  wget http://www.dicollecte.org/download/fr/$Input.zip
fi

if [ ! -f $Input.txt ]; then
  rm README*
  unzip $Input.zip
fi


# Mise en forme des données du Lexique Dicollecte
echo "\nFormatting lexique data ..."
echo "step 1 ..."
chmod +x Degraissage.sh
./Degraissage.sh $Input
echo "step 2 ..."
chmod +x DicollecteDataFormatting.pl
./DicollecteDataFormatting.pl $Input
echo "step 3 ..."
chmod +x Simplification.sh
./Simplification.sh $Input.maigre.LT 
echo "step 4 ..."
cat $Input.maigre.LT.txt | sort > $Input.sorted.txt 
echo "step 5 ..."
FontionSuppressionDoublons $Input.sorted.txt
mv $Input.sorted.txt $Input.LT.txt

# Compilation des dictionnaires avec LT
echo "\nCompiling dictionnaries ..."

echo "# Dictionary properties." > $Output.info
echo "fsa.dict.separator=+">> $Output.info
echo "fsa.dict.encoding=utf-8" >> $Output.info
echo "fsa.dict.encoder=SUFFIX" >> $Output.info

cp $Output.info "$Output""_synth.info"


java -cp "$ToolPath""$ToolName"".jar" \
org.languagetool.tools.POSDictionaryBuilder \
    -i $Input.LT.txt \
    -info $Output.info \
    -o $Output.dict

java -cp "$ToolPath""$ToolName"".jar" \
org.languagetool.tools.DictionaryExporter \
    -i $Output.dict \
    -info $Output.info \
    -o $Output.dump 

java -cp "$ToolPath""$ToolName"".jar" \
org.languagetool.tools.SynthDictionaryBuilder \
     -i $Output.dump \
     -info "$Output""_synth.info" \
     -o "$Output""_synth.dict"

mv -f /tmp/SynthDictionaryBuilder*.txt_tags.txt "$Output""_tags.txt"
echo "\nDone."


