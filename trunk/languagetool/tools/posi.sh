#
# posi.sh creates sorted error file for coloring/TKLSpell
# first parameter: languagetool's home directory
# second parameter: pathname of file to be checked
# third parameter: language (en, de or hu)
# result goes into languagetool/tools/checkout.txt
# calling example:
#sh posi.sh #/mnt/win_d/hattyu/tyuk/dtest/python/danielnaber/cvs3/languagetool #/home/en/tyuk/dtest/qt/examples/richedit2/lang/work/chk.txt
#
# this file will be used by TKLSpell
#
cd $1
base=`basename $2`
python TextChecker.py -l $3 -x $2 >/tmp/1_$base
awk -f $1/tools/posi.awk </tmp/1_$base >/tmp/2_$base
sort -n /tmp/2_$base >/tmp/3_$base
awk -f tools/noemptyline.awk </tmp/3_$base >$1/tools/checkout.txt
rm -f /tmp/1_$base /tmp/2_$base /tmp/3_$base
