#!/bin/sh
# Daily API usage reports. To be run as a cronjob at the end of the day.
# dnaber, 2015-11-14

DATE1=`date +"%Y%m%d"`
DATE2=`date +"%Y-%m-%d"`
# for testing:
#DATE1=20160331
#DATE2=2016-03-31
TMPFILE=/tmp/log.temp
OUTFILE=/tmp/statusmail.txt

echo "Daily LanguageTool API Report $DATE2" >$OUTFILE
echo "" >>$OUTFILE

grep "$DATE2 " log-[0-9]-$DATE1*.txt >$TMPFILE

TOTAL=`grep -c "Check done:" $TMPFILE`
printf "Total text checks : %'d\n" $TOTAL >>$OUTFILE

TOTALHOME=`grep "Check done:" $TMPFILE | grep -c languagetool.org`
printf "Checks from lt.org: %'d\n" $TOTALHOME >>$OUTFILE

FF=`grep -c "languagetoolfx"  $TMPFILE`
printf "Firefox Requests  : %'d\n" $FF >>$OUTFILE

CHROME=`grep -c "chrome-extension" $TMPFILE`
printf "Chrome Requests   : %'d\n" $CHROME >>$OUTFILE

ANDROID=`grep -c "androidspell" $TMPFILE`
printf "Android Requests   : %'d\n" $ANDROID >>$OUTFILE

echo "$DATE2;$TOTAL;$FF;$CHROME;$ANDROID" >>/home/languagetool/api/api-log.csv

echo "" >>$OUTFILE
echo "An error has occurred      : `grep -c 'An error has occurred' $TMPFILE`" >>$OUTFILE
echo "too many requests          : `grep -c 'too many requests' $TMPFILE`" >>$OUTFILE
echo "too many requests (Android): `grep -c 'androidspell.*too many requests' $TMPFILE`" >>$OUTFILE

DATE_APACHE=`LANG=C date +"%a %b %d"`
YEAR=`date +"%Y"`
echo "no buffer (Apache)         : `grep \"$DATE_APACHE\" /var/log/apache2/error.log | grep $YEAR | grep -c \"No buffer space available\"`" >>$OUTFILE

cat $OUTFILE | mail -a 'Content-Type: text/plain; charset=utf-8' -s "LanguageTool API Report" daniel.naber@languagetool.org
