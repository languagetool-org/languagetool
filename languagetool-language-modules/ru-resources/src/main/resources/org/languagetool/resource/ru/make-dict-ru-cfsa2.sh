#/bin/sh
java -jar morfologik-tools-1.5.2-standalone.jar  tab2morph  -i russian.txt -o output.txt
java -jar morfologik-tools-1.5.2-standalone.jar fsa_build -f cfsa2 -i output.txt -o mydict.dict