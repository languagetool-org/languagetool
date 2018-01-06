#!/bin/bash
# Daily API usage reports. To be run as a cronjob at the end of the day.
# dnaber, 2017-12-15

LANG="de_DE.UTF-8"
DATE=`date +"%Y-%m-%d"`
if [ "$#" -eq 1 ]; then
  DATE=$1
fi

# for testing:
#DATE=2016-03-31
INPFILE=/home/ubuntu/lt/log.txt
TMPFILE=/home/ubuntu/lt/log.txt.tmp
OUTFILE=/tmp/statusmail.txt
HOST=`hostname`

echo "$HOST Daily LanguageTool API Report $DATE" >$OUTFILE
echo "" >>$OUTFILE

grep --text -h "^$DATE " $INPFILE | sort >$TMPFILE

echo "From" >>$OUTFILE
head -n 1 $TMPFILE >>$OUTFILE
echo "To" >>$OUTFILE
tail -n 1 $TMPFILE >>$OUTFILE
echo "" >>$OUTFILE

TOTAL=`grep -c "Check done:" $TMPFILE`
printf "Total text checks : %'d\n" $TOTAL >>$OUTFILE

TOTALHOME=`grep "Check done:" $TMPFILE | grep -c languagetool.org`
printf "Checks from lt.org: %'d\n" $TOTALHOME >>$OUTFILE

CHROME=`grep -c "chrome-extension" $TMPFILE`
printf "Chrome Requests   : %'d\n" $CHROME >>$OUTFILE

WEBEXTFF=`grep -c "webextension-firefox" $TMPFILE`
printf "WebExtension FF   : %'d\n" $WEBEXTFF >>$OUTFILE

WEBEXTCHROME=`grep -c "webextension-chrome" $TMPFILE`
printf "WebExtension Chr. : %'d\n" $WEBEXTCHROME >>$OUTFILE

ANDROID=`grep -c "androidspell" $TMPFILE`
printf "Android Requests  : %'d\n" $ANDROID >>$OUTFILE

CLIENT=`grep -c "java-http-client" $TMPFILE`
printf "Java Client Req   : %'d\n" $CLIENT >>$OUTFILE

GOOGLEAPP=`grep -c ":googledocs" $TMPFILE`
printf "Google Docs       : %'d\n" $GOOGLEAPP >>$OUTFILE

SUBLIME=`grep -c ":sublime" $TMPFILE`
printf "Sublime Requests  : %'d\n" $SUBLIME >>$OUTFILE

MSWORD=`grep -c ":msword" $TMPFILE`
printf "MS-Word Requests  : %'d\n" $MSWORD >>$OUTFILE

# when adding items, add only at the end so scripts don't get confused:
echo "$DATE;$TOTAL;$FF;$CHROME;$ANDROID;$CLIENT;$SUBLIME;$WEBEXT;$MSWORD;$WEBEXTFF;$WEBEXTCHROME;$TOTALHOME;$GOOGLEAPP" >>/home/ubuntu/lt/api-log.csv

echo "" >>$OUTFILE
echo "OutOfMemoryError           : `grep -c 'OutOfMemoryError' $TMPFILE`" >>$OUTFILE
echo "too many parallel requests : `grep -c 'too many parallel requests' $TMPFILE`" >>$OUTFILE
echo "Incomplete results sent    : `grep -c  "matches found so far" $TMPFILE`" >>$OUTFILE
echo "ErrorRateTooHigh           : `grep -c  "ErrorRateTooHigh" $TMPFILE`" >>$OUTFILE

echo "" >>$OUTFILE
echo "An error has occurred      : `grep -c 'An error has occurred' $TMPFILE`" >>$OUTFILE
echo "too many requests          : `grep -c 'too many requests' $TMPFILE`" >>$OUTFILE
echo "too many requests (Android): `grep -c 'too many requests.*androidspell' $TMPFILE`" >>$OUTFILE

echo "Request limit              : `grep -c 'Request limit of' $TMPFILE`" >>$OUTFILE
echo "Request size limit         : `grep -c 'Request size limit of' $TMPFILE`" >>$OUTFILE

echo "" >>$OUTFILE
echo "Top API blocks:" >>$OUTFILE
grep "too many requests" $TMPFILE | cut -c 21-59 | sort | uniq -c | sort -r -n | head -n 10  >>$OUTFILE

#echo "" >>$OUTFILE
#echo "Top 10 external Referers:" >>$OUTFILE
#grep "Check done:" /tmp/log.temp | awk -F ', ' '{print $4}' | grep -v "languagetool.org" | cut -c -100 | sed 's#https\?://\([.a-z0-9:-]\+\)/.*#\1#' | sort | uniq -c | sort -r -n | head -n 10 >>$OUTFILE

cat $OUTFILE
