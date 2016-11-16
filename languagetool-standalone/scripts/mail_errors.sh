#!/bin/sh
#dnaber, 2016-11-14
# To be called hourly, at the end of the hour.

FIRSTPART=naber
LASTPART=danielnaber.de
TMPDATA=/tmp/lt_hourly_error_data.txt
LIMIT=10

tail -n 50000 log-1.txt log-2.txt | grep "`date +"%Y-%m-%d %H:"`" | egrep "An error has occurred" | grep -v "code 413" >$TMPDATA
MATCHES=`cat $TMPDATA | wc -l`

if [ "$MATCHES" -gt "$LIMIT" ]
then
  cat $TMPDATA | tail -n 100 | \
        ifne mail -a 'Content-Type: text/plain; charset=utf-8' -s "Hourly LT Error Report ($MATCHES total)" ${FIRSTPART}@${LASTPART}
fi
