#/bin/sh
gawk -f synteza.awk russian.txt >output1.txt
gawk -f tags.awk russian.txt | sort -u > mydict_tags.txt
java -jar morfologik-tools-1.5.2-standalone.jar tab2morph -nw -i output1.txt -o encoded.txt
java -jar morfologik-tools-1.5.2-standalone.jar fsa_build -f cfsa2 -i encoded.txt -o mydict_synth.dict