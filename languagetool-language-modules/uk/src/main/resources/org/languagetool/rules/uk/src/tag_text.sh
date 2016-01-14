#!/bin/sh

BASE="../../../../../../../../../.."
BASE1="$BASE/languagetool-commandline/target/classes"
BASE2="$BASE/languagetool-core/target/classes"
BASE3="$BASE/languagetool-language-modules/en/target/classes"
BASE4="$BASE/languagetool-language-modules/uk/target/classes"


LT_DIR=`ls $BASE/languagetool-standalone/target/LanguageTool-?.*-SNAPSHOT`
LIBDIR="$BASE/languagetool-standalone/target/$LT_DIR/$LT_DIR/libs"
#LIBS=`ls $LIBDIR | tr '\n' ':'`

CPATH=$LIBDIR/lucene-gosen-ipadic.jar:$LIBDIR/ictclas4j.jar:$LIBDIR/cjftransform.jar:$LIBDIR/jwordsplitter.jar:$LIBDIR/commons-logging.jar:$LIBDIR/segment.jar:$LIBDIR/morfologik-fsa.jar:$LIBDIR/morfologik-speller.jar:$LIBDIR/morfologik-stemming.jar:$LIBDIR/commons-lang.jar
CPATH=$CPATH:/home/arysin/.m2/repository/org/languagetool/language-dict-uk/3.2-SNAPSHOT/language-dict-uk-3.2-SNAPSHOT.jar

# Profiling

# For JVisualVM
#JAVA_OPTS="-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
#JAVA_OPTS="-Xverify:none"

# For JMC
#JAVA_OPTS="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=dumponexit.jfr"
#export PATH=/usr/java/latest/bin:$PATH


function run_lt() 
{
  SRC="$1"
  ID="$2"
    java $JAVA_OPTS -cp $BASE1:$BASE2:$BASE3:$BASE4:$CPATH \
       org.languagetool.commandline.Main -l uk -t $SRC > tagged$ID.out
}

SRC_BASE="$HOME/work/ukr/spelling/media"
SRCS="tyzhden/td.txt um/um.txt dt.txt.clean vz/vz.txt ukrlit/ukr_lit.txt mandela/mandela.txt"

IDS_TO_CHECK="$@"

echo $BASE
echo $LIBS

#mv -f checked.out checked.out.bak

if [ "$IDS_TO_CHECK" == "0" ]; then
    ID=$IDS_TO_CHECK
    SRC=text/t.txt
    echo "Tagging $SRC [$ID]"

    run_lt $SRC 0

    diff checked$ID.out.bak checked$ID.out > checked$ID.out.diff
    exit
fi


[ -f "$IDS_TO_CHECK" ] && {

    SRC=$IDS_TO_CHECK

    echo "Tagging $SRC"

    run_lt $SRC 0

  exit 0
}


ID=1
for src_file in $SRCS; do

  if [ "$IDS_TO_CHECK" == "" ] || (echo $IDS_TO_CHECK | grep -q $ID) ; then

    SRC=$SRC_BASE/$src_file
    
    echo "Tagging $SRC [$ID]"

    run_lt $SRC $ID
    
    diff tagged$ID.out.bak tagged$ID.out > tagged$ID.out.diff

  fi

  (( ID = ID + 1))
done

