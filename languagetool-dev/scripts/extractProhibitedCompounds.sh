#!/bin/bash
# transforms log by AutomaticProhibitedCompoundEvaluator into confusion set format for use by ProhibitedCompoundRule
LOG=$1
OUT=$2

cat $1 | fgrep "=>" | sed "s/=> //" | tr -s ' ' | sort -d >${OUT}
