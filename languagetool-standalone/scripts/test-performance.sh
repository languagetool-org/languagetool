#!/bin/bash

DATADIR=/home/languagetool/languagetool.org/languagetool-website/www/regression-tests
sleep $[ ( $RANDOM % 5 )  + 1 ]s

RUNTIME=`/usr/bin/time --format="%E" curl -s --max-time 30 "https://languagetool.org/api/v2/check?language=en-US&text=my+texd" >/dev/null 2>/tmp/runtime.log`
RUNTIME=`cat /tmp/runtime.log | sed s/0://`
DATE=`date +"%Y-%m-%d %H:%M:%S"`
echo "$DATE;$RUNTIME" >>$DATADIR/performance-api.log
# 8640 = 288 checks per day * 30 days:
tail -n 8640 $DATADIR/performance-api.log >$DATADIR/performance-api-recent.log
/home/languagetool/api/perf-plot-api.pg >$DATADIR/performance-api.png

RUNTIME=`/usr/bin/time --format="%E" curl -s --max-time 30 "https://languagetoolplus.com/api/v2/check?language=en-US&text=my+texd" >/dev/null 2>/tmp/runtime-ltp.log`
RUNTIME=`cat /tmp/runtime-ltp.log | sed s/0://`
DATE=`date +"%Y-%m-%d %H:%M:%S"`
echo "$DATE;$RUNTIME" >>$DATADIR/performance-ltp.log
tail -n 8640 $DATADIR/performance-ltp.log >$DATADIR/performance-ltp-recent.log

RUNTIME=`/usr/bin/time --format="%E" curl -s --max-time 30 "https://api.languagetool.org/v2/check?language=en-US&text=my+texd" >/dev/null 2>/tmp/runtime-cloud.log`
RUNTIME=`cat /tmp/runtime-cloud.log | sed s/0://`
DATE=`date +"%Y-%m-%d %H:%M:%S"`
echo "$DATE;$RUNTIME" >>$DATADIR/performance-api-cloud.log
# 8640 = 288 checks per day * 30 days:
tail -n 8640 $DATADIR/performance-api-cloud.log >$DATADIR/performance-api-recent2.log
