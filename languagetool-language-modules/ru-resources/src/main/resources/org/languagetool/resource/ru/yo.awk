BEGIN {FS="\t"}
{if ($1~/.*[ё].*/) {print $0}}
