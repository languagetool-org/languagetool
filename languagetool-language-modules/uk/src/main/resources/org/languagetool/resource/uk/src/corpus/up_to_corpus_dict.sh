#!/bin/sh
#
# Script to convert LT's pos tag dictionary to corpus format
#
sort -k 2,2 -k 3,3 -k 1,1 -t ' ' | uniq | sed -r "/advp/ s/(.*) .* (advp.*)/\1 \1 \2/" |\
 sed -r "/verb:inf/ s/(.*)ти((ся)? .* verb:inf.*)/\0\n\1ть\2:coll/" |\
 sed -r "/ noun/ s/:p:nv/\0:ns/" |\
 sed -r "/ (noun|adj|verb|pron)/ s/:[mnf]:/:s\0/" |\
 awk '/ noun/ && !/:anim/ { $0 = $0 ":inanim" } {print $0}'
# sed -r "/^noun/{ /:anim/! s/.*/\0:inanim/ }"
