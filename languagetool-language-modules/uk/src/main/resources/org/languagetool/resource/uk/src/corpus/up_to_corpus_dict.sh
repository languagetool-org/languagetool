#!/bin/sh
#
# Script to convert LT's pos tag dictionary to corpus format
#
 sed -r "/advp/ s/(.*) .* (advp.*)/\1 \1 \2/" |\
 sed -r "/сь advp/ s/(.*)сь .* (advp.*)/\0\n\1ся \1сь \2:coll/" |\
 sed -r "/verb:inf/ s/(.*)ти((ся)? .* verb:inf.*)/\0\n\1ть\2:coll/" |\
 sed -r "/ noun/ s/:p:nv/\0:ns/" |\
 sed -r "/ (noun|adj|verb|pron)/ s/:[mnf]:/:s\0/" |\
 awk '/ noun/ && !/:anim/ { $0 = $0 ":inanim" } {print $0}' |\
 awk -r '{ print $2,$1,$3 }' |\
sed -r "/verb/ s/(verb)(.*)(:(im)?perf)(.*)/\1\3\2\5/" |\
sed -r "/noun/ s/(noun)(.*)(:(in)?anim)(.*)/\1\3\2\5/" |\
sort -k 1,1 -k 3,3 -k 2,2 -t ' ' | uniq
#sed -r "/verb/ s/(verb)(:(?:im)?perf)(.*)/\1\3\2" |\
#sed -r "/noun/ s/(noun)(:(?:in)?anim)(.*)/\1\3\2"

# sed -r "/^noun/{ /:anim/! s/.*/\0:inanim/ }"
