DIR=`pwd`
#echo $DIR/..
WORTE=$DIR/worte
WOERTER=$DIR/woerter
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

sort -u  $WOERTER/alt.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/alt.txt

sort -u  $WOERTER/namen.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/namen.txt

sort -u  $WOERTER/vornamen.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/vornamen.txt
sort -u  $WOERTER/orte.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/orte.txt
sort -u  $WOERTER/woerter_komposita_A-B.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_A-B.txt
sort -u  $WOERTER/woerter_komposita_C-F.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_C-F.txt
sort -u  $WOERTER/woerter_komposita_G-J.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_G-J.txt
sort -u  $WOERTER/woerter_komposita_K-M.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_K-M.txt
sort -u  $WOERTER/woerter_komposita_N-R.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_N-R.txt
sort -u  $WOERTER/woerter_komposita_S-T.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_S-T.txt
sort -u  $WOERTER/woerter_komposita_U-Z.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_U-Z.txt
sort -u  $WOERTER/woerter_komposita_umlaute.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_komposita_umlaute.txt
sort -u  $WOERTER/woerter_klein.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_klein.txt 
sort -u  $WOERTER/woerter_gross.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_gross.txt
sort -u  $WOERTER/woerter_computer.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/woerter_computer.txt
sort -u  $WOERTER/selten.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/selten.txt
sort -u  $WOERTER/neue_woerter.txt >$WOERTER/x.dic
awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
mv $WOERTER/y.dic $WOERTER/neue_woerter.txt
#sort -u  $WOERTER/zuform.txt >$WOERTER/x.dic
#awk -f $AWK/dupl.awk < $WOERTER/x.dic >$WOERTER/y.dic
#awk -f $AWK/sortch.awk < $WOERTER/y.dic >$WOERTER/x.dic
#awk -f $AWK/nodupsw.awk < $WOERTER/x.dic >$WOERTER/y.dic
#mv $WOERTER/y.dic $WOERTER/zuform.txt

rm -f $WOERTER/x.dic
rm -f $WOERTER/y.dic


