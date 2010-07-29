BEGIN {FS="\t"}
{print $2"|"$3"\t"$1}
