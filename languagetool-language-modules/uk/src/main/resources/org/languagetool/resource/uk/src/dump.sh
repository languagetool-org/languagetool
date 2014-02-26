#/bin/sh

export PATH=$PATH:~/bin

if [ "$1" = "-b" ]; then
    BASE="1"
    shift
fi

WORD="$1"

if [ "$WORD" = "" ]; then
    echo "Usage: $0 <word>"
    exit 1
fi


#MFL_CMD="java -jar ./mfl/morfologik-tools-*-standalone.jar"
MFL_CMD="mfl"

if [ "$BASE" = "1" ]; then
    $MFL_CMD fsa_dump -x -d ../ukrainian.dict | iconv -f cp1251 | grep -E "\s($WORD)\s"
else
    $MFL_CMD fsa_dump -x -d ../ukrainian.dict | iconv -f cp1251 | grep -E "^($WORD)\s"
fi
