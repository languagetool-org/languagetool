DIR=`pwd`
#echo $DIR/..
WORTE=$DIR/worte
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
cp -f $WORTE/deutsch* $TMP
cp -f $WORTE/nonameortneu.txt $TMP

cat $TMP/deutsch* >> $TMP/deutsch1.dic
cat $TMP/nonameortneu.txt >> $TMP/deutsch1.dic
export LC_ALL=C
sort -u $TMP/deutsch1.dic > $TMP/deutsch2.dic
awk -f $AWK/dupl.awk < $TMP/deutsch2.dic >$TMP/deutsch1.dic
awk -f $AWK/sortch.awk < $TMP/deutsch1.dic >$TMP/deutsch2.dic
awk -f $AWK/nodupsw.awk < $TMP/deutsch2.dic >$TMP/deutsch1.dic
wc -l $TMP/deutsch1.dic | awk '{print $1;}' > $ERGEBNIS/deutschneu.dic
cat $TMP/deutsch1.dic >> $ERGEBNIS/deutschneu.dic
rm -rf $TMP/deutsch1.dic
rm -rf $TMP/deutsch2.dic
rm -rf $TMP/deutschnamen*
rm -rf $TMP/deutschfnamen*
rm -rf $TMP/deutschort*
rm -rf $TMP/nonameortneu.txt
 
