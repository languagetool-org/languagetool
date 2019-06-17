#!/bin/bash
CONFUSION_SET=$1
DISABLE=$2
OUT=$3

TMP_WITHOUT=/tmp/confusion_set_without_disabled.txt
fgrep -v -f $DISABLE $CONFUSION_SET >$TMP_WITHOUT

TMP_COMMENTED=/tmp/disabled_commented_out.txt
sed <$DISABLE "s/^/#/" >$TMP_COMMENTED

cat $TMP_WITHOUT $TMP_COMMENTED >$OUT
