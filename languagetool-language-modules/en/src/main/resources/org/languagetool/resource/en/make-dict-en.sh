#!/bin/sh

LANG=POSIX
TMP_OUTPUT=eng1.txt
TMP_GET_UNC=english.txt
TMP_FINAL=eng2.txt
OUTPUT=english.dict
OUTPUT_SYNTH=english_synth.dict
MORFOLOGIK=morfologik-tools-1.5.4-standalone.jar
rm $TMP_OUTPUT 
rm $TMP_GET_UNC
rm $TMP_FINAL
gawk -f filter_out.awk infl.txt part-of-speech.txt| gawk -f remap.awk >penn.txt 
cat penn.txt manually_added.txt | sort -u >$TMP_GET_UNC
cp $TMP_GET_UNC $TMP_FINAL
gawk -f get_unc.awk $TMP_FINAL |sort -u > $TMP_OUTPUT
#test
gawk -f test_dict.awk $TMP_OUTPUT
#create normal dictionary
#cp $TMP_OUTPUT 
#gawk -f morph_data.awk $TMP_OUTPUT | fsa_ubuild -O -o $OUTPUT
java -jar $MORFOLOGIK tab2morph -i $TMP_OUTPUT -o $TMP_OUTPUT.tab
java -jar $MORFOLOGIK fsa_build -f cfsa2 -i $TMP_OUTPUT.tab -o $OUTPUT
echo "Output written to $OUTPUT"
#create synthesis dictionary
rm $TMP_OUTPUT.tab
gawk -f synteza.awk $TMP_OUTPUT > $TMP_OUTPUT.revtab
java -jar $MORFOLOGIK tab2morph -nw -i $TMP_OUTPUT.revtab -o $TMP_OUTPUT.tab
#gawk -f morph_data.awk | sort -u | fsa_ubuild -O -o $OUTPUT_SYNTH
java -jar $MORFOLOGIK fsa_build -f cfsa2 -i $TMP_OUTPUT.tab -o $OUTPUT_SYNTH
gawk -f tags.awk $TMP_OUTPUT |sort -u >english_tags.txt
echo "Output written to $OUTPUT_SYNTH"