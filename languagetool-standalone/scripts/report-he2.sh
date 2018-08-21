#!/bin/bash
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

echo "Daily LanguageTool API Report $DATE2 (HE2)" >$OUTFILE
echo "" >>$OUTFILE

grep --text -h "^$DATE2 " log-[0-9]-$DATE1*.txt log-1.txt log-2.txt | sort >$TMPFILE
cat log-[0-9]-$DATE1*.txt log-1.txt log-2.txt >$TMPFILE_ALL

echo "From" >>$OUTFILE
head -n 1 $TMPFILE >>$OUTFILE
echo "To" >>$OUTFILE
tail -n 1 $TMPFILE >>$OUTFILE
echo "" >>$OUTFILE
grep "Cache stats:" $TMPFILE | tail -n 1 >>$OUTFILE
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
echo "$DATE2;$TOTAL;-;$CHROME;$ANDROID;$CLIENT;$SUBLIME;-;$MSWORD;$WEBEXTFF;$WEBEXTCHROME;$TOTALHOME;$GOOGLEAPP" >>/home/languagetool/api/api-log.csv
cp /home/languagetool/api/api-log.csv /home/languagetool/data.languagetool.org/public/analytics/

echo "" >>$OUTFILE
ERROR_OOM=`grep -c 'OutOfMemoryError' $TMPFILE`
echo "OutOfMemoryError           : $ERROR_OOM" >>$OUTFILE
ERROR_TOO_MANY_PAR_REQ=`grep -c 'too many parallel requests' $TMPFILE`
echo "too many parallel requests : $ERROR_TOO_MANY_PAR_REQ" >>$OUTFILE
ERROR_INCOMPLETE_RES=`grep -c  "matches found so far" $TMPFILE`
echo "Incomplete results sent    : $ERROR_INCOMPLETE_RES" >>$OUTFILE
ERROR_RATE_TOO_HIGH=`grep -c  "ErrorRateTooHigh" $TMPFILE`
echo "ErrorRateTooHigh           : $ERROR_RATE_TOO_HIGH" >>$OUTFILE
ERROR_WARN=`grep -c  "WARN:" $TMPFILE`
echo "WARN                       : $ERROR_WARN" >>$OUTFILE
ERROR_TIMEOUT=`grep -c  "Text checking took longer than allowed maximum" $TMPFILE`
echo "Check timeout              : $ERROR_TIMEOUT" >>$OUTFILE

echo "" >>$OUTFILE
ERROR_SOME=`grep -c 'An error has occurred' $TMPFILE`
echo "An error has occurred      : $ERROR_SOME" >>$OUTFILE
ERROR_TOO_MANY_REQ=`grep -c 'too many requests' $TMPFILE`
echo "too many requests          : $ERROR_TOO_MANY_REQ" >>$OUTFILE
ERROR_TOO_MANY_REQ_ANDROID=`grep -c 'too many requests.*androidspell' $TMPFILE`
echo "too many requests (Android): $ERROR_TOO_MANY_REQ_ANDROID" >>$OUTFILE
ERROR_REQ_LIMIT=`grep -c 'Request limit of' $TMPFILE`
echo "Request limit              : $ERROR_REQ_LIMIT" >>$OUTFILE
ERROR_REQ_SIZE_LIMIT=`grep -c 'Request size limit of' $TMPFILE`
echo "Request size limit         : $ERROR_REQ_SIZE_LIMIT" >>$OUTFILE

echo "$DATE2;$ERROR_OOM;$ERROR_TOO_MANY_PAR_REQ;$ERROR_INCOMPLETE_RES;$ERROR_RATE_TOO_HIGH;$ERROR_WARN;$ERROR_SOME;$ERROR_TOO_MANY_REQ;$ERROR_TOO_MANY_REQ_ANDROID;$ERROR_REQ_LIMIT;$ERROR_REQ_SIZE_LIMIT;-;$ERROR_TIMEOUT" >>/home/languagetool/api/api-errors-log.csv
cp /home/languagetool/api/api-errors-log.csv /home/languagetool/data.languagetool.org/public/analytics/

echo "" >>$OUTFILE
echo "Top HTTP error codes:" >>$OUTFILE
grep "An error has occurred" /tmp/log.temp|sed 's/.*HTTP code \([0-9]\+\)..*/HTTP code \1/'|sort |uniq -c| sort -r -n >>$OUTFILE

echo "" >>$OUTFILE
echo "API deploy dates:" >>$OUTFILE
echo -n "languagetool.org        : " >>$OUTFILE
curl -s "https://languagetool.org/api/v2/check?text=Test&language=en" | json_pp | grep buildDate >>$OUTFILE
echo -n "api.languagetool.org    : " >>$OUTFILE
curl -s "https://api.languagetool.org/v2/check?text=Test&language=en" | json_pp | grep buildDate >>$OUTFILE
echo -n "languagetoolplus.com    : " >>$OUTFILE
curl -s "https://languagetoolplus.com/api/v2/check?text=Test&language=en" | json_pp | grep buildDate >>$OUTFILE
echo -n "api.languagetoolplus.com: " >>$OUTFILE
curl -s "https://api.languagetoolplus.com/v2/check?text=Test&language=en" | json_pp | grep buildDate >>$OUTFILE
echo "TODO: premium only on api.languagetoolplus.com" >>$OUTFILE

echo "" >>$OUTFILE
echo "Top API blocks:" >>$OUTFILE
grep "too many requests" $TMPFILE | cut -c 21-59 | sort | uniq -c | sort -r -n | head -n 10  >>$OUTFILE

echo "" >>$OUTFILE
echo "Top 10 Errors:" >>$OUTFILE
grep 'Could not check sentence' $TMPFILE_ALL | grep -v "Caused by:" | uniq -c | sort -n -r | head -n 10 >>$OUTFILE

echo "" >>$OUTFILE
echo "Top 50 external Referers:" >>$OUTFILE
grep "Check done:" /tmp/log.temp | awk -F ', ' '{print $4}' | grep -v "languagetool.org" | cut -c -100 | sed 's#https\?://\([.a-z0-9:-]\+\)/.*#\1#' | sort | uniq -c | sort -r -n | head -n 50 >>$OUTFILE

#echo "" >>$OUTFILE
#echo "Up to 50 client errors sent to the server:" >>$OUTFILE
#grep "Log message from client:" $TMPFILE | head -n 50 >>$OUTFILE
#echo "Total client errors: `grep -c "Log message from client:" $TMPFILE`" >>$OUTFILE

echo "" >>$OUTFILE
echo "Client errors:" >>$OUTFILE
TMPFILE_ALL=/tmp/log-all-client-errors.temp

cat log-[12].txt log-[0-9]-$DATE1*.txt log-1.txt log-2.txt | grep "$DATE2" | grep "Log message from client:" >$TMPFILE_ALL

echo -n "siteNotSupported: " >>$OUTFILE
grep -c "siteNotSupported" $TMPFILE_ALL >>$OUTFILE

echo -n "freshInstallReload: " >>$OUTFILE
grep -c "freshInstallReload" $TMPFILE_ALL >>$OUTFILE

echo -n "Exception and failing fallback in checkText: " >>$OUTFILE
grep -c "Exception and failing fallback in checkText:" $TMPFILE_ALL >>$OUTFILE

echo -n "couldNotCheckText: " >>$OUTFILE
grep -c "couldNotCheckText" $TMPFILE_ALL >>$OUTFILE



DATE_APACHE=`LANG=C date +"%a %b %d"`
YEAR=`date +"%Y"`
# note: requires a root cronjob to copy the error.log file to ~/api/apache_error.log:
echo "" >>$OUTFILE
echo "Apache errors (max. 30):" >>$OUTFILE
grep "$DATE_APACHE" /home/languagetool/api/apache_error.log | grep -v "log.php" | grep $YEAR | tail -n 30 >>$OUTFILE

echo "" >>$OUTFILE
echo "Apache not found errors (filtered, max. 10):" >>$OUTFILE
grep "$DATE_APACHE" /home/languagetool/api/apache_not_found.log | grep $YEAR | tail -n 10 >>$OUTFILE

echo "" >>$OUTFILE
echo -n "Number of client-side errors: " >>$OUTFILE
grep "$DATE_APACHE" /home/languagetool/api/apache_error.log | grep -c $YEAR >>$OUTFILE

cat $OUTFILE | mail -a 'Content-Type: text/plain; charset=utf-8' -s "LanguageTool API Report (HE2)" report@languagetool.org
