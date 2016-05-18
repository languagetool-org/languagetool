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


#CP=`ls mfl/*.jar | tr '\n' ':'`
echo $CP
MFL_CMD="java -jar mfl/morfologik-tools-2.1.1-SNAPSHOT.jar"
#MFL_CMD="mfl"

if [ "$BASE" = "1" ]; then
    $MFL_CMD fsa_dump -i ../ukrainian.dict -o - | iconv -f cp1251 | grep -E "\s($WORD)\s"
else
    $MFL_CMD fsa_dump -i ../ukrainian.dict -o tmp
#    $MFL_CMD fsa_dump -i ../ukrainian.dict -o - | iconv -f cp1251 | grep -E "^($WORD)\s"
fi
