#!/bin/bash
IN=$1
OUT=$2
grep "^Message" ${IN} | sed -r "s/^.* (\w+)\/(\w+)$/\1; \2/" | grep -v "^Message" | sort -d | uniq -c | tr -s ' ' | sort -n -k1 >${OUT}
