DIR=`pwd`
#echo $DIR/..
WORTE=$DIR/worte
WOERTER=$DIR/woerter
TMP=$DIR/tmp
BIN=$DIR/bin
AWK=$DIR/awk
ERGEBNIS=$DIR/ergebnis
export TMP
export DIR
export WORTE
export WOERTER
export BIN
export AWK
export ERGEBNIS

cat $WOERTER/alt.txt >$WORTE/notinneu.txt
cat $WOERTER/altverb.txt >>$WORTE/notinneu.txt
cat $WOERTER/namen.txt >$WORTE/deutschfnamen.txt
cat $WOERTER/vornamen.txt >$WORTE/deutschnamen.txt
cat $WOERTER/orte.txt >$WORTE/deutschort.txt
cat $WOERTER/verbe.txt >$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_A-B.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_C-F.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_G-J.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_K-M.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_N-R.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_S-T.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_U-Z.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_komposita_umlaute.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_klein.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_gross.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/woerter_computer.txt >>$WORTE/nonameortneu.txt
#cat $WOERTER/zuform.txt >>$WORTE/nonameortneu.txt
#cat $WOERTER/selten.txt >>$WORTE/nonameortneu.txt
cat $WOERTER/neue_woerter.txt >>$WORTE/nonameortneu.txt

