BEGIN {IGNORECASE=1}
!/^(brother A:|sister A:|feces N:|axes\tN|bases\tN|phenomena[ \t]N[:]*|automata\tN|cherubim[ \t]N[:]*|\
apices\tN|cervices\tN|indices\tN|matrices\tN|vortices\tN|\
elves\tN|halves\tN|hooves\tN|knives\tN|leaves\tN|lives\tN|loaves\tN|\
scarves\tN|selves\tN|sheaves\tN|shelves\tN|wives\tN|wolves\tN|geese\tN|\
lice\tN|mice\tN|children\tN|corpora[ \t]N[:]*|genera\tN|foci\tN|fungi\tN|\
nuclei\tN|radii\tN|stimuli\tN|syllabi\tN|bacteria[ \t]N[:]*|data\tN|errata[ \t]N[:]*|\
media[ \t]N[:]*|memoranda[ \t]N[:]*|ova\tN|strata[ \t]N[:]*|oxen[ \t]N[:]*|calves\tN|feet\tN|\
women\tN|men\tN|well A:|who[ \t]N[:]*|an A:|an[ \t]DCN|themselves N:|\
a[ \t]DNVP|He[ \t]N[:]*|me[ \t]N[:]*|he[ \t]N[:]*|I[ \t]N[:]*|this[ \t]N[:]*|\
that[ \t][AN][:]*|such[ \t]N[:]*|his[ \t]N[:]*|t[ \t]N[:]*|she[ \t]N[:]*|\
you[ \t]N[:]*|we[ \t]N[:]*|they[ \t]N[:]*|them[ \t]N[:]*|him[ \t]N[:]*|\
her[ \t]N[:]*|your[ \t]N[:]*|my[ \t]N[:]*|herself[ \t]N[:]*|\
himself[ \t]N[:]*|it[ \t]N[:]*|itself[ \t]N[:]*|myself[ \t]N[:]*|\
oneself[ \t]N[:]*|our[ \t]N[:]*|our[ \t]A[:]*|their[ \t]|\
yourself[ \t]N[:]*|ourself[ \t]N[:]*|another[ \t]N[:]*|and[ \t]N[:]*|\
any[ \t]A[:]*|all[ \t]A[:]*|an[ \t]N[:]*|each[ \t]A[:]*|either[ \t]A[:]*|\
every[ \t]A[:]*|half[ \t]A[:]*|many[ \t]N[:]*|many[ \t]A[:]*|\
much[ \t]N[:]*|neither[ \t]A[:]*|no[ \t]A[:]*|some[ \t]N[:]*|\
some[ \t]A[:]*|such[ \t]A[:]*|those[ \t]N[:]*|those[ \t]A[:]*|\
these[ \t]N[:]*|those[ \t]A[:]*|and[ \t]N\?[:]*|within[ \t]|this[ \t]A[:]*|\
into[ \t]N[:]*|what[ \t][NA][:]*|whom[ \t][NA][:]*|whose[ \t][NA][:]*|\
which[ \t]N[:]*|which[ \t]A[:]*|there[ \t]|will[ \t]V[:]*|\
can[ \t]V[:]*|may[ \t]V[:]*|shall[ \t]V[:]*|ought[ \t]V[:]*|must[ \t]V[:]*|\
could[ \t]V[:]*|would[ \t]V[:]*|inter[ \t]A[:]*|school[ \t]A[:]*|\
the[ \t]A[:]*|mod[ \t]A:)/ && !/'/