#/bin/sh

export PATH=$PATH:~/bin

WORD="$1"

if [ "$WORD" = "" ]; then
    echo "Usage: $0 <word>"
    exit 1
fi


#MFL_CMD="java -jar ./mfl/morfologik-tools-*-standalone.jar"
MFL_CMD="mfl"

$MFL_CMD fsa_dump -x -d ../ukrainian.dict | iconv -f cp1251 | grep -E "^($WORD)\s"
