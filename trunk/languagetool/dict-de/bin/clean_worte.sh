DIR=`pwd`
#echo $DIR/..
WORTE=$DIR/worte
AWK=$DIR/awk
export DIR
export WORTE
export AWK

export LC_ALL=C
sort -u $WORTE/deutschort.txt > $WORTE/x.dic
awk -f $AWK/dupl.awk < $WORTE/x.dic >$WORTE/y.dic
awk -f $AWK/sortch.awk < $WORTE/y.dic >$WORTE/x.dic
awk -f $AWK/nodupsw.awk < $WORTE/x.dic >$WORTE/y.dic
mv $WORTE/y.dic $WORTE/deutschort.txt
rm -f $WORTE/x.dic
rm -f $WORTE/y.dic

sort -u $WORTE/deutschnamen.txt > $WORTE/x.dic
awk -f $AWK/dupl.awk < $WORTE/x.dic >$WORTE/y.dic
awk -f $AWK/sortch.awk < $WORTE/y.dic >$WORTE/x.dic
awk -f $AWK/nodupsw.awk < $WORTE/x.dic >$WORTE/y.dic
mv $WORTE/y.dic $WORTE/deutschnamen.txt
rm -f $WORTE/x.dic
rm -f $WORTE/y.dic

sort -u $WORTE/deutschfnamen.txt > $WORTE/x.dic
awk -f $AWK/dupl.awk < $WORTE/x.dic >$WORTE/y.dic
awk -f $AWK/sortch.awk < $WORTE/y.dic >$WORTE/x.dic
awk -f $AWK/nodupsw.awk < $WORTE/x.dic >$WORTE/y.dic
mv $WORTE/y.dic $WORTE/deutschfnamen.txt
rm -f $WORTE/x.dic
rm -f $WORTE/y.dic

sort -u $WORTE/nonameortneu.txt > $WORTE/x.dic
awk -f $AWK/dupl.awk < $WORTE/x.dic >$WORTE/y.dic
awk -f $AWK/sortch.awk < $WORTE/y.dic >$WORTE/x.dic
awk -f $AWK/nodupsw.awk < $WORTE/x.dic >$WORTE/y.dic
mv $WORTE/y.dic $WORTE/nonameortneu.txt
rm -f $WORTE/x.dic
rm -f $WORTE/y.dic

sort -u $WORTE/notinneu.txt > $WORTE/x.dic
awk -f $AWK/dupl.awk < $WORTE/x.dic >$WORTE/y.dic
awk -f $AWK/sortch.awk < $WORTE/y.dic >$WORTE/x.dic
awk -f $AWK/nodupsw.awk < $WORTE/x.dic >$WORTE/y.dic
mv $WORTE/y.dic $WORTE/notinneu.txt
rm -f $WORTE/x.dic
rm -f $WORTE/y.dic


