#!/bin/sh
# Daily API usage reports. To be run as a cronjob at the end of the day.
# dnaber, 2015-11-14

LANG="de_DE.UTF-8"
DATE1=`date +"%Y%m%d"`
DATE2=`date +"%Y-%m-%d"`
# for testing:
#DATE1=20160331
#DATE2=2016-03-31
TMPFILE=/tmp/log.temp
TMPFILE_ALL=/tmp/log-all.temp
OUTFILE=/tmp/statusmail.txt

echo "Daily LanguageTool API Report $DATE2" >$OUTFILE
echo "" >>$OUTFILE

grep -h "$DATE2 " log-[0-9]-$DATE1*.txt log-1.txt log-2.txt >$TMPFILE
cat log-[0-9]-$DATE1*.txt log-1.txt log-2.txt >$TMPFILE_ALL

TOTAL=`grep -c "Check done:" $TMPFILE`
printf "Total text checks : %'d\n" $TOTAL >>$OUTFILE

TOTALHOME=`grep "Check done:" $TMPFILE | grep -c languagetool.org`
printf "Checks from lt.org: %'d\n" $TOTALHOME >>$OUTFILE

FF=`grep -c "languagetoolfx"  $TMPFILE`
printf "LanguageToolFx Req: %'d\n" $FF >>$OUTFILE

CHROME=`grep -c "chrome-extension" $TMPFILE`
printf "Chrome Requests   : %'d\n" $CHROME >>$OUTFILE

WEBEXT=`grep -c "webextension" $TMPFILE`
printf "WebExtension Req  : %'d\n" $WEBEXT >>$OUTFILE

ANDROID=`grep -c "androidspell" $TMPFILE`
printf "Android Requests  : %'d\n" $ANDROID >>$OUTFILE

CLIENT=`grep -c "java-http-client" $TMPFILE`
printf "Java Client Req   : %'d\n" $CLIENT >>$OUTFILE

SUBLIME=`grep -c ":sublime" $TMPFILE`
printf "Sublime Requests  : %'d\n" $SUBLIME >>$OUTFILE

echo "$DATE2;$TOTAL;$FF;$CHROME;$ANDROID;$CLIENT;$SUBLIME;$WEBEXT" >>/home/languagetool/api/api-log.csv

echo "" >>$OUTFILE
echo "An error has occurred      : `grep -c 'An error has occurred' $TMPFILE`" >>$OUTFILE
echo "too many requests          : `grep -c 'too many requests' $TMPFILE`" >>$OUTFILE
echo "too many requests (Android): `grep -c 'too many requests.*androidspell' $TMPFILE`" >>$OUTFILE
#echo "TextTooLongException : `grep -c 'TextTooLongException' $TMPFILE`" >>$OUTFILE
#echo "TimeoutException     : `grep -c 'java.util.concurrent.TimeoutException' $TMPFILE`" >>$OUTFILE

echo "Top HTTP error codes:" >>$OUTFILE
grep "An error has occurred" /tmp/log.temp|sed 's/.*HTTP code \([0-9]\+\)..*/HTTP code \1/'|sort |uniq -c| sort -r -n >>$OUTFILE

echo "" >>$OUTFILE
echo "Top 10 Errors:" >>$OUTFILE
grep 'Could not check sentence' $TMPFILE_ALL | grep -v "Caused by:" | uniq -c | sort -n -r | head -n 10 >>$OUTFILE

echo "" >>$OUTFILE
echo "v1 API                     : `grep -c 'V1TextChecker' $TMPFILE`" >>$OUTFILE
echo "v2 API                     : `grep -c 'V2TextChecker' $TMPFILE`" >>$OUTFILE

DATE_APACHE=`LANG=C date +"%a %b %d"`
YEAR=`date +"%Y"`
# note: requires a root cronjob to copy the error.log file to ~/api/apache_error.log:
echo "" >>$OUTFILE
echo "no buffer (Apache)         : `grep \"$DATE_APACHE\" /home/languagetool/api/apache_error.log | grep $YEAR | grep -c \"No buffer space available\"`" >>$OUTFILE

cat $OUTFILE | mail -a 'Content-Type: text/plain; charset=utf-8' -s "LanguageTool API Report" daniel.naber@languagetool.org
