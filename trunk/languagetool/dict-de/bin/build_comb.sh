DIR=`pwd`
#echo $DIR/..
WORTE=$DIR/woerter
TMP=$DIR/tmp
BIN=$DIR/bin
AWK=$DIR/awk
ERGEBNIS=$DIR/ergebnis
export TMP
export DIR
export WORTE
export BIN
export AWK
export ERGEBNIS

rm -rf $TMP
mkdir $TMP

# wie fuer neu:
cat $WORTE/namen.txt > $TMP/deutsch1.dic
cat $WORTE/vornamen.txt >> $TMP/deutsch1.dic
cat $WORTE/orte.txt >> $TMP/deutsch1.dic
cat $WORTE/woerter_gross.txt >> $TMP/deutsch1.dic
cat $WORTE/woerter_klein.txt >> $TMP/deutsch1.dic
cat $WORTE/woerter_computer.txt >> $TMP/deutsch1.dic
cat $WORTE/woerter_komposita_*.txt >> $TMP/deutsch1.dic
cat $WORTE/neue_woerter.txt >> $TMP/deutsch1.dic
cat $WORTE/woerter_abkuerzungen.txt >> $TMP/deutsch1.dic
# und zusätzlich alt:
cat $WORTE/alt.txt >> $TMP/deutsch1.dic

export LC_ALL=C
sort -u $TMP/deutsch1.dic > $TMP/deutsch2.dic
awk -f $AWK/dupl.awk < $TMP/deutsch2.dic >$TMP/deutsch1.dic
awk -f $AWK/sortch.awk < $TMP/deutsch1.dic >$TMP/deutsch2.dic
awk -f $AWK/nodupsw.awk < $TMP/deutsch2.dic >$TMP/deutsch1.dic
wc -l $TMP/deutsch1.dic | awk '{print $1;}' > $ERGEBNIS/deutsch.dic
cat $TMP/deutsch1.dic >> $ERGEBNIS/deutsch.dic
rm -rf $TMP/deutsch1.dic
rm -rf $TMP/deutsch2.dic
