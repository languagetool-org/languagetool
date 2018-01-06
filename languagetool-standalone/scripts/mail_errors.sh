#!/bin/sh
#dnaber, 2016-11-14

FIRSTPART=naber
LASTPART=danielnaber.de
TMPDATA=/tmp/lt_hourly_error_data.txt
LIMIT=0
DATE=`date +"%Y%m"`

cat log-[12]-${DATE}*_*.txt log-1.txt log-2.txt | grep "`date +"%Y-%m-%d %H:"`" | \
  egrep "An error has occurred" | \
  grep -v "Could not decode query" | \
  grep -v "URLDecoder: Illegal hex characters" | \
  grep -v "Missing 'language' parameter" | \
  grep -v "Text checking took longer than" | \
  grep -v "Missing 'text' or 'data' parameter" | \
  grep -v "is not a language code known" | \
  grep -v "Unsupported action: 'v2/check" | \
  grep -v "You specified 'preferredVariants' but" | \
  grep -v "Unsupported action: ''" | \
  grep -v "langCode cannot be empty or whitespace only" | \
  grep -v "Invalid format for 'preferredVariants'" | \
  grep -v "ErrorRateTooHighException" | \
  grep -v "code 413" >$TMPDATA
#cat log-1.txt log-2.txt | grep "`date +"%Y-%m-%d %H:"`" | egrep "An error has occurred" | grep -v "code 413" >$TMPDATA
MATCHES=`cat $TMPDATA | wc -l`

if [ "$MATCHES" -gt "$LIMIT" ]
then
  cat $TMPDATA | tail -n 100 | \
        ifne mail -a 'Content-Type: text/plain; charset=utf-8' -s "Hourly LT Error Report ($MATCHES total)" ${FIRSTPART}@${LASTPART}
fi
