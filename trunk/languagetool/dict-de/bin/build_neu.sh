DIR=`pwd`
#echo $DIR/..
WORTE=$DIR/worte
TMP=$DIR/tmp
BIN=$DIR/bin
AWK=$DIR/awk
AFF=$DIR/affix
ERGEBNIS=$DIR/ergebnis
export TMP
export DIR
export WORTE
export BIN
export AWK
export AFF
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
# prepare unmunch/munch for not possible  DIXYa-> Da IXY
awk -f $AWK/abtren.awk < $TMP/deutsch1.dic >$TMP/deutsch2.dic
awk -f $AWK/abtrensav1.awk < $TMP/deutsch1.dic >$TMP/deutsch3.dic
awk -f $AWK/abtrensav2.awk < $TMP/deutsch1.dic >$TMP/deutsch31.dic
$BIN/unmunch $TMP/deutsch3.dic $AFF/deutsch.aff >$TMP/deutsch4.dic
wc -l $TMP/deutsch4.dic | awk '{print $1;}' > $TMP/deutsch41.dic
cat $TMP/deutsch4.dic >> $TMP/deutsch41.dic
$BIN/munch $TMP/deutsch41.dic $AFF/deutsch.aff >$TMP/deutsch5.dic
cat $TMP/deutsch31.dic $TMP/deutsch5.dic >$TMP/deutsch2.dic
sort -u $TMP/deutsch2.dic > $TMP/deutsch1.dic
# unmunch/munch done
wc -l $TMP/deutsch1.dic | awk '{print $1;}' > $ERGEBNIS/deutschneu.dic
cat $TMP/deutsch1.dic >> $ERGEBNIS/deutschneu.dic
rm -rf $TMP/deutsch1.dic
rm -rf $TMP/deutsch2.dic
rm -rf $TMP/deutschnamen*
rm -rf $TMP/deutschfnamen*
rm -rf $TMP/deutschort*
rm -rf $TMP/nonameortneu.txt
rm -rf $TMP/deutsch3.dic
rm -rf $TMP/deutsch31.dic
rm -rf $TMP/deutsch4.dic
rm -rf $TMP/deutsch41.dic
rm -rf $TMP/deutsch5.dic

