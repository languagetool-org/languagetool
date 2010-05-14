# This script coverts data in the format:
# inflected_formHTlexemeHTtags
# (where HT is the horizontal tabulation)
# to the form:
# inflected_form+Kending+tags
# where '+' is a separator, K is a character that specifies how many characters
# should be deleted from the end of the inflected form to produce the lexeme
# by concatenated the stripped string with the ending.
#
# Written by Jan Daciuk <jandac@pg.gda.pl>, 1997
#
function common_prefix(s1, s2, n,  i)
{
  for (i = 1; i <= n; i++)
    if (substr(s1, i, 1) != substr(s2, i, 1))
      return i - 1;
  return n;
}

BEGIN {separator = "*"}
{
  l1 = length($1);
  if ((prefix = common_prefix($1, $2, l1))) {
    printf "%s%c%c%s%c%s", $1, separator,
      (l1 - prefix + 65), substr($2, prefix + 1),
      separator, $3;
  }
  else {
    printf "%s%c%c%s%c%s", $1, separator, 65 + l1, $2, separator, $3;
  }
# Delete the following (1) line if your tags do not contain spaces
# and you would like to append comments at the end of lines
  for (i = 4; i <= NF; i++) printf " %s", $i;
# Do not delete this
  printf "\n";
}


		       
